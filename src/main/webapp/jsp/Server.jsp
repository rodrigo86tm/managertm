<div class="monitor-container">

    <!-- HEADER -->
    <header class="monitor-header">

        <div>
            <div class="header-title">
                SYSTEM MONITOR
            </div>

            <div class="header-subtitle">
                Infraestructura Tío Musa
            </div>
        </div>

        <div id="globalStatus"
             class="global-status loading">

            <span class="status-dot"></span>

            <span id="globalStatusText">
                Cargando...
            </span>

        </div>

    </header>


    <!-- RESUMEN -->
    <section class="summary">

        <div class="summary-card">

            <span id="totalServices"
                  class="summary-number">
                -
            </span>

            <span class="summary-label">
                SERVICIOS
            </span>

        </div>


        <div class="summary-card">

            <span id="onlineServices"
                  class="summary-number online-text">
                -
            </span>

            <span class="summary-label">
                ONLINE
            </span>

        </div>


        <div class="summary-card">

            <span id="offlineServices"
                  class="summary-number offline-text">
                -
            </span>

            <span class="summary-label">
                OFFLINE
            </span>

        </div>

    </section>


    <!-- SERVICIOS -->
    <section class="services-section">

        <div class="section-header">

            <span>Servicios</span>

            <button id="refreshButton"
                    class="refresh-button"
                    onclick="loadStatus()">

                ? Actualizar

            </button>

        </div>


        <div id="servicesContainer"
             class="services-container">

            <div class="loading-message">
                Consultando estado de los servicios...
            </div>

        </div>

    </section>


    <!-- FOOTER -->
    <footer class="monitor-footer">

        <div>
            Última comprobación:
            <span id="lastCheck">-</span>
        </div>

        <div>
            Próxima actualización:
            <span id="nextCheck">05:00</span>
        </div>

    </footer>

</div>


<script>  

    <%--const REFRESH_INTERVAL =
        5 * 60 * 1000;

    let nextRefresh =
        Date.now() + REFRESH_INTERVAL;


    async function loadStatus() {

        const button =
            document.getElementById("refreshButton");

        button.classList.add("rotating");

        try {

            const response =
                await fetch('${pageContext.request.contextPath}/Monitor', {
                    method: "GET",
                    cache: "no-store"
                });

            if (!response.ok) {
                throw new Error(
                    "HTTP " + response.status
                );
            }

            const data =
                await response.json();

            renderStatus(data);

            nextRefresh =
                Date.now() + REFRESH_INTERVAL;

        }
        catch (error) {

            console.error(
                "Error consultando monitor:",
                error
            );

            showConnectionError();

        }
        finally {

            button.classList.remove("rotating");

        }
    }


    function renderStatus(data) {

        document.getElementById(
            "totalServices"
        ).textContent = data.Total;

        document.getElementById(
            "onlineServices"
        ).textContent = data.Online;

        document.getElementById(
            "offlineServices"
        ).textContent = data.Offline;

        updateGlobalStatus(data);

        updateLastCheck(data.CheckedAt);

        renderServices(data.Services);
    }


    function updateGlobalStatus(data) {

        const status =
            document.getElementById(
                "globalStatus"
            );

        const text =
            document.getElementById(
                "globalStatusText"
            );

        status.classList.remove(
            "loading",
            "healthy",
            "warning"
        );

        if (data.Offline === 0) {

            status.classList.add("healthy");

            text.textContent =
                "TODOS LOS SISTEMAS OPERATIVOS";

        } else {

            status.classList.add("warning");

            text.textContent =
                data.Offline +
                " SERVICIO" +
                (data.Offline > 1 ? "S" : "") +
                " CON PROBLEMAS";
        }
    }


    function renderServices(services) {

    const container =
        document.getElementById(
            "servicesContainer"
        );

    container.innerHTML = "";
    if (!Array.isArray(services)) {

        console.error(
            "Services no es un array:",
            services
        );

        container.innerHTML =
            '<div class="loading-message">' +
            'No se pudo obtener la lista de servicios.' +
            '</div>';

        return;
    }

    services.forEach(service => {

        const card =
            document.createElement("div");

        card.className =
            "service-card " +
            (service.Online
                ? "service-online"
                : "service-offline");


        const endpoint =
            service.Type === "HTTP"
                ? service.Host
                : service.Host +
                  ":" +
                  service.Port;


        card.innerHTML =

            '<div class="service-main">' +

                '<div class="service-indicator">' +
                    '<span class="service-dot"></span>' +
                '</div>' +

                '<div class="service-info">' +

                    '<div class="service-name">' +
                        escapeHtml(service.Name) +
                    '</div>' +

                    '<div class="service-endpoint">' +

                        '<span class="service-type">' +
                            escapeHtml(service.Type) +
                        '</span>' +

                        '<span>' +
                            escapeHtml(endpoint || "") +
                        '</span>' +

                    '</div>' +

                '</div>' +

            '</div>' +


            '<div class="service-result">' +

                '<div class="service-status">' +

                    (service.Online
                        ? "ONLINE"
                        : "OFFLINE") +

                '</div>' +

                '<div class="service-time">' +

                    (service.Online
                        ? service.ResponseTimeMs + " ms"
                        : escapeHtml(
                            service.Error ||
                            "Sin respuesta"
                          )
                    ) +

                '</div>' +

            '</div>';


        container.appendChild(card);

    });
}


    function updateLastCheck(date) {

        if (!date)
            return;

        const value =
            new Date(date);

        document.getElementById(
            "lastCheck"
        ).textContent =
            value.toLocaleString(
                "es-AR"
            );
    }


    function showConnectionError() {

        const status =
            document.getElementById(
                "globalStatus"
            );

        const text =
            document.getElementById(
                "globalStatusText"
            );

        status.classList.remove(
            "loading",
            "healthy"
        );

        status.classList.add(
            "warning"
        );

        text.textContent =
            "NO SE PUDO CONSULTAR EL MONITOR";
    }


    function escapeHtml(value) {

        if (value == null)
            return "";

        const div =
            document.createElement("div");

        div.textContent = value;

        return div.innerHTML;
    }


    function updateCountdown() {

        const remaining =
            Math.max(
                0,
                nextRefresh - Date.now()
            );

        const totalSeconds =
            Math.floor(
                remaining / 1000
            );

        const minutes =
            Math.floor(
                totalSeconds / 60
            );

        const seconds =
            totalSeconds % 60;


        document.getElementById(
            "nextCheck"
        ).textContent =
            String(minutes)
                .padStart(2, "0")
            + ":" +
            String(seconds)
                .padStart(2, "0");
    }


    // Primera carga
    loadStatus();


    // Cada 5 minutos
    setInterval(
        loadStatus,
        REFRESH_INTERVAL
    );


    // Countdown visual
    setInterval(
        updateCountdown,
        1000
    ); --%>

</script>
