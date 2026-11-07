import { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { ArrowUpRight, ChevronDown, Menu, Plane, UserRound, X, LogOut } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
export default function Header(){
  const {session,logout,isAdmin}=useAuth();
  const [mobile,setMobile]=useState(false);const [account,setAccount]=useState(false);const navigate=useNavigate();
  const close=()=>setMobile(false);
  const signout=()=>{logout();setAccount(false);navigate('/');};
  return <header className="site-header"><div className="shell nav-shell">
    <Link to="/" className="brand" onClick={close} aria-label="SkyPort trang chủ"><span className="brand-mark"><Plane size={21} strokeWidth={2.6}/></span><span>sky<span>port</span><small>.</small></span></Link>
    <nav className={`top-nav ${mobile?'open':''}`} aria-label="Điều hướng chính">
      <NavLink onClick={close} end to="/">Trang chủ</NavLink>
      <NavLink onClick={close} to="/flights">Vé máy bay</NavLink>
      <NavLink onClick={close} to="/trips">Chuyến đi của tôi</NavLink>
      <NavLink onClick={close} to="/passengers">Hành khách</NavLink>
      {isAdmin&&<NavLink onClick={close} to="/admin/users">Quản trị</NavLink>}
    </nav>
    <div className="nav-actions">
      {session?<div className="account-wrap">
        <button className="nav-account" onClick={()=>setAccount(!account)} aria-expanded={account}>
          <span className="avatar">{(session.email||'U')[0].toUpperCase()}</span><span className="account-email">{session.email?.split('@')[0]}</span><ChevronDown size={15}/>
        </button>
        {account&&<div className="account-menu"><Link to="/profile" onClick={()=>setAccount(false)}><UserRound size={16}/> Tài khoản</Link><Link to="/reminders" onClick={()=>setAccount(false)}>Nhắc chuyến bay</Link><button onClick={signout}><LogOut size={16}/> Đăng xuất</button></div>}
      </div>:<><Link className="nav-login" to="/login">Đăng nhập</Link><Link className="btn btn-small btn-dark nav-signup" to="/register">Đăng ký <ArrowUpRight size={15}/></Link></>}
      <button className="mobile-menu-btn" aria-label="Mở menu" onClick={()=>setMobile(!mobile)}>{mobile?<X/>:<Menu/>}</button>
    </div>
  </div></header>;
}
