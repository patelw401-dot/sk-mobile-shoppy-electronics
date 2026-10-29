import React, { useState } from 'react';
import { STORE_INFO } from '../data/storeData';
import { signInWithGooglePopup, logoutUser } from '../firebase';
import { 
  Lock, 
  ShieldCheck, 
  Phone, 
  User, 
  Mail, 
  CheckCircle2, 
  AlertCircle,
  Sparkles,
  KeyRound,
  LogOut,
  ArrowRight
} from 'lucide-react';

export default function LoginPage({ currentUser, setActivePage, onShowToast }) {
  const [authMode, setAuthMode] = useState('google'); // Default to real Google Auth
  const [phoneNumber, setPhoneNumber] = useState('');
  const [otpSent, setOtpSent] = useState(false);
  const [otpCode, setOtpCode] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [authError, setAuthError] = useState(null);

  const handleGoogleSignIn = async () => {
    setIsLoading(true);
    setAuthError(null);
    try {
      const result = await signInWithGooglePopup();
      const user = result.user;
      onShowToast?.(`Signed in successfully as ${user.displayName || user.email}!`, 'success');
    } catch (err) {
      console.error('Google Sign-In error:', err);
      let errorMsg = 'Failed to sign in with Google.';
      if (err.code === 'auth/popup-closed-by-user') {
        errorMsg = 'Sign-in window was closed before completing.';
      } else if (err.code === 'auth/unauthorized-domain') {
        errorMsg = 'Current preview domain is not yet whitelisted in Firebase Auth Authorized Domains.';
      } else if (err.message) {
        errorMsg = err.message;
      }
      setAuthError(errorMsg);
      onShowToast?.(errorMsg, 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const handleLogout = async () => {
    setIsLoading(true);
    try {
      await logoutUser();
      onShowToast?.('Logged out successfully.', 'info');
    } catch (err) {
      onShowToast?.(err.message || 'Error signing out', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSendOtp = (e) => {
    e.preventDefault();
    if (!phoneNumber || phoneNumber.length < 10) {
      onShowToast?.('Please enter a valid 10-digit mobile number.', 'error');
      return;
    }
    setAuthError('Phone SMS OTP requires Firebase SMS reCAPTCHA quota. Please use Google Sign-In for instant verified authentication.');
    onShowToast?.('For web browser security, please use Google Sign-In.', 'info');
  };

  return (
    <div className="container py-12 flex justify-center items-center min-h-[70vh]">
      <div className="w-full max-w-md bg-[#141414] border border-[#2E2E2E] rounded-2xl p-6 sm:p-8 shadow-2xl relative overflow-hidden">
        {/* Decorative Top Accent */}
        <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-[#D4AF37] via-[#FFF0B3] to-[#B8860B]"></div>

        <div className="text-center mb-6">
          <div className="w-12 h-12 rounded-xl bg-[#D4AF37]/10 text-[#D4AF37] border border-[#D4AF37]/30 flex items-center justify-center mx-auto mb-3">
            <Lock size={22} />
          </div>
          <h1 className="text-2xl font-black text-white">SK Mobile Portal Login</h1>
          <p className="text-xs text-gray-400 mt-1">
            Access your customer repair bookings, order tracking, and account details.
          </p>
        </div>

        {/* If user is ALREADY authenticated */}
        {currentUser ? (
          <div className="space-y-6">
            <div className="p-4 rounded-xl bg-[#1C180B] border border-[#D4AF37]/40">
              <div className="flex items-center gap-3 mb-2">
                <div className="w-10 h-10 rounded-full bg-[#D4AF37] text-black font-bold flex items-center justify-center text-sm">
                  {currentUser.displayName ? currentUser.displayName[0].toUpperCase() : 'U'}
                </div>
                <div className="overflow-hidden">
                  <div className="text-sm font-bold text-white truncate">
                    {currentUser.displayName || 'Customer'}
                  </div>
                  <div className="text-xs text-[#D4AF37] truncate">
                    {currentUser.email || 'Verified Account'}
                  </div>
                </div>
              </div>

              <div className="pt-2 border-t border-[#333333] text-[11px] text-gray-400 flex justify-between items-center">
                <span>Firebase Auth UID:</span>
                <code className="text-gray-300 font-mono text-[10px]">
                  {currentUser.uid.slice(0, 10)}...
                </code>
              </div>
            </div>

            <div className="space-y-3">
              <button
                onClick={() => setActivePage?.('repairs')}
                className="gold-btn w-full !py-3 !text-sm flex items-center justify-center gap-2"
              >
                <span>Book Mobile Repair with this Account</span>
                <ArrowRight size={16} />
              </button>

              <button
                onClick={handleLogout}
                disabled={isLoading}
                className="w-full py-2.5 px-4 rounded-xl border border-red-500/30 text-red-400 hover:bg-red-500/10 transition text-xs font-semibold flex items-center justify-center gap-2"
              >
                <LogOut size={14} />
                <span>Sign Out of SK Mobile</span>
              </button>
            </div>
          </div>
        ) : (
          <>
            {/* Tab Selection: Google vs Phone */}
            <div className="grid grid-cols-2 gap-2 mb-6 p-1 rounded-xl bg-[#0E0E0E] border border-[#222222]">
              <button
                onClick={() => { setAuthMode('google'); setAuthError(null); }}
                className={`py-2 text-xs font-bold rounded-lg transition ${
                  authMode === 'google'
                    ? 'bg-[#D4AF37] text-black shadow'
                    : 'text-gray-400 hover:text-white'
                }`}
              >
                Google Sign-In
              </button>
              <button
                onClick={() => { setAuthMode('phone'); setAuthError(null); }}
                className={`py-2 text-xs font-bold rounded-lg transition ${
                  authMode === 'phone'
                    ? 'bg-[#D4AF37] text-black shadow'
                    : 'text-gray-400 hover:text-white'
                }`}
              >
                Mobile Number OTP
              </button>
            </div>

            {authError && (
              <div className="mb-4 p-3 rounded-lg bg-red-500/10 border border-red-500/30 text-red-300 text-xs flex items-start gap-2">
                <AlertCircle size={16} className="shrink-0 mt-0.5" />
                <span>{authError}</span>
              </div>
            )}

            {/* Mode 1: Google Sign-In */}
            {authMode === 'google' && (
              <div className="space-y-4 text-center">
                <p className="text-xs text-gray-300 leading-relaxed">
                  Sign in with your Google account to securely link repair bookings to your Firebase customer UID.
                </p>

                <button
                  onClick={handleGoogleSignIn}
                  disabled={isLoading}
                  className="w-full py-3 px-4 rounded-xl bg-white text-gray-900 font-bold text-xs flex items-center justify-center gap-2 hover:bg-gray-100 transition shadow disabled:opacity-50"
                >
                  {isLoading ? (
                    <>
                      <span className="w-4 h-4 border-2 border-gray-900 border-t-transparent rounded-full animate-spin"></span>
                      <span>Connecting with Google...</span>
                    </>
                  ) : (
                    <>
                      <svg className="w-4 h-4" viewBox="0 0 24 24">
                        <path fill="#4285F4" d="M23.745 12.27c0-.7-.06-1.4-.19-2.07H12v4.51h6.6c-.29 1.52-1.14 2.8-2.4 3.66v3.05h3.88c2.27-2.09 3.66-5.17 3.66-9.15z"/>
                        <path fill="#34A853" d="M12 24c3.24 0 5.95-1.08 7.93-2.91l-3.88-3.05c-1.08.72-2.45 1.16-4.05 1.16-3.12 0-5.77-2.1-6.72-4.94H1.27v3.15C3.25 21.27 7.31 24 12 24z"/>
                        <path fill="#FBBC05" d="M5.28 14.26c-.25-.72-.38-1.49-.38-2.26s.13-1.54.38-2.26V6.59H1.27C.46 8.21 0 10.05 0 12s.46 3.79 1.27 5.41l4.01-3.15z"/>
                        <path fill="#EA4335" d="M12 4.75c1.77 0 3.35.61 4.6 1.8l3.42-3.42C17.95 1.19 15.24 0 12 0 7.31 0 3.25 2.73 1.27 6.59l4.01 3.15c.95-2.84 3.6-4.99 6.72-4.99z"/>
                      </svg>
                      <span>Continue with Google</span>
                    </>
                  )}
                </button>
              </div>
            )}

            {/* Mode 2: Phone OTP */}
            {authMode === 'phone' && (
              <div className="space-y-4">
                <form onSubmit={handleSendOtp} className="space-y-4">
                  <div>
                    <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                      Customer Mobile Number
                    </label>
                    <div className="relative">
                      <div className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-sm font-semibold">
                        +91
                      </div>
                      <input
                        type="tel"
                        placeholder="9823109313"
                        maxLength={10}
                        value={phoneNumber}
                        onChange={(e) => setPhoneNumber(e.target.value)}
                        className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg pl-12 pr-4 py-2.5 text-sm text-white focus:outline-none focus:border-[#D4AF37]"
                      />
                    </div>
                  </div>

                  <button
                    type="submit"
                    className="gold-btn w-full !py-2.5 !text-sm"
                  >
                    Send Verification Code
                  </button>
                </form>
              </div>
            )}
          </>
        )}

        {/* Security & Admin Notice */}
        <div className="mt-8 pt-4 border-t border-[#222222] text-[11px] text-gray-400 space-y-2">
          <div className="flex items-start gap-2">
            <ShieldCheck size={14} className="text-[#D4AF37] shrink-0 mt-0.5" />
            <span>
              <strong>Zero-Trust Security:</strong> Repair bookings require an authenticated Firebase UID matching <code className="text-[#D4AF37]">customerId == request.auth.uid</code>.
            </span>
          </div>
          <div className="flex items-start gap-2">
            <KeyRound size={14} className="text-amber-400 shrink-0 mt-0.5" />
            <span>
              <strong>Authorized Store Admins:</strong> Restricted strictly to <code className="text-gray-300">skmobileshoppyandelectronics@gmail.com</code> and <code className="text-gray-300">patelw401@gmail.com</code>.
            </span>
          </div>
        </div>
      </div>
    </div>
  );
}
