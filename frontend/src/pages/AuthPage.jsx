import { useEffect, useState } from 'react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import {
    ArrowLeft,
    ArrowRight,
    ArrowUpRight,
    Check,
    CheckCircle2,
    Eye,
    EyeOff,
    LoaderCircle,
    LockKeyhole,
    Mail,
    MapPin,
    Phone,
    Plane,
    ShieldCheck,
    Sparkles,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import '../styles/auth.css';

const initialForm = { email: '', password: '', phone: '' };

export default function AuthPage({ mode = 'login' }) {
    const isRegister = mode === 'register';
    const { isAuthenticated, login, register: doRegister, logout } = useAuth();
    const location = useLocation();
    const navigate = useNavigate();
    const toast = useToast();

    const [form, setForm] = useState(() => ({
        ...initialForm,
        email: location.state?.email || '',
    }));
    const [showPassword, setShowPassword] = useState(false);
    const [busy, setBusy] = useState(false);

    useEffect(() => {
        setShowPassword(false);
        setForm((previous) => ({
            ...previous,
            email: location.state?.email || previous.email,
            password: '',
            phone: isRegister ? previous.phone : '',
        }));
    }, [isRegister, location.state?.email]);

    const change = (key, value) => {
        setForm((previous) => ({ ...previous, [key]: value }));
    };

    const passwordStrength = [
        form.password.length >= 8,
        /[a-z]/.test(form.password) && /[A-Z]/.test(form.password),
        /\d/.test(form.password),
        /[^A-Za-z\d]/.test(form.password),
    ].filter(Boolean).length;

    const submit = async (event) => {
        event.preventDefault();
        if (busy) return;
        setBusy(true);

        try {
            const email = form.email.trim();
            const password = form.password;

            if (isRegister) {
                if (password.length < 8) {
                    throw new Error('Mật khẩu phải có ít nhất 8 ký tự.');
                }

                await doRegister(email, password, form.phone.trim());
                // AuthContext hiện tại tự lưu session sau khi register.
                // Xóa session đó vì đăng ký xong phải về Login, không tự đăng nhập.
                logout();
                toast('Tạo tài khoản thành công! Hãy đăng nhập để tiếp tục.');
                setForm({ email, password: '', phone: '' });
                navigate('/login', { replace: true, state: { email } });
            } else {
                await login(email, password);
                toast('Đăng nhập thành công! Chào mừng trở lại SkyPort.');
                navigate('/', { replace: true });
            }
        } catch (error) {
            toast(error?.message || 'Có lỗi xảy ra. Vui lòng thử lại.', 'error');
        } finally {
            setBusy(false);
        }
    };

    if (isAuthenticated) return <Navigate to="/" replace />;

    return (
        <section className="sp-auth" aria-label={isRegister ? 'Đăng ký tài khoản' : 'Đăng nhập'}>
            <div className="sp-auth__ambient sp-auth__ambient--one" aria-hidden="true" />
            <div className="sp-auth__ambient sp-auth__ambient--two" aria-hidden="true" />

            <div className="sp-auth__shell">
                <aside className="sp-auth__hero">
                    <div className="sp-auth__hero-shade" aria-hidden="true" />
                    <div className="sp-auth__hero-top">
                        <Link className="sp-auth__back" to="/">
                            <ArrowLeft size={17} strokeWidth={2.2} />
                            Về trang chủ
                        </Link>
                        <span className="sp-auth__hero-coordinate">21°02′ N · 105°51′ E</span>
                    </div>

                    <div className="sp-auth__hero-copy">
                        <div className="sp-auth__hero-eyebrow">
                            <span className="sp-auth__hero-eyebrow-line" />
                            <Sparkles size={15} />
                            EVERY JOURNEY BEGINS HERE
                        </div>
                        <h2>
                            Đi xa hơn.
                            <br />
                            <span>Chạm gần hơn</span>
                            <br />
                            những giấc mơ.
                        </h2>
                        <p>
                            Những chân trời mới đang chờ bạn khám phá.
                            Cùng SkyPort bắt đầu hành trình theo cách thật đặc biệt.
                        </p>
                    </div>

                    <div className="sp-auth__hero-footer">
                        <div className="sp-auth__destination">
                            <span className="sp-auth__destination-icon"><MapPin size={18} /></span>
                            <span>
                <small>ĐIỂM ĐẾN TIẾP THEO</small>
                <strong>Bất cứ đâu bạn muốn</strong>
              </span>
                        </div>
                        <span className="sp-auth__hero-page">SKYPORT <b>·</b> 2026</span>
                    </div>

                    <div className="sp-auth__ticket" aria-hidden="true">
                        <div className="sp-auth__ticket-top">
                            <span><Plane size={15} /> SKYPORT AIR</span>
                            <span>BOARDING PASS</span>
                        </div>
                        <div className="sp-auth__ticket-route">
                            <div><strong>HAN</strong><small>HÀ NỘI</small></div>
                            <span className="sp-auth__ticket-flight"><span /> <Plane size={18} /> <span /></span>
                            <div><strong>SGN</strong><small>TP. HỒ CHÍ MINH</small></div>
                        </div>
                        <div className="sp-auth__ticket-bottom">
                            <span>YOUR NEXT ADVENTURE</span>
                            <span>✦ SKYPORT</span>
                        </div>
                    </div>
                </aside>

                <div className="sp-auth__right">
                    <div className="sp-auth__right-top">
                        <Link to="/" className="sp-auth__mini-brand" aria-label="SkyPort - Trang chủ">
                            <span><Plane size={19} fill="currentColor" /></span>
                            Sky<span>Port</span><span className="sp-auth__brand-dot">.</span>
                        </Link>
                        <span className="sp-auth__right-tag"><span /> Hành trình của bạn bắt đầu tại đây</span>
                    </div>

                    <div className="sp-auth__content">
                        <div className="sp-auth__intro">
                            <div className="sp-auth__eyebrow">
                                <span className="sp-auth__eyebrow-icon"><Sparkles size={16} /></span>
                                SKYPORT MEMBERSHIP
                            </div>
                            <h1>{isRegister ? 'Bắt đầu hành trình mới.' : 'Chào mừng trở lại!'}</h1>
                            <p>
                                {isRegister
                                    ? 'Tạo tài khoản để lưu chuyến bay yêu thích và quản lý mọi hành trình trong một nơi.'
                                    : 'Cùng tiếp tục khám phá thế giới. Đăng nhập để quản lý những chuyến đi của bạn.'}
                            </p>
                        </div>

                        <nav className="sp-auth__tabs" aria-label="Chọn hình thức xác thực">
                            <Link to="/login" aria-current={!isRegister ? 'page' : undefined} className={`sp-auth__tab ${!isRegister ? 'sp-auth__tab--active' : ''}`}>
                                Đăng nhập
                            </Link>
                            <Link to="/register" aria-current={isRegister ? 'page' : undefined} className={`sp-auth__tab ${isRegister ? 'sp-auth__tab--active' : ''}`}>
                                Đăng ký
                            </Link>
                        </nav>

                        <form className="sp-auth__form" onSubmit={submit}>
                            <div className="sp-auth__field">
                                <label htmlFor="sp-auth-email">Địa chỉ email</label>
                                <div className="sp-auth__input-wrap">
                                    <Mail size={19} aria-hidden="true" />
                                    <input
                                        id="sp-auth-email"
                                        type="email"
                                        placeholder="tenban@example.com"
                                        value={form.email}
                                        onChange={(event) => change('email', event.target.value)}
                                        autoComplete="email"
                                        maxLength={255}
                                        required
                                    />
                                </div>
                            </div>

                            {isRegister && (
                                <div className="sp-auth__field">
                                    <label htmlFor="sp-auth-phone">Số điện thoại <span>(không bắt buộc)</span></label>
                                    <div className="sp-auth__input-wrap">
                                        <Phone size={19} aria-hidden="true" />
                                        <input
                                            id="sp-auth-phone"
                                            type="tel"
                                            placeholder="09xx xxx xxx"
                                            value={form.phone}
                                            onChange={(event) => change('phone', event.target.value)}
                                            autoComplete="tel"
                                            inputMode="tel"
                                            maxLength={20}
                                        />
                                    </div>
                                </div>
                            )}

                            <div className="sp-auth__field">
                                <label htmlFor="sp-auth-password">Mật khẩu</label>
                                <div className="sp-auth__input-wrap">
                                    <LockKeyhole size={19} aria-hidden="true" />
                                    <input
                                        id="sp-auth-password"
                                        type={showPassword ? 'text' : 'password'}
                                        placeholder={isRegister ? 'Tạo mật khẩu ít nhất 8 ký tự' : 'Nhập mật khẩu của bạn'}
                                        value={form.password}
                                        onChange={(event) => change('password', event.target.value)}
                                        autoComplete={isRegister ? 'new-password' : 'current-password'}
                                        minLength={isRegister ? 8 : undefined}
                                        required
                                    />
                                    <button
                                        type="button"
                                        className="sp-auth__visibility"
                                        onClick={() => setShowPassword((previous) => !previous)}
                                        aria-label={showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                                        aria-pressed={showPassword}
                                    >
                                        {showPassword ? <EyeOff size={19} /> : <Eye size={19} />}
                                    </button>
                                </div>

                                {isRegister && (
                                    <div className="sp-auth__password-help">
                                        <div className="sp-auth__strength-bars" aria-label={`Độ mạnh mật khẩu: ${passwordStrength}/4`}>
                                            {[1, 2, 3, 4].map((level) => (
                                                <span key={level} className={level <= passwordStrength ? `sp-auth__strength-on sp-auth__strength-on--${passwordStrength}` : ''} />
                                            ))}
                                        </div>
                                        <small>Ít nhất 8 ký tự. Nên kết hợp chữ, số và ký tự đặc biệt.</small>
                                    </div>
                                )}
                            </div>

                            <button className="sp-auth__submit" type="submit" disabled={busy} aria-busy={busy}>
                <span>
                  {busy && <LoaderCircle size={19} className="sp-auth__spinner" />}
                    {busy ? 'Đang xử lý...' : isRegister ? 'Tạo tài khoản miễn phí' : 'Đăng nhập vào SkyPort'}
                </span>
                                {!busy && <span className="sp-auth__submit-arrow"><ArrowRight size={19} /></span>}
                            </button>
                        </form>

                        <div className="sp-auth__divider"><span>KHÁM PHÁ NHIỀU HƠN CÙNG SKYPORT</span></div>

                        <div className="sp-auth__features">
                            <div><span><CheckCircle2 size={17} /></span> Quản lý hành trình dễ dàng</div>
                            <div><span><Check size={17} /></span> Lưu thông tin hành khách</div>
                        </div>

                        <p className="sp-auth__switch">
                            {isRegister ? 'Bạn đã có tài khoản?' : 'Lần đầu đến với SkyPort?'}{' '}
                            <Link to={isRegister ? '/login' : '/register'}>
                                {isRegister ? 'Đăng nhập ngay' : 'Đăng ký miễn phí'}
                                <ArrowUpRight size={15} />
                            </Link>
                        </p>
                    </div>

                    <div className="sp-auth__right-bottom">
                        <div><ShieldCheck size={17} /> Tài khoản được bảo vệ bằng xác thực đăng nhập</div>
                        <span>© 2026 SkyPort</span>
                    </div>
                </div>
            </div>
        </section>
    );
}
