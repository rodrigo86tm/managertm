(() => {
  'use strict';
  // IMPORTANTE: reemplazar por la ruta REAL del servlet Monitor ya existente.
  const MONITOR_URL = contextPath + '/Monitor';
  const $ = id => document.getElementById(id);
  const setText = (id, value) => { if ($(id)) $(id).textContent = value; };
  const pick = (obj, ...keys) => keys.map(k => obj?.[k]).find(v => v !== undefined && v !== null);
  let fetching = false;
  function render(data) {
    // Ajustar estas claves al JSON REAL que devuelve /Monitor.
    const services = pick(data, 'Services', 'services', 'Servicios', 'servicios') || [];
    const total = Number(pick(data, 'Total', 'total') ?? services.length);
    const online = Number(pick(data, 'Online', 'online') ?? services.filter(s => pick(s,'Online','online','IsOnline','isOnline') === true).length);
    const offline = Number(pick(data, 'Offline', 'offline') ?? (total - online));
    setText('monitorTotal', total); setText('monitorOnline', online); setText('monitorOffline', offline);
    setText('metricServices', total); setText('metricAlerts', offline);
    setText('serverMonitor', `${online} en línea · ${offline} fuera de línea`);
    const global = $('monitorGlobal');
    global.className = 'global-status ' + (offline > 0 ? 'warning' : 'healthy');
    setText('monitorGlobalText', offline > 0 ? 'REQUIERE ATENCIÓN' : 'TODOS OPERATIVOS');
    const container = $('monitorServices');
    container.replaceChildren();
    if (!Array.isArray(services) || !services.length) {
      const empty = document.createElement('div'); empty.className='loading-message';
      empty.textContent='La respuesta no contiene una lista de servicios compatible. Revisar nombres de campos.';
      container.appendChild(empty); return;
    }
    services.forEach(s => {
      const isOnline = pick(s,'Online','online','IsOnline','isOnline','Healthy','healthy') === true ||
        String(pick(s,'Status','status','Estado','estado') || '').toLowerCase() === 'online';
      const name = String(pick(s,'Name','name','Nombre','nombre') ?? 'Servicio');
      const endpoint = String(pick(s,'Endpoint','endpoint','Url','url','Host','host') ?? '');
      const result = String(pick(s,'ResponseTime','responseTime','LatencyMs','latencyMs','Message','message') ?? '');
      const card = document.createElement('article'); card.className='service-card ' + (isOnline?'service-online':'service-offline');
      const main = document.createElement('div'); main.className='service-main';
      const dot = document.createElement('span'); dot.className='service-dot';
      const info = document.createElement('div');
      const title = document.createElement('div'); title.className='service-name'; title.textContent=name;
      const subtitle = document.createElement('div'); subtitle.className='service-endpoint'; subtitle.textContent=endpoint;
      info.append(title,subtitle); main.append(dot,info);
      const right = document.createElement('div'); right.className='service-result';
      const state = document.createElement('div'); state.className='service-status'; state.textContent=isOnline?'ONLINE':'OFFLINE';
      const time = document.createElement('div'); time.className='service-time'; time.textContent=result;
      right.append(state,time); card.append(main,right); container.append(card);
    });
  }
  async function refresh() {
    if(fetching) return;
    fetching=true; $('refreshMonitor').disabled=true;
    try {
      const response = await fetch(MONITOR_URL, {credentials:'include', cache:'no-store'});
      if (!response.ok) throw new Error('HTTP ' + response.status);
      const data = await response.json();
      console.log('Monitor response:', data);
      render(data);
      setText('monitorUpdated', 'Última actualización: ' + new Date().toLocaleTimeString('es-AR'));
    } catch(err) {
      console.error('Error consultando monitor:', err);
      setText('serverMonitor','No se pudo consultar el monitor');
      setText('monitorGlobalText','SIN DATOS');
      $('monitorGlobal').className='global-status warning';
      setText('monitorUpdated','Error: ' + err.message);
    } finally {fetching=false; $('refreshMonitor').disabled=false;}
  }
  $('refreshMonitor')?.addEventListener('click',refresh);
  refresh();
  setInterval(refresh, 5*60*1000);
  window.ManagerTMMonitor = {refresh};
})();
