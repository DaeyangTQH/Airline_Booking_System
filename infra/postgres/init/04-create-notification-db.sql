
CREATE DATABASE notification_db;

-- 2. Kết nối vào Database vừa tạo (Dành cho các tool tự động chạy script)
\c notification_db;

-- 3. Tạo bảng notifications
CREATE TABLE IF NOT EXISTS notifications (
                                             id UUID PRIMARY KEY,
                                             recipient VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    payload JSONB,
    status VARCHAR(50) NOT NULL,
    sent_at TIMESTAMP WITH TIME ZONE
                          );

-- 4. Tạo bảng scheduled_reminders
CREATE TABLE IF NOT EXISTS scheduled_reminders (
                                                   id UUID PRIMARY KEY,
                                                   booking_ref VARCHAR(50) NOT NULL,
    contact_email VARCHAR(255) NOT NULL,
    departure_time TIMESTAMP WITH TIME ZONE NOT NULL,
    remind_at TIMESTAMP WITH TIME ZONE NOT NULL,
                            sent BOOLEAN NOT NULL DEFAULT FALSE
                            );

-- 5. Tạo Index để tối ưu tốc độ quét của Background Scheduler
-- (Vì cứ 10 giây Scheduler lại query: WHERE sent = false AND remind_at <= now())
CREATE INDEX IF NOT EXISTS idx_reminders_due
    ON scheduled_reminders (sent, remind_at);