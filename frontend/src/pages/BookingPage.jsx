import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import {
    ArrowLeft, ArrowRight, Armchair, BadgeCheck, CalendarDays, Check,
    CheckCircle2, CircleAlert, Clock3, CreditCard, Info, LockKeyhole,
    Mail, MapPin, Minus, Phone, Plane, Plus, RefreshCw, ShieldCheck,
    Sparkles, Ticket, UsersRound
} from 'lucide-react';
import Busy from '../components/Busy';
import { money, uuidValid } from '../data/cities';
import { cabinName, currentFares, flightCardData, flightService } from '../services/flightService';
import { bookingService } from '../services/bookingService';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import '../styles/booking-premium.css';

const PHONE_RE = /^0\d{9}$/;
const GMAIL_RE = /^[a-zA-Z0-9._%+-]+@gmail\.com$/;
const TIME_ZONE = 'Asia/Ho_Chi_Minh';

const formatDate = (value) => {
    if (!value) return '—';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return '—';
    return new Intl.DateTimeFormat('vi-VN', {
        weekday: 'long', day: '2-digit', month: '2-digit', year: 'numeric', timeZone: TIME_ZONE
    }).format(date);
};
const formatTime = (value) => {
    if (!value) return '—';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return '—';
    return new Intl.DateTimeFormat('vi-VN', {
        hour: '2-digit', minute: '2-digit', hourCycle: 'h23', timeZone: TIME_ZONE
    }).format(date);
};
const formatFull = (value) => {
    if (!value) return '—';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return '—';
    return new Intl.DateTimeFormat('vi-VN', {
        hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit',
        year: 'numeric', timeZone: TIME_ZONE
    }).format(date);
};

function BookingSteps({ paymentStep = false }) {
    return (
        <div className="ab-steps" aria-label="Tiến trình đặt vé">
      <span className={`ab-step ${paymentStep ? 'ab-step--done' : 'ab-step--active'}`}>
        <span className="ab-step__number">{paymentStep ? <Check size={14} /> : '01'}</span>
        <span>Đặt chỗ</span>
      </span>
            <span className="ab-steps__connector" aria-hidden="true" />
            <span className={`ab-step ${paymentStep ? 'ab-step--active' : ''}`}>
        <span className="ab-step__number">02</span>
        <span>Thanh toán</span>
      </span>
            <span className="ab-steps__connector" aria-hidden="true" />
            <span className="ab-step">
        <span className="ab-step__number">03</span>
        <span>Chọn ghế</span>
      </span>
        </div>
    );
}

export default function BookingPage() {
    const { flightId } = useParams();
    const [params] = useSearchParams();
    const location = useLocation();
    const navigate = useNavigate();
    const { isAuthenticated, session } = useAuth();
    const toast = useToast();

    const [flight, setFlight] = useState(null);
    const [cabin, setCabin] = useState(params.get('cabin') || 'ECONOMY');
    const [passengers, setPassengers] = useState(
        Math.max(1, Math.min(9, Number(params.get('passengers')) || 1))
    );
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [reload, setReload] = useState(0);
    const [availability, setAvailability] = useState([]);
    const [availabilityLoading, setAvailabilityLoading] = useState(true);
    const [availabilityError, setAvailabilityError] = useState('');
    const [availabilityReload, setAvailabilityReload] = useState(0);
    const [contact, setContact] = useState({ email: '', phone: '' });
    const [submitting, setSubmitting] = useState(false);
    const [created, setCreated] = useState(null);

    useEffect(() => {
        setContact((current) => ({
            email: current.email || session?.email || '',
            phone: current.phone || session?.phone || ''
        }));
    }, [session?.email, session?.phone]);

    useEffect(() => {
        const controller = new AbortController();
        setLoading(true);
        setError('');
        setFlight(null);
        setCreated(null);

        if (!uuidValid(flightId)) {
            setError('Liên kết chuyến bay không hợp lệ. Vui lòng chọn lại chuyến bay.');
            setLoading(false);
            return () => controller.abort();
        }

        flightService.detail(flightId, { signal: controller.signal })
            .then((data) => { if (!controller.signal.aborted) setFlight(data); })
            .catch((err) => { if (!controller.signal.aborted) setError(err.message || 'Không tải được chuyến bay.'); })
            .finally(() => { if (!controller.signal.aborted) setLoading(false); });

        return () => controller.abort();
    }, [flightId, reload]);

    // Booking chỉ cần số lượng ghế còn trống; không tải seat map tại bước này.
    useEffect(() => {
        const controller = new AbortController();
        setAvailability([]);
        setAvailabilityError('');
        setAvailabilityLoading(true);

        if (!flight || flight.id !== flightId) {
            setAvailabilityLoading(false);
            return () => controller.abort();
        }

        flightService.availability(flightId, { signal: controller.signal })
            .then((rows) => {
                if (controller.signal.aborted) return;
                if (!Array.isArray(rows)) throw new Error('Dữ liệu số ghế không hợp lệ.');
                setAvailability(rows);
            })
            .catch((err) => {
                if (!controller.signal.aborted) {
                    setAvailabilityError(err.message || 'Không kiểm tra được số ghế còn lại.');
                }
            })
            .finally(() => { if (!controller.signal.aborted) setAvailabilityLoading(false); });

        return () => controller.abort();
    }, [flightId, flight, availabilityReload]);

    const fares = currentFares(flight);
    const card = flight ? flightCardData(flight, cabin) : null;
    const seatInfo = availability.find((entry) => entry.fareClass === cabin);
    const count = Number(seatInfo?.availableSeats);
    const availableSeats = seatInfo?.availableSeats != null && Number.isFinite(count) ? count : null;
    const notEnoughSeats = availableSeats !== null && availableSeats < passengers;
    const canSubmit = Boolean(card) && availableSeats !== null && !availabilityLoading &&
        !availabilityError && !notEnoughSeats && !submitting;

    const searchParams = flight ? new URLSearchParams({
        from: flight.originCode,
        to: flight.destinationCode,
        departure: new Date(flight.departureTime).toLocaleDateString('en-CA', { timeZone: TIME_ZONE }),
        passengers: String(passengers),
        cabin,
        trip: 'oneway'
    }) : null;
    const back = searchParams ? `/flights?${searchParams}` : '/flights';

    const changePassengers = (delta) => {
        setPassengers((current) => Math.max(1, Math.min(9, current + delta)));
    };

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
        } catch (err) {
            const message = err.message || 'Không tạo được booking. Vui lòng thử lại.';
            setError(message);
            toast(message, 'error');
        } finally {
            setSubmitting(false);
        }
    }

    if (created) {
        const bookingId = created.bookingId || created.id;
        return (
            <main className="air-booking ab-success-page">
                <div className="shell ab-shell">
                    <div className="ab-page-nav">
                        <Link to="/flights" className="ab-back"><ArrowLeft size={17} /> Tìm chuyến bay khác</Link>
                        <BookingSteps paymentStep />
                    </div>
                    <section className="ab-success" aria-labelledby="ab-success-heading">
                        <div className="ab-success__icon"><CheckCircle2 size={42} strokeWidth={1.7} /></div>
                        <div className="ab-success__eyebrow"><BadgeCheck size={15} /> TẠO ĐẶT CHỖ THÀNH CÔNG</div>
                        <h1 id="ab-success-heading">Sẵn sàng cho hành trình của bạn.</h1>
                        <p>Đặt chỗ đã được tạo và đang chờ thanh toán. Vui lòng hoàn tất thanh toán trước khi hết hạn giữ chỗ.</p>
                        <div className="ab-success__ticket">
                            <div className="ab-success__ticket-head">
                                <span><Ticket size={17} /> MÃ ĐẶT CHỖ</span>
                                <span className="ab-status-pill">Chờ thanh toán</span>
                            </div>
                            <div className="ab-success__code">{created.bookingReference}</div>
                            <div className="ab-success__facts">
                                <div><span>Hạng vé</span><strong>{cabinName(created.fareClass)}</strong></div>
                                <div><span>Số hành khách</span><strong>{created.ticketQuantity}</strong></div>
                                <div><span>Tổng thanh toán</span><strong>{money(created.totalAmount, created.currency)}</strong></div>
                                <div><span>Hạn thanh toán</span><strong>{formatFull(created.expiresAt)} (VN)</strong></div>
                            </div>
                        </div>
                        <div className="ab-success__actions">
                            <Link className="ab-cta" to={`/payment/${bookingId}`}>
                                <CreditCard size={19} /> Thanh toán qua SePay <ArrowRight size={19} />
                            </Link>
                            <Link className="ab-secondary-action" to={`/trips/${bookingId}`}>
                                Xem chi tiết đặt chỗ <ArrowRight size={16} />
                            </Link>
                        </div>
                        <p className="ab-success__foot"><LockKeyhole size={15} /> Bạn sẽ chọn ghế cụ thể trong chi tiết đặt chỗ sau khi thanh toán thành công.</p>
                    </section>
                </div>
            </main>
        );
    }

    return (
        <main className="air-booking">
            <div className="shell ab-shell">
                <div className="ab-page-nav">
                    <Link className="ab-back" to={back}><ArrowLeft size={17} /> Quay lại tìm chuyến bay</Link>
                    <BookingSteps />
                </div>

                {loading ? (
                    <div className="ab-loading"><Busy label="Đang tải hành trình của bạn..." /></div>
                ) : !flight ? (
                    <div className="ab-full-error" role="alert">
                        <CircleAlert size={34} />
                        <h2>Không thể tải chuyến bay</h2>
                        <p>{error || 'Không tìm thấy thông tin chuyến bay.'}</p>
                        <button className="ab-secondary-action" type="button" onClick={() => setReload((value) => value + 1)}>
                            <RefreshCw size={17} /> Thử tải lại
                        </button>
                    </div>
                ) : (
                    <>
                        <header className="ab-intro">
                            <span className="ab-overline"><Sparkles size={14} /> HÀNH TRÌNH CỦA BẠN</span>
                            <h1>Một chuyến đi tuyệt vời <em>bắt đầu từ đây.</em></h1>
                            <p>Kiểm tra thông tin chuyến bay, chọn hạng vé và hoàn tất đặt chỗ chỉ trong vài bước.</p>
                        </header>

                        <section className="ab-flight-hero" aria-label="Thông tin chuyến bay">
                            <div className="ab-flight-hero__top">
                                <span className="ab-flight-hero__label"><Plane size={16} /> THÔNG TIN CHUYẾN BAY</span>
                                <span className="ab-flight-hero__code">{flight.flightCode}</span>
                            </div>
                            <div className="ab-route">
                                <div className="ab-route__airport">
                                    <span>ĐIỂM KHỞI HÀNH</span>
                                    <strong>{flight.originCode}</strong>
                                    <p>{flight.originCity}</p>
                                </div>
                                <div className="ab-route__middle" aria-hidden="true">
                                    <span className="ab-route__line" />
                                    <span className="ab-route__plane"><Plane size={25} strokeWidth={1.7} /></span>
                                    <span className="ab-route__line" />
                                    <small>{card?.duration || 'Bay thẳng'}</small>
                                </div>
                                <div className="ab-route__airport ab-route__airport--right">
                                    <span>ĐIỂM ĐẾN</span>
                                    <strong>{flight.destinationCode}</strong>
                                    <p>{flight.destinationCity}</p>
                                </div>
                            </div>
                            <div className="ab-flight-hero__bottom">
                                <div><CalendarDays size={17} /><span><small>Ngày khởi hành</small><b>{formatDate(flight.departureTime)}</b></span></div>
                                <div><Clock3 size={17} /><span><small>Giờ bay</small><b>{formatTime(flight.departureTime)} – {formatTime(flight.arrivalTime)} (VN)</b></span></div>
                                <div><Ticket size={17} /><span><small>Loại máy bay</small><b>{flight.aircraftModel || 'Đang cập nhật'}</b></span></div>
                            </div>
                            <Plane className="ab-flight-hero__watermark" size={280} strokeWidth={0.7} aria-hidden="true" />
                        </section>

                        <div className="ab-content-grid">
                            <div className="ab-form-column">
                                <section className="ab-panel" aria-labelledby="ab-ticket-title">
                                    <div className="ab-panel__heading">
                                        <div className="ab-panel__icon"><Ticket size={22} /></div>
                                        <div><span className="ab-panel__step">BƯỚC 01</span><h2 id="ab-ticket-title">Lựa chọn vé</h2><p>Chọn hạng ghế và số lượng hành khách.</p></div>
                                    </div>

                                    <div className="ab-field-head"><h3>Hạng vé</h3><span>Giá theo từng hành khách</span></div>
                                    <div className="ab-fare-list" role="radiogroup" aria-label="Hạng vé">
                                        {fares.length === 0 ? (
                                            <div className="ab-soft-alert"><Info size={17} /> Chưa có hạng vé đang mở bán cho chuyến bay này.</div>
                                        ) : fares.map((fare) => (
                                            <label className={`ab-fare ${cabin === fare.fareClass ? 'ab-fare--active' : ''}`} key={fare.id || fare.fareClass}>
                                                <input
                                                    type="radio" name="fareClass" value={fare.fareClass}
                                                    checked={cabin === fare.fareClass}
                                                    onChange={() => setCabin(fare.fareClass)} disabled={submitting}
                                                />
                                                <span className="ab-fare__select" aria-hidden="true"><Check size={13} /></span>
                                                <span className="ab-fare__text"><strong>{cabinName(fare.fareClass)}</strong><small>{fare.fareClass}</small></span>
                                                <span className="ab-fare__price">{money(Number(fare.basePrice), fare.currency)}<small>/ khách</small></span>
                                            </label>
                                        ))}
                                    </div>
                                    {!card && fares.length > 0 && (
                                        <div className="ab-soft-alert ab-soft-alert--error"><CircleAlert size={17} /> Hạng vé đã chọn không có giá hợp lệ. Vui lòng chọn hạng khác.</div>
                                    )}

                                    <div className="ab-form-divider" />
                                    <div className="ab-field-head"><h3>Số hành khách</h3><span>1 đến 9 người / booking</span></div>
                                    <div className="ab-passenger-picker">
                                        <div className="ab-passenger-picker__info"><span className="ab-passenger-picker__icon"><UsersRound size={21} /></span><span><strong>Hành khách</strong><small>Chọn số lượng vé cần đặt</small></span></div>
                                        <div className="ab-counter" aria-label="Số hành khách">
                                            <button type="button" onClick={() => changePassengers(-1)} disabled={submitting || passengers <= 1} aria-label="Giảm số hành khách"><Minus size={17} /></button>
                                            <output aria-live="polite">{passengers}</output>
                                            <button type="button" onClick={() => changePassengers(1)} disabled={submitting || passengers >= 9} aria-label="Tăng số hành khách"><Plus size={17} /></button>
                                        </div>
                                    </div>

                                    <div className={`ab-availability ${notEnoughSeats ? 'ab-availability--low' : ''}`} aria-live="polite">
                                        <span className="ab-availability__symbol"><Armchair size={23} strokeWidth={1.8} /></span>
                                        <div className="ab-availability__copy"><strong>Số ghế còn trống · {cabinName(cabin)}</strong><small>Chọn vị trí ghế sau khi thanh toán thành công</small></div>
                                        {availabilityLoading ? (
                                            <span className="ab-availability__value ab-availability__value--small">Đang kiểm tra...</span>
                                        ) : availabilityError ? (
                                            <button type="button" className="ab-availability__retry" onClick={() => setAvailabilityReload((v) => v + 1)}><RefreshCw size={15} /> Thử lại</button>
                                        ) : availableSeats === null ? (
                                            <span className="ab-availability__value ab-availability__value--small">Chưa có dữ liệu</span>
                                        ) : (
                                            <span className="ab-availability__value">{availableSeats} <small>ghế</small></span>
                                        )}
                                    </div>
                                    {availabilityError && <p className="ab-message-error" role="alert">{availabilityError}</p>}
                                    {notEnoughSeats && <p className="ab-message-error" role="alert">Chỉ còn {availableSeats} ghế, không đủ cho {passengers} hành khách. Vui lòng giảm số lượng vé hoặc đổi hạng.</p>}
                                </section>

                                <section className="ab-panel" aria-labelledby="ab-contact-title">
                                    <div className="ab-panel__heading">
                                        <div className="ab-panel__icon ab-panel__icon--violet"><Mail size={21} /></div>
                                        <div><span className="ab-panel__step">BƯỚC 02</span><h2 id="ab-contact-title">Thông tin liên hệ</h2><p>Thông tin được sử dụng để liên hệ về đặt chỗ.</p></div>
                                    </div>
                                    <div className="ab-contact-fields">
                                        <label className="ab-input-field">
                                            <span>Địa chỉ Gmail <b>*</b></span>
                                            <span className="ab-input-wrap"><Mail size={18} /><input type="email" autoComplete="email" inputMode="email" placeholder="tenban@gmail.com" value={contact.email} onChange={(e) => setContact((c) => ({ ...c, email: e.target.value }))} disabled={submitting} /></span>
                                            <small>Dùng để nhận thông tin đặt vé và thông báo chuyến bay.</small>
                                        </label>
                                        <label className="ab-input-field">
                                            <span>Số điện thoại <b>*</b></span>
                                            <span className="ab-input-wrap"><Phone size={18} /><input type="tel" autoComplete="tel" inputMode="numeric" maxLength={10} placeholder="0912 345 678" value={contact.phone} onChange={(e) => setContact((c) => ({ ...c, phone: e.target.value }))} disabled={submitting} /></span>
                                            <small>10 chữ số, bắt đầu bằng số 0.</small>
                                        </label>
                                    </div>
                                    <div className="ab-note"><ShieldCheck size={19} /><span>Thông tin liên hệ sẽ được gắn với booking. Bạn có thể xem chi tiết và chọn ghế sau khi thanh toán.</span></div>
                                </section>
                                {error && <div className="ab-form-error" role="alert"><CircleAlert size={19} />{error}</div>}
                            </div>

                            <aside className="ab-summary" aria-label="Tóm tắt giá vé">
                                <div className="ab-summary__head"><span className="ab-summary__icon"><Ticket size={19} /></span><span>TÓM TẮT ĐẶT VÉ</span><BadgeCheck size={18} /></div>
                                <div className="ab-summary__body">
                                    <div className="ab-summary__route"><div><small>HÀNH TRÌNH</small><strong>{flight.originCity} <ArrowRight size={15} /> {flight.destinationCity}</strong><span>{flight.flightCode} · {formatDate(flight.departureTime)}</span></div></div>
                                    <div className="ab-summary__divider" />
                                    <div className="ab-summary__line"><span>Hạng vé</span><strong>{cabinName(cabin)}</strong></div>
                                    <div className="ab-summary__line"><span>Hành khách</span><strong>{passengers} người</strong></div>
                                    <div className="ab-summary__line"><span>Giá / người</span><strong>{card ? money(card.price, card.currency) : '—'}</strong></div>
                                    <div className="ab-summary__divider" />
                                    <div className="ab-summary__total"><span>TẠM TÍNH</span><strong>{card ? money(card.price * passengers, card.currency) : '—'}</strong><small>Giá vé tạm tính theo số hành khách đã chọn</small></div>
                                    <button className="ab-cta ab-cta--full" type="button" onClick={createBooking} disabled={!canSubmit}>
                                        {submitting ? 'Đang tạo đặt chỗ...' : isAuthenticated ? 'Xác nhận đặt chỗ' : 'Đăng nhập để đặt vé'}
                                        {!submitting && <ArrowRight size={19} />}
                                    </button>
                                    <div className="ab-summary__safe"><LockKeyhole size={14} /> Chưa thanh toán ở bước này</div>
                                    <div className="ab-summary__notice"><Clock3 size={18} /><span>Sau khi đặt chỗ, bạn sẽ có khoảng <b>15 phút</b> để hoàn tất thanh toán qua SePay.</span></div>
                                </div>
                            </aside>
                        </div>

                        <div className="ab-bottom-help"><MapPin size={17} /><span>Chỗ ngồi cụ thể được chọn tại <strong>Chi tiết đặt chỗ</strong> sau khi thanh toán thành công.</span><Link to={back}>Đổi chuyến bay <ArrowRight size={15} /></Link></div>
                    </>
                )}
            </div>
        </main>
    );
}
