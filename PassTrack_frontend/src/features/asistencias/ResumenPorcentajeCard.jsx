function obtenerColorSemaforo(porcentaje) {
  if (porcentaje >= 85) {
    return {
      badge: 'bg-emerald-50 text-emerald-700 border-emerald-200',
      barra: 'bg-emerald-500',
    };
  }
  if (porcentaje >= 75) {
    return {
      badge: 'bg-amber-50 text-amber-700 border-amber-200',
      barra: 'bg-amber-500',
    };
  }
  return {
    badge: 'bg-rose-50 text-rose-700 border-rose-200',
    barra: 'bg-rose-600',
  };
}

export default function ResumenPorcentajeCard({ resumen, seleccionado, onSeleccionar }) {
  const semaforo = obtenerColorSemaforo(resumen.porcentajeAsistencia);

  return (
    <button
      type="button"
      onClick={() => onSeleccionar(resumen.grupoId)}
      className={`text-left w-full rounded-lg border p-5 shadow-sm transition-all ${
        seleccionado
          ? 'border-sky-600 bg-white ring-1 ring-sky-600'
          : 'border-slate-200 bg-white hover:border-slate-300'
      }`}
    >
      <div className="flex items-start justify-between gap-2">
        <div>
          <span className="inline-flex items-center rounded-md bg-slate-100 px-2 py-0.5 text-xs font-semibold text-slate-700">
            {resumen.grupoClave}
          </span>
          <h3 className="mt-1.5 text-base font-semibold text-slate-900">{resumen.grupoNombre}</h3>
        </div>
        <span
          className={`inline-flex items-center rounded-md border px-2.5 py-1 text-sm font-bold ${semaforo.badge}`}
        >
          {resumen.porcentajeAsistencia.toFixed(1)}%
        </span>
      </div>

      <div className="mt-3 h-2 w-full rounded-full bg-slate-100 overflow-hidden">
        <div
          className={`h-full rounded-full transition-all ${semaforo.barra}`}
          style={{ width: `${Math.min(100, Math.max(0, resumen.porcentajeAsistencia))}%` }}
        />
      </div>

      <dl className="mt-4 grid grid-cols-4 gap-2 text-center text-xs">
        <div className="rounded-md bg-slate-50 py-1.5">
          <dt className="text-slate-500">Pres.</dt>
          <dd className="font-semibold text-emerald-700">{resumen.presentes}</dd>
        </div>
        <div className="rounded-md bg-slate-50 py-1.5">
          <dt className="text-slate-500">Faltas</dt>
          <dd className="font-semibold text-rose-700">{resumen.faltas}</dd>
        </div>
        <div className="rounded-md bg-slate-50 py-1.5">
          <dt className="text-slate-500">Ret.</dt>
          <dd className="font-semibold text-amber-700">{resumen.retardos}</dd>
        </div>
        <div className="rounded-md bg-slate-50 py-1.5">
          <dt className="text-slate-500">Just.</dt>
          <dd className="font-semibold text-sky-700">{resumen.justificados}</dd>
        </div>
      </dl>
    </button>
  );
}
