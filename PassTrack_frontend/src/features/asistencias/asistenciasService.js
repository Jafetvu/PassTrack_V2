import axiosClient from '../../api/axiosClient';

export async function obtenerGruposDocente() {
  const { data } = await axiosClient.get('/grupos/docente');
  return data;
}

export async function obtenerNominaGrupo(grupoId) {
  const { data } = await axiosClient.get(`/grupos/${grupoId}/inscripciones`);
  return data;
}

export async function obtenerAsistenciasHoy(grupoId) {
  const { data } = await axiosClient.get(`/asistencias/grupo/${grupoId}/hoy`);
  return data;
}

export async function registrarPaseLista(grupoId, payload) {
  const { data } = await axiosClient.post(`/asistencias/grupo/${grupoId}`, payload);
  return data;
}

export async function obtenerMisAsistencias() {
  const { data } = await axiosClient.get('/asistencias/mis-asistencias');
  return data;
}
