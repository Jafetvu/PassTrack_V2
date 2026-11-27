import axiosClient from '../../api/axiosClient';

export async function solicitarJustificante({ asistenciaId, motivo, archivo }) {
  const formData = new FormData();
  formData.append('asistenciaId', String(asistenciaId));
  formData.append('motivo', motivo);
  formData.append('archivo', archivo);

  const { data } = await axiosClient.post('/justificantes', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return data;
}

export async function obtenerJustificantesPendientes() {
  const { data } = await axiosClient.get('/justificantes/pendientes');
  return data;
}

export async function resolverJustificante(id, { estado, comentarioResolucion }) {
  const { data } = await axiosClient.patch(`/justificantes/${id}/estado`, {
    estado,
    comentarioResolucion,
  });
  return data;
}

export async function obtenerEvidenciaBlob(id) {
  const response = await axiosClient.get(`/justificantes/${id}/evidencia`, {
    responseType: 'blob',
  });
  return {
    blob: response.data,
    contentType: response.headers['content-type'] || response.data.type,
  };
}
