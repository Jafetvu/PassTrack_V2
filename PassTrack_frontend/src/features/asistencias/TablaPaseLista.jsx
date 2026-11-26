const OPCIONES_ESTADO = [
  {
    valor: 'PRESENTE',
    etiqueta: 'Presente',
    activoClass: 'bg-emerald-600 text-white border-emerald-600',
    inactivoClass: 'bg-white text-slate-700 border-slate-200 hover:bg-emerald-50',
  },
  {
    valor: 'RETARDO',
    etiqueta: 'Retardo',
    activoClass: 'bg-amber-500 text-white border-amber-500',
    inactivoClass: 'bg-white text-slate-700 border-slate-200 hover:bg-amber-50',
  },
  {
    valor: 'FALTA',
    etiqueta: 'Falta',
    activoClass: 'bg-rose-600 text-white border-rose-600',
    inactivoClass: 'bg-white text-slate-700 border-slate-200 hover:bg-rose-50',
  },
];

export default function TablaPaseLista({
  alumnos,
  estados,
  yaRegistrado,
  loading,
  onCambiarEstado,
}) {
  if (loading) {
    return (
      <div className="bg-white border border-slate-200 rounded-lg shadow-sm p-8 text-center text-sm text-slate-500">
        Cargando nómina del grupo...
      </div>
    );
  }

  if (!alumnos.length) {
    return (
      <div className="bg-white border border-slate-200 rounded-lg shadow-sm p-8 text-center text-sm text-slate-500">
        Este grupo no cuenta con alumnos inscritos todavía.
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
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-600 hidden sm:table-cell">
                Correo
              </th>
              <th className="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wider text-slate-600">
                Asistencia
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-200">
            {alumnos.map((item) => {
              const estadoActual = estados[item.alumnoId] || 'PRESENTE';
              return (
                <tr key={item.alumnoId} className="hover:bg-slate-50 transition-colors">
                  <td className="px-4 py-3 font-medium text-slate-900">{item.alumnoNombre}</td>
                  <td className="px-4 py-3 text-slate-600 hidden sm:table-cell">
                    {item.alumnoEmail || '—'}
                  </td>
                  <td className="px-4 py-3 text-right">
                    {estadoActual === 'JUSTIFICADO' ? (
                      <span className="inline-flex items-center rounded-md bg-sky-50 px-2.5 py-1 text-xs font-medium text-sky-700 border border-sky-200">
                        JUSTIFICADO
                      </span>
                    ) : (
                      <div
                        className="inline-flex items-center gap-1.5"
                        role="group"
                        aria-label={`Estado de ${item.alumnoNombre}`}
                      >
                        {OPCIONES_ESTADO.map((op) => {
                          const seleccionado = estadoActual === op.valor;
                          return (
                            <button
                              key={op.valor}
                              type="button"
                              disabled={yaRegistrado}
                              aria-pressed={seleccionado}
                              onClick={() => onCambiarEstado(item.alumnoId, op.valor)}
                              className={`rounded-md border px-2.5 py-1 text-xs font-medium transition-colors disabled:opacity-65 disabled:cursor-not-allowed ${
                                seleccionado ? op.activoClass : op.inactivoClass
                              }`}
                            >
                              {op.etiqueta}
                            </button>
                          );
                        })}
                      </div>
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
