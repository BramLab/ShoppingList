import { useState, useRef, useEffect } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const NAV = [
  { to: '/',                label: 'Dashboard'    },
  { to: '/foods',           label: 'Catalogue'    },
  { to: '/inventory',       label: 'Inventory'    },
  { to: '/stored-foods',    label: 'Stored Foods' },
  { to: '/deleted-foods',   label: 'Deleted Foods'},
];

const ADMIN_NAV = [
  { to: '/admin/users', label: 'Users' },
];

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const dropdownRef = useRef(null);

  // Close dropdown when clicking outside
  useEffect(() => {
    function handleClickOutside(e) {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  function handleLogout() {
    logout();
    navigate('/login');
  }

  const allNav = user?.role === 'ADMIN' ? [...NAV, ...ADMIN_NAV] : NAV;

  return (
      <div className="min-h-screen flex flex-col bg-cream">

        {/* ── Top nav ───────────────────────────────────────────────── */}
        <header className="bg-forest-dark border-b border-forest flex-shrink-0">
          <div className="max-w-5xl mx-auto w-full px-8 py-3 flex items-center justify-between">

            {/* Left: logo */}
            <div className="flex items-center gap-4">
              <h1 className="font-display text-cream text-lg font-bold leading-tight">
                Pantry
              </h1>
              <p className="font-mono text-xs text-green-400 opacity-70">
                {user?.username}
              </p>
            </div>

            {/* Centre: nav dropdown */}
            <div className="relative" ref={dropdownRef}>
              <button
                  onClick={() => setOpen(v => !v)}
                  className="flex items-center gap-2 font-body text-sm text-green-200
                         hover:text-cream transition-colors px-3 py-1.5 rounded-sm
                         border border-forest hover:border-green-400"
              >
                <span>Navigation</span>
                <span className={`font-mono text-xs transition-transform duration-150 ${open ? 'rotate-180' : ''}`}>
                ▾
              </span>
              </button>

              {open && (
                  <div className="absolute left-1/2 -translate-x-1/2 mt-2 w-48 bg-forest-dark
                              border border-forest rounded-sm shadow-xl z-50 overflow-hidden">
                    {allNav.map(({ to, label }) => (
                        <NavLink
                            key={to}
                            to={to}
                            end={to === '/'}
                            onClick={() => setOpen(false)}
                            className={({ isActive }) =>
                                `block px-4 py-2.5 font-body text-sm transition-colors
                       ${isActive
                                    ? 'bg-forest text-cream font-medium'
                                    : 'text-green-200 hover:bg-forest hover:text-cream'}`
                            }
                        >
                          {label}
                        </NavLink>
                    ))}

                    {user?.role === 'ADMIN' && (
                        <div className="border-t border-forest mt-1 pt-1">
                          <p className="px-4 py-1 font-mono text-xs text-green-600 uppercase tracking-widest">
                            Admin
                          </p>
                          {ADMIN_NAV.map(({ to, label }) => (
                              <NavLink
                                  key={to}
                                  to={to}
                                  onClick={() => setOpen(false)}
                                  className={({ isActive }) =>
                                      `block px-4 py-2.5 font-body text-sm transition-colors
                           ${isActive
                                          ? 'bg-forest text-cream font-medium'
                                          : 'text-green-200 hover:bg-forest hover:text-cream'}`
                                  }
                              >
                                {label}
                              </NavLink>
                          ))}
                        </div>
                    )}
                  </div>
              )}
            </div>

            {/* Right: logout */}
            <button
                onClick={handleLogout}
                className="flex items-center gap-1.5 font-body text-sm text-green-300
                       hover:text-red-300 transition-colors"
            >
              <span>→</span> Logout
            </button>
          </div>
        </header>

        {/* ── Main ──────────────────────────────────────────────────── */}
        <main className="flex-1 overflow-auto">
          <div className="max-w-5xl mx-auto w-full">
            <Outlet />
          </div>
        </main>
      </div>
  );
}