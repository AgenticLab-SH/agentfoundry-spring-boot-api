const $ = (selector) => document.querySelector(selector);
const state = { member: null, offerings: [], requests: [], engagements: [], action: null };

const labels = {
  listing: { ASSET: '자산', BUILD_SERVICE: '구축 서비스', EDUCATION: '교육', PROJECT: '프로젝트', KNOWLEDGE: '지식' },
  artifact: { INSTRUCTION: 'Instruction', SKILL: 'Skill', LOOP: 'Loop', CONTEXT: 'Context', HARNESS: 'Harness', FULL_AGENT: 'Full Agent' },
  domain: { DEVELOPMENT: '개발', DATA: '데이터', CONTENT: '콘텐츠', OPERATIONS: '운영', EDUCATION: '교육', BUSINESS: '비즈니스' },
  environment: { LOCAL: '로컬', CLOUD: '클라우드', HYBRID: '하이브리드', ANY: '환경 무관' },
  engagement: { ACTIVE: '진행 중', CANCELED: '취소', COMPLETED: '완료' },
  ledger: { INITIAL: '초기 크레딧', ENGAGEMENT_USE: '참여 사용', CANCEL_REFUND: '취소 환급', PROVIDER_REWARD: '제공자 보상' },
};

async function api(path, options = {}) {
  const response = await fetch(path, {
    credentials: 'same-origin',
    headers: options.body ? { 'Content-Type': 'application/json', ...(options.headers || {}) } : options.headers,
    ...options,
  });
  const payload = response.status === 204 ? null : await response.json().catch(() => ({ message: '응답 형식을 읽을 수 없습니다.' }));
  if (!response.ok) {
    const error = new Error(payload?.message || '요청에 실패했습니다.');
    error.payload = payload;
    throw error;
  }
  return payload;
}

function showToast(message, error = false) {
  const toast = $('#toast');
  toast.textContent = message;
  toast.classList.toggle('error', error);
  toast.hidden = false;
  clearTimeout(showToast.timer);
  showToast.timer = setTimeout(() => { toast.hidden = true; }, 3600);
}

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>'"]/g, (character) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' })[character]);
}

async function loadMember() {
  try {
    state.member = (await api('/api/auth/me')).data;
  } catch (error) {
    if (error.payload?.status !== 401) showToast(error.message, true);
    state.member = null;
  }
  renderMember();
}

function renderMember() {
  const loggedIn = Boolean(state.member);
  $('#loginForm').hidden = loggedIn;
  $('#memberCard').hidden = !loggedIn;
  $('#accountStatus').textContent = loggedIn ? '로그인됨' : '로그인 전';
  if (loggedIn) {
    $('#memberName').textContent = state.member.displayName;
    $('#memberCode').textContent = state.member.memberId;
    $('#creditBalance').textContent = state.member.creditBalance;
  }
}

async function loadOfferings() {
  $('#offeringLoading').hidden = false;
  $('#offeringGrid').hidden = true;
  const params = new URLSearchParams({ page: '0', size: '30', sort: 'createdAt,desc' });
  for (const [elementId, parameter] of [['keyword', 'keyword'], ['listingType', 'listingType'], ['artifactType', 'artifactType'], ['domain', 'domain']]) {
    const value = $(`#${elementId}`).value.trim();
    if (value) params.set(parameter, value);
  }
  try {
    state.offerings = (await api(`/api/offerings?${params}`)).data.content;
    renderOfferings();
  } catch (error) {
    showToast(error.message, true);
  } finally {
    $('#offeringLoading').hidden = true;
    $('#offeringGrid').hidden = false;
  }
}

function renderOfferings() {
  const grid = $('#offeringGrid');
  grid.replaceChildren();
  $('#offeringEmpty').hidden = state.offerings.length !== 0;
  for (const offering of state.offerings) {
    const card = document.createElement('article');
    card.className = 'offering-card';
    card.innerHTML = `
      <div class="card-badges"><span class="badge type">${labels.listing[offering.listingType]} · ${labels.artifact[offering.artifactType]}</span><span class="badge status ${offering.status.toLowerCase()}">${offering.status}</span></div>
      <h3>${escapeHtml(offering.title)}</h3>
      <p>${escapeHtml(offering.summary)}</p>
      <p class="provider">제공자 · ${escapeHtml(offering.providerDisplayName)}</p>
      <div class="meta-grid">
        <div><small>도메인</small><strong>${labels.domain[offering.domain]}</strong></div>
        <div><small>환경</small><strong>${labels.environment[offering.environment]}</strong></div>
        <div><small>필요 리소스</small><strong>${offering.minimumMemoryGb}GB · ${offering.estimatedHours}h</strong></div>
        <div><small>비용 / 슬롯</small><strong>${offering.creditCost}C · ${offering.availableSlots}/${offering.capacity}</strong></div>
      </div>
      <div class="signal-row"><span>완료 ${offering.completedCount}</span><span>추천 ${offering.recommendationCount}</span><span>${escapeHtml(offering.licenseName)}</span></div>`;
    grid.append(card);
  }
}

async function loadRequests() {
  const select = $('#requestSelect');
  select.innerHTML = '<option value="">내 요청을 선택하세요</option>';
  $('#findMatches').disabled = true;
  if (!state.member) return;
  try {
    const all = (await api('/api/requests?size=50&sort=createdAt,desc')).data.content;
    state.requests = all.filter((request) => request.requesterId === state.member.id && request.status === 'OPEN');
    for (const request of state.requests) {
      const option = document.createElement('option');
      option.value = request.id;
      option.textContent = `${request.title} · ${request.maxCredits}C`;
      select.append(option);
    }
    if (state.requests.length) {
      select.value = state.requests[0].id;
      renderRequestBrief();
      $('#findMatches').disabled = false;
    } else {
      $('#requestBrief').hidden = true;
      $('#matchList').innerHTML = '<p class="muted">현재 매칭 가능한 내 OPEN 요청이 없습니다.</p>';
    }
  } catch (error) {
    showToast(error.message, true);
  }
}

function selectedRequest() {
  return state.requests.find((request) => String(request.id) === $('#requestSelect').value);
}

function renderRequestBrief() {
  const request = selectedRequest();
  const brief = $('#requestBrief');
  if (!request) { brief.hidden = true; return; }
  brief.innerHTML = `<strong>${escapeHtml(request.title)}</strong><small>${labels.artifact[request.desiredArtifactType]} · ${labels.domain[request.domain]} · ${labels.environment[request.environment]} · ${request.availableMemoryGb}GB · 최대 ${request.maxCredits}C</small>`;
  brief.hidden = false;
}

async function findMatches() {
  const request = selectedRequest();
  if (!request) return;
  const list = $('#matchList');
  list.innerHTML = '<p class="muted">호환도 근거를 계산하는 중입니다.</p>';
  try {
    const matches = (await api(`/api/requests/${request.id}/matches`)).data;
    renderMatches(request, matches);
  } catch (error) {
    list.innerHTML = `<p class="muted">${escapeHtml(error.message)}</p>`;
    showToast(error.message, true);
  }
}

function renderMatches(request, matches) {
  const list = $('#matchList');
  list.replaceChildren();
  if (!matches.length) list.innerHTML = '<p class="muted">현재 참여 가능한 후보가 없습니다.</p>';
  for (const match of matches.slice(0, 5)) {
    const details = match.scoreDetails;
    const compatible = details.typeAndArtifact === 25 && details.domain === 25 && details.resource === 20 && details.budget === 15;
    const card = document.createElement('article');
    card.className = 'match-card';
    card.innerHTML = `
      <div class="match-top"><div><h3>${escapeHtml(match.offering.title)}</h3><span class="muted">${labels.listing[match.offering.listingType]} · ${labels.artifact[match.offering.artifactType]}</span></div><div class="score">${match.compatibilityScore}<small>/100</small></div></div>
      <div class="score-track" aria-label="호환도 ${match.compatibilityScore}점"><span style="width:${match.compatibilityScore}%"></span></div>
      <div class="score-details"><div><small>유형</small><strong>${details.typeAndArtifact}/25</strong></div><div><small>도메인</small><strong>${details.domain}/25</strong></div><div><small>리소스</small><strong>${details.resource}/20</strong></div><div><small>예산</small><strong>${details.budget}/15</strong></div><div><small>슬롯</small><strong>${details.availability}/10</strong></div><div><small>신뢰</small><strong>${details.trust}/5</strong></div></div>
      <button class="${compatible ? 'primary-button' : 'secondary-button'}" type="button" ${compatible ? '' : 'disabled'}>${compatible ? `${match.offering.creditCost}C로 참여` : '필수 조건 불일치'}</button>`;
    if (compatible) card.querySelector('button').addEventListener('click', () => openAction('create', { request, offering: match.offering }));
    list.append(card);
  }
}

async function loadEngagements() {
  const list = $('#engagementList');
  if (!state.member) { list.innerHTML = '<p class="muted">로그인하면 요청자·제공자 관점의 참여를 확인할 수 있습니다.</p>'; return; }
  try {
    state.engagements = (await api('/api/engagements/me?size=30&sort=createdAt,desc')).data.content;
    list.replaceChildren();
    if (!state.engagements.length) list.innerHTML = '<p class="muted">아직 참여 내역이 없습니다.</p>';
    for (const engagement of state.engagements) {
      const role = engagement.requesterId === state.member.id ? '요청자' : '제공자';
      const item = document.createElement('div');
      item.className = 'list-item';
      item.innerHTML = `<div><strong>${escapeHtml(engagement.offeringTitle)}</strong><small>${role} · ${labels.engagement[engagement.status]} · 호환도 ${engagement.compatibilityScore}점 · ${engagement.creditCost}C</small><div class="item-actions"></div></div><div class="amount ${engagement.status === 'COMPLETED' ? 'positive' : ''}">${engagement.status}</div>`;
      const actions = item.querySelector('.item-actions');
      if (engagement.status === 'ACTIVE' && role === '요청자') addActionButton(actions, '참여 취소', () => openAction('cancel', engagement));
      if (engagement.status === 'ACTIVE' && role === '제공자') addActionButton(actions, '완료 처리', () => openAction('complete', engagement));
      if (engagement.status === 'COMPLETED' && role === '요청자') addActionButton(actions, '추천 남기기', () => openAction('recommend', engagement));
      list.append(item);
    }
  } catch (error) {
    list.innerHTML = `<p class="muted">${escapeHtml(error.message)}</p>`;
  }
}

function addActionButton(container, text, handler) {
  const button = document.createElement('button');
  button.type = 'button';
  button.textContent = text;
  button.addEventListener('click', handler);
  container.append(button);
}

async function loadCredits() {
  const list = $('#creditList');
  if (!state.member) { list.innerHTML = '<p class="muted">로그인하면 크레딧 변동을 확인할 수 있습니다.</p>'; return; }
  try {
    const transactions = (await api('/api/credits/me/transactions?size=30&sort=createdAt,desc')).data.content;
    list.replaceChildren();
    for (const transaction of transactions) {
      const item = document.createElement('div');
      item.className = 'list-item';
      const sign = transaction.amount > 0 ? '+' : '';
      item.innerHTML = `<div><strong>${labels.ledger[transaction.type] || transaction.type}</strong><small>${escapeHtml(transaction.description)} · 처리 후 ${transaction.balanceAfter}C</small></div><div class="amount ${transaction.amount >= 0 ? 'positive' : 'negative'}">${sign}${transaction.amount}C</div>`;
      list.append(item);
    }
  } catch (error) {
    list.innerHTML = `<p class="muted">${escapeHtml(error.message)}</p>`;
  }
}

function openAction(type, data) {
  state.action = { type, data };
  const recommendation = type === 'recommend';
  const titles = { create: '이 Agent Offering에 참여할까요?', cancel: '참여를 취소할까요?', complete: '참여를 완료 처리할까요?', recommend: '완료한 Offering을 추천할까요?' };
  const descriptions = {
    create: `${data.offering.creditCost} 크레딧이 차감되고 제공 슬롯과 요청 상태가 함께 변경됩니다.`,
    cancel: '사용한 크레딧이 환급되고 제공 슬롯과 요청 상태가 복구됩니다.',
    complete: '제공자에게 크레딧이 지급되고 완료 건수와 요청 상태가 함께 변경됩니다.',
    recommend: '완료한 참여 한 건당 한 번만 추천할 수 있습니다.',
  };
  $('#dialogTitle').textContent = titles[type];
  $('#dialogDescription').textContent = descriptions[type];
  $('#recommendationField').hidden = !recommendation;
  $('#recommendationComment').value = '';
  $('#actionDialog').showModal();
}

async function confirmAction(event) {
  event.preventDefault();
  if (!state.action) return;
  const { type, data } = state.action;
  try {
    let payload;
    if (type === 'create') payload = await api('/api/engagements', { method: 'POST', body: JSON.stringify({ requestId: data.request.id, offeringId: data.offering.id }) });
    if (type === 'cancel') payload = await api(`/api/engagements/${data.id}/cancel`, { method: 'POST' });
    if (type === 'complete') payload = await api(`/api/engagements/${data.id}/complete`, { method: 'POST' });
    if (type === 'recommend') {
      const comment = $('#recommendationComment').value.trim();
      if (comment.length < 5) { showToast('추천 의견을 5자 이상 입력해 주세요.', true); return; }
      payload = await api(`/api/engagements/${data.id}/recommendation`, { method: 'POST', body: JSON.stringify({ comment }) });
    }
    $('#actionDialog').close();
    showToast(payload.message);
    await refreshPrivateData();
    await loadOfferings();
  } catch (error) {
    showToast(`${error.payload?.code || 'ERROR'} · ${error.message}`, true);
  } finally {
    state.action = null;
  }
}

async function refreshPrivateData() {
  await loadMember();
  await Promise.all([loadRequests(), loadEngagements(), loadCredits()]);
}

$('#loginForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  try {
    const payload = await api('/api/auth/login', { method: 'POST', body: JSON.stringify({ memberId: $('#memberId').value, password: $('#password').value }) });
    showToast(payload.message);
    await refreshPrivateData();
  } catch (error) { showToast(`${error.payload?.code || 'ERROR'} · ${error.message}`, true); }
});
$('#logoutButton').addEventListener('click', async () => {
  try { await api('/api/auth/logout', { method: 'POST' }); } catch (error) { showToast(error.message, true); }
  state.member = null; state.requests = []; state.engagements = [];
  renderMember(); await Promise.all([loadRequests(), loadEngagements(), loadCredits()]);
  showToast('로그아웃되었습니다.');
});
$('#filterForm').addEventListener('submit', (event) => { event.preventDefault(); loadOfferings(); });
$('#requestSelect').addEventListener('change', () => { renderRequestBrief(); $('#findMatches').disabled = !selectedRequest(); });
$('#findMatches').addEventListener('click', findMatches);
$('#refreshEngagements').addEventListener('click', loadEngagements);
$('#refreshCredits').addEventListener('click', loadCredits);
$('#confirmAction').addEventListener('click', confirmAction);

await loadMember();
await Promise.all([loadOfferings(), loadRequests(), loadEngagements(), loadCredits()]);

