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
  const chatButton = document.getElementById('chatButton');
  const chatPanel = document.getElementById('chatWindow');
  const chatClose = document.getElementById('chatClose');
  function toggleChat(open) {
    chatPanel.hidden = !open;
    chatButton.setAttribute('aria-expanded', String(open));
    if (open) document.dispatchEvent(new Event('managertm:chatopen'));
  }
  chatButton?.addEventListener('click', () => toggleChat(chatPanel.hidden));
  chatClose?.addEventListener('click', () => toggleChat(false));
  showView('dashboard');
  window.ManagerTM = {showView, toggleChat};
})();
