import { http } from './http.js';

const clock = new Intl.DateTimeFormat('vi-VN', {
  hour: '2-digit', minute: '2-digit', hourCycle: 'h23', timeZone: 'Asia/Ho_Chi_Minh'
});
export const flightService = {
  search({ from, to, departure, cabin = 'ECONOMY', passengers = 1, page = 0, signal }) {
    const query = new URLSearchParams({ origin: from, destination: to, departureDate: departure, fareClass: cabin, quantity: passengers, page, size: 20 });
    return http(`/v1/flights?${query}`, { useAuth: false, signal });
  },
  detail(id, { signal } = {}) {
    return http(`/v1/flights/${encodeURIComponent(id)}`, { useAuth: false, signal });
  }
};
export function currentFares(flight, now = Date.now()) {
  return (flight?.fares || []).filter(fare => Date.parse(fare.validFrom) <= now && now < Date.parse(fare.validTo));
}
export function flightCardData(flight, cabin) {
  const fare = currentFares(flight).find(item => item.fareClass === cabin);
  if (!fare) return null;
  const minutes = Math.round((Date.parse(flight.arrivalTime) - Date.parse(flight.departureTime)) / 60000);
  return { ...flight, cabin, price: Number(fare.basePrice), currency: fare.currency,
    depart: clock.format(new Date(flight.departureTime)), arrive: clock.format(new Date(flight.arrivalTime)),
    duration: `${Math.floor(minutes / 60)}h ${minutes % 60}m` };
}
export const cabinName = cabin => cabin === 'ECONOMY' ? 'Phổ thông' : cabin === 'BUSINESS' ? 'Thương gia' : 'Hạng nhất';
