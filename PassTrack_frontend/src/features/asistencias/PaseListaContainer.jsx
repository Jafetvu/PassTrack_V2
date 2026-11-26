import { useCallback, useEffect, useState } from 'react';
import { CalendarCheck2, CheckCircle2, Save } from 'lucide-react';
import {
  obtenerAsistenciasHoy,
  obtenerGruposDocente,
  obtenerNominaGrupo,
  registrarPaseLista,
} from './asistenciasService';
import TablaPaseLista from './TablaPaseLista';

function obtenerFechaLocalIso() {
  const ahora = new Date();
  const anio = ahora.getFullYear();
  const mes = String(ahora.getMonth() + 1).padStart(2, '0');
  const dia = String(ahora.getDate()).padStart(2, '0');
  return `${anio}-${mes}-${dia}`;
}

export default function PaseListaContainer() {
  const [grupos, setGrupos] = useState([]);
  const [grupoActivoId, setGrupoActivoId] = useState(null);
  const [loadingGrupos, setLoadingGrupos] = useState(true);

  const [alumnos, setAlumnos] = useState([]);
  const [estados, setEstados] = useState({});
  const [yaRegistrado, setYaRegistrado] = useState(false);
  const [loadingNomina, setLoadingNomina] = useState(false);

  const [guardando, setGuardando] = useState(false);
  const [mensaje, setMensaje] = useState('');
  const [error, setError] = useState('');

  const fechaHoy = obtenerFechaLocalIso();

  useEffect(() => {
    let activo = true;
    (async () => {
      setLoadingGrupos(true);
      setError('');
      try {
        const lista = await obtenerGruposDocente();
        if (!activo) return;
        const gruposSeguros = Array.isArray(lista) ? lista : [];
        setGrupos(gruposSeguros);
        if (gruposSeguros.length > 0) {
          setGrupoActivoId(gruposSeguros[0].id);
        }
      } catch (err) {
        if (activo) {
          setError(err?.response?.data?.message || 'No fue posible cargar tus grupos asignados.');
        }
      } finally {
        if (activo) setLoadingGrupos(false);
      }
    })();
    return () => {
      activo = false;
    };
  }, []);

  const cargarGrupo = useCallback(async (grupoId) => {
    if (!grupoId) return;
    setLoadingNomina(true);
    setError('');
    setMensaje('');
    try {
      const [nomina, asistenciasHoy] = await Promise.all([
        obtenerNominaGrupo(grupoId),
        obtenerAsistenciasHoy(grupoId),
      ]);

      const listaNomina = Array.isArray(nomina) ? nomina : [];
      const listaHoy = Array.isArray(asistenciasHoy) ? asistenciasHoy : [];
      setAlumnos(listaNomina);

      if (listaHoy.length > 0) {
        const mapaExistente = {};
        listaHoy.forEach((registro) => {
          mapaExistente[registro.alumnoId] = registro.estado;
        });
        setEstados(mapaExistente);
        setYaRegistrado(true);
      } else {
        // Preselección optimista en PRESENTE (P-01)
        const mapaInicial = {};
        listaNomina.forEach((ins) => {
          mapaInicial[ins.alumnoId] = 'PRESENTE';
        });
        setEstados(mapaInicial);
        setYaRegistrado(false);
      }
    } catch (err) {
      setError(err?.response?.data?.message || 'No fue posible cargar la nómina del grupo.');
    } finally {
      setLoadingNomina(false);
    }
  }, []);

  useEffect(() => {
    if (grupoActivoId) {
      cargarGrupo(grupoActivoId);
    }
  }, [grupoActivoId, cargarGrupo]);

  const handleCambiarEstado = (alumnoId, nuevoEstado) => {
    if (yaRegistrado) return;
    setEstados((prev) => ({ ...prev, [alumnoId]: nuevoEstado }));
  };

  const handleGuardar = async () => {
    if (!grupoActivoId || !alumnos.length || yaRegistrado) return;
    setGuardando(true);
    setError('');
    setMensaje('');
    try {
      const lista = alumnos.map((ins) => ({
        alumnoId: ins.alumnoId,
        estado: estados[ins.alumnoId] || 'PRESENTE',
      }));
      await registrarPaseLista(grupoActivoId, { fecha: fechaHoy, lista });
      setYaRegistrado(true);
      setMensaje('Pase de lista registrado exitosamente.');
    } catch (err) {
      setError(err?.response?.data?.message || 'Ocurrió un error al guardar el pase de lista.');
    } finally {
      setGuardando(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">
            Pase de Lista del Día
          </h1>
          <p className="mt-1 text-sm text-slate-600 inline-flex items-center gap-1.5">
            <CalendarCheck2 className="w-4 h-4 text-sky-600" aria-hidden="true" />
            Fecha de sesión: <span className="font-medium text-slate-900">{fechaHoy}</span>
          </p>
        </div>

        {grupoActivoId && alumnos.length > 0 && (
          <button
            type="button"
            disabled={yaRegistrado || guardando || loadingNomina}
            onClick={handleGuardar}
            className="inline-flex items-center justify-center gap-2 rounded-md bg-sky-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-sky-700 disabled:opacity-60 disabled:cursor-not-allowed transition-colors"
          >
            <Save className="w-4 h-4" aria-hidden="true" />
            {guardando
              ? 'Guardando...'
              : yaRegistrado
                ? 'Pase ya registrado'
                : 'Guardar pase de lista'}
          </button>
        )}
      </div>

      {loadingGrupos ? (
        <div className="bg-white border border-slate-200 rounded-lg shadow-sm p-6 text-sm text-slate-500">
          Cargando tus grupos...
        </div>
      ) : !grupos.length ? (
        <div className="bg-white border border-slate-200 rounded-lg shadow-sm p-6 text-sm text-slate-600">
          No tienes grupos asignados actualmente. Contacta a Dirección para la asignación de grupos.
        </div>
      ) : (
        <>
          <div className="flex flex-wrap gap-2" role="tablist" aria-label="Mis grupos asignados">
            {grupos.map((g) => {
              const activo = g.id === grupoActivoId;
              return (
                <button
                  key={g.id}
                  type="button"
                  role="tab"
                  aria-selected={activo}
                  onClick={() => setGrupoActivoId(g.id)}
                  className={`rounded-md px-3.5 py-2 text-sm font-medium transition-colors ${
                    activo
                      ? 'bg-slate-900 text-white shadow-sm'
                      : 'bg-white border border-slate-200 text-slate-700 hover:bg-slate-50'
                  }`}
                >
                  <span className="font-semibold">{g.clave}</span> · {g.nombre}
                </button>
              );
            })}
          </div>

          {yaRegistrado && (
            <div
              role="status"
              className="flex items-center gap-2 rounded-md border border-sky-200 bg-sky-50 px-4 py-3 text-sm text-sky-800"
            >
              <CheckCircle2 className="w-4 h-4 shrink-0" aria-hidden="true" />
              El pase de lista de hoy para este grupo ya fue registrado y está en modo solo lectura.
            </div>
          )}

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

          <TablaPaseLista
            alumnos={alumnos}
            estados={estados}
            yaRegistrado={yaRegistrado}
            loading={loadingNomina}
            onCambiarEstado={handleCambiarEstado}
          />
        </>
      )}
    </div>
  );
}
