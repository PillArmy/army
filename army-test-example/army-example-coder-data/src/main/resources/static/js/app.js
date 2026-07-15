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

    async init() {
        this.bindEvents();

        // 1. 先请求会话列表
        await this.loadChatList();

        // 2. 若会话列表不为空，请求当前会话 ID
        if (this.chats.size > 0) {
            const currentConversationId = await this.getCurrentConversationId();

            // 3. 若 conversationId 不为 null，切换到相应会话并加载消息
            if (currentConversationId) {
                await this.loadConversationMessages(currentConversationId);
            }
            // 若为 null，保持 empty-chat 状态（默认状态）
        }
        // 若会话列表为空，保持 empty-chat 状态（默认状态）
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

    // 加载会话列表
    async loadChatList() {
        try {
            const response = await fetch(API_CONVERSATION_LIST, {
                method: 'GET'
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();
            console.log('loadChatList - 响应:', result);

            if (result.code === 0) {
                const conversationList = result.data?.list || [];
                console.log('loadChatList - 会话列表:', conversationList);

                // 清空现有会话
                this.chats.clear();

                // 按时间倒序添加到 Map（最新的在前）
                conversationList.sort((a, b) => {
                    const timeA = new Date(a.createTime || 0).getTime();
                    const timeB = new Date(b.createTime || 0).getTime();
                    return timeB - timeA;
                });

                conversationList.forEach(item => {
                    // 确保 id 是数字类型，与后端返回的 conversationId 类型一致
                    const chatId = Number(item.id);
                    this.chats.set(chatId, {
                        id: chatId,
                        title: item.title || '未命名会话',
                        messages: [],
                        timestamp: item.createTime,
                        createdAt: item.createTime
                    });
                });

                console.log('loadChatList - chats.size:', this.chats.size);
                this.renderChatHistory();
            } else {
                console.warn('服务端返回错误:', result.msg);
            }
        } catch (error) {
            console.error('加载会话列表失败:', error);
        }
    }

    // 获取当前会话 ID
    async getCurrentConversationId() {
        try {
            const response = await fetch(API_CONVERSATION_CURRENT_ID, {
                method: 'GET'
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();
            console.log('getCurrentConversationId - 响应:', result);

            if (result.code === 0) {
                const conversationId = result.data?.conversationId;
                console.log('getCurrentConversationId - conversationId:', conversationId);
                // 返回数字类型，确保与 chats Map 中的 key 类型一致
                return conversationId ? Number(conversationId) : null;
            }
            return null;
        } catch (error) {
            console.error('获取当前会话 ID 失败:', error);
            return null;
        }
    }

    // 加载会话消息
    async loadConversationMessages(conversationId) {
        try {
            const response = await fetch(API_CONVERSATION_MESSAGES(conversationId), {
                method: 'GET'
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();
            console.log('loadConversationMessages - 响应:', result);

            if (result.code === 0) {
                const messageList = result.data?.list || [];
                console.log('loadConversationMessages - messageList:', messageList);

                // 转换消息格式
                const messages = messageList.map(msg => ({
                    role: msg.messageType === 'USER' ? 'user' : 'assistant',
                    content: msg.text,
                    timestamp: msg.createTime || new Date().toISOString(),
                    createTime: msg.createTime
                }));

                console.log('loadConversationMessages - 转换后的 messages:', messages);

                // 确保 conversationId 是数字类型（与后端返回的 id 类型一致）
                const numericId = Number(conversationId);

                // 更新本地会话数据
                if (this.chats.has(numericId)) {
                    const chat = this.chats.get(numericId);
                    chat.messages = messages;
                    // 更新标题（如果后端返回了标题）
                    if (result.data?.title) {
                        chat.title = result.data.title;
                    }
                } else {
                    // 如果会话不存在，创建一个
                    this.chats.set(numericId, {
                        id: numericId,
                        title: result.data?.title || '当前会话',
                        messages: messages,
                        timestamp: new Date().toISOString(),
                        createdAt: new Date().toISOString()
                    });
                }

                // 设置当前会话（使用数字类型）
                this.currentChatId = numericId;
                console.log('loadConversationMessages - 设置 currentChatId:', numericId);

                // 更新 main 元素的 CSS 类
                const mainElement = document.querySelector('main.main-content');
                if (mainElement) {
                    if (messages.length > 0) {
                        mainElement.classList.remove('empty-state');
                        mainElement.classList.add('has-messages', 'has-conversation');
                        console.log('loadConversationMessages - 已添加 has-messages has-conversation 类');
                    }
                }

                // 渲染聊天历史列表（包含高亮）
                this.renderChatHistory();

                // 渲染消息
                this.renderMessages();

                console.log('loadConversationMessages - 完成，当前会话:', this.currentChatId);
            } else {
                console.warn('加载会话消息失败:', result.msg);
            }
        } catch (error) {
            console.error('加载会话消息失败:', error);
        }
    }

    // 创建新会话
    // 创建新会话（不请求服务器，仅切换页面）
    async createNewChat() {
        // 检查当前是否已经在 empty-chat 状态
        const mainElement = document.querySelector('main.main-content');
        if (mainElement && mainElement.classList.contains('empty-state')) {
            // 已经是 empty-chat 状态，无动作
            console.log('createNewChat - 已经是 empty-chat 状态，无需切换');
            return;
        }

        // 清除当前会话 ID
        this.currentChatId = null;

        // 清除 body 上的 conversationId
        if (document.body.dataset.conversationId) {
            delete document.body.dataset.conversationId;
        }

        // 切换 main 元素到 empty-state 状态
        if (mainElement) {
            mainElement.classList.remove('has-messages', 'has-conversation');
            mainElement.classList.add('empty-state');
            console.log('createNewChat - 已切换 main 元素到 empty-state 状态');
        }

        // 清空消息区域
        const chatMessages = document.getElementById('chat-messages');
        if (chatMessages) {
            chatMessages.innerHTML = `
                <div class="welcome-message">
                    <h2>👋 欢迎使用 AI 智能助手</h2>
                    <p>我可以帮您解答问题、编写代码、分析数据等。请随时向我提问！</p>
                </div>
            `;
        }

        // 清空输入框
        const messageInput = document.getElementById('message-input');
        if (messageInput) {
            messageInput.value = '';
            this.autoResizeInput();
        }

        // 清空文件选择
        this.clearFiles();

        // 重新渲染聊天历史列表（移除高亮）
        this.renderChatHistory();

        console.log('createNewChat - 已切换到 empty-chat 状态');
    }

    // 创建会话并发送消息（用于 empty-chat 页面，异步请求）
    async createChatAndSendMessage(content) {
        try {
            console.log('createChatAndSendMessage - 开始创建会话');

            // 异步请求后端创建新会话
            const response = await fetch(API_CONVERSATION_CREATE, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: `content=${encodeURIComponent(content)}`
            });

            console.log('createChatAndSendMessage - 服务器响应状态:', response.status);

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();
            console.log('createChatAndSendMessage - 服务器响应结果:', result);

            // 检查响应 code，0 表示成功
            if (result.code === 0 && result.data) {
                const data = result.data;
                const conversationId = data?.conversationId;

                if (conversationId) {
                    console.log('createChatAndSendMessage - 会话创建成功, conversationId:', conversationId);

                    // 设置当前会话 ID
                    this.currentChatId = conversationId;

                    // 保存到 body 的 data 属性
                    document.body.dataset.conversationId = conversationId;

                    // 创建会话对象
                    this.chats.set(conversationId, {
                        id: conversationId,
                        title: content.substring(0, 30) + (content.length > 30 ? '...' : ''),
                        messages: [],
                        timestamp: new Date().toISOString(),
                        createdAt: new Date().toISOString()
                    });

                    // 切换 main 元素到 has-messages 状态
                    const mainElement = document.querySelector('main.main-content');
                    if (mainElement) {
                        mainElement.classList.remove('empty-state');
                        mainElement.classList.add('has-messages', 'has-conversation');
                        console.log('createChatAndSendMessage - 已切换页面状态');
                    }

                    // 渲染聊天历史列表
                    this.renderChatHistory();

                    // 创建用户消息
                    const userMessage = {
                        role: 'user',
                        content: content,
                        timestamp: new Date().toISOString()
                    };

                    // 添加到会话
                    const chat = this.chats.get(conversationId);
                    chat.messages.push(userMessage);

                    // 显示用户消息
                    this.appendMessage(userMessage);

                    // 清空输入框
                    const input = document.getElementById('message-input');
                    if (input) {
                        input.value = '';
                        this.autoResizeInput();
                    }

                    console.log('createChatAndSendMessage - 开始发送消息到服务器');

                    // 异步发送到服务器获取 AI 响应
                    await this.sendToServer(userMessage);

                    console.log('createChatAndSendMessage - 完成');
                }
            } else {
                this.showNotification(result.msg || '创建会话失败', 'error');
            }
        } catch (error) {
            console.error('创建会话并发送消息失败:', error);
            this.showNotification('创建会话失败，请稍后重试', 'error');
        }
    }

    // 切换会话
    async switchChat(chatId) {
        this.currentChatId = chatId;

        // 确保 chatId 是数字类型
        const numericId = Number(chatId);

        // 检查本地是否已有该会话的消息
        const chat = this.chats.get(numericId);

        // 如果是 empty-chat 状态（没有当前会话或会话没有消息），从服务器加载消息
        const mainElement = document.querySelector('main.main-content');
        const isEmptyState = mainElement && mainElement.classList.contains('empty-state');
        const hasNoMessages = !chat || chat.messages.length === 0;

        // 保存当前进度条状态（如果有）
        const progressContainer = document.getElementById('upload-progress');
        const isUploading = window.uploadState && window.uploadState.isUploading;

        if (isEmptyState || hasNoMessages) {
            // 从服务器加载消息
            await this.loadConversationMessages(numericId);
        } else {
            // 本地已有消息，直接渲染
            this.renderChatHistory();
            this.renderMessages();

            // 更新 main 元素的 CSS 类
            if (mainElement && chat.messages.length > 0) {
                mainElement.classList.remove('empty-state');
                mainElement.classList.add('has-messages', 'has-conversation');
            }
        }

        // 如果正在上传，恢复进度条显示
        if (isUploading && progressContainer) {
            // 进度条应该继续显示
            console.log('会话切换，但上传仍在进行中，保持进度条显示');
        }
    }

    // 切换到无消息页面（不刷新，不更新URL）
    navigateToEmptyChat() {
        // 不做任何操作，保持当前页面
        console.log('导航到 empty-chat (无刷新)');
    }

    // 切换到有消息页面（不刷新，不更新URL）
    navigateToChat() {
        // 不做任何操作，保持当前页面
        console.log('导航到 chat (无刷新)');
    }

    // 删除会话
    async deleteChat(chatId, event) {
        event.stopPropagation();

        if (!confirm('确定要删除这个会话吗？')) {
            return;
        }

        try {
            // 使用新的 API 路径删除会话
            const response = await fetch(API_CONVERSATION_DELETE(chatId), {
                method: 'POST'
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

                // 异步刷新会话列表（从服务器重新加载）
                await this.loadChatList();
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

        // 将 Map 转换为数组，并按时间戳倒序排序（最新的在顶部）
        const sortedChats = Array.from(this.chats.values())
            .sort((a, b) => {
                const timeA = new Date(a.timestamp || a.createdAt || 0).getTime();
                const timeB = new Date(b.timestamp || b.createdAt || 0).getTime();
                return timeB - timeA; // 倒序：最新的在前
            });

        sortedChats.forEach(chat => {
            const chatItem = document.createElement('div');
            // 当前会话且消息不为空时添加 highlighted 类
            const isHighlighted = chat.id === this.currentChatId && chat.messages.length > 0;
            chatItem.className = `chat-item ${chat.id === this.currentChatId ? 'active' : ''} ${isHighlighted ? 'highlighted' : ''}`;

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
                <div class="chat-item-actions">
                    <button class="chat-item-menu-btn" title="更多操作">
                        <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                            <circle cx="4" cy="8" r="1.5"/>
                            <circle cx="8" cy="8" r="1.5"/>
                            <circle cx="12" cy="8" r="1.5"/>
                        </svg>
                    </button>
                    <button class="chat-item-delete" title="删除会话">
                        <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                            <path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/>
                        </svg>
                    </button>
                </div>
                <div class="chat-item-menu" style="display: none;">
                    <div class="menu-item" data-action="rename">重命名</div>
                </div>
                <div class="chat-item-rename-input" style="display: none;">
                    <input type="text" class="rename-input" placeholder="输入新名称" maxlength="30" />
                </div>
            `;

            // 绑定删除按钮事件
            chatItem.querySelector('.chat-item-delete').addEventListener('click', (e) => {
                this.deleteChat(chat.id, e);
            });

            // 绑定菜单按钮事件
            const menuBtn = chatItem.querySelector('.chat-item-menu-btn');
            const menu = chatItem.querySelector('.chat-item-menu');
            menuBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                e.preventDefault();

                // 关闭其他菜单
                document.querySelectorAll('.chat-item-menu').forEach(m => {
                    if (m !== menu) m.style.display = 'none';
                });

                // 获取按钮位置
                const rect = menuBtn.getBoundingClientRect();

                // 设置菜单位置（显示在按钮下方）
                menu.style.position = 'fixed';
                menu.style.left = rect.left + 'px';
                menu.style.top = (rect.bottom + 5) + 'px';
                menu.style.right = 'auto';
                menu.style.transform = 'none';
                menu.style.display = 'block';
            });

            // 绑定菜单项事件
            const renameItem = menu.querySelector('[data-action="rename"]');
            renameItem.addEventListener('click', (e) => {
                e.stopPropagation();
                menu.style.display = 'none';
                this.showRenameInput(chatItem, chat.id, chat.title);
            });

            container.appendChild(chatItem);
        });

        // 点击其他地方关闭菜单
        document.addEventListener('click', () => {
            document.querySelectorAll('.chat-item-menu').forEach(m => {
                m.style.display = 'none';
            });
        });
    }

    // 显示重命名对话框
    showRenameInput(chatItem, chatId, currentTitle) {
        // 创建对话框
        const dialog = document.createElement('div');
        dialog.className = 'rename-dialog-overlay';
        dialog.innerHTML = `
            <div class="rename-dialog">
                <h3>重命名会话</h3>
                <input type="text" class="rename-dialog-input" placeholder="输入新名称" maxlength="30" value="${this.escapeHtml(currentTitle)}" />
                <div class="rename-dialog-buttons">
                    <button class="rename-dialog-cancel">取消</button>
                    <button class="rename-dialog-confirm">确定</button>
                </div>
            </div>
        `;

        document.body.appendChild(dialog);

        const input = dialog.querySelector('.rename-dialog-input');
        const cancelBtn = dialog.querySelector('.rename-dialog-cancel');
        const confirmBtn = dialog.querySelector('.rename-dialog-confirm');

        // 自动聚焦并选中文字
        setTimeout(() => {
            input.focus();
            input.select();
        }, 100);

        // 关闭对话框
        const closeDialog = () => {
            dialog.remove();
        };

        // 处理重命名
        const handleRename = async () => {
            const newTitle = input.value.trim();

            // 校验
            if (!newTitle) {
                this.showNotification('会话名称不能为空', 'warning');
                input.focus();
                return;
            }

            if (newTitle.length > 30) {
                this.showNotification('会话名称不能超过30个字符', 'warning');
                input.focus();
                return;
            }

            // 立即关闭对话框
            closeDialog();

            try {
                // 请求服务器更新标题
                const response = await fetch(API_CONVERSATION_UPDATE_TITLE(chatId), {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: `title=${encodeURIComponent(newTitle)}`
                });

                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }

                const result = await response.json();

                if (result.code === 0) {
                    // 更新本地数据
                    const chat = this.chats.get(chatId);
                    if (chat) {
                        chat.title = newTitle;
                    }

                    // 异步刷新会话列表
                    await this.loadChatList();

                    this.showNotification('重命名成功', 'success');
                } else {
                    this.showNotification(result.msg || '重命名失败', 'error');
                }
            } catch (error) {
                console.error('重命名失败:', error);
                this.showNotification('重命名失败，请稍后重试', 'error');
            }
        };

        // 绑定事件
        confirmBtn.addEventListener('click', handleRename);
        cancelBtn.addEventListener('click', closeDialog);

        // 回车键确认
        input.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                handleRename();
            } else if (e.key === 'Escape') {
                closeDialog();
            }
        });

        // 点击遮罩层关闭
        dialog.addEventListener('click', (e) => {
            if (e.target === dialog) {
                closeDialog();
            }
        });
    }

    // 渲染消息列表
    renderMessages() {
        const container = document.getElementById('chat-messages');
        if (!container) {
            console.warn('chat-messages 容器不存在');
            // 如果在 empty-chat.html 页面，不需要渲染消息
            return;
        }

        container.innerHTML = '';

        const chat = this.chats.get(this.currentChatId);
        console.log('renderMessages - currentChatId:', this.currentChatId);
        console.log('renderMessages - chat:', chat);
        console.log('renderMessages - messages:', chat?.messages);

        if (!chat || chat.messages.length === 0) {
            // 无消息时显示欢迎信息
            console.log('renderMessages - 显示欢迎信息');
            container.innerHTML = `
                <div class="welcome-message">
                    <h2>💬 开始新的对话</h2>
                    <p>发送一条消息开始聊天吧！</p>
                </div>
            `;
            return;
        }

        console.log('renderMessages - 渲染', chat.messages.length, '条消息');
        // 有消息时按顺序显示
        chat.messages.forEach(msg => {
            this.appendMessage(msg, false);
        });

        this.scrollToBottom();
    }

    // 格式化消息时间（人类友好）
    formatMessageTime(timestamp) {
        if (!timestamp) return '';

        const date = new Date(timestamp);
        const now = new Date();
        const diffMs = now - date;
        const diffSec = Math.floor(diffMs / 1000);
        const diffMin = Math.floor(diffSec / 60);
        const diffHour = Math.floor(diffMin / 60);
        const diffDay = Math.floor(diffHour / 24);

        // 刚刚
        if (diffSec < 60) {
            return '刚刚';
        }

        // 几分钟前
        if (diffMin < 60) {
            return `${diffMin}分钟前`;
        }

        // 几小时前
        if (diffHour < 24) {
            return `${diffHour}小时前`;
        }

        // 几天前
        if (diffDay < 7) {
            return `${diffDay}天前`;
        }

        // 今年内
        if (date.getFullYear() === now.getFullYear()) {
            const month = date.getMonth() + 1;
            const day = date.getDate();
            const hour = date.getHours().toString().padStart(2, '0');
            const minute = date.getMinutes().toString().padStart(2, '0');
            return `${month}月${day}日 ${hour}:${minute}`;
        }

        // 更早
        const year = date.getFullYear();
        const month = date.getMonth() + 1;
        const day = date.getDate();
        const hour = date.getHours().toString().padStart(2, '0');
        const minute = date.getMinutes().toString().padStart(2, '0');
        return `${year}年${month}月${day}日 ${hour}:${minute}`;
    }

    // 添加消息到界面
    appendMessage(message, animate = true) {
        const container = document.getElementById('chat-messages');
        const messageEl = document.createElement('div');
        messageEl.className = `message ${message.role}`;
        if (!animate) {
            messageEl.style.animation = 'none';
        }

        // 使用人类友好的时间格式
        const time = this.formatMessageTime(message.createTime || message.timestamp);
        const avatar = message.role === 'user' ? '👤' : '🤖';
        const label = message.role === 'user' ? '我: ' : '';
        const content = message.role === 'user'
            ? this.escapeHtml(message.content)
            : this.md.render(message.content);

        messageEl.innerHTML = `
            <div class="message-avatar">${avatar}</div>
            <div class="message-content">
                ${label ? `<div class="message-label">${label}</div>` : ''}
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

        // 优先处理文件上传
        if (this.selectedFiles.length > 0) {
            await this.uploadFiles();
            return;
        }

        if (!content) {
            this.showNotification('请输入消息内容', 'warning');
            return;
        }

        if (!this.currentChatId) {
            // 没有会话 ID，说明在 empty-chat 页面
            // 需要请求服务器创建会话，然后发送消息
            await this.createChatAndSendMessage(content);
            return;
        }

        // 创建用户消息
        const userMessage = {
            role: 'user',
            content: content,
            timestamp: new Date().toISOString()
        };

        // 添加到会话
        const chat = this.chats.get(this.currentChatId);
        chat.messages.push(userMessage);

        // 更新会话标题
        if (chat.messages.length === 1) {
            chat.title = content.substring(0, 30) + (content.length > 30 ? '...' : '');
            this.renderChatHistory();
            // 第一条消息发送后，切换到有消息页面
            this.navigateToChat();
        }

        this.appendMessage(userMessage);

        // 清空输入
        input.value = '';
        this.autoResizeInput();

        // 发送到服务器
        await this.sendToServer(userMessage);
    }

    // 上传文件到服务器
    async uploadFiles() {
        const files = this.selectedFiles;
        if (files.length === 0) {
            this.showNotification('请先选择文件', 'warning');
            return;
        }

        const sendBtn = document.getElementById('send-btn');

        try {
            // 禁用发送按钮
            sendBtn.disabled = true;
            sendBtn.textContent = '上传中...';

            // 显示进度条
            this.showUploadProgress(0, files);

            // 获取 conversationId
            const conversationId = this.currentChatId || document.body.dataset.conversationId;
            if (!conversationId) {
                throw new Error('没有有效的 conversationId');
            }

            // 构建 FormData
            const formData = new FormData();
            files.forEach(file => {
                formData.append('files', file);
            });

            // 更新进度
            this.updateUploadProgress(30, files);

            // 上传文件
            const response = await fetch(API_CONVERSATION_UPLOAD_DOCUMENTS(conversationId), {
                method: 'POST',
                body: formData
            });

            if (!response.ok) {
                throw new Error(`上传文件失败: HTTP ${response.status}`);
            }

            this.updateUploadProgress(100, files);

            const result = await response.json();
            if (result.code !== 0) {
                throw new Error(result.msg || '上传文件失败');
            }

            // 隐藏进度条
            setTimeout(() => {
                this.hideUploadProgress();
                this.showUploadCompleteMessage(files);
                this.showNotification('文件上传成功', 'success');
            }, 500);

            // 清空文件选择
            this.clearFiles();

        } catch (error) {
            console.error('上传文件失败:', error);
            this.showNotification('上传文件失败: ' + error.message, 'error');
            this.hideUploadProgress();
        } finally {
            sendBtn.disabled = false;
            sendBtn.innerHTML = '<svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor"><path d="M10.894 2.553a1 1 0 0 0-1.788 0l-7 14a1 1 0 0 0 1.169 1.409l5-1.429A1 1 0 0 0 9 15.571V11a1 1 0 1 1 2 0v4.571a1 1 0 0 0 .725.962l5 1.428a1 1 0 0 0 1.17-1.408l-7-14z"/></svg>发送';
        }
    }

    // 显示上传进度
    showUploadProgress(percent, files) {
        this.hideUploadProgress();

        const inputArea = document.querySelector('.input-area');
        if (!inputArea) return;

        const progressContainer = document.createElement('div');
        progressContainer.className = 'upload-progress-container';
        progressContainer.id = 'upload-progress';

        const fileNames = files.map(f => f.name).join(', ');

        progressContainer.innerHTML = `
            <div class="upload-progress-bar">
                <div class="upload-progress-fill" style="width: ${percent}%"></div>
            </div>
            <div class="upload-progress-text">上传中... ${percent}%</div>
            <div class="upload-progress-files">${fileNames}</div>
        `;

        inputArea.appendChild(progressContainer);
    }

    // 更新上传进度
    updateUploadProgress(percent, files) {
        const progressContainer = document.getElementById('upload-progress');
        if (!progressContainer) return;

        const fill = progressContainer.querySelector('.upload-progress-fill');
        const text = progressContainer.querySelector('.upload-progress-text');

        if (fill) fill.style.width = `${percent}%`;
        if (text) text.textContent = `上传中... ${percent}%`;
    }

    // 隐藏上传进度
    hideUploadProgress() {
        const progressContainer = document.getElementById('upload-progress');
        if (progressContainer) {
            progressContainer.remove();
        }
    }

    // 显示上传完成消息
    showUploadCompleteMessage(files) {
        const fileListMarkdown = files.map((file, index) =>
            `${index + 1}. ${file.name}`
        ).join('\n');

        const messageContent = `文件已上传完成：\n\n${fileListMarkdown}`;

        // 添加到聊天消息
        const chatMessages = document.getElementById('chat-messages');
        if (chatMessages) {
            const messageEl = document.createElement('div');
            messageEl.className = 'message assistant';

            const time = new Date().toLocaleString('zh-CN');
            messageEl.innerHTML = `
                <div class="message-avatar">🤖</div>
                <div class="message-content">
                    <div class="message-bubble">${this.md.render(messageContent)}</div>
                    <div class="message-time">${time}</div>
                </div>
            `;

            chatMessages.appendChild(messageEl);
            this.scrollToBottom();
        }

        console.log('上传完成，文件列表:', fileListMarkdown);
    }

    // 发送到服务器（流式响应）
    async sendToServer(message) {
        const sendBtn = document.getElementById('send-btn');
        sendBtn.disabled = true;

        try {
            // 获取 conversationId
            const conversationId = this.currentChatId || document.body.dataset.conversationId;

            if (!conversationId) {
                throw new Error('没有有效的 conversationId');
            }

            // 发送请求到新的 API 路径
            const response = await fetch(API_CONVERSATION_TALK(conversationId), {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: `content=${encodeURIComponent(message.content)}`
            });

            // 只要 HTTP 状态是 200 就是成功
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            // 创建 AI 消息元素
            const container = document.getElementById('chat-messages');
            const messageEl = document.createElement('div');
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

            const bubble = messageEl.querySelector('.message-bubble');
            let aiContent = '';

            // 读取流式响应
            const reader = response.body.getReader();
            const decoder = new TextDecoder('utf-8');

            while (true) {
                const {done, value} = await reader.read();
                if (done) break;

                const chunk = decoder.decode(value, {stream: true});
                aiContent += chunk;

                // 响应式更新内容
                bubble.innerHTML = this.md.render(aiContent);
                this.scrollToBottom();
            }

            // 移除 streaming 标记
            messageEl.classList.remove('streaming');

            // 保存AI响应
            const chat = this.chats.get(this.currentChatId);
            if (chat) {
                chat.messages.push({
                    role: 'assistant',
                    content: aiContent,
                    timestamp: new Date().toISOString()
                });
            }

        } catch (error) {
            console.error('发送消息失败:', error);
            this.showNotification('发送消息失败，请稍后重试', 'error');

            // 添加错误消息
            const chat = this.chats.get(this.currentChatId);
            if (chat) {
                chat.messages.push({
                    role: 'assistant',
                    content: '抱歉，处理您的请求时出现错误。请稍后重试。',
                    timestamp: new Date().toISOString()
                });
            }
            this.appendMessage({
                role: 'assistant',
                content: '抱歉，处理您的请求时出现错误。请稍后重试。',
                timestamp: new Date().toISOString()
            });
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
        // 使用 requestAnimationFrame 确保滚动生效
        requestAnimationFrame(() => {
            container.scrollTop = container.scrollHeight;
        });
    }

    // 从服务端加载会话列表（向后兼容，调用 loadChatList）
    async loadChatsFromStorage() {
        return this.loadChatList();
    }

    // 加载会话内容（双击时调用）
    async loadChatContent(chatId) {
        try {
            // 请求后端获取会话详情
            const response = await fetch(API_SESSION_CONTENT(chatId), {
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
