import axios from "axios";

// Backend API base URL (from your .env)
const API_URL = `${import.meta.env.VITE_API_URL}/auth`;

// Store JWT in localStorage
const storeToken = (token: string) => {
  localStorage.setItem("token", token);
};

// Remove JWT
export const clearToken = () => {
  localStorage.removeItem("token");
};

// Get JWT
export const getToken = () => {
  return localStorage.getItem("token");
};

// Sign up new user
export const signUp = async (name: string, email: string, password: string) => {
  const response = await axios.post(`${API_URL}/signup`, {
    name,
    email,
    password,
  });
  return response.data;
};

// Sign in existing user
export const signIn = async (email: string, password: string) => {
  const response = await axios.post(`${API_URL}/signin`, {
    email,
    password,
  });

  const { token } = response.data;
  storeToken(token);

  return response.data;
};

// Sign out current user
export const signOut = async () => {
  const token = getToken();

  if (!token) return;

  await axios.post(
    `${API_URL}/signout`,
    {},
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );

  clearToken();
};

// Check if the user is authenticated
export const isAuthenticated = () => {
  return !!getToken();
};
