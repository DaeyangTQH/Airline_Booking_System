import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { ArrowLeft, CircleCheck, CircleX, Ban, LoaderCircle } from 'lucide-react';
import { paymentService } from '../services/paymentService';

const COPY = {
  SUCCESS: {
    icon: CircleCheck,
    title: 'Thanh toán thành công',
    message: 'SePay đã xác nhận giao dịch. Booking của bạn sẽ được cập nhật trạng thái xác nhận.',
    tone: 'success'
  },
  ERROR: {
    icon: CircleX,
    title: 'Thanh toán thất bại',
    message: 'Giao dịch không hoàn tất. Bạn có thể quay lại trang thanh toán và thử lại.',
    tone: 'error'
  },
  CANCEL: {
    icon: Ban,
    title: 'Đã hủy thanh toán',
    message: 'Bạn đã hủy trên cổng SePay. Booking vẫn đang chờ thanh toán nếu còn hạn giữ chỗ.',
    tone: 'cancel'
  },
  PENDING: {
    icon: LoaderCircle,
    title: 'Đang đồng bộ thanh toán',
    message: 'SePay đã redirect về, hệ thống đang cập nhật trạng thái payment vào database.',
    tone: 'cancel'
  }
};

const LAST_PAYMENT_KEY = 'skyport_last_payment';

function readLastPayment() {
  try {
    return JSON.parse(sessionStorage.getItem(LAST_PAYMENT_KEY) || 'null');
  } catch {
    return null;
  }
}

/** paymentUuid hoặc paymentUuid-suffix → paymentUuid */
function extractPaymentId(raw) {
  if (!raw) return '';
  const value = String(raw).trim();
  if (value.length > 36 && value[36] === '-') return value.slice(0, 36);
  return value;
}

export default function PaymentReturnPage() {
  const [params] = useSearchParams();
  const returnStatus = (params.get('status') || 'ERROR').toUpperCase();
  const invoice = params.get('invoice') || '';
  const lastPayment = readLastPayment();
  const paymentId = extractPaymentId(invoice || lastPayment?.paymentId || '');
  const bookingId = lastPayment?.bookingId || '';

  const [dbStatus, setDbStatus] = useState('');
  const [pollError, setPollError] = useState('');

  useEffect(() => {
    if (!paymentId || returnStatus !== 'SUCCESS') return undefined;

    let cancelled = false;
    let attempts = 0;
    const maxAttempts = 8;

    async function poll() {
      attempts += 1;
      try {
        const data = await paymentService.status(paymentId);
        if (cancelled) return;
        const status = (data?.status || '').toUpperCase();
        setDbStatus(status);
        setPollError('');
        if (status === 'COMPLETED' || status === 'FAILED' || status === 'CANCELLED') {
          return;
        }
      } catch (e) {
        if (!cancelled) setPollError(e.message || 'Không đọc được trạng thái payment.');
      }
      if (!cancelled && attempts < maxAttempts) {
        window.setTimeout(poll, 1200);
      }
    }

    poll();
    return () => { cancelled = true; };
  }, [paymentId, returnStatus]);

  const effectiveStatus = returnStatus === 'SUCCESS'
    ? (dbStatus === 'COMPLETED' ? 'SUCCESS' : dbStatus === 'FAILED' ? 'ERROR' : dbStatus ? 'PENDING' : 'PENDING')
    : returnStatus;
  const view = COPY[effectiveStatus] || COPY.ERROR;
  const Icon = view.icon;

  return (
    <section className="shell account-page">
      <Link className="back-link" to="/trips">
        <ArrowLeft size={17} /> Về danh sách đặt chỗ
      </Link>

      <div className={`account-card payment-return payment-return-${view.tone}`}>
        <div className="payment-return-icon"><Icon size={34} className={effectiveStatus === 'PENDING' ? 'sp-auth__spinner' : undefined} /></div>
        <span className="eyebrow">KẾT QUẢ THANH TOÁN</span>
        <h1>{view.title}</h1>
        <p>{view.message}</p>
        <div className="detail-list" style={{ marginTop: 18 }}>
          {paymentId ? (
            <div><span>Payment ID</span><b className="long-id">{paymentId}</b></div>
          ) : null}
          <div><span>Return từ SePay</span><b>{returnStatus}</b></div>
          {dbStatus ? (
            <div><span>Trạng thái trong DB</span><b>{dbStatus}</b></div>
          ) : null}
          {pollError ? (
            <div><span>Lỗi đồng bộ</span><b>{pollError}</b></div>
          ) : null}
        </div>
        <div className="payment-return-actions">
          <Link className="btn btn-primary" to={bookingId ? `/trips/${bookingId}` : '/trips'}>
            {bookingId ? 'Xem chi tiết booking' : 'Xem chuyến đi'}
          </Link>
          {effectiveStatus !== 'SUCCESS' && bookingId ? (
            <Link className="btn btn-outline" to={`/payment/${bookingId}`}>Thanh toán lại</Link>
          ) : null}
        </div>
      </div>
    </section>
  );
}
