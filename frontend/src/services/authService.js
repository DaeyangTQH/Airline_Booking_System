import { http } from './http';
export const authService = {
  login: (email,password) => http('/auth/login',{method:'POST',body:{email,password},useAuth:false}),
  register: (email,password,phone) => http('/auth/register',{method:'POST',body:{email,password,phone},useAuth:false})
};
