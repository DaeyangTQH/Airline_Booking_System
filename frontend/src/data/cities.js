export const AIRPORTS = [
  { code: 'HAN', city: 'Hà Nội', airport: 'Nội Bài' },
  { code: 'SGN', city: 'TP. Hồ Chí Minh', airport: 'Tân Sơn Nhất' },
  { code: 'DAD', city: 'Đà Nẵng', airport: 'Đà Nẵng' },
  { code: 'CXR', city: 'Nha Trang', airport: 'Cam Ranh' },
  { code: 'PQC', city: 'Phú Quốc', airport: 'Phú Quốc' },
  { code: 'DLI', city: 'Đà Lạt', airport: 'Liên Khương' },
  { code: 'HUI', city: 'Huế', airport: 'Phú Bài' },
  { code: 'VDO', city: 'Quảng Ninh', airport: 'Vân Đồn' },
  { code: 'BKK', city: 'Bangkok', airport: 'Suvarnabhumi' },
  { code: 'SIN', city: 'Singapore', airport: 'Changi' }
];
export const airportByCode = (code) => AIRPORTS.find(a => a.code === code) || { code, city: code || '—', airport: '' };
export const dateInputValue = (days = 5) => { const d = new Date(); d.setDate(d.getDate() + days); return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`; };
export const money = (value, currency = 'VND') => value == null ? '—' : new Intl.NumberFormat('vi-VN',{ style:'currency', currency: currency || 'VND', maximumFractionDigits:0 }).format(Number(value));
export const displayDate = (s) => !s ? '—' : new Date(s.length === 10 ? `${s}T00:00:00` : s).toLocaleDateString('vi-VN',{ day:'2-digit', month:'short', year:'numeric' });
export const uuidValid = (str='') => /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(str);
