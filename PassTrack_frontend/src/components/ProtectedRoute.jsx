import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getHomeRouteByRole } from '../context/roleRoutes';

export default function ProtectedRoute({ allowedRoles, children }) {
  const { isAuthenticated, rol } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(rol)) {
    return <Navigate to={getHomeRouteByRole(rol)} replace />;
  }

  return children;
}
