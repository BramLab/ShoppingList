import { useState, useEffect } from 'react';
import { foodApi } from '../../frontend/src/api/api';

// ── Small helpers ─────────────────────────────────────────────────────────────

function daysUntil(dateStr) {
  if (!dateStr) return null;
  return Math.ceil((new Date(dateStr) - new Date()) / 86_400_000);
}

function ExpiryBadge({ food }) {
  const days = daysUntil(food.effectiveUseBy);
  if (food.empty)         return <span className="badge bg-gray-100 text-gray-500">Empty</span>;
  if (days === null)      return null;
  if (days < 0)           return <span className="badge bg-red-200 text-red-800">Expired</span>;
  if (days === 0)         return <span className="badge bg-red-100 text-red-700">Today</span>;
  if (days <= 3)          return <span className="badge bg-red-100 text-red-700">{days}d left</span>;
  if (days <= 7)          return <span className="badge bg-amber-100 text-amber-700">{days}d left</span>;
  return <span className="badge bg-green-100 text-green-700">{days}d left</span>;
}

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

// ── Add / Edit food form ──────────────────────────────────────────────────────
// Shared for both "Add food" and "Edit food" — identical fields.

function FoodForm({ initial, onSave, onCancel }) {
  const [form, setForm] = useState(initial ?? {
    name: '', remarks: '', bestBeforeEnd: '', original_ml_g: '',
  });
  const [error, setError]   = useState('');
  const [saving, setSaving] = useState(false);

  function set(f) { return e => setForm(v => ({ ...v, [f]: e.target.value })); }

  async function handleSubmit(e) {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      const payload = {
        ...form,
        original_ml_g: Number(form.original_ml_g),
      };
      await onSave(payload);
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
          <label className="label">Name *</label>
          <input className="input" value={form.name} onChange={set('name')} required placeholder="e.g. Soja melk" />
        </div>
        <div>
          <label className="label">Remarks</label>
          <input className="input" value={form.remarks ?? ''} onChange={set('remarks')} placeholder="e.g. brand / variant" />
        </div>
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="label">Best before *</label>
            <input className="input" type="date" value={form.bestBeforeEnd ?? ''} onChange={set('bestBeforeEnd')} required />
          </div>
          <div>
            <label className="label">Amount (ml/g) *</label>
            <input className="input" type="number" min="1" step="any" value={form.original_ml_g} onChange={set('original_ml_g')} required />
          </div>
        </div>
        <div className="flex gap-3 pt-1">
          <button type="submit" className="btn-primary flex-1" disabled={saving}>
            {saving ? 'Saving…' : 'Save'}
          </button>
          <button type="button" className="btn-secondary flex-1" onClick={onCancel}>Cancel</button>
        </div>
      </form>
  );
}

// ── Open package form ─────────────────────────────────────────────────────────

function OpenPackageForm({ food, onSave, onCancel }) {
  const [form, setForm] = useState({ useBy: '', initialConsumption: 0 });
  const [error, setError]   = useState('');
  const [saving, setSaving] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      await onSave({ useBy: form.useBy, initialConsumption: Number(form.initialConsumption) });
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  }

  return (
      <form onSubmit={handleSubmit} className="space-y-4">
        <p className="font-body text-sm text-ink-muted">
          Opening <strong>{food.name}</strong>. Set a shorter use-by date for the opened package.
        </p>
        {error && <p className="text-sm text-red-600 bg-red-50 px-3 py-2 rounded-sm">{error}</p>}
        <div>
          <label className="label">Use by *</label>
          <input className="input" type="date" value={form.useBy}
                 max={food.bestBeforeEnd}
                 onChange={e => setForm(f => ({ ...f, useBy: e.target.value }))} required />
        </div>
        <div>
          <label className="label">Initial consumption (ml/g)</label>
          <input className="input" type="number" min="0" value={form.initialConsumption}
                 onChange={e => setForm(f => ({ ...f, initialConsumption: e.target.value }))} />
        </div>
        <div className="flex gap-3 pt-1">
          <button type="submit" className="btn-terra flex-1" disabled={saving}>
            {saving ? 'Opening…' : 'Open package'}
          </button>
          <button type="button" className="btn-secondary flex-1" onClick={onCancel}>Cancel</button>
        </div>
      </form>
  );
}

// ── Consume form ──────────────────────────────────────────────────────────────

function ConsumeForm({ food, onSave, onCancel }) {
  const [amount, setAmount] = useState('');
  const [error, setError]   = useState('');
  const [saving, setSaving] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    if (!amount || Number(amount) <= 0) { setError('Amount must be > 0'); return; }
    setSaving(true);
    setError('');
    try {
      await onSave({ amount: Number(amount) });
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  }

  return (
      <form onSubmit={handleSubmit} className="space-y-4">
        <p className="font-body text-sm text-ink-muted">
          Remaining: <strong>{food.remaining_ml_g} ml/g</strong>
        </p>
        {error && <p className="text-sm text-red-600 bg-red-50 px-3 py-2 rounded-sm">{error}</p>}
        <div>
          <label className="label">Amount to consume (ml/g) *</label>
          <input className="input" type="number" min="1" max={food.remaining_ml_g}
                 value={amount} onChange={e => setAmount(e.target.value)} required autoFocus />
        </div>
        <div className="flex gap-3 pt-1">
          <button type="submit" className="btn-primary flex-1" disabled={saving}>
            {saving ? 'Consuming…' : 'Consume'}
          </button>
          <button type="button" className="btn-secondary flex-1" onClick={onCancel}>Cancel</button>
        </div>
      </form>
  );
}

// ── Food row ──────────────────────────────────────────────────────────────────

function FoodRow({ food, onEdit, onDelete, onOpen, onConsume }) {
  return (
      <tr className="border-b border-gray-100 hover:bg-cream transition-colors">
        {/* Name */}
        <td className="py-3 px-4">
          <p className="font-body font-medium text-ink">{food.name}</p>
          {food.remarks && <p className="font-body text-xs text-ink-muted">{food.remarks}</p>}
        </td>

        {/* Best before */}
        <td className="py-3 px-4 font-mono text-sm text-ink-muted">{food.bestBeforeEnd}</td>

        {/* Status */}
        <td className="py-3 px-4">
          <ExpiryBadge food={food} />
        </td>

        {/* Remaining / Original */}
        <td className="py-3 px-4 font-mono text-sm">
          {food.empty ? '—' : `${food.remaining_ml_g} / ${food.original_ml_g}`}
        </td>

        {/* Edit column */}
        <td className="py-3 px-4">
          <button
              onClick={() => onEdit(food)}
              className="btn-secondary text-xs px-3 py-1.5"
          >
            Edit
          </button>
        </td>

        {/* Other actions */}
        <td className="py-3 px-4">
          <div className="flex gap-2 flex-wrap">
            {!food.useBy && !food.empty && (
                <button onClick={() => onOpen(food)} className="btn-terra text-xs px-3 py-1.5">Open</button>
            )}
            {food.useBy && !food.empty && (
                <button onClick={() => onConsume(food)} className="btn-secondary text-xs px-3 py-1.5">Consume</button>
            )}
            <button onClick={() => onDelete(food)} className="btn-danger text-xs px-3 py-1.5">Delete</button>
          </div>
        </td>
      </tr>
  );
}

// ── Main page ─────────────────────────────────────────────────────────────────

export default function Foods() {
  const [foods, setFoods]       = useState([]);
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState('');

  // Modal state: null | 'add' | 'edit' | 'open' | 'consume'
  const [modal, setModal]       = useState(null);
  const [selected, setSelected] = useState(null);

  const [search, setSearch] = useState('');

  async function loadFoods() {
    try {
      setFoods(await foodApi.getAll());
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { loadFoods(); }, []);

  async function handleAdd(payload) {
    await foodApi.create(payload);
    await loadFoods();
    setModal(null);
  }

  async function handleEdit(payload) {
    await foodApi.update(selected.id, payload);
    await loadFoods();
    setModal(null);
  }

  async function handleDelete(food) {
    if (!confirm(`Delete "${food.name}"? This cannot be undone.`)) return;
    await foodApi.delete(food.id);
    await loadFoods();
  }

  async function handleOpen(payload) {
    await foodApi.openPackage(selected.id, payload);
    await loadFoods();
    setModal(null);
  }

  async function handleConsume(payload) {
    await foodApi.consume(selected.id, payload);
    await loadFoods();
    setModal(null);
  }

  const filtered = foods.filter(f =>
      f.name.toLowerCase().includes(search.toLowerCase()) ||
      (f.remarks ?? '').toLowerCase().includes(search.toLowerCase())
  );

  // Column headers — Edit is now its own dedicated column
  const COL_HEADERS = ['Name', 'Best before', 'Status', 'Remaining / Original', 'Edit', 'Actions'];

  return (
      <div className="p-8 max-w-6xl">
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <div>
            <h2 className="font-display text-3xl font-bold text-ink">Food Catalogue</h2>
            <p className="font-body text-sm text-ink-muted mt-1">{foods.length} items total</p>
          </div>
          <button className="btn-primary" onClick={() => { setSelected(null); setModal('add'); }}>
            + Add food
          </button>
        </div>

        {/* Search */}
        <div className="mb-5">
          <input
              className="input max-w-xs"
              placeholder="Search by name or remarks…"
              value={search}
              onChange={e => setSearch(e.target.value)}
          />
        </div>

        {error && <p className="text-sm text-red-600 mb-4">{error}</p>}

        {/* Table */}
        {loading ? (
            <p className="font-mono text-sm text-ink-muted animate-pulse">Loading…</p>
        ) : (
            <div className="card p-0 overflow-hidden">
              <table className="w-full text-sm">
                <thead className="bg-cream-dark border-b border-gray-200">
                <tr>
                  {COL_HEADERS.map(h => (
                      <th key={h} className="text-left py-3 px-4 font-mono text-xs text-ink-muted uppercase tracking-wider">
                        {h}
                      </th>
                  ))}
                </tr>
                </thead>
                <tbody>
                {filtered.length === 0 ? (
                    <tr>
                      <td colSpan={COL_HEADERS.length} className="py-10 text-center font-body text-sm text-ink-muted">
                        No items found.
                      </td>
                    </tr>
                ) : (
                    filtered.map(f => (
                        <FoodRow
                            key={f.id}
                            food={f}
                            onEdit={food => { setSelected(food); setModal('edit'); }}
                            onDelete={handleDelete}
                            onOpen={food => { setSelected(food); setModal('open'); }}
                            onConsume={food => { setSelected(food); setModal('consume'); }}
                        />
                    ))
                )}
                </tbody>
              </table>
            </div>
        )}

        {/* Modals */}
        {modal === 'add' && (
            <Modal title="Add food item" onClose={() => setModal(null)}>
              <FoodForm onSave={handleAdd} onCancel={() => setModal(null)} />
            </Modal>
        )}

        {modal === 'edit' && selected && (
            <Modal title="Edit food item" onClose={() => setModal(null)}>
              <FoodForm
                  initial={{
                    name:          selected.name,
                    remarks:       selected.remarks ?? '',
                    bestBeforeEnd: selected.bestBeforeEnd,
                    original_ml_g: selected.original_ml_g,
                  }}
                  onSave={handleEdit}
                  onCancel={() => setModal(null)}
              />
            </Modal>
        )}

        {modal === 'open' && selected && (
            <Modal title="Open package" onClose={() => setModal(null)}>
              <OpenPackageForm food={selected} onSave={handleOpen} onCancel={() => setModal(null)} />
            </Modal>
        )}

        {modal === 'consume' && selected && (
            <Modal title="Record consumption" onClose={() => setModal(null)}>
              <ConsumeForm food={selected} onSave={handleConsume} onCancel={() => setModal(null)} />
            </Modal>
        )}
      </div>
  );
}
