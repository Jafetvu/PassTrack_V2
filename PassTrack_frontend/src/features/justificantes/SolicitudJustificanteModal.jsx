import { Upload, X } from 'lucide-react';

export default function SolicitudJustificanteModal({
  asistencia,
  motivo,
  archivo,
  error,
  submitting,
  onChangeMotivo,
  onChangeArchivo,
  onSubmit,
  onClose,
}) {
  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-justificante-titulo"
    >
      <div className="w-full max-w-md rounded-lg border border-slate-200 bg-white shadow-lg">
        <div className="flex items-center justify-between border-b border-slate-200 px-5 py-4">
          <div>
            <h2 id="modal-justificante-titulo" className="text-lg font-semibold text-slate-900">
              Solicitar Justificante
            </h2>
            <p className="text-xs text-slate-500">
              Sesión del {asistencia.fecha} · Estado actual: {asistencia.estado}
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

        <form onSubmit={onSubmit} className="p-5 space-y-4" noValidate>
          {error && (
            <div
              role="alert"
              className="rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-xs text-rose-700"
            >
              {error}
            </div>
          )}

          <div>
            <label htmlFor="justificante-motivo" className="block text-sm font-medium text-slate-700">
              Motivo de la inasistencia o retardo
            </label>
            <textarea
              id="justificante-motivo"
              rows={3}
              maxLength={500}
              value={motivo}
              onChange={(e) => onChangeMotivo(e.target.value)}
              disabled={submitting}
              placeholder="Describe brevemente el motivo (máx. 500 caracteres)..."
              className="mt-1 block w-full rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-sky-600 focus:outline-none focus:ring-1 focus:ring-sky-600"
            />
          </div>

          <div>
            <label htmlFor="justificante-archivo" className="block text-sm font-medium text-slate-700">
              Comprobante (PDF, JPEG o PNG · máx. 2 MB)
            </label>
            <input
              id="justificante-archivo"
              type="file"
              accept="application/pdf,image/jpeg,image/png"
              onChange={(e) => onChangeArchivo(e.target.files?.[0] || null)}
              disabled={submitting}
              className="mt-1 block w-full text-xs text-slate-600 file:mr-3 file:rounded-md file:border file:border-slate-200 file:bg-slate-50 file:px-3 file:py-1.5 file:text-xs file:font-medium file:text-slate-700 hover:file:bg-slate-100"
            />
            {archivo && (
              <p className="mt-1 text-xs text-slate-500">
                Archivo seleccionado: <span className="font-medium text-slate-700">{archivo.name}</span>{' '}
                ({(archivo.size / 1024).toFixed(0)} KB)
              </p>
            )}
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
              className="inline-flex items-center gap-1.5 rounded-md bg-sky-600 px-4 py-2 text-sm font-medium text-white hover:bg-sky-700 disabled:opacity-60"
            >
              <Upload className="w-4 h-4" aria-hidden="true" />
              {submitting ? 'Enviando...' : 'Enviar solicitud'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
