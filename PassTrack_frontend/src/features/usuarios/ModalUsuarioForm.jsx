import { useState } from 'react';
import { X } from 'lucide-react';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export default function ModalUsuarioForm({ usuarioInicial, submitting, apiError, onSubmit, onClose }) {
  const esEdicion = Boolean(usuarioInicial);

  const [nombre, setNombre] = useState(usuarioInicial?.nombre ?? '');
  const [email, setEmail] = useState(usuarioInicial?.email ?? '');
  const [password, setPassword] = useState('');
  const [rol, setRol] = useState(usuarioInicial?.rol ?? 'DOCENTE');
  const [validationError, setValidationError] = useState('');

  const handleSubmit = (event) => {
    event.preventDefault();
    setValidationError('');

    if (!nombre.trim()) {
      setValidationError('El nombre completo es obligatorio.');
      return;
    }

    if (!esEdicion) {
      if (!EMAIL_PATTERN.test(email.trim())) {
        setValidationError('Ingrese un correo electrónico válido.');
        return;
      }
      if (password.length < 8) {
        setValidationError('La contraseña debe contener al menos 8 caracteres.');
        return;
      }
    }

    onSubmit(
      esEdicion
        ? { nombre: nombre.trim(), rol }
        : { nombre: nombre.trim(), email: email.trim().toLowerCase(), password, rol },
    );
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-usuario-titulo"
    >
      <div className="w-full max-w-md rounded-lg border border-slate-200 bg-white shadow-lg">
        <div className="flex items-center justify-between border-b border-slate-200 px-5 py-4">
          <h2 id="modal-usuario-titulo" className="text-lg font-semibold text-slate-900">
            {esEdicion ? 'Editar usuario' : 'Registrar nuevo usuario'}
          </h2>
          <button
            type="button"
            onClick={onClose}
            className="rounded-md p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
            aria-label="Cerrar modal"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-5 space-y-4" noValidate>
          {(validationError || apiError) && (
            <div
              role="alert"
              className="rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-xs text-rose-700"
            >
              {validationError || apiError}
            </div>
          )}

          <div>
            <label htmlFor="usuario-nombre" className="block text-sm font-medium text-slate-700">
              Nombre completo
            </label>
            <input
              id="usuario-nombre"
              type="text"
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
              disabled={submitting}
              className="mt-1 block w-full rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-sky-600 focus:outline-none focus:ring-1 focus:ring-sky-600"
              placeholder="Ej. Profa. María López"
            />
          </div>

          {!esEdicion && (
            <>
              <div>
                <label htmlFor="usuario-email" className="block text-sm font-medium text-slate-700">
                  Correo institucional
                </label>
                <input
                  id="usuario-email"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  disabled={submitting}
                  className="mt-1 block w-full rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-sky-600 focus:outline-none focus:ring-1 focus:ring-sky-600"
                  placeholder="nombre@passtrack.edu"
                />
              </div>

              <div>
                <label htmlFor="usuario-password" className="block text-sm font-medium text-slate-700">
                  Contraseña inicial
                </label>
                <input
                  id="usuario-password"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  disabled={submitting}
                  className="mt-1 block w-full rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-sky-600 focus:outline-none focus:ring-1 focus:ring-sky-600"
                  placeholder="Mínimo 8 caracteres"
                />
              </div>
            </>
          )}

          <div>
            <label htmlFor="usuario-rol" className="block text-sm font-medium text-slate-700">
              Rol asignado
            </label>
            <select
              id="usuario-rol"
              value={rol}
              onChange={(e) => setRol(e.target.value)}
              disabled={submitting}
              className="mt-1 block w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 focus:border-sky-600 focus:outline-none focus:ring-1 focus:ring-sky-600"
            >
              <option value="DOCENTE">DOCENTE</option>
              <option value="ALUMNO">ALUMNO</option>
              <option value="DIRECTOR">DIRECTOR</option>
            </select>
          </div>

          <div className="flex items-center justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={onClose}
              disabled={submitting}
              className="rounded-md border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="rounded-md bg-sky-600 px-4 py-2 text-sm font-medium text-white hover:bg-sky-700 disabled:opacity-60"
            >
              {submitting ? 'Guardando...' : esEdicion ? 'Guardar cambios' : 'Crear usuario'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
