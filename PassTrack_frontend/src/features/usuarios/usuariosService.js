import axiosClient from '../../api/axiosClient';

export async function obtenerUsuarios({ page = 0, size = 50 } = {}) {
  const { data } = await axiosClient.get('/usuarios', {
    params: { page, size },
  });
  return data;
}

export async function obtenerUsuariosPorRol(rol) {
  const { data } = await axiosClient.get(`/usuarios/rol/${rol}`);
  return data;
}

export async function crearUsuario(payload) {
  const { data } = await axiosClient.post('/usuarios', payload);
  return data;
}

export async function actualizarUsuario(id, payload) {
  const { data } = await axiosClient.put(`/usuarios/${id}`, payload);
  return data;
}

export async function desactivarUsuario(id) {
  await axiosClient.delete(`/usuarios/${id}`);
}
