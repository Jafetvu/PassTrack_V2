import { useState } from 'react';
import { solicitarJustificante } from './justificantesService';
import SolicitudJustificanteModal from './SolicitudJustificanteModal';

const MAX_BYTES = 2 * 1024 * 1024;
const ALLOWED_TYPES = new Set(['application/pdf', 'image/jpeg', 'image/png']);

export default function SolicitarJustificanteContainer({ asistencia, onClose, onSuccess }) {
  const [motivo, setMotivo] = useState('');
  const [archivo, setArchivo] = useState(null);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');

    if (!motivo.trim()) {
      setError('Debe explicar el motivo de la solicitud.');
      return;
    }
    if (!archivo) {
      setError('Debe adjuntar un archivo de comprobante.');
      return;
    }
    if (!ALLOWED_TYPES.has(archivo.type)) {
      setError('Formato no válido. Solo se permiten archivos PDF, JPEG o PNG.');
      return;
    }
    if (archivo.size > MAX_BYTES) {
      setError('El archivo supera el tamaño máximo permitido de 2 MB.');
      return;
    }

    setSubmitting(true);
    try {
      await solicitarJustificante({
        asistenciaId: asistencia.id,
        motivo: motivo.trim(),
        archivo,
      });
      await onSuccess();
    } catch (err) {
      setError(err?.response?.data?.message || 'No fue posible enviar la solicitud de justificante.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <SolicitudJustificanteModal
      asistencia={asistencia}
      motivo={motivo}
      archivo={archivo}
      error={error}
      submitting={submitting}
      onChangeMotivo={setMotivo}
      onChangeArchivo={setArchivo}
      onSubmit={handleSubmit}
      onClose={onClose}
    />
  );
}
