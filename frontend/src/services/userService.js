import { http } from './http';
export const userService = {
  me: () => http('/users/me'),
  update: (email,phone) => http('/users/me',{method:'PUT',body:{email,phone}}),
  changePassword: (currentPassword,newPassword) => http('/users/me/password',{method:'PUT',body:{currentPassword,newPassword}}),
  listAdmin: (page=0,keyword='') => http(`/admin/users?${new URLSearchParams({page:String(page),size:'10',keyword})}`),
  setActive: (id,active) => http(`/admin/users/${id}/status`,{method:'PATCH',body:{active}})
};
