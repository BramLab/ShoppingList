import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Register() {
  const { register } = useAuth();
  const navigate     = useNavigate();

  const [form, setForm] = useState({
    userName: '', email: '', password: '', homeName: '', role: 'NORMAL',
  });
  const [error, setError]     = useState('');
  const [loading, setLoading] = useState(false);

  function set(field) {
    return (e) => setForm(f => ({ ...f, [field]: e.target.value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    if (form.password.length < 8) {
      setError('Password must be at least 8 characters.');
      return;
    }
    setLoading(true);
    try {
      await register({ ...form, id: 0 });
      navigate('/login');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen bg-cream flex items-center justify-center px-8 py-12">
      <div className="w-full max-w-md">
        <Link to="/login" className="font-mono text-xs text-ink-muted hover:text-forest mb-6 inline-block">
          ← Back to login
        </Link>

        <h2 className="font-display text-3xl font-bold text-ink mb-1">Create account</h2>
        <p className="font-body text-sm text-ink-muted mb-8">
          A new home will be created for you automatically.
        </p>

        {error && (
          <div className="mb-5 px-4 py-3 bg-red-50 border border-red-200 rounded-sm">
            <p className="text-sm font-body text-red-700">{error}</p>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Username</label>
              <input className="input" type="text" placeholder="janedoe"
                value={form.userName} onChange={set('userName')} required />
            </div>
            <div>
              <label className="label">Role</label>
              <select className="input" value={form.role} onChange={set('role')}>
                <option value="NORMAL">Normal</option>
                <option value="ADMIN">Admin</option>
              </select>
            </div>
          </div>

          <div>
            <label className="label">Email</label>
            <input className="input" type="email" placeholder="jane@example.com"
              value={form.email} onChange={set('email')} required />
          </div>

          <div>
            <label className="label">Password</label>
            <input className="input" type="password" placeholder="min. 8 characters"
              value={form.password} onChange={set('password')} required />
          </div>

          <div>
            <label className="label">Home name</label>
            <input className="input" type="text" placeholder="e.g. The Smith Household"
              value={form.homeName} onChange={set('homeName')} required />
          </div>

          <button className="btn-primary w-full mt-2" type="submit" disabled={loading}>
            {loading ? 'Creating account…' : 'Create account'}
          </button>
        </form>
      </div>
    </div>
  );
}
