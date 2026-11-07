import { http } from './http';
export const reminderService = { create: (payload) => http('/v1/reminders',{method:'POST',body:payload}) };
