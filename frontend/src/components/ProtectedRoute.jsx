import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function ProtectedRoute({ children, adminOnly = false }) {
    const { user, loading } = useAuth();

    if (loading) return null; // or a spinner

    if (!user) return <Navigate to="/login" replace />;

    if (adminOnly && user.role !== 'ADMIN') return <Navigate to="/" replace />;

    return children;
}

// in App.jsx, ProtectedRoute wraps <Layout /> as a parent route,
// but adminOnly routes are also wrapped again inside.
// This means admin route protection works, but the outer ProtectedRoute renders children directly
// — which is fine as long as children is <Layout /> (which renders <Outlet /> for nested routes).
// Just make sure ProtectedRoute returns children and not <Outlet /> itself.