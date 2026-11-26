import { useState } from 'react';
import { UserPlus, X } from 'lucide-react';

export default function AsignarAlumnoModal({
  grupo,
  alumnosDisponibles,
  inscripciones,
  loadingInscripciones,
  submitting,
  apiError,
  onInscribir,
  onClose,
}) {
  const [alumnoIdSeleccionado, setAlumnoIdSeleccionado] = useState('');
  const [validationError, setValidationError] = useState('');

  const handleSubmit = (event) => {
    event.preventDefault();
    setValidationError('');
    if (!alumnoIdSeleccionado) {
      setValidationError('Seleccione un alumno para matricular.');
      return;
    }
    onInscribir(Number(alumnoIdSeleccionado));
    setAlumnoIdSeleccionado('');
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-inscripcion-titulo"
    >
      <div className="w-full max-w-lg rounded-lg border border-slate-200 bg-white shadow-lg">
        <div className="flex items-center justify-between border-b border-slate-200 px-5 py-4">
          <div>
            <h2 id="modal-inscripcion-titulo" className="text-lg font-semibold text-slate-900">
              Inscripciones · {grupo.clave}
            </h2>
            <p className="text-xs text-slate-500">{grupo.nombre}</p>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="rounded-md p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
            aria-label="Cerrar modal"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-5 space-y-5">
          <form onSubmit={handleSubmit} className="space-y-3" noValidate>
            {(validationError || apiError) && (
              <div
                role="alert"
                className="rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-xs text-rose-700"
              >
                {validationError || apiError}
              </div>
            )}

            <label htmlFor="select-alumno" className="block text-sm font-medium text-slate-700">
              Matricular alumno activo
            </label>
            <div className="flex gap-2">
              <select
                id="select-alumno"
                value={alumnoIdSeleccionado}
                onChange={(e) => setAlumnoIdSeleccionado(e.target.value)}
                disabled={submitting || !alumnosDisponibles.length}
                className="flex-1 rounded-md border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 focus:border-sky-600 focus:outline-none focus:ring-1 focus:ring-sky-600"
              >
                <option value="">
                  {alumnosDisponibles.length
                    ? 'Seleccione un alumno...'
                    : 'No hay alumnos disponibles por inscribir'}
                </option>
                {alumnosDisponibles.map((alumno) => (
                  <option key={alumno.id} value={alumno.id}>
                    {alumno.nombre} ({alumno.email})
                  </option>
                ))}
              </select>
              <button
                type="submit"
                disabled={submitting || !alumnosDisponibles.length}
                className="inline-flex items-center gap-1.5 rounded-md bg-sky-600 px-3.5 py-2 text-sm font-medium text-white hover:bg-sky-700 disabled:opacity-60"
              >
                <UserPlus className="w-4 h-4" aria-hidden="true" />
                {submitting ? 'Agregando...' : 'Inscribir'}
              </button>
            </div>
          </form>

          <div className="border-t border-slate-200 pt-4">
            <h3 className="text-xs font-semibold uppercase tracking-wider text-slate-500 mb-2">
              Alumnos inscritos ({inscripciones.length})
            </h3>
            {loadingInscripciones ? (
              <p className="text-sm text-slate-500">Cargando nómina...</p>
            ) : !inscripciones.length ? (
              <p className="text-sm text-slate-500">Este grupo aún no tiene alumnos inscritos.</p>
            ) : (
              <ul className="max-h-56 overflow-y-auto divide-y divide-slate-100 rounded-md border border-slate-200">
                {inscripciones.map((ins) => (
                  <li key={ins.id} className="px-3 py-2 flex items-center justify-between text-sm">
                    <span className="font-medium text-slate-900">{ins.alumnoNombre}</span>
                    <span className="text-xs text-slate-500">{ins.alumnoEmail}</span>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
