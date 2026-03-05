import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const NAV = [
  { to: '/',          label: 'Dashboard',  icon: '⌂' },
  { to: '/foods',     label: 'Catalogue',  icon: '◈' },
  { to: '/inventory', label: 'Inventory',  icon: '▤' },
];

const ADMIN_NAV = [
  { to: '/admin/users', label: 'Users', icon: '◎' },
];

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login');
  }

  return (
    <div className="min-h-screen flex bg-cream">
      {/* ── Sidebar ──────────────────────────────────────────────── */}
      <aside className="w-60 flex-shrink-0 bg-forest-dark flex flex-col">
        {/* Logo */}
        <div className="px-6 pt-8 pb-6 border-b border-forest">
          <h1 className="font-display text-cream text-xl font-bold leading-tight">
            Pantry
          </h1>
          <p className="font-mono text-xs text-green-400 mt-0.5 opacity-70">
            {user?.username}
          </p>
        </div>

        {/* Nav links */}
        <nav className="flex-1 px-3 py-5 space-y-1">
          {NAV.map(({ to, label, icon }) => (
            <NavLink
              key={to}
              to={to}
              end={to === '/'}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-sm text-sm font-body transition-colors duration-100
                 ${isActive
                   ? 'bg-forest text-cream font-medium'
                   : 'text-green-200 hover:bg-forest hover:text-cream'}`
              }
            >
              <span className="text-base w-5 text-center">{icon}</span>
              {label}
            </NavLink>
          ))}

          {user?.role === 'ADMIN' && (
            <>
              <div className="pt-4 pb-1 px-3">
                <span className="font-mono text-xs text-green-600 uppercase tracking-widest">Admin</span>
              </div>
              {ADMIN_NAV.map(({ to, label, icon }) => (
                <NavLink
                  key={to}
                  to={to}
                  className={({ isActive }) =>
                    `flex items-center gap-3 px-3 py-2.5 rounded-sm text-sm font-body transition-colors
                     ${isActive
                       ? 'bg-forest text-cream font-medium'
                       : 'text-green-200 hover:bg-forest hover:text-cream'}`
                  }
                >
                  <span className="text-base w-5 text-center">{icon}</span>
                  {label}
                </NavLink>
              ))}
            </>
          )}
        </nav>

        {/* Footer */}
        <div className="px-4 pb-6">
          <button
            onClick={handleLogout}
            className="w-full flex items-center gap-2 px-3 py-2 text-sm text-green-300
                       hover:text-red-300 font-body transition-colors rounded-sm"
          >
            <span>→</span> Logout
          </button>
        </div>
      </aside>

      {/* ── Main ──────────────────────────────────────────────────── */}
      <main className="flex-1 overflow-auto">
        <Outlet />
      </main>
    </div>
  );
}
