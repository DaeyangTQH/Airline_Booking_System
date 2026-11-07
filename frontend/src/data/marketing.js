// Các ảnh chỉ mang tính minh họa điểm đến; giá vé không phải giá thực.
export const destinations = [
  {code:'DAD',city:'Đà Nẵng', subtitle:'Biển xanh & nhịp sống rực rỡ', image:'https://images.unsplash.com/photo-1559592413-7cec4d0cae2b?auto=format&fit=crop&w=900&q=80', tag:'Được yêu thích'},
  {code:'PQC',city:'Phú Quốc',subtitle:'Thiên đường biển đảo',image:'https://images.unsplash.com/photo-1732243395944-cb3ff9311091?auto=format&fit=crop&w=900&q=80',tag:'Nghỉ dưỡng'},
  {code:'DLI',city:'Đà Lạt',subtitle:'Chạm vào những ngày bình yên',image:'https://images.unsplash.com/photo-1448375240586-882707db888b?auto=format&fit=crop&w=900&q=80',tag:'Trốn phố'},
  {code:'SGN',city:'TP. Hồ Chí Minh',subtitle:'Thành phố không ngủ',image:'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?auto=format&fit=crop&w=900&q=80',tag:'Khám phá'}
];

// Dữ liệu trình diễn UI – không phải kết quả API, không được dùng để đặt vé thật.
export const demoFlights = [
  { id:'demo-1', airline:'Vietnam Airlines', short:'VN', number:'VN 247', depart:'06:15', arrive:'08:25', duration:'2h 10m', stops:0, price:1290000, cabin:'ECONOMY', benefit:'Hành lý xách tay 10kg', accent:'#1750aa' },
  { id:'demo-2', airline:'Vietjet Air', short:'VJ', number:'VJ 129', depart:'09:45', arrive:'11:55', duration:'2h 10m', stops:0, price:890000, cabin:'ECONOMY', benefit:'Hành lý xách tay 7kg', accent:'#e84b43' },
  { id:'demo-3', airline:'Bamboo Airways', short:'QH', number:'QH 221', depart:'13:20', arrive:'15:35', duration:'2h 15m', stops:0, price:1150000, cabin:'ECONOMY', benefit:'Hành lý xách tay 7kg', accent:'#209779' },
  { id:'demo-4', airline:'Vietnam Airlines', short:'VN', number:'VN 261', depart:'17:10', arrive:'19:20', duration:'2h 10m', stops:0, price:1590000, cabin:'BUSINESS', benefit:'Hành lý linh hoạt', accent:'#1750aa' }
];
