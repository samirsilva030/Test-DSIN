const API = window.location.origin;
const TELEFONE_SALAO = '(85) 3222-1000';
const STORAGE_CLIENTE = 'cabeleleila_cliente';
const STORAGE_ADMIN_TOKEN = 'cabeleleila_admin_token';

function getAdminToken() {
  return sessionStorage.getItem(STORAGE_ADMIN_TOKEN);
}

function setAdminToken(token) {
  sessionStorage.setItem(STORAGE_ADMIN_TOKEN, token);
}

function sairAdmin() {
  sessionStorage.removeItem(STORAGE_ADMIN_TOKEN);
}

function isAdminAutenticado() {
  return !!getAdminToken();
}

function exigirAdmin() {
  if (!isAdminAutenticado()) {
    window.location.href = '/admin-login.html';
    return false;
  }
  return true;
}

function validarEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function validarTelefone(tel) {
  const n = tel.replace(/\D/g, '');
  return n.length >= 10 && n.length <= 11;
}

function toast(message, type = 'success') {
  const container = document.getElementById('toast-container');
  if (!container) {
    alert(message);
    return;
  }
  const el = document.createElement('div');
  el.className = `toast ${type}`;
  el.textContent = message;
  container.appendChild(el);
  setTimeout(() => el.remove(), 5000);
}

function extrairErro(data) {
  if (!data) return 'Erro desconhecido. Verifique se o backend está rodando (mvn spring-boot:run).';
  if (typeof data === 'string') return data;
  if (data.erro) return data.erro;
  return Object.entries(data)
    .map(([k, v]) => `${k}: ${v}`)
    .join(' | ') || 'Operação não concluída.';
}

async function api(metodo, rota, body, usarAuthAdmin = false) {
  const opts = { method: metodo, headers: { 'Content-Type': 'application/json' } };
  if (usarAuthAdmin) {
    const token = getAdminToken();
    if (token) opts.headers['X-Admin-Auth'] = token;
  }
  if (body !== undefined) opts.body = JSON.stringify(body);

  let res;
  try {
    res = await fetch(`${API}${rota}`, opts);
  } catch {
    throw new Error('Não foi possível conectar ao servidor. Abra http://localhost:8080 e rode mvn spring-boot:run');
  }

  const text = await res.text();
  let data = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = text;
    }
  }
  return { ok: res.ok, status: res.status, data };
}

function formatarData(iso) {
  if (!iso) return '-';
  try {
    return new Date(iso).toLocaleString('pt-BR');
  } catch {
    return iso;
  }
}

function paraIsoLocal(valor) {
  if (!valor) return null;
  return valor.length === 16 ? `${valor}:00` : valor;
}

function podeAlterarPeloSistema(dataHoraIso) {
  const agendada = new Date(dataHoraIso);
  const limite = new Date(agendada);
  limite.setDate(limite.getDate() - 2);
  return new Date() < limite;
}

function getClienteLogado() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_CLIENTE));
  } catch {
    return null;
  }
}

function setClienteLogado(cliente) {
  localStorage.setItem(STORAGE_CLIENTE, JSON.stringify(cliente));
}

function sairCliente() {
  localStorage.removeItem(STORAGE_CLIENTE);
}

function setMinDatetime(ids) {
  const now = new Date();
  now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
  const val = now.toISOString().slice(0, 16);
  ids.forEach((id) => {
    const el = document.getElementById(id);
    if (el) el.min = val;
  });
}

function initTabs(selector = '.tab-btn') {
  document.querySelectorAll(selector).forEach((btn) => {
    btn.addEventListener('click', () => {
      const scope =
        btn.closest('#secaoLogin') ||
        btn.closest('#secaoLogada') ||
        btn.closest('main') ||
        document;
      scope.querySelectorAll(selector).forEach((b) => b.classList.remove('active'));
      scope.querySelectorAll('.panel').forEach((p) => p.classList.remove('active'));
      btn.classList.add('active');
      const panel = document.getElementById(btn.dataset.panel);
      if (panel) panel.classList.add('active');
    });
  });
}
