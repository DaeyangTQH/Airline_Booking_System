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

Chưa thể nối thật:
- Flight Search integration is described below; only implemented catalog APIs are connected.
- Flight details use backend UUIDs and current fares; booking confirmation is not enabled.
- `payment-service` chưa có REST API thanh toán trong ZIP; frontend không mô phỏng thanh toán thành công.
- Notification Service chỉ có tạo thông báo và lịch nhắc, chưa có API đọc danh sách thông báo, nên `/reminders` chỉ tạo lịch nhắc.

**Lưu ý bảo mật:** BookingController nhận `X-User-Id` từ frontend. Trong hệ thống thật, gateway/booking-service phải kiểm chứng hoặc tự gán header từ JWT thay vì tin header do client gửi. Frontend dùng `sessionStorage` để lưu phiên đăng nhập trong tab; vẫn cần CSP và phòng ngừa XSS. Khi đổi `USER_JWT_SECRET`, đăng xuất rồi đăng nhập lại để lấy token mới. Không đưa `.env` backend vào frontend.

## Thiết kế tham khảo

Tham khảo **mô hình UX**, không sao chép hình thức nhận diện của Skyscanner, Google Flights, Traveloka. Các pattern áp dụng: tìm vé ở phần đầu trang, điểm đi/điểm đến và ngày bay, kết quả theo giá/giờ, bộ lọc và thẻ chuyến bay. Ảnh minh họa sử dụng hình tải trực tiếp từ Unsplash (cần internet khi xem), không phải ảnh của dự án hoặc ảnh giá vé thực. Nguồn tham khảo: https://www.skyscanner.com/ và https://support.google.com/travel/answer/2475306.

## Flight Search integration

Airport selectors load GET /api/v1/airports and map name to the airport label.
Aircraft has backend catalog APIs; no aircraft-management page is added.
Flight search and details consume /api/v1/flights with pagination and current prices.
