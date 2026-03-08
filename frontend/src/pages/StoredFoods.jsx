import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { storedFoodApi, storageTypeApi } from '../api/api';

// ─── API call (add to api.js) ──────────────────────────────────────────────
// storedFoodApi.consume  = (id, body) => request(`/api/stored-foods/${id}/consume`, { method: 'POST', body: JSON.stringify(body) });
// foodOriginalApi.addToStorage = (body) => request('/api/food-originals', { method: 'POST', body: JSON.stringify(body) });
// (these are inlined here via the existing request helper pattern for brevity)

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
        {/* Header */}
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

// ─── Consume Modal ────────────────────────────────────────────────────────

function ConsumeModal({ entry, storageTypes, onSave, onClose }) {
  const food = entry.food;
  const [form, setForm] = useState({
    remainingMlG: food?.remaining_ml_g > 0 ? String(food.remaining_ml_g) : '',
    useBy: food?.useBy ?? food?.effectiveUseBy ?? '',
    storageTypeId: String(entry.storageType?.id ?? ''),
    consumeAll: false,
  });
  const [saving, setSaving] = useState(false);
  const [error, setError]   = useState('');

  function set(f) { return e => setForm(v => ({ ...v, [f]: e.target.value })); }

  async function handleSubmit(e) {
    e.preventDefault();
    setSaving(true); setError('');
    try {
      const body = {
        remainingMlG: form.consumeAll ? 0 : Number(form.remainingMlG),
        useBy:        form.useBy || null,
        storageTypeId: form.storageTypeId ? Number(form.storageTypeId) : null,
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
      title="Consume unit"
      subtitle={`${food?.name}${food?.remarks ? ` · ${food.remarks}` : ''}`}
      onClose={onClose}
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && (
          <p className="text-sm text-red-600 bg-red-50 border border-red-200 px-3 py-2 rounded-sm">{error}</p>
        )}

        {/* Consume all toggle */}
        <label className="flex items-center gap-3 p-3 border border-gray-200 rounded-sm cursor-pointer hover:bg-cream transition-colors">
          <input
            type="checkbox"
            className="w-4 h-4 accent-forest"
            checked={form.consumeAll}
            onChange={e => setForm(v => ({ ...v, consumeAll: e.target.checked }))}
          />
          <div>
            <p className="font-body font-medium text-sm text-ink">Consume everything</p>
            <p className="font-body text-xs text-ink-muted">Sets remaining to 0 — item will be soft-deleted</p>
          </div>
        </label>

        {/* Remaining ml/g */}
        {!form.consumeAll && (
          <div>
            <label className="label">Remaining ml / g after this use</label>
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
          <label className="label">Use by</label>
          <input
            className="input"
            type="date"
            value={form.useBy}
            onChange={set('useBy')}
          />
        </div>

        {/* Storage type */}
        <div>
          <label className="label">Storage location</label>
          <select className="input" value={form.storageTypeId} onChange={set('storageTypeId')}>
            <option value="">— Keep current ({entry.storageType?.name}) —</option>
            {storageTypes.map(st => (
              <option key={st.id} value={st.id}>{st.name}</option>
            ))}
          </select>
        </div>

        {/* Current state summary */}
        <div className="bg-cream rounded-sm p-3 border border-gray-100 text-xs font-mono space-y-1">
          <p className="text-ink-muted">Original: <span className="text-ink">{food?.original_ml_g ?? '—'} ml/g</span></p>
          <p className="text-ink-muted">Currently remaining: <span className="text-ink">{food?.remaining_ml_g > 0 ? `${food.remaining_ml_g} ml/g` : 'empty'}</span></p>
          {food?.effectiveUseBy && (
            <p className="text-ink-muted">Effective use-by: <span className="text-ink">{food.effectiveUseBy}</span></p>
          )}
        </div>

        <div className="flex gap-3 pt-1">
          <button
            type="submit"
            className={`flex-1 font-body font-medium px-5 py-2.5 rounded-sm transition-all duration-150
              ${form.consumeAll
                ? 'bg-terra text-white hover:bg-terra-light active:scale-[0.98]'
                : 'btn-primary'}`}
            disabled={saving}
          >
            {saving ? 'Saving…' : form.consumeAll ? 'Consume all & delete' : 'Record consumption'}
          </button>
          <button type="button" className="btn-secondary flex-1" onClick={onClose}>Cancel</button>
        </div>
      </form>
    </Modal>
  );
}

// ─── Add to Storage Modal ─────────────────────────────────────────────────

function AddToStorageModal({ homeId, storageTypes, onSave, onClose }) {
  const [form, setForm] = useState({
    name: '', remarks: '', bestBeforeEnd: '', originalMlG: '',
    remainingMlG: '', useBy: '',
    storageTypeId: '', quantity: '1',
    isOpened: false,
  });
  const [saving, setSaving] = useState(false);
  const [error, setError]   = useState('');

  function set(f) { return e => setForm(v => ({ ...v, [f]: e.target.value })); }

  async function handleSubmit(e) {
    e.preventDefault();
    setSaving(true); setError('');
    try {
      const body = {
        name:          form.name,
        remarks:       form.remarks || null,
        bestBeforeEnd: form.bestBeforeEnd || null,
        originalMlG:   Number(form.originalMlG),
        remainingMlG:  form.isOpened && form.remainingMlG ? Number(form.remainingMlG) : null,
        useBy:         form.isOpened && form.useBy ? form.useBy : null,
        homeId:        homeId,
        storageTypeId: Number(form.storageTypeId),
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

        {/* Name + remarks */}
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

        {/* Dates + size */}
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

        {/* Already opened toggle */}
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

        {/* Storage + qty */}
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="label">Storage location *</label>
            <select className="input" value={form.storageTypeId} onChange={set('storageTypeId')} required>
              <option value="">— Select —</option>
              {storageTypes.map(st => (
                <option key={st.id} value={st.id}>{st.name}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="label">Quantity *</label>
            <input className="input" type="number" min="1" value={form.quantity} onChange={set('quantity')} required />
          </div>
        </div>

        <div className="flex gap-3 pt-1">
          <button type="submit" className="btn-primary flex-1" disabled={saving}>
            {saving ? 'Adding…' : 'Add to storage'}
          </button>
          <button type="button" className="btn-secondary flex-1" onClick={onClose}>Cancel</button>
        </div>
      </form>
    </Modal>
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

function StoredFoodRow({ entry, onConsume }) {
  const food     = entry.food;
  const urgency  = rowUrgency(entry);

  return (
    <tr className={`border-b border-gray-100 transition-colors ${URGENCY_ROW[urgency]}`}>
      {/* Name */}
      <td className="py-3 px-4">
        <p className="font-body font-semibold text-ink leading-snug">{food?.name ?? '—'}</p>
        {food?.remarks && (
          <p className="font-body text-xs text-ink-muted mt-0.5">{food.remarks}</p>
        )}
      </td>

      {/* Storage location */}
      <td className="py-3 px-4">
        <span className="badge bg-forest/10 text-forest font-mono text-xs">
          {entry.storageType?.name ?? '—'}
        </span>
      </td>

      {/* Qty */}
      <td className="py-3 px-4 font-display text-2xl font-bold text-ink text-center">
        {entry.quantity}
      </td>

      {/* Remaining */}
      <td className="py-3 px-4 font-mono text-sm text-center">
        {food?.empty
          ? <span className="text-gray-400">—</span>
          : food?.remaining_ml_g > 0
            ? <span className="text-ink">{food.remaining_ml_g}<span className="text-ink-muted text-xs"> / {food?.original_ml_g}</span></span>
            : <span className="text-gray-400">—</span>
        }
      </td>

      {/* Best before / use-by */}
      <td className="py-3 px-4">
        <ExpiryBadge food={food} />
        {food?.useBy && (
          <p className="font-mono text-xs text-ink-muted mt-0.5">{food.useBy}</p>
        )}
        {!food?.useBy && food?.bestBeforeEnd && (
          <p className="font-mono text-xs text-ink-muted mt-0.5">{food.bestBeforeEnd}</p>
        )}
      </td>

      {/* Actions */}
      <td className="py-3 px-4">
        {!food?.empty && (
          <button
            onClick={() => onConsume(entry)}
            className="btn-terra text-xs px-3 py-1.5"
          >
            Consume
          </button>
        )}
        {food?.empty && (
          <span className="font-mono text-xs text-gray-400 italic">consumed</span>
        )}
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

  const [modal, setModal]         = useState(null);   // null | 'consume' | 'add'
  const [selected, setSelected]   = useState(null);

  const [search, setSearch]       = useState('');
  const [filterLocation, setFilterLocation] = useState('');
  const [sortKey, setSortKey]     = useState('expiry'); // 'expiry' | 'name' | 'location' | 'qty'

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

  // ── Consume ─────────────────────────────────────────────────────────────
  async function handleConsume(id, body) {
    await request(`/api/stored-foods/${id}/consume`, {
      method: 'POST',
      body: JSON.stringify(body),
    });
    await load();
    setModal(null);
  }

  // ── Add to storage ──────────────────────────────────────────────────────
  async function handleAddToStorage(body) {
    await request('/api/food-originals', {
      method: 'POST',
      body: JSON.stringify(body),
    });
    await load();
    setModal(null);
  }

  // ── Filter + sort ────────────────────────────────────────────────────────
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
      {/* ── Page header ──────────────────────────────────────────────── */}
      <div className="flex items-start justify-between mb-6">
        <div>
          <h2 className="font-display text-3xl font-bold text-ink">Stored Foods</h2>
          <p className="font-body text-sm text-ink-muted mt-1">
            Your home's current inventory — click <em>Consume</em> to use a unit.
          </p>
        </div>
        <button
          className="btn-primary"
          onClick={() => setModal('add')}
        >
          + Add to storage
        </button>
      </div>

      {/* ── Summary strip ────────────────────────────────────────────── */}
      {!loading && <SummaryStrip entries={entries} />}

      {/* ── Toolbar ──────────────────────────────────────────────────── */}
      <div className="flex flex-wrap items-center gap-3 mb-5">
        {/* Search */}
        <input
          className="input max-w-xs"
          placeholder="Search by name or remarks…"
          value={search}
          onChange={e => setSearch(e.target.value)}
        />

        {/* Location filter pills */}
        <div className="flex gap-1.5 flex-wrap">
          <button
            onClick={() => setFilterLocation('')}
            className={`badge text-xs px-3 py-1.5 cursor-pointer transition-colors
              ${!filterLocation ? 'bg-forest text-cream' : 'bg-gray-100 text-ink-muted hover:bg-gray-200'}`}
          >All</button>
          {storageTypes.map(st => (
            <button
              key={st.id}
              onClick={() => setFilterLocation(f => f === st.name ? '' : st.name)}
              className={`badge text-xs px-3 py-1.5 cursor-pointer transition-colors
                ${filterLocation === st.name ? 'bg-forest text-cream' : 'bg-gray-100 text-ink-muted hover:bg-gray-200'}`}
            >{st.name}</button>
          ))}
        </div>

        {/* Sort */}
        <div className="ml-auto flex items-center gap-2">
          <span className="font-mono text-xs text-ink-muted">Sort:</span>
          {[['expiry', 'Expiry'], ['name', 'Name'], ['location', 'Location'], ['qty', 'Qty']].map(([k, l]) => (
            <button
              key={k}
              onClick={() => setSortKey(k)}
              className={`font-mono text-xs px-2.5 py-1 rounded-sm border transition-colors
                ${sortKey === k
                  ? 'bg-forest text-cream border-forest'
                  : 'border-gray-200 text-ink-muted hover:border-forest hover:text-forest'}`}
            >{l}</button>
          ))}
        </div>
      </div>

      {error && <p className="text-sm text-red-600 mb-4">{error}</p>}

      {/* ── Table ────────────────────────────────────────────────────── */}
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

      {/* ── Modals ───────────────────────────────────────────────────── */}
      {modal === 'consume' && selected && (
        <ConsumeModal
          entry={selected}
          storageTypes={storageTypes}
          onSave={handleConsume}
          onClose={() => setModal(null)}
        />
      )}
      {modal === 'add' && (
        <AddToStorageModal
          homeId={user?.homeId}
          storageTypes={storageTypes}
          onSave={handleAddToStorage}
          onClose={() => setModal(null)}
        />
      )}
    </div>
  );
}
