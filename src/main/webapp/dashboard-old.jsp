<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%-- Requiere que el servlet de autenticación haya creado una sesión válida. --%>
<%
    String ctx = request.getContextPath();
%>
<!DOCTYPE html><html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>ManagerTM · Panel de dirección</title><link rel="icon" type="image/svg+xml" href="<%=ctx%>/assets/managertm-icon.svg">
        <link rel="stylesheet" href="<%=ctx%>/css/managertm.css"></head>
    <body>
        <div class="app-shell">
            <aside class="sidebar" id="sidebar">
                <div class="brand">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7" rx="1"/>
                    <rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/>
                    <rect x="14" y="14" width="7" height="7" rx="1"/></svg><span class="brand-name">ManagerTM</span></div>
                <nav class="side-nav" aria-label="Navegación principal">
                    <button class="nav-item active" data-view="dashboard" data-title="Panel de dirección" title="Dashboard">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/>
                        <rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/>
                        </svg><span>Dashboard</span></button>
                    <button class="nav-item" data-view="servidores" data-title="Servidores" title="Servidores"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="8" rx="2"/><rect x="3" y="13" width="18" height="8" rx="2"/><path d="M7 7h.01M7 17h.01"/></svg><span>Servidores</span></button>
                    <button class="nav-item" data-view="agentes" data-title="Agentes IA" title="Agentes IA"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="4" y="7" width="16" height="13" rx="3"/><path d="M12 3v4M9 13h.01M15 13h.01M9 17h6"/></svg><span>Agentes IA</span></button>
                    <button class="nav-item" data-view="supervision" data-title="Supervisión" title="Supervisión"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="5" y="4" width="14" height="18" rx="2"/><path d="M9 4V2h6v2M9 13l2 2 4-4"/></svg><span>Supervisión</span></button>
                    <button class="nav-item" data-view="informes" data-title="Informes" title="Informes"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 21V11h4v10M10 21V3h4v18M16 21v-7h4v7M2 21h20"/></svg><span>Informes</span></button>
                    <button class="nav-item" data-view="usuarios" data-title="Administración de usuarios" title="Usuarios">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="9" cy="8" r="3"/><path d="M3 20v-2a6 6 0 0 1 12 0v2M17 8h5M19.5 5.5v5"/></svg>
                        <span>Usuarios</span>
                    </button>
                </nav>
                <div class="sidebar-bottom"><button class="collapse-btn" id="collapseSidebar" aria-expanded="true" title="Contraer menú">☰ <span class="collapse-label">Contraer menú</span></button></div></aside>
            <main class="workspace"><header class="topbar"><span id="pageTitle">Panel de dirección</span><div class="topbar-actions"><button class="icon-button" aria-label="Notificaciones" title="Notificaciones (pendiente de integrar)"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4"/></svg></button></div></header>
                <div class="page-content"><section class="view active" id="view-dashboard"><div class="metrics"><article class="metric-card"><div class="metric-label">Servicios</div><div class="metric-value" id="metricServices">—</div></article><article class="metric-card"><div class="metric-label">Alertas</div><div class="metric-value" id="metricAlerts">—</div></article><article class="metric-card"><div class="metric-label">Tareas</div><div class="metric-value" id="metricTasks">—</div></article><article class="metric-card"><div class="metric-label">Agentes</div><div class="metric-value" id="metricAgents">—</div></article></div><section class="panel"><h2 class="panel-title">Estado de infraestructura</h2><div class="panel-row"><span class="panel-subtitle">Monitor de servicios</span><span class="status-icon" aria-label="Estado pendiente de consulta"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M5 14h4l3-6 3 8 2-4h3"/></svg></span></div><div class="panel-placeholder" id="serverMonitor">Esperando estado de servicios...</div></section></section>
                    <section class="view" id="view-servidores"><div class="section-card"><h2>Servidores</h2><p>Espacio para integrar tu monitor de servicios existente y el detalle de salud de cada componente.</p><div id="serversDetail"><div id="serverMonitorFull" class="monitor-container"><header class="monitor-header"><div><div class="header-title">Monitor de infraestructura</div><div class="header-subtitle">Estado de servicios y bases de datos</div></div><span id="monitorGlobal" class="global-status loading"><span class="status-dot"></span><span id="monitorGlobalText">Consultando...</span></span></header><div class="summary"><div class="summary-card"><strong class="summary-number" id="monitorTotal">—</strong><span class="summary-label">TOTAL</span></div><div class="summary-card"><strong class="summary-number online-text" id="monitorOnline">—</strong><span class="summary-label">EN LÍNEA</span></div><div class="summary-card"><strong class="summary-number offline-text" id="monitorOffline">—</strong><span class="summary-label">FUERA DE LÍNEA</span></div></div><div class="services-section"><div class="section-header"><span>Servicios monitoreados</span><button type="button" class="refresh-button" id="refreshMonitor">Actualizar</button></div><div class="services-container" id="monitorServices"><div class="loading-message">Esperando consulta...</div></div></div><footer class="monitor-footer"><span id="monitorUpdated">Sin actualizar</span><span>Actualización automática cada 5 minutos</span></footer></div></div></div></section>
                    <section class="view" id="view-agentes"><div class="section-card"><h2>Agentes IA</h2><p>Administración de agentes internos, herramientas y prompts autorizados.</p><div id="agentsModule"></div></div></section>
                    <section class="view" id="view-supervision"><div class="section-card"><h2>Supervisión</h2><p>Tareas, confirmaciones, vencimientos y escalaciones de tus agentes.</p><div id="supervisionModule"></div></div></section>
                    <section class="view" id="view-informes"><div class="section-card"><h2>Informes</h2><p>Indicadores de operaciones y reportes de dirección.</p><div id="reportsModule"></div></div></section></div>
                <section class="view" id="view-usuarios">
                    <div class="ua">
                        <div class="ua-head"><div><h2>Personas y accesos</h2><div class="ua-sub">Personal · Usuarios de ManagerTM · Roles y seguridad</div></div>
                            <div><button type="button" class="ua-btn" id="uaRefresh">↻ Actualizar</button> <button type="button" class="ua-btn primary" id="uaNew">+ Nuevo usuario</button></div></div>
                        <div class="ua-metrics"><div class="ua-stat"><span>Personal registrado</span><strong id="uaTotal">—</strong></div>
                            <div class="ua-stat"><span>Usuarios activos</span><strong id="uaActiveCount">—</strong></div>
                            <div class="ua-stat"><span>Sin cuenta</span><strong id="uaPending">—</strong></div></div>
                        <div id="uaNotice" class="ua-alert" hidden></div>
                        <div class="ua-card"><div class="ua-toolbar"><input class="ua-input" id="uaSearch" placeholder="Buscar por legajo, nombre, usuario..." aria-label="Buscar personal">
                                <select class="ua-select" id="uaArea"><option value="">Todas las áreas</option></select>
                                <select class="ua-select" id="uaStatus"><option value="all">Todos</option><option value="active">Usuarios activos</option><option value="inactive">Usuarios inactivos</option><option value="pending">Sin usuario</option></select></div>
                            <div class="ua-sub" id="uaCount" aria-live="polite"></div>
                            <div class="ua-tablewrap"><table class="ua-table"><thead><tr><th>Empleado</th><th>Área / Puesto</th><th>Sucursal</th><th>Usuario / Roles</th><th>Estado</th><th>Acciones</th></tr></thead><tbody id="uaRows"><tr><td colspan="6" class="ua-empty">Abrí el módulo para cargar el personal</td></tr></tbody></table></div>
                        </div>
                        <div class="ua-overlay" id="uaModal" hidden><div class="ua-modal" role="dialog" aria-modal="true" aria-labelledby="uaModalTitle">
                                <div class="ua-modalhead"><h3 id="uaModalTitle">Crear usuario</h3><button type="button" id="uaClose" class="ua-close" aria-label="Cerrar">×</button></div>
                                <p id="uaEmployee" class="ua-sub"></p><form id="uaForm">
                                    <div id="uaUsernameBox" class="ua-field"><label for="uaUsername">Nombre de usuario</label><input class="ua-input" id="uaUsername" autocomplete="off" maxlength="80" required></div>
                                    <div id="uaPasswordBox" class="ua-field"><label for="uaPassword">Contraseña temporal (mínimo 12 caracteres)</label><input class="ua-input" id="uaPassword" type="password" autocomplete="new-password" minlength="12"></div>
                                    <div id="uaRolesBox" class="ua-field"><label>Roles</label><div class="ua-roles" id="uaRoles"></div></div>
                                    <div id="uaActiveBox" class="ua-field" hidden><label><input type="checkbox" id="uaEnabled"> Usuario habilitado</label></div>
                                    <div id="uaModalError" class="ua-alert error" hidden></div>
                                    <div class="ua-footer"><button type="button" id="uaCancel" class="ua-btn">Cancelar</button><button type="submit" id="uaSubmit" class="ua-btn primary">Guardar</button></div>
                                </form></div></div>
                    </div>


                </section>
            </main>
        </div>
        <button class="chat-launcher chat-button" id="chatButton" aria-label="Abrir chat interno" aria-expanded="false" title="Chat interno">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path d="M20 11.5a8 8 0 0 1-8 8 8.5 8.5 0 0 1-4-.9L3 20l1.4-4.7A8 8 0 1 1 20 11.5Z"/></svg>
            <span id="chatUnreadBadge"
                  class="chat-unread-badge"
                  hidden></span>
        </button>

        <section class="chat-panel chat-window chat-hidden" id="chatWindow" aria-label="Chat interno">
            <div class="chat-header">
                <div class="chat-header-main">
                    <button type="button" id="chatBack" class="chat-hidden" aria-label="Volver a empleados">‹</button>
                    <div><strong id="chatTitle">ManagerTM · Asistente IA</strong>
                        <div class="chat-status" id="chatStatus">Desconectado</div>
                    </div>
                </div>
                <button type="button" id="chatClose" aria-label="Cerrar chat">×</button>
            </div>
            <div class="chat-navigation">
                <button type="button" id="chatAiButton">🤖 Asistente IA</button>
                <button type="button" id="chatPeopleButton">👥 Empleados</button>
            </div>
            <div id="chatPeoplePanel" class="chat-people-panel chat-hidden">
                <input type="search" id="chatPeopleSearch" placeholder="Buscar empleado..." autocomplete="off" aria-label="Buscar empleado">
                <div id="chatPeopleList" class="chat-people-list"></div>
            </div>
            <div class="chat-messages" id="chatMessages">
                <div class="chat-loading chat-hidden" id="chatLoading">Cargando historial...</div>
            </div>
            <div class="chat-typing chat-hidden" id="chatTyping">El asistente está escribiendo...</div>
            <div class="chat-input-container">
                <textarea id="chatInput" rows="2" maxlength="4000" placeholder="Escribí un mensaje..."></textarea>
                <button id="chatSend" type="button" disabled aria-label="Enviar mensaje">➤</button>
            </div>
            <button type="button" id="btnVoz" class="chat-voice">🔊 Activar voz</button>
        </section>
        <script>const contextPath = '<%=ctx%>';</script>
        <link rel="stylesheet" href="<%=ctx%>/css/monitor-managertm.css">
        <link rel="stylesheet" href="<%=ctx%>/css/webchat-managertm.css">
        <link rel="stylesheet" href="<%=ctx%>/css/admin-usuarios.css">
        <script src="<%=ctx%>/js/WebChat.js"></script>
        <script src="<%=ctx%>/js/managertm.js"></script>
        <script src="<%=ctx%>/js/admin-usuarios.js"></script>
        <script src="<%=ctx%>/js/monitor-managertm.js"></script>

    </body>
</html>