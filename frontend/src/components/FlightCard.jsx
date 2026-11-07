import { ArrowRight, BriefcaseBusiness, Clock3, Plane, Sparkles } from 'lucide-react';
import { Link } from 'react-router-dom';
import { money } from '../data/cities';
export default function FlightCard({flight,from,to,departure,passengers}){
  const params=new URLSearchParams({from,to,departure,passengers:String(passengers)});
  return <article className="flight-card"><div className="flight-card-main"><div className="airline"><div className="airline-logo" style={{color:flight.accent,background:`${flight.accent}12`}}>{flight.short}</div><div><strong>{flight.airline}</strong><span>{flight.number} · {flight.cabin==='ECONOMY'?'Phổ thông':flight.cabin==='BUSINESS'?'Thương gia':'Hạng nhất'}</span></div></div>
  <div className="flight-path"><div><b>{flight.depart}</b><small>{from}</small></div><div className="flight-connector"><span>{flight.duration}</span><div className="path-line"><i/><Plane size={18} fill="currentColor"/><i/></div><small>{flight.stops===0?'Bay thẳng':`${flight.stops} điểm dừng`}</small></div><div><b>{flight.arrive}</b><small>{to}</small></div></div>
  <div className="flight-benefit"><BriefcaseBusiness size={15}/>{flight.benefit}</div></div>
  <div className="flight-price"><span className="flight-price-label">Giá tham khảo / khách</span><strong>{money(flight.price)}</strong><span className="flight-price-note">Chưa bao gồm thuế, phí</span><Link to={`/booking/${flight.id}?${params}`} className="btn btn-primary btn-flight">Xem chi tiết <ArrowRight size={16}/></Link></div></article>
}
