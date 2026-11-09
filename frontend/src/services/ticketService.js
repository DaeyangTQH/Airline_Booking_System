import { http } from './http';

export const ticketService = {
  get: (ticketId) => http(`/v1/tickets/${ticketId}`, { booking: true }),
  assignSeat: (ticketId, seatId) => http(`/v1/tickets/${ticketId}/seat`, {
    method: 'PUT',
    body: { seatId },
    booking: true
  }),
  removeSeat: (ticketId) => http(`/v1/tickets/${ticketId}/seat`, {
    method: 'DELETE',
    booking: true
  })
};
