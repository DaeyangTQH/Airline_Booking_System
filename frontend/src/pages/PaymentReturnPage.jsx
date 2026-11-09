import { Link, useSearchParams } from 'react-router-dom';
import { ArrowLeft, CircleCheck, CircleX, Ban } from 'lucide-react';

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
  }
};

export default function PaymentReturnPage() {
  const [params] = useSearchParams();
  const status = (params.get('status') || 'ERROR').toUpperCase();
  const invoice = params.get('invoice') || '';
  const view = COPY[status] || COPY.ERROR;
  const Icon = view.icon;

  return (
    <section className="shell account-page">
      <Link className="back-link" to="/trips">
        <ArrowLeft size={17} /> Về danh sách đặt chỗ
      </Link>

      <div className={`account-card payment-return payment-return-${view.tone}`}>
        <div className="payment-return-icon"><Icon size={34} /></div>
        <span className="eyebrow">KẾT QUẢ THANH TOÁN</span>
        <h1>{view.title}</h1>
        <p>{view.message}</p>
        {invoice ? (
          <div className="detail-list" style={{ marginTop: 18 }}>
            <div><span>Mã hóa đơn / payment</span><b className="long-id">{invoice}</b></div>
            <div><span>Trạng thái SePay return</span><b>{status}</b></div>
          </div>
        ) : null}
        <div className="payment-return-actions">
          <Link className="btn btn-primary" to="/trips">Xem chuyến đi</Link>
          {status !== 'SUCCESS' ? (
            <Link className="btn btn-outline" to="/trips">Chọn booking để thanh toán lại</Link>
          ) : null}
        </div>
      </div>
    </section>
  );
}
