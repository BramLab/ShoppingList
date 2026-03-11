import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { storedFoodApi, storageTypeApi } from '../api/api';

const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';
function getToken() { return localStorage.getItem('token'); }
async function request(path, options = {}) {
  const token = getToken();
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };
  const res = await fetch(`${BASE_URL}${path}`, { ...options, headers });
  if (res.status === 204) return null;
  const data = await res.json().catch(() => null);
  if (!res.ok) { throw new Error(data?.message ?? `Request failed: ${res.status}`); }
  return data;
}

// ─── Constants ─────────────────────────────────────────────────────────────
const NEW_LOCATION_SENTINEL = '__new__';

// ─── Helpers ───────────────────────────────────────────────────────────────

function daysUntil(dateStr) {
  if (!dateStr) return null;
  return Math.ceil((new Date(dateStr) - new Date()) / 86_400_000);
}

function ExpiryBadge({ food }) {
  const days = daysUntil(food?.effectiveUseBy);
  if (food?.empty)     return <span className="badge bg-gray-100 text-gray-500 font-mono">Empty</span>;
  if (days === null)   return <span className="badge bg-gray-100 text-gray-400 font-mono">—</span>;
  if (days < 0)        return <span className="badge bg-red-200 text-red-800 font-mono">Expired</span>;
  if (days === 0)      return <span className="badge bg-red-100 text-red-700 font-mono">Today</span>;
  if (days <= 3)       return <span className="badge bg-red-100 text-red-700 font-mono">{days}d left</span>;
  if (days <= 7)       return <span className="badge bg-amber-100 text-amber-700 font-mono">{days}d left</span>;
  return <span className="badge bg-green-100 text-green-700 font-mono">{days}d left</span>;
}

function rowUrgency(entry) {
  const days = daysUntil(entry.food?.effectiveUseBy);
  if (entry.food?.empty) return 'empty';
  if (days !== null && days < 0) return 'expired';
  if (days !== null && days <= 3) return 'urgent';
  if (days !== null && days <= 7) return 'soon';
  return 'ok';
}

// ─── Modal shell ──────────────────────────────────────────────────────────

function Modal({ title, subtitle, onClose, children }) {
  return (
      <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
        <div className="bg-white rounded-sm shadow-2xl w-full max-w-lg mx-4 overflow-hidden">
          <div className="bg-forest-dark px-6 py-5 flex items-start justify-between">
            <div>
              <h3 className="font-display text-xl font-bold text-cream">{title}</h3>
              {subtitle && <p className="font-body text-xs text-green-300 mt-0.5">{subtitle}</p>}
            </div>
            <button
                onClick={onClose}
                className="text-green-400 hover:text-cream font-mono text-xl leading-none mt-0.5 transition-colors"
            >×</button>
          </div>
          <div className="p-6">{children}</div>
        </div>
      </div>
  );
}

// ─── Storage location select (shared) ─────────────────────────────────────

function StorageLocationSelect({ storageTypes, value, newName, onChange, onNewNameChange, required }) {
  const isNew = value === NEW_LOCATION_SENTINEL;
  return (
      <div>
        <label className="label">Storage location{required ? ' *' : ''}</label>
        <select className="input" value={value} onChange={onChange} required={required}>
          {!required && <option value="">— Keep current —</option>}
          {required   && <option value="">— Select —</option>}
          {storageTypes.map(st => (
              <option key={st.id} value={st.id}>{st.name}</option>
          ))}
          <option value={NEW_LOCATION_SENTINEL}>+ New location…</option>
        </select>
        {isNew && (
            <div className="mt-2 pl-3 border-l-2 border-forest/40">
              <input
                  className="input text-sm h-8"
                  placeholder="Location name, e.g. Freezer"
                  value={newName}
                  onChange={onNewNameChange}
                  required={isNew}
                  autoFocus
              />
            </div>
        )}
      </div>
  );
}

// ─── Consume Modal ────────────────────────────────────────────────────────

function ConsumeModal({ entry, storageTypes, homeId, onSave, onClose, onStorageTypeCreated }) {
  const food     = entry.food;
  const maxQty   = entry.quantity;           // can't consume more than in stock

  const [form, setForm] = useState({
    quantity:        '1',
    remainingMlG:    food?.remaining_ml_g > 0 ? String(food.remaining_ml_g) : '',
    useBy:           food?.useBy ?? food?.effectiveUseBy ?? '',
    storageTypeId:   String(entry.storageType?.id ?? ''),
    consumeAll:      false,
    newLocationName: '',
  });
  const [saving, setSaving] = useState(false);
  const [error, setError]   = useState('');

  const isNewLocation = form.storageTypeId === NEW_LOCATION_SENTINEL;
  const qty           = Math.max(1, Math.min(maxQty, Number(form.quantity) || 1));

  function set(f) { return e => setForm(v => ({ ...v, [f]: e.target.value })); }

  function stepQty(delta) {
    setForm(v => ({
      ...v,
      quantity: String(Math.max(1, Math.min(maxQty, (Number(v.quantity) || 1) + delta))),
    }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setSaving(true); setError('');
    try {
      let resolvedStorageTypeId = form.storageTypeId && !isNewLocation
          ? Number(form.storageTypeId)
          : null;

      if (isNewLocation) {
        if (!form.newLocationName.trim()) throw new Error('Location name is required');
        const created = await request('/api/storage-types', {
          method: 'POST',
          body: JSON.stringify({ name: form.newLocationName.trim(), homeId }),
        });
        resolvedStorageTypeId = created.id;
        onStorageTypeCreated && onStorageTypeCreated(created);
      }

      const body = {
        quantity:      qty,
        remainingMlG:  form.consumeAll ? 0 : Number(form.remainingMlG),
        useBy:         form.useBy || null,
        storageTypeId: resolvedStorageTypeId,
      };
      await onSave(entry.id, body);
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  }

  return (
      <Modal
          title="Consume units"
          subtitle={`${food?.name}${food?.remarks ? ` · ${food.remarks}` : ''}`}
          onClose={onClose}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          {error && (
              <p className="text-sm text-red-600 bg-red-50 border border-red-200 px-3 py-2 rounded-sm">{error}</p>
          )}

          {/* ── Quantity stepper ── */}
          <div>
            <label className="label">How many units to consume *</label>
            <div className="flex items-center gap-3">
              <button
                  type="button"
                  onClick={() => stepQty(-1)}
                  disabled={qty <= 1}
                  className="w-9 h-9 flex items-center justify-center border border-gray-300
                             rounded-sm font-mono text-lg text-ink hover:border-forest
                             hover:text-forest transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
              >−</button>

              <input
                  className="input text-center font-display text-xl font-bold w-20"
                  type="number"
                  min="1"
                  max={maxQty}
                  value={form.quantity}
                  onChange={e => setForm(v => ({ ...v, quantity: e.target.value }))}
                  required
              />

              <button
                  type="button"
                  onClick={() => stepQty(1)}
                  disabled={qty >= maxQty}
                  className="w-9 h-9 flex items-center justify-center border border-gray-300
                             rounded-sm font-mono text-lg text-ink hover:border-forest
                             hover:text-forest transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
              >+</button>

              <span className="font-mono text-xs text-ink-muted whitespace-nowrap">
                of {maxQty} in stock
              </span>
            </div>

            {/* Visual stock indicator */}
            {maxQty > 1 && (
                <div className="mt-2 h-1.5 rounded-full bg-gray-100 overflow-hidden">
                  <div
                      className="h-full rounded-full bg-terra transition-all duration-200"
                      style={{ width: `${(qty / maxQty) * 100}%` }}
                  />
                </div>
            )}
          </div>

          {/* Consume all toggle */}
          <label className="flex items-center gap-3 p-3 border border-gray-200 rounded-sm cursor-pointer hover:bg-cream transition-colors">
            <input
                type="checkbox"
                className="w-4 h-4 accent-forest"
                checked={form.consumeAll}
                onChange={e => setForm(v => ({ ...v, consumeAll: e.target.checked }))}
            />
            <div>
              <p className="font-body font-medium text-sm text-ink">Mark opened unit as empty</p>
              <p className="font-body text-xs text-ink-muted">Sets remaining to 0 — opened unit will be soft-deleted</p>
            </div>
          </label>

          {/* Remaining ml/g */}
          {!form.consumeAll && (
              <div>
                <label className="label">Remaining ml / g in the opened unit</label>
                <div className="flex items-center gap-2">
                  <input
                      className="input"
                      type="number"
                      min="0"
                      step="any"
                      value={form.remainingMlG}
                      onChange={set('remainingMlG')}
                      required={!form.consumeAll}
                      placeholder={`was ${food?.original_ml_g ?? '?'}`}
                  />
                  <span className="font-mono text-xs text-ink-muted whitespace-nowrap">ml/g</span>
                </div>
              </div>
          )}

          {/* Use-by */}
          <div>
            <label className="label">Use by (opened unit)</label>
            <input className="input" type="date" value={form.useBy} onChange={set('useBy')} />
          </div>

          {/* Storage type */}
          <StorageLocationSelect
              storageTypes={storageTypes}
              value={form.storageTypeId}
              newName={form.newLocationName}
              onChange={set('storageTypeId')}
              onNewNameChange={set('newLocationName')}
              required={false}
          />

          {/* Current state summary */}
          <div className="bg-cream rounded-sm p-3 border border-gray-100 text-xs font-mono space-y-1">
            <p className="text-ink-muted">Original: <span className="text-ink">{food?.original_ml_g ?? '—'} ml/g</span></p>
            <p className="text-ink-muted">Currently remaining: <span className="text-ink">{food?.remaining_ml_g > 0 ? `${food.remaining_ml_g} ml/g` : 'empty'}</span></p>
            {food?.effectiveUseBy && (
                <p className="text-ink-muted">Effective use-by: <span className="text-ink">{food.effectiveUseBy}</span></p>
            )}
            <p className="text-ink-muted">
              Stock after consuming:{' '}
              <span className={`${maxQty - qty === 0 ? 'text-terra' : 'text-ink'} font-medium`}>
                {maxQty - qty} unit{maxQty - qty !== 1 ? 's' : ''} remaining
              </span>
            </p>
          </div>

          <div className="flex gap-3 pt-1">
            <button
                type="submit"
                className={`flex-1 font-body font-medium px-5 py-2.5 rounded-sm transition-all duration-150 active:scale-[0.98]
              ${form.consumeAll
                    ? 'bg-terra text-white hover:bg-terra-light'
                    : 'btn-primary'}`}
                disabled={saving}
            >
              {saving
                  ? 'Saving…'
                  : form.consumeAll
                      ? `Consume ${qty} & mark empty`
                      : `Consume ${qty} unit${qty !== 1 ? 's' : ''}`}
            </button>
            <button type="button" className="btn-secondary flex-1" onClick={onClose}>Cancel</button>
          </div>
        </form>
      </Modal>
  );
}

// ─── Edit Food Original Modal ──────────────────────────────────────────────

function EditFoodModal({ entry, storageTypes, homeId, onSave, onClose, onStorageTypeCreated }) {
  const food = entry.food;
  const [form, setForm] = useState({
    name:            food.name ?? '',
    remarks:         food.remarks ?? '',
    bestBeforeEnd:   food.bestBeforeEnd ?? '',
    original_ml_g:   food.original_ml_g ?? '',
    storageTypeId:   String(entry.storageType?.id ?? ''),
    quantity:        String(entry.quantity ?? 1),
    newLocationName: '',
  });
  const [saving, setSaving] = useState(false);
  const [error, setError]   = useState('');

  const isNewLocation = form.storageTypeId === NEW_LOCATION_SENTINEL;

  function set(f) { return e => setForm(v => ({ ...v, [f]: e.target.value })); }

  async function handleSubmit(e) {
    e.preventDefault();
    setSaving(true); setError('');
    try {
      let resolvedStorageTypeId = isNewLocation ? null : Number(form.storageTypeId);
      if (isNewLocation) {
        if (!form.newLocationName.trim()) throw new Error('Location name is required');
        const created = await request('/api/storage-types', {
          method: 'POST',
          body: JSON.stringify({ name: form.newLocationName.trim(), homeId }),
        });
        resolvedStorageTypeId = created.id;
        onStorageTypeCreated && onStorageTypeCreated(created);
      }

      await onSave({
        entryId:         entry.id,
        foodId:          food.id,
        foodPayload: {
          name:          form.name,
          remarks:       form.remarks || null,
          bestBeforeEnd: form.bestBeforeEnd,
          original_ml_g: Number(form.original_ml_g),
        },
        entryPayload: {
          storageTypeId: resolvedStorageTypeId,
          quantity:      Number(form.quantity),
        },
      });
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  }

  return (
      <Modal
          title="Edit food item"
          subtitle={`${food.name}${food.remarks ? ` · ${food.remarks}` : ''}`}
          onClose={onClose}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          {error && (
              <p className="text-sm text-red-600 bg-red-50 border border-red-200 px-3 py-2 rounded-sm">{error}</p>
          )}

          <p className="font-mono text-xs uppercase tracking-widest text-ink-muted border-b border-gray-100 pb-1">
            Food details
          </p>

          <div>
            <label className="label">Name *</label>
            <input className="input" value={form.name} onChange={set('name')} required />
          </div>
          <div>
            <label className="label">Remarks</label>
            <input className="input" value={form.remarks} onChange={set('remarks')} placeholder="e.g. brand / variant" />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="label">Best before *</label>
              <input className="input" type="date" value={form.bestBeforeEnd} onChange={set('bestBeforeEnd')} required />
            </div>
            <div>
              <label className="label">Total ml / g *</label>
              <input className="input" type="number" min="1" step="any" value={form.original_ml_g} onChange={set('original_ml_g')} required />
            </div>
          </div>

          <p className="font-mono text-xs uppercase tracking-widest text-ink-muted border-b border-gray-100 pb-1 pt-1">
            Storage
          </p>

          <div className="grid grid-cols-2 gap-3">
            <StorageLocationSelect
                storageTypes={storageTypes}
                value={form.storageTypeId}
                newName={form.newLocationName}
                onChange={set('storageTypeId')}
                onNewNameChange={set('newLocationName')}
                required
            />
            <div>
              <label className="label">Quantity *</label>
              <input
                  className="input"
                  type="number"
                  min="0"
                  value={form.quantity}
                  onChange={set('quantity')}
                  required
              />
            </div>
          </div>

          <div className="flex gap-3 pt-1">
            <button type="submit" className="btn-primary flex-1" disabled={saving}>
              {saving ? 'Saving…' : 'Save changes'}
            </button>
            <button type="button" className="btn-secondary flex-1" onClick={onClose}>Cancel</button>
          </div>
        </form>
      </Modal>
  );
}

// ─── Add to Storage Modal ─────────────────────────────────────────────────

function AddToStorageModal({ homeId, storageTypes, onSave, onClose, onStorageTypeCreated }) {
  const [form, setForm] = useState({
    name: '', remarks: '', bestBeforeEnd: '', originalMlG: '',
    remainingMlG: '', useBy: '',
    storageTypeId: '', newLocationName: '',
    quantity: '1',
    isOpened: false,
  });
  const [saving, setSaving] = useState(false);
  const [error, setError]   = useState('');

  const isNewLocation = form.storageTypeId === NEW_LOCATION_SENTINEL;

  function set(f) { return e => setForm(v => ({ ...v, [f]: e.target.value })); }

  async function handleSubmit(e) {
    e.preventDefault();
    setSaving(true); setError('');
    try {
      let resolvedStorageTypeId = Number(form.storageTypeId);
      if (isNewLocation) {
        if (!form.newLocationName.trim()) throw new Error('Location name is required');
        const created = await request('/api/storage-types', {
          method: 'POST',
          body: JSON.stringify({ name: form.newLocationName.trim(), homeId }),
        });
        resolvedStorageTypeId = created.id;
        onStorageTypeCreated && onStorageTypeCreated(created);
      }

      const body = {
        name:          form.name,
        remarks:       form.remarks || null,
        bestBeforeEnd: form.bestBeforeEnd || null,
        originalMlG:   Number(form.originalMlG),
        remainingMlG:  form.isOpened && form.remainingMlG ? Number(form.remainingMlG) : null,
        useBy:         form.isOpened && form.useBy ? form.useBy : null,
        homeId:        homeId,
        storageTypeId: resolvedStorageTypeId,
        quantity:      Number(form.quantity),
      };
      await onSave(body);
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  }

  return (
      <Modal title="Add food to storage" subtitle="Creates a FoodOriginal and registers it in your pantry" onClose={onClose}>
        <form onSubmit={handleSubmit} className="space-y-4">
          {error && (
              <p className="text-sm text-red-600 bg-red-50 border border-red-200 px-3 py-2 rounded-sm">{error}</p>
          )}

          <div className="grid grid-cols-2 gap-3">
            <div className="col-span-2">
              <label className="label">Name *</label>
              <input className="input" value={form.name} onChange={set('name')} required placeholder="e.g. Soja melk" />
            </div>
            <div className="col-span-2">
              <label className="label">Remarks</label>
              <input className="input" value={form.remarks} onChange={set('remarks')} placeholder="e.g. Brand / variant" />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="label">Best before *</label>
              <input className="input" type="date" value={form.bestBeforeEnd} onChange={set('bestBeforeEnd')} required />
            </div>
            <div>
              <label className="label">Total ml / g *</label>
              <input className="input" type="number" min="1" step="any" value={form.originalMlG} onChange={set('originalMlG')} required />
            </div>
          </div>

          <label className="flex items-center gap-3 p-3 border border-gray-200 rounded-sm cursor-pointer hover:bg-cream transition-colors">
            <input
                type="checkbox"
                className="w-4 h-4 accent-forest"
                checked={form.isOpened}
                onChange={e => setForm(v => ({ ...v, isOpened: e.target.checked }))}
            />
            <div>
              <p className="font-body font-medium text-sm text-ink">Already opened</p>
              <p className="font-body text-xs text-ink-muted">Set remaining amount and a use-by date</p>
            </div>
          </label>

          {form.isOpened && (
              <div className="grid grid-cols-2 gap-3 pl-3 border-l-2 border-forest/30">
                <div>
                  <label className="label">Remaining ml / g</label>
                  <input className="input" type="number" min="0" step="any" value={form.remainingMlG}
                         onChange={set('remainingMlG')} placeholder={form.originalMlG || '…'} />
                </div>
                <div>
                  <label className="label">Use by</label>
                  <input className="input" type="date" value={form.useBy} onChange={set('useBy')} />
                </div>
              </div>
          )}

          <div className="grid grid-cols-2 gap-3">
            <StorageLocationSelect
                storageTypes={storageTypes}
                value={form.storageTypeId}
                newName={form.newLocationName}
                onChange={set('storageTypeId')}
                onNewNameChange={set('newLocationName')}
                required
            />
            <div>
              <label className="label">Quantity *</label>
              <input className="input" type="number" min="1" value={form.quantity} onChange={set('quantity')} required />
            </div>
          </div>

          <div className="flex gap-3 pt-1">
            <button type="submit" className="btn-primary flex-1" disabled={saving}>
              {saving ? (isNewLocation ? 'Creating location…' : 'Adding…') : 'Add to storage'}
            </button>
            <button type="button" className="btn-secondary flex-1" onClick={onClose}>Cancel</button>
          </div>
        </form>
      </Modal>
  );
}

// ─── Storage Type Manager ─────────────────────────────────────────────────

function StorageTypeManager({ storageTypes, entries, onUpdate, onDelete }) {
  const [editingId, setEditingId] = useState(null);
  const [editName, setEditName]   = useState('');
  const [busy, setBusy]           = useState(false);
  const [err, setErr]             = useState('');

  const countById = entries.reduce((acc, e) => {
    const id = e.storageType?.id;
    if (id != null) acc[id] = (acc[id] ?? 0) + 1;
    return acc;
  }, {});

  function startEdit(st) {
    setEditingId(st.id);
    setEditName(st.name);
    setErr('');
  }

  function cancelEdit() {
    setEditingId(null);
    setEditName('');
    setErr('');
  }

  async function saveEdit(id) {
    if (!editName.trim()) { setErr('Name cannot be empty'); return; }
    setBusy(true); setErr('');
    try {
      const updated = await request(`/api/storage-types/${id}`, {
        method: 'PUT',
        body: JSON.stringify({ name: editName.trim() }),
      });
      onUpdate(updated);
      cancelEdit();
    } catch (e) {
      setErr(e.message);
    } finally {
      setBusy(false);
    }
  }

  async function handleDelete(st) {
    if (!confirm(`Delete storage location "${st.name}"? This cannot be undone.`)) return;
    setBusy(true); setErr('');
    try {
      await request(`/api/storage-types/${st.id}`, { method: 'DELETE' });
      onDelete(st.id);
    } catch (e) {
      setErr(e.message);
    } finally {
      setBusy(false);
    }
  }

  if (storageTypes.length === 0) {
    return <p className="font-body text-xs text-ink-muted py-1">No storage types defined yet.</p>;
  }

  return (
      <div className="mt-3 border border-gray-200 rounded-sm overflow-hidden">
        {err && <p className="px-3 py-2 text-xs text-red-600 bg-red-50 border-b border-red-100">{err}</p>}
        <table className="w-full text-sm">
          <thead className="bg-cream-dark border-b border-gray-200">
          <tr>
            <th className="text-left py-2 px-3 font-mono text-xs text-ink-muted uppercase tracking-wider">Location</th>
            <th className="text-center py-2 px-3 font-mono text-xs text-ink-muted uppercase tracking-wider">Items stored</th>
            <th className="py-2 px-3 font-mono text-xs text-ink-muted uppercase tracking-wider text-right">Actions</th>
          </tr>
          </thead>
          <tbody>
          {storageTypes.map(st => {
            const count = countById[st.id] ?? 0;
            return (
                <tr key={st.id} className="border-b border-gray-100 last:border-0">
                  <td className="py-2 px-3">
                    {editingId === st.id ? (
                        <input
                            className="input text-sm py-1 h-8"
                            value={editName}
                            onChange={e => setEditName(e.target.value)}
                            onKeyDown={e => {
                              if (e.key === 'Enter') saveEdit(st.id);
                              if (e.key === 'Escape') cancelEdit();
                            }}
                            autoFocus
                        />
                    ) : (
                        <span className="font-body text-ink">{st.name}</span>
                    )}
                  </td>
                  <td className="py-2 px-3 text-center">
                  <span className={`badge font-mono text-xs ${
                      count === 0
                          ? 'bg-gray-100 text-gray-400'
                          : 'bg-forest/10 text-forest'
                  }`}>
                    {count}
                  </span>
                  </td>
                  <td className="py-2 px-3">
                    <div className="flex gap-2 justify-end">
                      {editingId === st.id ? (
                          <>
                            <button onClick={() => saveEdit(st.id)} disabled={busy} className="btn-primary text-xs px-3 py-1">
                              {busy ? '…' : 'Save'}
                            </button>
                            <button onClick={cancelEdit} className="btn-secondary text-xs px-3 py-1">Cancel</button>
                          </>
                      ) : (
                          <>
                            <button onClick={() => startEdit(st)} className="btn-secondary text-xs px-3 py-1">Rename</button>
                            {count === 0 && (
                                <button onClick={() => handleDelete(st)} disabled={busy} className="btn-danger text-xs px-3 py-1">
                                  Delete
                                </button>
                            )}
                          </>
                      )}
                    </div>
                  </td>
                </tr>
            );
          })}
          </tbody>
        </table>
      </div>
  );
}

// ─── Table row ─────────────────────────────────────────────────────────────

const URGENCY_ROW = {
  expired: 'bg-red-50 hover:bg-red-100',
  urgent:  'bg-orange-50 hover:bg-orange-100',
  soon:    'bg-amber-50/60 hover:bg-amber-100/60',
  empty:   'bg-gray-50 opacity-60 hover:bg-gray-100',
  ok:      'hover:bg-cream',
};

function StoredFoodRow({ entry, onConsume, onEditFood }) {
  const food    = entry.food;
  const urgency = rowUrgency(entry);

  if (!food) {
    return (
        <tr className="border-b border-gray-100 bg-gray-50 opacity-60">
          <td className="py-3 px-4" colSpan={5}>
          <span className="font-body text-sm text-gray-400 italic">
            Food deleted — restore it on the <strong>Deleted Foods</strong> page to see details
          </span>
          </td>
          <td className="py-3 px-4">
            <span className="font-mono text-xs text-gray-400">qty: {entry.quantity}</span>
          </td>
        </tr>
    );
  }

  return (
      <tr className={`border-b border-gray-100 transition-colors ${URGENCY_ROW[urgency]}`}>
        <td className="py-3 px-4">
          <p className="font-body font-semibold text-ink leading-snug">{food.name}</p>
          {food.remarks && (
              <p className="font-body text-xs text-ink-muted mt-0.5">{food.remarks}</p>
          )}
        </td>
        <td className="py-3 px-4">
        <span className="badge bg-forest/10 text-forest font-mono text-xs">
          {entry.storageType?.name ?? '—'}
        </span>
        </td>
        <td className="py-3 px-4 font-display text-2xl font-bold text-ink text-center">
          {entry.quantity}
        </td>
        <td className="py-3 px-4 font-mono text-sm text-center">
          {food.empty
              ? <span className="text-gray-400">—</span>
              : food.remaining_ml_g > 0
                  ? <span className="text-ink">{food.remaining_ml_g}<span className="text-ink-muted text-xs"> / {food.original_ml_g}</span></span>
                  : <span className="text-gray-400">—</span>
          }
        </td>
        <td className="py-3 px-4">
          <ExpiryBadge food={food} />
          {food.useBy && (
              <p className="font-mono text-xs text-ink-muted mt-0.5">{food.useBy}</p>
          )}
          {!food.useBy && food.bestBeforeEnd && (
              <p className="font-mono text-xs text-ink-muted mt-0.5">{food.bestBeforeEnd}</p>
          )}
        </td>
        <td className="py-3 px-4">
          <div className="flex gap-2 flex-wrap">
            <button onClick={() => onEditFood(entry)} className="btn-secondary text-xs px-3 py-1.5">
              Edit
            </button>
            {!food.empty && (
                <button onClick={() => onConsume(entry)} className="btn-terra text-xs px-3 py-1.5">
                  Consume
                </button>
            )}
            {food.empty && (
                <span className="font-mono text-xs text-gray-400 italic self-center">consumed</span>
            )}
          </div>
        </td>
      </tr>
  );
}

// ─── Summary strip ─────────────────────────────────────────────────────────

function SummaryStrip({ entries }) {
  const total    = entries.length;
  const expiring = entries.filter(e => {
    const d = daysUntil(e.food?.effectiveUseBy);
    return d !== null && d >= 0 && d <= 7;
  }).length;
  const empty    = entries.filter(e => e.food?.empty).length;

  const items = [
    { label: 'Stored entries', value: total,    color: 'text-forest' },
    { label: 'Expiring ≤ 7d',  value: expiring, color: expiring > 0 ? 'text-terra' : 'text-forest' },
    { label: 'Empty units',    value: empty,    color: empty > 0    ? 'text-gray-500' : 'text-forest' },
  ];

  return (
      <div className="grid grid-cols-3 gap-4 mb-6">
        {items.map(({ label, value, color }) => (
            <div key={label} className="card border-l-4 border-forest py-3">
              <p className="label">{label}</p>
              <p className={`font-display text-3xl font-bold ${color}`}>{value}</p>
            </div>
        ))}
      </div>
  );
}

// ─── Main page ──────────────────────────────────────────────────────────────

export default function StoredFoods() {
  const { user } = useAuth();

  const [entries, setEntries]           = useState([]);
  const [storageTypes, setStorageTypes] = useState([]);
  const [loading, setLoading]           = useState(true);
  const [error, setError]               = useState('');

  const [modal, setModal]         = useState(null);
  const [selected, setSelected]   = useState(null);

  const [search, setSearch]             = useState('');
  const [filterLocation, setFilterLocation] = useState('');
  const [sortKey, setSortKey]           = useState('expiry');
  const [showLocationMgr, setShowLocationMgr] = useState(false);

  const load = useCallback(async () => {
    if (!user?.homeId) return;
    try {
      const [e, st] = await Promise.all([
        storedFoodApi.getAll(user.homeId),
        storageTypeApi.getAll(),
      ]);
      setEntries(e);
      setStorageTypes(st);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [user?.homeId]);

  useEffect(() => { load(); }, [load]);

  async function handleConsume(id, body) {
    await request(`/api/stored-foods/${id}/consume`, {
      method: 'POST',
      body: JSON.stringify(body),
    });
    await load();
    setModal(null);
  }

  async function handleAddToStorage(body) {
    await request('/api/food-originals', {
      method: 'POST',
      body: JSON.stringify(body),
    });
    await load();
    setModal(null);
  }

  async function handleEditFood({ entryId, foodId, foodPayload, entryPayload }) {
    await Promise.all([
      request(`/api/foods/${foodId}`, {
        method: 'PATCH',
        body: JSON.stringify(foodPayload),
      }),
      request(`/api/stored-foods/${entryId}`, {
        method: 'PATCH',
        body: JSON.stringify(entryPayload),
      }),
    ]);
    await load();
    setModal(null);
  }

  function handleStorageTypeCreated(st) {
    setStorageTypes(prev => [...prev, st]);
  }

  function handleStorageTypeUpdated(updated) {
    setStorageTypes(prev => prev.map(st => st.id === updated.id ? updated : st));
  }

  function handleStorageTypeDeleted(id) {
    setStorageTypes(prev => prev.filter(st => st.id !== id));
    if (filterLocation) {
      const gone = storageTypes.find(st => st.id === id);
      if (gone && filterLocation === gone.name) setFilterLocation('');
    }
  }

  const SORT_FNS = {
    expiry:   (a, b) => {
      const da = daysUntil(a.food?.effectiveUseBy) ?? 9999;
      const db = daysUntil(b.food?.effectiveUseBy) ?? 9999;
      return da - db;
    },
    name:     (a, b) => (a.food?.name ?? '').localeCompare(b.food?.name ?? ''),
    location: (a, b) => (a.storageType?.name ?? '').localeCompare(b.storageType?.name ?? ''),
    qty:      (a, b) => b.quantity - a.quantity,
  };

  const filtered = entries
      .filter(e =>
          (!filterLocation || e.storageType?.name === filterLocation) &&
          (!search || (e.food?.name ?? '').toLowerCase().includes(search.toLowerCase())
              || (e.food?.remarks ?? '').toLowerCase().includes(search.toLowerCase()))
      )
      .sort(SORT_FNS[sortKey]);

  const COL_HEADERS = [
    { key: 'name',     label: 'Food item' },
    { key: 'location', label: 'Location' },
    { key: 'qty',      label: 'Qty' },
    { key: null,       label: 'Remaining / Total' },
    { key: 'expiry',   label: 'Expiry' },
    { key: null,       label: 'Actions' },
  ];

  return (
      <div className="p-8 max-w-6xl">
        <div className="flex items-start justify-between mb-6">
          <div>
            <h2 className="font-display text-3xl font-bold text-ink">Stored Foods</h2>
            <p className="font-body text-sm text-ink-muted mt-1">
              Your home's current inventory — click <em>Consume</em> to use a unit.
            </p>
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-3 mb-2">
          <input
              className="input max-w-48 h-8 px-2.5"
              placeholder="Search by name or remarks…"
              value={search}
              onChange={e => setSearch(e.target.value)}
          />

          <div className="flex items-center gap-2 h-8">
            <label className="font-mono text-xs text-ink-muted whitespace-nowrap">Filter:</label>
            <select
                value={filterLocation}
                onChange={e => setFilterLocation(e.target.value)}
                className="input text-sm h-8"
            >
              <option value="">All locations</option>
              {storageTypes.map(st => (
                  <option key={st.id} value={st.name}>{st.name}</option>
              ))}
            </select>
          </div>

          <button
              onClick={() => setShowLocationMgr(v => !v)}
              title="Manage storage locations"
              className={`h-8 text-xs px-2.5 py-1.5 cursor-pointer transition-colors border rounded-sm
            ${showLocationMgr
                  ? 'bg-forest text-cream border-forest'
                  : 'border-gray-300 text-ink-muted hover:border-forest hover:text-forest'}`}
          >
            ⚙ Manage
          </button>

          <div className="flex items-center gap-2 h-8">
            <label className="font-mono text-xs text-ink-muted whitespace-nowrap">Sort:</label>
            <select
                value={sortKey}
                onChange={e => setSortKey(e.target.value)}
                className="input text-sm h-8"
            >
              <option value="expiry">Expiry</option>
              <option value="name">Name</option>
              <option value="location">Location</option>
              <option value="qty">Quantity</option>
            </select>
          </div>

          <button className="btn-primary ml-auto h-8" onClick={() => setModal('add')}>
            + Add to storage
          </button>
        </div>

        {showLocationMgr && (
            <div className="mb-5 bg-cream-dark border border-gray-200 rounded-sm p-4">
              <p className="font-mono text-xs uppercase tracking-widest text-ink-muted mb-2">
                Manage storage locations
              </p>
              <StorageTypeManager
                  storageTypes={storageTypes}
                  entries={entries}
                  onUpdate={handleStorageTypeUpdated}
                  onDelete={handleStorageTypeDeleted}
              />
            </div>
        )}

        {error && <p className="text-sm text-red-600 mb-4 mt-3">{error}</p>}

        {loading ? (
            <div className="card py-20 text-center">
              <p className="font-mono text-sm text-ink-muted animate-pulse">Loading inventory…</p>
            </div>
        ) : filtered.length === 0 ? (
            <div className="card py-20 text-center">
              <p className="font-display text-2xl text-ink-muted mb-2">Nothing here yet</p>
              <p className="font-body text-sm text-ink-muted mb-5">
                {entries.length === 0
                    ? "Your pantry is empty. Add some food to get started."
                    : "No entries match your current filters."}
              </p>
              {entries.length === 0 && (
                  <button className="btn-primary" onClick={() => setModal('add')}>
                    Add first item
                  </button>
              )}
            </div>
        ) : (
            <div className="card p-0 overflow-hidden">
              <table className="w-full text-sm">
                <thead className="bg-cream-dark border-b border-gray-200">
                <tr>
                  {COL_HEADERS.map(({ key, label }) => (
                      <th
                          key={label}
                          onClick={key ? () => setSortKey(key) : undefined}
                          className={`text-left py-3 px-4 font-mono text-xs text-ink-muted uppercase tracking-wider select-none
                      ${key ? 'cursor-pointer hover:text-forest transition-colors' : ''}
                      ${sortKey === key ? 'text-forest' : ''}`}
                      >
                        {label}
                        {sortKey === key && <span className="ml-1 text-forest">↑</span>}
                      </th>
                  ))}
                </tr>
                </thead>
                <tbody>
                {filtered.map(entry => (
                    <StoredFoodRow
                        key={entry.id}
                        entry={entry}
                        onConsume={e => { setSelected(e); setModal('consume'); }}
                        onEditFood={e => { setSelected(e); setModal('editFood'); }}
                    />
                ))}
                </tbody>
              </table>
              <div className="border-t border-gray-100 px-4 py-2">
                <p className="font-mono text-xs text-ink-muted">
                  Showing {filtered.length} of {entries.length} entries
                </p>
              </div>
            </div>
        )}

        {modal === 'consume' && selected && (
            <ConsumeModal
                entry={selected}
                storageTypes={storageTypes}
                homeId={user?.homeId}
                onSave={handleConsume}
                onClose={() => setModal(null)}
                onStorageTypeCreated={handleStorageTypeCreated}
            />
        )}
        {modal === 'add' && (
            <AddToStorageModal
                homeId={user?.homeId}
                storageTypes={storageTypes}
                onSave={handleAddToStorage}
                onClose={() => setModal(null)}
                onStorageTypeCreated={handleStorageTypeCreated}
            />
        )}
        {modal === 'editFood' && selected && (
            <EditFoodModal
                entry={selected}
                storageTypes={storageTypes}
                homeId={user?.homeId}
                onSave={handleEditFood}
                onClose={() => setModal(null)}
                onStorageTypeCreated={handleStorageTypeCreated}
            />
        )}
      </div>
  );
}