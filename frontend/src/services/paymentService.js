import { http } from './http';

export const paymentService = {
  status: (paymentId, signal) =>
    http(`/v1/payments/${paymentId}/status`, { useAuth: false, signal })
};
