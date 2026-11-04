# Integración ManagerTM: monitor + WebChat

1. Reemplazar dashboard.jsp, js/managertm.js; agregar los dos CSS y js/monitor-managertm.js.
2. Copiar **tu WebChat.js real** en js/WebChat.js. No viene incluido porque no forma parte del paquete anterior y hay que conservar su lógica original.
3. Verificar que `/Monitor` sea la ruta real de tu servlet; cambiar MONITOR_URL si difiere. Abrir consola y revisar `Monitor response` para ajustar claves de JSON (Services/Name/Status...). No se dispone de Server.jsp ni de la respuesta real en esta entrega.
4. Tu WebChat.js tiene una URL fija ws://localhost:47994; corregirla a URL publicada mediante proxy WSS. En `handleServerMessage` usar `this.vozActiva` y `this.hablar(data.text)`; en `toggleVoz` usar `this.vozActiva`.
5. Si la sesión de tu aplicación usa otro atributo distinto de `userId`, ajustar la comprobación inicial de dashboard.jsp. Proteger igualmente el servlet Monitor y los endpoints de chat con autenticación y autorización de servidor.
6. Los estilos del monitor están aislados por #serverMonitorFull. El dashboard muestra resumen y Servidores muestra detalle. Actualización cada cinco minutos.
7. No incluir el CSS antiguo `menu.css` ni el antiguo `monitor.css` porque pisan el layout.
