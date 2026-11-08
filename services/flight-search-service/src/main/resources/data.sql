-- Keep existing airport records unchanged when the service restarts.
INSERT INTO airports (id, code, name, city, country)
VALUES
    ('a1000000-0000-4000-8000-000000000001', 'HAN', 'Nội Bài', 'Hà Nội', 'Vietnam'),
    ('a1000000-0000-4000-8000-000000000002', 'SGN', 'Tân Sơn Nhất', 'TP. Hồ Chí Minh', 'Vietnam'),
    ('a1000000-0000-4000-8000-000000000003', 'DAD', 'Đà Nẵng', 'Đà Nẵng', 'Vietnam'),
    ('a1000000-0000-4000-8000-000000000004', 'CXR', 'Cam Ranh', 'Nha Trang', 'Vietnam'),
    ('a1000000-0000-4000-8000-000000000005', 'PQC', 'Phú Quốc', 'Phú Quốc', 'Vietnam'),
    ('a1000000-0000-4000-8000-000000000006', 'DLI', 'Liên Khương', 'Đà Lạt', 'Vietnam'),
    ('a1000000-0000-4000-8000-000000000007', 'HUI', 'Phú Bài', 'Huế', 'Vietnam'),
    ('a1000000-0000-4000-8000-000000000008', 'VDO', 'Vân Đồn', 'Quảng Ninh', 'Vietnam'),
    ('a1000000-0000-4000-8000-000000000009', 'BKK', 'Suvarnabhumi', 'Bangkok', 'Thailand'),
    ('a1000000-0000-4000-8000-000000000010', 'SIN', 'Changi', 'Singapore', 'Singapore')
ON CONFLICT (code) DO NOTHING;
