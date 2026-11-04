function SendPayload(payload) {                                               
    var jsonPayLoad = JSON.stringify(payload);
    fetch(contextPath + '/ProcesoDatosSend', {
        method: 'POST',
        credentials:'include',
        headers: {
            'Content-Type': 'application/json'
        },
        body: jsonPayLoad
    })
        .then(async response => {
                        const data = await response.json();
                        if(!response.ok){
                            if(data.error.toLowerCase() === "tokenvencido" || data.error.toLowerCase() === "token invalido"){
                                window.location.href = 'index.jsp';
                            }else{
                                throw new Error(data.error || 'Error de autenticacion');
                            };
                        }
                        return data;
                    })      
        .catch(error => {
            console.error('Error al cargar los datos:', error.message);            
            return {success:false,error:error.message};
        });
} 
function returnSendPayload(payload) {   
    if (!payload || !payload.Controller || !payload.Accion) {
        console.warn("Payload inválido", payload);
        return Promise.reject(new Error("Payload inválido"));
    }                                            
    var jsonPayLoad = JSON.stringify(payload);
    return fetch(contextPath + '/ProcesoDatosSend', {
        method: 'POST',
        credentials:'include',
        headers: {
            'Content-Type': 'application/json'
        },
        body: jsonPayLoad
    })
        .then(async response => {
            const data = await response.json();
            if(!response.ok){
                if(data.error.toLowerCase() === "tokenvencido" || data.error.toLowerCase() === "token invalido"){
                    window.location.href = 'index.jsp';
                }else{
                    throw new Error(data.error || 'Error de autenticacion');
                };
            }
            return data;
        })       
        .catch(error => {
            console.error('Error al cargar los datos:', error.message);            
            throw error;
        });
} 
function returnSendPayloadExt(payload) {                                                  
    var jsonPayLoad = JSON.stringify(payload);
    return fetch(contextPath + '/ProcesoDatosSheets', {
        method: 'POST',
        credentials:'include',
        headers: {
            'Content-Type': 'application/json'
        },
        body: jsonPayLoad
    })
        .then(async response => {
            const data = await response.json();
            if(!response.ok){
                if(data.error.toLowerCase() === "tokenvencido" || data.error.toLowerCase() === "token invalido"){
                    window.location.href = contextPath + '/index.jsp';
                    return;
                }else{
                    throw new Error(data.error || 'Error de autenticacion');
                };
            }
            return data;
        })       
        .catch(error => {
            console.error('Error al cargar los datos:', error.message);            
            return {success:false,error:error.message};
        });
} 
function getCookie(name){
    const cookies = document.cookie.split(';');
    for(const cookie of cookies){
        const [cname,cvalue] = cookie.trim().split('=');
        if(cname === name) return decodeURIComponent(cvalue);
    }
    return null;
}
function ImprimirReporteCompleto(jsonDataInfo, logistica,fechad,fechah){
    const usuario = getCookie('user') || 'User Unkwown';
    const fecha = new Date().toLocaleString('es-AR');
    const encabezado = `
        <div style="text-align: center; padding: 10px; background-color: #003366; color: white;">
            <h2 style="margin: 5px;">REPORTE DE OBLEAS</h2>
            <p style="margin: 5px;">Fecha: ${fecha}</p>
            <p style="margin: 5px;">Logística: ${logistica}</p>
            <p style="margin: 5px;">Rango de fechas desde: ${fechad} hasta ${fechah}</p>
        </div>
    `;
    
    const filas = jsonDataInfo.map(item =>`
        <tr>
            <td>${item.Oblea}</td>
            <td>${item.ER}</td>
            <td>${item.Producto}</td>
            <td>paq: ${item.Paquete || ''} de ${item.Bultos || ''}</td>
            <td>${item.Cliente || ''}</td>
            <td>${item.Localidad || ''}</td>
        </tr>
    `).join('');
    
    const tablaHTML = `
        <table border="1" style="width: 100%; border-collapse: collapse; margin-top: 10px;">
            <thead style="background-color: #006699; color: white;">
                <tr>
                    <th>Oblea</th>
                    <th>ER</th>
                    <th>Producto</th>
                    <th>Paq/Bulto</th>
                    <th>Cliente</th>
                    <th>Localidad</th>
                </tr>
            </thead>
            <tbody>
                ${filas}
            </tbody>
        </table>
    `;
    const pieHTML = `
        <div style="text-align: right; padding: 10px; background-color: #003366; color: white; margin-top: 20px;">
            Impreso por: ${usuario}
        </div>
    `;
    const htmlFinal = `
        <html>
            <head>
                <title>Reporte</title>
                <style>
                    @media print {
                        body { font-family: Arial, sans-serif; margin: 20px; }
                        table, th, td { border: 1px solid black; }
                        th, td { padding: 6px; text-align: center; }
                        thead { display: table-header-group; }
                        tfoot { display: table-footer-group; }
                    }
                </style>
            </head>
            <body>
                ${encabezado}
                ${tablaHTML}
                ${pieHTML}
            </body>
        </html>        
    `;
    const ventana = window.open('','','width=900,height=700');
    if (!ventana) {
        alert('La ventana emergente fue bloqueada por el navegador. Habilita los pop-ups e intenta de nuevo.');
        return;
    }
    ventana.document.write(htmlFinal);
    ventana.document.close();
    ventana.focus();
    //ventana.print();
    //ventana.close();    
}
function ImprimirOrdenRetiro(jsonDataInfo, logistica,fechad){
    const usuario = getCookie('user') || 'User Unkwown';
    var dataF = [];
    dataF.push(jsonDataInfo);
    var data = dataF[0];
    const fecha = new Date().toLocaleString('es-AR');
    const encabezado = `        
        <div class="header">
            <div class="left-header">
                <h2>Tio Musa<br><small>30 años</small></h2>
                <p>Aranguren 1853 C.A.B.A.<br>
                Nro. Ingresos Brutos: CM 30-66168573-1<br>
                IVA RESPONSABLE INSCRIPTO</p>
            </div>
            <div class="right-header">
                <p><strong>${fecha}</strong></p>
                <h3>ORDEN DE RETIRO</h3>
                <p>C.U.I.T.: 30661685731</p>
                <p>Empresa de Envio: <strong>${logistica}</strong></p>
            </div>
        </div>
    `;
    const midle = `
        <div class="section">
            <div class="grid">
                <div><strong>Fecha de Envio:</strong>${fechad}</div>
                <div><strong>Nro de Envio:</strong>${data.ER}</div>
                <div><strong>Cliente:</strong>${data.Cliente}</div>
                <div><strong>DNI:</strong><input></input></div>
            </div>
            <div class="grid">
                <div><strong>CP:</strong>${data.Cp}</div>
                <div><strong>Ciudad:</strong>${data.Localidad}</div>
                <div><strong>Telefono:</strong>${data.Telefono}</div>
            </div>
            <div class="row"><strong>Direccion:</strong>${data.Direccion}</div>
            <div class="row">
                <strong>Observaciones:</strong>
                <textarea class="obs-input" rows="3">${data.DetalleDirec}</textarea>
            </div>
        </div>
    `;
    const filas = dataF.map(item =>{
        return`
        <tr>
            <td>${item.Producto}</td>
            <td><input class="inputs"></input></td>
            <td>${item.Familia}</td>
            <td>${item.Obs}</td>
        </tr>
    `;
    }).join('');
    const filasb = `
        <tr>
            <td></td>
            <td><input class="inputs"></input></td>
            <td></td>
            <td></td>
        </tr>
        <tr>
            <td></td>
            <td><input class="inputs"></input></td>
            <td></td>
            <td></td>
        </tr>
        <tr>
            <td></td>
            <td><input class="inputs"></input></td>
            <td></td>
            <td></td>
        </tr>
        <tr>
            <td></td>
            <td><input class="inputs"></input></td>
            <td></td>
            <td></td>
        </tr>
        <tr>
            <td></td>
            <td><input class="inputs"></input></td>
            <td></td>
            <td></td>
        </tr>        
    `;
    
    const tablaHTML = `
        <table border="1" style="width: 100%; border-collapse: collapse; margin-top: 10px;">
            <thead style="background-color: #006699; color: white;">
                <tr>
                    <th>Producto</th>
                    <th>Cant</th>
                    <th>Familia</th>
                    <th>Obs</th>
                </tr>
            </thead>
            <tbody>
                ${filas}
                ${filasb}
            </tbody>
        </table>
    `;
    const pieHTML = `
        <div class="footer">
            PERSONA QUE ENTREGA EL PRODUCTO<br>
        </div>

        <div class="signature">
            <br>
            <div>FIRMA</div>
            <div>ACLARACION</div>
            <div>DNI</div>
        </div>

        <div class="note">
            Este documento no es valido como Factura, sino un formulario de retiro de mercadería para su información y verificación.<br>
            Por favor no realice ningún pago contra este documento.
        </div>
    `;
    const htmlFinal = `
        <html>
            <head>
                <title>Orden de Retiro</title>
                <link href="css/ordenRetiro.css?v=<%=version%>" rel="stylesheet">
                <script src="js/html2pdf.bundle.min.js"></script>
            </head>
            <body>
                <div class="page">
                    ${encabezado}
                    ${midle}
                    ${tablaHTML}
                    <div class="footer-section">
                        ${pieHTML}
                    </div>
                </div>
            </body>
            <button id="btnDownloadPdf" style="margin-top:20px; padding:10px 20px;">Descargar</button>
            <script>
                document.getElementById('btnDownloadPdf').addEventListener('click',function(){
                    const element = document.querySelector('.page');
                    const opt ={
                        margin: [0,0,0,0],
                        pagebrek: {mode: ['css']},
                        filename: 'OrdenRetiro_${data.ER}.pdf',
                        image:{type:'jpeg',quality:0.98},
                        html2canvas:{scale:2},
                        jsPDF: {unit:'in',format:'a4',orientation:'portrait'}
                    };
                    html2pdf().set(opt).from(element).save();
                });
            </script>
        </html>        
    `;
    const ventana = window.open('','','width=900,height=700');
    if (!ventana) {
        alert('La ventana emergente fue bloqueada por el navegador. Habilita los pop-ups e intenta de nuevo.');
        return;
    }
    ventana.document.write(htmlFinal);
    ventana.document.close();
    ventana.focus();
    //ventana.print();
    //ventana.close();
    
}
function ConvertCsv(data){
    try {
        const csv = atob(data.csvBase64);
        const blob = new Blob([csv],{type:"text/csv;charset=utf-8"});
        const link = document.createElement("a");
        link.href = URL.createObjectURL(blob);
        link.download = data.name;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    } catch (err) {
        console.error("Error al convertir CSV:", err);
        alert("Ocurrió un error al generar el CSV");
    }
}
