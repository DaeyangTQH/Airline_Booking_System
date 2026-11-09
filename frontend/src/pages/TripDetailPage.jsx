import { useEffect, useMemo, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ArrowLeft, CreditCard, Ticket, Users } from 'lucide-react';
import { bookingService } from '../services/bookingService';
import { passengerService } from '../services/passengerService';
import { seatService } from '../services/seatService';
import { ticketService } from '../services/ticketService';
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

const SEAT_STATUS_VI = {
  AVAILABLE: 'Trống',
  HELD: 'Đang giữ',
  BOOKED: 'Đã đặt',
  BLOCKED: 'Khóa'
};

function todayInputMax() {
  return new Date().toISOString().slice(0, 10);
}

function draftFromTickets(ticketList) {
  const next = {};
  ticketList.forEach((t) => {
    next[t.ticketId] = t.seatId || null;
  });
  return next;
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

  const [seats, setSeats] = useState([]);
  const [seatLoading, setSeatLoading] = useState(false);
  const [seatError, setSeatError] = useState('');
  const [seatReload, setSeatReload] = useState(0);
  const [selectedTicketId, setSelectedTicketId] = useState('');
  const [draftSeats, setDraftSeats] = useState({});
  const [seatBusy, setSeatBusy] = useState(false);

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

  const passengers = data?.passengers || [];
  const tickets = useMemo(
    () => passengers.filter((p) => p.ticketId),
    [passengers]
  );

  const ticketSignature = useMemo(
    () => tickets.map((t) => `${t.ticketId}:${t.seatId || ''}`).join('|'),
    [tickets]
  );

  useEffect(() => {
    if (!tickets.length) {
      setDraftSeats({});
      setSelectedTicketId('');
      return;
    }
    setDraftSeats(draftFromTickets(tickets));
    setSelectedTicketId((current) => (
      tickets.some((t) => t.ticketId === current) ? current : tickets[0].ticketId
    ));
  }, [ticketSignature]);

  useEffect(() => {
    if (!data?.flightId || data.status !== 'CONFIRMED' || !tickets.length) {
      setSeats([]);
      setSeatError('');
      return undefined;
    }

    const controller = new AbortController();
    setSeatLoading(true);
    setSeatError('');
    seatService.list(data.flightId, { seatClass: data.fareClass, signal: controller.signal })
      .then((list) => {
        if (!controller.signal.aborted) setSeats(Array.isArray(list) ? list : []);
      })
      .catch((e) => {
        if (!controller.signal.aborted) setSeatError(apiMessageVi(e.message));
      })
      .finally(() => {
        if (!controller.signal.aborted) setSeatLoading(false);
      });

    return () => controller.abort();
  }, [data?.flightId, data?.fareClass, data?.status, tickets.length, seatReload]);

  const displayStatus = bookingDisplayStatus(data);
  const expired = isBookingHoldExpired(data);
  const statusClass = String(displayStatus || '').toLowerCase();
  const ticketQuantity = Number(data?.ticketQuantity) || 0;
  const canAddPassenger = data?.status === 'CONFIRMED' && passengers.length < ticketQuantity;
  const slotsFull = data?.status === 'CONFIRMED' && ticketQuantity > 0 && passengers.length >= ticketQuantity;
  const canPickSeat = data?.status === 'CONFIRMED' && tickets.length > 0;

  const selectedPassenger = tickets.find((t) => t.ticketId === selectedTicketId) || null;
  const seatById = useMemo(() => {
    const map = new Map();
    seats.forEach((seat) => map.set(seat.id, seat));
    return map;
  }, [seats]);

  const draftSeatOwner = useMemo(() => {
    const map = new Map();
    Object.entries(draftSeats).forEach(([ticketId, seatId]) => {
      if (seatId) map.set(seatId, ticketId);
    });
    return map;
  }, [draftSeats]);

  const hasSeatDraftChanges = tickets.some((t) => {
    const draft = draftSeats[t.ticketId] ?? null;
    const saved = t.seatId || null;
    return draft !== saved;
  });

  let paymentNote = 'Trạng thái booking hiện tại.';
  if (data?.status === 'CONFIRMED') paymentNote = 'Booking đã được xác nhận sau thanh toán. Hãy thêm hành khách và chọn ghế.';
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

  function seatNumberOf(seatId) {
    if (!seatId) return null;
    return seatById.get(seatId)?.seatNumber || null;
  }

  function pickSeatForSelected(seat) {
    if (!selectedTicketId || seatBusy) return;

    const ownerTicketId = draftSeatOwner.get(seat.id);
    const currentDraft = draftSeats[selectedTicketId] ?? null;
    const savedSeatId = selectedPassenger?.seatId || null;

    const isAvailable = seat.status === 'AVAILABLE';
    const isOwnSaved = seat.id === savedSeatId;
    const isOwnDraft = seat.id === currentDraft;
    const takenByOtherDraft = ownerTicketId && ownerTicketId !== selectedTicketId;

    if (takenByOtherDraft) {
      toast('Ghế này đang được chọn cho hành khách khác.', 'error');
      return;
    }

    if (!isAvailable && !isOwnSaved && !isOwnDraft) return;

    setDraftSeats((prev) => ({
      ...prev,
      [selectedTicketId]: isOwnDraft ? null : seat.id
    }));
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
      if (result?.ticket?.ticketId) {
        setSelectedTicketId(result.ticket.ticketId);
      }
      await loadDetail();
      setSeatReload((v) => v + 1);
    } catch (err) {
      const message = apiMessageVi(err.message || 'Không thêm được hành khách.');
      setFormError(message);
      toast(message, 'error');
    } finally {
      setSaving(false);
    }
  }

  async function saveSeatAssignments() {
    if (!hasSeatDraftChanges || seatBusy) return;
    setSeatBusy(true);
    try {
      for (const ticket of tickets) {
        const draft = draftSeats[ticket.ticketId] ?? null;
        const saved = ticket.seatId || null;
        if (draft === saved) continue;
        if (draft == null) {
          await ticketService.removeSeat(ticket.ticketId);
        } else {
          await ticketService.assignSeat(ticket.ticketId, draft);
        }
      }
      toast('Đã lưu ghế cho các hành khách.');
      await loadDetail();
      setSeatReload((v) => v + 1);
    } catch (err) {
      toast(apiMessageVi(err.message || 'Không lưu được ghế.'), 'error');
      await loadDetail().catch(() => {});
      setSeatReload((v) => v + 1);
    } finally {
      setSeatBusy(false);
    }
  }

  function clearSelectedDraftSeat() {
    if (!selectedTicketId) return;
    setDraftSeats((prev) => ({ ...prev, [selectedTicketId]: null }));
  }

  function seatLabel(seat) {
    const ownerTicketId = draftSeatOwner.get(seat.id);
    if (ownerTicketId === selectedTicketId) return 'Đang chọn';
    if (ownerTicketId) {
      const owner = tickets.find((t) => t.ticketId === ownerTicketId);
      return owner ? owner.fullName.split(' ').slice(-1)[0] : 'Đã chọn';
    }
    return SEAT_STATUS_VI[seat.status] || seat.status;
  }

  function seatClassName(seat) {
    const ownerTicketId = draftSeatOwner.get(seat.id);
    const isSelectedDraft = ownerTicketId === selectedTicketId;
    const isOtherDraft = ownerTicketId && ownerTicketId !== selectedTicketId;
    const classes = ['seat-catalog-item', `seat-${String(seat.status || '').toLowerCase()}`];
    if (isSelectedDraft) classes.push('seat-mine');
    if (isOtherDraft) classes.push('seat-owned');
    const canPick =
      seat.status === 'AVAILABLE'
      || seat.id === (selectedPassenger?.seatId || null)
      || isSelectedDraft;
    if (canPick && !isOtherDraft) classes.push('seat-selectable');
    return classes.join(' ');
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
              <p>Theo dõi trạng thái giữ chỗ, thanh toán, hành khách và ghế ngồi.</p>
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

          <div className="trip-detail-stack">
            <div className="account-card detail-passengers" style={{ marginTop: 0 }}>
              <h3>
                <Users size={19} /> Hành khách trong booking
                <span className="counter">{passengers.length}/{ticketQuantity || 0}</span>
              </h3>

              {passengers.length ? (
                <div className="detail-list">
                  {passengers.map((p, i) => {
                    const draftSeatId = p.ticketId ? (draftSeats[p.ticketId] ?? p.seatId) : null;
                    const assigned = draftSeatId ? seatById.get(draftSeatId) : null;
                    return (
                      <div key={p.id || i}>
                        <span>{p.fullName}</span>
                        <b>
                          {p.ticketNumber ? `Vé ${p.ticketNumber}` : 'Chưa có vé'}
                          {assigned
                            ? ` · Ghế ${assigned.seatNumber}`
                            : draftSeatId
                              ? ' · Đã chọn ghế'
                              : ' · Chưa chọn ghế'}
                        </b>
                      </div>
                    );
                  })}
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
                <form className="stack-form" style={{ marginTop: 22 }} onSubmit={submitPassenger}>
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

            {canPickSeat && (
              <div className="account-card seat-assign-card">
                <h3><Ticket size={19} /> Chọn ghế · {data.fareClass}</h3>
                <p className="seat-assign-hint">
                  Bấm một hành khách để chọn, rồi bấm ghế trống. Sau khi chọn xong cho từng người, bấm Lưu ghế.
                </p>

                <div className="seat-pax-grid" role="listbox" aria-label="Danh sách hành khách">
                  {tickets.map((t) => {
                    const draftSeatId = draftSeats[t.ticketId] ?? null;
                    const seatNo = seatNumberOf(draftSeatId);
                    const saved = t.seatId || null;
                    const pending = draftSeatId !== saved;
                    const active = t.ticketId === selectedTicketId;
                    return (
                      <button
                        key={t.ticketId}
                        type="button"
                        role="option"
                        aria-selected={active}
                        className={`seat-pax-card${active ? ' is-active' : ''}`}
                        onClick={() => setSelectedTicketId(t.ticketId)}
                        disabled={seatBusy}
                      >
                        <strong>{t.fullName}</strong>
                        <small>Vé {t.ticketNumber}</small>
                        <div className={`seat-pax-seat${pending ? ' is-pending' : ''}`}>
                          {seatNo
                            ? `Ghế ${seatNo}${pending ? ' (chưa lưu)' : ''}`
                            : pending
                              ? 'Sẽ gỡ ghế (chưa lưu)'
                              : 'Chưa chọn ghế'}
                        </div>
                      </button>
                    );
                  })}
                </div>

                {selectedPassenger && (
                  <p className="seat-assign-hint">
                    Đang chọn ghế cho <b>{selectedPassenger.fullName}</b>
                    {draftSeats[selectedTicketId]
                      ? ` · ghế nháp: ${seatNumberOf(draftSeats[selectedTicketId]) || '…'}`
                      : ' · chưa chọn ghế'}
                  </p>
                )}

                {seatLoading ? (
                  <Busy label="Đang tải ghế..." />
                ) : seatError ? (
                  <div className="inline-error" role="alert">
                    {seatError}{' '}
                    <button type="button" onClick={() => setSeatReload((v) => v + 1)}>Thử lại</button>
                  </div>
                ) : seats.length ? (
                  <div className="seat-catalog-grid">
                    {seats.map((seat) => {
                      const ownerTicketId = draftSeatOwner.get(seat.id);
                      const isSelectedDraft = ownerTicketId === selectedTicketId;
                      const isOtherDraft = ownerTicketId && ownerTicketId !== selectedTicketId;
                      const canPick =
                        !seatBusy
                        && !isOtherDraft
                        && (
                          seat.status === 'AVAILABLE'
                          || seat.id === (selectedPassenger?.seatId || null)
                          || isSelectedDraft
                        );
                      return (
                        <button
                          key={seat.id}
                          type="button"
                          className={seatClassName(seat)}
                          disabled={!canPick}
                          onClick={() => pickSeatForSelected(seat)}
                          title={seatLabel(seat)}
                        >
                          <strong>{seat.seatNumber}</strong>
                          <small>{seatLabel(seat)}</small>
                        </button>
                      );
                    })}
                  </div>
                ) : (
                  <p>Chưa có ghế được khai báo cho hạng vé này.</p>
                )}

                <div className="seat-assign-actions">
                  <button
                    type="button"
                    className="btn btn-primary"
                    disabled={!hasSeatDraftChanges || seatBusy}
                    onClick={saveSeatAssignments}
                  >
                    {seatBusy ? 'Đang lưu...' : 'Lưu ghế'}
                  </button>
                  <button
                    type="button"
                    className="btn btn-outline"
                    disabled={!selectedTicketId || seatBusy || !(draftSeats[selectedTicketId] ?? null)}
                    onClick={clearSelectedDraftSeat}
                  >
                    Bỏ ghế khách đang chọn
                  </button>
                </div>
              </div>
            )}
          </div>
        </>
      ) : null}
    </section>
  );
}
