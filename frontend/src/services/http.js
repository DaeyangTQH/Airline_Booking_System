const SESSION_KEY = 'skyport_v2_session';
export const readSession = () => { try { return JSON.parse(sessionStorage.getItem(SESSION_KEY) || 'null'); } catch { return null; } };
export const writeSession = (session) => session ? sessionStorage.setItem(SESSION_KEY, JSON.stringify(session)) : sessionStorage.removeItem(SESSION_KEY);

export async function http(path, { method='GET', body, useAuth=true, booking=false, signal }={}) {
  const session = readSession();
  const headers = { Accept: 'application/json' };
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (useAuth && session?.accessToken) headers.Authorization = `Bearer ${session.accessToken}`;
  // BookingController hiện đọc X-User-Id, gateway vẫn phải bảo vệ và xác thực header này.
  if (booking && session?.userId) headers['X-User-Id'] = session.userId;
  let response;
  try {
    response = await fetch(`/api${path}`, { method, headers, body: body !== undefined ? JSON.stringify(body) : undefined, signal });
  } catch(err) {
    if (err?.name === 'AbortError') throw err;
    throw new Error('Không kết nối được API Gateway. Hãy chạy backend tại localhost:8080.');
  }
  const raw = await response.text();
  let data;
  try { data = raw ? JSON.parse(raw) : null; } catch { data = raw; }
  if (!response.ok) {
    if (response.status === 401 && useAuth) window.dispatchEvent(new Event('skyport-auth-invalid'));
    const detail = typeof data === 'object' && data ? (data.message || data.detail || data.error || data.title) : data;
    const e = new Error(detail || `Yêu cầu thất bại (HTTP ${response.status}).`);
    e.status = response.status;
    throw e;
  }
  return data;
}
