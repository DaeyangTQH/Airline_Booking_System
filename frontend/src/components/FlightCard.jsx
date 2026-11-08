import { ArrowRight, Plane } from 'lucide-react';
import { Link } from 'react-router-dom';
import { money } from '../data/cities';
import { cabinName } from '../services/flightService';

export default function FlightCard({ flight, departure, passengers }) {
  const params = new URLSearchParams({ from: flight.originCode, to: flight.destinationCode,
    departure, passengers: String(passengers), cabin: flight.cabin });
  return <article className="flight-card">
    <div className="flight-card-main">
      <div className="airline"><div className="airline-logo"><Plane size={22}/></div>
        <div><strong>{flight.flightCode}</strong><span>{flight.aircraftModel} · {cabinName(flight.cabin)}</span></div>
      </div>
      <div className="flight-path">
        <div><b>{flight.depart}</b><small>{flight.originCode}</small></div>
        <div className="flight-connector"><span>{flight.duration}</span><div className="path-line"><i/><Plane size={18}/><i/></div><small>Chuyến bay trực tiếp</small></div>
        <div><b>{flight.arrive}</b><small>{flight.destinationCode}</small></div>
      </div>
      <div className="flight-benefit">{flight.status === 'DELAYED' ? 'Đã điều chỉnh lịch bay' : 'Theo lịch trình'}</div>
      <div className="flight-benefit">{flight.availableSeats ?? '—'} / {flight.classCapacity ?? '—'} ghế chưa giữ · {cabinName(flight.cabin)}</div>
    </div>
    <div className="flight-price"><span className="flight-price-label">Giá cơ bản / khách</span>
      <strong>{money(flight.price, flight.currency)}</strong><span className="flight-price-note">Giờ hiển thị theo múi giờ Việt Nam</span>
      <Link to={`/booking/${flight.id}?${params}`} className="btn btn-primary btn-flight">Xem chi tiết <ArrowRight size={16}/></Link>
    </div>
  </article>;
}
