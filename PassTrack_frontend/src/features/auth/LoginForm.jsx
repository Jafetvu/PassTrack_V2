import { AlertCircle, Lock, Mail, ShieldCheck } from 'lucide-react';

export default function LoginForm({
  email,
  password,
  error,
  isSubmitting,
  onEmailChange,
  onPasswordChange,
  onSubmit,
}) {
  return (
    <main className="min-h-screen flex items-center justify-center bg-slate-50 px-4 py-12">
      <section className="w-full max-w-md bg-white border border-slate-200 rounded-lg shadow-sm p-8">
        <header className="mb-8">
          <div className="inline-flex items-center gap-2 text-sky-600 mb-3">
            <ShieldCheck className="w-6 h-6" aria-hidden="true" />
            <span className="text-xs font-semibold uppercase tracking-wider">
              Control Escolar
            </span>
          </div>
          <h1 className="text-3xl font-bold tracking-tight text-slate-900">
            PassTrack
          </h1>
          <p className="mt-1.5 text-sm text-slate-600">
            Inicia sesión con tu cuenta institucional para continuar.
          </p>
        </header>

        {error && (
          <div
            role="alert"
            className="mb-6 flex items-start gap-2.5 rounded-md border border-rose-200 bg-rose-50 px-3.5 py-3 text-sm text-rose-600"
          >
            <AlertCircle className="w-4 h-4 mt-0.5 shrink-0" aria-hidden="true" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={onSubmit} className="space-y-5" noValidate>
          <div>
            <label
              htmlFor="email"
              className="block text-sm font-medium text-slate-900 mb-1.5"
            >
              Correo institucional
            </label>
            <div className="relative">
              <Mail
                className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none"
                aria-hidden="true"
              />
              <input
                id="email"
                name="email"
                type="email"
                autoComplete="email"
                required
                value={email}
                onChange={(e) => onEmailChange(e.target.value)}
                disabled={isSubmitting}
                placeholder="usuario@passtrack.edu"
                className="w-full rounded-md border border-slate-200 bg-white pl-9 pr-3 py-2 text-sm text-slate-900 placeholder:text-slate-400 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-600/20 disabled:bg-slate-50 disabled:text-slate-500"
              />
            </div>
          </div>

          <div>
            <label
              htmlFor="password"
              className="block text-sm font-medium text-slate-900 mb-1.5"
            >
              Contraseña
            </label>
            <div className="relative">
              <Lock
                className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none"
                aria-hidden="true"
              />
              <input
                id="password"
                name="password"
                type="password"
                autoComplete="current-password"
                required
                value={password}
                onChange={(e) => onPasswordChange(e.target.value)}
                disabled={isSubmitting}
                placeholder="••••••••"
                className="w-full rounded-md border border-slate-200 bg-white pl-9 pr-3 py-2 text-sm text-slate-900 placeholder:text-slate-400 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-600/20 disabled:bg-slate-50 disabled:text-slate-500"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full rounded-md bg-sky-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-sky-700 focus:outline-none focus:ring-2 focus:ring-sky-600/30 disabled:opacity-60 disabled:cursor-not-allowed transition-colors"
          >
            {isSubmitting ? 'Verificando credenciales...' : 'Iniciar sesión'}
          </button>
        </form>
      </section>
    </main>
  );
}
