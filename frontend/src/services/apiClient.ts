const BASE_URL = '/api';

export async function apiClient<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const token = localStorage.getItem('riwi_access_token');

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`${BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  if (response.status === 401 && endpoint !== '/auth/login' && endpoint !== '/auth/refresh') {
    // Attempt token refresh
    const refreshToken = localStorage.getItem('riwi_refresh_token');
    if (refreshToken) {
      try {
        const refreshRes = await fetch(`${BASE_URL}/auth/refresh`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ refreshToken }),
        });

        if (refreshRes.ok) {
          const data = await refreshRes.json();
          localStorage.setItem('riwi_access_token', data.accessToken);
          localStorage.setItem('riwi_refresh_token', data.refreshToken);

          // Retry original request with new token
          headers['Authorization'] = `Bearer ${data.accessToken}`;
          const retryRes = await fetch(`${BASE_URL}${endpoint}`, {
            ...options,
            headers,
          });
          if (!retryRes.ok) {
            const errData = await retryRes.json().catch(() => ({}));
            throw new Error(errData.message || 'Request failed');
          }
          return retryRes.json();
        }
      } catch {
        // Refresh failed, clear session
        localStorage.removeItem('riwi_access_token');
        localStorage.removeItem('riwi_refresh_token');
        localStorage.removeItem('riwi_user');
        window.location.reload();
      }
    }
  }

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || `HTTP Error ${response.status}`);
  }

  // Handle 204 No Content
  if (response.status === 204) {
    return {} as T;
  }

  return response.json();
}
