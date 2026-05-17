// AI 聊天应用主脚本
class ChatApp {
    constructor() {
        this.currentChatId = null;
        this.chats = new Map();
        this.selectedFiles = [];
        this.md = window.markdownit({
            html: true,
            linkify: true,
            typographer: true,
            breaks: true
        });

        this.init();
    }

    init() {
        this.bindEvents();
        this.loadChatsFromStorage();
        this.showNotification('欢迎使用 AI 智能助手！', 'info');
    }

    bindEvents() {
        // 新建会话
        document.getElementById('new-chat-btn').addEventListener('click', () => this.createNewChat());

        // 发送消息
        document.getElementById('send-btn').addEventListener('click', () => this.sendMessage());

        // 输入框事件
        const messageInput = document.getElementById('message-input');
        messageInput.addEventListener('keydown', (e) => this.handleInputKeydown(e));
        messageInput.addEventListener('input', () => this.autoResizeInput());

        // 文件上传
        document.getElementById('upload-file-btn').addEventListener('click', () => {
            document.getElementById('file-input').click();
        });
        document.getElementById('file-input').addEventListener('change', (e) => this.handleFileSelect(e));
        document.getElementById('clear-files-btn').addEventListener('click', () => this.clearFiles());

        // 拖拽上传
        this.setupDragAndDrop();
    }

    // 创建新会话
    async createNewChat() {
        try {
            // 请求后端创建新会话
            const response = await fetch('/api/chat/session/create', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    title: '新会话 ' + (this.chats.size + 1)
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();

            // 检查响应 code，0 表示成功
            if (result.code === 0) {
                const chat = result.data;
                // 保存到本地
                this.chats.set(chat.id, chat);
                this.switchChat(chat.id);
                this.renderChatHistory();
                this.showNotification('已创建新会话', 'success');
            } else {
                this.showNotification(result.msg || '创建会话失败', 'error');
            }
        } catch (error) {
            console.error('创建会话失败:', error);
            this.showNotification('创建会话失败，请稍后重试', 'error');
        }
    }

    // 切换会话
    switchChat(chatId) {
        this.currentChatId = chatId;
        this.renderChatHistory();
        this.renderMessages();
    }

    // 删除会话
    async deleteChat(chatId, event) {
        event.stopPropagation();

        if (!confirm('确定要删除这个会话吗？')) {
            return;
        }

        try {
            // 使用 RESTful 风格删除会话
            const response = await fetch(`/api/chat/session/${chatId}`, {
                method: 'DELETE'
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();

            // 检查响应 code，0 表示成功
            if (result.code === 0) {
                // 从本地删除
                this.chats.delete(chatId);

                if (this.currentChatId === chatId) {
                    if (this.chats.size > 0) {
                        this.switchChat(Array.from(this.chats.keys())[0]);
                    } else {
                        this.currentChatId = null;
                        this.renderMessages();
                    }
                }

                this.renderChatHistory();
                this.showNotification('会话已删除', 'success');
            } else {
                this.showNotification(result.msg || '删除会话失败', 'error');
            }
        } catch (error) {
            console.error('删除会话失败:', error);
            this.showNotification('删除会话失败，请稍后重试', 'error');
        }
    }

    // 渲染聊天历史列表
    renderChatHistory() {
        const container = document.getElementById('chat-history');
        container.innerHTML = '';

        this.chats.forEach(chat => {
            const chatItem = document.createElement('div');
            chatItem.className = `chat-item ${chat.id === this.currentChatId ? 'active' : ''}`;

            // 单击切换会话
            chatItem.onclick = () => this.switchChat(chat.id);

            // 双击加载会话内容
            chatItem.ondblclick = (e) => {
                e.preventDefault();
                this.loadChatContent(chat.id);
            };

            // 显示标题和时间
            const timeText = this.formatRelativeTime(chat.timestamp || chat.createdAt);
            chatItem.innerHTML = `
                <div class="chat-item-content">
                    <span class="chat-item-title">${this.escapeHtml(chat.title)}</span>
                    <span class="chat-item-time">${timeText}</span>
                </div>
                <button class="chat-item-delete" title="删除会话">
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                        <path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/>
                    </svg>
                </button>
            `;

            chatItem.querySelector('.chat-item-delete').addEventListener('click', (e) => {
                this.deleteChat(chat.id, e);
            });

            container.appendChild(chatItem);
        });
    }

    // 渲染消息列表
    renderMessages() {
        const container = document.getElementById('chat-messages');
        container.innerHTML = '';

        if (!this.currentChatId || !this.chats.has(this.currentChatId)) {
            container.innerHTML = `
                <div class="welcome-message">
                    <h2>👋 欢迎使用 AI 智能助手</h2>
                    <p>我可以帮您解答问题、编写代码、分析数据等。请随时向我提问！</p>
                </div>
            `;
            return;
        }

        const chat = this.chats.get(this.currentChatId);
        if (chat.messages.length === 0) {
            container.innerHTML = `
                <div class="welcome-message">
                    <h2>💬 开始新的对话</h2>
                    <p>发送一条消息开始聊天吧！</p>
                </div>
            `;
            return;
        }

        chat.messages.forEach(msg => {
            this.appendMessage(msg, false);
        });

        this.scrollToBottom();
    }

    // 添加消息到界面
    appendMessage(message, animate = true) {
        const container = document.getElementById('chat-messages');
        const messageEl = document.createElement('div');
        messageEl.className = `message ${message.role}`;
        if (!animate) {
            messageEl.style.animation = 'none';
        }

        const time = new Date(message.timestamp).toLocaleString('zh-CN');
        const avatar = message.role === 'user' ? '👤' : '🤖';
        const content = message.role === 'user'
            ? this.escapeHtml(message.content)
            : this.md.render(message.content);

        messageEl.innerHTML = `
            <div class="message-avatar">${avatar}</div>
            <div class="message-content">
                <div class="message-bubble">${content}</div>
                <div class="message-time">${time}</div>
            </div>
        `;

        container.appendChild(messageEl);
        this.scrollToBottom();
    }

    // 流式输出消息（AI响应）
    appendStreamingMessage(content) {
        const container = document.getElementById('chat-messages');
        let messageEl = container.querySelector('.message.assistant.streaming');

        if (!messageEl) {
            messageEl = document.createElement('div');
            messageEl.className = 'message assistant streaming';

            const time = new Date().toLocaleString('zh-CN');
            messageEl.innerHTML = `
                <div class="message-avatar">🤖</div>
                <div class="message-content">
                    <div class="message-bubble"></div>
                    <div class="message-time">${time}</div>
                </div>
            `;
            container.appendChild(messageEl);
        }

        const bubble = messageEl.querySelector('.message-bubble');
        bubble.innerHTML = this.md.render(content);
        this.scrollToBottom();

        return messageEl;
    }

    // 发送消息
    async sendMessage() {
        const input = document.getElementById('message-input');
        const content = input.value.trim();

        if (!content && this.selectedFiles.length === 0) {
            this.showNotification('请输入消息内容或上传文件', 'warning');
            return;
        }

        if (!this.currentChatId) {
            this.createNewChat();
        }

        // 构建消息内容
        let messageContent = content;
        if (this.selectedFiles.length > 0) {
            messageContent += '\n\n[文件附件]';
        }

        // 创建用户消息
        const userMessage = {
            role: 'user',
            content: messageContent,
            timestamp: new Date().toISOString(),
            files: this.selectedFiles.map(f => f.name)
        };

        // 添加到会话
        const chat = this.chats.get(this.currentChatId);
        chat.messages.push(userMessage);

        // 更新会话标题
        if (chat.messages.length === 1) {
            chat.title = content.substring(0, 30) + (content.length > 30 ? '...' : '');
            this.renderChatHistory();
        }

        this.appendMessage(userMessage);

        // 清空输入
        input.value = '';
        this.autoResizeInput();
        this.clearFiles();

        // 发送到服务器
        await this.sendToServer(userMessage);
    }

    // 发送到服务器
    async sendToServer(message) {
        const sendBtn = document.getElementById('send-btn');
        sendBtn.disabled = true;

        try {
            // 构建请求数据
            const requestData = {
                chatId: this.currentChatId,
                message: message.content,
                files: this.selectedFiles.map(f => ({
                    name: f.name,
                    type: f.type,
                    size: f.size
                }))
            };

            // 如果有文件，使用 FormData
            let response;
            if (this.selectedFiles.length > 0) {
                const formData = new FormData();
                formData.append('chatId', this.currentChatId);
                formData.append('message', message.content);
                this.selectedFiles.forEach(file => {
                    formData.append('files', file);
                });

                response = await fetch('/api/chat/message/send', {
                    method: 'POST',
                    body: formData
                });
            } else {
                response = await fetch('/api/chat/message/send', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(requestData)
                });
            }

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            // 处理流式响应
            const reader = response.body.getReader();
            const decoder = new TextDecoder('utf-8');
            let aiContent = '';

            while (true) {
                const {done, value} = await reader.read();
                if (done) break;

                const chunk = decoder.decode(value, {stream: true});
                aiContent += chunk;
                this.appendStreamingMessage(aiContent);
            }

            // 保存AI响应
            const chat = this.chats.get(this.currentChatId);
            chat.messages.push({
                role: 'assistant',
                content: aiContent,
                timestamp: new Date().toISOString()
            });

            // 移除streaming标记
            const streamingMsg = document.querySelector('.message.assistant.streaming');
            if (streamingMsg) {
                streamingMsg.classList.remove('streaming');
            }

        } catch (error) {
            console.error('发送消息失败:', error);
            this.showNotification('发送消息失败，请稍后重试', 'error');

            // 添加错误消息
            const chat = this.chats.get(this.currentChatId);
            chat.messages.push({
                role: 'assistant',
                content: '抱歉，处理您的请求时出现错误。请稍后重试。',
                timestamp: new Date().toISOString()
            });
            this.appendMessage(chat.messages[chat.messages.length - 1]);
        } finally {
            sendBtn.disabled = false;
        }
    }

    // 处理输入框按键
    handleInputKeydown(event) {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();
            this.sendMessage();
        }
    }

    // 自动调整输入框高度
    autoResizeInput() {
        const input = document.getElementById('message-input');
        input.style.height = 'auto';
        input.style.height = Math.min(input.scrollHeight, 200) + 'px';
    }

    // 文件选择处理
    handleFileSelect(event) {
        const files = Array.from(event.target.files);
        this.addFiles(files);
        event.target.value = ''; // 重置input
    }

    // 添加文件
    addFiles(files) {
        files.forEach(file => {
            if (!this.selectedFiles.some(f => f.name === file.name && f.size === file.size)) {
                this.selectedFiles.push(file);
            }
        });
        this.renderFilePreview();
        this.showNotification(`已添加 ${files.length} 个文件`, 'success');
    }

    // 渲染文件预览
    renderFilePreview() {
        const preview = document.getElementById('file-preview');
        const fileList = document.getElementById('file-list');

        if (this.selectedFiles.length === 0) {
            preview.style.display = 'none';
            return;
        }

        preview.style.display = 'block';
        fileList.innerHTML = '';

        this.selectedFiles.forEach((file, index) => {
            const fileItem = document.createElement('div');
            fileItem.className = 'file-item';
            fileItem.innerHTML = `
                <span class="file-item-name">${this.escapeHtml(file.name)}</span>
                <span style="color: #999; font-size: 12px;">(${this.formatFileSize(file.size)})</span>
                <button class="file-item-remove" title="移除文件">✕</button>
            `;

            fileItem.querySelector('.file-item-remove').addEventListener('click', () => {
                this.selectedFiles.splice(index, 1);
                this.renderFilePreview();
            });

            fileList.appendChild(fileItem);
        });
    }

    // 清空文件
    clearFiles() {
        this.selectedFiles = [];
        this.renderFilePreview();
    }

    // 设置拖拽上传
    setupDragAndDrop() {
        const overlay = document.getElementById('drop-overlay');
        let dragCounter = 0;

        document.addEventListener('dragenter', (e) => {
            e.preventDefault();
            dragCounter++;
            overlay.style.display = 'flex';
        });

        document.addEventListener('dragleave', (e) => {
            e.preventDefault();
            dragCounter--;
            if (dragCounter === 0) {
                overlay.style.display = 'none';
            }
        });

        document.addEventListener('dragover', (e) => {
            e.preventDefault();
        });

        document.addEventListener('drop', (e) => {
            e.preventDefault();
            dragCounter = 0;
            overlay.style.display = 'none';

            const files = Array.from(e.dataTransfer.files);
            this.addFiles(files);
        });
    }

    // 显示通知
    showNotification(message, type = 'info') {
        const container = document.getElementById('notification-container');
        const notification = document.createElement('div');
        notification.className = `notification ${type}`;
        notification.textContent = message;

        container.appendChild(notification);

        // 3秒后自动消失
        setTimeout(() => {
            notification.style.animation = 'fadeOut 0.3s ease-out';
            setTimeout(() => {
                container.removeChild(notification);
            }, 300);
        }, 3000);
    }

    // 统一的 API 响应处理
    async handleApiResponse(response) {
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const result = await response.json();

        // code 为 0 表示成功，其它均失败
        if (result.code === 0) {
            return result.data;
        } else {
            throw new Error(result.msg || '请求失败');
        }
    }

    // 滚动到底部
    scrollToBottom() {
        const container = document.getElementById('chat-messages');
        container.scrollTop = container.scrollHeight;
    }

    // 保存到本地存储（已禁用，仅保留方法避免报错）
    saveChatsToStorage() {
        // 不再使用 localStorage，所有数据从服务端获取
    }

    // 从服务端加载会话列表
    async loadChatsFromStorage() {
        try {
            // 从后端加载会话列表
            const response = await fetch('/api/chat/session/list', {
                method: 'GET'
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();

            // 检查响应 code，0 表示成功
            if (result.code === 0) {
                const historyList = result.data?.history || [];

                // 转换历史数据格式
                this.chats = new Map();
                historyList.forEach(item => {
                    this.chats.set(item.id, {
                        id: item.id,
                        title: item.title || '未命名会话',
                        messages: [],
                        timestamp: item.timestamp,
                        createdAt: item.timestamp
                    });
                });

                if (this.chats.size > 0) {
                    this.switchChat(Array.from(this.chats.keys())[0]);
                }
                this.renderChatHistory();
            } else {
                console.warn('服务端返回错误:', result.msg);
                this.showNotification(result.msg || '加载会话失败', 'error');
            }
        } catch (error) {
            console.error('从服务端加载会话失败:', error);
            this.showNotification('加载会话失败，请检查网络连接', 'error');
        }
    }

    // 加载会话内容（双击时调用）
    async loadChatContent(chatId) {
        try {
            // 请求后端获取会话详情
            const response = await fetch(`/api/chat/session/${chatId}/content`, {
                method: 'GET'
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();

            // 检查响应 code，0 表示成功
            if (result.code === 0) {
                const chatData = result.data;

                // 更新本地会话数据
                if (this.chats.has(chatId)) {
                    const chat = this.chats.get(chatId);
                    chat.messages = chatData.messages || [];
                    this.switchChat(chatId);
                    this.showNotification('会话内容已加载', 'success');
                }
            } else {
                this.showNotification(result.msg || '加载会话内容失败', 'error');
            }
        } catch (error) {
            console.error('加载会话内容失败:', error);
            this.showNotification('加载会话内容失败，请稍后重试', 'error');
        }
    }

    // 工具方法：转义HTML
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    // 工具方法：格式化文件大小
    formatFileSize(bytes) {
        if (bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
    }

    // 工具方法：格式化相对时间
    formatRelativeTime(timestamp) {
        if (!timestamp) return '';

        const now = new Date();
        const date = new Date(timestamp);
        const diffMs = now - date;
        const diffSeconds = Math.floor(diffMs / 1000);
        const diffMinutes = Math.floor(diffSeconds / 60);
        const diffHours = Math.floor(diffMinutes / 60);
        const diffDays = Math.floor(diffHours / 24);

        // 1分钟内
        if (diffSeconds < 60) {
            return '刚刚';
        }

        // 1小时内
        if (diffMinutes < 60) {
            return `${diffMinutes}分钟前`;
        }

        // 今天
        if (diffHours < 24) {
            return `${diffHours}小时前`;
        }

        // 昨天
        if (diffDays === 1) {
            const hour = date.getHours().toString().padStart(2, '0');
            const minute = date.getMinutes().toString().padStart(2, '0');
            return `昨天 ${hour}:${minute}`;
        }

        // 前天
        if (diffDays === 2) {
            const hour = date.getHours().toString().padStart(2, '0');
            const minute = date.getMinutes().toString().padStart(2, '0');
            return `前天 ${hour}:${minute}`;
        }

        // 本周
        if (diffDays < 7) {
            const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
            const hour = date.getHours().toString().padStart(2, '0');
            const minute = date.getMinutes().toString().padStart(2, '0');
            return `${days[date.getDay()]} ${hour}:${minute}`;
        }

        // 更早
        const year = date.getFullYear();
        const month = (date.getMonth() + 1).toString().padStart(2, '0');
        const day = date.getDate().toString().padStart(2, '0');
        const hour = date.getHours().toString().padStart(2, '0');
        const minute = date.getMinutes().toString().padStart(2, '0');

        // 同一年不显示年份
        if (year === now.getFullYear()) {
            return `${month}-${day} ${hour}:${minute}`;
        }

        return `${year}-${month}-${day} ${hour}:${minute}`;
    }
}

// 初始化应用
document.addEventListener('DOMContentLoaded', () => {
    window.chatApp = new ChatApp();
});
