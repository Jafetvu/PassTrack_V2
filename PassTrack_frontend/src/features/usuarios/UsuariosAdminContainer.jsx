import { useCallback, useEffect, useMemo, useState } from 'react';
import { Plus } from 'lucide-react';
import ModalUsuarioForm from './ModalUsuarioForm';
import TablaUsuarios from './TablaUsuarios';
import {
  actualizarUsuario,
  crearUsuario,
  desactivarUsuario,
  obtenerUsuarios,
} from './usuariosService';

const FILTROS_ROL = ['TODOS', 'DOCENTE', 'ALUMNO', 'DIRECTOR'];

export default function UsuariosAdminContainer() {
  const [usuarios, setUsuarios] = useState([]);
  const [filtroRol, setFiltroRol] = useState('TODOS');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [mensaje, setMensaje] = useState('');

  const [modalAbierto, setModalAbierto] = useState(false);
  const [usuarioEnEdicion, setUsuarioEnEdicion] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [modalError, setModalError] = useState('');

  const [usuarioParaBaja, setUsuarioParaBaja] = useState(null);
  const [bajaEnProceso, setBajaEnProceso] = useState(false);

  const cargarUsuarios = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const pagina = await obtenerUsuarios({ page: 0, size: 100 });
      setUsuarios(Array.isArray(pagina?.content) ? pagina.content : []);
    } catch (err) {
      setError(err?.response?.data?.message || 'No fue posible cargar la lista de usuarios.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    cargarUsuarios();
  }, [cargarUsuarios]);

  const usuariosFiltrados = useMemo(() => {
    if (filtroRol === 'TODOS') return usuarios;
    return usuarios.filter((u) => u.rol === filtroRol);
  }, [usuarios, filtroRol]);

  const abrirModalCrear = () => {
    setUsuarioEnEdicion(null);
    setModalError('');
    setModalAbierto(true);
  };

  const abrirModalEditar = (usuario) => {
    setUsuarioEnEdicion(usuario);
    setModalError('');
    setModalAbierto(true);
  };

  const handleGuardarUsuario = async (payload) => {
    setSubmitting(true);
    setModalError('');
    setMensaje('');
    try {
      if (usuarioEnEdicion) {
        await actualizarUsuario(usuarioEnEdicion.id, payload);
        setMensaje('Usuario actualizado correctamente.');
      } else {
        await crearUsuario(payload);
        setMensaje('Usuario registrado correctamente.');
      }
      setModalAbierto(false);
      await cargarUsuarios();
    } catch (err) {
      setModalError(err?.response?.data?.message || 'Ocurrió un error al guardar el usuario.');
    } finally {
      setSubmitting(false);
    }
  };

  const confirmarBajaLogica = async () => {
    if (!usuarioParaBaja) return;
    setBajaEnProceso(true);
    setError('');
    setMensaje('');
    try {
      await desactivarUsuario(usuarioParaBaja.id);
      setMensaje(`Usuario ${usuarioParaBaja.nombre} dado de baja lógicamente.`);
      setUsuarioParaBaja(null);
      await cargarUsuarios();
    } catch (err) {
      setError(err?.response?.data?.message || 'No se pudo desactivar el usuario.');
    } finally {
      setBajaEnProceso(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">
            Administración de Usuarios
          </h1>
          <p className="mt-1 text-sm text-slate-600">
            Alta, edición y baja lógica de docentes, alumnos y directores.
          </p>
        </div>

        <button
          type="button"
          onClick={abrirModalCrear}
          className="inline-flex items-center justify-center gap-2 rounded-md bg-sky-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-sky-700 transition-colors"
        >
          <Plus className="w-4 h-4" aria-hidden="true" />
          Nuevo usuario
        </button>
      </div>

      <div className="flex flex-wrap items-center gap-2" role="group" aria-label="Filtrar por rol">
        {FILTROS_ROL.map((rolItem) => (
          <button
            key={rolItem}
            type="button"
            onClick={() => setFiltroRol(rolItem)}
            className={`rounded-md px-3 py-1.5 text-xs font-medium transition-colors ${
              filtroRol === rolItem
                ? 'bg-slate-900 text-white'
                : 'bg-white border border-slate-200 text-slate-600 hover:bg-slate-50'
            }`}
          >
            {rolItem}
          </button>
        ))}
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

      <TablaUsuarios
        usuarios={usuariosFiltrados}
        loading={loading}
        onEditar={abrirModalEditar}
        onSolicitarBaja={setUsuarioParaBaja}
      />

      {modalAbierto && (
        <ModalUsuarioForm
          key={usuarioEnEdicion?.id ?? 'nuevo'}
          usuarioInicial={usuarioEnEdicion}
          submitting={submitting}
          apiError={modalError}
          onSubmit={handleGuardarUsuario}
          onClose={() => setModalAbierto(false)}
        />
      )}

      {usuarioParaBaja && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4"
          role="dialog"
          aria-modal="true"
          aria-labelledby="modal-baja-titulo"
        >
          <div className="w-full max-w-md rounded-lg border border-slate-200 bg-white p-6 shadow-lg space-y-4">
            <h2 id="modal-baja-titulo" className="text-lg font-semibold text-slate-900">
              Confirmar baja lógica
            </h2>
            <p className="text-sm text-slate-600">
              ¿Desea desactivar la cuenta de{' '}
              <span className="font-semibold text-slate-900">{usuarioParaBaja.nombre}</span>? Su
              historial académico permanecerá intacto, pero no podrá iniciar sesión.
            </p>
            <div className="flex items-center justify-end gap-2 pt-2">
              <button
                type="button"
                onClick={() => setUsuarioParaBaja(null)}
                disabled={bajaEnProceso}
                className="rounded-md border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
              >
                Cancelar
              </button>
              <button
                type="button"
                onClick={confirmarBajaLogica}
                disabled={bajaEnProceso}
                className="rounded-md bg-rose-600 px-4 py-2 text-sm font-medium text-white hover:bg-rose-700 disabled:opacity-60"
              >
                {bajaEnProceso ? 'Desactivando...' : 'Confirmar baja'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
