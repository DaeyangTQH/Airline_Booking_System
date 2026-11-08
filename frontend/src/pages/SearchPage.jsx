import { useEffect, useMemo, useState } from 'react';
import { ArrowRight, BadgeInfo, ChevronDown, CircleAlert, Plane, SlidersHorizontal } from 'lucide-react';
import { useSearchParams } from 'react-router-dom';
import SearchWidget from '../components/SearchWidget';
import FlightCard from '../components/FlightCard';
import Busy from '../components/Busy';
import { airportByCode, displayDate, dateInputValue } from '../data/cities';
import { flightService, flightCardData } from '../services/flightService';

export default function SearchPage() {
  const [params] = useSearchParams();
  const from = params.get('from') || 'HAN', to = params.get('to') || 'SGN';
  const departure = params.get('departure') || dateInputValue(7);
  const returnDate = params.get('returnDate') || dateInputValue(13);
  const passengers = Math.max(1, Math.min(9, Number(params.get('passengers')) || 1));
  const cabin = params.get('cabin') || 'ECONOMY', roundtrip = params.get('trip') !== 'oneway';
  const [leg, setLeg] = useState('outbound'), [page, setPage] = useState(0);
  const [sort, setSort] = useState('recommended'), [morning, setMorning] = useState(false);
  const [rows, setRows] = useState([]), [loading, setLoading] = useState(true);
  const [error, setError] = useState(''), [reload, setReload] = useState(0), [hasNext, setHasNext] = useState(false);
  const returnLeg = roundtrip && leg === 'return';
  const searchFrom = returnLeg ? to : from, searchTo = returnLeg ? from : to;
  const searchDate = returnLeg ? returnDate : departure;
  useEffect(() => { setPage(0); }, [searchFrom, searchTo, searchDate, cabin, passengers]);
  useEffect(() => {
    const controller = new AbortController();
    setLoading(true); setError(''); setRows([]);
    flightService.search({ from: searchFrom, to: searchTo, departure: searchDate, cabin, passengers, page, signal: controller.signal })
      .then(data => {
        if (controller.signal.aborted) return;
        if (!Array.isArray(data)) throw new Error('Danh sách chuyến bay không hợp lệ.');
        setRows(data); setHasNext(data.length === 20);
      })
      .catch(e => { if (!controller.signal.aborted) setError(e.message); })
      .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [searchFrom, searchTo, searchDate, cabin, passengers, page, reload]);
  const flights = useMemo(() => rows.map(flight => flightCardData(flight, cabin)).filter(Boolean)
    .filter(flight => !morning || Number(flight.depart.slice(0, 2)) < 12)
    .sort((a, b) => sort === 'price' ? a.currency.localeCompare(b.currency) || a.price - b.price
      : Date.parse(a.departureTime) - Date.parse(b.departureTime)), [rows, cabin, morning, sort]);

  return <>
    <section className="search-result-head"><div className="shell"><div className="search-results-hero"><div>
      <div className="eyebrow"><Plane size={16}/> KHÁM PHÁ CHUYẾN ĐI</div>
      <h1>{airportByCode(from).city} <ArrowRight size={23}/> {airportByCode(to).city}</h1>
      <p>{displayDate(departure)} · {passengers} hành khách · {roundtrip ? 'Khứ hồi' : 'Một chiều'}</p>
    </div></div><SearchWidget compact defaultValues={{ from, to, departure, returnDate, passengers, cabin, trip: roundtrip ? 'roundtrip' : 'oneway' }}/></div></section>
    <section className="shell search-results-body">
      <div className="demo-banner"><BadgeInfo size={22}/><div><strong>Lịch bay và giá từ hệ thống</strong>
        <p>Dữ liệu mẫu dùng để thử nghiệm. Kết quả lọc theo số hành khách và ghế chưa giữ; xem kết quả chưa giữ chỗ cho bạn.{roundtrip && ' Xem riêng từng chiều; chưa ghép thành vé khứ hồi.'}</p>
      </div></div>
      {roundtrip && <div className="booking-buttons" role="group" aria-label="Chiều bay">
        <button className={returnLeg ? 'btn btn-outline' : 'btn btn-primary'} onClick={() => { setLeg('outbound'); setPage(0); }}>Chiều đi · {displayDate(departure)}</button>
        <button className={returnLeg ? 'btn btn-primary' : 'btn btn-outline'} onClick={() => { setLeg('return'); setPage(0); }}>Chiều về · {displayDate(returnDate)}</button>
      </div>}
      <div className="results-layout"><aside className="filter-panel">
        <div className="filter-head"><h3><SlidersHorizontal size={18}/> Bộ lọc</h3><button onClick={() => { setMorning(false); setSort('recommended'); }}>Đặt lại</button></div>
        <div className="filter-group"><h4>Thời gian khởi hành</h4><label className="filter-option">
          <input type="checkbox" checked={morning} onChange={e => setMorning(e.target.checked)}/> Trước 12:00
        </label></div>
        <div className="filter-note"><p>Bộ lọc giờ và sắp xếp áp dụng cho trang kết quả hiện tại. Giá được nhóm theo tiền tệ khi sắp xếp.</p></div>
      </aside><div className="results-main">
        <div className="results-toolbar"><div><h2>{searchFrom} → {searchTo}</h2><p>{displayDate(searchDate)} · {flights.length} chuyến trên trang {page + 1}</p></div>
          <label className="sort-field">Sắp xếp <select value={sort} onChange={e => setSort(e.target.value)}>
            <option value="recommended">Giờ khởi hành</option><option value="price">Giá thấp nhất</option>
          </select><ChevronDown size={15}/></label>
        </div>
        {loading ? <Busy label="Đang tìm chuyến bay..."/> : error ? <div className="inline-error" role="alert">{error} <button onClick={() => setReload(value => value + 1)}>Thử lại</button></div>
          : flights.length ? <div className="results-list">{flights.map(flight => <FlightCard key={flight.id} flight={flight} departure={searchDate} passengers={passengers}/>)}</div>
            : <div className="no-results"><CircleAlert size={25}/><h3>Không có chuyến bay phù hợp</h3><p>Thử đổi ngày bay, hạng vé hoặc bộ lọc giờ khởi hành.</p></div>}
        {!loading && !error && <div className="booking-buttons">
          <button className="btn btn-outline" disabled={page === 0} onClick={() => setPage(value => value - 1)}>Trang trước</button>
          <button className="btn btn-outline" disabled={!hasNext} onClick={() => setPage(value => value + 1)}>Trang sau</button>
        </div>}
      </div></div>
    </section>
  </>;
}
