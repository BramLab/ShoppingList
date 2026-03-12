import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { foodApi, storedFoodApi } from '../api/api';

function StatCard({ label, value, sub, color = 'kale' }) {
    const borderMap = { kale: 'border-l-kale', carrot: 'border-l-carrot', leaf: 'border-l-leaf' };
    const textMap   = { kale: 'text-kale', carrot: 'text-carrot', leaf: 'text-leaf' };
    return (
        <div className={`card border-l-4 ${borderMap[color] ?? 'border-l-kale'}`}>
            <p className="label text-[10px] sm:text-xs">{label}</p>
            <p className={`font-display text-3xl sm:text-4xl font-bold ${textMap[color] ?? 'text-kale'} mt-1`}>{value}</p>
            {sub && <p className="font-body text-xs text-ink-muted mt-1 hidden sm:block">{sub}</p>}
        </div>
    );
}

export default function Dashboard() {
    const { user } = useAuth();
    const [foods, setFoods]   = useState([]);
    const [stored, setStored] = useState([]);

    useEffect(() => {
        foodApi.getAll().then(setFoods).catch(console.error);
        if (user?.homeId) {
            storedFoodApi.getAll(user.homeId).then(setStored).catch(console.error);
        }
    }, [user]);

    const today        = new Date();
    const expiringSoon = foods.filter(f => {
        if (!f.effectiveUseBy) return false;
        const days = Math.ceil((new Date(f.effectiveUseBy) - today) / 86_400_000);
        return days >= 0 && days <= 7;
    });
    const empty = foods.filter(f => f.empty);

    return (
        <div className="p-4 sm:p-8 max-w-5xl">

            {/* Header */}
            <div className="mb-6 sm:mb-8">
                <h2 className="font-display text-2xl sm:text-3xl font-bold text-ink">
                    Good to see you, {user?.username}.
                </h2>
                <p className="font-body text-ink-muted mt-1 text-sm hidden sm:block">Here's a snapshot of your pantry.</p>
            </div>

            {/* Stats */}
            <div className="grid grid-cols-2 gap-3 sm:gap-4 mb-8 sm:mb-10">
                <StatCard label="Total items"       value={foods.length}        sub="in catalogue"   color="kale"   />
                <StatCard label="In inventory"      value={stored.length}       sub="stored entries" color="leaf"   />
                <StatCard label="Expiring ≤ 7d"     value={expiringSoon.length} sub="check soon"     color="carrot" />
                <StatCard label="Empty packages"    value={empty.length}        sub="restock soon"   color="carrot" />
            </div>

            {/* Expiring soon list */}
            {expiringSoon.length > 0 && (
                <div className="mb-6 sm:mb-8">
                    <div className="flex items-center justify-between mb-3">
                        <h3 className="font-display text-lg font-semibold text-ink">Expiring soon</h3>
                        <Link to="/stored-foods" className="font-mono text-xs text-kale hover:underline">View all →</Link>
                    </div>
                    <div className="space-y-2">
                        {expiringSoon.slice(0, 5).map(f => {
                            const days = Math.ceil((new Date(f.effectiveUseBy) - today) / 86_400_000);
                            return (
                                <div key={f.id} className="card flex items-center justify-between py-3">
                                    <div className="min-w-0 mr-3">
                                        <p className="font-body font-medium text-ink truncate">{f.name}</p>
                                        {f.remarks && <p className="font-body text-xs text-ink-muted truncate">{f.remarks}</p>}
                                    </div>
                                    <span className={`badge font-mono flex-shrink-0 ${days <= 2 ? 'bg-red-100 text-red-700' : 'bg-amber-100 text-amber-700'}`}>
                    {days === 0 ? 'Today' : `${days}d`}
                  </span>
                                </div>
                            );
                        })}
                    </div>
                </div>
            )}

            {/* Quick links */}
            <div className="grid grid-cols-2 gap-3 sm:gap-4">
                <Link to="/stored-foods" className="card hover:border-kale transition-colors group">
                    <p className="font-mono text-2xl mb-2 text-leaf">◈</p>
                    <p className="font-body font-semibold text-ink group-hover:text-kale text-sm sm:text-base">Stored Foods</p>
                    <p className="font-body text-xs text-ink-muted mt-1 hidden sm:block">Manage your current inventory</p>
                </Link>
                <Link to="/deleted-foods" className="card hover:border-kale transition-colors group">
                    <p className="font-mono text-2xl mb-2 text-leaf">↩</p>
                    <p className="font-body font-semibold text-ink group-hover:text-kale text-sm sm:text-base">Deleted Foods</p>
                    <p className="font-body text-xs text-ink-muted mt-1 hidden sm:block">Restore soft-deleted items</p>
                </Link>
            </div>
        </div>
    );
}
