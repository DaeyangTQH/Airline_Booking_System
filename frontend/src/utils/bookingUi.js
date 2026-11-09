const STATUS_VI = {
  PENDING: 'Chờ thanh toán',
  CONFIRMED: 'Đã xác nhận',
  CANCELLED: 'Đã hủy',
  EXPIRED: 'Hết hạn'
};

const API_MESSAGE_VI = {
  'Cannot initiate payment for an expired booking':
    'Booking đã hết hạn giữ chỗ, không thể thanh toán. Vui lòng tạo đặt chỗ mới.',
  'Cannot confirm payment for an expired booking':
    'Booking đã hết hạn giữ chỗ, không thể xác nhận thanh toán.',
  'Payment can only be attached to a pending booking':
    'Chỉ booking đang chờ thanh toán mới gắn được thanh toán.',
  'Booking is already linked to another payment':
    'Booking này đã gắn với một thanh toán khác.',
  'Confirmed booking cannot be cancelled':
    'Booking đã xác nhận không thể hủy.',
  'Cannot confirm payment for a cancelled booking':
    'Không thể xác nhận thanh toán cho booking đã hủy.',
  'Only confirmed booking can be add passengers':
    'Chỉ booking đã xác nhận mới được thêm hành khách.',
  'Passenger already exists in this booking':
    'Hành khách này đã có trong booking (trùng hộ chiếu hoặc hồ sơ).',
  'Passenger count cannot exceed ticket quantity':
    'Số hành khách không được vượt quá số vé đã mua.',
  'Invalid phone number':
    'Số điện thoại không hợp lệ. Dùng dạng 0xxxxxxxxx.',
  'Invalid email address':
    'Email không hợp lệ. Vui lòng dùng địa chỉ @gmail.com.',
  'Passenger full name must not be blank':
    'Họ tên hành khách không được để trống.',
  'Passport number must not be blank':
    'Số hộ chiếu không được để trống.',
  'Date of birth is required':
    'Ngày sinh là bắt buộc.',
  'Date of birth must be in the past or present':
    'Ngày sinh phải là ngày trong quá khứ hoặc hôm nay.',
  'You do not have permission to modify this booking':
    'Bạn không có quyền chỉnh sửa booking này.'
};

export function bookingStatusLabel(status) {
  if (!status) return '—';
  return STATUS_VI[String(status).toUpperCase()] || status;
}

export function isBookingHoldExpired(booking) {
  if (!booking?.expiresAt) return false;
  if (booking.status === 'CONFIRMED') return false;
  const expires = Date.parse(booking.expiresAt);
  return Number.isFinite(expires) && expires <= Date.now();
}

export function bookingDisplayStatus(booking) {
  if (!booking) return '';
  if (booking.status === 'CONFIRMED') return 'CONFIRMED';
  // Scheduler đổi PENDING → CANCELLED khi hết hạn giữ chỗ.
  if (isBookingHoldExpired(booking) && (booking.status === 'PENDING' || booking.status === 'CANCELLED')) {
    return 'EXPIRED';
  }
  return booking.status;
}

export function apiMessageVi(message) {
  if (!message) return 'Đã xảy ra lỗi.';
  const exact = API_MESSAGE_VI[message];
  if (exact) return exact;
  const lower = String(message).toLowerCase();
  if (lower.includes('expired booking')) {
    return 'Booking đã hết hạn giữ chỗ, không thể thanh toán. Vui lòng tạo đặt chỗ mới.';
  }
  if (lower.includes('only confirmed booking') && lower.includes('passenger')) {
    return 'Chỉ booking đã xác nhận mới được thêm hành khách.';
  }
  if (lower.includes('passenger already exists')) {
    return 'Hành khách này đã có trong booking (trùng hộ chiếu hoặc hồ sơ).';
  }
  if (lower.includes('cannot exceed ticket quantity')) {
    return 'Số hành khách không được vượt quá số vé đã mua.';
  }
  return message;
}
