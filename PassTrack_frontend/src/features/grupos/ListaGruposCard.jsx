import { GraduationCap, UserPlus, Users } from 'lucide-react';

export default function ListaGruposCard({ grupos, loading, onGestionarAlumnos }) {
  if (loading) {
    return (
      <div className="bg-white border border-slate-200 rounded-lg shadow-sm p-8 text-center text-sm text-slate-500">
        Cargando grupos académicos...
      </div>
    );
  }

  if (!grupos.length) {
    return (
      <div className="bg-white border border-slate-200 rounded-lg shadow-sm p-8 text-center text-sm text-slate-500">
        Aún no hay grupos registrados. Cree el primer grupo asignando un docente titular.
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      {grupos.map((grupo) => (
        <article
          key={grupo.id}
          className="bg-white border border-slate-200 rounded-lg shadow-sm p-5 flex flex-col justify-between gap-4"
        >
          <div className="space-y-2">
            <div className="flex items-center justify-between gap-2">
              <span className="inline-flex items-center rounded-md bg-sky-50 px-2 py-0.5 text-xs font-semibold text-sky-700 border border-sky-200">
                {grupo.clave}
              </span>
              <span className="inline-flex items-center gap-1 text-xs font-medium text-slate-600">
                <Users className="w-3.5 h-3.5 text-slate-400" aria-hidden="true" />
                {grupo.totalAlumnos} {grupo.totalAlumnos === 1 ? 'alumno' : 'alumnos'}
              </span>
            </div>

            <h3 className="text-base font-semibold text-slate-900">{grupo.nombre}</h3>

            <p className="inline-flex items-center gap-1.5 text-xs text-slate-600">
              <GraduationCap className="w-4 h-4 text-slate-400" aria-hidden="true" />
              Titular: <span className="font-medium text-slate-800">{grupo.docenteNombre}</span>
            </p>
          </div>

          <button
            type="button"
            onClick={() => onGestionarAlumnos(grupo)}
            className="w-full inline-flex items-center justify-center gap-1.5 rounded-md border border-slate-200 bg-slate-50 px-3 py-2 text-xs font-medium text-slate-800 hover:bg-slate-100 transition-colors"
          >
            <UserPlus className="w-3.5 h-3.5" aria-hidden="true" />
            Matricular / Ver alumnos
          </button>
        </article>
      ))}
    </div>
  );
}
