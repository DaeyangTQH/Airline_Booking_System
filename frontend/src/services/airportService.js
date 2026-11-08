import { http } from './http.js';

export const airportService = {
  async list({ signal } = {}) {
    const airports = [];
    const size = 100;
    for (let page = 0; ; page += 1) {
      const batch = await http(`/v1/airports?${new URLSearchParams({ page, size })}`, {
        useAuth: false,
        signal
      });
      if (!Array.isArray(batch)) throw new Error('Danh sách sân bay trả về không hợp lệ.');
      airports.push(...batch.map(({ id, code, name, city, country }) => ({
        id, code, airport: name, city, country
      })));
      if (batch.length < size) return airports;
    }
  }
};
