import { http } from './http';
export const bookingService = {
  mine: () => http('/v1/bookings/me',{booking:true}),
  detail: (id) => http(`/v1/bookings/${id}`,{booking:true}),
  byReference: (ref) => http(`/v1/bookings/ref/${encodeURIComponent(ref)}`,{booking:true}),
  create: (payload) => http('/v1/bookings',{method:'POST',body:payload,booking:true}),
  initiatePayment: (bookingId) => http(`/v1/bookings/${bookingId}/payment`,{method:'POST',booking:true}),
  listPassengers: (bookingId) => http(`/v1/bookings/${bookingId}/passengers`,{booking:true}),
  addPassenger: (bookingId, payload) => http(`/v1/bookings/${bookingId}/passengers`,{
    method:'POST',
    body:payload,
    booking:true
  })
};
