import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import DirectorLayout from './components/DirectorLayout';
import ProtectedRoute from './components/ProtectedRoute';
import RoleDashboardPlaceholder from './components/RoleDashboardPlaceholder';
import { AuthProvider, useAuth } from './context/AuthContext';
import { getHomeRouteByRole } from './context/roleRoutes';
import LoginContainer from './features/auth/LoginContainer';
import UsuariosAdminContainer from './features/usuarios/UsuariosAdminContainer';

function RootRedirect() {
  const { isAuthenticated, rol } = useAuth();
  return <Navigate to={isAuthenticated ? getHomeRouteByRole(rol) : '/login'} replace />;
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginContainer />} />
          <Route
            path="/director"
            element={
              <ProtectedRoute allowedRoles={['DIRECTOR']}>
                <DirectorLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Navigate to="/director/usuarios" replace />} />
            <Route path="usuarios" element={<UsuariosAdminContainer />} />
            <Route
              path="grupos"
              element={
                <section className="bg-white border border-slate-200 rounded-lg shadow-sm p-6">
                  <h1 className="text-2xl font-bold text-slate-900">Gestión de Grupos</h1>
                  <p className="mt-1 text-sm text-slate-600">Módulo en construcción.</p>
                </section>
              }
            />
          </Route>
          <Route
            path="/docente/*"
            element={
              <ProtectedRoute allowedRoles={['DOCENTE']}>
                <RoleDashboardPlaceholder
                  title="Panel Docente"
                  subtitle="Consulta de grupos asignados y pase de lista del día."
                />
              </ProtectedRoute>
            }
          />
          <Route
            path="/alumno/*"
            element={
              <ProtectedRoute allowedRoles={['ALUMNO']}>
                <RoleDashboardPlaceholder
                  title="Portal del Alumno"
                  subtitle="Historial de asistencias y solicitud de justificantes."
                />
              </ProtectedRoute>
            }
          />
          <Route path="/" element={<RootRedirect />} />
          <Route path="*" element={<RootRedirect />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
