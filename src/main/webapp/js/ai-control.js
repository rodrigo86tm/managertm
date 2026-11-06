(() => {
    'use strict';
    const root = document.getElementById('view-ai-control');
    if (!root)
        return;
    const $ = s => root.querySelector(s), esc = s => String(s??'').replace(/[&<>"']/g, c => ({'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[c]));
    const ctx = typeof contextPath === 'string' ? contextPath : '';
    const schemas = {
        agents: {label: 'Agentes', fields: {Codigo: 'text!', Nombre: 'text!', Descripcion: 'textarea', AreaCodigo: 'text', PromptId: 'number', Activo: 'bool', Configuracion: 'json'}},
        tools: {label: 'Herramientas asignadas', fields: {AgentId: 'agent!', ToolName: 'text!', PuedeEjecutar: 'bool', RequiereAprobacion: 'bool', Configuracion: 'json'}},
        definitions: {label: 'Definiciones de tareas', fields: {AgentId: 'agent!', Codigo: 'text!', Nombre: 'text!', Categoria: 'text!', Descripcion: 'textarea', Instrucciones: 'textarea', Tipo: 'select:MANUAL|SCHEDULED|EVENT|ON_DEMAND', Prioridad: 'select:BAJA|MEDIA|ALTA|CRITICA', ObjetivoMinutos: 'number', VerificacionRequerida: 'bool', PermiteAsignacion: 'bool', Configuracion: 'json', Activo: 'bool'}},
        schedules: {label: 'Programación', fields: {TaskDefinitionId: 'definition!', Nombre: 'text!', ScheduleType: 'select:INTERVAL|CRON|ONCE', CronExpression: 'text', IntervalMinutes: 'number', TimeZoneId: 'text', StartAtUtc: 'datetime', NextRunAtUtc: 'datetime', Activo: 'bool', Configuracion: 'json'}},
        cases: {label: 'Casos', fields: {TaskDefinitionId: 'definition!', CorrelationKey: 'text!', SourceSystem: 'text', EntityType: 'text', EntityId: 'text', Titulo: 'text!', Descripcion: 'textarea', Severidad: 'select:BAJA|MEDIA|ALTA|CRITICA', DueAtUtc: 'datetime', AttributesJson: 'json'}},
        executions: {label: 'Ejecuciones', readonly: true}, events: {label: 'Eventos', readonly: true}, evidence: {label: 'Evidencias', readonly: true}, assignments: {label: 'Asignaciones', readonly: true}
    };
    let tab = 'agents', page = 1, rows = [], agents = [], definitions = [], selected = null;
    const display = {agents: ['Id', 'Codigo', 'Nombre', 'AreaCodigo', 'Activo'], tools: ['Id', 'AgentId', 'ToolName', 'PuedeEjecutar', 'RequiereAprobacion'], definitions: ['Id', 'Codigo', 'Nombre', 'Tipo', 'Prioridad', 'Activo'], schedules: ['Id', 'Nombre', 'ScheduleType', 'NextRunAtUtc', 'Activo'], cases: ['Id', 'CorrelationKey', 'Titulo', 'Estado', 'Severidad'], executions: ['Id', 'TaskDefinitionId', 'ExecutionStatus', 'BusinessStatus', 'AttemptCount', 'LastError'], events: ['Id', 'CaseId', 'EventType', 'ActorType', 'CreatedAtUtc'], evidence: ['Id', 'CaseId', 'EvidenceType', 'SourceSystem', 'CreatedAtUtc'], assignments: ['Id', 'CaseId', 'EmployeeId', 'Estado', 'AssignedAtUtc']};
    async function api(resource, method = 'GET', body = null, id = null, action = null, opts = {}){
        const q = new URLSearchParams({resource});
        if (id != null)
            q.set('id', id);
        if (action)
            q.set('action', action);
        if (method === 'GET') {
            q.set('page', opts.page || 1);
            q.set('size', opts.size || 50);
            if (opts.search)
                q.set('search', opts.search);
        }
        const r = await fetch(ctx + '/AdminAI?' + q, {method, credentials: 'same-origin', headers: {'Accept': 'application/json', ...(body ? {'Content-Type': 'application/json'} : {})}, body: body ? JSON.stringify(body) : undefined});
        const raw = await r.text();
        let data;
        try {
            data = raw ? JSON.parse(raw) : null;
        } catch {
            data = null;
        }
        if (!r.ok)
            throw new Error(data?.message || `Error HTTP ${r.status}`);
        return data;
    }
    function notice(message, error = false) {
        const n = $('#aiNotice');
        n.textContent = message;
        n.classList.toggle('error', error);
        n.hidden = !message;
    }
    async function lookups() {
        try {
            [agents, definitions] = await Promise.all([api('agents', 'GET', null, null, null, {size: 200}), api('definitions', 'GET', null, null, null, {size: 200})]);
        } catch (e) {
            notice(e.message, true);
        }
    }
    function options(type, value) {
        const src = type === 'agent' ? agents : definitions;
        return '<option value="">Seleccionar...</option>' + src.map(x => `<option value="${x.Id}" ${String(value??'') === String(x.Id) ? 'selected' : ''}>${esc(x.Codigo || x.Nombre || x.Id)} · ${esc(x.Nombre)}</option>`).join('');
    }
    function control(name, type, value) {
        const required = type.endsWith('!');
        type = type.replace('!', '');
        const id = 'aiField' + name;
        let html = '';
        if (type === 'bool')
            html = `<input type="checkbox" id="${id}" ${value === true || value === 1 ? 'checked' : ''}>`;
        else if (type === 'textarea' || type === 'json')
            html = `<textarea id="${id}" rows="${type === 'json' ? 4 : 3}" ${required ? 'required' : ''}>${esc(value??'')}</textarea>`;
        else if (type === 'agent' || type === 'definition')
            html = `<select id="${id}" ${required ? 'required' : ''}>${options(type, value)}</select>`;
        else if (type.startsWith('select:'))
            html = `<select id="${id}"><option value="">Seleccionar...</option>${type.substring(7).split('|').map(x => `<option value="${esc(x)}" ${value === x ? 'selected' : ''}>${esc(x)}</option>`).join('')}</select>`;
        else {
            const v = type === 'datetime' && value ? String(value).substring(0, 16) : value??'';
            html = `<input id="${id}" type="${type === 'number' ? 'number' : type === 'datetime' ? 'datetime-local' : 'text'}" value="${esc(v)}" ${required ? 'required' : ''}>`;
        }
        return `<label class="ai-field ${type === 'textarea' || type === 'json' ? 'wide' : ''}"><span>${esc(name)}${required ? ' *' : ''}</span>${html}</label>`;
    }
    function formOpen(record = null) {
        selected = record;
        const schema = schemas[tab];
        if (schema.readonly)
            return;
        $('#aiModalTitle').textContent = (record ? 'Editar ' : 'Nuevo ') + schema.label;
        $('#aiFields').innerHTML = Object.entries(schema.fields).map(([n, t]) => control(n, t, record?.[n])).join('');
        $('#aiModal').hidden = false;
        $('#aiFormError').textContent = '';
    }
    function formClose() {
        $('#aiModal').hidden = true;
        selected = null;
    }
    async function save(e) {
        e.preventDefault();
        const schema = schemas[tab], payload = {};
        for (const [name, typeRaw] of Object.entries(schema.fields)) {
            const el = $('#aiField' + name), type = typeRaw.replace('!', ''); let v = type === 'bool' ? el.checked : el.value.trim();
            if (type === 'number' || type === 'agent' || type === 'definition')
                v = v === '' ? null : Number(v);
            else if (type === 'datetime')
                v = v ? new Date(v).toISOString() : null;
            else if (v === '')
                v = null;
            if (type === 'json' && v !== null) {
                try {
                    JSON.parse(v);
                } catch {
                    $('#aiFormError').textContent = name + ': JSON inválido';
                    return;
                }
            }
            if (!selected || String(v??'') !== String(selected[name]??''))
                payload[name] = v;
        }
        try {
            if (Object.keys(payload).length === 0) {
                formClose();
                return;
            }
            await api(tab, selected ? 'PUT' : 'POST', payload, selected?.Id);
            formClose();
            await load();
            notice('Cambios guardados');
        } catch (err) {
            $('#aiFormError').textContent = err.message;
        }
    }
    function render() {
        const cols = display[tab];
        $('#aiTableHead').innerHTML = '<tr>' + cols.map(x => `<th>${esc(x)}</th>`).join('') + '<th>Acciones</th></tr>';
        $('#aiTableBody').innerHTML = rows.length ? rows.map(r => '<tr>' + cols.map(c => `<td title="${esc(r[c])}">${esc(r[c] === true ? 'Sí' : r[c] === false ? 'No' : r[c]??'—')}</td>`).join('') + `<td><div class="ai-actions"><button data-edit="${r.Id}">Ver / editar</button>${tab === 'cases' ? `<button data-status="${r.Id}">Estado</button><button data-assign="${r.Id}">Asignar</button>` : ''}${tab === 'assignments' ? `<button data-status="${r.Id}">Estado</button>` : ''}${tab === 'executions' ? `<button data-detail="${r.Id}">Detalle</button>` : ''}${tab === 'definitions' ? `<button data-queue="${r.Id}">Ejecutar</button>` : ''}${tab === 'schedules' ? `<button data-toggle="${r.Id}">${r.Activo ? 'Pausar' : 'Activar'}</button>` : ''}</div></td></tr>`).join('') : '<tr><td colspan="12">No hay registros</td></tr>';
        $('#aiPage').textContent = 'Página ' + page;
        $('#aiPrev').disabled = page === 1;
        $('#aiNext').disabled = rows.length < 50;
        $('#aiNew').hidden = !!schemas[tab].readonly;
        $('#aiHeading').textContent = schemas[tab].label;
    }
    async function load() {
        try {
            notice('Cargando...');
            rows = await api(tab, 'GET', null, null, null, {page, size: 50, search: $('#aiSearch').value});
            render();
            notice('');
        } catch (e) {
            notice(e.message, true);
        }
    }
    async function switchTab(t) {
        tab = t;
        page = 1;
        $('#aiSearch').value = '';
        root.querySelectorAll('[data-ai-tab]').forEach(b => b.classList.toggle('active', b.dataset.aiTab === t));
        await lookups();
        await load();
    }
    async function status(id) {
        const valid = tab === 'cases' ? ['OPEN', 'IN_PROGRESS', 'WAITING_EXTERNAL', 'REVIEW_REQUIRED', 'RESOLVED', 'VERIFIED', 'CLOSED', 'CANCELLED'] : ['PENDING', 'ACCEPTED', 'COMPLETED', 'REVOKED'];
        const value = prompt('Nuevo estado: ' + valid.join(', '));
        if (!value)
            return;
        if (!valid.includes(value.toUpperCase()))
            return notice('Estado no válido', true);
        try {
            await api(tab, 'POST', {status: value.toUpperCase()}, id, 'status');
            await load();
        } catch (e) {
            notice(e.message, true);
        }
    }
    async function assign(caseId) {
        const raw = prompt('ID numérico del empleado al que asignar el caso:');
        if (!raw)
            return;
        const employeeId = Number(raw);
        if (!Number.isSafeInteger(employeeId) || employeeId < 1)
            return notice('Empleado inválido', true);
        try {
            await api('assignments', 'POST', {caseId, employeeId}, null, 'create');
            notice('Asignación registrada');
        } catch (e) {
            notice(e.message, true);
        }
    }
    async function queue(id) {
        if (!confirm('¿Encolar una ejecución para esta definición?'))
            return;
        try {
            const x = await api('executions', 'POST', {taskDefinitionId: id, executionKey: 'MANUAL-' + id + '-' + Date.now()}, null, 'queue');
            notice('Ejecución #' + x.id + ' encolada. El worker debe procesarla.');
        } catch (e) {
            notice(e.message, true);
        }
    }
    async function detail(id) {
        const x = await api('executions', 'GET', null, id);
        alert(JSON.stringify(x, null, 2));
    }
    root.addEventListener('click', async e => {
        const b = e.target.closest('button');
        if (!b)
            return;
        if (b.dataset.aiTab)
            return switchTab(b.dataset.aiTab);
        if (b.dataset.edit) {
            const r = rows.find(x => String(x.Id) === b.dataset.edit);
            if (!r)
                return;
            if (schemas[tab].readonly) {
                alert(JSON.stringify(r, null, 2));
                return;
            }
            formOpen(r);
            return;
        }
        if (b.dataset.status)
            return status(Number(b.dataset.status));
        if (b.dataset.assign)
            return assign(Number(b.dataset.assign));
        if (b.dataset.queue)
            return queue(Number(b.dataset.queue));
        if (b.dataset.detail)
            return detail(Number(b.dataset.detail));
        if (b.dataset.toggle) {
            const r = rows.find(x => String(x.Id) === b.dataset.toggle);
            try {
                await api('schedules', 'PUT', {Activo: !r.Activo}, r.Id);
                await load();
            } catch (e) {
                notice(e.message, true);
            }
            return;
        }
    });
    $('#aiNew').addEventListener('click', () => formOpen());
    $('#aiRefresh').addEventListener('click', load);
    $('#aiPrev').addEventListener('click', () => {
        page = Math.max(1, page - 1);
        load();
    });
    $('#aiNext').addEventListener('click', () => {
        page++;
        load();
    });
    $('#aiSearch').addEventListener('change', () => {
        page = 1;
        load();
    });
    $('#aiForm').addEventListener('submit', save);
    $('#aiClose').addEventListener('click', formClose);
    $('#aiCancel').addEventListener('click', formClose);
    $('#aiModal').addEventListener('click', e => {
        if (e.target === $('#aiModal'))
            formClose();
    });
    let loaded = false;
    document.addEventListener('managertm:viewchange', async e => {
        if (e.detail.view === 'ai-control' && !loaded) {
            loaded = true;
            await switchTab('agents');
        }
    });
    
})();
