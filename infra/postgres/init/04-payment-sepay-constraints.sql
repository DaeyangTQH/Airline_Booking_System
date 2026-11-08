-- Chạy trên payment_db nếu DB đã tạo lúc còn VNPAY-only check constraints.
-- (Hibernate ddl-auto=update không luôn đổi CHECK enum sẵn có.)

ALTER TABLE IF EXISTS payments DROP CONSTRAINT IF EXISTS payments_method_check;
ALTER TABLE IF EXISTS payments DROP CONSTRAINT IF EXISTS payments_provider_check;

ALTER TABLE IF EXISTS payments
    ADD CONSTRAINT payments_method_check
    CHECK (method::text = ANY (ARRAY['VNPAY'::text, 'SEPAY'::text]));

ALTER TABLE IF EXISTS payments
    ADD CONSTRAINT payments_provider_check
    CHECK (provider::text = ANY (ARRAY['VNPAY'::text, 'SEPAY'::text]));
