import { Eye } from 'lucide-react';

export default function TablaJustificantesPendientes({
  justificantes,
  loading,
  onRevisar,
}) {
  if (loading) {
    return (
      <div className="bg-white border border-slate-200 rounded-lg shadow-sm p-8 text-center text-sm text-slate-500">
        Cargando solicitudes pendientes...
      </div>
    );
  }

  if (!justificantes.length) {
    return (
      <div className="bg-white border border-slate-200 rounded-lg shadow-sm p-8 text-center text-sm text-slate-500">
        No hay solicitudes de justificante pendientes por dictaminar.
      </div>
    );
  }

  return (
    <div className="bg-white border border-slate-200 rounded-lg shadow-sm overflow-hidden">
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-slate-200 text-sm">
          <thead className="bg-slate-50">
            <tr>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-600">
                Alumno
              </th>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-600">
                Grupo
              </th>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-600">
                Fecha falta
              </th>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-600">
                Motivo
              </th>
              <th className="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wider text-slate-600">
                Acción
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-200">
            {justificantes.map((j) => (
              <tr key={j.id} className="hover:bg-slate-50 transition-colors">
                <td className="px-4 py-3 font-medium text-slate-900">{j.alumnoNombre}</td>
                <td className="px-4 py-3 text-slate-600">
                  <span className="font-semibold text-slate-800">{j.grupoClave}</span> ·{' '}
                  {j.grupoNombre}
                </td>
                <td className="px-4 py-3 text-slate-600">{j.fechaAsistencia}</td>
                <td className="px-4 py-3 text-slate-600 max-w-xs truncate" title={j.motivo}>
                  {j.motivo}
                </td>
                <td className="px-4 py-3 text-right">
                  <button
                    type="button"
                    onClick={() => onRevisar(j)}
                    className="inline-flex items-center gap-1.5 rounded-md bg-sky-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-sky-700 transition-colors"
                  >
                    <Eye className="w-3.5 h-3.5" aria-hidden="true" />
                    Revisar
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
