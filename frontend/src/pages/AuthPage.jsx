import { useState } from 'react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { ArrowLeft, ArrowRight, Eye, EyeOff, LockKeyhole, Mail, Plane, ShieldCheck, Sparkles } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
export default function AuthPage({mode='login'}){
  const register=mode==='register';const {isAuthenticated,login,register:doRegister}=useAuth();const location=useLocation();const navigate=useNavigate();const toast=useToast();
  const [form,setForm]=useState({email:'',password:'',phone:''});const [show,setShow]=useState(false);const [busy,setBusy]=useState(false);
  if(isAuthenticated)return <Navigate to="/" replace/>;
  const change=(key,val)=>setForm(f=>({...f,[key]:val}));


    const submit = async (e) => {
        e.preventDefault();
        setBusy(true);

        try {
            if (register) {
                if (form.password.length < 8) {
                    throw new Error('Mật khẩu phải có ít nhất 8 ký tự.');
                }

                // Đăng ký tài khoản
                await doRegister(form.email, form.password, form.phone);

                toast('Đăng ký thành công! Vui lòng đăng nhập.');

                // Đăng ký xong chuyển về trang đăng nhập
                navigate('/login', { replace: true });

            } else {
                // Đăng nhập tài khoản
                await login(form.email, form.password);

                toast('Đăng nhập thành công!');

                // Đăng nhập xong chuyển về trang chủ
                navigate('/', { replace: true });
            }

        } catch (err) {
            toast(err.message || 'Có lỗi xảy ra, vui lòng thử lại.', 'error');
        } finally {
            setBusy(false);
        }
    };


    return <section className="auth-section"><div className="shell auth-shell"><div className="auth-visual"><div className="auth-visual-overlay"/><Link to="/" className="auth-back"><ArrowLeft size={17}/> Về trang chủ</Link><div className="auth-visual-text"><span className="auth-kicker"><Sparkles size={15}/> SKYPORT STORIES</span><h2>Mỗi hành trình<br/>đều có một<br/><i>câu chuyện.</i></h2><p>Đăng nhập để tiếp tục chuyến đi của riêng bạn.</p></div><div className="auth-visual-bottom"><span>✈ KHÁM PHÁ THẾ GIỚI</span><span>01 — 05</span></div></div>
  <div className="auth-panel"><span className="auth-welcome"><Plane size={17}/> CHÀO MỪNG ĐẾN SKYPORT</span><h1>{register?'Tạo tài khoản mới':'Rất vui được gặp lại!'}</h1><p>{register?'Chỉ vài bước nữa để bắt đầu hành trình của bạn.':'Đăng nhập để quản lý vé và lên kế hoạch bay.'}</p>
  <form onSubmit={submit} className="auth-form"><label className="field"><span>Email của bạn</span><div className="with-icon"><Mail size={18}/><input type="email" required placeholder="you@example.com" value={form.email} onChange={e=>change('email',e.target.value)}/></div></label>
  {register&&<label className="field"><span>Số điện thoại</span><input type="tel" placeholder="09xxxxxxxx (không bắt buộc)" value={form.phone} onChange={e=>change('phone',e.target.value)}/></label>}
  <label className="field"><span>Mật khẩu</span><div className="with-icon"><LockKeyhole size={18}/><input type={show?'text':'password'} required placeholder={register?'Tối thiểu 8 ký tự':'Nhập mật khẩu của bạn'} value={form.password} onChange={e=>change('password',e.target.value)}/><button type="button" className="show-password" onClick={()=>setShow(!show)} aria-label="Hiện/ẩn mật khẩu">{show?<EyeOff size={19}/>:<Eye size={19}/>}</button></div></label>
  <button className="btn btn-primary auth-submit" disabled={busy} type="submit">{busy?'Đang xử lý...':register?'Đăng ký tài khoản':'Đăng nhập'} <ArrowRight size={18}/></button></form>
  <div className="auth-switch">{register?<>Bạn đã có tài khoản? <Link to="/login">Đăng nhập ngay</Link></>:<>Chưa có tài khoản? <Link to="/register">Đăng ký miễn phí</Link></>}</div><div className="auth-safety"><ShieldCheck size={17}/> Kết nối được bảo vệ bởi hệ thống xác thực của dự án.</div>
  </div></div></section>
}
