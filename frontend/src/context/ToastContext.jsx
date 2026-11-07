import { createContext, useContext, useState, useCallback } from 'react';
import { CheckCircle2, AlertCircle, X } from 'lucide-react';
const ToastContext = createContext(null);
export function ToastProvider({children}) {
  const [alerts,setAlerts] = useState([]);
  const dismiss = useCallback(id=>setAlerts(prev=>prev.filter(item=>item.id !== id)),[]);
  const toast = useCallback((message,type='success')=>{
    const id = `${Date.now()}-${Math.random()}`;
    setAlerts(prev=>[...prev.slice(-2),{id,message,type}]);
    setTimeout(()=>dismiss(id),4400);
  },[dismiss]);
  return <ToastContext.Provider value={toast}>{children}<div className="toast-stack" aria-live="polite">
    {alerts.map(a=><div className={`toast toast-${a.type}`} key={a.id}>{a.type==='error'?<AlertCircle size={18}/>:<CheckCircle2 size={18}/>}<span>{a.message}</span><button className="icon-btn" onClick={()=>dismiss(a.id)} aria-label="Đóng"><X size={15}/></button></div>)}
  </div></ToastContext.Provider>;
}
export const useToast = () => useContext(ToastContext);
