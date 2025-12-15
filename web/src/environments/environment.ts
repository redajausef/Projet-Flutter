export const environment = {
  production: false,
  // En Docker, l'API est accessible via le proxy nginx sur /api
  // En développement local, on utilise directement le backend
  apiUrl: '/api'
};
