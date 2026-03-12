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

const NEW_LOCATION_SENTINEL = '__new__';

// ─── Helpers ───────────────────────────────────────────────────────────────

function daysUntil(dateStr) {
    if (!dateStr) return null;
    return Math.ceil((new Date(dateStr) - new Date()) / 86_400_000);
}

function ExpiryBadge({ food }) {
    const days = daysUntil(food?.effectiveUseBy);
    if (food?.empty)   return <span className="badge bg-parchment-dark text-ink-muted font-mono">Empty</span>;
    if (days === null) return <span className="badge bg-parchment-dark text-ink-muted font-mono">—</span>;
    if (days < 0)      return <span className="badge bg-red-100 text-red-800 font-mono">Expired</span>;
    if (days === 0)    return <span className="badge bg-red-100 text-red-700 font-mono">Today</span>;
    if (days <= 3)     return <span className="badge bg-red-100 text-red-700 font-mono">{days}d left</span>;
    if (days <= 7)     return <span className="badge bg-amber-100 text-amber-700 font-mono">{days}d left</span>;
    return <span className="badge bg-mist text-kale font-mono">{days}d left</span>;
}

function rowUrgency(entry) {
    const days = daysUntil(entry.food?.effectiveUseBy);
    if (entry.food?.empty)             return 'empty';
    if (days !== null && days < 0)     return 'expired';
    if (days !== null && days <= 3)    return 'urgent';
    if (days !== null && days <= 7)    return 'soon';
    return 'ok';
}

// ─── Modal shell ──────────────────────────────────────────────────────────

function Modal({ title, subtitle, onClose, children }) {
    useEffect(() => {
        document.body.style.overflow = 'hidden';
        return () => { document.body.style.overflow = ''; };
    }, []);

    return (
        <div
            className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/50 backdrop-blur-sm"
            onClick={e => { if (e.target === e.currentTarget) onClose(); }}
        >
            <div className="bg-white w-full sm:max-w-lg sm:mx-4 rounded-t-2xl sm:rounded-2xl shadow-2xl overflow-hidden
                      max-h-[92dvh] sm:max-h-[90dvh] flex flex-col">
                <div className="sm:hidden flex justify-center pt-2.5 pb-1 flex-shrink-0">
                    <div className="w-10 h-1 rounded-full bg-sage-dark" />
                </div>
                <div className="bg-kale-dark px-5 sm:px-6 py-4 sm:py-5 flex items-start justify-between border-b-2 border-carrot flex-shrink-0">
                    <div>
                        <h3 className="font-display text-lg sm:text-xl font-bold text-parchment">{title}</h3>
                        {subtitle && <p className="font-body text-xs text-sprout mt-0.5 line-clamp-1">{subtitle}</p>}
                    </div>
                    <button onClick={onClose}
                            className="text-sprout hover:text-parchment font-mono text-2xl leading-none ml-4 flex-shrink-0 transition-colors"
                    >×</button>
                </div>
                <div className="p-4 sm:p-6 bg-parchment overflow-y-auto">{children}</div>
            </div>
        </div>
    );
}

// ─── Storage location select ───────────────────────────────────────────────

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
                <div className="mt-2 pl-3 border-l-2 border-leaf/40">
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
    const food   = entry.food;
    const maxQty = entry.quantity;

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
                    <p className="text-sm text-red-600 bg-red-50 border border-red-200 px-3 py-2 rounded">{error}</p>
                )}

                {/* Quantity stepper */}
                <div>
                    <label className="label">How many units to consume *</label>
                    <div className="flex items-center gap-3">
                        <button
                            type="button"
                            onClick={() => stepQty(-1)}
                            disabled={qty <= 1}
                            className="w-9 h-9 flex items-center justify-center border border-sage-dark
                         rounded font-mono text-lg text-ink hover:border-kale
                         hover:text-kale transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
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
                            className="w-9 h-9 flex items-center justify-center border border-sage-dark
                         rounded font-mono text-lg text-ink hover:border-kale
                         hover:text-kale transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
                        >+</button>

                        <span className="font-mono text-xs text-ink-muted whitespace-nowrap">
              of {maxQty} in stock
            </span>
                    </div>

                    {maxQty > 1 && (
                        <div className="mt-2 h-1.5 rounded-full bg-sage overflow-hidden">
                            <div
                                className="h-full rounded-full bg-carrot transition-all duration-200"
                                style={{ width: `${(qty / maxQty) * 100}%` }}
                            />
                        </div>
                    )}
                </div>

                {/* Consume all toggle */}
                <label className="flex items-center gap-3 p-3 border border-sage-dark rounded cursor-pointer hover:bg-mist/40 transition-colors">
                    <input
                        type="checkbox"
                        className="w-4 h-4 accent-kale"
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

                {/* Summary */}
                <div className="bg-mist/40 rounded p-3 border border-sage text-xs font-mono space-y-1">
                    <p className="text-ink-muted">Original: <span className="text-ink">{food?.original_ml_g ?? '—'} ml/g</span></p>
                    <p className="text-ink-muted">Currently remaining: <span className="text-ink">{food?.remaining_ml_g > 0 ? `${food.remaining_ml_g} ml/g` : 'empty'}</span></p>
                    {food?.effectiveUseBy && (
                        <p className="text-ink-muted">Effective use-by: <span className="text-ink">{food.effectiveUseBy}</span></p>
                    )}
                    <p className="text-ink-muted">
                        Stock after consuming:{' '}
                        <span className={`${maxQty - qty === 0 ? 'text-carrot' : 'text-ink'} font-medium`}>
              {maxQty - qty} unit{maxQty - qty !== 1 ? 's' : ''} remaining
            </span>
                    </p>
                </div>

                <div className="flex gap-3 pt-1">
                    <button
                        type="submit"
                        className={`flex-1 font-body font-medium px-5 py-2.5 rounded transition-all duration-150 active:scale-[0.98]
              ${form.consumeAll ? 'bg-carrot text-white hover:bg-carrot-light' : 'btn-primary'}`}
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

// ─── Edit Food Modal ───────────────────────────────────────────────────────

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
                entryId: entry.id,
                foodId:  food.id,
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
                    <p className="text-sm text-red-600 bg-red-50 border border-red-200 px-3 py-2 rounded">{error}</p>
                )}

                <p className="font-mono text-xs uppercase tracking-widest text-ink-muted border-b border-sage pb-1">
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

                <p className="font-mono text-xs uppercase tracking-widest text-ink-muted border-b border-sage pb-1 pt-1">
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
                homeId,
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
        <Modal title="Add food to storage" subtitle="Creates a food entry and registers it in your pantry" onClose={onClose}>
            <form onSubmit={handleSubmit} className="space-y-4">
                {error && (
                    <p className="text-sm text-red-600 bg-red-50 border border-red-200 px-3 py-2 rounded">{error}</p>
                )}

                <div className="grid grid-cols-2 gap-3">
                    <div className="col-span-2">
                        <label className="label">Name *</label>
                        <input className="input" value={form.name} onChange={set('name')} required placeholder="e.g. Oat milk" />
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

                <label className="flex items-center gap-3 p-3 border border-sage-dark rounded cursor-pointer hover:bg-mist/40 transition-colors">
                    <input
                        type="checkbox"
                        className="w-4 h-4 accent-kale"
                        checked={form.isOpened}
                        onChange={e => setForm(v => ({ ...v, isOpened: e.target.checked }))}
                    />
                    <div>
                        <p className="font-body font-medium text-sm text-ink">Already opened</p>
                        <p className="font-body text-xs text-ink-muted">Set remaining amount and a use-by date</p>
                    </div>
                </label>

                {form.isOpened && (
                    <div className="grid grid-cols-2 gap-3 pl-3 border-l-2 border-leaf/40">
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
                        {saving ? 'Adding…' : 'Add to storage'}
                    </button>
                    <button type="button" className="btn-secondary flex-1" onClick={onClose}>Cancel</button>
                </div>
            </form>
        </Modal>
    );
}

// ─── Storage Type Manager ──────────────────────────────────────────────────

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

    function startEdit(st) { setEditingId(st.id); setEditName(st.name); setErr(''); }
    function cancelEdit()  { setEditingId(null); setEditName(''); setErr(''); }

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
        if (!confirm(`Delete storage location "${st.name}"?`)) return;
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
        <div className="mt-3 border border-sage rounded overflow-hidden">
            {err && <p className="px-3 py-2 text-xs text-red-600 bg-red-50 border-b border-red-100">{err}</p>}
            <table className="w-full text-sm">
                <thead className="bg-parchment-dark border-b border-sage">
                <tr>
                    <th className="text-left py-2 px-3 font-mono text-xs text-ink-muted uppercase tracking-wider">Location</th>
                    <th className="text-center py-2 px-3 font-mono text-xs text-ink-muted uppercase tracking-wider">Items</th>
                    <th className="py-2 px-3 font-mono text-xs text-ink-muted uppercase tracking-wider text-right">Actions</th>
                </tr>
                </thead>
                <tbody>
                {storageTypes.map(st => {
                    const count = countById[st.id] ?? 0;
                    return (
                        <tr key={st.id} className="border-b border-sage/50 last:border-0">
                            <td className="py-2 px-3">
                                {editingId === st.id ? (
                                    <input
                                        className="input text-sm py-1 h-8"
                                        value={editName}
                                        onChange={e => setEditName(e.target.value)}
                                        onKeyDown={e => {
                                            if (e.key === 'Enter')  saveEdit(st.id);
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
                      count === 0 ? 'bg-parchment-dark text-ink-muted' : 'bg-mist text-kale'
                  }`}>{count}</span>
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

// ─── Mobile card ───────────────────────────────────────────────────────────

const URGENCY_CARD = {
    expired: 'border-l-red-500 bg-red-50',
    urgent:  'border-l-orange-400 bg-orange-50',
    soon:    'border-l-amber-400 bg-amber-50/60',
    empty:   'border-l-sage opacity-60',
    ok:      'border-l-kale',
};

function StoredFoodCard({ entry, onConsume, onEditFood }) {
    const food    = entry.food;
    const urgency = rowUrgency(entry);

    if (!food) {
        return (
            <div className="card border-l-4 border-l-sage opacity-60 py-3">
                <p className="font-body text-sm text-ink-muted italic">
                    Food deleted — restore on the Deleted Foods page
                </p>
                <p className="font-mono text-xs text-ink-muted mt-1">qty: {entry.quantity}</p>
            </div>
        );
    }

    return (
        <div className={`card border-l-4 ${URGENCY_CARD[urgency]} py-3 px-4`}>
            <div className="flex items-start justify-between gap-3 mb-2">
                <div className="min-w-0">
                    <p className="font-body font-semibold text-ink leading-snug truncate">{food.name}</p>
                    {food.remarks && <p className="font-body text-xs text-ink-muted mt-0.5 truncate">{food.remarks}</p>}
                </div>
                <ExpiryBadge food={food} />
            </div>
            <div className="flex items-center gap-3 text-xs font-mono text-ink-muted mb-3 flex-wrap">
                <span className="badge bg-mist text-kale">{entry.storageType?.name ?? '—'}</span>
                <span>×{entry.quantity}</span>
                {food.remaining_ml_g > 0 && (
                    <span>{food.remaining_ml_g}<span className="text-sage-dark">/{food.original_ml_g} ml/g</span></span>
                )}
                {(food.useBy || food.bestBeforeEnd) && (
                    <span className="text-ink-muted">{food.useBy || food.bestBeforeEnd}</span>
                )}
            </div>
            <div className="flex gap-2">
                <button onClick={() => onEditFood(entry)} className="btn-secondary text-xs px-3 py-1.5 flex-1">
                    Edit
                </button>
                {!food.empty && (
                    <button onClick={() => onConsume(entry)} className="btn-terra text-xs px-3 py-1.5 flex-1">
                        Consume
                    </button>
                )}
                {food.empty && (
                    <span className="font-mono text-xs text-ink-muted italic self-center ml-1">consumed</span>
                )}
            </div>
        </div>
    );
}

// ─── Desktop table row ────────────────────────────────────────────────────

const URGENCY_ROW = {
    expired: 'bg-red-50 hover:bg-red-100',
    urgent:  'bg-orange-50 hover:bg-orange-100',
    soon:    'bg-amber-50/60 hover:bg-amber-100/60',
    empty:   'bg-parchment opacity-60 hover:bg-parchment-dark',
    ok:      'hover:bg-mist/30',
};

function StoredFoodRow({ entry, onConsume, onEditFood }) {
    const food    = entry.food;
    const urgency = rowUrgency(entry);

    if (!food) {
        return (
            <tr className="border-b border-sage/50 bg-parchment opacity-60">
                <td className="py-3 px-4" colSpan={5}>
          <span className="font-body text-sm text-ink-muted italic">
            Food deleted — restore it on the <strong>Deleted Foods</strong> page to see details
          </span>
                </td>
                <td className="py-3 px-4">
                    <span className="font-mono text-xs text-ink-muted">qty: {entry.quantity}</span>
                </td>
            </tr>
        );
    }

    return (
        <tr className={`border-b border-sage/50 transition-colors ${URGENCY_ROW[urgency]}`}>
            <td className="py-3 px-4">
                <p className="font-body font-semibold text-ink leading-snug">{food.name}</p>
                {food.remarks && (
                    <p className="font-body text-xs text-ink-muted mt-0.5">{food.remarks}</p>
                )}
            </td>
            <td className="py-3 px-4">
        <span className="badge bg-mist text-kale font-mono text-xs">
          {entry.storageType?.name ?? '—'}
        </span>
            </td>
            <td className="py-3 px-4 font-display text-2xl font-bold text-kale text-center">
                {entry.quantity}
            </td>
            <td className="py-3 px-4 font-mono text-sm text-center">
                {food.empty
                    ? <span className="text-ink-muted">—</span>
                    : food.remaining_ml_g > 0
                        ? <span className="text-ink">{food.remaining_ml_g}<span className="text-ink-muted text-xs"> / {food.original_ml_g}</span></span>
                        : <span className="text-ink-muted">—</span>
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
                        <span className="font-mono text-xs text-ink-muted italic self-center">consumed</span>
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
        { label: 'Stored entries', value: total,    color: 'text-kale' },
        { label: 'Expiring ≤ 7d',  value: expiring, color: expiring > 0 ? 'text-carrot' : 'text-kale' },
        { label: 'Empty units',    value: empty,    color: empty > 0    ? 'text-ink-muted' : 'text-kale' },
    ];

    return (
        <div className="grid grid-cols-3 gap-4 mb-6">
            {items.map(({ label, value, color }) => (
                <div key={label} className="card border-l-4 border-l-kale py-3">
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

    const [entries, setEntries]             = useState([]);
    const [storageTypes, setStorageTypes]   = useState([]);
    const [loading, setLoading]             = useState(true);
    const [error, setError]                 = useState('');

    const [modal, setModal]       = useState(null);
    const [selected, setSelected] = useState(null);

    const [search, setSearch]                   = useState('');
    const [filterLocation, setFilterLocation]   = useState('');
    const [sortKey, setSortKey]                 = useState('expiry');
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
            request(`/api/foods/${foodId}`, { method: 'PATCH', body: JSON.stringify(foodPayload) }),
            request(`/api/stored-foods/${entryId}`, { method: 'PATCH', body: JSON.stringify(entryPayload) }),
        ]);
        await load();
        setModal(null);
    }

    function handleStorageTypeCreated(st)  { setStorageTypes(prev => [...prev, st]); }
    function handleStorageTypeUpdated(upd) { setStorageTypes(prev => prev.map(st => st.id === upd.id ? upd : st)); }
    function handleStorageTypeDeleted(id)  { setStorageTypes(prev => prev.filter(st => st.id !== id)); }

    const SORT_FNS = {
        expiry:   (a, b) => (daysUntil(a.food?.effectiveUseBy) ?? 9999) - (daysUntil(b.food?.effectiveUseBy) ?? 9999),
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
        { key: 'location', label: 'Location'  },
        { key: 'qty',      label: 'Qty'       },
        { key: null,       label: 'Remaining / Total' },
        { key: 'expiry',   label: 'Expiry'    },
        { key: null,       label: 'Actions'   },
    ];

    return (
        <div className="p-4 sm:p-8 max-w-6xl">
            {/* Page header */}
            <div className="flex items-center justify-between mb-4 sm:mb-6">
                <div>
                    <h2 className="font-display text-2xl sm:text-3xl font-bold text-ink">Stored Foods</h2>
                    <p className="font-body text-xs sm:text-sm text-ink-muted mt-0.5 hidden sm:block">
                        Your home's current inventory — tap <em>Consume</em> to use a unit.
                    </p>
                </div>
                <button className="btn-primary text-sm px-4 py-2" onClick={() => setModal('add')}>
                    + Add
                </button>
            </div>

            {/* Toolbar: stacks on mobile */}
            <div className="flex flex-col sm:flex-row sm:flex-wrap sm:items-center gap-2 sm:gap-3 mb-3">
                <input
                    className="input py-2 px-3 text-sm"
                    placeholder="Search…"
                    value={search}
                    onChange={e => setSearch(e.target.value)}
                />

                <div className="grid grid-cols-2 gap-2 sm:flex sm:items-center sm:gap-3">
                    <select
                        value={filterLocation}
                        onChange={e => setFilterLocation(e.target.value)}
                        className="input text-sm py-2"
                    >
                        <option value="">All locations</option>
                        {storageTypes.map(st => (
                            <option key={st.id} value={st.name}>{st.name}</option>
                        ))}
                    </select>

                    <select
                        value={sortKey}
                        onChange={e => setSortKey(e.target.value)}
                        className="input text-sm py-2"
                    >
                        <option value="expiry">Sort: Expiry</option>
                        <option value="name">Sort: Name</option>
                        <option value="location">Sort: Location</option>
                        <option value="qty">Sort: Qty</option>
                    </select>
                </div>

                <button
                    onClick={() => setShowLocationMgr(v => !v)}
                    className={`text-xs px-3 py-2 transition-colors border rounded self-start sm:self-auto
            ${showLocationMgr
                        ? 'bg-kale text-parchment border-kale'
                        : 'border-sage-dark text-ink-muted hover:border-kale hover:text-kale'}`}
                >
                    ⚙ Manage locations
                </button>
            </div>

            {/* Location manager */}
            {showLocationMgr && (
                <div className="mb-4 sm:mb-5 bg-parchment-dark border border-sage rounded p-3 sm:p-4">
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

            {error && <p className="text-sm text-red-600 mb-4">{error}</p>}

            {loading ? (
                <div className="card py-16 text-center">
                    <p className="font-mono text-sm text-ink-muted animate-pulse">Loading inventory…</p>
                </div>
            ) : filtered.length === 0 ? (
                <div className="card py-16 text-center">
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
                <>
                    {/* Mobile: card list */}
                    <div className="sm:hidden space-y-2.5">
                        {filtered.map(entry => (
                            <StoredFoodCard
                                key={entry.id}
                                entry={entry}
                                onConsume={e => { setSelected(e); setModal('consume'); }}
                                onEditFood={e => { setSelected(e); setModal('editFood'); }}
                            />
                        ))}
                        <p className="font-mono text-xs text-ink-muted text-center pt-1">
                            {filtered.length} of {entries.length} entries
                        </p>
                    </div>

                    {/* Desktop: table */}
                    <div className="hidden sm:block card p-0 overflow-hidden">
                        <table className="w-full text-sm">
                            <thead className="bg-parchment-dark border-b border-sage">
                            <tr>
                                {COL_HEADERS.map(({ key, label }) => (
                                    <th
                                        key={label}
                                        onClick={key ? () => setSortKey(key) : undefined}
                                        className={`text-left py-3 px-4 font-mono text-xs text-ink-muted uppercase tracking-wider select-none
                        ${key ? 'cursor-pointer hover:text-kale transition-colors' : ''}
                        ${sortKey === key ? 'text-kale' : ''}`}
                                    >
                                        {label}
                                        {sortKey === key && <span className="ml-1 text-kale">↑</span>}
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
                        <div className="border-t border-sage/60 px-4 py-2 bg-parchment">
                            <p className="font-mono text-xs text-ink-muted">
                                Showing {filtered.length} of {entries.length} entries
                            </p>
                        </div>
                    </div>
                </>
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
