import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { getHomeRouteByRole } from '../../context/roleRoutes';
import { loginRequest } from './authService';
import LoginForm from './LoginForm';

export default function LoginContainer() {
  const { isAuthenticated, rol, login } = useAuth();
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  if (isAuthenticated) {
    return <Navigate to={getHomeRouteByRole(rol)} replace />;
  }

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');

    if (!email.trim() || !password) {
      setError('Ingresa tu correo electrónico y contraseña.');
      return;
    }

    setIsSubmitting(true);
    try {
      const authData = await loginRequest({ email, password });
      login(authData);
      navigate(getHomeRouteByRole(authData.rol), { replace: true });
    } catch (err) {
      const serverMessage = err.response?.data?.message;
      if (err.response?.status === 401) {
        setError(serverMessage || 'Credenciales inválidas o usuario inactivo.');
      } else if (err.response?.status === 400) {
        setError(serverMessage || 'Verifica el formato de los datos ingresados.');
      } else {
        setError('No fue posible conectar con el servidor. Intenta nuevamente.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <LoginForm
      email={email}
      password={password}
      error={error}
      isSubmitting={isSubmitting}
      onEmailChange={setEmail}
      onPasswordChange={setPassword}
      onSubmit={handleSubmit}
    />
  );
}
