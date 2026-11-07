import { useEffect, useState } from 'react';
import { ArrowLeftRight, ArrowRight, CalendarDays, MapPin, Search, UsersRound } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { AIRPORTS, dateInputValue } from '../data/cities';
const choose = (code) => AIRPORTS.find(x=>x.code===code)?.city || code;
export default function SearchWidget({defaultValues={},compact=false}){
  const nav=useNavigate();
  const [trip,setTrip]=useState(defaultValues.trip||'roundtrip');
  const [from,setFrom]=useState(defaultValues.from||'HAN');
  const [to,setTo]=useState(defaultValues.to||'SGN');
  const [departure,setDeparture]=useState(defaultValues.departure||dateInputValue(7));
  const [returnDate,setReturnDate]=useState(defaultValues.returnDate||dateInputValue(13));
  const [passengers,setPassengers]=useState(Number(defaultValues.passengers)||1);
  const [cabin,setCabin]=useState(defaultValues.cabin||'ECONOMY');
  const [direct,setDirect]=useState(false);
  useEffect(()=>{if(defaultValues.from)setFrom(defaultValues.from);if(defaultValues.to)setTo(defaultValues.to);},[defaultValues.from,defaultValues.to]);
  const submit=e=>{e.preventDefault();if(from===to)return;const q=new URLSearchParams({from,to,departure,passengers:String(passengers),cabin,trip}); if(trip==='roundtrip'&&returnDate)q.set('returnDate',returnDate);if(direct)q.set('direct','true');nav(`/flights?${q}`);};
  return <div className={`search-widget ${compact?'search-compact':''}`}><div className="search-topline"><div className="trip-switch" role="group" aria-label="Loại vé"><button className={trip==='roundtrip'?'active':''} onClick={()=>setTrip('roundtrip')} type="button"><i/> Khứ hồi</button><button className={trip==='oneway'?'active':''} onClick={()=>setTrip('oneway')} type="button"><i/> Một chiều</button></div><span className="search-help"><span className="tiny-plane">✈</span> Chuyến đi tuyệt vời bắt đầu từ đây</span></div>
  <form onSubmit={submit} className="search-grid">
    <label className="search-field"><span className="field-hint"><MapPin size={14}/> Điểm khởi hành</span><select value={from} onChange={e=>setFrom(e.target.value)}>{AIRPORTS.map(a=><option key={a.code} value={a.code}>{a.city}</option>)}</select><span className="field-sub">{from} · {AIRPORTS.find(a=>a.code===from)?.airport}</span></label>
    <button type="button" className="swap-btn" onClick={()=>{setFrom(to);setTo(from);}} aria-label="Đổi điểm đi và điểm đến"><ArrowLeftRight size={18}/></button>
    <label className="search-field"><span className="field-hint"><MapPin size={14}/> Điểm đến</span><select value={to} onChange={e=>setTo(e.target.value)}>{AIRPORTS.map(a=><option key={a.code} value={a.code}>{a.city}</option>)}</select><span className="field-sub">{to} · {AIRPORTS.find(a=>a.code===to)?.airport}</span></label>
    <label className="search-field"><span className="field-hint"><CalendarDays size={14}/> Ngày đi</span><input type="date" min={dateInputValue(0)} value={departure} onChange={e=>{setDeparture(e.target.value);if(returnDate<e.target.value)setReturnDate(e.target.value);}} required/><span className="field-sub">Chọn ngày khởi hành</span></label>
    <label className={`search-field ${trip==='oneway'?'is-disabled':''}`}><span className="field-hint"><CalendarDays size={14}/> Ngày về</span><input type="date" min={departure} value={trip==='oneway'?'':returnDate} disabled={trip==='oneway'} onChange={e=>setReturnDate(e.target.value)} required={trip==='roundtrip'}/><span className="field-sub">{trip==='oneway'?'Vé một chiều':'Chọn ngày trở về'}</span></label>
    <label className="search-field travellers-field"><span className="field-hint"><UsersRound size={14}/> Hành khách / Hạng vé</span><span className="travel-selects"><select aria-label="Số hành khách" value={passengers} onChange={e=>setPassengers(Number(e.target.value))}>{[1,2,3,4,5,6,7,8,9].map(n=><option key={n} value={n}>{n} khách</option>)}</select><select aria-label="Hạng vé" value={cabin} onChange={e=>setCabin(e.target.value)}><option value="ECONOMY">Phổ thông</option><option value="BUSINESS">Thương gia</option><option value="FIRST">Hạng nhất</option></select></span><span className="field-sub">Tùy chọn phù hợp hành trình</span></label>
    <button className="btn btn-primary search-submit" type="submit"><Search size={19}/> Tìm chuyến bay <ArrowRight size={19}/></button>
  </form>
  <div className="search-bottom"><label className="check-inline"><input type="checkbox" checked={direct} onChange={e=>setDirect(e.target.checked)}/> Chỉ chuyến bay thẳng</label>{from===to?<span className="text-error">Điểm đi và điểm đến phải khác nhau</span>:<span>Giá tốt hơn khi bạn linh hoạt ngày bay <span className="sparkle">✦</span></span>}</div>
  </div>;
}
