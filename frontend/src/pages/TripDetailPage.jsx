import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ArrowLeft, CreditCard, Ticket, Users } from 'lucide-react';
import { bookingService } from '../services/bookingService';
import { passengerService } from '../services/passengerService';
import Busy from '../components/Busy';
import { displayDate, money } from '../data/cities';
import { useToast } from '../context/ToastContext';
import {
  apiMessageVi,
  bookingDisplayStatus,
  bookingStatusLabel,
  isBookingHoldExpired
} from '../utils/bookingUi';

const EMPTY_FORM = {
  passengerProfileId: '',
  fullName: '',
  passportNumber: '',
  dateOfBirth: '',
  phone: '',
  email: ''
};

function todayInputMax() {
  return new Date().toISOString().slice(0, 10);
}

export default function TripDetailPage() {
  const { bookingId } = useParams();
  const toast = useToast();
  const [data, setData] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [profiles, setProfiles] = useState([]);
  const [form, setForm] = useState(EMPTY_FORM);
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);

  async function loadDetail() {
    const d = await bookingService.detail(bookingId);
    setData(d);
    return d;
  }

  useEffect(() => {
    let live = true;
    setLoading(true);
    setError('');
    Promise.all([
      bookingService.detail(bookingId),
      passengerService.list().catch(() => [])
    ])
      .then(([detail, list]) => {
        if (!live) return;
        setData(detail);
        setProfiles(Array.isArray(list) ? list : []);
      })
      .catch((e) => {
        if (live) setError(apiMessageVi(e.message));
      })
      .finally(() => {
        if (live) setLoading(false);
      });
    return () => { live = false; };
  }, [bookingId]);

  const displayStatus = bookingDisplayStatus(data);
  const expired = isBookingHoldExpired(data);
  const statusClass = String(displayStatus || '').toLowerCase();
  const passengers = data?.passengers || [];
  const ticketQuantity = Number(data?.ticketQuantity) || 0;
  const canAddPassenger = data?.status === 'CONFIRMED' && passengers.length < ticketQuantity;
  const slotsFull = data?.status === 'CONFIRMED' && ticketQuantity > 0 && passengers.length >= ticketQuantity;

  let paymentNote = 'Trạng thái booking hiện tại.';
  if (data?.status === 'CONFIRMED') paymentNote = 'Booking đã được xác nhận sau thanh toán. Hãy thêm hành khách để phát hành vé.';
  else if (expired) paymentNote = 'Thời gian giữ chỗ đã hết. Vui lòng tạo đặt chỗ mới nếu bạn vẫn muốn bay.';
  else if (data?.status === 'PENDING') paymentNote = 'Booking đang chờ thanh toán. Thanh toán xong mới thêm hành khách và nhận vé.';
  else if (data?.status === 'CANCELLED') paymentNote = 'Booking đã bị hủy.';

  function changeField(key, value) {
    setForm((prev) => ({ ...prev, [key]: value }));
  }

  function applyProfile(profileId) {
    if (!profileId) {
      setForm(EMPTY_FORM);
      return;
    }
    const profile = profiles.find((p) => String(p.id) === String(profileId));
    if (!profile) {
      setForm((prev) => ({ ...prev, passengerProfileId: profileId }));
      return;
    }
    setForm({
      passengerProfileId: profile.id,
      fullName: profile.fullName || '',
      passportNumber: profile.passportNo || '',
      dateOfBirth: profile.dob ? String(profile.dob).slice(0, 10) : '',
      phone: profile.phone || '',
      email: profile.email || ''
    });
  }

  async function submitPassenger(e) {
    e.preventDefault();
    if (!canAddPassenger || saving) return;

    const fullName = form.fullName.trim();
    const passportNumber = form.passportNumber.trim();
    const phone = form.phone.trim();
    const email = form.email.trim();
    const dateOfBirth = form.dateOfBirth;

    if (!fullName) {
      setFormError('Họ tên hành khách không được để trống.');
      return;
    }
    if (!passportNumber) {
      setFormError('Số hộ chiếu không được để trống.');
      return;
    }
    if (!dateOfBirth) {
      setFormError('Ngày sinh là bắt buộc.');
      return;
    }
    if (!/^0\d{9}$/.test(phone)) {
      setFormError('Số điện thoại phải gồm 10 số và bắt đầu bằng 0.');
      return;
    }
    if (!/^[a-zA-Z0-9._%+-]+@gmail\.com$/.test(email)) {
      setFormError('Email liên hệ phải là địa chỉ @gmail.com.');
      return;
    }

    setSaving(true);
    setFormError('');
    try {
      const result = await bookingService.addPassenger(bookingId, {
        passengerProfileId: form.passengerProfileId || null,
        fullName,
        passportNumber,
        dateOfBirth,
        contact: { phone, email }
      });
      const ticketNo = result?.ticket?.ticketNumber;
      toast(
        ticketNo
          ? `Đã thêm hành khách. Mã vé: ${ticketNo}`
          : 'Đã thêm hành khách và phát hành vé.'
      );
      setForm(EMPTY_FORM);
      await loadDetail();
    } catch (err) {
      const message = apiMessageVi(err.message || 'Không thêm được hành khách.');
      setFormError(message);
      toast(message, 'error');
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="shell account-page">
      <Link className="back-link" to="/trips">
        <ArrowLeft size={17} /> Tất cả đặt chỗ
      </Link>

      {loading ? <Busy /> : error ? (
        <div className="inline-error">{error}</div>
      ) : data ? (
        <>
          <div className="trip-detail-top">
            <div>
              <span className="eyebrow">CHI TIẾT CHUYẾN ĐI</span>
              <h1>Đặt chỗ <span>{data.bookingReference}</span></h1>
              <p>Theo dõi trạng thái giữ chỗ, thanh toán và hành khách của bạn.</p>
            </div>
            <span className={`status-pill status-${statusClass}`}>
              {bookingStatusLabel(displayStatus)}
            </span>
          </div>

          <div className="detail-grid">
            <div className="account-card detail-card">
              <h3><Ticket size={19} /> Thông tin vé</h3>
              <div className="detail-list">
                <div><span>Mã đặt chỗ</span><b>{data.bookingReference}</b></div>
                <div><span>Hạng vé</span><b>{data.fareClass}</b></div>
                <div><span>Số vé</span><b>{data.ticketQuantity}</b></div>
                <div><span>Ngày tạo</span><b>{displayDate(data.createdAt)}</b></div>
                <div><span>Hết hạn giữ chỗ</span><b>{displayDate(data.expiresAt)}</b></div>
              </div>
            </div>

            <div className="account-card detail-card">
              <h3><CreditCard size={19} /> Tổng thanh toán</h3>
              <div className="detail-total">{money(data.totalAmount, data.currency)}</div>
              <div className="detail-list">
                <div><span>Đơn giá</span><b>{money(data.unitPrice, data.currency)}</b></div>
                <div><span>Số vé</span><b>{data.ticketQuantity}</b></div>
                <div><span>Trạng thái</span><b>{bookingStatusLabel(displayStatus)}</b></div>
              </div>
              <div className="detail-note">{paymentNote}</div>
              {data.status === 'PENDING' && !expired ? (
                <Link className="btn btn-outline" style={{ marginTop: 14 }} to={`/payment/${data.id}`}>
                  Thanh toán ngay
                </Link>
              ) : data.status === 'CONFIRMED' ? (
                <Link className="btn btn-outline" style={{ marginTop: 14 }} to="/trips">
                  Về danh sách chuyến đi
                </Link>
              ) : (
                <Link className="btn btn-outline" style={{ marginTop: 14 }} to="/flights">
                  Tìm chuyến bay mới
                </Link>
              )}
            </div>
          </div>

          <div className="account-card detail-passengers">
            <h3>
              <Users size={19} /> Hành khách trong booking
              <span className="counter">{passengers.length}/{ticketQuantity || 0}</span>
            </h3>

            {passengers.length ? (
              <div className="detail-list">
                {passengers.map((p, i) => (
                  <div key={p.id || i}>
                    <span>{p.fullName}</span>
                    <b>
                      {p.passportNumber || 'Không có hộ chiếu'}
                      {p.dateOfBirth ? ` · Sinh: ${displayDate(p.dateOfBirth)}` : ''}
                    </b>
                  </div>
                ))}
              </div>
            ) : (
              <p>
                {data.status === 'CONFIRMED'
                  ? 'Chưa có hành khách. Thêm đủ số hành khách theo số vé để phát hành vé.'
                  : 'Booking chưa có thông tin hành khách được gắn.'}
              </p>
            )}

            {data.status !== 'CONFIRMED' && (
              <div className="detail-note" style={{ marginTop: 14 }}>
                Thanh toán xong (trạng thái Đã xác nhận) mới thêm hành khách và nhận vé.
              </div>
            )}

            {slotsFull && (
              <div className="detail-note" style={{ marginTop: 14 }}>
                Đã đủ số hành khách theo số vé đã mua.
              </div>
            )}

            {canAddPassenger && (
              <form className="stack-form" style={{ marginTop: 18 }} onSubmit={submitPassenger}>
                <h4 style={{ margin: '0 0 8px', fontSize: 15 }}>Thêm hành khách</h4>
                <p className="muted" style={{ marginTop: 0, fontSize: 12 }}>
                  Mỗi hành khách được phát hành một vé. Email phải @gmail.com, SĐT dạng 0xxxxxxxxx.
                </p>

                {profiles.length > 0 && (
                  <label className="field">
                    <span>Chọn từ hành khách đã lưu</span>
                    <select
                      value={form.passengerProfileId || ''}
                      onChange={(e) => applyProfile(e.target.value)}
                    >
                      <option value="">Nhập thủ công</option>
                      {profiles.map((p) => (
                        <option key={p.id} value={p.id}>
                          {p.fullName}{p.passportNo ? ` · ${p.passportNo}` : ''}
                        </option>
                      ))}
                    </select>
                  </label>
                )}

                <div className="form-grid">
                  <label className="field col-full">
                    <span>Họ tên đầy đủ *</span>
                    <input
                      required
                      maxLength={150}
                      value={form.fullName}
                      onChange={(e) => changeField('fullName', e.target.value)}
                    />
                  </label>
                  <label className="field">
                    <span>Ngày sinh *</span>
                    <input
                      required
                      type="date"
                      max={todayInputMax()}
                      value={form.dateOfBirth}
                      onChange={(e) => changeField('dateOfBirth', e.target.value)}
                    />
                  </label>
                  <label className="field">
                    <span>Số hộ chiếu *</span>
                    <input
                      required
                      maxLength={50}
                      value={form.passportNumber}
                      onChange={(e) => changeField('passportNumber', e.target.value)}
                    />
                  </label>
                  <label className="field">
                    <span>Số điện thoại *</span>
                    <input
                      required
                      type="tel"
                      placeholder="0xxxxxxxxx"
                      value={form.phone}
                      onChange={(e) => changeField('phone', e.target.value)}
                    />
                  </label>
                  <label className="field">
                    <span>Email (@gmail.com) *</span>
                    <input
                      required
                      type="email"
                      placeholder="ban@gmail.com"
                      value={form.email}
                      onChange={(e) => changeField('email', e.target.value)}
                    />
                  </label>
                </div>

                {formError ? <p className="inline-error" role="alert">{formError}</p> : null}

                <button className="btn btn-primary" type="submit" disabled={saving}>
                  {saving ? 'Đang thêm...' : 'Thêm hành khách & phát hành vé'}
                </button>
              </form>
            )}
          </div>
        </>
      ) : null}
    </section>
  );
}
