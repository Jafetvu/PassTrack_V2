import { BookOpen, FileCheck2, LogOut, ShieldCheck, Users } from 'lucide-react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function DirectorLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  const navClass = ({ isActive }) =>
    `inline-flex items-center gap-2 rounded-md px-3 py-1.5 text-sm font-medium transition-colors ${
      isActive
        ? 'bg-sky-600 text-white'
        : 'text-slate-300 hover:bg-slate-800 hover:text-white'
    }`;

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      <header className="bg-slate-900 text-white border-b border-slate-800">
        <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between gap-4">
          <div className="flex items-center gap-6">
            <div className="flex items-center gap-2.5">
              <ShieldCheck className="w-5 h-5 text-sky-400" aria-hidden="true" />
              <span className="font-semibold tracking-tight">PassTrack</span>
              <span className="hidden sm:inline-flex items-center rounded-md bg-sky-500/15 px-2 py-0.5 text-xs font-medium text-sky-300 border border-sky-400/20">
                DIRECTOR
              </span>
            </div>

            <nav className="flex items-center gap-1.5" aria-label="Navegación de Dirección">
              <NavLink to="/director/usuarios" className={navClass}>
                <Users className="w-4 h-4" aria-hidden="true" />
                Usuarios
              </NavLink>
              <NavLink to="/director/grupos" className={navClass}>
                <BookOpen className="w-4 h-4" aria-hidden="true" />
                Grupos
              </NavLink>
              <NavLink to="/director/justificantes" className={navClass}>
                <FileCheck2 className="w-4 h-4" aria-hidden="true" />
                Justificantes
              </NavLink>
            </nav>
          </div>

          <div className="flex items-center gap-4">
            <span className="text-sm text-slate-300 hidden md:inline">
              {user?.nombre || user?.email}
            </span>
            <button
              type="button"
              onClick={handleLogout}
              className="inline-flex items-center gap-1.5 rounded-md border border-slate-700 bg-slate-800 px-3 py-1.5 text-xs font-medium text-slate-200 hover:bg-slate-700 transition-colors"
            >
              <LogOut className="w-3.5 h-3.5" aria-hidden="true" />
              Cerrar sesión
            </button>
          </div>
        </div>
      </header>

      <main className="flex-1 max-w-6xl w-full mx-auto px-6 py-8">
        <Outlet />
      </main>
    </div>
  );
}
