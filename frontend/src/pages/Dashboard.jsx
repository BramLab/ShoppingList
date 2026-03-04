import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { foodApi, storedFoodApi } from '../api.js';

function StatCard({ label, value, sub, color = 'forest' }) {
  const border = color === 'terra' ? 'border-terra' : 'border-forest';
  const text   = color === 'terra' ? 'text-terra'   : 'text-forest';
  return (
    <div className={`card border-l-4 ${border}`}>
      <p className="label">{label}</p>
      <p className={`font-display text-4xl font-bold ${text} mt-1`}>{value}</p>
      {sub && <p className="font-body text-xs text-ink-muted mt-1">{sub}</p>}
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

  const today       = new Date();
  const expiringSoon = foods.filter(f => {
    if (!f.effectiveUseBy) return false;
    const days = Math.ceil((new Date(f.effectiveUseBy) - today) / 86_400_000);
    return days >= 0 && days <= 7;
  });
  const empty = foods.filter(f => f.empty);

  return (
    <div className="p-8 max-w-5xl">
      {/* Header */}
      <div className="mb-8">
        <h2 className="font-display text-3xl font-bold text-ink">
          Good to see you, {user?.userName}.
        </h2>
        <p className="font-body text-ink-muted mt-1">Here's a snapshot of your pantry.</p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-10">
        <StatCard label="Total items"       value={foods.length}         sub="in catalogue" />
        <StatCard label="In inventory"      value={stored.length}        sub="stored entries" />
        <StatCard label="Expiring ≤ 7 days" value={expiringSoon.length}  sub="check soon" color="terra" />
        <StatCard label="Empty packages"    value={empty.length}         sub="consider restocking" color="terra" />
      </div>

      {/* Expiring soon list */}
      {expiringSoon.length > 0 && (
        <div className="mb-8">
          <div className="flex items-center justify-between mb-3">
            <h3 className="font-display text-lg font-semibold text-ink">Expiring soon</h3>
            <Link to="/foods" className="font-mono text-xs text-forest hover:underline">View all →</Link>
          </div>
          <div className="space-y-2">
            {expiringSoon.slice(0, 5).map(f => {
              const days = Math.ceil((new Date(f.effectiveUseBy) - today) / 86_400_000);
              return (
                <div key={f.id} className="card flex items-center justify-between py-3">
                  <div>
                    <p className="font-body font-medium text-ink">{f.name}</p>
                    {f.remarks && <p className="font-body text-xs text-ink-muted">{f.remarks}</p>}
                  </div>
                  <span className={`badge font-mono ${days <= 2 ? 'bg-red-100 text-red-700' : 'bg-amber-100 text-amber-700'}`}>
                    {days === 0 ? 'Today' : `${days}d`}
                  </span>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Quick links */}
      <div className="grid grid-cols-2 gap-4">
        <Link to="/foods" className="card hover:border-forest transition-colors group">
          <p className="font-mono text-2xl mb-2">◈</p>
          <p className="font-body font-semibold text-ink group-hover:text-forest">Food catalogue</p>
          <p className="font-body text-xs text-ink-muted mt-1">Add, open and consume food items</p>
        </Link>
        <Link to="/inventory" className="card hover:border-forest transition-colors group">
          <p className="font-mono text-2xl mb-2">▤</p>
          <p className="font-body font-semibold text-ink group-hover:text-forest">Inventory</p>
          <p className="font-body text-xs text-ink-muted mt-1">Manage stored food across locations</p>
        </Link>
      </div>
    </div>
  );
}
