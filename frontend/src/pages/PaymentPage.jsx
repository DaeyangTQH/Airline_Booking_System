import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ArrowLeft, BadgeInfo, Building2, CreditCard, LockKeyhole } from 'lucide-react';
import { bookingService } from '../services/bookingService';
import { money } from '../data/cities';
import Busy from '../components/Busy';
import {
  apiMessageVi,
  bookingDisplayStatus,
  bookingStatusLabel,
  isBookingHoldExpired
} from '../utils/bookingUi';

export default function PaymentPage() {
  const { bookingId } = useParams();
  const [booking, setBooking] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [paying, setPaying] = useState(false);

  useEffect(() => {
    let active = true;
    bookingService.detail(bookingId)
      .then((data) => { if (active) setBooking(data); })
      .catch((e) => { if (active) setError(apiMessageVi(e.message)); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [bookingId]);

  const expired = isBookingHoldExpired(booking);
  const displayStatus = bookingDisplayStatus(booking);
  const canPay = booking?.status === 'PENDING' && !expired;
  const alreadyPaid = booking?.status === 'CONFIRMED';

  async function startPayment() {
    if (!canPay || paying) return;
    setPaying(true);
    setError('');
    try {
      const result = await bookingService.initiatePayment(bookingId);
      if (result?.paymentId) {
        sessionStorage.setItem('skyport_last_payment', JSON.stringify({
          paymentId: result.paymentId,
          bookingId
        }));
      }
      if (result?.status === 'COMPLETED') {
        window.location.assign(`/payment-return?status=SUCCESS&invoice=${encodeURIComponent(result.paymentId || '')}`);
        return;
      }
      if (!result?.paymentUrl) {
        throw new Error('Không nhận được URL thanh toán từ hệ thống.');
      }
      window.location.assign(result.paymentUrl);
    } catch (e) {
      setError(apiMessageVi(e.message || 'Không khởi tạo được thanh toán.'));
      setPaying(false);
    }
  }

  return (
    <section className="shell account-page">
      <Link className="back-link" to={`/trips/${bookingId}`}>
        <ArrowLeft size={17} /> Quay về chi tiết đặt chỗ
      </Link>
      <div className="page-intro">
        <div>
          <span className="eyebrow">THANH TOÁN AN TOÀN</span>
          <h1>Thanh toán đặt chỗ</h1>
          <p>Thanh toán qua cổng SePay (chuyển khoản QR).</p>
        </div>
      </div>

      <div className="payment-layout">
        <div className="account-card payment-method">
          <h2><CreditCard size={21} /> Phương thức thanh toán</h2>
          <p>Chọn SePay rồi bấm thanh toán để mở trang checkout.</p>

          <div className={`payment-option ${canPay ? 'is-active' : ''}`}>
            <span className="payment-logo"><Building2 size={22} /></span>
            <div>
              <b>SePay — Chuyển khoản QR</b>
              <small>Redirect tới trang thanh toán SePay</small>
            </div>
            <span className="payment-radio" />
          </div>

          {alreadyPaid && (
            <div className="demo-banner">
              <BadgeInfo size={20} />
              <p>Booking này đã thanh toán và xác nhận. Bạn có thể xem lại trong danh sách chuyến đi.</p>
            </div>
          )}

          {expired && !alreadyPaid && (
            <div className="demo-banner">
              <BadgeInfo size={20} />
              <p>Booking đã hết hạn giữ chỗ. Vui lòng tạo đặt chỗ mới để tiếp tục thanh toán.</p>
            </div>
          )}

          {!alreadyPaid && !expired && !canPay && booking && (
            <div className="demo-banner">
              <BadgeInfo size={20} />
              <p>Booking đang ở trạng thái {bookingStatusLabel(displayStatus)}, không thể khởi tạo thanh toán.</p>
            </div>
          )}

          {error && <p className="inline-error" role="alert">{error}</p>}

          {canPay ? (
            <button className="btn btn-primary" type="button" disabled={paying || loading} onClick={startPayment}>
              <LockKeyhole size={16} /> {paying ? 'Đang chuyển tới SePay...' : 'Thanh toán với SePay'}
            </button>
          ) : alreadyPaid ? (
            <Link className="btn btn-primary" to="/trips">Xem chuyến đi của tôi</Link>
          ) : (
            <Link className="btn btn-primary" to="/flights">Tìm chuyến bay mới</Link>
          )}
        </div>

        <aside className="account-card payment-recap">
          <h3>Thông tin đơn hàng</h3>
          {loading ? <Busy /> : booking ? (
            <>
              <div className="detail-list">
                <div><span>Mã booking</span><b>{booking.bookingReference}</b></div>
                <div><span>Số vé</span><b>{booking.ticketQuantity}</b></div>
                <div><span>Trạng thái</span><b>{bookingStatusLabel(displayStatus)}</b></div>
              </div>
              <div className="detail-total">{money(booking.totalAmount, booking.currency)}</div>
            </>
          ) : null}
        </aside>
      </div>
    </section>
  );
}
