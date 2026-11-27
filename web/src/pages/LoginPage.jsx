import React, { useState } from 'react';
import {
  signInWithGooglePopup,
  signInWithEmail,
  signUpWithEmail,
  resetPasswordWithEmail,
  formatFirebaseAuthError,
  logoutUser,
  isUserAdmin
} from '../firebase';
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
  ArrowRight,
  Eye,
  EyeOff
} from 'lucide-react';

export default function LoginPage({ currentUser, setActivePage, onShowToast }) {
  const [authMode, setAuthMode] = useState('google'); // 'google' | 'email' | 'phone'
  const [isSignUp, setIsSignUp] = useState(false);
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [resetMessage, setResetMessage] = useState(null);
  const [phoneNumber, setPhoneNumber] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [authError, setAuthError] = useState(null);

  const handleGoogleSignIn = async () => {
    setIsLoading(true);
    setAuthError(null);
    setResetMessage(null);
    try {
      const result = await signInWithGooglePopup();
      const user = result.user;
      onShowToast?.(`Signed in successfully as ${user.displayName || user.email}!`, 'success');
    } catch (err) {
      console.error('Google Sign-In error:', err);
      const errorMsg = formatFirebaseAuthError(err);
      setAuthError(errorMsg);
      onShowToast?.(errorMsg, 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const handleEmailAuth = async (e) => {
    e.preventDefault();
    setAuthError(null);
    setResetMessage(null);

    const trimmedEmail = email.trim();
    if (isSignUp && !fullName.trim()) {
      setAuthError('Please enter your full name.');
      return;
    }
    if (!trimmedEmail || !trimmedEmail.includes('@')) {
      setAuthError('Please enter a valid email address.');
      return;
    }
    if (!password || (isSignUp && password.length < 6)) {
      setAuthError(isSignUp ? 'Password must be at least 6 characters long.' : 'Please enter your password.');
      return;
    }

    setIsLoading(true);
    try {
      const result = isSignUp
        ? await signUpWithEmail(fullName, trimmedEmail, password)
        : await signInWithEmail(trimmedEmail, password);
      const user = result.user;
      onShowToast?.(
        isSignUp
          ? `Account created! Welcome, ${fullName.trim()}!`
          : `Signed in successfully as ${user.displayName || user.email}!`,
        'success'
      );
    } catch (err) {
      console.error('Email auth error:', err);
      const errorMsg = formatFirebaseAuthError(err);
      setAuthError(errorMsg);
      onShowToast?.(errorMsg, 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const handleForgotPassword = async () => {
    setAuthError(null);
    setResetMessage(null);
    const trimmedEmail = email.trim();
    if (!trimmedEmail || !trimmedEmail.includes('@')) {
      setAuthError('Please enter your registered email address to reset your password.');
      return;
    }

    setIsLoading(true);
    try {
      await resetPasswordWithEmail(trimmedEmail);
      const msg = `Password reset link sent to ${trimmedEmail}. Please check your inbox or spam folder.`;
      setResetMessage(msg);
      onShowToast?.(msg, 'success');
    } catch (err) {
      const errorMsg = formatFirebaseAuthError(err);
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
    setAuthError('Phone SMS OTP requires Firebase SMS reCAPTCHA quota. Please use Google Sign-In or Email & Password.');
    onShowToast?.('Please use Google Sign-In or Email & Password.', 'info');
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
                  {currentUser.displayName ? currentUser.displayName[0].toUpperCase() : (currentUser.email ? currentUser.email[0].toUpperCase() : 'U')}
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
              {isUserAdmin(currentUser) && (
                <button
                  onClick={() => setActivePage?.('admin')}
                  className="w-full py-3 px-4 rounded-xl bg-amber-500 hover:bg-amber-600 text-black font-extrabold text-sm flex items-center justify-center gap-2 shadow-lg shadow-amber-500/20 transition"
                >
                  <ShieldCheck size={18} />
                  <span>Open Store Admin Dashboard</span>
                  <ArrowRight size={16} />
                </button>
              )}

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
            {/* Tab Selection: Google vs Email vs Phone */}
            <div className="grid grid-cols-3 gap-1.5 mb-6 p-1 rounded-xl bg-[#0E0E0E] border border-[#222222]">
              <button
                type="button"
                onClick={() => { setAuthMode('google'); setAuthError(null); setResetMessage(null); }}
                className={`py-2 text-xs font-bold rounded-lg transition ${
                  authMode === 'google'
                    ? 'bg-[#D4AF37] text-black shadow'
                    : 'text-gray-400 hover:text-white'
                }`}
              >
                Google
              </button>
              <button
                type="button"
                onClick={() => { setAuthMode('email'); setAuthError(null); setResetMessage(null); }}
                className={`py-2 text-xs font-bold rounded-lg transition ${
                  authMode === 'email'
                    ? 'bg-[#D4AF37] text-black shadow'
                    : 'text-gray-400 hover:text-white'
                }`}
              >
                Email
              </button>
              <button
                type="button"
                onClick={() => { setAuthMode('phone'); setAuthError(null); setResetMessage(null); }}
                className={`py-2 text-xs font-bold rounded-lg transition ${
                  authMode === 'phone'
                    ? 'bg-[#D4AF37] text-black shadow'
                    : 'text-gray-400 hover:text-white'
                }`}
              >
                Mobile OTP
              </button>
            </div>

            {authError && (
              <div className="mb-4 p-3 rounded-lg bg-red-500/10 border border-red-500/30 text-red-300 text-xs flex items-start gap-2">
                <AlertCircle size={16} className="shrink-0 mt-0.5" />
                <span>{authError}</span>
              </div>
            )}

            {resetMessage && (
              <div className="mb-4 p-3 rounded-lg bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 text-xs flex items-start gap-2">
                <CheckCircle2 size={16} className="shrink-0 mt-0.5" />
                <span>{resetMessage}</span>
              </div>
            )}

            {/* Mode 1: Google Sign-In */}
            {authMode === 'google' && (
              <div className="space-y-4 text-center">
                <p className="text-xs text-gray-300 leading-relaxed">
                  Sign in with your Google account to securely link repair bookings and orders to your Firebase customer UID.
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

            {/* Mode 2: Email & Password */}
            {authMode === 'email' && (
              <form onSubmit={handleEmailAuth} className="space-y-4">
                <div className="grid grid-cols-2 gap-2 p-1 rounded-lg bg-[#0E0E0E] border border-[#222222]">
                  <button
                    type="button"
                    onClick={() => { setIsSignUp(false); setAuthError(null); setResetMessage(null); }}
                    className={`py-1.5 text-xs font-bold rounded-md transition ${
                      !isSignUp ? 'bg-[#262626] text-[#D4AF37]' : 'text-gray-400 hover:text-white'
                    }`}
                  >
                    Sign In
                  </button>
                  <button
                    type="button"
                    onClick={() => { setIsSignUp(true); setAuthError(null); setResetMessage(null); }}
                    className={`py-1.5 text-xs font-bold rounded-md transition ${
                      isSignUp ? 'bg-[#262626] text-[#D4AF37]' : 'text-gray-400 hover:text-white'
                    }`}
                  >
                    Create Account
                  </button>
                </div>

                {isSignUp && (
                  <div>
                    <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                      Full Name
                    </label>
                    <input
                      type="text"
                      placeholder="Enter your full name"
                      value={fullName}
                      onChange={(e) => setFullName(e.target.value)}
                      className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3.5 py-2.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-[#D4AF37]"
                    />
                  </div>
                )}

                <div>
                  <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                    Email Address
                  </label>
                  <input
                    type="email"
                    placeholder="name@example.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3.5 py-2.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-[#D4AF37]"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                    Password
                  </label>
                  <div className="relative">
                    <input
                      type={showPassword ? 'text' : 'password'}
                      placeholder={isSignUp ? 'Minimum 6 characters' : 'Enter your password'}
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg pl-3.5 pr-10 py-2.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-[#D4AF37]"
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword((prev) => !prev)}
                      aria-label={showPassword ? 'Hide password' : 'Show password'}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-white"
                    >
                      {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                    </button>
                  </div>
                </div>

                {!isSignUp && (
                  <div className="flex justify-end">
                    <button
                      type="button"
                      onClick={handleForgotPassword}
                      disabled={isLoading}
                      className="text-xs font-semibold text-[#D4AF37] hover:underline disabled:opacity-50"
                    >
                      Forgot Password?
                    </button>
                  </div>
                )}

                <button
                  type="submit"
                  disabled={isLoading}
                  className="gold-btn w-full !py-2.5 !text-sm disabled:opacity-50"
                >
                  {isLoading
                    ? (isSignUp ? 'Creating Account...' : 'Signing In...')
                    : (isSignUp ? 'Create Account' : 'Sign In with Email')}
                </button>
              </form>
            )}

            {/* Mode 3: Phone OTP */}
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
              <button 
                type="button"
                onClick={() => setActivePage?.('admin')} 
                className="text-amber-400 hover:underline font-semibold ml-1.5 inline-flex items-center"
              >
                Go to Admin Portal →
              </button>
            </span>
          </div>
        </div>
      </div>
    </div>
  );
}
