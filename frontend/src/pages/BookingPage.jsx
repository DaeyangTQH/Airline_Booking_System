import { useEffect, useState } from 'react';
import { useParams, useSearchParams, Link } from 'react-router-dom';
import { ArrowLeft, ArrowRight, BadgeInfo, Plane, Ticket } from 'lucide-react';
import Busy from '../components/Busy';
import { money, uuidValid } from '../data/cities';
import { flightService, currentFares, flightCardData, cabinName } from '../services/flightService';


export default function BookingPage() {
  const { flightId } = useParams(), [params] = useSearchParams();
  const [flight, setFlight] = useState(null), [cabin, setCabin] = useState(params.get('cabin') || 'ECONOMY');
  const [passengers, setPassengers] = useState(Math.max(1, Math.min(9, Number(params.get('passengers')) || 1)));
  const [loading, setLoading] = useState(true), [error, setError] = useState(''), [reload, setReload] = useState(0);
  useEffect(() => {
    const controller = new AbortController();
    setLoading(true); setError(''); setFlight(null);
    if (!uuidValid(flightId)) {
      setError('Liên kết chuyến bay không hợp lệ. Vui lòng chọn lại chuyến bay.'); setLoading(false);
      return () => controller.abort();
    }
    flightService.detail(flightId, { signal: controller.signal })
      .then(data => { if (!controller.signal.aborted) setFlight(data); })
      .catch(e => { if (!controller.signal.aborted) setError(e.message); })
      .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [flightId, reload]);
  const fares = currentFares(flight), card = flight && flightCardData(flight, cabin);
  const searchParams = flight ? new URLSearchParams({
    from: flight.originCode, to: flight.destinationCode,
    departure: new Date(flight.departureTime).toLocaleDateString('en-CA', { timeZone: 'Asia/Ho_Chi_Minh' }),
    passengers: String(passengers), cabin, trip: 'oneway'
  }) : null;
  const back = searchParams ? `/flights?${searchParams}` : '/flights';
  const dateTime = value => new Date(value).toLocaleString('vi-VN', { timeZone: 'Asia/Ho_Chi_Minh' });
  return <section className="booking-page shell">
    <Link className="back-link" to={back}><ArrowLeft size={17}/> Quay về danh sách chuyến bay</Link>
    <div className="booking-heading"><div><span className="eyebrow">THÔNG TIN HÀNH TRÌNH</span><h1>Chi tiết chuyến bay</h1><p>Kiểm tra lịch trình và giá vé hiện hành.</p></div></div>
    {loading ? <Busy/> : error ? <div className="inline-error" role="alert">{error} <button onClick={() => setReload(value => value + 1)}>Thử lại</button></div> : flight && <>
      <div className="demo-banner booking-demo"><BadgeInfo size={22}/><p>Đây là dữ liệu mẫu để thử nghiệm. Chức năng kiểm tra ghế và xác nhận đặt chỗ đang được hoàn thiện.</p></div>
      <div className="booking-layout"><div className="booking-form-panel">
        <div className="form-section-title"><span className="form-step-icon"><Ticket size={21}/></span><div><h2>{flight.flightCode}</h2><p>{flight.aircraftModel} · {flight.status}</p></div></div>
        <div className="confirm-list">
          <div><span>Điểm đi</span><b>{flight.originCity} ({flight.originCode})</b></div>
          <div><span>Điểm đến</span><b>{flight.destinationCity} ({flight.destinationCode})</b></div>
          <div><span>Khởi hành</span><b>{dateTime(flight.departureTime)}</b></div>
          <div><span>Đến nơi</span><b>{dateTime(flight.arrivalTime)}</b></div>
        </div>
        <div className="form-grid">
          <label className="field"><span>Hạng vé</span><select value={cabin} onChange={e => setCabin(e.target.value)}>
            {!fares.some(fare => fare.fareClass === cabin) && <option value={cabin}>{cabinName(cabin)} — chưa có giá</option>}
            {fares.map(fare => <option key={fare.id} value={fare.fareClass}>{cabinName(fare.fareClass)}</option>)}
          </select></label>
          <label className="field"><span>Số hành khách</span><select value={passengers} onChange={e => setPassengers(Number(e.target.value))}>
            {[1,2,3,4,5,6,7,8,9].map(value => <option key={value} value={value}>{value}</option>)}
          </select></label>
        </div>
        {!card && <div className="inline-error">Chưa có giá đang có hiệu lực cho hạng vé này.</div>}
        <div className="booking-buttons"><button className="btn btn-primary" disabled>Đặt chỗ chưa khả dụng</button><Link className="btn btn-outline" to={back}>Chọn chuyến khác <ArrowRight size={16}/></Link></div>
      </div><aside className="booking-summary">
        <div className="summary-top"><Plane size={20}/> TÓM TẮT HÀNH TRÌNH</div>
        <h3>{flight.originCity} <ArrowRight size={19}/> {flight.destinationCity}</h3>
        <span>{dateTime(flight.departureTime)} · Giờ Việt Nam</span>
        {card && <><div className="summary-flight">
          <div><small>KHỞI HÀNH</small><b>{card.depart}</b><span>{flight.originCode}</span></div><Plane size={22}/>
          <div><small>ĐẾN NƠI</small><b>{card.arrive}</b><span>{flight.destinationCode}</span></div>
        </div><div className="summary-price"><small>Giá cơ bản / khách</small><strong>{money(card.price, card.currency)}</strong></div>
        <div className="summary-meta"><span>Tạm tính · {passengers} khách</span><b>{money(card.price * passengers, card.currency)}</b></div></>}
        <div className="summary-warning">Giá và trạng thái ghế có thể thay đổi. Xem danh sách không đồng nghĩa với giữ chỗ.</div>
      </aside></div>
    </>}
  </section>;
}
