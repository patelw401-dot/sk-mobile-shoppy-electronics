import React, { useState, useEffect } from 'react';
import { STORE_INFO, REPAIR_SERVICES, REPAIR_BRANDS } from '../data/storeData';
import { saveRepairBookingToFirestore, signInWithGooglePopup } from '../firebase';
import { 
  Wrench, 
  ShieldCheck, 
  Clock, 
  MapPin, 
  Phone, 
  CheckCircle2, 
  AlertCircle,
  Truck,
  Store,
  Calendar,
  Sparkles,
  Lock,
  UserCheck,
  ArrowRight,
  Database
} from 'lucide-react';

export default function RepairBookingPage({ currentUser, setActivePage, onShowToast }) {
  const [customerName, setCustomerName] = useState(currentUser?.displayName || '');
  const [mobileNumber, setMobileNumber] = useState('');
  const [selectedBrand, setSelectedBrand] = useState('Samsung');
  const [modelName, setModelName] = useState('');
  const [selectedProblem, setSelectedProblem] = useState(REPAIR_SERVICES[0].title);
  const [pickupRequired, setPickupRequired] = useState(true);
  const [address, setAddress] = useState('');
  const [preferredSlot, setPreferredSlot] = useState('Today / Earliest Slot');
  const [additionalDetails, setAdditionalDetails] = useState('');

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isSigningIn, setIsSigningIn] = useState(false);
  const [errors, setErrors] = useState({});
  const [firestoreError, setFirestoreError] = useState(null);
  const [confirmedBooking, setConfirmedBooking] = useState(null);

  // Sync customer name from auth when user signs in
  useEffect(() => {
    if (currentUser?.displayName && !customerName) {
      setCustomerName(currentUser.displayName);
    }
  }, [currentUser]);

  const timeSlots = [
    'Today / Earliest Slot',
    'Tomorrow Morning (10 AM - 1 PM)',
    'Tomorrow Evening (5 PM - 8 PM)',
    'Weekend Slot'
  ];

  const handleQuickGoogleSignIn = async () => {
    setIsSigningIn(true);
    setFirestoreError(null);
    try {
      const result = await signInWithGooglePopup();
      const user = result.user;
      if (user.displayName) setCustomerName(user.displayName);
      onShowToast?.(`Signed in as ${user.displayName || user.email}! You can now submit your booking.`, 'success');
    } catch (err) {
      console.error('Quick Sign-in error:', err);
      const msg = err.code === 'auth/popup-closed-by-user'
        ? 'Sign-in window closed before completing.'
        : (err.message || 'Failed to sign in with Google');
      setFirestoreError(msg);
      onShowToast?.(msg, 'error');
    } finally {
      setIsSigningIn(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (isSubmitting) return;
    setFirestoreError(null);

    // Rule 1: Zero-trust authentication enforcement
    if (!currentUser || !currentUser.uid) {
      const authErr = 'Customer authentication required: Firestore security rules require an authenticated user with customerId matching request.auth.uid.';
      setFirestoreError(authErr);
      onShowToast?.('Please sign in with your customer account before booking.', 'error');
      return;
    }

    const newErrors = {};
    if (!customerName.trim()) newErrors.customerName = 'Please enter your full name';
    if (!mobileNumber.trim() || mobileNumber.trim().length < 10) {
      newErrors.mobileNumber = 'Please enter a valid 10-digit mobile number';
    }
    if (!modelName.trim()) newErrors.modelName = 'Please enter your phone model name';
    if (pickupRequired && !address.trim()) {
      newErrors.address = 'Please enter your doorstep address in Udgir';
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      onShowToast?.('Please fill all required booking fields correctly.', 'error');
      return;
    }

    setErrors({});
    setIsSubmitting(true);

    try {
      const bookingId = `SKM-REP-${Date.now().toString().slice(-6)}`;
      const bookingPayload = {
        bookingId,
        customerName: customerName.trim(),
        mobileNumber: mobileNumber.trim(),
        brand: selectedBrand,
        model: modelName.trim(),
        problem: selectedProblem,
        pickupRequired,
        address: pickupRequired ? address.trim() : 'Drop-off at Nagar Parishad Store, Udgir',
        preferredSlot,
        additionalDetails: additionalDetails.trim(),
        estimatedCost: '₹499 - ₹1,499 (Final quote on diagnosis)'
      };

      // Real write to Cloud Firestore 'repairBookings' collection
      const result = await saveRepairBookingToFirestore(bookingPayload, currentUser);

      const confirmedData = {
        bookingId: result.bookingId,
        customerName: customerName.trim(),
        mobileNumber: mobileNumber.trim(),
        brand: selectedBrand,
        model: modelName.trim(),
        problem: selectedProblem,
        pickupRequired,
        address: pickupRequired ? address.trim() : 'Drop-off at Nagar Parishad Store, Udgir',
        preferredSlot,
        additionalDetails: additionalDetails.trim(),
        status: pickupRequired ? 'Pickup Scheduled' : 'Appointment Booked',
        estimatedCost: '₹499 - ₹1,499 (Final quote on diagnosis)',
        date: new Date().toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' }),
        customerId: currentUser.uid,
        isCloudSaved: true
      };

      setConfirmedBooking(confirmedData);
      onShowToast?.(`Repair booking ${result.bookingId} successfully saved to Firestore!`, 'success');

      // Clear model & notes
      setModelName('');
      setAdditionalDetails('');
    } catch (err) {
      console.error('Firestore saveRepairBooking error:', err);
      const errMsg = err.code === 'permission-denied'
        ? 'Permission denied: Please ensure your account has customer booking privileges.'
        : (err.message || 'Failed to save booking to Firestore. Please check your connection.');
      setFirestoreError(errMsg);
      onShowToast?.(`Booking submission error: ${errMsg}`, 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="container py-8 space-y-10">
      {/* Header */}
      <div className="border-b border-[#222222] pb-6">
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#D4AF37]/15 border border-[#D4AF37]/30 text-xs font-bold text-[#D4AF37] mb-2">
          <Wrench size={13} />
          <span>SK Mobile Authorized Repair Center • Udgir</span>
        </div>
        <h1 className="text-3xl font-black text-white">Book Mobile Repair & Service</h1>
        <p className="text-xs sm:text-sm text-gray-400 mt-1 max-w-2xl">
          Get transparent diagnosis, genuine components, and expert service. Choose free doorstep device pickup anywhere in Udgir or visit our service desk.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Left Form: Booking inputs */}
        <div className="lg:col-span-7 bg-[#141414] border border-[#262626] rounded-2xl p-6 sm:p-8">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-6">
            <h2 className="text-lg font-bold text-white flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-[#D4AF37]"></span>
              <span>Enter Repair & Device Details</span>
            </h2>

            {/* Auth Indicator Badge */}
            {currentUser ? (
              <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-500/15 border border-emerald-500/30 text-[11px] text-emerald-400 font-medium">
                <UserCheck size={13} />
                <span>Verified Account: {currentUser.displayName || currentUser.email}</span>
              </div>
            ) : (
              <button
                type="button"
                onClick={handleQuickGoogleSignIn}
                disabled={isSigningIn}
                className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#D4AF37]/15 border border-[#D4AF37]/40 text-[11px] text-[#D4AF37] font-semibold hover:bg-[#D4AF37]/25 transition"
              >
                <Lock size={12} />
                <span>{isSigningIn ? 'Connecting...' : 'Sign in to Book'}</span>
              </button>
            )}
          </div>

          {/* Authentication Requirement Alert when not logged in */}
          {!currentUser && (
            <div className="mb-6 p-4 rounded-xl bg-gradient-to-r from-[#1E190B] to-[#141414] border border-[#D4AF37]/40 text-xs text-gray-300 space-y-2">
              <div className="flex items-center gap-2 text-[#D4AF37] font-bold">
                <ShieldCheck size={16} />
                <span>Customer Authentication Required by Cloud Security Rules</span>
              </div>
              <p className="text-[11px] text-gray-400 leading-relaxed">
                To guarantee repair integrity and real-time order tracking, Firestore security rules require bookings to be authenticated. Sign in with Google to enable instant booking.
              </p>
              <div className="pt-2 flex flex-wrap gap-2">
                <button
                  type="button"
                  onClick={handleQuickGoogleSignIn}
                  disabled={isSigningIn}
                  className="px-3 py-1.5 rounded-lg bg-white text-gray-900 font-bold text-xs flex items-center gap-1.5 hover:bg-gray-100 transition shadow"
                >
                  <svg className="w-3.5 h-3.5" viewBox="0 0 24 24">
                    <path fill="#4285F4" d="M23.745 12.27c0-.7-.06-1.4-.19-2.07H12v4.51h6.6c-.29 1.52-1.14 2.8-2.4 3.66v3.05h3.88c2.27-2.09 3.66-5.17 3.66-9.15z"/>
                    <path fill="#34A853" d="M12 24c3.24 0 5.95-1.08 7.93-2.91l-3.88-3.05c-1.08.72-2.45 1.16-4.05 1.16-3.12 0-5.77-2.1-6.72-4.94H1.27v3.15C3.25 21.27 7.31 24 12 24z"/>
                    <path fill="#FBBC05" d="M5.28 14.26c-.25-.72-.38-1.49-.38-2.26s.13-1.54.38-2.26V6.59H1.27C.46 8.21 0 10.05 0 12s.46 3.79 1.27 5.41l4.01-3.15z"/>
                    <path fill="#EA4335" d="M12 4.75c1.77 0 3.35.61 4.6 1.8l3.42-3.42C17.95 1.19 15.24 0 12 0 7.31 0 3.25 2.73 1.27 6.59l4.01 3.15c.95-2.84 3.6-4.99 6.72-4.99z"/>
                  </svg>
                  <span>{isSigningIn ? 'Connecting...' : 'Continue with Google'}</span>
                </button>
                <button
                  type="button"
                  onClick={() => setActivePage?.('login')}
                  className="px-3 py-1.5 rounded-lg border border-[#333333] hover:border-[#D4AF37] text-gray-300 text-xs font-semibold"
                >
                  Go to Login Portal
                </button>
              </div>
            </div>
          )}

          {/* Firestore Error Alert */}
          {firestoreError && (
            <div className="mb-6 p-3.5 rounded-xl bg-red-500/10 border border-red-500/30 text-red-300 text-xs flex items-start gap-2.5">
              <AlertCircle size={16} className="shrink-0 mt-0.5 text-red-400" />
              <div>
                <strong className="block font-semibold">Booking Submission Notice:</strong>
                <span className="text-[11px] leading-relaxed">{firestoreError}</span>
              </div>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            {/* Customer Name & Phone */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                  Your Full Name *
                </label>
                <input
                  type="text"
                  placeholder="e.g. Ramesh Pawar"
                  value={customerName}
                  onChange={(e) => setCustomerName(e.target.value)}
                  className={`w-full bg-[#0E0E0E] border rounded-lg px-3.5 py-2.5 text-sm text-white placeholder-gray-500 focus:outline-none ${
                    errors.customerName ? 'border-red-500' : 'border-[#2E2E2E] focus:border-[#D4AF37]'
                  }`}
                />
                {errors.customerName && (
                  <p className="text-[11px] text-red-400 mt-1">{errors.customerName}</p>
                )}
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                  Mobile Number (10 Digits) *
                </label>
                <input
                  type="tel"
                  placeholder="e.g. 9823109313"
                  maxLength={10}
                  value={mobileNumber}
                  onChange={(e) => setMobileNumber(e.target.value)}
                  className={`w-full bg-[#0E0E0E] border rounded-lg px-3.5 py-2.5 text-sm text-white placeholder-gray-500 focus:outline-none ${
                    errors.mobileNumber ? 'border-red-500' : 'border-[#2E2E2E] focus:border-[#D4AF37]'
                  }`}
                />
                {errors.mobileNumber && (
                  <p className="text-[11px] text-red-400 mt-1">{errors.mobileNumber}</p>
                )}
              </div>
            </div>

            {/* Brand & Model */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                  Device Brand *
                </label>
                <select
                  value={selectedBrand}
                  onChange={(e) => setSelectedBrand(e.target.value)}
                  className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3 py-2.5 text-sm text-white focus:outline-none focus:border-[#D4AF37]"
                >
                  {REPAIR_BRANDS.map((brand) => (
                    <option key={brand} value={brand}>{brand}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                  Exact Phone Model *
                </label>
                <input
                  type="text"
                  placeholder="e.g. Galaxy S23, iPhone 12, Redmi Note 13"
                  value={modelName}
                  onChange={(e) => setModelName(e.target.value)}
                  className={`w-full bg-[#0E0E0E] border rounded-lg px-3.5 py-2.5 text-sm text-white placeholder-gray-500 focus:outline-none ${
                    errors.modelName ? 'border-red-500' : 'border-[#2E2E2E] focus:border-[#D4AF37]'
                  }`}
                />
                {errors.modelName && (
                  <p className="text-[11px] text-red-400 mt-1">{errors.modelName}</p>
                )}
              </div>
            </div>

            {/* Primary Problem */}
            <div>
              <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                Primary Issue / Fault *
              </label>
              <select
                value={selectedProblem}
                onChange={(e) => setSelectedProblem(e.target.value)}
                className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3 py-2.5 text-sm text-white focus:outline-none focus:border-[#D4AF37]"
              >
                {REPAIR_SERVICES.map((s) => (
                  <option key={s.id} value={s.title}>
                    {s.title} ({s.priceRange})
                  </option>
                ))}
              </select>
            </div>

            {/* Service Mode Selection (Doorstep Pickup vs Shop Visit) */}
            <div>
              <label className="block text-xs font-semibold text-gray-300 mb-2">
                Service Option in Udgir:
              </label>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <button
                  type="button"
                  onClick={() => setPickupRequired(true)}
                  className={`p-3.5 rounded-xl border text-left flex items-start gap-3 transition ${
                    pickupRequired
                      ? 'border-[#D4AF37] bg-[#D4AF37]/10'
                      : 'border-[#2A2A2A] bg-[#0E0E0E] hover:border-gray-500'
                  }`}
                >
                  <Truck size={20} className={pickupRequired ? 'text-[#D4AF37]' : 'text-gray-400'} />
                  <div>
                    <div className="text-xs font-bold text-white">Doorstep Pickup</div>
                    <div className="text-[11px] text-gray-400">Technician collects device from your home/shop in Udgir</div>
                  </div>
                </button>

                <button
                  type="button"
                  onClick={() => setPickupRequired(false)}
                  className={`p-3.5 rounded-xl border text-left flex items-start gap-3 transition ${
                    !pickupRequired
                      ? 'border-[#D4AF37] bg-[#D4AF37]/10'
                      : 'border-[#2A2A2A] bg-[#0E0E0E] hover:border-gray-500'
                  }`}
                >
                  <Store size={20} className={!pickupRequired ? 'text-[#D4AF37]' : 'text-gray-400'} />
                  <div>
                    <div className="text-xs font-bold text-white">Visit Shop Counter</div>
                    <div className="text-[11px] text-gray-400">Nagar Parishad, Udgir (30-min express counter service)</div>
                  </div>
                </button>
              </div>
            </div>

            {/* Address (If Doorstep) */}
            {pickupRequired && (
              <div>
                <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                  Udgir Pickup Address *
                </label>
                <textarea
                  rows={2}
                  placeholder="House/Shop No., Street, Colony, Landmark in Udgir..."
                  value={address}
                  onChange={(e) => setAddress(e.target.value)}
                  className={`w-full bg-[#0E0E0E] border rounded-lg px-3.5 py-2 text-sm text-white placeholder-gray-500 focus:outline-none ${
                    errors.address ? 'border-red-500' : 'border-[#2E2E2E] focus:border-[#D4AF37]'
                  }`}
                />
                {errors.address && (
                  <p className="text-[11px] text-red-400 mt-1">{errors.address}</p>
                )}
              </div>
            )}

            {/* Preferred Slot */}
            <div>
              <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                Preferred Slot:
              </label>
              <div className="grid grid-cols-2 gap-2">
                {timeSlots.map((slot) => (
                  <button
                    key={slot}
                    type="button"
                    onClick={() => setPreferredSlot(slot)}
                    className={`py-2 px-3 rounded-lg text-xs font-medium border text-left transition ${
                      preferredSlot === slot
                        ? 'border-[#D4AF37] bg-[#D4AF37]/15 text-[#D4AF37] font-bold'
                        : 'border-[#262626] bg-[#0E0E0E] text-gray-400 hover:text-white'
                    }`}
                  >
                    {slot}
                  </button>
                ))}
              </div>
            </div>

            {/* Additional Notes */}
            <div>
              <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                Additional Fault Details (Optional)
              </label>
              <textarea
                rows={2}
                placeholder="e.g. Phone fell in water, display flickering, charging disconnects when moved..."
                value={additionalDetails}
                onChange={(e) => setAdditionalDetails(e.target.value)}
                className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3.5 py-2 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-[#D4AF37]"
              />
            </div>

            {/* Submit Button */}
            <div className="pt-2">
              <button
                type="submit"
                disabled={isSubmitting}
                className="gold-btn w-full !py-3 !text-sm !font-bold"
              >
                {isSubmitting ? (
                  <>
                    <span className="w-4 h-4 border-2 border-black border-t-transparent rounded-full animate-spin"></span>
                    <span>Saving Booking to Cloud Firestore...</span>
                  </>
                ) : currentUser ? (
                  <>
                    <Wrench size={18} />
                    <span>Confirm & Save Repair Booking (Cloud)</span>
                  </>
                ) : (
                  <>
                    <Lock size={18} />
                    <span>Sign In Required to Confirm Booking</span>
                  </>
                )}
              </button>
            </div>
          </form>
        </div>

        {/* Right Column: Service Trust & Pricing Guide */}
        <div className="lg:col-span-5 space-y-6">
          {/* Repair Warranty Guarantee Card */}
          <div className="p-6 rounded-2xl bg-gradient-to-br from-[#1C180B] to-[#141414] border border-[#D4AF37]/40 shadow-lg">
            <div className="flex items-center gap-3 mb-3">
              <div className="p-2.5 rounded-xl bg-[#D4AF37] text-black font-bold">
                <ShieldCheck size={24} />
              </div>
              <div>
                <h3 className="text-base font-black text-white">SK Service Guarantee</h3>
                <p className="text-xs text-[#D4AF37] font-semibold">100% Genuine Replacement Parts</p>
              </div>
            </div>
            <p className="text-xs text-gray-300 leading-relaxed mb-4">
              Every display, battery, and charging port installed at our Udgir center is thoroughly tested and verified with genuine components. Fast diagnosis and dependable turnaround on all repairs.
            </p>
            <div className="space-y-2 text-xs text-gray-300 pt-2 border-t border-[#333333]">
              <div className="flex items-center gap-2">
                <CheckCircle2 size={14} className="text-emerald-400" />
                <span>Zero charge if phone is diagnosed irreparable</span>
              </div>
              <div className="flex items-center gap-2">
                <CheckCircle2 size={14} className="text-emerald-400" />
                <span>Express 30-min repair for displays & batteries</span>
              </div>
              <div className="flex items-center gap-2">
                <CheckCircle2 size={14} className="text-emerald-400" />
                <span>Free doorstep pickup & delivery in Udgir municipal limits</span>
              </div>
            </div>
          </div>

          {/* Service Price Estimator Table */}
          <div className="p-6 rounded-2xl bg-[#141414] border border-[#262626]">
            <h3 className="text-sm font-bold text-white uppercase tracking-wider mb-4 flex items-center gap-2">
              <Clock size={16} className="text-[#D4AF37]" />
              <span>Standard Repair Price Guide</span>
            </h3>

            <div className="space-y-3">
              {REPAIR_SERVICES.map((item) => (
                <div key={item.id} className="flex justify-between items-center py-2 border-b border-[#222222] last:border-none text-xs">
                  <div>
                    <span className="font-semibold text-gray-200 block">{item.title}</span>
                    <span className="text-[10px] text-gray-500">{item.timeEstimate}</span>
                  </div>
                  <span className="font-bold text-[#D4AF37]">{item.priceRange}</span>
                </div>
              ))}
            </div>

            <div className="mt-4 pt-3 border-t border-[#222222] text-[11px] text-gray-400">
              * Exact cost depends on your device make (OLED vs LCD / Flagship models).
            </div>
          </div>
        </div>
      </div>

      {/* Confirmation Modal */}
      {confirmedBooking && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-in fade-in">
          <div className="bg-[#141414] border border-[#D4AF37] rounded-2xl max-w-md w-full p-6 text-white relative shadow-2xl">
            <div className="w-14 h-14 rounded-full bg-emerald-500/20 text-emerald-400 flex items-center justify-center mx-auto mb-4 border border-emerald-500/40">
              <CheckCircle2 size={32} />
            </div>

            <div className="text-center mb-1">
              <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-[10px] text-emerald-400 font-bold uppercase tracking-wider mb-2">
                <Database size={11} />
                <span>Saved to Cloud Firestore</span>
              </span>
              <h3 className="text-xl font-black text-white">
                Repair Request Booked!
              </h3>
            </div>
            
            <p className="text-xs text-gray-400 text-center mb-5">
              Booking ID: <strong className="text-[#D4AF37]">{confirmedBooking.bookingId}</strong>
            </p>

            <div className="bg-[#0E0E0E] rounded-xl p-4 border border-[#222222] space-y-2 text-xs mb-5">
              <div className="flex justify-between">
                <span className="text-gray-400">Device:</span>
                <span className="text-white font-semibold">{confirmedBooking.brand} {confirmedBooking.model}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-400">Fault:</span>
                <span className="text-[#D4AF37] font-semibold">{confirmedBooking.problem}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-400">Customer:</span>
                <span className="text-white">{confirmedBooking.customerName} ({confirmedBooking.mobileNumber})</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-400">Service Mode:</span>
                <span className="text-emerald-400 font-semibold">
                  {confirmedBooking.pickupRequired ? 'Doorstep Pickup' : 'Shop Visit'}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-400">Slot:</span>
                <span className="text-white">{confirmedBooking.preferredSlot}</span>
              </div>
              {confirmedBooking.customerId && (
                <div className="flex justify-between pt-1 border-t border-[#222222] text-[10px]">
                  <span className="text-gray-500">Linked Customer UID:</span>
                  <code className="text-gray-400 font-mono">{confirmedBooking.customerId.slice(0, 10)}...</code>
                </div>
              )}
            </div>

            <p className="text-xs text-gray-300 leading-relaxed text-center mb-6">
              Our technician from SK Mobile Shoppy and Electronics (Udgir) will call you shortly on <strong>{confirmedBooking.mobileNumber}</strong> to confirm your slot.
            </p>

            <div className="flex gap-3">
              <button
                onClick={() => setConfirmedBooking(null)}
                className="gold-btn w-full !py-2.5"
              >
                Done
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
