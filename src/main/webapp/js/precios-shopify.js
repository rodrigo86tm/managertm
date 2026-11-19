(() => {
    'use strict';
    const root = document.getElementById('view-precios');
    if (!root)
        return;
    const el = id => document.getElementById(id), esc = s => String(s??'').replace(/[&<>"']/g, c => ({'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[c]));
    let current = '';
    const ctx = typeof contextPath === 'string' ? contextPath : '';
    async function request(sku, variantId = null) {
        const sync = variantId !== null;
        let forbidden = ['total', 'variants', 'prices'];
        const url = ctx + '/PreciosShopify' + (sync ? '' : '?sku=' + encodeURIComponent(sku));
        const r = await fetch(url, {method: sync ? 'POST' : 'GET', credentials: 'same-origin', headers: {'Content-Type': 'application/json'}, body: sync ? JSON.stringify({sku, variantId}) : undefined});
        
        const raw = await r.text();

        console.log('[SHOPIFY] URL:', url);
        console.log('[SHOPIFY] HTTP:', r.status);
        console.log('[SHOPIFY] RAW:', raw);

        let json;

        try {
            json = JSON.parse(raw);
        } catch (e) {
            throw new Error(
                    'La respuesta del servlet no es JSON válido. HTTP ' + r.status
                    );
        }

        if (!r.ok) {
            throw new Error(json.message || 'HTTP ' + r.status);
        }

        if (!sync && !forbidden.includes(sku)) {
            if (!json ||
                    !Object.prototype.hasOwnProperty.call(json, 'product') ||
                    !Array.isArray(json.variants)) {
                throw new Error(
                        'La respuesta no contiene product y variants.'
                        );
            }
        }

        return json;
    }
    function status(msg) {
        el('spStatus').textContent = msg;
    }
    async function search() {
        current = el('spSku').value.trim();
        if (!current)
            return;
        status('Buscando...');
        el('spVariants').textContent = '';
        el('spSyncSku').hidden = true;
        try {
            const data = await request(current);
            const p = data.product || {}, v = data.variants || [];
            el('spProduct').textContent = Object.keys(p).length ? `${p.Denominacion || ''} · Mínimo: ${p.PrecioMinimo??'—'} · Lista: ${p.PrecioLista??'—'}` : 'Producto no encontrado en ERP';
            el('spVariants').innerHTML = `<p>${v.length} variantes registradas</p><table><thead><tr><th>Variante</th><th>Producto Shopify</th><th>Precio local</th><th>Comparación</th><th>Activo</th><th>Acción</th></tr></thead><tbody>${v.map(x => `<tr><td>${esc(x.VariantId)}</td><td>${esc(x.ProductId)}</td><td>${esc(x.PrecioShopify)}</td><td>${esc(x.PrecioComparacion)}</td><td>${x.Activo ? 'Sí' : 'No'}</td><td><button data-variant="${Number(x.VariantId)}" ${!x.Activo ? 'disabled' : ''}>Sincronizar</button></td></tr>`).join('')}</tbody></table>`;
            status('Consulta finalizada. Los precios mostrados son la copia local, no una lectura en vivo de Shopify.');
        } catch (e) {
            status(e.message)
        }
    }
    async function ActualizaBD() {        
        status('Actualizando...');
        el('spVariants').textContent = '';
        el('spSyncSku').hidden = true;
        try {
            const data = await request('total');            
            status('Actualizacion finalizada');
        } catch (e) {
            status(e.message)
        }
    }
    async function VariantPrices(action) {        
        status('Actualizando...');
        el('spVariants').textContent = '';
        el('spSyncSku').hidden = true;
        try {
            const data = await request(action);            
            status('Actualizacion finalizada');
        } catch (e) {
            status(e.message)
        }
    }
    el('spSearchForm').addEventListener('submit', e => {
        e.preventDefault();
        search()
    });
    el('spDatabaseForm').addEventListener('submit', e => {
        e.preventDefault();
        ActualizaBD()
    });
    el('spVariantsDownloads').addEventListener('submit', e => {
        e.preventDefault();
        VariantPrices('variants')
    });
    el('spPrices').addEventListener('submit', e => {
        e.preventDefault();
        VariantPrices('prices')
    });
    el('spVariants').addEventListener('click', async e => {
        const b = e.target.closest('button[data-variant]');
        if (!b)
            return;
        const id = Number(b.dataset.variant);
        if (!confirm('¿Sincronizar variante ' + id + ' con los precios calculados?'))
            return;
        b.disabled = true;
        status('Enviando a Shopify...');
        try {
            const r = await request(current, id);
            status('Variante ' + r.variantId + ' sincronizada.');
            await search()
        } catch (ex) {
            status(ex.message)
        } finally {
            b.disabled = false
        }
    });
})();
