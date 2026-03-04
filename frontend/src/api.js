const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

function getToken() {
  return localStorage.getItem('token');
}

async function request(path, options = {}) {
  const token = getToken();
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  const res = await fetch(`${BASE_URL}${path}`, { ...options, headers });

  // 204 No Content — return null, not JSON
  if (res.status === 204) return null;

  const data = await res.json().catch(() => null);

  if (!res.ok) {
    const msg = data?.message ?? `Request failed: ${res.status}`;
    throw new Error(msg);
  }
  return data;
}

// ── Auth ────────────────────────────────────────────────────────
export const authApi = {
  login:    (body) => request('/api/auth/login',    { method: 'POST', body: JSON.stringify(body) }),
  register: (body) => request('/api/auth/register', { method: 'POST', body: JSON.stringify(body) }),
  me:       ()     => request('/api/users/me'),
};

// ── Foods ───────────────────────────────────────────────────────
export const foodApi = {
  getAll:      ()           => request('/api/foods'),
  getById:     (id)         => request(`/api/foods/${id}`),
  create:      (body)       => request('/api/foods',          { method: 'POST',   body: JSON.stringify(body) }),
  update:      (id, body)   => request(`/api/foods/${id}`,    { method: 'PATCH',  body: JSON.stringify(body) }),
  delete:      (id)         => request(`/api/foods/${id}`,    { method: 'DELETE' }),
  openPackage: (id, body)   => request(`/api/foods/${id}/open`,    { method: 'POST', body: JSON.stringify(body) }),
  consume:     (id, body)   => request(`/api/foods/${id}/consume`, { method: 'POST', body: JSON.stringify(body) }),
};

// ── Storage Types ────────────────────────────────────────────────
export const storageTypeApi = {
  getAll:  ()           => request('/api/storage-types'),
  create:  (body)       => request('/api/storage-types',       { method: 'POST',   body: JSON.stringify(body) }),
  update:  (id, body)   => request(`/api/storage-types/${id}`, { method: 'PUT',    body: JSON.stringify(body) }),
  delete:  (id)         => request(`/api/storage-types/${id}`, { method: 'DELETE' }),
};

// ── Stored Foods ─────────────────────────────────────────────────
export const storedFoodApi = {
  getAll:          (homeId)       => request(`/api/stored-foods${homeId ? `?homeId=${homeId}` : ''}`),
  getById:         (id)           => request(`/api/stored-foods/${id}`),
  create:          (body)         => request('/api/stored-foods',              { method: 'POST',  body: JSON.stringify(body) }),
  update:          (id, body)     => request(`/api/stored-foods/${id}`,        { method: 'PATCH', body: JSON.stringify(body) }),
  delete:          (id)           => request(`/api/stored-foods/${id}`,        { method: 'DELETE' }),
  adjustQuantity:  (id, delta)    => request(`/api/stored-foods/${id}/quantity`,{ method: 'PATCH', body: JSON.stringify({ delta }) }),
};

// ── Homes ────────────────────────────────────────────────────────
export const homeApi = {
  getAll:  () => request('/api/homes'),
  getById: (id) => request(`/api/homes/${id}`),
};

// ── Admin ─────────────────────────────────────────────────────────
export const adminApi = {
  getUsers:    ()         => request('/api/admin/users'),
  deleteUser:  (id)       => request(`/api/admin/users/${id}`,       { method: 'DELETE' }),
  changeRole:  (id, body) => request(`/api/admin/users/${id}/role`,  { method: 'PATCH', body: JSON.stringify(body) }),
};
