<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="javax.servlet.http.HttpSession"%>
<%@ page import="java.text.SimpleDateFormat" %>
<% String ctx = request.getContextPath(); %>
<%
    HttpSession sessio = request.getSession(false);
    String rolx = (sessio != null) ? (String) sessio.getAttribute("rol") : null;
    String version = new SimpleDateFormat("yyyy-MMddHHmmss").format(new java.util.Date());
%>

<!DOCTYPE html><html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width,initial-scale=1">
        <title>ManagerTM · Iniciar sesión</title>
        <link rel="icon" type="image/svg+xml" href="<%=ctx%>/assets/managertm-icon.svg">
        <link rel="stylesheet" href="<%=ctx%>/css/login.css">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    </head>
    <body>
        <div class="login-layout">
            <aside class="login-brand-side">
                <div class="brand-mark"><img src="<%=ctx%>/assets/managertm-icon.svg" alt=""><span>ManagerTM</span></div>
                <h1>El control de tu operación, en un solo lugar.</h1>
                <p>Infraestructura, agentes inteligentes y supervisión centralizada para la dirección de Tío Musa.</p>
                <div class="brand-foot">TÍO MUSA · PLATAFORMA INTERNA</div>
            </aside>
            <main class="login-form-side">
                <div class="login-card">
                    <div class="eyebrow">Acceso corporativo</div>
                    <h2>Bienvenido</h2><p class="description">Ingresá con tus credenciales para acceder al panel de dirección.</p>
                    <% if ("1".equals(request.getParameter("error"))) { %>
                    <div class="login-error" role="alert">No se pudo iniciar sesión. Verificá tus credenciales.</div><% }%>
                    <%-- Cambiar /login por la ruta real de tu servlet autenticador. --%>
                    <form method="post" action="<%=ctx%>/login">
                        <div class="field">
                            <label for="username">Usuario</label>
                            <input id="username" name="username" type="text" autocomplete="username" required maxlength="120" placeholder="Tu usuario">
                        </div><div class="field">
                            <label for="password">Contraseña</label>
                            <input id="password" name="password" type="password" autocomplete="current-password" required placeholder="Tu contraseña">
                        </div>
                        <button id="submitBtn" class="login-submit" type="submit">Ingresar al panel →</button>
                    </form>
                    <div class="login-note">
                        Acceso exclusivo para personal autorizado.<br>Las operaciones pueden ser auditadas.</div>
                </div>
            </main>
            <div id="mfa-overlay"
                 class="mfa-overlay"
                 style="display:none;"
                 role="dialog"
                 aria-modal="true"
                 aria-labelledby="mfa-title"
                 aria-hidden="true">

                <div id="mfa-box" class="mfa-box">

                    <div class="mfa-icon">
                        <i class="bi bi-shield-lock"></i>
                    </div>

                    <div class="mfa-eyebrow">
                        SEGURIDAD MANAGERTM
                    </div>

                    <h3 id="mfa-title">Verificación en dos pasos</h3>

                    <p class="mfa-description">
                        Abrí Google Authenticator e ingresá
                        el código de 6 dígitos para continuar.
                    </p>

                    <label for="mfa-code">
                        Código de autenticación
                    </label>

                    <input
                        type="text"
                        id="mfa-code"
                        class="mfa-input"
                        placeholder="000000"
                        maxlength="6"
                        inputmode="numeric"
                        pattern="[0-9]{6}"
                        autocomplete="one-time-code">

                    <div id="mfa-error"
                         class="mfa-error"
                         role="alert"></div>

                    <div class="mfa-actions">
                        <button type="button"
                                id="btnCloseMfa"
                                class="mfa-btn-secondary">
                            Cancelar
                        </button>

                        <button type="button"
                                id="btnConfirmMfa"
                                class="mfa-btn-primary">
                            Validar código
                        </button>
                    </div>

                    <div class="mfa-footer">
                        <i class="bi bi-lock"></i>
                        Acceso protegido
                    </div>

                </div>
            </div>        
        </div>
        <script src="js/jquery.js"></script>
        <script>
        const ctx = '${pageContext.request.contextPath}';

        const loginForm = document.querySelector(".login-card form");
        const usernameInput = document.getElementById("username");
        const passwordInput = document.getElementById("password");
        const submitBtn = document.getElementById("submitBtn");

        const mfaOverlay = document.getElementById("mfa-overlay");
        const mfaCodeInput = document.getElementById("mfa-code");
        const btnConfirmMfa = document.getElementById("btnConfirmMfa");
        const btnCloseMfa = document.getElementById("btnCloseMfa");

        let usuarioPendienteMfa = null;
        let procesando = false;

        function mostrarError(mensaje) {
            let errorDiv = document.getElementById("loginErrorJs");

            if (!errorDiv) {
                errorDiv = document.createElement("div");
                errorDiv.id = "loginErrorJs";
                errorDiv.className = "login-error";
                errorDiv.setAttribute("role", "alert");

                loginForm.before(errorDiv);
            }

            errorDiv.textContent = mensaje;
            errorDiv.style.display = "block";
        }

        function limpiarError() {
            const errorDiv = document.getElementById("loginErrorJs");

            if (errorDiv) {
                errorDiv.style.display = "none";
                errorDiv.textContent = "";
            }
        }

        function mostrarErrorMfa(mensaje) {
            document.getElementById("mfa-error").textContent = mensaje;
        }

        function abrirModalMFA() {
            mfaCodeInput.value = "";
            mostrarErrorMfa("");

            mfaOverlay.style.display = "flex";
            mfaOverlay.setAttribute("aria-hidden", "false");

            mfaCodeInput.focus();
        }

        function cerrarModalMFA() {
            mfaOverlay.style.display = "none";
            mfaOverlay.setAttribute("aria-hidden", "true");

            mfaCodeInput.value = "";
            mostrarErrorMfa("");
        }

        function setProcesando(valor, esMfa = false) {
            procesando = valor;

            submitBtn.disabled = valor;
            btnConfirmMfa.disabled = valor;
            btnCloseMfa.disabled = valor;

            submitBtn.textContent =
                    valor && !esMfa
                    ? "Autenticando..."
                    : "Ingresar al panel →";

            btnConfirmMfa.textContent =
                    valor && esMfa
                    ? "Validando..."
                    : "Validar código";
        }

        async function solicitarLogin(usuario, password, tipo) {
            const params = new URLSearchParams();

            params.append("userN", usuario);
            params.append("passW", password);
            params.append("type", tipo);

            const response = await fetch(ctx + "/Login", {
                method: "POST",
                credentials: "include",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: params.toString()
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                        data.message || "Error HTTP " + response.status
                        );
            }

            return data;
        }

        function loginExitoso() {
            usernameInput.value = "";
            passwordInput.value = "";
            mfaCodeInput.value = "";

            window.location.replace(ctx + "/verificarToken");
        }

        async function iniciarSesion(event) {
            event.preventDefault();

            if (procesando)
                return;

            limpiarError();

            const usuario = usernameInput.value.trim();
            const password = passwordInput.value;

            if (!usuario || !password) {
                mostrarError("Ingresá tu usuario y contraseña.");
                return;
            }

            setProcesando(true);

            try {
                const data = await solicitarLogin(
                        usuario,
                        password,
                        "login"
                        );

                if (data.success === true && !data.requireMfa) {
                    loginExitoso();
                    return;
                }

                if (data.requireMfa === true) {
                    usuarioPendienteMfa = usuario;
                    passwordInput.value = "";
                    abrirModalMFA();
                    return;
                }

                mostrarError(
                        data.message || "Usuario o contraseña incorrectos."
                        );

            } catch (error) {
                console.error("Error de autenticación:", error);
                mostrarError(
                        "No se pudo iniciar sesión. " + error.message
                        );
            } finally {
                setProcesando(false);
            }
        }

        async function confirmarMFA(event) {
            event.preventDefault();

            if (procesando)
                return;

            const codigo = mfaCodeInput.value.trim();

            if (!/^\d{6}$/.test(codigo)) {
                mostrarErrorMfa("Ingresá un código de 6 dígitos.");
                mfaCodeInput.focus();
                return;
            }

            if (!usuarioPendienteMfa) {
                mostrarErrorMfa(
                        "La sesión de autenticación no es válida. Volvé a ingresar."
                        );
                return;
            }

            mostrarErrorMfa("");
            setProcesando(true, true);

            try {
                const data = await solicitarLogin(
                        usuarioPendienteMfa,
                        codigo,
                        "code"
                        );

                if (data.success === true && !data.requireMfa) {
                    loginExitoso();
                    return;
                }

                mostrarErrorMfa(
                        data.message || "El código ingresado no es válido."
                        );

                mfaCodeInput.value = "";
                mfaCodeInput.focus();

            } catch (error) {
                console.error("Error MFA:", error);
                mostrarErrorMfa(
                        "No se pudo validar el código. Intentá nuevamente."
                        );
            } finally {
                setProcesando(false, true);
            }
        }

        loginForm.addEventListener("submit", iniciarSesion);

        submitBtn.addEventListener("click", iniciarSesion);

        btnConfirmMfa.addEventListener("click", confirmarMFA);

        btnCloseMfa.addEventListener("click", () => {
            if (procesando)
                return;

            cerrarModalMFA();
            usuarioPendienteMfa = null;
        });

        mfaCodeInput.addEventListener("keydown", event => {
            if (event.key === "Enter") {
                confirmarMFA(event);
            }
        });

        mfaCodeInput.addEventListener("input", () => {
            mfaCodeInput.value =
                    mfaCodeInput.value.replace(/\D/g, "").slice(0, 6);

            mostrarErrorMfa("");
        });

        </script>
    </body>
</html>