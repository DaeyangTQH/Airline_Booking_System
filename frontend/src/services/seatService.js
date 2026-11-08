import { http } from './http.js';

export const seatService = {
  async list(flightId, { seatClass, signal } = {}) {
    const seats = [];
    for (let page = 0; ; page += 1) {
      const query = new URLSearchParams({ page, size: 100 });
      if (seatClass) query.set('seatClass', seatClass);
      const batch = await http(`/v1/flights/${encodeURIComponent(flightId)}/seats?${query}`, {
        useAuth: false, signal
      });
      if (!Array.isArray(batch)) throw new Error('Danh sách ghế không hợp lệ.');
      seats.push(...batch);
      if (batch.length < 100) return seats.sort((a, b) => a.seatNumber.localeCompare(b.seatNumber, 'en', { numeric: true }));
    }
  }
};
