import { useCallback, useEffect, useMemo, useState } from 'react';
import { Plus } from 'lucide-react';
import { obtenerUsuariosPorRol } from '../usuarios/usuariosService';
import AsignarAlumnoModal from './AsignarAlumnoModal';
import {
  crearGrupo,
  inscribirAlumnoEnGrupo,
  obtenerGrupos,
  obtenerInscripcionesGrupo,
} from './gruposService';
import ListaGruposCard from './ListaGruposCard';

export default function GruposContainer() {
  const [grupos, setGrupos] = useState([]);
  const [docentes, setDocentes] = useState([]);
  const [alumnos, setAlumnos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [mensaje, setMensaje] = useState('');

  const [clave, setClave] = useState('');
  const [nombre, setNombre] = useState('');
  const [docenteId, setDocenteId] = useState('');
  const [formError, setFormError] = useState('');
  const [creandoGrupo, setCreandoGrupo] = useState(false);

  const [grupoSeleccionado, setGrupoSeleccionado] = useState(null);
  const [inscripciones, setInscripciones] = useState([]);
  const [loadingInscripciones, setLoadingInscripciones] = useState(false);
  const [inscribiendo, setInscribiendo] = useState(false);
  const [modalError, setModalError] = useState('');

  const cargarDatosIniciales = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const [listaGrupos, listaDocentes, listaAlumnos] = await Promise.all([
        obtenerGrupos(),
        obtenerUsuariosPorRol('DOCENTE'),
        obtenerUsuariosPorRol('ALUMNO'),
      ]);
      setGrupos(Array.isArray(listaGrupos) ? listaGrupos : []);
      setDocentes(Array.isArray(listaDocentes) ? listaDocentes : []);
      setAlumnos(Array.isArray(listaAlumnos) ? listaAlumnos : []);
    } catch (err) {
      setError(err?.response?.data?.message || 'No fue posible cargar la información de grupos.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    cargarDatosIniciales();
  }, [cargarDatosIniciales]);

  const handleCrearGrupo = async (event) => {
    event.preventDefault();
    setFormError('');
    setMensaje('');

    if (!clave.trim()) {
      setFormError('La clave del grupo es obligatoria.');
      return;
    }
    if (!nombre.trim()) {
      setFormError('El nombre de la materia o grupo es obligatorio.');
      return;
    }
    if (!docenteId) {
      setFormError('Debe asignar un docente titular.');
      return;
    }

    setCreandoGrupo(true);
    try {
      await crearGrupo({
        clave: clave.trim().toUpperCase(),
        nombre: nombre.trim(),
        docenteId: Number(docenteId),
      });
      setClave('');
      setNombre('');
      setDocenteId('');
      setMensaje('Grupo creado correctamente.');
      await cargarDatosIniciales();
    } catch (err) {
      setFormError(err?.response?.data?.message || 'No se pudo crear el grupo.');
    } finally {
      setCreandoGrupo(false);
    }
  };

  const abrirModalAlumnos = async (grupo) => {
    setGrupoSeleccionado(grupo);
    setModalError('');
    setLoadingInscripciones(true);
    try {
      const lista = await obtenerInscripcionesGrupo(grupo.id);
      setInscripciones(Array.isArray(lista) ? lista : []);
    } catch (err) {
      setModalError(err?.response?.data?.message || 'No se pudieron obtener las inscripciones.');
      setInscripciones([]);
    } finally {
      setLoadingInscripciones(false);
    }
  };

  const alumnosDisponibles = useMemo(() => {
    const idsInscritos = new Set(inscripciones.map((i) => i.alumnoId));
    return alumnos.filter((a) => !idsInscritos.has(a.id));
  }, [alumnos, inscripciones]);

  const handleInscribirAlumno = async (alumnoId) => {
    if (!grupoSeleccionado) return;
    setInscribiendo(true);
    setModalError('');
    try {
      await inscribirAlumnoEnGrupo(grupoSeleccionado.id, alumnoId);
      const [listaActualizada, gruposActualizados] = await Promise.all([
        obtenerInscripcionesGrupo(grupoSeleccionado.id),
        obtenerGrupos(),
      ]);
      setInscripciones(Array.isArray(listaActualizada) ? listaActualizada : []);
      setGrupos(Array.isArray(gruposActualizados) ? gruposActualizados : []);
    } catch (err) {
      setModalError(err?.response?.data?.message || 'No fue posible inscribir al alumno.');
    } finally {
      setInscribiendo(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-slate-900">
          Gestión de Grupos e Inscripciones
        </h1>
        <p className="mt-1 text-sm text-slate-600">
          Asigne docentes titulares a nuevos grupos y matricule alumnos activos.
        </p>
      </div>

      <section className="bg-white border border-slate-200 rounded-lg shadow-sm p-5">
        <h2 className="text-base font-semibold text-slate-900 mb-4">Crear nuevo grupo</h2>

        {formError && (
          <div
            role="alert"
            className="mb-4 rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-xs text-rose-700"
          >
            {formError}
          </div>
        )}

        <form onSubmit={handleCrearGrupo} className="grid grid-cols-1 md:grid-cols-4 gap-3" noValidate>
          <div>
            <label htmlFor="grupo-clave" className="block text-xs font-medium text-slate-700">
              Clave
            </label>
            <input
              id="grupo-clave"
              type="text"
              value={clave}
              onChange={(e) => setClave(e.target.value)}
              disabled={creandoGrupo}
              placeholder="Ej. MAT-101"
              className="mt-1 block w-full rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-sky-600 focus:outline-none focus:ring-1 focus:ring-sky-600"
            />
          </div>

          <div>
            <label htmlFor="grupo-nombre" className="block text-xs font-medium text-slate-700">
              Nombre del grupo
            </label>
            <input
              id="grupo-nombre"
              type="text"
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
              disabled={creandoGrupo}
              placeholder="Ej. Cálculo Diferencial"
              className="mt-1 block w-full rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-sky-600 focus:outline-none focus:ring-1 focus:ring-sky-600"
            />
          </div>

          <div>
            <label htmlFor="grupo-docente" className="block text-xs font-medium text-slate-700">
              Docente titular
            </label>
            <select
              id="grupo-docente"
              value={docenteId}
              onChange={(e) => setDocenteId(e.target.value)}
              disabled={creandoGrupo}
              className="mt-1 block w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 focus:border-sky-600 focus:outline-none focus:ring-1 focus:ring-sky-600"
            >
              <option value="">Seleccione un docente...</option>
              {docentes.map((doc) => (
                <option key={doc.id} value={doc.id}>
                  {doc.nombre}
                </option>
              ))}
            </select>
          </div>

          <div className="flex items-end">
            <button
              type="submit"
              disabled={creandoGrupo}
              className="w-full inline-flex items-center justify-center gap-1.5 rounded-md bg-sky-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-sky-700 disabled:opacity-60 transition-colors"
            >
              <Plus className="w-4 h-4" aria-hidden="true" />
              {creandoGrupo ? 'Creando...' : 'Crear grupo'}
            </button>
          </div>
        </form>
      </section>

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

      <ListaGruposCard
        grupos={grupos}
        loading={loading}
        onGestionarAlumnos={abrirModalAlumnos}
      />

      {grupoSeleccionado && (
        <AsignarAlumnoModal
          grupo={grupoSeleccionado}
          alumnosDisponibles={alumnosDisponibles}
          inscripciones={inscripciones}
          loadingInscripciones={loadingInscripciones}
          submitting={inscribiendo}
          apiError={modalError}
          onInscribir={handleInscribirAlumno}
          onClose={() => setGrupoSeleccionado(null)}
        />
      )}
    </div>
  );
}
