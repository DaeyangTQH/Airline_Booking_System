import { Link } from 'react-router-dom';import { ArrowLeft, Compass } from 'lucide-react';
export default function NotFoundPage(){return <div className="not-found shell"><Compass size={60}/><h1>404</h1><h2>Có vẻ bạn đã bay lạc đường!</h2><p>Trang bạn tìm kiếm không tồn tại hoặc đã được chuyển đi.</p><Link to="/" className="btn btn-primary"><ArrowLeft size={18}/> Về trang chủ</Link></div>}
