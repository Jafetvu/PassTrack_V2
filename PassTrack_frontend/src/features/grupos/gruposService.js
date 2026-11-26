import axiosClient from '../../api/axiosClient';

export async function obtenerGrupos() {
  const { data } = await axiosClient.get('/grupos');
  return data;
}

export async function crearGrupo(payload) {
  const { data } = await axiosClient.post('/grupos', payload);
  return data;
}

export async function obtenerInscripcionesGrupo(grupoId) {
  const { data } = await axiosClient.get(`/grupos/${grupoId}/inscripciones`);
  return data;
}

export async function inscribirAlumnoEnGrupo(grupoId, alumnoId) {
  const { data } = await axiosClient.post(`/grupos/${grupoId}/inscripciones`, {
    alumnoId,
  });
  return data;
}
