
class WebChat {

    constructor() {

        // ============================
        // CONEXION WEBSOCKET
        // ============================

        this.socket = null;
        this.token = null;

        this.connected = false;
        this.connecting = false;

        this.reconnectTimer = null;
        this.heartbeatInterval = null;

        // ============================
        // ESTADO GENERAL
        // ============================

        this.initialized = false;
        this.initializing = null;

        this.mode = 'AI';

        this.conversationId = null;
        this.aiConversationId = null;

        this.currentUserId = null;
        this.activePeer = null;

        this.users = [];
        this.conversations = [];

        this.vozActiva = false;

        // ============================
        // SPAN BUBBLES
        // ============================

        this.unreadBadge = document.getElementById('chatUnreadBadge');

        this.unreadMessageKeys = new Set();
        this.unreadSequence = 0;

        // ============================
        // HISTORIAL
        // ============================

        this.messageIds = new Set();
        this.pending = new Map();

        this.oldestMessageId = null;

        this.hasMoreMessages = true;
        this.loadingHistory = false;
        this.initialHistoryLoaded = false;

        this.historyVersion = 0;
        this.historyAbort = null;

        // ============================
        // ELEMENTOS HTML
        // ============================

        this.button = document.getElementById('chatButton');
        this.window = document.getElementById('chatWindow');
        this.closeButton = document.getElementById('chatClose');

        this.messages = document.getElementById('chatMessages');
        this.input = document.getElementById('chatInput');
        this.sendButton = document.getElementById('chatSend');

        this.status = document.getElementById('chatStatus');
        this.typing = document.getElementById('chatTyping');
        this.loading = document.getElementById('chatLoading');

        this.btnvoz = document.getElementById('btnVoz');
        this.title = document.getElementById('chatTitle');

        this.peoplePanel = document.getElementById('chatPeoplePanel');
        this.peopleList = document.getElementById('chatPeopleList');
        this.search = document.getElementById('chatPeopleSearch');

        this.back = document.getElementById('chatBack');
        this.peopleButton = document.getElementById('chatPeopleButton');
        this.aiButton = document.getElementById('chatAiButton');

        this.registerEvents();
    }

    // ==========================================
    // EVENTOS
    // ==========================================

    registerEvents() {

        this.button?.addEventListener('click', () => {
            this.open();
        });

        this.closeButton?.addEventListener('click', () => {
            this.close();
        });

        this.sendButton?.addEventListener('click', () => {
            this.send();
        });

        this.input?.addEventListener('keydown', e => {

            if (e.key === 'Enter' &&
                    !e.shiftKey &&
                    !e.isComposing) {

                e.preventDefault();
                this.send();
            }
        });

        this.messages?.addEventListener('scroll', () => {
            this.handleScroll();
        });

        this.btnvoz?.addEventListener('click', () => {
            this.toggleVoz();
        });

        this.peopleButton?.addEventListener('click', async () => {
            await this.showPeople();
        });

        this.back?.addEventListener('click', async () => {
            await this.showPeople();
        });

        this.aiButton?.addEventListener('click', async () => {
            await this.selectAI();
        });

        this.search?.addEventListener('input', () => {
            this.renderPeople();
        });
    }

    // ==========================================
    // API HTTP
    // ==========================================

    async api(action, params = {}, signal = undefined) {

        const response = await fetch(
                contextPath + '/ProcesoDatosChat',
                {
                    method: 'POST',
                    credentials: 'include',
                    signal,
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        Accion: action,
                        ...params
                    })
                }
        );

        if (response.status === 401) {
            throw new Error('Sesión vencida');
        }

        if (!response.ok) {

            let detail = '';

            try {
                const error = await response.json();
                detail = error.error ?? error.message ?? '';
            } catch (_) {
            }

            throw new Error(
                    'HTTP ' + response.status +
                    (detail ? ': ' + detail : '')
                    );
        }

        const result = await response.json();

        if (result?.success === false) {
            throw new Error(
                    result.message ?? 'Error del servidor'
                    );
        }

        return result?.data ?? result?.Data ?? result;
    }

    // ==========================================
    // ABRIR / CERRAR
    // ==========================================
    async initializeNotifications() {
        try {
            await this.loadInitialData();
            this.updateUnreadBadge();
        } catch (error) {
            console.error(
                    'Error inicializando notificaciones:',
                    error
                    );
        }
    }
    async open() {

        if (!this.window)
            return;

        this.window.classList.remove('chat-hidden');
        //buble
        this.clearUnreadNotifications();
        this.button?.setAttribute(
                'aria-expanded',
                'true'
                );

        if (!this.initialized) {

            if (!this.initializing) {

                this.initializing = (async () => {

                    await this.loadInitialData();

                    this.initialized = true;

                    await this.selectAI();

                })().catch(error => {

                    console.error(
                            'Inicialización chat:',
                            error
                            );

                    this.initialized = false;

                    this.setStatus(
                            'Error cargando datos'
                            );

                }).finally(() => {
                    this.initializing = null;
                });
            }

            await this.initializing;
        }

        if (!this.connected && !this.connecting) {
            await this.startChat();
        }

        this.input?.focus();
    }

    close() {

        this.window?.classList.add('chat-hidden');

        this.button?.setAttribute(
                'aria-expanded',
                'false'
                );
    }

    // ==========================================
    // DATOS INICIALES
    // ==========================================

    async loadInitialData() {

        const data = await this.api(
                'GetInternalChatInitialData'
                );

        const currentUserId =
                data.currentUserId ??
                data.CurrentUserId;

        this.currentUserId =
                currentUserId == null
                ? null
                : String(currentUserId);

        this.aiConversationId =
                data.aiConversationId ??
                data.AiConversationId ??
                data.AIConversationId ??
                this.aiConversationId;

        const rawUsers =
                data.users ??
                data.Users ??
                [];

        this.users = Array.isArray(rawUsers)
                ? rawUsers.map(u => ({
                        id: u.id ?? u.Id,
                        name: u.name ?? u.Name ?? '',
                        department:
                                u.department ??
                                u.Department ??
                                ''
                    })).filter(u => u.id != null)
                : [];

        const rawConversations =
                data.conversations ??
                data.Conversations ??
                [];

        this.conversations =
                Array.isArray(rawConversations)
                ? rawConversations.map(c =>
                    this.normalizeConversation(c)
                )
                : [];

        console.log('Chat inicial:', {
            currentUserId: this.currentUserId,
            aiConversationId: this.aiConversationId,
            users: this.users.length,
            conversations: this.conversations.length
        });

        this.renderPeople();
        this.updateUnreadBadge();
    }

    normalizeConversation(c) {

        return {
            conversationId:
                    c.conversationId ??
                    c.ConversationId ??
                    c.id ??
                    c.Id,

            peerUserId:
                    c.peerUserId ??
                    c.PeerUserId,

            peerName:
                    c.peerName ??
                    c.PeerName ??
                    '',

            lastMessage:
                    c.lastMessage ??
                    c.LastMessage ??
                    '',

            lastMessageAt:
                    c.lastMessageAt ??
                    c.LastMessageAt ??
                    '',

            unreadCount:
                    Number(
                            c.unreadCount ??
                            c.UnreadCount ??
                            0
                            )
        };
    }

    async refreshInitial() {

        try {

            const mode = this.mode;
            const conversationId = this.conversationId;
            const peerId = this.activePeer?.id;

            const data = await this.api(
                    'GetInternalChatInitialData'
                    );

            const rawUsers =
                    data.users ??
                    data.Users ??
                    [];

            const rawConversations =
                    data.conversations ??
                    data.Conversations ??
                    [];

            const currentId =
                    data.currentUserId ??
                    data.CurrentUserId;

            if (currentId != null) {
                this.currentUserId = String(currentId);
            }

            if (Array.isArray(rawUsers)) {
                this.users = rawUsers.map(u => ({
                        id: u.id ?? u.Id,
                        name: u.name ?? u.Name ?? '',
                        department:
                                u.department ??
                                u.Department ??
                                ''
                    }));
            }

            if (Array.isArray(rawConversations)) {
                this.conversations =
                        rawConversations.map(c =>
                            this.normalizeConversation(c)
                        );
            }

            this.renderPeople();
            this.updateUnreadBadge();

        } catch (error) {
            console.error(
                    'Actualizando contactos:',
                    error
                    );
        }
    }

    // ==========================================
    // PANEL DE EMPLEADOS
    // ==========================================

    async showPeople() {

        this.peoplePanel?.classList.remove(
                'chat-hidden'
                );

        this.messages?.classList.add(
                'chat-hidden'
                );

        this.typing?.classList.add(
                'chat-hidden'
                );

        this.back?.classList.add(
                'chat-hidden'
                );

        if (this.title) {
            this.title.textContent = 'Empleados';
        }

        await this.refreshInitial();

        this.renderPeople();
    }

    showMessages() {

        this.peoplePanel?.classList.add(
                'chat-hidden'
                );

        this.messages?.classList.remove(
                'chat-hidden'
                );

        this.back?.classList.remove(
                'chat-hidden'
                );
    }

    renderPeople() {

        if (!this.peopleList)
            return;

        this.peopleList.replaceChildren();

        const filter = (
                this.search?.value ?? ''
                ).trim().toLocaleLowerCase('es');

        const conversationsByPeer = new Map(
                this.conversations.map(c => [
                        String(c.peerUserId),
                        c
                    ])
                );

        const users = this.users
                .filter(u =>
                    String(u.id) !== this.currentUserId
                )
                .filter(u =>
                    String(u.name ?? '')
                            .toLocaleLowerCase('es')
                            .includes(filter)
                );

        users.sort((a, b) => {

            const da =
                    conversationsByPeer.get(
                            String(a.id)
                            )?.lastMessageAt ?? '';

            const db =
                    conversationsByPeer.get(
                            String(b.id)
                            )?.lastMessageAt ?? '';

            return db.localeCompare(da) ||
                    String(a.name).localeCompare(
                    String(b.name),
                    'es'
                    );
        });

        if (!users.length) {

            const empty =
                    document.createElement('div');

            empty.className =
                    'chat-people-empty';

            empty.textContent =
                    'No hay empleados para mostrar.';

            this.peopleList.append(empty);

            return;
        }

        for (const user of users) {

            const conv =
                    conversationsByPeer.get(
                            String(user.id)
                            );
            const button =
                    document.createElement('button');

            button.type = 'button';
            button.className = 'chat-person';

            const name =
                    document.createElement('strong');

            name.textContent =
                    user.name || 'Empleado';

            const detail =
                    document.createElement('small');

            detail.textContent =
                    conv?.lastMessage ||
                    user.department ||
                    'Iniciar conversación';

            button.append(name, detail);

            if (Number(conv?.unreadCount) > 0) {

                const badge =
                        document.createElement('span');

                badge.className =
                        'chat-person-unread';

                badge.textContent =
                        String(conv.unreadCount);

                button.append(badge);
            }

            button.addEventListener(
                    'click',
                    () => this.selectEmployee(user)
            );

            this.peopleList.append(button);
        }
    }

    // ==========================================
    // LIMPIAR MENSAJES
    // ==========================================

    resetMessages() {

        this.historyVersion++;

        this.historyAbort?.abort();
        this.historyAbort = null;

        if (this.messages) {

            this.messages.replaceChildren();

            // El indicador de carga está dentro
            // del contenedor de mensajes.
            if (this.loading) {
                this.messages.append(this.loading);
            }
        }

        this.messageIds.clear();

        this.oldestMessageId = null;
        this.hasMoreMessages = true;
        this.loadingHistory = false;
        this.initialHistoryLoaded = false;

        this.showLoading(false);
        this.showTyping(false);
    }

    // ==========================================
    // SELECCIONAR IA
    // ==========================================

    async selectAI() {

        this.mode = 'AI';

        this.activePeer = null;

        this.conversationId =
                this.aiConversationId;

        this.resetMessages();

        this.showMessages();

        if (this.title) {
            this.title.textContent =
                    'ManagerTM · Asistente IA';
        }

        if (this.btnvoz) {
            this.btnvoz.hidden = false;
        }

        if (this.input) {
            this.input.disabled = false;
        }

        if (this.conversationId != null) {
            await this.requestHistory();
        }
    }

    // ==========================================
    // SELECCIONAR EMPLEADO
    // ==========================================

    async selectEmployee(user) {

        this.mode = 'DIRECT';
        this.activePeer = user;

        this.conversationId = null;

        this.resetMessages();
        this.showMessages();

        if (this.title) {
            this.title.textContent =
                    user.name || 'Empleado';
        }

        if (this.btnvoz) {
            this.btnvoz.hidden = true;
        }

        if (this.input) {
            this.input.disabled = true;
        }

        const version = this.historyVersion;

        try {

            const data = await this.api(
                    'GetOrCreateDirectConversation',
                    {
                        Parametro1: user.id
                    }
            );

            if (version !== this.historyVersion ||
                    this.mode !== 'DIRECT') {
                return;
            }

            const conversationId =
                    data.conversationId ??
                    data.ConversationId;

            if (conversationId == null) {
                throw new Error(
                        'La API no devolvió conversationId'
                        );
            }

            this.conversationId = conversationId;

            this.upsertConversation({
                conversationId,
                peerUserId: user.id,
                peerName: user.name
            });

            if (this.input) {
                this.input.disabled = false;
            }

            await this.requestHistory();

            await this.markConversationRead(
                    conversationId
                    );

        } catch (error) {

            console.error(
                    'Seleccionar empleado:',
                    error
                    );

            if (version === this.historyVersion) {
                this.setStatus(
                        'No se pudo abrir conversación'
                        );
            }
        }
    }

    upsertConversation(conv) {

        const normalized =
                this.normalizeConversation(conv);

        const id =
                String(normalized.conversationId);

        const index =
                this.conversations.findIndex(
                        c => String(c.conversationId) === id
                );

        if (index >= 0) {

            this.conversations[index] = {
                ...this.conversations[index],
                ...normalized
            };

        } else {

            this.conversations.unshift(
                    normalized
                    );
        }

        this.renderPeople();
    }

    async markConversationRead(conversationId) {

        if (conversationId == null)
            return;

        try {

            await this.api(
                    'MarkDirectConversationRead',
                    {
                        Parametro1: conversationId
                    }
            );

            const conv = this.conversations.find(
                    c => String(c.conversationId) ===
                        String(conversationId)
            );

            if (conv) {
                conv.unreadCount = 0;
            }

            this.renderPeople();

        } catch (error) {

            console.warn(
                    'Marcar conversación leída:',
                    error
                    );
        }
    }

    // ==========================================
    // NORMALIZAR MENSAJES
    // ==========================================

    normalizeMessage(m, mode = this.mode) {

        let role = String(
                m.role ??
                m.Role ??
                m.senderType ??
                m.SenderType ??
                m.sender ??
                m.Sender ??
                ''
                ).toUpperCase();

        if (role === 'CUSTOMER') {
            role = 'USER';
        }

        if (role === 'AI') {
            role = 'ASSISTANT';
        }

        const senderId =
                m.senderUserId ??
                m.SenderUserId;

        const mine = mode === 'DIRECT'
                ? (
                        senderId != null &&
                        String(senderId) ===
                        String(this.currentUserId)
                        )
                : role === 'USER';

        return {
            id: m.id ?? m.Id ?? null,

            clientMessageId:
                    m.clientMessageId ??
                    m.ClientMessageId ??
                    m.externalMessageId ??
                    m.ExternalMessageId ??
                    m.messageId ??
                    m.MessageId ??
                    null,

            role,
            mine,

            text:
                    m.text ??
                    m.Text ??
                    m.content ??
                    m.Content ??
                    m.message ??
                    m.Message ??
                    '',

            createdAt:
                    m.createdAt ??
                    m.CreatedAt ??
                    m.timestamp ??
                    m.Timestamp ??
                    null
        };
    }

    // ==========================================
    // HISTORIAL HTTP
    // ==========================================

    async requestHistory() {

        if (this.loadingHistory ||
                !this.hasMoreMessages) {
            return;
        }

        if (this.conversationId == null) {
            return;
        }

        const version = this.historyVersion;
        const mode = this.mode;
        const conversationId = this.conversationId;
        const beforeId = this.oldestMessageId;

        const controller = new AbortController();

        this.historyAbort = controller;
        this.loadingHistory = true;

        this.showLoading(true);

        const oldHeight =
                this.messages.scrollHeight;

        const oldTop =
                this.messages.scrollTop;

        try {

            const data = await this.api(
                    'GetUnifiedChatMessages',
                    {
                        Parametro1: mode,
                        Parametro2: conversationId,
                        Parametro3: beforeId ?? '',
                        Parametro4: 30
                    },
                    controller.signal
                    );

            if (version !== this.historyVersion ||
                    mode !== this.mode ||
                    String(conversationId) !==
                    String(this.conversationId)) {
                return;
            }

            const raw = Array.isArray(data)
                    ? data
                    : (
                            data.messages ??
                            data.Messages ??
                            []
                            );

            const normalized = raw.map(m =>
                this.normalizeMessage(m, mode)
            );

            // El backend devuelve los mensajes
            // ordenados ascendentemente por Id.

            for (
                    let i = normalized.length - 1;
                    i >= 0;
                    i--
                    ) {

                this.addMessage(
                        normalized[i],
                        false,
                        true
                        );
            }

            if (normalized.length) {

                this.oldestMessageId =
                        normalized[0].id;
            }

            this.hasMoreMessages =
                    Array.isArray(data)
                    ? normalized.length === 30
                    : (
                            data.hasMore ??
                            data.HasMore ??
                            false
                            );

            if (!this.initialHistoryLoaded) {

                this.initialHistoryLoaded = true;
                this.scrollToBottom();

            } else {

                this.messages.scrollTop =
                        this.messages.scrollHeight -
                        oldHeight +
                        oldTop;
            }

            if (mode === 'DIRECT' &&
                    normalized.length) {

                this.markConversationRead(
                        conversationId
                        );
            }

        } catch (error) {

            if (error.name !== 'AbortError') {

                console.error(
                        'Error recuperando historial:',
                        error
                        );

                this.setStatus(
                        'Error cargando historial'
                        );
            }

        } finally {

            if (version === this.historyVersion) {

                this.loadingHistory = false;
                this.showLoading(false);
            }

            if (this.historyAbort === controller) {
                this.historyAbort = null;
            }
        }
    }

    // ==========================================
    // SCROLL / PAGINACION
    // ==========================================

    handleScroll() {

        if (!this.messages ||
                this.messages.classList.contains(
                        'chat-hidden'
                        )) {
            return;
        }

        if (!this.initialHistoryLoaded) {
            return;
        }

        if (this.messages.scrollTop <= 50 &&
                !this.loadingHistory &&
                this.hasMoreMessages) {

            this.requestHistory();
        }
    }

    // ==========================================
    // MOSTRAR MENSAJES
    // ==========================================

    addMessage(m, scroll = false, prepend = false) {

        if (!this.messages)
            return;

        const id = m.id == null
                ? null
                : String(m.id);

        if (id && this.messageIds.has(id)) {
            return;
        }

        // Reconciliar mensaje provisional
        // con el persistido por la API.

        if (m.clientMessageId) {

            const provisional = [
                ...this.messages.querySelectorAll(
                        '[data-client-message-id]'
                        )
            ].find(el =>
                el.dataset.clientMessageId ===
                        String(m.clientMessageId)
            );

            if (provisional) {

                if (id) {
                    provisional.dataset.messageId = id;
                    this.messageIds.add(id);
                }

                provisional.querySelector(
                        '.chat-pending'
                        )?.remove();

                return;
            }
        }

        if (id) {
            this.messageIds.add(id);
        }

        const row =
                document.createElement('div');

        row.className =
                'chat-message ' +
                (
                        m.mine
                        ? 'chat-message-user'
                        : 'chat-message-assistant'
                        );

        if (id) {
            row.dataset.messageId = id;
        }

        if (m.clientMessageId) {

            row.dataset.clientMessageId =
                    String(m.clientMessageId);
        }

        const content =
                document.createElement('span');

        content.textContent = m.text ?? '';

        row.append(content);

        if (m.createdAt) {

            const time =
                    document.createElement('span');

            time.className =
                    'chat-message-time';

            time.textContent =
                    this.formatTime(m.createdAt);

            row.append(time);
        }

        if (prepend) {
            this.messages.prepend(row);
        } else {
            this.messages.append(row);
        }

        if (scroll) {
            this.scrollToBottom();
    }
    }

    // ==========================================
    // WEBSOCKET
    // ==========================================

    async startChat() {

        if (this.connecting || this.connected) {
            return;
        }

        this.connecting = true;

        this.setStatus('Conectando...');

        try {

            const response = await fetch(
                    contextPath + '/chat/token',
                    {
                        method: 'POST',
                        credentials: 'include'
                    }
            );

            if (!response.ok) {
                throw new Error(
                        'No se pudo obtener token'
                        );
            }

            const data = await response.json();

            if (!data.token) {
                throw new Error('Token vacío');
            }

            this.token = data.token;

            this.connectWebSocket();

        } catch (error) {

            this.connecting = false;

            this.setStatus('Sin conexión');

            console.error(
                    'Error conectando WebSocket:',
                    error
                    );
        }
    }

    connectWebSocket() {

        const protocol =
                location.protocol === 'https:'
                ? 'wss:'
                : 'ws:';

        const url =
                protocol +
                '//' +
                location.host +
                '/ws/chat?token=' +
                encodeURIComponent(this.token);

        const ws = new WebSocket(url);

        this.socket = ws;

        ws.onopen = () => {

            if (this.socket !== ws)
                return;

            this.connected = true;
            this.connecting = false;

            this.setStatus('En línea');

            if (this.sendButton) {
                this.sendButton.disabled = false;
            }

            this.startHeartbeat();
        };

        ws.onmessage = event => {

            try {

                const data =
                        JSON.parse(event.data);

                this.handleServerMessage(data);

            } catch (error) {

                console.error(
                        'Evento WebSocket inválido:',
                        error
                        );
            }
        };

        ws.onerror = error => {

            console.error(
                    'Error WebSocket:',
                    error
                    );

            this.setStatus(
                    'Error de conexión'
                    );
        };

        ws.onclose = () => {

            if (this.socket !== ws)
                return;

            this.connected = false;
            this.connecting = false;

            this.socket = null;

            this.stopHeartbeat();

            if (this.sendButton) {
                this.sendButton.disabled = true;
            }

            this.setStatus('Desconectado');

            if (
                    this.window &&
                    !this.window.classList.contains(
                            'chat-hidden'
                            )
                    ) {

                this.scheduleReconnect();
            }
        };
    }

    // ==========================================
    // RECONEXION
    // ==========================================

    async refreshCurrentHistory() {

        if (this.conversationId == null) {
            return;
        }

        this.resetMessages();

        await this.requestHistory();
    }

    scheduleReconnect() {

        if (this.reconnectTimer)
            return;

        this.reconnectTimer = setTimeout(
                () => {

            this.reconnectTimer = null;

            if (!this.connected &&
                    !this.connecting &&
                    this.window &&
                    !this.window.classList.contains(
                            'chat-hidden'
                            )) {

                this.startChat();
            }

        },
                3000
                );
    }

    startHeartbeat() {

        this.stopHeartbeat();

        this.heartbeatInterval = setInterval(
                () => {

            if (
                    this.socket?.readyState ===
                    WebSocket.OPEN
                    ) {

                this.socket.send(
                        JSON.stringify({
                            type: 'PING'
                        })
                        );
            }

        },
                25000
                );
    }

    stopHeartbeat() {

        if (this.heartbeatInterval) {
            clearInterval(
                    this.heartbeatInterval
                    );
        }

        this.heartbeatInterval = null;
    }

    // ==========================================
    // ENVIAR MENSAJES
    // ==========================================

    send() {

        const text =
                this.input?.value.trim();

        if (!text ||
                !this.connected ||
                this.socket?.readyState !==
                WebSocket.OPEN) {
            return;
        }

        if (this.mode === 'DIRECT' &&
                this.conversationId == null) {
            return;
        }

        const messageId =
                crypto.randomUUID();

        const mode = this.mode;
        const conversationId =
                this.conversationId;

        const payload = mode === 'DIRECT'
                ? {
                    type: 'SEND_DIRECT_MESSAGE',
                    conversationId,
                    messageId,
                    text
                }
        : {
            type: 'MESSAGE',
            messageId,
            text
        };

        this.pending.set(messageId, {
            mode,
            conversationId,
            text
        });

        try {

            this.socket.send(
                    JSON.stringify(payload)
                    );

        } catch (error) {

            this.pending.delete(messageId);

            console.error(
                    'Error enviando mensaje:',
                    error
                    );

            return;
        }

        this.addMessage(
                {
                    id: null,
                    clientMessageId: messageId,
                    role: 'USER',
                    mine: true,
                    text,
                    createdAt:
                            new Date().toISOString()
                },
                true
                );

        const provisional = [
            ...this.messages.querySelectorAll(
                    '[data-client-message-id]'
                    )
        ].find(el =>
            el.dataset.clientMessageId ===
                    messageId
        );

        if (provisional) {

            const state =
                    document.createElement('small');

            state.className = 'chat-pending';

            state.textContent =
                    ' · Enviando';

            provisional.append(state);
        }

        this.input.value = '';
    }

    // ==========================================
    // EVENTOS DEL SERVIDOR
    // ==========================================

    handleServerMessage(data) {

        switch (data.type) {

            case 'CONNECTED':
            case 'CONVERSATION_CREATED':
            {

                const id =
                        data.conversationId ??
                        data.ConversationId;

                if (id != null) {

                    const changed =
                            String(this.aiConversationId) !==
                            String(id);

                    this.aiConversationId = id;

                    if (this.mode === 'AI') {

                        this.conversationId = id;

                        if (changed &&
                                this.initialHistoryLoaded) {

                            this.refreshCurrentHistory();

                        } else if (
                                !this.initialHistoryLoaded &&
                                !this.loadingHistory
                                ) {

                            this.requestHistory();
                        }
                    }
                }

                // Recuperar mensajes perdidos
                // después de una reconexión.
                if (
                        data.type === 'CONNECTED' &&
                        this.initialHistoryLoaded
                        ) {

                    this.refreshCurrentHistory();
                }

                break;
            }

            case 'MESSAGE':
            {

                if (this.mode !== 'AI') {
                    return;
                }

                if (
                        data.conversationId != null &&
                        this.aiConversationId != null &&
                        String(data.conversationId) !==
                        String(this.aiConversationId)
                        ) {
                    return;
                }

                const message = this.normalizeMessage(
                        data,
                        'AI'
                        );
                //buble
                this.registerUnreadMessage(
                        message,
                        'AI',
                        this.aiConversationId
                        );

                this.addMessage(message, true);

                if (
                        this.vozActiva &&
                        message.role === 'ASSISTANT'
                        ) {

                    this.hablar(
                            message.text
                            );
                }

                break;
            }

            case 'DIRECT_MESSAGE_CREATED':
            {

                const cid =
                        String(data.conversationId);

                const rawMessage =
                        data.message ??
                        data.Message;

                if (!rawMessage)
                    return;

                const message =
                        this.normalizeMessage(
                                rawMessage,
                                'DIRECT'
                                );
                //buble
                this.registerUnreadMessage(
                        message,
                        'DIRECT',
                        data.conversationId
                        );

                const conv =
                        this.conversations.find(
                                c =>
                            String(c.conversationId) ===
                                    cid
                        );

                if (conv) {

                    conv.lastMessage =
                            message.text;

                    conv.lastMessageAt =
                            message.createdAt ??
                            new Date().toISOString();

                    if (
                            this.mode !== 'DIRECT' ||
                            String(this.conversationId) !==
                            cid
                            ) {

                        if (!message.mine) {
                            conv.unreadCount =
                                    Number(
                                            conv.unreadCount ?? 0
                                            ) + 1;
                        }
                    }
                }

                this.renderPeople();

                if (
                        this.mode === 'DIRECT' &&
                        String(this.conversationId) ===
                        cid
                        ) {

                    if (data.clientMessageId) {

                        message.clientMessageId =
                                data.clientMessageId;
                    }

                    this.addMessage(
                            message,
                            true
                            );

                    this.markConversationRead(cid);
                }

                if (data.clientMessageId) {

                    this.pending.delete(
                            data.clientMessageId
                            );
                }

                // Actualizar conversaciones,
                // contactos y mensajes no leídos.
                this.refreshInitial();

                break;
            }

            case 'ACK':
            {

                const messageId =
                        data.messageId ??
                        data.MessageId;

                this.pending.delete(
                        messageId
                        );

                const row = [
                    ...this.messages.querySelectorAll(
                            '[data-client-message-id]'
                            )
                ].find(el =>
                    el.dataset.clientMessageId ===
                            String(messageId)
                );

                const state =
                        row?.querySelector(
                                '.chat-pending'
                                );

                if (state) {
                    state.textContent =
                            ' · Recibido';
                }

                // ACK confirma recepción inicial,
                // no necesariamente persistencia.
                break;
            }

            case 'MESSAGE_ERROR':
            case 'ERROR':
            {

                console.error(
                        'Error del chat:',
                        data.message
                        );

                this.setStatus(
                        'Error enviando mensaje'
                        );

                break;
            }

            case 'TYPING':
            {

                if (this.mode === 'AI') {

                    this.showTyping(
                            data.value ??
                            data.isTyping ??
                            false
                            );
                }

                break;
            }

            case 'PONG':
                break;

                // El historial se maneja por HTTP.
            case 'HISTORY':
                break;

            default:

                console.debug(
                        'Evento chat:',
                        data.type
                        );
        }
    }

    // ==========================================
    // INTERFAZ
    // ==========================================

    scrollToBottom() {

        if (!this.messages)
            return;

        requestAnimationFrame(() => {

            this.messages.scrollTop =
                    this.messages.scrollHeight;
        });
    }

    showTyping(value) {

        this.typing?.classList.toggle(
                'chat-hidden',
                !value
                );
    }

    showLoading(value) {

        this.loading?.classList.toggle(
                'chat-hidden',
                !value
                );
    }

    setStatus(value) {

        if (!this.status)
            return;

        this.status.textContent = value;

        this.status.classList.toggle(
                'connected',
                value === 'En línea'
                );
    }

    formatTime(value) {

        if (!value)
            return '';

        return new Date(value)
                .toLocaleTimeString(
                        'es-AR',
                        {
                            hour: '2-digit',
                            minute: '2-digit'
                        }
                );
    }

    // ==========================================
    // VOZ
    // ==========================================

    hablar(text) {

        if (!('speechSynthesis' in window)) {
            return;
        }

        speechSynthesis.cancel();

        const utterance =
                new SpeechSynthesisUtterance(text);

        utterance.lang = 'es-AR';

        speechSynthesis.speak(
                utterance
                );
    }

    toggleVoz() {

        this.vozActiva =
                !this.vozActiva;

        if (this.btnvoz) {

            this.btnvoz.textContent =
                    this.vozActiva
                    ? '🔇 Desactivar voz'
                    : '🔊 Activar voz';
        }

        if (!this.vozActiva) {
            window.speechSynthesis?.cancel();
        }
    }
    //buble
    isChatVisible() {
        return this.window &&
                !this.window.classList.contains('chat-hidden');
    }

    registerUnreadMessage(message, mode, conversationId) {

        // No contar mensajes enviados por nosotros.
        if (message.mine)
            return;

        // Si la conversación está visible, no hay
        // necesidad de notificarla como no leída.
        const currentConversation =
                this.mode === mode &&
                String(this.conversationId) === String(conversationId);

        if (this.isChatVisible() && currentConversation) {
            return;
        }

        const key = message.id != null
                ? `${mode}:${conversationId}:${message.id}`
                : `${mode}:${conversationId}:event:${++this.unreadSequence}`;

        this.unreadMessageKeys.add(key);
        this.updateUnreadBadge();
    }

    clearUnreadNotifications() {
        this.unreadMessageKeys.clear();
        this.updateUnreadBadge();
    }

    updateUnreadBadge() {

        if (!this.unreadBadge)
            return;

        const totalDirect = this.conversations.reduce(
                (total, conv) =>
            total + Number(conv.unreadCount ?? 0),
                0
                );

        const count = totalDirect;

        this.unreadBadge.hidden = count === 0;

        this.unreadBadge.textContent =
                count > 99 ? '99+' : String(count);

        this.button?.setAttribute(
                'aria-label',
                count > 0
                ? `Abrir chat, ${count} mensajes sin leer`
                : 'Abrir chat'
                );
    }
}

// ==========================================
// INICIALIZACION UNICA
// ==========================================

document.addEventListener(
        'DOMContentLoaded',
        () => {

    if (!window.webChat) {

        window.webChat =
                new WebChat();
    }
    window.webChat.initializeNotifications();
}
);
