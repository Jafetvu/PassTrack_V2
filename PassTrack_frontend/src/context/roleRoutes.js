export function getHomeRouteByRole(rol) {
  switch (rol) {
    case 'DIRECTOR':
      return '/director';
    case 'DOCENTE':
      return '/docente';
    case 'ALUMNO':
      return '/alumno';
    default:
      return '/login';
  }
}
