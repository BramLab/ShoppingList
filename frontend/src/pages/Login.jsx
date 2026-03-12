import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Carrot from '../components/Carrot';

export default function Login() {
    const { login } = useAuth();
    const navigate  = useNavigate();

    const [form, setForm]       = useState({ username: '', password: '' });
    const [error, setError]     = useState('');
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
        <div className="min-h-screen bg-parchment flex">

            {/* Left decorative panel */}
            <div className="hidden lg:flex w-1/2 bg-kale-dark flex-col justify-between p-12 relative overflow-hidden">
                {/* Subtle background texture circles */}
                <div className="absolute -right-16 -top-16 w-64 h-64 rounded-full bg-kale opacity-40" />
                <div className="absolute -left-8 bottom-32 w-40 h-40 rounded-full bg-kale opacity-30" />
                <div className="absolute right-12 bottom-16 w-24 h-24 rounded-full bg-leaf opacity-20" />

                <div className="relative z-10">
                    <div className="flex items-center gap-3 mb-8">
                        <Carrot size={32} />
                        <span className="font-display text-parchment text-2xl font-bold tracking-tight">
              Pantry
            </span>
                    </div>
                    <h2 className="font-display text-parchment text-4xl font-bold leading-tight mb-4">
                        Kook meer zelf.
                    </h2>
                    <p className="font-body text-sprout text-base leading-relaxed max-w-xs">
                        Gezonder, goedkoper, lekkerder.
                    </p>
                </div>

                <div className="relative z-10">
                    <div className="flex items-center gap-2 mb-3">
                        {['🥕', '🥦', '🌿', '🍅', '🫛'].map((e, i) => (
                            <span key={i} className="text-lg opacity-60">{e}</span>
                        ))}
                    </div>
                    <p className="font-mono text-xs text-leaf">v0.0.1</p>
                </div>
            </div>

            {/* Right form panel */}
            <div className="flex-1 flex items-center justify-center px-8">
                <div className="w-full max-w-sm">

                    {/* Mobile logo */}
                    <div className="flex items-center gap-2 mb-8 lg:hidden">
                        <Carrot size={24} />
                        <span className="font-display text-kale text-xl font-bold">Pantry</span>
                    </div>

                    <h2 className="font-display text-3xl font-bold text-ink mb-1">Welcome back</h2>
                    <p className="font-body text-sm text-ink-muted mb-8">Sign in to your account</p>

                    {error && (
                        <div className="mb-5 px-4 py-3 bg-red-50 border border-red-200 rounded">
                            <p className="text-sm font-body text-red-700">{error}</p>
                        </div>
                    )}

                    <form onSubmit={handleSubmit} className="space-y-5">
                        <div>
                            <label className="label">Username</label>
                            <input
                                className="input"
                                type="text"
                                placeholder="janedoe"
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
                        <Link to="/register" className="text-kale font-medium hover:underline">
                            Register here
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    );
}
