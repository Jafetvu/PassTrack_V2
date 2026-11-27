import { useCallback, useEffect, useState } from 'react';
import {
  obtenerEvidenciaBlob,
  obtenerJustificantesPendientes,
  resolverJustificante,
} from './justificantesService';
import TablaJustificantesPendientes from './TablaJustificantesPendientes';
import VisorEvidenciaModal from './VisorEvidenciaModal';

export default function BandejaJustificantesContainer() {
  const [pendientes, setPendientes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [mensaje, setMensaje] = useState('');

  const [seleccionado, setSeleccionado] = useState(null);
  const [evidenciaUrl, setEvidenciaUrl] = useState('');
  const [evidenciaTipo, setEvidenciaTipo] = useState('');
  const [loadingEvidencia, setLoadingEvidencia] = useState(false);
  const [comentario, setComentario] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [modalError, setModalError] = useState('');

  const cargarPendientes = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const lista = await obtenerJustificantesPendientes();
      setPendientes(Array.isArray(lista) ? lista : []);
    } catch (err) {
      setError(err?.response?.data?.message || 'No fue posible cargar la bandeja de justificantes.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    cargarPendientes();
  }, [cargarPendientes]);

  useEffect(() => {
    return () => {
      if (evidenciaUrl) URL.revokeObjectURL(evidenciaUrl);
    };
  }, [evidenciaUrl]);

  const cerrarModal = () => {
    if (evidenciaUrl) {
      URL.revokeObjectURL(evidenciaUrl);
      setEvidenciaUrl('');
    }
    setSeleccionado(null);
    setComentario('');
    setModalError('');
  };

  const abrirRevision = async (justificante) => {
    if (evidenciaUrl) {
      URL.revokeObjectURL(evidenciaUrl);
      setEvidenciaUrl('');
    }
    setSeleccionado(justificante);
    setComentario('');
    setModalError('');
    setLoadingEvidencia(true);
    try {
      const { blob, contentType } = await obtenerEvidenciaBlob(justificante.id);
      setEvidenciaUrl(URL.createObjectURL(blob));
      setEvidenciaTipo(contentType || blob.type || '');
    } catch (err) {
      setModalError(err?.response?.data?.message || 'No se pudo cargar el archivo de evidencia.');
    } finally {
      setLoadingEvidencia(false);
    }
  };

  const handleDictaminar = async (estado) => {
    if (!seleccionado || !comentario.trim()) return;
    setSubmitting(true);
    setModalError('');
    setMensaje('');
    try {
      await resolverJustificante(seleccionado.id, {
        estado,
        comentarioResolucion: comentario.trim(),
      });
      setPendientes((prev) => prev.filter((item) => item.id !== seleccionado.id));
      setMensaje(
        estado === 'APROBADO'
          ? `Justificante de ${seleccionado.alumnoNombre} aprobado. La asistencia fue marcada como JUSTIFICADO.`
          : `Justificante de ${seleccionado.alumnoNombre} rechazado.`,
      );
      cerrarModal();
    } catch (err) {
      setModalError(err?.response?.data?.message || 'No fue posible registrar el dictamen.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-slate-900">
          Bandeja de Justificantes
        </h1>
        <p className="mt-1 text-sm text-slate-600">
          Revisa los comprobantes enviados por alumnos y emite el dictamen correspondiente.
        </p>
      </div>

      {mensaje && (
        <div
          role="status"
          className="rounded-md border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800"
        >
          {mensaje}
        </div>
      )}

      {error && (
        <div
          role="alert"
          className="rounded-md border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700"
        >
          {error}
        </div>
      )}

      <TablaJustificantesPendientes
        justificantes={pendientes}
        loading={loading}
        onRevisar={abrirRevision}
      />

      {seleccionado && (
        <VisorEvidenciaModal
          justificante={seleccionado}
          evidenciaUrl={evidenciaUrl}
          evidenciaTipo={evidenciaTipo}
          loadingEvidencia={loadingEvidencia}
          comentario={comentario}
          submitting={submitting}
          error={modalError}
          onChangeComentario={setComentario}
          onDictaminar={handleDictaminar}
          onClose={cerrarModal}
        />
      )}
    </div>
  );
}
