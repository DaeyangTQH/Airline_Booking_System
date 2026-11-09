import { useEffect, useState } from 'react';
import { useParams, useSearchParams, Link, useNavigate, useLocation } from 'react-router-dom';
import { ArrowLeft, ArrowRight, CircleCheck, Mail, Phone, Plane, Ticket } from 'lucide-react';
import Busy from '../components/Busy';
import { money, uuidValid } from '../data/cities';
import { flightService, currentFares, flightCardData, cabinName } from '../services/flightService';
import { seatService } from '../services/seatService';
import { bookingService } from '../services/bookingService';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

const seatStatusName = { AVAILABLE: 'Chưa giữ', HELD: 'Đang giữ', BOOKED: 'Đã đặt', BLOCKED: 'Tạm khóa' };
const PHONE_RE = /^0\d{9}$/;
const GMAIL_RE = /^[a-zA-Z0-9._%+-]+@gmail\.com$/;

export default function BookingPage() {
  const { flightId } = useParams();
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated, session } = useAuth();
  const toast = useToast();

  const [flight, setFlight] = useState(null);
  const [cabin, setCabin] = useState(params.get('cabin') || 'ECONOMY');
  const [passengers, setPassengers] = useState(Math.max(1, Math.min(9, Number(params.get('passengers')) || 1)));
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reload, setReload] = useState(0);
  const [seats, setSeats] = useState([]);
  const [seatLoading, setSeatLoading] = useState(true);
  const [seatError, setSeatError] = useState('');
  const [seatReload, setSeatReload] = useState(0);
  const [availability, setAvailability] = useState([]);
  const [contact, setContact] = useState({ email: '', phone: '' });
  const [submitting, setSubmitting] = useState(false);
  const [created, setCreated] = useState(null);

  useEffect(() => {
    setContact((prev) => ({
      email: prev.email || session?.email || '',
      phone: prev.phone || session?.phone || ''
    }));
  }, [session?.email, session?.phone]);

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true); setError(''); setFlight(null); setCreated(null);
    if (!uuidValid(flightId)) {
      setError('Liên kết chuyến bay không hợp lệ. Vui lòng chọn lại chuyến bay.');
      setLoading(false);
      return () => controller.abort();
    }
    flightService.detail(flightId, { signal: controller.signal })
      .then((data) => { if (!controller.signal.aborted) setFlight(data); })
      .catch((e) => { if (!controller.signal.aborted) setError(e.message); })
      .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [flightId, reload]);

  useEffect(() => {
    const controller = new AbortController();
    setSeats([]); setAvailability([]); setSeatError(''); setSeatLoading(true);
    if (!flight || flight.id !== flightId) return () => controller.abort();
    Promise.all([
      seatService.list(flightId, { seatClass: cabin, signal: controller.signal }),
      flightService.availability(flightId, { signal: controller.signal })
    ]).then(([data, counts]) => {
      if (controller.signal.aborted) return;
      if (!Array.isArray(counts)) throw new Error('Số liệu ghế không hợp lệ.');
      setSeats(data); setAvailability(counts);
    })
      .catch((e) => { if (!controller.signal.aborted) setSeatError(e.message); })
      .finally(() => { if (!controller.signal.aborted) setSeatLoading(false); });
    return () => controller.abort();
  }, [flightId, flight, cabin, seatReload]);

  const fares = currentFares(flight);
  const card = flight && flightCardData(flight, cabin);
  const capacity = availability.find((row) => row.fareClass === cabin);
  const searchParams = flight ? new URLSearchParams({
    from: flight.originCode,
    to: flight.destinationCode,
    departure: new Date(flight.departureTime).toLocaleDateString('en-CA', { timeZone: 'Asia/Ho_Chi_Minh' }),
    passengers: String(passengers),
    cabin,
    trip: 'oneway'
  }) : null;
  const back = searchParams ? `/flights?${searchParams}` : '/flights';
  const dateTime = (value) => new Date(value).toLocaleString('vi-VN', { timeZone: 'Asia/Ho_Chi_Minh' });
  const notEnoughSeats = capacity && capacity.availableSeats < passengers;
  const canSubmit = !!card && !notEnoughSeats && !submitting;

  async function createBooking() {
    if (!canSubmit) return;

    if (!isAuthenticated) {
      toast('Vui lòng đăng nhập để đặt chỗ.', 'error');
      navigate('/login', { state: { from: location, email: contact.email } });
      return;
    }

    const email = contact.email.trim();
    const phone = contact.phone.trim();
    if (!GMAIL_RE.test(email)) {
      toast('Email liên hệ phải là địa chỉ @gmail.com.', 'error');
      return;
    }
    if (!PHONE_RE.test(phone)) {
      toast('Số điện thoại phải gồm 10 số và bắt đầu bằng 0.', 'error');
      return;
    }

    setSubmitting(true);
    setError('');
    try {
      const result = await bookingService.create({
        flightId,
        fareClass: cabin,
        quantity: passengers,
        contact: { email, phone }
      });
      setCreated(result);
      toast(`Đã tạo booking ${result.bookingReference}.`);
    } catch (e) {
      setError(e.message || 'Không tạo được booking.');
      toast(e.message || 'Không tạo được booking.', 'error');
    } finally {
      setSubmitting(false);
    }
  }

  if (created) {
    const bookingId = created.bookingId || created.id;
    return (
      <section className="booking-page shell">
        <div className="success-panel">
          <div className="success-icon"><CircleCheck size={34} /></div>
          <h2>Đặt chỗ thành công</h2>
          <p>Booking đang chờ thanh toán. Bạn có 15 phút để hoàn tất qua SePay.</p>
          <div className="booking-code">{created.bookingReference}</div>
          <div className="confirm-list" style={{ textAlign: 'left', maxWidth: 420, margin: '0 auto' }}>
            <div><span>Trạng thái</span><b>{created.status === 'PENDING' ? 'Chờ thanh toán' : created.status}</b></div>
            <div><span>Hạng vé</span><b>{cabinName(created.fareClass)}</b></div>
            <div><span>Số vé</span><b>{created.ticketQuantity}</b></div>
            <div><span>Tổng tiền</span><b>{money(created.totalAmount, created.currency)}</b></div>
            <div><span>Hết hạn giữ chỗ</span><b>{dateTime(created.expiresAt)}</b></div>
          </div>
          <div className="success-actions">
            <Link className="btn btn-primary" to={`/payment/${bookingId}`}>Thanh toán ngay</Link>
            <Link className="btn btn-outline" to={`/trips/${bookingId}`}>Xem chi tiết đặt chỗ</Link>
          </div>
        </div>
      </section>
    );
  }

  return (
    <section className="booking-page shell">
      <Link className="back-link" to={back}><ArrowLeft size={17} /> Quay về danh sách chuyến bay</Link>
      <div className="booking-heading">
        <div>
          <span className="eyebrow">THÔNG TIN HÀNH TRÌNH</span>
          <h1>Chi tiết chuyến bay</h1>
          <p>Kiểm tra lịch trình, điền thông tin liên hệ rồi tạo đặt chỗ.</p>
        </div>
      </div>

      {loading ? <Busy /> : error && !flight ? (
        <div className="inline-error" role="alert">{error} <button onClick={() => setReload((v) => v + 1)}>Thử lại</button></div>
      ) : flight ? (
        <>
          <div className="booking-layout">
            <div className="booking-form-panel">
              <div className="form-section-title">
                <span className="form-step-icon"><Ticket size={21} /></span>
                <div><h2>{flight.flightCode}</h2><p>{flight.aircraftModel} · {flight.status}</p></div>
              </div>

              <div className="confirm-list">
                <div><span>Điểm đi</span><b>{flight.originCity} ({flight.originCode})</b></div>
                <div><span>Điểm đến</span><b>{flight.destinationCity} ({flight.destinationCode})</b></div>
                <div><span>Khởi hành</span><b>{dateTime(flight.departureTime)}</b></div>
                <div><span>Đến nơi</span><b>{dateTime(flight.arrivalTime)}</b></div>
              </div>

              <div className="form-grid">
                <label className="field">
                  <span>Hạng vé</span>
                  <select value={cabin} onChange={(e) => setCabin(e.target.value)}>
                    {!fares.some((fare) => fare.fareClass === cabin) && (
                      <option value={cabin}>{cabinName(cabin)} — chưa có giá</option>
                    )}
                    {fares.map((fare) => (
                      <option key={fare.id} value={fare.fareClass}>{cabinName(fare.fareClass)}</option>
                    ))}
                  </select>
                </label>
                <label className="field">
                  <span>Số hành khách</span>
                  <select value={passengers} onChange={(e) => setPassengers(Number(e.target.value))}>
                    {[1, 2, 3, 4, 5, 6, 7, 8, 9].map((value) => (
                      <option key={value} value={value}>{value}</option>
                    ))}
                  </select>
                </label>
              </div>

              {!card && <div className="inline-error">Chưa có giá đang có hiệu lực cho hạng vé này.</div>}

              <section className="contact-block" aria-label="Thông tin liên hệ">
                <div className="form-section-title">
                  <div>
                    <h2>Thông tin liên hệ</h2>
                    <p>Dùng để gắn vào booking (bắt buộc theo API).</p>
                  </div>
                </div>
                <div className="form-grid">
                  <label className="field">
                    <span><Mail size={14} /> Email (@gmail.com)</span>
                    <input
                      type="email"
                      value={contact.email}
                      onChange={(e) => setContact((c) => ({ ...c, email: e.target.value }))}
                      placeholder="you@gmail.com"
                      required
                    />
                  </label>
                  <label className="field">
                    <span><Phone size={14} /> Số điện thoại</span>
                    <input
                      type="tel"
                      value={contact.phone}
                      onChange={(e) => setContact((c) => ({ ...c, phone: e.target.value }))}
                      placeholder="090postgres4567"
                      required
                    />
                  </label>
                </div>
              </section>

              <section className="seat-catalog" aria-label="Danh sách ghế">
                <div className="form-section-title">
                  <div>
                    <h2>Ghế · {cabinName(cabin)}</h2>
                    <p>Chỉ xem trạng thái ghế; booking giữ theo số lượng, chưa chọn ghế cụ thể.</p>
                  </div>
                </div>
                {!seatLoading && !seatError && capacity && (
                  <div className="confirm-list">
                    <div><span>Ghế đã khai báo</span><b>{capacity.classCapacity}</b></div>
                    <div><span>Ghế chưa giữ</span><b>{capacity.availableSeats}</b></div>
                    <div><span>Số hành khách</span><b>{passengers}</b></div>
                  </div>
                )}
                {!seatLoading && !seatError && notEnoughSeats && (
                  <div className="inline-error" role="status">Chưa đủ ghế cho {passengers} hành khách ở hạng vé này.</div>
                )}
                {seatLoading ? <Busy label="Đang tải ghế..." /> : seatError ? (
                  <div className="inline-error" role="alert">
                    {seatError} <button onClick={() => setSeatReload((v) => v + 1)}>Thử lại</button>
                  </div>
                ) : seats.length ? (
                  <div className="seat-catalog-grid">
                    {seats.map((seat) => (
                      <div key={seat.id} className={`seat-catalog-item seat-${seat.status.toLowerCase()}`}>
                        <strong>{seat.seatNumber}</strong>
                        <small>{seatStatusName[seat.status] || seat.status}</small>
                      </div>
                    ))}
                  </div>
                ) : <p>Chưa có ghế được khai báo cho hạng vé này.</p>}
              </section>

              {error && <p className="inline-error" role="alert">{error}</p>}

              <div className="booking-buttons">
                <button className="btn btn-primary" type="button" disabled={!canSubmit} onClick={createBooking}>
                  {submitting ? 'Đang tạo booking...' : isAuthenticated ? 'Xác nhận đặt chỗ' : 'Đăng nhập để đặt chỗ'}
                </button>
                <Link className="btn btn-outline" to={back}>Chọn chuyến khác <ArrowRight size={16} /></Link>
              </div>
            </div>

            <aside className="booking-summary">
              <div className="summary-top"><Plane size={20} /> TÓM TẮT HÀNH TRÌNH</div>
              <h3>{flight.originCity} <ArrowRight size={19} /> {flight.destinationCity}</h3>
              <span>{dateTime(flight.departureTime)} · Giờ Việt Nam</span>
              {card && (
                <>
                  <div className="summary-flight">
                    <div><small>KHỞI HÀNH</small><b>{card.depart}</b><span>{flight.originCode}</span></div>
                    <Plane size={22} />
                    <div><small>ĐẾN NƠI</small><b>{card.arrive}</b><span>{flight.destinationCode}</span></div>
                  </div>
                  <div className="summary-price">
                    <small>Giá cơ bản / khách</small>
                    <strong>{money(card.price, card.currency)}</strong>
                  </div>
                  <div className="summary-meta">
                    <span>Tạm tính · {passengers} khách</span>
                    <b>{money(card.price * passengers, card.currency)}</b>
                  </div>
                </>
              )}
              <div className="summary-warning">
                Sau khi tạo booking, hệ thống giữ chỗ khoảng 15 phút để bạn thanh toán SePay.
              </div>
            </aside>
          </div>
        </>
      ) : null}
    </section>
  );
}
