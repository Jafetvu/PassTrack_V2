import { useCallback, useEffect, useMemo, useState } from 'react';
import { obtenerMisAsistencias } from './asistenciasService';
import HistorialAsistenciasTable from './HistorialAsistenciasTable';
import ResumenPorcentajeCard from './ResumenPorcentajeCard';

export default function MisAsistenciasContainer({ renderJustificanteModal }) {
  const [resumenes, setResumenes] = useState([]);
  const [grupoSeleccionadoId, setGrupoSeleccionadoId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [asistenciaParaJustificar, setAsistenciaParaJustificar] = useState(null);
  const [aviso, setAviso] = useState('');

  const cargarResumen = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await obtenerMisAsistencias();
      const lista = Array.isArray(data) ? data : [];
      setResumenes(lista);
      setGrupoSeleccionadoId((actual) => {
        if (actual && lista.some((r) => r.grupoId === actual)) return actual;
        return lista.length > 0 ? lista[0].grupoId : null;
      });
    } catch (err) {
      setError(err?.response?.data?.message || 'No fue posible cargar tu historial de asistencias.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    cargarResumen();
  }, [cargarResumen]);

  const resumenActivo = useMemo(
    () => resumenes.find((r) => r.grupoId === grupoSeleccionadoId) || null,
    [resumenes, grupoSeleccionadoId],
  );

  const handleSolicitarJustificante = (registro) => {
    if (renderJustificanteModal) {
      setAsistenciaParaJustificar(registro);
    } else {
      setAviso(
        `Solicitud preparada para la sesión del ${registro.fecha}. El envío de justificantes se habilitará en el módulo de Justificantes.`,
      );
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-slate-900">Mis Asistencias</h1>
        <p className="mt-1 text-sm text-slate-600">
          Consulta tu porcentaje de asistencia por materia y solicita justificantes para faltas o retardos.
        </p>
      </div>

      {error && (
        <div
          role="alert"
          className="rounded-md border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700"
        >
          {error}
        </div>
      )}

      {aviso && (
        <div
          role="status"
          className="rounded-md border border-sky-200 bg-sky-50 px-4 py-3 text-sm text-sky-800"
        >
          {aviso}
        </div>
      )}

      {loading ? (
        <div className="bg-white border border-slate-200 rounded-lg shadow-sm p-8 text-center text-sm text-slate-500">
          Cargando tus materias y asistencias...
        </div>
      ) : !resumenes.length ? (
        <div className="bg-white border border-slate-200 rounded-lg shadow-sm p-8 text-center text-sm text-slate-500">
          Aún no te encuentras inscrito en ningún grupo académico.
        </div>
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {resumenes.map((res) => (
              <ResumenPorcentajeCard
                key={res.grupoId}
                resumen={res}
                seleccionado={res.grupoId === grupoSeleccionadoId}
                onSeleccionar={setGrupoSeleccionadoId}
              />
            ))}
          </div>

          {resumenActivo && (
            <section className="space-y-3">
              <h2 className="text-lg font-semibold text-slate-900">
                Historial · {resumenActivo.grupoClave} — {resumenActivo.grupoNombre}
              </h2>
              <HistorialAsistenciasTable
                historial={resumenActivo.historial}
                onSolicitarJustificante={handleSolicitarJustificante}
              />
            </section>
          )}
        </>
      )}

      {renderJustificanteModal &&
        asistenciaParaJustificar &&
        renderJustificanteModal({
          asistencia: asistenciaParaJustificar,
          onClose: () => setAsistenciaParaJustificar(null),
          onSuccess: async () => {
            setAsistenciaParaJustificar(null);
            await cargarResumen();
          },
        })}
    </div>
  );
}
