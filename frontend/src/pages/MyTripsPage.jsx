import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, CalendarDays, Plane, Ticket, RefreshCw } from 'lucide-react';
import PageIntro from '../components/PageIntro';
import EmptyState from '../components/EmptyState';
import Busy from '../components/Busy';
import { bookingService } from '../services/bookingService';
import { displayDate, money } from '../data/cities';
import {
  apiMessageVi,
  bookingDisplayStatus,
  bookingStatusLabel
} from '../utils/bookingUi';

export default function MyTripsPage() {
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [tab, setTab] = useState('all');

  async function load() {
    setLoading(true);
    setError('');
    try {
      const d = await bookingService.mine();
      setList(Array.isArray(d) ? d : []);
    } catch (e) {
      setError(apiMessageVi(e.message));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, []);

  const shown = list.filter((x) => {
    const status = bookingDisplayStatus(x);
    if (tab === 'all') return true;
    if (tab === 'active') return ['PENDING', 'CONFIRMED'].includes(status);
    return !['PENDING', 'CONFIRMED'].includes(status);
  });

  return (
    <section className="shell account-page">
      <PageIntro
        eyebrow="HÀNH TRÌNH CỦA TÔI"
        title="Vé & đặt chỗ"
        description="Theo dõi tất cả booking của bạn tại một nơi."
      >
        <Link to="/flights" className="btn btn-primary">
          Tìm chuyến bay <ArrowRight size={17} />
        </Link>
      </PageIntro>

      <div className="account-card">
        <div className="account-card-header">
          <div className="segmented">
            {[['all', 'Tất cả'], ['active', 'Đang xử lý'], ['past', 'Khác']].map(([key, label]) => (
              <button key={key} className={tab === key ? 'active' : ''} onClick={() => setTab(key)}>
                {label}
              </button>
            ))}
          </div>
          <button className="link-button" onClick={load}>
            <RefreshCw size={16} /> Làm mới
          </button>
        </div>

        {loading ? (
          <Busy />
        ) : error ? (
          <div className="inline-error">
            {error} <button onClick={load}>Thử lại</button>
          </div>
        ) : shown.length ? (
          <div className="trip-list">
            {shown.map((b) => {
              const displayStatus = bookingDisplayStatus(b);
              return (
                <Link className="trip-row" to={`/trips/${b.id}`} key={b.id}>
                  <div className="trip-icon"><Ticket size={24} /></div>
                  <div className="trip-middle">
                    <div className="trip-ref">
                      {b.bookingReference}
                      <span className={`status-pill status-${String(displayStatus || '').toLowerCase()}`}>
                        {bookingStatusLabel(displayStatus)}
                      </span>
                    </div>
                    <div className="trip-meta">
                      <span><Plane size={14} /> {b.fareClass}</span>
                      <span><CalendarDays size={14} /> {displayDate(b.createdAt)}</span>
                      <span>{b.ticketQuantity} hành khách</span>
                    </div>
                  </div>
                  <div className="trip-end">
                    <b>{money(b.totalAmount, b.currency)}</b>
                    <ArrowRight size={19} />
                  </div>
                </Link>
              );
            })}
          </div>
        ) : (
          <EmptyState
            icon={Ticket}
            title="Bạn chưa có đặt chỗ nào"
            description="Khi bạn có booking thành công, thông tin sẽ xuất hiện ở đây."
            action={
              <Link className="btn btn-primary" to="/flights">
                Khám phá chuyến bay <ArrowRight size={17} />
              </Link>
            }
          />
        )}
      </div>
    </section>
  );
}
