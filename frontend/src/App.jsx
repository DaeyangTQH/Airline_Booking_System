import { Routes, Route } from 'react-router-dom';
import SiteLayout from './layouts/SiteLayout';
import ProtectedRoute from './components/ProtectedRoute';
import ScrollToTop from './components/ScrollToTop';
import HomePage from './pages/HomePage';
import SearchPage from './pages/SearchPage';
import BookingPage from './pages/BookingPage';
import MyTripsPage from './pages/MyTripsPage';
import TripDetailPage from './pages/TripDetailPage';
import PassengerPage from './pages/PassengerPage';
import ProfilePage from './pages/ProfilePage';
import ReminderPage from './pages/ReminderPage';
import AuthPage from './pages/AuthPage';
import AdminUsersPage from './pages/AdminUsersPage';
import NotFoundPage from './pages/NotFoundPage';
import PaymentPage from './pages/PaymentPage';
import PaymentReturnPage from './pages/PaymentReturnPage';

export default function App() {
  return (
    <>
      <ScrollToTop />
      <Routes>
        <Route element={<SiteLayout />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/flights" element={<SearchPage />} />
          <Route path="/booking/:flightId" element={<BookingPage />} />
          <Route path="/login" element={<AuthPage mode="login" />} />
          <Route path="/register" element={<AuthPage mode="register" />} />
          <Route path="/payment-return" element={<PaymentReturnPage />} />
          <Route element={<ProtectedRoute />}>
            <Route path="/trips" element={<MyTripsPage />} />
            <Route path="/trips/:bookingId" element={<TripDetailPage />} />
            <Route path="/payment/:bookingId" element={<PaymentPage />} />
            <Route path="/passengers" element={<PassengerPage />} />
            <Route path="/profile" element={<ProfilePage />} />
            <Route path="/reminders" element={<ReminderPage />} />
          </Route>
          <Route element={<ProtectedRoute admin />}>
            <Route path="/admin/users" element={<AdminUsersPage />} />
          </Route>
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Routes>
    </>
  );
}
