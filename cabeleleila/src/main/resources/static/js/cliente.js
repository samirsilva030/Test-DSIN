let servicos = [];
let meusAgendamentos = [];

function servicosSelecionados(containerId) {
  const container = document.getElementById(containerId);
  if (!container) return [];
  return Array.from(container.querySelectorAll('input[name="servicosCheck"]:checked')).map((cb) => ({
    id: cb.value,
  }));
}

function atualizarTotalPreview() {
  const elValor = document.getElementById('valorTotalPreview');
  const elDuracao = document.getElementById('duracaoTotalPreview');
  const ids = servicosSelecionados('containerServicos').map((s) => s.id);
  const selecionados = servicos.filter((s) => ids.includes(String(s.id)));
  const total = selecionados.reduce((acc, s) => acc + Number(s.preco), 0);
  const duracao = selecionados.reduce((acc, s) => acc + Number(s.duracao), 0);
  if (elValor) elValor.textContent = `Total estimado: R$ ${total.toFixed(2)}`;
  if (elDuracao) elDuracao.textContent = `Duração estimada: ${duracao} min`;
}

function renderizarServicos(containerId, selecionados = []) {
  const container = document.getElementById(containerId);
  if (!container) return;

  if (!servicos.length) {
    container.innerHTML =
      '<p class="empty-state">Nenhum serviço disponível. Peça à Leila cadastrar na área administrativa.</p>';
    return;
  }

  const ids = new Set(selecionados.map((s) => String(s.id || s)));
  container.innerHTML = servicos
    .map(
      (s) => `
    <label class="servico-item">
      <input type="checkbox" name="servicosCheck" value="${s.id}" ${ids.has(String(s.id)) ? 'checked' : ''}>
      <div class="info">
        <strong>${s.nome}</strong><br>
        <small>${s.duracao} min</small>
      </div>
      <span class="preco">R$ ${Number(s.preco).toFixed(2)}</span>
    </label>`
    )
    .join('');

  if (containerId === 'containerServicos') {
    container.querySelectorAll('input').forEach((cb) => {
      cb.addEventListener('change', atualizarTotalPreview);
    });
    atualizarTotalPreview();
  }
}

function mostrarLogado(cliente) {
  document.getElementById('secaoLogin').classList.add('hidden');
  document.getElementById('secaoLogada').classList.remove('hidden');
  document.getElementById('btnSair').classList.remove('hidden');
  document.getElementById('clienteLogadoInfo').innerHTML = `
    Olá, <strong>${cliente.nome}</strong> — ${cliente.email} | Tel: ${cliente.telefone}
  `;
}

function mostrarLogin() {
  document.getElementById('secaoLogin').classList.remove('hidden');
  document.getElementById('secaoLogada').classList.add('hidden');
  document.getElementById('btnSair').classList.add('hidden');
}

async function carregarServicos() {
  const { ok, data } = await api('GET', '/servicos');
  if (!ok) throw new Error(extrairErro(data));
  servicos = data;
  renderizarServicos('containerServicos');
  renderizarServicos('containerServicosAlterar');
}

function formatarValor(v) {
  if (v == null) return '-';
  return `R$ ${Number(v).toFixed(2)}`;
}

function renderizarLinhaMeus(a) {
  const nomes = a.servicos?.map((s) => s.nome).join(', ') || '-';
  const alteravel = podeAlterarPeloSistema(a.dataHora) && a.status !== 'CANCELADO';
  const acoes = [];

  if (a.status === 'CANCELADO') {
    acoes.push('<span class="badge badge-blocked">Cancelado</span>');
  } else if (alteravel) {
    acoes.push(`<button type="button" class="btn btn-warning btn-sm" data-editar="${a.id}">Alterar</button>`);
    acoes.push(`<button type="button" class="btn btn-danger btn-sm" data-cancelar="${a.id}">Cancelar</button>`);
  } else {
    acoes.push(`<span class="badge badge-blocked">Ligue ${TELEFONE_SALAO}</span>`);
  }

  return `
    <tr>
      <td>${nomes}</td>
      <td>${formatarData(a.dataHora)}</td>
      <td>${formatarValor(a.valorTotal)}</td>
      <td>${a.status || 'AGENDADO'}</td>
      <td><div class="actions">${acoes.join('')}</div></td>
    </tr>`;
}

async function carregarMeusAgendamentos() {
  const cliente = getClienteLogado();
  const { ok, data } = await api('GET', `/agendamentos/cliente/${cliente.id}`);
  if (!ok) throw new Error(extrairErro(data));

  meusAgendamentos = data;
  const tbodyMeus = document.getElementById('tabelaMeus');
  const tbodyAlt = document.getElementById('tabelaAlterar');

  if (!meusAgendamentos.length) {
    const vazio = '<tr><td colspan="5" class="empty-state">Você ainda não tem agendamentos.</td></tr>';
    tbodyMeus.innerHTML = vazio;
    tbodyAlt.innerHTML = '<tr><td colspan="3" class="empty-state">Nenhum agendamento.</td></tr>';
    return;
  }

  tbodyMeus.innerHTML = meusAgendamentos.map(renderizarLinhaMeus).join('');
  tbodyAlt.innerHTML = meusAgendamentos
    .filter((a) => a.status !== 'CANCELADO')
    .map((a) => {
      const nomes = a.servicos?.map((s) => s.nome).join(', ') || '-';
      const alteravel = podeAlterarPeloSistema(a.dataHora);
      const btn = alteravel
        ? `<button type="button" class="btn btn-warning btn-sm" data-editar="${a.id}">Editar</button>`
        : `<span class="badge badge-blocked">Só telefone</span>`;
      return `<tr><td>${nomes}</td><td>${formatarData(a.dataHora)}</td><td>${btn}</td></tr>`;
    })
    .join('');

  document.querySelectorAll('[data-editar]').forEach((btn) => {
    btn.addEventListener('click', () => abrirEdicao(btn.dataset.editar));
  });
  document.querySelectorAll('[data-cancelar]').forEach((btn) => {
    btn.addEventListener('click', () => cancelarAgendamento(btn.dataset.cancelar));
  });
}

async function cancelarAgendamento(id) {
  if (!confirm('Deseja cancelar este agendamento?')) return;
  const { ok, data } = await api('PATCH', `/agendamentos/${id}/cancelar`);
  if (ok) {
    toast('Agendamento cancelado.');
    await carregarMeusAgendamentos();
  } else {
    toast(extrairErro(data), 'error');
  }
}

function abrirEdicao(id) {
  const ag = meusAgendamentos.find((a) => String(a.id) === String(id));
  if (!ag) return;

  if (!podeAlterarPeloSistema(ag.dataHora)) {
    toast(`Alteração bloqueada. Ligue: ${TELEFONE_SALAO}`, 'error');
    return;
  }

  document.getElementById('formAlteracao').classList.remove('hidden');
  document.getElementById('editId').value = ag.id;
  document.getElementById('novaDataHora').value = ag.dataHora?.substring(0, 16) || '';
  renderizarServicos('containerServicosAlterar', ag.servicos || []);
  const tabAlterar = document.querySelector('#secaoLogada [data-panel="panel-alterar"]');
  if (tabAlterar) tabAlterar.click();
}

async function entrar(e) {
  e.preventDefault();
  const email = document.getElementById('loginEmail').value.trim().toLowerCase();

  if (!validarEmail(email)) {
    toast('Informe um e-mail válido.', 'error');
    return;
  }

  const { ok, data } = await api('GET', `/clientes/email/${encodeURIComponent(email)}`);
  if (!ok || !data || !data.id) {
    toast('E-mail não encontrado. Use "Primeiro acesso" para se cadastrar.', 'error');
    return;
  }

  setClienteLogado(data);
  toast(`Bem-vindo(a), ${data.nome}!`);
  await iniciarAreaLogada();
}

async function cadastrar(e) {
  e.preventDefault();
  const payload = {
    nome: document.getElementById('cadNome').value.trim(),
    telefone: document.getElementById('cadTelefone').value.trim(),
    email: document.getElementById('cadEmail').value.trim().toLowerCase(),
  };

  if (!payload.nome) {
    toast('Informe o nome.', 'error');
    return;
  }
  if (!validarTelefone(payload.telefone)) {
    toast('Telefone inválido (10 ou 11 dígitos).', 'error');
    return;
  }
  if (!validarEmail(payload.email)) {
    toast('E-mail inválido.', 'error');
    return;
  }

  const { ok, data } = await api('POST', '/clientes', payload);
  if (!ok) {
    toast(extrairErro(data), 'error');
    return;
  }

  setClienteLogado(data);
  toast('Cadastro realizado! Agora você pode agendar.');
  e.target.reset();
  await iniciarAreaLogada();
}

async function agendar(e) {
  e.preventDefault();
  const cliente = getClienteLogado();
  const dataHora = paraIsoLocal(document.getElementById('dataHora').value);
  const servicosSel = servicosSelecionados('containerServicos');

  if (!servicosSel.length) {
    toast('Selecione pelo menos um serviço.', 'error');
    return;
  }

  const { ok, data } = await api('POST', '/agendamentos', {
    dataHora,
    cliente: { id: cliente.id },
    servicos: servicosSel,
  });

  if (ok) {
    toast(`Agendamento confirmado! Total: ${formatarValor(data.valorTotal)}`);
    e.target.reset();
    renderizarServicos('containerServicos');
    await carregarMeusAgendamentos();
  } else {
    toast(extrairErro(data), 'error');
  }
}

async function alterar(e) {
  e.preventDefault();
  const id = document.getElementById('editId').value;
  const dataHora = paraIsoLocal(document.getElementById('novaDataHora').value);
  const servicosSel = servicosSelecionados('containerServicosAlterar');

  const payload = { dataHora };
  if (servicosSel.length) payload.servicos = servicosSel;

  const { ok, data } = await api('PUT', `/agendamentos/${id}`, payload);
  if (ok) {
    toast(`Atualizado! Novo total: ${formatarValor(data.valorTotal)}`);
    document.getElementById('formAlteracao').classList.add('hidden');
    await carregarMeusAgendamentos();
  } else {
    toast(extrairErro(data), 'error');
  }
}

async function historico() {
  const inicio = paraIsoLocal(document.getElementById('filtroInicio').value);
  const fim = paraIsoLocal(document.getElementById('filtroFim').value);
  if (!inicio || !fim) {
    toast('Informe o período (de/até).', 'error');
    return;
  }

  const cliente = getClienteLogado();
  const { ok, data } = await api(
    'GET',
    `/agendamentos/historico?inicio=${encodeURIComponent(inicio)}&fim=${encodeURIComponent(fim)}`
  );

  if (!ok) {
    toast(extrairErro(data), 'error');
    return;
  }

  const filtrados = data.filter((a) => a.cliente && String(a.cliente.id) === String(cliente.id));
  const tbody = document.getElementById('tabelaHistorico');

  if (!filtrados.length) {
    tbody.innerHTML = '<tr><td colspan="4" class="empty-state">Nenhum agendamento neste período.</td></tr>';
    return;
  }

  tbody.innerHTML = filtrados
    .map((a) => {
      const nomes = a.servicos?.map((s) => s.nome).join(', ') || '-';
      return `<tr>
        <td>${nomes}</td>
        <td>${formatarData(a.dataHora)}</td>
        <td>${formatarValor(a.valorTotal)}</td>
        <td>${a.status}</td>
      </tr>`;
    })
    .join('');

  toast(`${filtrados.length} registro(s) encontrado(s).`);
}

async function iniciarAreaLogada() {
  const cliente = getClienteLogado();
  if (!cliente) return;
  mostrarLogado(cliente);
  try {
    await carregarServicos();
    await carregarMeusAgendamentos();
  } catch (err) {
    toast(err.message, 'error');
  }
}

document.addEventListener('DOMContentLoaded', () => {
  initTabs('.tab-btn');
  setMinDatetime(['dataHora', 'novaDataHora']);

  document.getElementById('formEntrar').addEventListener('submit', entrar);
  document.getElementById('formCadastroCliente').addEventListener('submit', cadastrar);
  document.getElementById('formAgendamento').addEventListener('submit', agendar);
  document.getElementById('formAlteracao').addEventListener('submit', alterar);
  document.getElementById('btnFiltrar').addEventListener('click', historico);
  document.getElementById('btnSair').addEventListener('click', () => {
    sairCliente();
    mostrarLogin();
    toast('Você saiu da conta.');
  });

  const logado = getClienteLogado();
  if (logado) {
    iniciarAreaLogada();
  }
});
