import { Check, X, XCircle } from 'lucide-react';

export default function VisorEvidenciaModal({
  justificante,
  evidenciaUrl,
  evidenciaTipo,
  loadingEvidencia,
  comentario,
  submitting,
  error,
  onChangeComentario,
  onDictaminar,
  onClose,
}) {
  const comentarioValido = comentario.trim().length > 0;
  const esPdf = evidenciaTipo?.includes('pdf');

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-visor-titulo"
    >
      <div className="w-full max-w-3xl max-h-[90vh] flex flex-col rounded-lg border border-slate-200 bg-white shadow-xl overflow-hidden">
        <div className="flex items-center justify-between border-b border-slate-200 px-5 py-4">
          <div>
            <h2 id="modal-visor-titulo" className="text-lg font-semibold text-slate-900">
              Dictamen de Justificante · {justificante.alumnoNombre}
            </h2>
            <p className="text-xs text-slate-500">
              {justificante.grupoClave} — {justificante.grupoNombre} · Fecha:{' '}
              {justificante.fechaAsistencia}
            </p>
          </div>
          <button
            type="button"
            onClick={onClose}
            disabled={submitting}
            className="rounded-md p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
            aria-label="Cerrar modal"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-5 overflow-y-auto space-y-4">
          <div className="rounded-md bg-slate-50 border border-slate-200 p-3 text-sm">
            <span className="font-semibold text-slate-700">Motivo declarado: </span>
            <span className="text-slate-700">{justificante.motivo}</span>
          </div>

          <div className="rounded-md border border-slate-200 bg-slate-100 min-h-[260px] flex items-center justify-center overflow-hidden">
            {loadingEvidencia ? (
              <p className="text-sm text-slate-500">Cargando comprobante...</p>
            ) : !evidenciaUrl ? (
              <p className="text-sm text-slate-500">No fue posible previsualizar la evidencia.</p>
            ) : esPdf ? (
              <iframe
                title="Previsualización de comprobante PDF"
                src={evidenciaUrl}
                className="w-full h-80 bg-white"
              />
            ) : (
              <img
                src={evidenciaUrl}
                alt={`Evidencia de ${justificante.alumnoNombre}`}
                className="max-h-80 object-contain"
              />
            )}
          </div>

          {error && (
            <div
              role="alert"
              className="rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-xs text-rose-700"
            >
              {error}
            </div>
          )}

          <div>
            <label
              htmlFor="comentario-resolucion"
              className="block text-sm font-medium text-slate-700"
            >
              Comentario de resolución (obligatorio)
            </label>
            <textarea
              id="comentario-resolucion"
              rows={2}
              maxLength={500}
              value={comentario}
              onChange={(e) => onChangeComentario(e.target.value)}
              disabled={submitting}
              placeholder="Indique el fundamento de la aprobación o rechazo..."
              className="mt-1 block w-full rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-sky-600 focus:outline-none focus:ring-1 focus:ring-sky-600"
            />
          </div>
        </div>

        <div className="flex items-center justify-end gap-2 border-t border-slate-200 bg-slate-50 px-5 py-3">
          <button
            type="button"
            onClick={onClose}
            disabled={submitting}
            className="rounded-md border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
          >
            Cancelar
          </button>
          <button
            type="button"
            disabled={!comentarioValido || submitting}
            onClick={() => onDictaminar('RECHAZADO')}
            className="inline-flex items-center gap-1.5 rounded-md bg-rose-600 px-4 py-2 text-sm font-medium text-white hover:bg-rose-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <XCircle className="w-4 h-4" aria-hidden="true" />
            Rechazar
          </button>
          <button
            type="button"
            disabled={!comentarioValido || submitting}
            onClick={() => onDictaminar('APROBADO')}
            className="inline-flex items-center gap-1.5 rounded-md bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <Check className="w-4 h-4" aria-hidden="true" />
            Aprobar
          </button>
        </div>
      </div>
    </div>
  );
}
