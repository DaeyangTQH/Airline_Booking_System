# SkyPort V2 – Airline Booking System Frontend

Frontend ReactJS dạng **website đặt vé máy bay dành cho khách hàng**, không phải admin dashboard. Mỗi trang, component và API nằm trong file riêng.

## Cài đặt (Windows / IntelliJ Terminal / VS Code)

```powershell
cd frontend
npm install
npm run dev
```

Mở `http://localhost:5173`.

Vite chuyển `/api/**` đến `http://localhost:8080` (API Gateway). Khi muốn đổi proxy, tạo file `.env` dựa trên `.env.example` và đặt `VITE_GATEWAY_URL=http://localhost:8080`.

Khi backend đã chạy: PostgreSQL Docker `5432`, RabbitMQ `5672`, Eureka `8761`, Flight Search `8083`, Booking `8082`, User `8081`, Payment `8084`, Notification `8085`, API Gateway `8080`.

## Cấu trúc chính

```text
frontend/
├── index.html
├── package.json
├── vite.config.js
└── src/
    ├── App.jsx                   # Chỉ khai báo các routes
    ├── main.jsx
    ├── components/               # Header, Footer, SearchWidget, FlightCard, ...
    ├── context/                  # AuthProvider, ToastProvider
    ├── data/                     # Cities & mock flights (label demo)
    ├── layouts/SiteLayout.jsx
    ├── pages/                    # 11 trang riêng biệt
    ├── services/                 # HTTP & từng domain API
    └── styles/                   # Global, components, pages
```

## API đối chiếu với Airline_Booking_System(5).zip

Đã nối API qua Gateway:
- `POST /api/auth/login`, `POST /api/auth/register`
- `GET, PUT /api/users/me`, `PUT /api/users/me/password`
- `GET, POST /api/passengers`, `PUT, DELETE /api/passengers/{id}`
- `GET /api/v1/bookings/me`, `GET /api/v1/bookings/{id}`, `POST /api/v1/bookings`
- `POST /api/v1/reminders`
- `GET /api/admin/users`, `PATCH /api/admin/users/{id}/status`
- `GET /api/v1/airports?page=0&size=100`: SearchWidget tải đủ các trang danh mục sân bay, dùng `code`, `city` và ánh xạ `name` thành nhãn sân bay. API đọc này không yêu cầu đăng nhập; các API ghi vẫn theo chính sách xác thực hiện có. Widget hiển thị trạng thái tải/lỗi/danh mục rỗng và không dùng danh mục tĩnh thay cho kết quả API.
- `GET /api/v1/flights?origin=HAN&destination=SGN&departureDate=YYYY-MM-DD&fareClass=ECONOMY&quantity=1&page=0&size=20`: trang `/flights` truyền số hành khách (1-9); backend lọc giá còn hiệu lực và đủ ghế cùng hạng trước khi phân trang. Thẻ chuyến bay hiển thị số ghế chưa giữ/tổng đã khai báo. Bộ lọc giờ và sắp xếp áp dụng cho trang hiện tại; giá được nhóm theo tiền tệ. Khứ hồi hiển thị riêng từng chiều.
- `GET /api/v1/flights/{id}`: trang `/booking/:flightId` dùng UUID, lịch bay, model máy bay và các giá còn hiệu lực từ backend. Giờ/ngày bay hiển thị theo múi giờ Việt Nam. Đã bỏ danh sách chuyến bay hardcode và thông tin hãng/hành lý không có trong schema.
- `GET /api/v1/flights/{id}/seats?seatClass=ECONOMY&page=0&size=100`: màn chi tiết tải đủ các trang ghế theo hạng vé, sắp xếp số ghế tự nhiên, hiển thị trạng thái chỉ đọc và xử lý tải/lỗi/rỗng. Chưa chọn hoặc giữ ghế.
- `GET /api/v1/flights/{id}/availability`: màn chi tiết hiển thị số ghế chưa giữ và số ghế đã khai báo theo hạng vé; cảnh báo nếu không đủ cho số hành khách đã chọn. Mẫu mặc định ECONOMY 11/12, BUSINESS 6/6, FIRST 6/6. Xem số liệu chưa phải giữ chỗ.

Chưa thể nối thật:
- Dữ liệu Aircraft có sẵn ở backend; model máy bay được hiển thị qua API Flight. Flight Search chỉ cung cấp API đọc cho frontend, không có API quản lý Aircraft.
- Đã có Seat và Availability; phần gọi nội bộ giữa các service không thuộc các commit entity này. Xác nhận đặt chỗ vẫn bị khóa cho đến khi hoàn thiện giữ ghế và tích hợp Booking. Frontend không gửi internal key.
- Backend seed 14 chuyến bay và 42 giá vé (3 hạng/chuyến). Ngày đi là 7 ngày và ngày về là 13 ngày sau lần đầu seed Flight, theo múi giờ Việt Nam. Restart giữ nguyên lịch và giá đã có; chọn đúng ngày đã seed để xem kết quả.
- Seed ghế mẫu tối đa 24 ghế/chuyến (336 ghế cho 14 chuyến mới): 6 FIRST, 6 BUSINESS, 12 ECONOMY; 4F tạm khóa, các ghế còn lại AVAILABLE. Seed không vượt sức chứa và giữ nguyên các ghế đã có khi restart.
- `payment-service` chưa có REST API thanh toán trong ZIP; frontend không mô phỏng thanh toán thành công.
- Notification Service chỉ có tạo thông báo và lịch nhắc, chưa có API đọc danh sách thông báo, nên `/reminders` chỉ tạo lịch nhắc.

**Lưu ý bảo mật:** BookingController nhận `X-User-Id` từ frontend. Trong hệ thống thật, gateway/booking-service phải kiểm chứng hoặc tự gán header từ JWT thay vì tin header do client gửi. Frontend dùng `sessionStorage` để lưu phiên đăng nhập trong tab; vẫn cần CSP và phòng ngừa XSS. Khi đổi `USER_JWT_SECRET`, đăng xuất rồi đăng nhập lại để lấy token mới. Không đưa `.env` backend vào frontend.

## Thiết kế tham khảo

Tham khảo **mô hình UX**, không sao chép hình thức nhận diện của Skyscanner, Google Flights, Traveloka. Các pattern áp dụng: tìm vé ở phần đầu trang, điểm đi/điểm đến và ngày bay, kết quả theo giá/giờ, bộ lọc và thẻ chuyến bay. Ảnh minh họa sử dụng hình tải trực tiếp từ Unsplash (cần internet khi xem), không phải ảnh của dự án hoặc ảnh giá vé thực. Nguồn tham khảo: https://www.skyscanner.com/ và https://support.google.com/travel/answer/2475306.
