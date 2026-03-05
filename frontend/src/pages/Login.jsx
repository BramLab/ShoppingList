import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Login() {
  const { login } = useAuth();
  const navigate  = useNavigate();

  const [form, setForm]     = useState({ username: '', password: '' });
  const [error, setError]   = useState('');
  const [loading, setLoading] = useState(false);

  function set(field) {
    return (e) => setForm(f => ({ ...f, [field]: e.target.value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(form.username, form.password);
      navigate('/');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen bg-cream flex">
      {/* Left decorative panel */}
      <div className="hidden lg:flex w-1/2 bg-forest-dark flex-col justify-between p-12">
        <div>
          <h1 className="font-display text-cream text-4xl font-bold leading-snug">
            Pantry Manager
          </h1>
          <p className="font-body text-green-300 mt-4 text-base leading-relaxed max-w-xs">
            Track what you have, know what you need. Your household food inventory — organised.
          </p>
        </div>
        <p className="font-mono text-xs text-green-700">ShoppingList v0.0.1</p>
      </div>

      {/* Right form panel */}
      <div className="flex-1 flex items-center justify-center px-8">
        <div className="w-full max-w-sm">
          <h2 className="font-display text-3xl font-bold text-ink mb-1">Welcome back</h2>
          <p className="font-body text-sm text-ink-muted mb-8">Sign in to your account</p>

          {error && (
            <div className="mb-5 px-4 py-3 bg-red-50 border border-red-200 rounded-sm">
              <p className="text-sm font-body text-red-700">{error}</p>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="label">Username</label>
              <input
                className="input"
                type="text"
                placeholder="user01"
                value={form.username}
                onChange={set('username')}
                required
                autoFocus
              />
            </div>

            <div>
              <label className="label">Password</label>
              <input
                className="input"
                type="password"
                placeholder="••••••••"
                value={form.password}
                onChange={set('password')}
                required
              />
            </div>

            <button className="btn-primary w-full mt-2" type="submit" disabled={loading}>
              {loading ? 'Signing in…' : 'Sign in'}
            </button>
          </form>

          <p className="font-body text-sm text-ink-muted mt-6 text-center">
            No account?{' '}
            <Link to="/register" className="text-forest font-medium hover:underline">
              Register here
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
