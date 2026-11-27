import { FileUp } from 'lucide-react';

const ESTADO_STYLES = {
  PRESENTE: 'bg-emerald-50 text-emerald-700 border-emerald-200',
  FALTA: 'bg-rose-50 text-rose-700 border-rose-200',
  RETARDO: 'bg-amber-50 text-amber-700 border-amber-200',
  JUSTIFICADO: 'bg-sky-50 text-sky-700 border-sky-200',
};

const JUSTIFICANTE_STYLES = {
  PENDIENTE: 'bg-amber-50 text-amber-700 border-amber-200',
  APROBADO: 'bg-emerald-50 text-emerald-700 border-emerald-200',
  RECHAZADO: 'bg-rose-50 text-rose-700 border-rose-200',
};

export default function HistorialAsistenciasTable({ historial, onSolicitarJustificante }) {
  if (!historial.length) {
    return (
      <div className="bg-white border border-slate-200 rounded-lg shadow-sm p-8 text-center text-sm text-slate-500">
        Aún no hay sesiones registradas en esta materia.
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
                Fecha
              </th>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-600">
                Estado
              </th>
              <th className="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wider text-slate-600">
                Justificante
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-200">
            {historial.map((registro) => {
              const puedeJustificar =
                (registro.estado === 'FALTA' || registro.estado === 'RETARDO') &&
                !registro.tieneJustificante;

              return (
                <tr key={registro.id} className="hover:bg-slate-50 transition-colors">
                  <td className="px-4 py-3 font-medium text-slate-900">{registro.fecha}</td>
                  <td className="px-4 py-3">
                    <span
                      className={`inline-flex items-center rounded-md border px-2 py-0.5 text-xs font-medium ${
                        ESTADO_STYLES[registro.estado] || 'bg-slate-100 text-slate-700'
                      }`}
                    >
                      {registro.estado}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right">
                    {puedeJustificar ? (
                      <button
                        type="button"
                        onClick={() => onSolicitarJustificante(registro)}
                        className="inline-flex items-center gap-1.5 rounded-md bg-sky-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-sky-700 transition-colors"
                      >
                        <FileUp className="w-3.5 h-3.5" aria-hidden="true" />
                        Solicitar Justificante
                      </button>
                    ) : registro.tieneJustificante ? (
                      <span
                        className={`inline-flex items-center rounded-md border px-2.5 py-1 text-xs font-medium ${
                          JUSTIFICANTE_STYLES[registro.estadoJustificante] ||
                          'bg-slate-100 text-slate-700'
                        }`}
                      >
                        Solicitud: {registro.estadoJustificante}
                      </span>
                    ) : (
                      <span className="text-xs text-slate-400">No aplica</span>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
