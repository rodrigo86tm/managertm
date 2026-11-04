(() => {
    'use strict';
    const root = document.getElementById('view-usuarios');
    if (!root)
        return;
    const $ = (selector) => root.querySelector(selector);
    let data = null, loaded = false, busy = false, mode = 'create', selected = null;
    const esc = s => String(s??'').replace(/[&<>"']/g, c => ({'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[c]));
    const state = {search: '', area: '', status: 'all'};
    const ctx = typeof contextPath === 'string' ? contextPath : '';
    async function api(action, method = 'GET', body = null, id = null) {
        const q = new URLSearchParams({action});
        if (id !== null)
            q.set('id', id);
        const headers = {'Accept': 'application/json'};
        if (body !== null)
            headers['Content-Type'] = 'application/json';
        const csrf = document.querySelector('meta[name="admin-csrf"]')?.content;
        if (method !== 'GET' && csrf)
            headers['X-Admin-CSRF'] = csrf;
        const res = await fetch(ctx + '/AdminUsuarios?' + q, {method, credentials: 'same-origin', headers, body: body === null ? undefined : JSON.stringify(body)});
        const raw = await res.text();
        let json = {};
        try {
            json = raw ? JSON.parse(raw) : {}
        } catch {
        }
        if (!res.ok)
            throw new Error(json.message || (res.status === 401 ? 'La sesión venció' : res.status === 403 ? 'No tenés permisos para administrar usuarios' : 'Error HTTP ' + res.status));
        return json;
    }
    function notice(text, type = '') {
        const n = $('#uaNotice');
        n.textContent = text;
        n.className = 'ua-alert ' + type;
        n.hidden = !text;
    }
    // La API puede serializar los DTO con PascalCase o camelCase.
    function normalizeInitial(response) {
        const r = response.data ?? response.Data ?? response;
        const get = (obj, camel, pascal, fallback = null) =>
            obj?.[camel] ?? obj?.[pascal] ?? fallback;
        return {
            personal: (get(r, 'personal', 'Personal', [])).map(p => ({
                idPersonal: get(p, 'idPersonal', 'IdPersonal'),
                legajo: get(p, 'legajo', 'Legajo'),
                nombre: get(p, 'nombre', 'Nombre', ''),
                apellido: get(p, 'apellido', 'Apellido', ''),
                idArea: get(p, 'idArea', 'IdArea'),
                area: get(p, 'area', 'Area', ''),
                puesto: get(p, 'puesto', 'Puesto', ''),
                sucursal: get(p, 'sucursal', 'Sucursal', ''),
                idUsuario: get(p, 'idUsuario', 'IdUsuario'),
                username: get(p, 'username', 'Username', ''),
                usuarioActivo: get(p, 'usuarioActivo', 'UsuarioActivo', false),
                roles: (get(p, 'roles', 'Roles', [])).map(x =>
                    Number(typeof x === 'object' ? get(x, 'idRol', 'IdRol') : x))
            })),
            roles: (get(r, 'roles', 'Roles', [])).map(x => ({
                idRol: Number(get(x, 'idRol', 'IdRol')),
                nombre: get(x, 'nombre', 'Nombre', '')
            })),
            areas: (get(r, 'areas', 'Areas', [])).map(x => ({
                id: get(x, 'id', 'Id'),
                nombre: get(x, 'nombre', 'Nombre', '')
            })),
            puestos: get(r, 'puestos', 'Puestos', []),
            sucursales: get(r, 'sucursales', 'Sucursales', [])
        };
    }
    async function load() {
        if (busy)
            return;
        busy = true;
        notice('Cargando personal y usuarios...');
        try {
            const response = await api('initial');
            if (response.success === false)
                throw new Error(response.message || 'No se pudo cargar usuarios');
            data = normalizeInitial(response);
            loaded = true;
            notice('');
            render();
        } catch (e) {
            notice(e.message, 'error');
        } finally {
            busy = false;
        }
    }
    function render() {
        if (!data)
            return;
        const rows = data.personal || [];
        $('#uaTotal').textContent = rows.length;
        $('#uaActiveCount').textContent = rows.filter(x => x.idUsuario && x.usuarioActivo).length;
        $('#uaPending').textContent = rows.filter(x => !x.idUsuario).length;
        const area = $('#uaArea');
        const selected = area.value;
        area.innerHTML = '<option value="">Todas las áreas</option>' + (data.areas || []).map(x => `<option value="${x.id}">${esc(x.nombre)}</option>`).join('');
        area.value = selected;
        const query = state.search.toLocaleLowerCase('es');
        const filtered = rows.filter(x => {
            const text = [x.legajo, x.nombre, x.apellido, x.username, x.area, x.puesto, x.sucursal].join(' ').toLocaleLowerCase('es');
            return (!query || text.includes(query)) && (!state.area || String(x.idArea) === state.area) &&
                    (state.status === 'all' || (state.status === 'pending' ? !x.idUsuario : state.status === 'active' ? !!x.usuarioActivo : !!x.idUsuario && !x.usuarioActivo));
        });
        $('#uaCount').textContent = filtered.length + ' registros';
        $('#uaRows').innerHTML = filtered.length ? filtered.map(x => `<tr>
      <td><span class="ua-person">${esc(x.apellido)}, ${esc(x.nombre)}</span><span class="ua-secondary">Legajo ${esc(x.legajo??'—')} · #${x.idPersonal}</span></td>
      <td>${esc(x.area || '—')}<span class="ua-secondary">${esc(x.puesto || '')}</span></td><td>${esc(x.sucursal || '—')}</td>
      <td>${x.idUsuario ? `<strong>${esc(x.username)}</strong><span class="ua-secondary">${(x.roles || []).map(id => esc(data.roles.find(r => r.idRol === id)?.nombre || id)).join(', ') || 'Sin roles'}</span>` : '—'}</td>
      <td><span class="ua-pill ${!x.idUsuario ? 'none' : x.usuarioActivo ? 'ok' : 'off'}">${!x.idUsuario ? 'Sin usuario' : x.usuarioActivo ? 'Activo' : 'Inactivo'}</span></td>
      <td><div class="ua-actions">${x.idUsuario ? `<button data-action="edit" data-id="${x.idPersonal}">Editar</button><button data-action="reset" data-id="${x.idPersonal}">Contraseña</button>` : `<button data-action="create" data-id="${x.idPersonal}">Crear usuario</button>`}</div></td></tr>`).join('') : '<tr><td colspan="6" class="ua-empty">No hay empleados para esos filtros</td></tr>';
    }
    function rolesMarkup(selectedRoles = []) {
        return (data.roles || []).map(r => `<label class="ua-role"><input type="checkbox" name="uaRole" value="${r.idRol}" ${selectedRoles.includes(r.idRol) ? 'checked' : ''}>${esc(r.nombre)}</label>`).join('');
    }
    function openModal(action, person) {
        mode = action;
        selected = person;
        $('#uaModal').hidden = false;
        $('#uaModalTitle').textContent = action === 'create' ? 'Crear usuario' : action === 'edit' ? 'Editar usuario' : 'Restablecer contraseña';
        $('#uaEmployee').textContent = person.apellido + ', ' + person.nombre + ' · Legajo ' + (person.legajo??'—');
        $('#uaUsername').value = person.username || '';
        $('#uaUsername').disabled = action !== 'create';
        $('#uaPassword').value = '';
        $('#uaPasswordBox').hidden = action === 'edit';
        $('#uaUsernameBox').hidden = action === 'reset';
        $('#uaRolesBox').hidden = action === 'reset';
        $('#uaActiveBox').hidden = action !== 'edit';
        $('#uaEnabled').checked = !!person.usuarioActivo;
        $('#uaRoles').innerHTML = rolesMarkup(person.roles || []);
        $('#uaSubmit').textContent = action === 'create' ? 'Crear usuario' : action === 'edit' ? 'Guardar cambios' : 'Restablecer contraseña';
        $('#uaModalError').hidden = true;
    }
    function closeModal() {
        $('#uaModal').hidden = true;
        $('#uaPassword').value = '';
    }
    async function save(e) {
        e.preventDefault();
        if (!selected)
            return;
        const roles = [...root.querySelectorAll('input[name="uaRole"]:checked')].map(x => Number(x.value));
        const password = $('#uaPassword').value;
        const btn = $('#uaSubmit');
        btn.disabled = true;
        $('#uaModalError').hidden = true;
        try {
            if (mode === 'create')
                await api('create', 'POST', {idPersonal: selected.idPersonal, username: $('#uaUsername').value.trim(), passwordTemporal: password, roles});
            else if (mode === 'edit')
                await api('update', 'PUT', {activo: $('#uaEnabled').checked, roles}, selected.idUsuario);
            else
                await api('reset', 'POST', {passwordTemporal: password}, selected.idUsuario);
            closeModal();
            await load();
            notice('Operación realizada correctamente.', 'success');
        } catch (err) {
            $('#uaModalError').textContent = err.message;
            $('#uaModalError').hidden = false;
        } finally {
            btn.disabled = false;
        }
    }
    root.addEventListener('click', e => {
        const button = e.target.closest('button[data-action]');
        if (!button || !data)
            return;
        const person = data.personal.find(x => x.idPersonal === Number(button.dataset.id));
        if (person)
            openModal(button.dataset.action, person);
    });
    $('#uaSearch').addEventListener('input', e => {
        state.search = e.target.value;
        render();
    });
    $('#uaArea').addEventListener('change', e => {
        state.area = e.target.value;
        render();
    });
    $('#uaStatus').addEventListener('change', e => {
        state.status = e.target.value;
        render();
    });
    $('#uaRefresh').addEventListener('click', load);
    $('#uaNew').addEventListener('click', () => {
        if (!data)
            return;
        notice('Seleccioná un empleado sin usuario en el listado para crear su cuenta.');
    });
    $('#uaClose').addEventListener('click', closeModal);
    $('#uaCancel').addEventListener('click', closeModal);
    $('#uaForm').addEventListener('submit', save);
    document.addEventListener('keydown', e => {
        if (e.key === 'Escape' && !$('#uaModal').hidden)
            closeModal();
    });
    document.addEventListener('managertm:viewchange', e => {
        if (e.detail.view === 'usuarios' && !loaded)
            load();
    });
})();
