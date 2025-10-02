# 🚀 Hướng dẫn xây dựng Frontend React cho ChatApp

Tài liệu này hướng dẫn từng bước xây dựng giao diện React để tích hợp với backend ChatApp.

---

## 📋 Mục lục

1. [Setup Project](#1-setup-project)
2. [Cấu trúc thư mục](#2-cấu-trúc-thư-mục)
3. [Dependencies cần thiết](#3-dependencies-cần-thiết)
4. [API Service Layer](#4-api-service-layer)
5. [Authentication & Protected Routes](#5-authentication--protected-routes)
6. [WebSocket Integration](#6-websocket-integration)
7. [Core Components](#7-core-components)
8. [WebRTC Video Call](#8-webrtc-video-call)
9. [Styling & UI](#9-styling--ui)
10. [Deployment](#10-deployment)

---

## 1. Setup Project

### Tạo React app với Vite (khuyên dùng - nhanh hơn CRA)

```bash
npm create vite@latest chatapp-frontend -- --template react
cd chatapp-frontend
npm install
```

### Hoặc dùng Create React App

```bash
npx create-react-app chatapp-frontend
cd chatapp-frontend
```

---

## 2. Cấu trúc thư mục

```
chatapp-frontend/
├── public/
│   └── index.html
├── src/
│   ├── api/
│   │   ├── axios.js          # Axios instance config
│   │   ├── authApi.js        # Auth endpoints
│   │   ├── userApi.js        # User endpoints
│   │   ├── roomApi.js        # Room endpoints
│   │   ├── messageApi.js     # Message endpoints
│   │   └── friendApi.js      # Friend endpoints
│   ├── components/
│   │   ├── auth/
│   │   │   ├── Login.jsx
│   │   │   └── Register.jsx
│   │   ├── chat/
│   │   │   ├── ChatRoom.jsx
│   │   │   ├── MessageList.jsx
│   │   │   ├── MessageInput.jsx
│   │   │   └── TypingIndicator.jsx
│   │   ├── friends/
│   │   │   ├── FriendList.jsx
│   │   │   └── FriendRequest.jsx
│   │   ├── rooms/
│   │   │   └── RoomList.jsx
│   │   ├── call/
│   │   │   └── VideoCall.jsx
│   │   └── layout/
│   │       ├── Navbar.jsx
│   │       └── Sidebar.jsx
│   ├── context/
│   │   ├── AuthContext.jsx
│   │   └── WebSocketContext.jsx
│   ├── hooks/
│   │   ├── useAuth.js
│   │   ├── useWebSocket.js
│   │   └── useWebRTC.js
│   ├── utils/
│   │   ├── storage.js       # LocalStorage helpers
│   │   └── constants.js     # API URLs, etc
│   ├── App.jsx
│   ├── App.css
│   └── main.jsx
├── .env
└── package.json
```

---

## 3. Dependencies cần thiết

### Cài đặt packages

```bash
npm install axios
npm install @stomp/stompjs sockjs-client
npm install react-router-dom
npm install zustand               # State management (hoặc dùng Context API)
npm install react-hot-toast       # Notifications
npm install date-fns              # Date formatting
npm install @headlessui/react     # UI components (optional)
npm install tailwindcss           # Styling (optional)
```

### package.json

```json
{
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "react-router-dom": "^6.20.0",
    "axios": "^1.6.0",
    "@stomp/stompjs": "^7.0.0",
    "sockjs-client": "^1.6.1",
    "zustand": "^4.4.0",
    "react-hot-toast": "^2.4.0",
    "date-fns": "^3.0.0"
  }
}
```

---

## 4. API Service Layer

### `.env`

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_URL=http://localhost:8080/ws
```

### `src/api/axios.js`

```javascript
import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor: thêm JWT token
axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor: handle token expiry
axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // Nếu 401 và chưa retry
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = localStorage.getItem('refreshToken');
        const { data } = await axios.post(`${API_BASE_URL}/api/auth/refresh`, refreshToken, {
          headers: { 'Content-Type': 'text/plain' }
        });

        localStorage.setItem('accessToken', data.accessToken);
        localStorage.setItem('refreshToken', data.refreshToken);

        originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
        return axiosInstance(originalRequest);
      } catch (refreshError) {
        localStorage.clear();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;
```

### `src/api/authApi.js`

```javascript
import axiosInstance from './axios';

export const authApi = {
  register: async (data) => {
    const response = await axiosInstance.post('/api/auth/register', data);
    return response.data;
  },

  login: async (credentials) => {
    const response = await axiosInstance.post('/api/auth/login', credentials);
    return response.data;
  },

  logout: async () => {
    await axiosInstance.post('/api/auth/logout');
    localStorage.clear();
  },

  refresh: async (refreshToken) => {
    const response = await axiosInstance.post('/api/auth/refresh', refreshToken, {
      headers: { 'Content-Type': 'text/plain' }
    });
    return response.data;
  }
};
```

### `src/api/messageApi.js`

```javascript
import axiosInstance from './axios';

export const messageApi = {
  send: async (roomId, senderId, content) => {
    const response = await axiosInstance.post(
      `/api/messages?roomId=${roomId}&senderId=${senderId}`,
      content,
      { headers: { 'Content-Type': 'text/plain' } }
    );
    return response.data;
  },

  getByRoom: async (roomId, page = 0, size = 50) => {
    const response = await axiosInstance.get(
      `/api/messages/room/${roomId}?page=${page}&size=${size}`
    );
    return response.data;
  },

  edit: async (messageId, editorUserId, newContent) => {
    const response = await axiosInstance.put(
      `/api/messages/${messageId}?editorUserId=${editorUserId}`,
      newContent,
      { headers: { 'Content-Type': 'text/plain' } }
    );
    return response.data;
  },

  softDelete: async (messageId, requesterUserId) => {
    await axiosInstance.delete(`/api/messages/${messageId}?requesterUserId=${requesterUserId}`);
  },

  markSeen: async (roomId, messageId, userId) => {
    const response = await axiosInstance.post(
      `/api/messages/room/${roomId}/seen/${messageId}?userId=${userId}`
    );
    return response.data;
  },

  uploadMedia: async (roomId, senderId, file) => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await axiosInstance.post(
      `/api/messages/room/${roomId}/media?senderId=${senderId}`,
      formData,
      { headers: { 'Content-Type': 'multipart/form-data' } }
    );
    return response.data;
  }
};
```

### `src/api/roomApi.js`

```javascript
import axiosInstance from './axios';

export const roomApi = {
  create: async (roomName, isGroup) => {
    const response = await axiosInstance.post(
      `/api/rooms?roomName=${encodeURIComponent(roomName)}&isGroup=${isGroup}`
    );
    return response.data;
  },

  getAll: async () => {
    const response = await axiosInstance.get('/api/rooms');
    return response.data;
  },

  getById: async (roomId) => {
    const response = await axiosInstance.get(`/api/rooms/${roomId}`);
    return response.data;
  },

  addMember: async (roomId, userId, role = 'member') => {
    const response = await axiosInstance.post(
      `/api/rooms/${roomId}/members?userId=${userId}&role=${role}`
    );
    return response.data;
  },

  getMembers: async (roomId) => {
    const response = await axiosInstance.get(`/api/rooms/${roomId}/members`);
    return response.data;
  }
};
```

---

## 5. Authentication & Protected Routes

### `src/context/AuthContext.jsx`

```javascript
import { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../api/authApi';
import toast from 'react-hot-toast';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if user is logged in
    const token = localStorage.getItem('accessToken');
    const savedUser = localStorage.getItem('user');
    if (token && savedUser) {
      setUser(JSON.parse(savedUser));
    }
    setLoading(false);
  }, []);

  const login = async (username, password) => {
    try {
      const data = await authApi.login({ username, password });
      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshToken);
      localStorage.setItem('user', JSON.stringify(data.user));
      setUser(data.user);
      toast.success('Đăng nhập thành công!');
      return data;
    } catch (error) {
      toast.error(error.response?.data || 'Đăng nhập thất bại');
      throw error;
    }
  };

  const register = async (userData) => {
    try {
      const data = await authApi.register(userData);
      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshToken);
      localStorage.setItem('user', JSON.stringify(data.user));
      setUser(data.user);
      toast.success('Đăng ký thành công!');
      return data;
    } catch (error) {
      toast.error(error.response?.data || 'Đăng ký thất bại');
      throw error;
    }
  };

  const logout = async () => {
    await authApi.logout();
    setUser(null);
    toast.success('Đã đăng xuất');
  };

  return (
    <AuthContext.Provider value={{ user, login, register, logout, loading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
```

### `src/components/auth/Login.jsx`

```javascript
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await login(username, password);
      navigate('/chat');
    } catch (error) {
      console.error('Login failed', error);
    }
  };

  return (
    <div className="login-container">
      <form onSubmit={handleSubmit} className="login-form">
        <h2>Đăng nhập</h2>
        <input
          type="text"
          placeholder="Username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <button type="submit">Đăng nhập</button>
        <p>
          Chưa có tài khoản? <a href="/register">Đăng ký</a>
        </p>
      </form>
    </div>
  );
}
```

### `src/App.jsx` - Protected Routes

```javascript
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Toaster } from 'react-hot-toast';
import Login from './components/auth/Login';
import Register from './components/auth/Register';
import ChatLayout from './components/layout/ChatLayout';

function ProtectedRoute({ children }) {
  const { user, loading } = useAuth();
  
  if (loading) return <div>Loading...</div>;
  if (!user) return <Navigate to="/login" />;
  
  return children;
}

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Toaster position="top-right" />
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route
            path="/chat/*"
            element={
              <ProtectedRoute>
                <ChatLayout />
              </ProtectedRoute>
            }
          />
          <Route path="/" element={<Navigate to="/chat" />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
```

---

## 6. WebSocket Integration

### `src/hooks/useWebSocket.js`

```javascript
import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const WS_URL = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws';

export const useWebSocket = (roomId, onMessage) => {
  const clientRef = useRef(null);
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    if (!roomId) return;

    const socket = new SockJS(WS_URL);
    const stompClient = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    stompClient.onConnect = () => {
      console.log('WebSocket connected');
      setConnected(true);

      // Subscribe to room messages
      stompClient.subscribe(`/topic/rooms/${roomId}`, (message) => {
        const msg = JSON.parse(message.body);
        onMessage(msg);
      });

      // Subscribe to typing indicator
      stompClient.subscribe(`/topic/rooms/${roomId}/typing`, (message) => {
        const typing = JSON.parse(message.body);
        console.log('Typing:', typing);
      });
    };

    stompClient.onDisconnect = () => {
      console.log('WebSocket disconnected');
      setConnected(false);
    };

    stompClient.activate();
    clientRef.current = stompClient;

    return () => {
      if (clientRef.current) {
        clientRef.current.deactivate();
      }
    };
  }, [roomId, onMessage]);

  const sendMessage = (senderId, content) => {
    if (clientRef.current?.connected) {
      clientRef.current.publish({
        destination: `/app/rooms/${roomId}/send`,
        body: JSON.stringify({ senderId, content }),
      });
    }
  };

  const sendTyping = (userId, isTyping) => {
    if (clientRef.current?.connected) {
      clientRef.current.publish({
        destination: '/app/typing',
        body: JSON.stringify({ roomId, userId, typing: isTyping }),
      });
    }
  };

  return { connected, sendMessage, sendTyping };
};
```

### `src/components/chat/ChatRoom.jsx`

```javascript
import { useState, useEffect, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useWebSocket } from '../../hooks/useWebSocket';
import { messageApi } from '../../api/messageApi';
import MessageList from './MessageList';
import MessageInput from './MessageInput';

export default function ChatRoom() {
  const { roomId } = useParams();
  const { user } = useAuth();
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(true);

  // Load old messages
  useEffect(() => {
    const loadMessages = async () => {
      try {
        const data = await messageApi.getByRoom(roomId);
        setMessages(data);
      } catch (error) {
        console.error('Failed to load messages', error);
      } finally {
        setLoading(false);
      }
    };
    loadMessages();
  }, [roomId]);

  // Handle new message from WebSocket
  const handleNewMessage = useCallback((msg) => {
    setMessages((prev) => [...prev, msg]);
  }, []);

  const { connected, sendMessage, sendTyping } = useWebSocket(roomId, handleNewMessage);

  const handleSend = (content) => {
    sendMessage(user.userId, content);
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div className="chat-room">
      <div className="chat-header">
        <h3>Room #{roomId}</h3>
        <span>{connected ? '🟢 Connected' : '🔴 Disconnected'}</span>
      </div>
      <MessageList messages={messages} currentUserId={user.userId} />
      <MessageInput onSend={handleSend} onTyping={(typing) => sendTyping(user.userId, typing)} />
    </div>
  );
}
```

### `src/components/chat/MessageInput.jsx`

```javascript
import { useState, useRef } from 'react';

export default function MessageInput({ onSend, onTyping }) {
  const [content, setContent] = useState('');
  const typingTimeoutRef = useRef(null);

  const handleChange = (e) => {
    setContent(e.target.value);
    
    // Typing indicator
    onTyping(true);
    clearTimeout(typingTimeoutRef.current);
    typingTimeoutRef.current = setTimeout(() => {
      onTyping(false);
    }, 1000);
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (content.trim()) {
      onSend(content);
      setContent('');
      onTyping(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="message-input">
      <input
        type="text"
        placeholder="Nhập tin nhắn..."
        value={content}
        onChange={handleChange}
      />
      <button type="submit">Gửi</button>
    </form>
  );
}
```

### `src/components/chat/MessageList.jsx`

```javascript
import { useEffect, useRef } from 'react';
import { format } from 'date-fns';

export default function MessageList({ messages, currentUserId }) {
  const messagesEndRef = useRef(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  return (
    <div className="message-list">
      {messages.map((msg) => (
        <div
          key={msg.messageId}
          className={`message ${msg.senderId === currentUserId ? 'sent' : 'received'}`}
        >
          <div className="message-content">
            {msg.deleted ? (
              <em>{msg.content}</em>
            ) : (
              <>
                <p>{msg.content}</p>
                {msg.editedAt && <small>(đã chỉnh sửa)</small>}
              </>
            )}
          </div>
          <div className="message-meta">
            <small>{format(new Date(msg.sentAt), 'HH:mm')}</small>
            {msg.senderId === currentUserId && msg.status && (
              <small> {msg.status === 'SEEN' ? '✓✓' : '✓'}</small>
            )}
          </div>
        </div>
      ))}
      <div ref={messagesEndRef} />
    </div>
  );
}
```

---

## 8. WebRTC Video Call - Gọi thực như Zalo

### 🎯 **Tổng quan WebRTC**

WebRTC cho phép gọi video/audio trực tiếp giữa 2 trình duyệt mà không cần server trung gian. Chỉ cần:
- **Signaling server** (đã có - WebSocket STOMP)
- **STUN server** (đã có - Google STUN)
- **Frontend WebRTC code** (sẽ implement)

### 🔄 **Flow gọi video:**
1. User A nhấn "Gọi video" → tạo offer
2. Gửi offer qua WebSocket → User B
3. User B nhận offer → tạo answer
4. Trao đổi ICE candidates
5. Kết nối P2P thành công → stream video/audio

### `src/hooks/useWebRTC.js`

```javascript
import { useState, useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import axiosInstance from '../api/axios';

const WS_URL = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws';

export const useWebRTC = (userId, remoteUserId) => {
  const [localStream, setLocalStream] = useState(null);
  const [remoteStream, setRemoteStream] = useState(null);
  const [calling, setCalling] = useState(false);
  const pcRef = useRef(null);
  const stompClientRef = useRef(null);

  useEffect(() => {
    // Get ICE servers
    const initWebRTC = async () => {
      const { data } = await axiosInstance.get('/api/webrtc/ice-servers');
      
      const socket = new SockJS(WS_URL);
      const stompClient = new Client({
        webSocketFactory: () => socket,
      });

      stompClient.onConnect = () => {
        // Subscribe to WebRTC signals
        stompClient.subscribe(`/topic/webrtc/${userId}/offer`, async (message) => {
          const offer = JSON.parse(message.body);
          await handleOffer(offer, data.iceServers);
        });

        stompClient.subscribe(`/topic/webrtc/${userId}/answer`, async (message) => {
          const answer = JSON.parse(message.body);
          await handleAnswer(answer);
        });

        stompClient.subscribe(`/topic/webrtc/${userId}/ice`, async (message) => {
          const ice = JSON.parse(message.body);
          await handleIceCandidate(ice);
        });
      };

      stompClient.activate();
      stompClientRef.current = stompClient;
    };

    initWebRTC();

    return () => {
      if (stompClientRef.current) {
        stompClientRef.current.deactivate();
      }
      if (pcRef.current) {
        pcRef.current.close();
      }
      if (localStream) {
        localStream.getTracks().forEach(track => track.stop());
      }
    };
  }, [userId]);

  const startCall = async (video = true) => {
    try {
      // Get user media
      const stream = await navigator.mediaDevices.getUserMedia({
        video: video,
        audio: true
      });
      setLocalStream(stream);

      // Get ICE servers
      const { data } = await axiosInstance.get('/api/webrtc/ice-servers');
      
      // Create peer connection
      const pc = new RTCPeerConnection({ iceServers: data.iceServers });
      pcRef.current = pc;

      // Add local stream
      stream.getTracks().forEach(track => pc.addTrack(track, stream));

      // Handle remote stream
      pc.ontrack = (event) => {
        setRemoteStream(event.streams[0]);
      };

      // Handle ICE candidates
      pc.onicecandidate = (event) => {
        if (event.candidate && stompClientRef.current?.connected) {
          stompClientRef.current.publish({
            destination: '/app/webrtc/ice',
            body: JSON.stringify({
              fromUserId: userId,
              toUserId: remoteUserId,
              candidate: JSON.stringify(event.candidate)
            })
          });
        }
      };

      // Create and send offer
      const offer = await pc.createOffer();
      await pc.setLocalDescription(offer);

      stompClientRef.current.publish({
        destination: '/app/webrtc/offer',
        body: JSON.stringify({
          fromUserId: userId,
          toUserId: remoteUserId,
          sdp: offer.sdp
        })
      });

      setCalling(true);
    } catch (error) {
      console.error('Failed to start call', error);
    }
  };

  const handleOffer = async (offer, iceServers) => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: true,
        audio: true
      });
      setLocalStream(stream);

      const pc = new RTCPeerConnection({ iceServers });
      pcRef.current = pc;

      stream.getTracks().forEach(track => pc.addTrack(track, stream));

      pc.ontrack = (event) => {
        setRemoteStream(event.streams[0]);
      };

      pc.onicecandidate = (event) => {
        if (event.candidate && stompClientRef.current?.connected) {
          stompClientRef.current.publish({
            destination: '/app/webrtc/ice',
            body: JSON.stringify({
              fromUserId: userId,
              toUserId: offer.fromUserId,
              candidate: JSON.stringify(event.candidate)
            })
          });
        }
      };

      await pc.setRemoteDescription(new RTCSessionDescription({ type: 'offer', sdp: offer.sdp }));
      const answer = await pc.createAnswer();
      await pc.setLocalDescription(answer);

      stompClientRef.current.publish({
        destination: '/app/webrtc/answer',
        body: JSON.stringify({
          fromUserId: userId,
          toUserId: offer.fromUserId,
          sdp: answer.sdp
        })
      });

      setCalling(true);
    } catch (error) {
      console.error('Failed to handle offer', error);
    }
  };

  const handleAnswer = async (answer) => {
    if (pcRef.current) {
      await pcRef.current.setRemoteDescription(
        new RTCSessionDescription({ type: 'answer', sdp: answer.sdp })
      );
    }
  };

  const handleIceCandidate = async (ice) => {
    if (pcRef.current) {
      const candidate = JSON.parse(ice.candidate);
      await pcRef.current.addIceCandidate(new RTCIceCandidate(candidate));
    }
  };

  const endCall = () => {
    if (pcRef.current) {
      pcRef.current.close();
    }
    if (localStream) {
      localStream.getTracks().forEach(track => track.stop());
    }
    setLocalStream(null);
    setRemoteStream(null);
    setCalling(false);
  };

  return { localStream, remoteStream, calling, startCall, endCall };
};
```

### 🎮 **Cách sử dụng WebRTC Hook:**

```javascript
// Trong component Chat
import { useWebRTC } from '../hooks/useWebRTC';

function ChatRoom() {
  const { user } = useAuth();
  const [showVideoCall, setShowVideoCall] = useState(false);
  const [remoteUserId, setRemoteUserId] = useState(null);

  const startVideoCall = (targetUserId) => {
    setRemoteUserId(targetUserId);
    setShowVideoCall(true);
  };

  return (
    <div>
      {/* Chat UI */}
      <button onClick={() => startVideoCall(5)}>
        📹 Gọi video
      </button>

      {/* Video Call Modal */}
      {showVideoCall && (
        <VideoCall 
          remoteUserId={remoteUserId}
          onClose={() => setShowVideoCall(false)}
        />
      )}
    </div>
  );
}
```

### 📱 **Test trên cùng mạng LAN:**

1. **Mở 2 tab browser** (hoặc 2 máy khác nhau)
2. **Đăng nhập 2 user khác nhau** (userId: 1 và 5)
3. **User 1 nhấn "Gọi video"** → User 5 sẽ nhận được offer
4. **User 5 tự động accept** → Kết nối P2P thành công
5. **Thấy video của nhau** → Giống Zalo!

### 🔧 **Troubleshooting:**

**Không thấy video:**
- Check camera/mic permission
- Mở F12 → Console xem lỗi
- Kiểm tra STUN server connection

**Không kết nối được:**
- Firewall block WebRTC ports
- Cần TURN server cho mạng phức tạp
- Check WebSocket connection

### `src/components/call/VideoCall.jsx`

```javascript
import { useEffect, useRef } from 'react';
import { useWebRTC } from '../../hooks/useWebRTC';
import { useAuth } from '../../context/AuthContext';

export default function VideoCall({ remoteUserId, onClose }) {
  const { user } = useAuth();
  const { localStream, remoteStream, calling, startCall, endCall } = useWebRTC(user.userId, remoteUserId);
  const localVideoRef = useRef();
  const remoteVideoRef = useRef();

  useEffect(() => {
    if (localStream && localVideoRef.current) {
      localVideoRef.current.srcObject = localStream;
    }
  }, [localStream]);

  useEffect(() => {
    if (remoteStream && remoteVideoRef.current) {
      remoteVideoRef.current.srcObject = remoteStream;
    }
  }, [remoteStream]);

  const handleEndCall = () => {
    endCall();
    onClose();
  };

  return (
    <div className="video-call-container">
      <div className="video-grid">
        <div className="video-box remote">
          <video ref={remoteVideoRef} autoPlay playsInline />
          <span>Người nhận</span>
        </div>
        <div className="video-box local">
          <video ref={localVideoRef} autoPlay playsInline muted />
          <span>Bạn</span>
        </div>
      </div>
      <div className="call-controls">
        {!calling ? (
          <button onClick={() => startCall(true)} className="btn-call">
            📹 Bắt đầu gọi video
          </button>
        ) : (
          <button onClick={handleEndCall} className="btn-end-call">
            ❌ Kết thúc
          </button>
        )}
      </div>
    </div>
  );
}
```

### 🚀 **Demo HTML đơn giản (Test ngay 5 phút):**

Tạo file `test-webrtc.html` để test nhanh:

```html
<!DOCTYPE html>
<html>
<head>
    <title>WebRTC Test</title>
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/@stomp/stompjs@7/bundles/stomp.umd.min.js"></script>
</head>
<body>
    <h1>WebRTC Test - ChatApp</h1>
    
    <div>
        <label>Your User ID: <input id="userId" value="1" /></label>
        <label>Call User ID: <input id="remoteUserId" value="5" /></label>
        <button onclick="startCall()">📹 Start Video Call</button>
        <button onclick="endCall()">❌ End Call</button>
    </div>
    
    <div style="display: flex; gap: 20px; margin-top: 20px;">
        <div>
            <h3>Local Video (You)</h3>
            <video id="localVideo" width="300" height="200" autoplay muted></video>
        </div>
        <div>
            <h3>Remote Video (Friend)</h3>
            <video id="remoteVideo" width="300" height="200" autoplay></video>
        </div>
    </div>
    
    <div id="status" style="margin-top: 20px; font-weight: bold;"></div>

    <script>
        let stompClient = null;
        let peerConnection = null;
        let localStream = null;
        
        const localVideo = document.getElementById('localVideo');
        const remoteVideo = document.getElementById('remoteVideo');
        const status = document.getElementById('status');
        
        // Connect WebSocket
        const socket = new SockJS('http://localhost:8080/ws');
        stompClient = new StompJs.Client({
            webSocketFactory: () => socket,
            onConnect: () => {
                status.textContent = '🟢 WebSocket Connected';
                subscribeToSignals();
            },
            onDisconnect: () => {
                status.textContent = '🔴 WebSocket Disconnected';
            }
        });
        stompClient.activate();
        
        function subscribeToSignals() {
            const userId = document.getElementById('userId').value;
            
            // Subscribe to offers
            stompClient.subscribe(`/topic/webrtc/${userId}/offer`, async (message) => {
                const offer = JSON.parse(message.body);
                await handleOffer(offer);
            });
            
            // Subscribe to answers
            stompClient.subscribe(`/topic/webrtc/${userId}/answer`, async (message) => {
                const answer = JSON.parse(message.body);
                await handleAnswer(answer);
            });
            
            // Subscribe to ICE candidates
            stompClient.subscribe(`/topic/webrtc/${userId}/ice`, async (message) => {
                const ice = JSON.parse(message.body);
                await handleIceCandidate(ice);
            });
        }
        
        async function startCall() {
            try {
                status.textContent = '📹 Starting call...';
                
                // Get user media
                localStream = await navigator.mediaDevices.getUserMedia({
                    video: true,
                    audio: true
                });
                localVideo.srcObject = localStream;
                
                // Get ICE servers
                const response = await fetch('http://localhost:8080/api/webrtc/ice-servers');
                const { iceServers } = await response.json();
                
                // Create peer connection
                peerConnection = new RTCPeerConnection({ iceServers });
                
                // Add local stream
                localStream.getTracks().forEach(track => {
                    peerConnection.addTrack(track, localStream);
                });
                
                // Handle remote stream
                peerConnection.ontrack = (event) => {
                    remoteVideo.srcObject = event.streams[0];
                    status.textContent = '🎉 Call connected!';
                };
                
                // Handle ICE candidates
                peerConnection.onicecandidate = (event) => {
                    if (event.candidate) {
                        const userId = document.getElementById('userId').value;
                        const remoteUserId = document.getElementById('remoteUserId').value;
                        
                        stompClient.publish({
                            destination: '/app/webrtc/ice',
                            body: JSON.stringify({
                                fromUserId: userId,
                                toUserId: remoteUserId,
                                candidate: JSON.stringify(event.candidate)
                            })
                        });
                    }
                };
                
                // Create and send offer
                const offer = await peerConnection.createOffer();
                await peerConnection.setLocalDescription(offer);
                
                const userId = document.getElementById('userId').value;
                const remoteUserId = document.getElementById('remoteUserId').value;
                
                stompClient.publish({
                    destination: '/app/webrtc/offer',
                    body: JSON.stringify({
                        fromUserId: userId,
                        toUserId: remoteUserId,
                        sdp: offer.sdp
                    })
                });
                
                status.textContent = '📞 Calling...';
                
            } catch (error) {
                console.error('Error starting call:', error);
                status.textContent = '❌ Error: ' + error.message;
            }
        }
        
        async function handleOffer(offer) {
            try {
                status.textContent = '📞 Incoming call...';
                
                // Get user media
                localStream = await navigator.mediaDevices.getUserMedia({
                    video: true,
                    audio: true
                });
                localVideo.srcObject = localStream;
                
                // Get ICE servers
                const response = await fetch('http://localhost:8080/api/webrtc/ice-servers');
                const { iceServers } = await response.json();
                
                // Create peer connection
                peerConnection = new RTCPeerConnection({ iceServers });
                
                // Add local stream
                localStream.getTracks().forEach(track => {
                    peerConnection.addTrack(track, localStream);
                });
                
                // Handle remote stream
                peerConnection.ontrack = (event) => {
                    remoteVideo.srcObject = event.streams[0];
                    status.textContent = '🎉 Call connected!';
                };
                
                // Handle ICE candidates
                peerConnection.onicecandidate = (event) => {
                    if (event.candidate) {
                        const userId = document.getElementById('userId').value;
                        
                        stompClient.publish({
                            destination: '/app/webrtc/ice',
                            body: JSON.stringify({
                                fromUserId: userId,
                                toUserId: offer.fromUserId,
                                candidate: JSON.stringify(event.candidate)
                            })
                        });
                    }
                };
                
                // Set remote description and create answer
                await peerConnection.setRemoteDescription(
                    new RTCSessionDescription({ type: 'offer', sdp: offer.sdp })
                );
                
                const answer = await peerConnection.createAnswer();
                await peerConnection.setLocalDescription(answer);
                
                // Send answer
                const userId = document.getElementById('userId').value;
                stompClient.publish({
                    destination: '/app/webrtc/answer',
                    body: JSON.stringify({
                        fromUserId: userId,
                        toUserId: offer.fromUserId,
                        sdp: answer.sdp
                    })
                });
                
            } catch (error) {
                console.error('Error handling offer:', error);
                status.textContent = '❌ Error: ' + error.message;
            }
        }
        
        async function handleAnswer(answer) {
            if (peerConnection) {
                await peerConnection.setRemoteDescription(
                    new RTCSessionDescription({ type: 'answer', sdp: answer.sdp })
                );
            }
        }
        
        async function handleIceCandidate(ice) {
            if (peerConnection) {
                const candidate = JSON.parse(ice.candidate);
                await peerConnection.addIceCandidate(new RTCIceCandidate(candidate));
            }
        }
        
        function endCall() {
            if (peerConnection) {
                peerConnection.close();
                peerConnection = null;
            }
            if (localStream) {
                localStream.getTracks().forEach(track => track.stop());
                localStream = null;
            }
            localVideo.srcObject = null;
            remoteVideo.srcObject = null;
            status.textContent = '📴 Call ended';
        }
    </script>
</body>
</html>
```

### 🧪 **Cách test demo HTML:**

1. **Lưu file** `test-webrtc.html`
2. **Mở 2 tab browser** (hoặc 2 máy)
3. **Tab 1**: User ID = 1, Call User ID = 5
4. **Tab 2**: User ID = 5, Call User ID = 1  
5. **Tab 1 nhấn "Start Video Call"**
6. **Tab 2 sẽ tự động nhận cuộc gọi**
7. **Thấy video 2 bên** → Thành công! 🎉

### 🎯 **Kết quả mong đợi:**
- ✅ Thấy camera của mình (local video)
- ✅ Thấy camera của bạn (remote video)  
- ✅ Nghe được âm thanh 2 chiều
- ✅ Kết nối P2P trực tiếp (không qua server)
- ✅ Hoạt động trên cùng mạng LAN

**→ Đây chính là cơ sở để build thành app React hoàn chỉnh như Zalo!**

---

## 7. Core Components

### `src/components/rooms/RoomList.jsx`

```javascript
import { useState, useEffect } from 'react';
import { roomApi } from '../../api/roomApi';
import { useNavigate } from 'react-router-dom';

export default function RoomList() {
  const [rooms, setRooms] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const loadRooms = async () => {
      const data = await roomApi.getAll();
      setRooms(data);
    };
    loadRooms();
  }, []);

  return (
    <div className="room-list">
      <h3>Phòng chat</h3>
      <ul>
        {rooms.map(room => (
          <li key={room.chatRoomId} onClick={() => navigate(`/chat/room/${room.chatRoomId}`)}>
            <strong>{room.roomName}</strong>
            <small>{room.membersCount} thành viên</small>
          </li>
        ))}
      </ul>
    </div>
  );
}
```

---

## 9. Styling & UI

### `src/App.css` - Basic styles

```css
/* Global */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
  background: #f0f2f5;
}

/* Login/Register */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-form {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.15);
  width: 90%;
  max-width: 400px;
}

.login-form h2 {
  margin-bottom: 1.5rem;
  color: #333;
  text-align: center;
}

.login-form input {
  width: 100%;
  padding: 12px;
  margin-bottom: 1rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
}

.login-form button {
  width: 100%;
  padding: 12px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.3s;
}

.login-form button:hover {
  background: #5568d3;
}

/* Chat Layout */
.chat-layout {
  display: flex;
  height: 100vh;
}

.sidebar {
  width: 300px;
  background: white;
  border-right: 1px solid #e0e0e0;
  overflow-y: auto;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* Chat Room */
.chat-room {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: white;
}

.chat-header {
  padding: 1rem;
  background: #667eea;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* Messages */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
  background: #f5f5f5;
}

.message {
  margin-bottom: 1rem;
  display: flex;
  flex-direction: column;
  max-width: 70%;
}

.message.sent {
  align-self: flex-end;
  align-items: flex-end;
}

.message.received {
  align-self: flex-start;
  align-items: flex-start;
}

.message-content {
  background: #667eea;
  color: white;
  padding: 10px 14px;
  border-radius: 18px;
  word-wrap: break-word;
}

.message.received .message-content {
  background: #e4e6eb;
  color: #050505;
}

.message-meta {
  font-size: 11px;
  color: #65676b;
  margin-top: 4px;
}

/* Message Input */
.message-input {
  display: flex;
  padding: 1rem;
  background: white;
  border-top: 1px solid #e0e0e0;
}

.message-input input {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 20px;
  font-size: 15px;
  outline: none;
}

.message-input button {
  margin-left: 10px;
  padding: 10px 24px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-weight: 600;
}

.message-input button:hover {
  background: #5568d3;
}

/* Video Call */
.video-call-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #1a1a1a;
  z-index: 1000;
}

.video-grid {
  display: grid;
  grid-template-columns: 1fr;
  height: calc(100% - 80px);
  gap: 10px;
  padding: 10px;
}

.video-box {
  position: relative;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
}

.video-box video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.video-box.local {
  position: absolute;
  bottom: 80px;
  right: 20px;
  width: 200px;
  height: 150px;
  z-index: 10;
}

.call-controls {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px;
  background: rgba(0,0,0,0.7);
  display: flex;
  justify-content: center;
  gap: 20px;
}

.btn-call, .btn-end-call {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-call {
  background: #4caf50;
  color: white;
}

.btn-end-call {
  background: #f44336;
  color: white;
}

/* Room List */
.room-list ul {
  list-style: none;
}

.room-list li {
  padding: 12px 16px;
  border-bottom: 1px solid #e0e0e0;
  cursor: pointer;
  transition: background 0.2s;
}

.room-list li:hover {
  background: #f5f5f5;
}

.room-list strong {
  display: block;
  font-size: 15px;
  margin-bottom: 4px;
}

.room-list small {
  color: #65676b;
  font-size: 13px;
}
```

---

## 10. Deployment

### Build production

```bash
npm run build
```

### Deploy options:

**Vercel (khuyên dùng - miễn phí):**
```bash
npm install -g vercel
vercel
```

**Netlify:**
```bash
npm install -g netlify-cli
netlify deploy --prod
```

**Nginx (VPS):**
```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /var/www/chatapp/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }

    location /ws {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
    }
}
```

---

## 🎯 Checklist hoàn chỉnh Frontend

### Phase 1: Basic Setup (1-2 ngày)
- [ ] Setup Vite/CRA project
- [ ] Cài dependencies
- [ ] Tạo folder structure
- [ ] Setup axios với interceptors
- [ ] Implement AuthContext
- [ ] Login/Register pages
- [ ] Protected routes

### Phase 2: Core Chat (2-3 ngày)
- [ ] Room list component
- [ ] Message list với scroll
- [ ] Message input với typing indicator
- [ ] WebSocket integration (STOMP)
- [ ] Realtime message receiving
- [ ] Mark seen/delivered

### Phase 3: Advanced Features (2-3 ngày)
- [ ] Friend management UI
- [ ] Create/manage rooms
- [ ] Upload media (images/files)
- [ ] Notifications bell icon
- [ ] Search messages
- [ ] Edit/delete messages

### Phase 4: WebRTC Call (2-3 ngày)
- [ ] Video call component
- [ ] Audio call
- [ ] Mute/unmute controls
- [ ] Screen share (optional)
- [ ] Call history

### Phase 5: Polish (1-2 ngày)
- [ ] Responsive design
- [ ] Dark mode
- [ ] Loading states
- [ ] Error boundaries
- [ ] Accessibility
- [ ] Performance optimization

---

## 🚀 Quick Start Example

### Minimal working example (5 phút)

**`src/App.jsx`:**
```javascript
import { useState, useEffect } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

function App() {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [client, setClient] = useState(null);

  useEffect(() => {
    const socket = new SockJS('http://localhost:8080/ws');
    const stompClient = new Client({ webSocketFactory: () => socket });
    
    stompClient.onConnect = () => {
      console.log('Connected');
      stompClient.subscribe('/topic/rooms/1', (msg) => {
        setMessages(prev => [...prev, JSON.parse(msg.body)]);
      });
    };
    
    stompClient.activate();
    setClient(stompClient);
    
    return () => stompClient.deactivate();
  }, []);

  const send = () => {
    if (client?.connected && input.trim()) {
      client.publish({
        destination: '/app/rooms/1/send',
        body: JSON.stringify({ senderId: 1, content: input })
      });
      setInput('');
    }
  };

  return (
    <div style={{ padding: 20 }}>
      <h1>Simple Chat</h1>
      <div style={{ border: '1px solid #ccc', height: 400, overflowY: 'auto', marginBottom: 10, padding: 10 }}>
        {messages.map((msg, i) => (
          <div key={i}>{msg.content}</div>
        ))}
      </div>
      <input value={input} onChange={e => setInput(e.target.value)} onKeyPress={e => e.key === 'Enter' && send()} />
      <button onClick={send}>Send</button>
    </div>
  );
}

export default App;
```

Chạy: `npm run dev` → mở `http://localhost:5173`

---

## 📚 Tài liệu tham khảo

- [React Router](https://reactrouter.com/)
- [STOMP.js](https://stomp-js.github.io/stomp-websocket/)
- [WebRTC API](https://developer.mozilla.org/en-US/docs/Web/API/WebRTC_API)
- [Axios Interceptors](https://axios-http.com/docs/interceptors)

---

## 💡 Tips & Best Practices

### Security
- Không lưu password trong state
- Clear tokens khi logout
- Validate input trước khi gửi
- Sanitize HTML nếu render user content

### Performance
- Lazy load components: `React.lazy()`
- Virtualize long lists: `react-window`
- Debounce typing indicator
- Optimize re-renders: `React.memo()`, `useMemo()`

### UX
- Show loading states
- Error boundaries cho crash handling
- Offline detection
- Retry failed requests
- Optimistic UI updates

### WebSocket
- Auto-reconnect khi mất kết nối
- Queue messages khi offline
- Show connection status
- Handle reconnect logic

---

## 🎨 UI Libraries (Optional)

**Component Libraries:**
- Material-UI: `npm install @mui/material @emotion/react @emotion/styled`
- Ant Design: `npm install antd`
- Chakra UI: `npm install @chakra-ui/react @emotion/react @emotion/styled`

**Styling:**
- TailwindCSS: `npm install -D tailwindcss postcss autoprefixer`
- Styled Components: `npm install styled-components`

**Icons:**
- React Icons: `npm install react-icons`

---

## ⚡ Optimization Tips

### Code Splitting
```javascript
const ChatRoom = React.lazy(() => import('./components/chat/ChatRoom'));
const VideoCall = React.lazy(() => import('./components/call/VideoCall'));

<Suspense fallback={<div>Loading...</div>}>
  <ChatRoom />
</Suspense>
```

### Memoization
```javascript
const MessageItem = React.memo(({ message }) => {
  return <div>{message.content}</div>;
});
```

### Virtual Scrolling (for 1000+ messages)
```javascript
import { FixedSizeList } from 'react-window';

<FixedSizeList
  height={600}
  itemCount={messages.length}
  itemSize={60}
>
  {({ index, style }) => (
    <div style={style}>
      <MessageItem message={messages[index]} />
    </div>
  )}
</FixedSizeList>
```

---

## 🔧 Environment Variables

### `.env.development`
```env
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_URL=http://localhost:8080/ws
```

### `.env.production`
```env
VITE_API_BASE_URL=https://api.your-domain.com
VITE_WS_URL=https://api.your-domain.com/ws
```

---

## 🎯 Kết luận

### Ước lượng thời gian:
- **Minimal viable product**: 3-5 ngày
- **Full-featured app**: 7-10 ngày
- **Production-ready**: 15-20 ngày

### Priority order:
1. ✅ Auth + Protected routes (1 ngày)
2. ✅ Basic chat UI (2 ngày)
3. ✅ WebSocket realtime (1 ngày)
4. ✅ Friend system (1 ngày)
5. ⭐ WebRTC calling (2-3 ngày)
6. ⭐ Polish & optimize (2-3 ngày)

**Bắt đầu từ minimal example trên, sau đó mở rộng dần theo checklist!**




