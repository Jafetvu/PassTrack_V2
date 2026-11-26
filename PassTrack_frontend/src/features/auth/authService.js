import axiosClient from '../../api/axiosClient';

export async function loginRequest({ email, password }) {
  const { data } = await axiosClient.post('/auth/login', {
    email: email.trim(),
    password,
  });
  return data;
}
