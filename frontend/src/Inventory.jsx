import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { storedFoodApi, foodApi, storageTypeApi } from './api.js';

// ── Modal shell ───────────────────────────────────────────────────────────────

function Modal({ title, onClose, children }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="bg-white rounded-sm shadow-xl w-full max-w-md mx-4 p-6">
        <div className="flex items-center justify-between mb-5">
          <h3 className="font-display text-xl font-bold text-ink">{title}</h3>
          <button onClick={onClose} className="font-mono text-lg text-ink-muted hover:text-ink">×</button>
        </div>
        {children}
      </div>
    </div>
  );
}

// ── Add stored food form ──────────────────────────────────────────────────────

function AddStoredFoodForm({ homeId, foods, storageTypes, onSave, onCancel }) {
  const [form, setForm] = useState({ foodId: '', storageTypeId: '', quantity: 1 });
  const [error, setError]   = useState('');
  const [saving, setSaving] = useState(false);

  function set(f) { return e => setForm(v => ({ ...v, [f]: e.target.value })); }

  async function handleSubmit(e) {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      await onSave({
        homeId,
        foodId:        Number(form.foodId),
        storageTypeId: Number(form.storageTypeId),
        quantity:      Number(form.quantity),
      });
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {error && <p className="text-sm text-red-600 bg-red-50 px-3 py-2 rounded-sm">{error}</p>}

      <div>
        <label className="label">Food item *</label>
        <select className="input" value={form.foodId} onChange={set('foodId')} required>
          <option value="">— Select —</option>
          {foods.map(f => (
            <option key={f.id} value={f.id}>{f.name}{f.remarks ? ` (${f.remarks})` : ''}</option>
          ))}
        </select>
      </div>

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
        <input className="input" type="number" min="0" value={form.quantity}
          onChange={set('quantity')} required />
      </div>

      <div className="flex gap-3 pt-1">
        <button type="submit" className="btn-primary flex-1" disabled={saving}>
          {saving ? 'Adding…' : 'Add to inventory'}
        </button>
        <button type="button" className="btn-secondary flex-1" onClick={onCancel}>Cancel</button>
      </div>
    </form>
  );
}

// ── Adjust quantity form ──────────────────────────────────────────────────────

function AdjustForm({ entry, onSave, onCancel }) {
  const [delta, setDelta] = useState('');
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    const d = Number(delta);
    if (!delta || d === 0) { setError('Enter a non-zero delta.'); return; }
    setSaving(true);
    setError('');
    try {
      await onSave(d);
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <p className="font-body text-sm text-ink-muted">
        <strong>{entry.food.name}</strong> — current quantity: <strong>{entry.quantity}</strong>
      </p>
      {error && <p className="text-sm text-red-600 bg-red-50 px-3 py-2 rounded-sm">{error}</p>}
      <div>
        <label className="label">Delta (+ restock, − consume)</label>
        <input className="input" type="number" value={delta}
          onChange={e => setDelta(e.target.value)} required autoFocus
          placeholder="e.g. 3 or -1" />
      </div>
      <div className="flex gap-3 pt-1">
        <button type="submit" className="btn-primary flex-1" disabled={saving}>
          {saving ? 'Saving…' : 'Adjust'}
        </button>
        <button type="button" className="btn-secondary flex-1" onClick={onCancel}>Cancel</button>
      </div>
    </form>
  );
}

// ── Stored food card ──────────────────────────────────────────────────────────

function StoredFoodCard({ entry, onAdjust, onDelete }) {
  const food = entry.food;

  function expiryColor() {
    if (!food?.effectiveUseBy) return 'text-ink-muted';
    const days = Math.ceil((new Date(food.effectiveUseBy) - new Date()) / 86_400_000);
    if (days <= 3) return 'text-red-600';
    if (days <= 7) return 'text-amber-600';
    return 'text-green-700';
  }

  return (
    <div className="card flex flex-col gap-3">
      {/* Top row */}
      <div className="flex items-start justify-between">
        <div>
          <p className="font-body font-semibold text-ink">{food?.name ?? '—'}</p>
          {food?.remarks && <p className="font-body text-xs text-ink-muted">{food.remarks}</p>}
        </div>
        <span className="badge bg-forest/10 text-forest font-mono">
          {entry.storageType.name}
        </span>
      </div>

      {/* Expiry */}
      {food?.effectiveUseBy && (
        <p className={`font-mono text-xs ${expiryColor()}`}>
          Use by: {food.effectiveUseBy}
        </p>
      )}

      {/* Quantity stepper */}
      <div className="flex items-center gap-3 mt-auto pt-2 border-t border-gray-100">
        <span className="font-mono text-xs text-ink-muted">Qty</span>
        <span className="font-display text-2xl font-bold text-ink">{entry.quantity}</span>
        <div className="flex gap-2 ml-auto">
          <button onClick={() => onAdjust(entry)}
            className="border border-forest text-forest rounded-sm w-8 h-8 flex items-center justify-center
                       text-lg font-bold hover:bg-forest hover:text-cream transition-colors">
            ±
          </button>
          <button onClick={() => onDelete(entry)}
            className="border border-red-300 text-red-500 rounded-sm w-8 h-8 flex items-center justify-center
                       text-sm hover:bg-red-500 hover:text-white transition-colors">
            ×
          </button>
        </div>
      </div>
    </div>
  );
}

// ── Main page ─────────────────────────────────────────────────────────────────

export default function Inventory() {
  const { user } = useAuth();

  const [entries, setEntries]           = useState([]);
  const [foods, setFoods]               = useState([]);
  const [storageTypes, setStorageTypes] = useState([]);
  const [loading, setLoading]           = useState(true);
  const [error, setError]               = useState('');

  const [modal, setModal]       = useState(null);   // null | 'add' | 'adjust'
  const [selected, setSelected] = useState(null);

  const [filterStorage, setFilterStorage] = useState('');

  async function loadAll() {
    try {
      const [e, f, st] = await Promise.all([
        storedFoodApi.getAll(user?.homeId),
        foodApi.getAll(),
        storageTypeApi.getAll(),
      ]);
      setEntries(e);
      setFoods(f);
      setStorageTypes(st);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { if (user) loadAll(); }, [user]);

  async function handleAdd(payload) {
    await storedFoodApi.create(payload);
    await loadAll();
    setModal(null);
  }

  async function handleAdjust(delta) {
    const updated = await storedFoodApi.adjustQuantity(selected.id, delta);
    // If null returned, entry was auto-deleted (qty hit 0)
    if (!updated) {
      setEntries(prev => prev.filter(e => e.id !== selected.id));
    } else {
      await loadAll();
    }
    setModal(null);
  }

  async function handleDelete(entry) {
    if (!confirm(`Remove "${entry.food.name}" from inventory?`)) return;
    await storedFoodApi.delete(entry.id);
    setEntries(prev => prev.filter(e => e.id !== entry.id));
  }

  const filtered = filterStorage
    ? entries.filter(e => e.storageType.name === filterStorage)
    : entries;

  // Group by storage type
  const grouped = storageTypes.reduce((acc, st) => {
    const items = filtered.filter(e => e.storageType.name === st.name);
    if (items.length > 0) acc[st.name] = items;
    return acc;
  }, {});

  return (
    <div className="p-8 max-w-6xl">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="font-display text-3xl font-bold text-ink">Inventory</h2>
          <p className="font-body text-sm text-ink-muted mt-1">{entries.length} stored entries</p>
        </div>
        <button className="btn-primary" onClick={() => { setSelected(null); setModal('add'); }}>
          + Add to inventory
        </button>
      </div>

      {/* Filter */}
      <div className="mb-6 flex gap-2 flex-wrap">
        <button
          onClick={() => setFilterStorage('')}
          className={`badge text-xs px-3 py-1.5 cursor-pointer transition-colors
            ${!filterStorage ? 'bg-forest text-cream' : 'bg-gray-100 text-ink-muted hover:bg-gray-200'}`}>
          All
        </button>
        {storageTypes.map(st => (
          <button key={st.id}
            onClick={() => setFilterStorage(st.name === filterStorage ? '' : st.name)}
            className={`badge text-xs px-3 py-1.5 cursor-pointer transition-colors
              ${filterStorage === st.name ? 'bg-forest text-cream' : 'bg-gray-100 text-ink-muted hover:bg-gray-200'}`}>
            {st.name}
          </button>
        ))}
      </div>

      {error && <p className="text-sm text-red-600 mb-4">{error}</p>}

      {loading ? (
        <p className="font-mono text-sm text-ink-muted animate-pulse">Loading…</p>
      ) : filtered.length === 0 ? (
        <div className="card py-16 text-center">
          <p className="font-body text-ink-muted">No inventory entries yet.</p>
          <button className="btn-primary mt-4" onClick={() => setModal('add')}>
            Add your first item
          </button>
        </div>
      ) : (
        <div className="space-y-8">
          {Object.entries(grouped).map(([location, items]) => (
            <div key={location}>
              <h3 className="font-mono text-xs uppercase tracking-widest text-ink-muted mb-3">
                {location} — {items.length} item{items.length !== 1 ? 's' : ''}
              </h3>
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                {items.map(entry => (
                  <StoredFoodCard
                    key={entry.id}
                    entry={entry}
                    onAdjust={e => { setSelected(e); setModal('adjust'); }}
                    onDelete={handleDelete}
                  />
                ))}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Modals */}
      {modal === 'add' && (
        <Modal title="Add to inventory" onClose={() => setModal(null)}>
          <AddStoredFoodForm
            homeId={user?.homeId}
            foods={foods.filter(f => !f.empty)}
            storageTypes={storageTypes}
            onSave={handleAdd}
            onCancel={() => setModal(null)}
          />
        </Modal>
      )}
      {modal === 'adjust' && selected && (
        <Modal title="Adjust quantity" onClose={() => setModal(null)}>
          <AdjustForm entry={selected} onSave={handleAdjust} onCancel={() => setModal(null)} />
        </Modal>
      )}
    </div>
  );
}
