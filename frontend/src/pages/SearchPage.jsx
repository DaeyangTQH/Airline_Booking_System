import { useMemo, useState } from 'react';
import { ArrowRight, BadgeInfo, Check, ChevronDown, CircleAlert, Plane, SlidersHorizontal, Sparkles } from 'lucide-react';
import { useSearchParams } from 'react-router-dom';
import SearchWidget from '../components/SearchWidget';
import FlightCard from '../components/FlightCard';
import { airportByCode, displayDate, money, dateInputValue } from '../data/cities';
import { demoFlights } from '../data/marketing';
export default function SearchPage(){
  const [params]=useSearchParams();const [sort,setSort]=useState('recommended');const [airlines,setAirlines]=useState('all');const [morning,setMorning]=useState(false);
  const from=params.get('from')||'HAN';const to=params.get('to')||'SGN';const departure=params.get('departure')||dateInputValue(7);const passengers=Number(params.get('passengers')||1);
  const flights=useMemo(()=>{
    let list=demoFlights.filter(f=>airlines==='all'||f.short===airlines).filter(f=>!morning||Number(f.depart.slice(0,2))<12);
    return [...list].sort((a,b)=>sort==='price'?a.price-b.price:sort==='early'?a.depart.localeCompare(b.depart):0);
  },[sort,airlines,morning]);
  return <><section className="search-result-head"><div className="shell"><div className="search-results-hero"><div><div className="eyebrow"><Plane size={16}/> KHÁM PHÁ CHUYẾN ĐI</div><h1>{airportByCode(from).city} <ArrowRight size={23} strokeWidth={1.6}/> {airportByCode(to).city}</h1><p>{displayDate(departure)} · {passengers} hành khách · {params.get('trip')==='oneway'?'Một chiều':'Khứ hồi'}</p></div><span className="search-bubble">Cùng bay đến chân trời mới ✦</span></div><SearchWidget compact defaultValues={{from,to,departure,returnDate:params.get('returnDate')||dateInputValue(13),passengers,cabin:params.get('cabin')||'ECONOMY',trip:params.get('trip')||'roundtrip'}}/></div></section>
<section className="shell search-results-body"><div className="demo-banner"><BadgeInfo size={22}/><div><strong>Chế độ xem trước giao diện</strong><p>Flight Search Service trong bản backend hiện tại chưa có REST API. Các chuyến bay và giá bên dưới là <b>dữ liệu minh họa</b>, không phải vé đang mở bán. Bạn chưa thể đặt vé thật từ danh sách này.</p></div></div>
<div className="results-layout"><aside className="filter-panel"><div className="filter-head"><h3><SlidersHorizontal size={18}/> Bộ lọc</h3><button onClick={()=>{setAirlines('all');setMorning(false);setSort('recommended')}}>Đặt lại</button></div><div className="filter-group"><h4>Hãng hàng không</h4>{[['all','Tất cả hãng'],['VN','Vietnam Airlines'],['VJ','Vietjet Air'],['QH','Bamboo Airways']].map(([value,label])=><label key={value} className="filter-option"><input type="radio" name="airline" checked={airlines===value} onChange={()=>setAirlines(value)}/>{label}</label>)}</div><div className="filter-group"><h4>Thời gian khởi hành</h4><label className="filter-option"><input type="checkbox" checked={morning} onChange={e=>setMorning(e.target.checked)}/> Trước 12:00</label></div><div className="filter-note"><Sparkles size={17}/><p>Tùy chọn bộ lọc hiện hoạt động trên dữ liệu demo.</p></div></aside>
<div className="results-main"><div className="results-toolbar"><div><h2>Chuyến bay dành cho bạn</h2><p>Đang hiển thị {flights.length} phương án minh họa</p></div><label className="sort-field">Sắp xếp <select value={sort} onChange={e=>setSort(e.target.value)}><option value="recommended">Đề xuất</option><option value="price">Giá thấp nhất</option><option value="early">Giờ bay sớm</option></select><ChevronDown size={15}/></label></div>
<div className="results-list">{flights.map(f=><FlightCard key={f.id} flight={f} from={from} to={to} departure={departure} passengers={passengers}/>)}</div>
{!flights.length&&<div className="no-results"><CircleAlert size={25}/><h3>Không có chuyến bay phù hợp</h3><p>Thử điều chỉnh bộ lọc để xem thêm các chuyến bay minh họa.</p></div>}
<div className="results-tip"><Check size={17}/> Mẹo: Sau khi nhóm tích hợp API tìm chuyến bay, có thể thay dữ liệu demo trong <code>SearchPage.jsx</code> bằng dữ liệu trả về từ backend.</div></div></div></section></>
}
