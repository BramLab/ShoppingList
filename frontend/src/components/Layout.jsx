import { useState, useRef, useEffect } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Carrot from './Carrot';

const NAV = [
    { to: '/',              label: 'Dashboard',    icon: '⌂' },
    { to: '/stored-foods',  label: 'Stored',       icon: '◈' },
    { to: '/deleted-foods', label: 'Deleted',      icon: '↩' },
];
const ADMIN_NAV = [
    { to: '/admin/users', label: 'Users', icon: '◉' },
];

export default function Layout() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const [open, setOpen] = useState(false);
    const dropdownRef = useRef(null);

    useEffect(() => {
        function outside(e) {
            if (dropdownRef.current && !dropdownRef.current.contains(e.target)) setOpen(false);
        }
        document.addEventListener('mousedown', outside);
        return () => document.removeEventListener('mousedown', outside);
    }, []);

    function handleLogout() { logout(); navigate('/login'); }

    const allNav = user?.role === 'ADMIN' ? [...NAV, ...ADMIN_NAV] : NAV;

    return (
        <div className="min-h-screen flex flex-col bg-parchment">

            {/* ── Top header ───────────────────────────────────────────── */}
            <header className="bg-kale-dark border-b-2 border-carrot flex-shrink-0 sticky top-0 z-40">
                <div className="max-w-5xl mx-auto w-full px-4 sm:px-8 py-2.5 flex items-center justify-between">

                    {/* Left: carrot + wordmark */}
                    <div className="flex items-center gap-2.5">
                        <Carrot size={26} />
                        <h1 className="font-display text-parchment text-base sm:text-lg font-bold tracking-tight">
                            Pantry
                        </h1>
                        <span className="font-mono text-xs text-sprout/60 ml-1 hidden sm:inline">
              {user?.username}
            </span>
                    </div>

                    {/* Desktop nav: inline links */}
                    <nav className="hidden sm:flex items-center gap-1">
                        {allNav.map(({ to, label }) => (
                            <NavLink
                                key={to}
                                to={to}
                                end={to === '/'}
                                className={({ isActive }) =>
                                    `px-3 py-1.5 rounded font-body text-sm transition-colors
                   ${isActive
                                        ? 'bg-kale text-parchment font-medium'
                                        : 'text-sprout hover:bg-kale/60 hover:text-parchment'}`
                                }
                            >
                                {label}
                            </NavLink>
                        ))}
                    </nav>

                    {/* Right: username (mobile) + logout */}
                    <div className="flex items-center gap-3">
                        <span className="font-mono text-xs text-sprout/60 sm:hidden">{user?.username}</span>
                        <button
                            onClick={handleLogout}
                            className="font-body text-sm text-sprout hover:text-red-300 transition-colors
                         flex items-center gap-1"
                            aria-label="Logout"
                        >
                            <span className="hidden sm:inline">Logout</span>
                            <span className="text-base leading-none">→</span>
                        </button>
                    </div>
                </div>
            </header>

            {/* ── Main ─────────────────────────────────────────────────── */}
            <main className="flex-1 overflow-auto pb-20 sm:pb-0">
                <div className="max-w-5xl mx-auto w-full">
                    <Outlet />
                </div>
            </main>

            {/* ── Mobile bottom tab bar ────────────────────────────────── */}
            <nav className="sm:hidden fixed bottom-0 inset-x-0 z-40 bg-kale-dark border-t-2 border-carrot
                      flex items-stretch">
                {allNav.map(({ to, label, icon }) => (
                    <NavLink
                        key={to}
                        to={to}
                        end={to === '/'}
                        className={({ isActive }) =>
                            `flex-1 flex flex-col items-center justify-center gap-0.5 py-2.5 transition-colors
               ${isActive
                                ? 'text-parchment bg-kale'
                                : 'text-sprout/70 hover:text-parchment'}`
                        }
                    >
                        <span className="text-lg leading-none">{icon}</span>
                        <span className="font-mono text-[10px] tracking-wide">{label}</span>
                    </NavLink>
                ))}
                {/* Logout tab on mobile */}
                <button
                    onClick={handleLogout}
                    className="flex-1 flex flex-col items-center justify-center gap-0.5 py-2.5
                     text-sprout/70 hover:text-red-300 transition-colors"
                >
                    <span className="text-lg leading-none">→</span>
                    <span className="font-mono text-[10px] tracking-wide">Out</span>
                </button>
            </nav>
        </div>
    );
}
