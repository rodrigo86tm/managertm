(() => {
  'use strict';
  const sidebar = document.getElementById('sidebar');
  const collapse = document.getElementById('collapseSidebar');
  const title = document.getElementById('pageTitle');
  const links = [...document.querySelectorAll('[data-view]')];
  const views = [...document.querySelectorAll('.view')];
  function showView(name) {
    const selected = document.getElementById('view-' + name);
    if (!selected) return;
    views.forEach(v => v.classList.toggle('active', v === selected));
    links.forEach(a => a.classList.toggle('active', a.dataset.view === name));
    const active = links.find(a => a.dataset.view === name);
    title.textContent = active?.dataset.title || 'Panel de dirección';
    document.dispatchEvent(new CustomEvent('managertm:viewchange', {detail:{view:name}}));
  }
  links.forEach(a => a.addEventListener('click', e => { e.preventDefault(); showView(a.dataset.view); }));
  collapse?.addEventListener('click', () => {
    sidebar.classList.toggle('collapsed');
    collapse.setAttribute('aria-expanded', String(!sidebar.classList.contains('collapsed')));
  });
  // WebChat.js es el único controlador del panel de chat.
  // No modificar aquí el atributo hidden: WebChat usa la clase chat-hidden.
  function toggleChat(open) {
    const chat = window.webChat;
    if (!chat) return;
    if (open) chat.open();
    else chat.close();
  }
  showView('dashboard');
  window.ManagerTM = {showView, toggleChat};
})();
