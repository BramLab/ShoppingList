import { useState, useEffect } from 'react';
import { adminApi } from '../api/api';

export default function AdminUsers() {
  const [users, setUsers]     = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState('');

  async function load() {
    try {
      setUsers(await adminApi.getUsers());
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, []);

  async function handleRoleToggle(user) {
    const newRole = user.role === 'ADMIN' ? 'NORMAL' : 'ADMIN';
    if (!confirm(`Change ${user.username}'s role to ${newRole}?`)) return;
    await adminApi.changeRole(user.id, { id: user.id, role: newRole });
    await load();
  }

  async function handleDelete(user) {
    if (!confirm(`Delete user "${user.username}"? This cannot be undone.`)) return;
    await adminApi.deleteUser(user.id);
    setUsers(prev => prev.filter(u => u.id !== user.id));
  }

  return (
    <div className="p-8 max-w-4xl">
      <div className="mb-6">
        <h2 className="font-display text-3xl font-bold text-ink">User Management</h2>
        <p className="font-body text-sm text-ink-muted mt-1">{users.length} registered users</p>
      </div>

      {error && <p className="text-sm text-red-600 mb-4">{error}</p>}

      {loading ? (
        <p className="font-mono text-sm text-ink-muted animate-pulse">Loading…</p>
      ) : (
        <div className="card p-0 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-cream-dark border-b border-gray-200">
              <tr>
                {['ID', 'Username', 'Email', 'Role', 'Home', 'Actions'].map(h => (
                  <th key={h} className="text-left py-3 px-4 font-mono text-xs text-ink-muted uppercase tracking-wider">
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {users.map(u => (
                <tr key={u.id} className="border-b border-gray-100 hover:bg-cream transition-colors">
                  <td className="py-3 px-4 font-mono text-xs text-ink-muted">{u.id}</td>
                  <td className="py-3 px-4 font-body font-medium text-ink">{u.username}</td>
                  <td className="py-3 px-4 font-body text-ink-muted">{u.email}</td>
                  <td className="py-3 px-4">
                    <span className={`badge font-mono ${u.role === 'ADMIN'
                      ? 'bg-forest/10 text-forest'
                      : 'bg-gray-100 text-ink-muted'}`}>
                      {u.role}
                    </span>
                  </td>
                  <td className="py-3 px-4 font-mono text-xs text-ink-muted">{u.homeId}</td>
                  <td className="py-3 px-4">
                    <div className="flex gap-2">
                      <button onClick={() => handleRoleToggle(u)}
                        className="btn-secondary text-xs px-3 py-1.5">
                        Toggle role
                      </button>
                      <button onClick={() => handleDelete(u)}
                        className="btn-danger text-xs px-3 py-1.5">
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
