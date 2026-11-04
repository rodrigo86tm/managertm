<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.text.SimpleDateFormat" %>
<% 
    /*HttpSession sessio = request.getSession(false);
    String rolx = (sessio != null) ? (String)sessio.getAttribute("rol") : null;*/
    String version = new SimpleDateFormat("yyyy-MMddHHmmss").format(new java.util.Date());
%>

<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        
        <title>Panel Logística - Dashboard</title> 
        <link href="css/webchat.css?v=<%=version%>" rel="stylesheet">
        <link href="css/monitor.css?v=<%=version%>" rel="stylesheet">
        <link href="css/menu.css?v=<%=version%>" rel="stylesheet">
        <script src="https://cdn.jsdelivr.net/npm/echarts@5.0.2/dist/echarts.min.js"></script>
        <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
        <link href="https://maxcdn.bootstrapcdn.com/font-awesome/4.2.0/css/font-awesome.min.css" rel="stylesheet">
        <script src="https://cdnjs.cloudflare.com/ajax/libs/modernizr/2.8.3/modernizr.min.js" type="text/javascript"></script>
        <script src="https://unpkg.com/eva-icons"></script>
    </head>
    <body>    
        <nav id="navmenu">
            <a href="#" title="Menu" onclick="showSection('Main')">
              <span class="fa fa-bars"></span>
              <span class="label" >Menu</span>
            </a>
            <a href="#" title="Gestion" onclick="showSection('Server')">
              <span class="fa fa-truck"></span>
              <span class="label" >Server</span>
            </a>        
            <a href="#" title="Controls" onclick="showSection('Precios')">
              <span class="fa fa-barcode"></span>
              <span class="label">Precios</span>
            </a>
            <a href="#" title="Etiquetas" onclick="showSection('Procesos')">
              <span class="fa fa-qrcode"></span>
              <span class="label">Procesos</span>
            </a> 
            <button id="btnVoz" onclick="toggleVoz()">
                🔊 Activar voz
            </button>
        </nav>
        <div id="Main" class="content active">
            <%@ include file="jsp/Main.jsp" %>
        </div>
        <div id="Server" class="content">
            <%@ include file="jsp/Server.jsp" %>
        </div>
        <div id="Precios" class="content">
            <%@ include file="jsp/Precios.jsp" %>
        </div>
        <div id="Procesos" class="content">
            <%@ include file="jsp/Procesos.jsp" %>
        </div>
        <div id="webChat">
            <!-- Botón flotante -->
            <button id="chatButton"
                    class="chat-button"
                    type="button"
                    aria-label="Abrir chat">

                <span class="chat-icon">💬</span>

            </button>


            <!-- Ventana -->
            <div id="chatWindow"
                 class="chat-window chat-hidden">

                <!-- Header -->
                <div class="chat-header">

                    <div class="chat-header-info">

                        <div class="chat-title">
                            Asistente virtual
                        </div>

                        <div id="chatStatus"
                             class="chat-status">
                            Desconectado
                        </div>

                    </div>

                    <button id="chatClose"
                            class="chat-close"
                            type="button">
                        ×
                    </button>

                </div>


                <!-- Mensajes -->
                <div id="chatMessages"
                     class="chat-messages">

                    <div id="chatLoading"
                         class="chat-loading chat-hidden">
                        Cargando...
                    </div>

                </div>


                <!-- Typing -->
                <div id="chatTyping"
                     class="chat-typing chat-hidden">

                    El asistente está escribiendo...

                </div>


                <!-- Input -->
                <div class="chat-input-container">

                    <textarea id="chatInput"
                              class="chat-input"
                              rows="1"
                              placeholder="Escribí un mensaje..."
                              maxlength="4000"></textarea>

                    <button id="chatSend"
                            class="chat-send"
                            type="button">

                        ➤

                    </button>

                </div>

            </div>

        </div>
        <div id="loadingOverlay" class="loading">
            <div class="loading-back">
                ⏳ Procesando, por favor espere...
            </div>
        </div>
        <script src="js/jquery.js"></script>
        <script src="js/bootstrap.min.js"></script>        
        <script src="js/WebChat.js?v=<%=version%>"></script>
        <script>  var contextPath = '<%= request.getContextPath() %>'; </script>
        <script> eva.replace(); </script>
        <script>
            const confirmBeep = document.getElementById("confirmation-sound");
            const okTic = document.getElementById("Ok-sound");
            const errorBeep = document.getElementById("beepOk-sound");
            const okBeep = document.getElementById("errorW-sound");
            let currentSection;   
            let conversationId = null;
            let oldestMessageId = null;
            let loadingHistory = false;
            let hasMoreMessages = true;
            let socket = null;
            function mostrarLoading() {
                document.getElementById("loadingOverlay").style.display = "flex";
            }
            function ocultarLoading() {
                document.getElementById("loadingOverlay").style.display = "none";
            } 
            function getCookie(name) {
                const value = `; ${document.cookie}`;
                const parts = value.split(`; ${name}=`);
                if (parts.length === 2) return parts.pop().split(';').shift();
            }            
            function floatLabel(inputType){
                $(inputType).each(function(){
                        var $this = $(this);
                        // on focus add cladd active to label
                        $this.focus(function(){
                                $this.next().addClass("actives");
                        });
                        //on blur check field and remove class if needed
                        $this.blur(function(){
                            if($this.attr('type') !== 'date'){
                                if($this.val() === '' || $this.val() === 'blank'){
                                        $this.next().removeClass();
                                }
                            }
                        });
                        if ($this.is('select')) {
                            if ($this.val() !== '' && $this.val() !== 'blank') {
                                $this.next().addClass("actives");
                            } else {
                                $this.next().removeClass("actives");
                            }
                        }
                });
            }
            function initSection(sectionId) {
                console.log(sectionId);
                switch(sectionId) {
                    case 'SeccionRegistro':
                        floatLabel(".floatLabel"); 
                        break;                    
                    case 'Login':
                        iniciarLogin(); // si existe esta función
                        break;
                    // Más secciones...
                }
            }            
            function showSection(sectionId){
                const newSection = document.getElementById(sectionId);
                if (!newSection) return;
                var forbiden = ['SeccionRegistro','Reimpresiones'];
                if(forbiden.includes(sectionId)){
                    const rol = getCookie("rol");
                    if(rol === 'Logistica') return;
                }
                if (currentSection === newSection) return;
                    
                if (currentSection) {
                    currentSection.classList.add("exit-left");
                    setTimeout(() => {
                        currentSection.classList.remove("active", "exit-left");
                        currentSection.style.visibility = "hidden";
                        newSection.classList.add("active");
                        newSection.style.visibility = "visible";
                        currentSection = newSection;
                        initSection(sectionId);
                    }, 500);
                } else {
                    newSection.classList.add("active");
                    newSection.style.visibility = "visible";
                    currentSection = newSection;
                    initSection(sectionId);
                }               
            }    
            async function openChat() {
                document
                    .getElementById("chatWindow")
                    .classList.remove("chat-hidden");

                if (!conversationId) {
                    await createSession();
                    await loadInitialMessages();
                    connectWebSocket();
                }
            }
            document.addEventListener("DOMContentLoaded",
                () => {
                    window.webChat = new WebChat();
                    currentSection =
                        document.getElementById("Server");

                    currentSection.classList.add("active");
                    currentSection.style.visibility = "visible";

                    ocultarLoading();

                    floatLabel(".floatLabel");

                    // listeners...
                }
            );          
        </script>
        
    </body>
</html>
