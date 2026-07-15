/**
 * ===== API URL 全局常量 =====
 * 所有请求服务端的 URL 集中在这里管理，方便统一修改。
 * 修改 API_BASE 即可变更所有接口的前缀路径。
 */

const API_BASE = '/api/chat';

// --- 会话列表 ---
const API_CONVERSATION_LIST = `${API_BASE}/conversation/list`;

// --- 当前会话 ID ---
const API_CONVERSATION_CURRENT_ID = `${API_BASE}/conversation/currentId`;

// --- 创建会话 ---
const API_CONVERSATION_CREATE = `${API_BASE}/conversation/create`;

// --- 会话消息（参数: conversationId） ---
const API_CONVERSATION_MESSAGES = (id) => `${API_BASE}/conversation/${id}/messages`;

// --- 删除会话（参数: chatId） ---
const API_CONVERSATION_DELETE = (id) => `${API_BASE}/conversation/delete/${id}`;

// --- 更新会话标题（参数: chatId） ---
const API_CONVERSATION_UPDATE_TITLE = (id) => `${API_BASE}/conversation/updateTitle/${id}`;

// --- 发送消息/流式对话（参数: conversationId） ---
const API_CONVERSATION_TALK = (id) => `${API_BASE}/conversation/talk/${id}`;

// --- 上传文件（参数: conversationId） ---
const API_CONVERSATION_UPLOAD_DOCUMENTS = (id) => `${API_BASE}/conversation/${id}/upload/documents`;

// --- 会话内容（参数: chatId） ---
const API_SESSION_CONTENT = (id) => `${API_BASE}/session/${id}/content`;

// --- 页面路由 ---
const PAGE_CHAT = '/chat.html';
