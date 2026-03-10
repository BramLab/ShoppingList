import { useState, useEffect } from 'react';
import { foodApi } from '../api/api';

// ─── Food row ──────────────────────────────────────────────────────────────

function DeletedFoodRow({ food, onRestore }) {
    const [restoring, setRestoring] = useState(false);

    async function handleRestore() {
        if (!confirm(`Restore "${food.name}"?`)) return;
        setRestoring(true);
        try {
            await onRestore(food.id);
        } finally {
            setRestoring(false);
        }
    }

    const lastModified = food.updatedAt
        ? (() => {
            const d = new Date(food.updatedAt);
            const yyyy = d.getFullYear();
            const MM   = String(d.getMonth() + 1).padStart(2, '0');
            const dd   = String(d.getDate()).padStart(2, '0');
            const HH   = String(d.getHours()).padStart(2, '0');
            const mm   = String(d.getMinutes()).padStart(2, '0');
            return `${yyyy}-${MM}-${dd} ${HH}:${mm}`;
        })()
        : '—';

    return (
        <tr className="border-b border-gray-100 hover:bg-cream transition-colors opacity-70 hover:opacity-100">
            <td className="py-3 px-4">
                <p className="font-body font-medium text-ink line-through decoration-gray-400">{food.name}</p>
                {food.remarks && <p className="font-body text-xs text-ink-muted">{food.remarks}</p>}
            </td>
            <td className="py-3 px-4 font-mono text-sm text-ink-muted">{food.bestBeforeEnd ?? '—'}</td>
            <td className="py-3 px-4 font-mono text-sm text-ink-muted">
                {food.original_ml_g ? `${food.original_ml_g} ml/g` : '—'}
            </td>
            <td className="py-3 px-4 font-mono text-sm text-ink-muted">
                {food.quantity > 0
                    ? <span className="inline-flex items-center gap-1">
                        <span className="bg-gray-100 text-ink-muted rounded px-1.5 py-0.5 text-xs font-mono">×{food.quantity}</span>
                      </span>
                    : <span className="text-gray-300">—</span>
                }
            </td>
            <td className="py-3 px-4 font-mono text-sm text-ink-muted">{lastModified}</td>
            <td className="py-3 px-4">
                <button
                    onClick={handleRestore}
                    disabled={restoring}
                    className="btn-secondary text-xs px-3 py-1.5"
                >
                    {restoring ? 'Restoring…' : '↩ Restore'}
                </button>
            </td>
        </tr>
    );
}

// ─── Main page ─────────────────────────────────────────────────────────────

export default function DeletedFoods() {
    const [foods, setFoods]     = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError]     = useState('');
    const [search, setSearch]   = useState('');

    async function load() {
        try {
            setFoods(await foodApi.getDeleted());
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => { load(); }, []);

    async function handleRestore(id) {
        try {
            await foodApi.restore(id);
            setFoods(prev => prev.filter(f => f.id !== id));
        } catch (err) {
            setError(err.message);
        }
    }

    const filtered = foods.filter(f =>
        f.name.toLowerCase().includes(search.toLowerCase()) ||
        (f.remarks ?? '').toLowerCase().includes(search.toLowerCase())
    );

    return (
        <div className="p-8">
            {/* Header */}
            <div className="mb-6">
                <h2 className="font-display text-3xl font-bold text-ink">Deleted Foods</h2>
                <p className="font-body text-sm text-ink-muted mt-1">
                    {foods.length} soft-deleted item{foods.length !== 1 ? 's' : ''} — restore to bring them back.
                </p>
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

            {loading ? (
                <p className="font-mono text-sm text-ink-muted animate-pulse">Loading…</p>
            ) : filtered.length === 0 ? (
                <div className="card py-16 text-center">
                    <p className="font-display text-2xl text-ink-muted mb-2">
                        {foods.length === 0 ? 'No deleted foods' : 'No results'}
                    </p>
                    <p className="font-body text-sm text-ink-muted">
                        {foods.length === 0
                            ? 'Nothing has been soft-deleted yet.'
                            : 'Try a different search term.'}
                    </p>
                </div>
            ) : (
                <div className="card p-0 overflow-hidden">
                    <table className="w-full text-sm">
                        <thead className="bg-cream-dark border-b border-gray-200">
                        <tr>
                            {['Name', 'Best before', 'Amount', 'Qty', 'Last modified', 'Actions'].map(h => (
                                <th key={h} className="text-left py-3 px-4 font-mono text-xs text-ink-muted uppercase tracking-wider">
                                    {h}
                                </th>
                            ))}
                        </tr>
                        </thead>
                        <tbody>
                        {filtered.map(f => (
                            <DeletedFoodRow key={f.id} food={f} onRestore={handleRestore} />
                        ))}
                        </tbody>
                    </table>
                    <div className="border-t border-gray-100 px-4 py-2">
                        <p className="font-mono text-xs text-ink-muted">
                            Showing {filtered.length} of {foods.length} deleted items
                        </p>
                    </div>
                </div>
            )}
        </div>
    );
}