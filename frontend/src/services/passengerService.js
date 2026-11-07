import { http } from './http';
export const passengerService = {
  list: () => http('/passengers'),
  create: (payload) => http('/passengers',{method:'POST',body:payload}),
  update: (id,payload) => http(`/passengers/${id}`,{method:'PUT',body:payload}),
  remove: (id) => http(`/passengers/${id}`,{method:'DELETE'})
};
