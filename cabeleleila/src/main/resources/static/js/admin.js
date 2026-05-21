async function listarClientes() {
  const { ok, data } = await api('GET', '/clientes');
  if (!ok) {
    toast(extrairErro(data), 'error');
    return;
  }

  const tbody = document.getElementById('tabelaClientes');
  if (!data.length) {
    tbody.innerHTML = '<tr><td colspan="4" class="empty-state">Nenhum cliente.</td></tr>';
    return;
  }

  tbody.innerHTML = data
    .map(
      (c) => `
    <tr>
      <td>${c.nome}</td>
      <td>${c.telefone}</td>
      <td>${c.email}</td>
      <td><button type="button" class="btn btn-danger" data-del-cliente="${c.id}">Excluir</button></td>
    </tr>`
    )
    .join('');

  tbody.querySelectorAll('[data-del-cliente]').forEach((btn) => {
    btn.addEventListener('click', async () => {
      if (!confirm('Excluir este cliente?')) return;
      const { ok, data } = await api('DELETE', `/clientes/${btn.dataset.delCliente}`, undefined, true);
      if (ok) {
        toast('Cliente removido.');
        listarClientes();
      } else {
        toast(extrairErro(data), 'error');
      }
    });
  });
}

async function listarServicos() {
  const { ok, data } = await api('GET', '/servicos');
  if (!ok) {
    toast(extrairErro(data), 'error');
    return;
  }

  const tbody = document.getElementById('tabelaServicos');
  if (!data.length) {
    tbody.innerHTML = '<tr><td colspan="4" class="empty-state">Nenhum serviço.</td></tr>';
    return;
  }

  tbody.innerHTML = data
    .map(
      (s) => `
    <tr>
      <td>${s.nome}</td>
      <td>R$ ${Number(s.preco).toFixed(2)}</td>
      <td>${s.duracao} min</td>
      <td><button type="button" class="btn btn-danger" data-del-servico="${s.id}">Excluir</button></td>
    </tr>`
    )
    .join('');

  tbody.querySelectorAll('[data-del-servico]').forEach((btn) => {
    btn.addEventListener('click', async () => {
      if (!confirm('Excluir este serviço?')) return;
      const { ok, data } = await api('DELETE', `/servicos/${btn.dataset.delServico}`, undefined, true);
      if (ok) {
        toast('Serviço removido.');
        listarServicos();
      } else {
        toast(extrairErro(data), 'error');
      }
    });
  });
}

function renderizarAgendamentos(lista) {
  const tbody = document.getElementById('tabelaAgendamentos');
  if (!lista.length) {
    tbody.innerHTML = '<tr><td colspan="6" class="empty-state">Nenhum agendamento.</td></tr>';
    return;
  }

  tbody.innerHTML = lista
    .map((a) => {
      const alteravel = podeAlterarPeloSistema(a.dataHora);
      const badge = alteravel
        ? '<span class="badge badge-ok">Online</span>'
        : '<span class="badge badge-blocked">Telefone</span>';
      const servicos = a.servicos?.map((s) => s.nome).join(', ') || '-';
      return `
      <tr>
        <td>${a.cliente?.nome || '—'}</td>
        <td>${servicos}</td>
        <td>${formatarData(a.dataHora)}</td>
        <td>${a.status || 'AGENDADO'}</td>
        <td>${badge}</td>
        <td><button type="button" class="btn btn-danger" data-del-ag="${a.id}">Cancelar</button></td>
      </tr>`;
    })
    .join('');

  tbody.querySelectorAll('[data-del-ag]').forEach((btn) => {
    btn.addEventListener('click', async () => {
      if (!confirm('Cancelar agendamento?')) return;
      const { ok } = await api('DELETE', `/agendamentos/${btn.dataset.delAg}`, undefined, true);
      if (ok) {
        toast('Agendamento cancelado.');
        listarAgendamentos();
      }
    });
  });
}

async function listarAgendamentos() {
  const { ok, data } = await api('GET', '/agendamentos');
  if (!ok) {
    toast(extrairErro(data), 'error');
    return;
  }
  renderizarAgendamentos(data);
}

async function filtrarHistoricoAdmin() {
  const inicio = paraIsoLocal(document.getElementById('admFiltroInicio').value);
  const fim = paraIsoLocal(document.getElementById('admFiltroFim').value);
  if (!inicio || !fim) {
    toast('Informe o período.', 'error');
    return;
  }

  const { ok, data } = await api(
    'GET',
    `/agendamentos/historico?inicio=${encodeURIComponent(inicio)}&fim=${encodeURIComponent(fim)}`
  );

  if (!ok) {
    toast(extrairErro(data), 'error');
    return;
  }

  renderizarAgendamentos(data);
  toast(`${data.length} agendamento(s) no período.`);
}

async function salvarCliente(e) {
  e.preventDefault();
  const payload = {
    nome: document.getElementById('admNome').value.trim(),
    telefone: document.getElementById('admTelefone').value.trim(),
    email: document.getElementById('admEmail').value.trim(),
  };

  const { ok, data } = await api('POST', '/clientes', payload);
  if (ok) {
    toast('Cliente cadastrado!');
    e.target.reset();
    listarClientes();
  } else {
    toast(extrairErro(data), 'error');
  }
}

async function salvarServico(e) {
  e.preventDefault();
  const payload = {
    nome: document.getElementById('admNomeServico').value.trim(),
    preco: parseFloat(document.getElementById('admPreco').value),
    duracao: parseInt(document.getElementById('admDuracao').value, 10),
  };

  const { ok, data } = await api('POST', '/servicos', payload);
  if (ok) {
    toast('Serviço cadastrado!');
    e.target.reset();
    listarServicos();
  } else {
    toast(extrairErro(data), 'error');
  }
}

document.addEventListener('DOMContentLoaded', () => {
  if (!isAdminAutenticado()) return;

  initTabs();
  document.getElementById('btnSairAdmin').addEventListener('click', () => {
    sairAdmin();
    toast('Sessão administrativa encerrada.');
    window.location.href = '/admin-login.html';
  });
  document.getElementById('formCliente').addEventListener('submit', salvarCliente);
  document.getElementById('formServico').addEventListener('submit', salvarServico);
  document.getElementById('btnHistAdmin').addEventListener('click', filtrarHistoricoAdmin);
  document.getElementById('btnListarTodos').addEventListener('click', listarAgendamentos);

  Promise.all([listarClientes(), listarServicos(), listarAgendamentos()]).catch((err) =>
    toast(err.message, 'error')
  );
});
