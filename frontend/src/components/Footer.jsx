import { Link } from 'react-router-dom';
import { ArrowUpRight, Globe2, Instagram, Plane, Send } from 'lucide-react';
export default function Footer(){return <footer className="site-footer"><div className="shell">
  <div className="footer-main"><div className="footer-about"><Link to="/" className="brand brand-footer"><span className="brand-mark"><Plane size={21}/></span><span>sky<span>port</span><small>.</small></span></Link><p>Đi xa hơn, dễ dàng hơn. Một nơi để bắt đầu mọi hành trình của bạn.</p><div className="footer-social"><span><Globe2 size={17}/></span><span><Instagram size={17}/></span><span><Send size={17}/></span></div></div>
    <div className="footer-col"><h4>Khám phá</h4><Link to="/flights">Tìm chuyến bay</Link><Link to="/?destination=DAD">Điểm đến nổi bật</Link><Link to="/trips">Chuyến đi của tôi</Link></div>
    <div className="footer-col"><h4>Tài khoản</h4><Link to="/profile">Hồ sơ</Link><Link to="/passengers">Hành khách</Link><Link to="/reminders">Nhắc lịch bay</Link></div>
    <div className="footer-news"><h4>Sẵn sàng cất cánh?</h4><p>Khám phá hành trình tiếp theo của bạn ngay hôm nay.</p><Link to="/flights" className="btn btn-lime">Tìm chuyến bay <ArrowUpRight size={16}/></Link></div>
  </div><div className="footer-bottom"><span>© {new Date().getFullYear()} SkyPort. Airline Booking System – Dự án học tập.</span><span>Thiết kế để những chuyến đi trở nên đơn giản.</span></div>
</div></footer>}
