import React, { useState, useEffect } from 'react';
import { 
  createCustomerOrderInFirestore, 
  signInWithGooglePopup 
} from '../firebase';
import { STORE_INFO } from '../data/storeData';
import { 
  CheckCircle2, 
  AlertCircle, 
  Lock, 
  ShieldCheck, 
  ArrowLeft, 
  ShoppingBag, 
  Phone, 
  MapPin, 
  Truck, 
  CreditCard, 
  Sparkles,
  User,
  ArrowRight
} from 'lucide-react';

export default function CheckoutPage({ 
  cart, 
  currentUser, 
  onClearCart, 
  setActivePage, 
  onShowToast 
}) {
  const [formData, setFormData] = useState({
    customerName: currentUser?.displayName || '',
    phone: '',
    alternatePhone: '',
    address: '',
    city: 'Udgir',
    state: 'Maharashtra',
    pincode: '413517',
    deliveryType: 'Store Pickup (Udgir Showroom)',
    paymentMethod: 'Cash on Delivery (COD)',
    notes: ''
  });

  const [isPlacingOrder, setIsPlacingOrder] = useState(false);
  const [placedOrder, setPlacedOrder] = useState(null);
  const [isSigningIn, setIsSigningIn] = useState(false);
  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (currentUser?.displayName && !formData.customerName) {
      setFormData(prev => ({ ...prev, customerName: currentUser.displayName }));
    }
  }, [currentUser]);

  const subtotal = cart.reduce((sum, item) => {
    const price = typeof item.price === 'number' ? item.price : (parseFloat(item.price) || 0);
    const qty = typeof item.quantity === 'number' ? item.quantity : (parseInt(item.quantity, 10) || 1);
    return sum + (price * qty);
  }, 0);

  const deliveryFee = 0;
  const totalAmount = subtotal + deliveryFee;

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: null }));
    }
  };

  const handleQuickGoogleSignIn = async () => {
    setIsSigningIn(true);
    try {
      const res = await signInWithGooglePopup();
      if (res.user) {
        setFormData(prev => ({
          ...prev,
          customerName: prev.customerName || res.user.displayName || ''
        }));
        onShowToast?.(`Signed in as ${res.user.displayName || res.user.email}!`, 'success');
      }
    } catch (err) {
      console.error('Google Sign-In failed:', err);
      onShowToast?.(err.message || 'Google sign-in error.', 'error');
    } finally {
      setIsSigningIn(false);
    }
  };

  const validateForm = () => {
    const newErrors = {};
    if (!formData.customerName.trim()) {
      newErrors.customerName = 'Please enter your full name.';
    }

    const cleanPhone = formData.phone.replace(/\D/g, '');
    if (!cleanPhone || cleanPhone.length < 10) {
      newErrors.phone = 'Please enter a valid 10-digit mobile number.';
    }

    if (!formData.address.trim()) {
      newErrors.address = 'Please provide your address or landmark.';
    }

    const cleanPin = formData.pincode.replace(/\D/g, '');
    if (!cleanPin || cleanPin.length !== 6) {
      newErrors.pincode = 'Please enter a valid 6-digit PIN code (e.g. 413517 for Udgir).';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handlePlaceOrder = async (e) => {
    e.preventDefault();

    if (!currentUser) {
      onShowToast?.('Please sign in with Google to place and track your order securely.', 'error');
      return;
    }

    if (cart.length === 0) {
      onShowToast?.('Your cart is empty. Add products to checkout.', 'error');
      return;
    }

    if (!validateForm()) {
      onShowToast?.('Please complete all required fields.', 'error');
      return;
    }

    setIsPlacingOrder(true);
    try {
      const orderPayload = {
        customerName: formData.customerName.trim(),
        phone: formData.phone.trim(),
        alternatePhone: formData.alternatePhone.trim(),
        address: formData.address.trim(),
        city: formData.city.trim(),
        state: formData.state.trim(),
        pincode: formData.pincode.trim(),
        deliveryType: formData.deliveryType,
        paymentMethod: formData.paymentMethod,
        notes: formData.notes.trim(),
        items: cart,
        total: Math.round(totalAmount)
      };

      const result = await createCustomerOrderInFirestore(orderPayload, currentUser);

      setPlacedOrder(result.order);
      onClearCart?.();
      onShowToast?.(`Order #${result.orderId} placed successfully!`, 'success');
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } catch (err) {
      console.error('Failed to place order:', err);
      onShowToast?.(err.message || 'Error placing order in Firestore.', 'error');
    } finally {
      setIsPlacingOrder(false);
    }
  };

  // -------------------------------------------------------------
  // SUCCESS SCREEN
  // -------------------------------------------------------------
  if (placedOrder) {
    return (
      <div className="container py-12">
        <div className="max-w-2xl mx-auto bg-[#141414] border border-[#2E2E2E] rounded-3xl p-6 sm:p-10 text-center space-y-8 relative overflow-hidden">
          <div className="absolute top-0 left-0 right-0 h-1.5 bg-gradient-to-r from-emerald-500 via-[#D4AF37] to-emerald-500"></div>

          <div className="w-20 h-20 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 flex items-center justify-center mx-auto shadow-lg shadow-emerald-500/10">
            <CheckCircle2 size={40} />
          </div>

          <div className="space-y-2">
            <span className="text-[11px] font-bold uppercase tracking-widest text-emerald-400">
              Order Confirmed & Logged
            </span>
            <h1 className="text-2xl sm:text-3xl font-black text-white">
              Thank You, {placedOrder.customerName}!
            </h1>
            <p className="text-xs sm:text-sm text-gray-400 max-w-lg mx-auto leading-relaxed">
              Your order has been recorded in our store system. Our Udgir showroom team is preparing your items.
            </p>
          </div>

          {/* Order Details Ticket */}
          <div className="p-5 rounded-2xl bg-[#0D0D0D] border border-[#262626] text-left space-y-4 text-xs">
            <div className="flex flex-wrap items-center justify-between gap-2 border-b border-[#222222] pb-3">
              <div>
                <span className="text-gray-500 block text-[10px] uppercase font-bold">Order ID</span>
                <span className="text-sm font-black text-[#D4AF37] font-mono">#{placedOrder.id}</span>
              </div>
              <div>
                <span className="text-gray-500 block text-[10px] uppercase font-bold">Status</span>
                <span className="inline-block px-2.5 py-0.5 rounded-full bg-amber-500/20 text-amber-300 font-bold border border-amber-500/30">
                  {placedOrder.orderStatus}
                </span>
              </div>
              <div>
                <span className="text-gray-500 block text-[10px] uppercase font-bold">Payment</span>
                <span className="font-semibold text-white">{placedOrder.paymentMethod}</span>
              </div>
            </div>

            {/* Items Summary */}
            <div className="space-y-2">
              <span className="text-[11px] font-bold text-gray-400 uppercase tracking-wider">Ordered Items</span>
              <div className="divide-y divide-[#1F1F1F]">
                {placedOrder.items?.map((item, idx) => (
                  <div key={idx} className="py-2 flex items-center justify-between gap-2">
                    <div>
                      <span className="font-bold text-white block">{item.name}</span>
                      <span className="text-gray-500 text-[11px]">Qty: {item.quantity} × ₹{item.price.toLocaleString('en-IN')}</span>
                    </div>
                    <span className="font-black text-gray-200">
                      ₹{(item.price * item.quantity).toLocaleString('en-IN')}
                    </span>
                  </div>
                ))}
              </div>
            </div>

            {/* Total */}
            <div className="pt-3 border-t border-[#222222] flex justify-between items-baseline">
              <span className="font-bold text-gray-300">Total Payable</span>
              <span className="text-lg font-black text-[#D4AF37]">
                ₹{placedOrder.total?.toLocaleString('en-IN')}
              </span>
            </div>

            {/* Address */}
            <div className="pt-2 border-t border-[#1F1F1F] text-[11px] text-gray-400 space-y-1">
              <div><strong className="text-gray-300">Fulfillment:</strong> {placedOrder.deliveryType}</div>
              <div><strong className="text-gray-300">Contact Address:</strong> {placedOrder.address}, {placedOrder.city} - {placedOrder.pincode}</div>
              <div><strong className="text-gray-300">Mobile:</strong> {placedOrder.phone}</div>
            </div>
          </div>

          {/* Hotline Callout */}
          <div className="p-4 rounded-xl bg-[#1C180C] border border-[#D4AF37]/30 text-xs text-gray-300 flex flex-col sm:flex-row items-center justify-between gap-3 text-left">
            <div>
              <strong className="text-white block font-bold">Need instant pickup or have questions?</strong>
              <span className="text-gray-400 text-[11px]">Our Udgir store counter is open today 9:30 AM – 9:30 PM.</span>
            </div>
            <a
              href={`tel:${STORE_INFO.phone}`}
              className="gold-btn !text-xs !py-2 !px-4 whitespace-nowrap shrink-0"
            >
              <Phone size={13} />
              <span>Call: {STORE_INFO.displayPhone}</span>
            </a>
          </div>

          {/* Action CTAs */}
          <div className="flex flex-col sm:flex-row gap-3 pt-2">
            <button
              onClick={() => { setActivePage('my-orders'); window.scrollTo({ top: 0, behavior: 'smooth' }); }}
              className="gold-btn flex-1 !text-xs !py-3 font-bold"
            >
              <span>View My Orders</span>
            </button>
            <button
              onClick={() => { setActivePage('home'); window.scrollTo({ top: 0, behavior: 'smooth' }); }}
              className="outline-gold-btn flex-1 !text-xs !py-3 font-semibold"
            >
              <span>Back to Store Front</span>
            </button>
          </div>
        </div>
      </div>
    );
  }

  // -------------------------------------------------------------
  // EMPTY CART CHECK
  // -------------------------------------------------------------
  if (cart.length === 0) {
    return (
      <div className="container py-16 text-center space-y-4">
        <h2 className="text-2xl font-black text-white">Your cart is empty</h2>
        <p className="text-xs text-gray-400">Add products to your cart before proceeding to checkout.</p>
        <button
          onClick={() => setActivePage('products')}
          className="gold-btn !text-xs !py-2.5 !px-5 inline-flex items-center gap-2"
        >
          <ArrowLeft size={14} />
          <span>Browse Smartphones</span>
        </button>
      </div>
    );
  }

  return (
    <div className="container py-8 space-y-8">
      {/* Header */}
      <div className="flex items-center justify-between pb-4 border-b border-[#222222]">
        <div>
          <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-[#D4AF37] mb-1">
            <Lock size={14} />
            <span>Secure Store Checkout</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-black text-white">
            Complete Your Order
          </h1>
        </div>

        <button
          onClick={() => setActivePage('cart')}
          className="text-xs font-semibold text-gray-400 hover:text-white transition flex items-center gap-1.5"
        >
          <ArrowLeft size={14} />
          <span>Return to Cart</span>
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Form Column */}
        <div className="lg:col-span-2 space-y-6">
          {/* User Sign-In Banner */}
          {!currentUser ? (
            <div className="p-4 sm:p-5 rounded-2xl bg-amber-500/10 border border-amber-500/30 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
              <div className="space-y-1">
                <h3 className="text-sm font-bold text-amber-300 flex items-center gap-2">
                  <User size={16} />
                  <span>Google Sign-In Required to Secure Order</span>
                </h3>
                <p className="text-xs text-gray-300 leading-relaxed">
                  Sign in with 1 click to track your order history and receive live status updates.
                </p>
              </div>
              <button
                type="button"
                onClick={handleQuickGoogleSignIn}
                disabled={isSigningIn}
                className="gold-btn !text-xs !py-2.5 !px-4 whitespace-nowrap shrink-0 shadow-md"
              >
                <span>{isSigningIn ? 'Connecting...' : 'Sign In with Google'}</span>
              </button>
            </div>
          ) : (
            <div className="p-3.5 rounded-xl bg-emerald-500/10 border border-emerald-500/30 flex items-center justify-between text-xs text-emerald-300">
              <div className="flex items-center gap-2">
                <CheckCircle2 size={16} className="text-emerald-400 shrink-0" />
                <span>
                  Logged in as: <strong>{currentUser.displayName || currentUser.email}</strong>
                </span>
              </div>
              <span className="text-[10px] uppercase font-bold px-2 py-0.5 rounded bg-emerald-500/20 border border-emerald-500/40">
                Verified Account
              </span>
            </div>
          )}

          <form onSubmit={handlePlaceOrder} className="space-y-6">
            {/* 1. Customer Contact Details */}
            <div className="bg-[#141414] border border-[#262626] rounded-2xl p-6 space-y-4">
              <h2 className="text-sm font-bold text-white uppercase tracking-wider flex items-center gap-2 border-b border-[#222222] pb-3">
                <span className="w-6 h-6 rounded-full bg-[#D4AF37]/20 text-[#D4AF37] flex items-center justify-center text-xs font-black">1</span>
                <span>Contact & Customer Details</span>
              </h2>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                    Full Name <span className="text-red-400">*</span>
                  </label>
                  <input
                    type="text"
                    name="customerName"
                    value={formData.customerName}
                    onChange={handleInputChange}
                    placeholder="Enter your full name"
                    className={`w-full bg-[#0D0D0D] border ${errors.customerName ? 'border-red-500' : 'border-[#2E2E2E]'} rounded-xl px-3.5 py-2.5 text-xs text-white focus:outline-none focus:border-[#D4AF37]`}
                  />
                  {errors.customerName && (
                    <span className="text-[11px] text-red-400 mt-1 block">{errors.customerName}</span>
                  )}
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                    10-Digit Mobile Number <span className="text-red-400">*</span>
                  </label>
                  <div className="relative">
                    <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-xs text-gray-500 font-semibold">+91</span>
                    <input
                      type="tel"
                      name="phone"
                      maxLength={10}
                      value={formData.phone}
                      onChange={handleInputChange}
                      placeholder="9876543210"
                      className={`w-full bg-[#0D0D0D] border ${errors.phone ? 'border-red-500' : 'border-[#2E2E2E]'} rounded-xl pl-12 pr-3.5 py-2.5 text-xs text-white focus:outline-none focus:border-[#D4AF37]`}
                    />
                  </div>
                  {errors.phone && (
                    <span className="text-[11px] text-red-400 mt-1 block">{errors.phone}</span>
                  )}
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                    Alternate Mobile Number (Optional)
                  </label>
                  <input
                    type="tel"
                    name="alternatePhone"
                    maxLength={10}
                    value={formData.alternatePhone}
                    onChange={handleInputChange}
                    placeholder="Optional backup phone"
                    className="w-full bg-[#0D0D0D] border border-[#2E2E2E] rounded-xl px-3.5 py-2.5 text-xs text-white focus:outline-none focus:border-[#D4AF37]"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                    PIN Code <span className="text-red-400">*</span>
                  </label>
                  <input
                    type="text"
                    name="pincode"
                    maxLength={6}
                    value={formData.pincode}
                    onChange={handleInputChange}
                    placeholder="413517"
                    className={`w-full bg-[#0D0D0D] border ${errors.pincode ? 'border-red-500' : 'border-[#2E2E2E]'} rounded-xl px-3.5 py-2.5 text-xs text-white focus:outline-none focus:border-[#D4AF37]`}
                  />
                  {errors.pincode && (
                    <span className="text-[11px] text-red-400 mt-1 block">{errors.pincode}</span>
                  )}
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                  Complete Address & Landmark <span className="text-red-400">*</span>
                </label>
                <textarea
                  rows={2}
                  name="address"
                  value={formData.address}
                  onChange={handleInputChange}
                  placeholder="House / Shop No., Street, Colony, Near Nagar Parishad / Bus Stand, Udgir"
                  className={`w-full bg-[#0D0D0D] border ${errors.address ? 'border-red-500' : 'border-[#2E2E2E]'} rounded-xl p-3 text-xs text-white focus:outline-none focus:border-[#D4AF37]`}
                />
                {errors.address && (
                  <span className="text-[11px] text-red-400 mt-1 block">{errors.address}</span>
                )}
              </div>
            </div>

            {/* 2. Fulfillment Preference */}
            <div className="bg-[#141414] border border-[#262626] rounded-2xl p-6 space-y-4">
              <h2 className="text-sm font-bold text-white uppercase tracking-wider flex items-center gap-2 border-b border-[#222222] pb-3">
                <span className="w-6 h-6 rounded-full bg-[#D4AF37]/20 text-[#D4AF37] flex items-center justify-center text-xs font-black">2</span>
                <span>Fulfillment Method</span>
              </h2>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <label 
                  className={`p-4 rounded-xl border cursor-pointer transition flex items-start gap-3 ${
                    formData.deliveryType.includes('Store Pickup') 
                      ? 'border-[#D4AF37] bg-[#D4AF37]/10' 
                      : 'border-[#2E2E2E] bg-[#0E0E0E] hover:border-gray-500'
                  }`}
                >
                  <input
                    type="radio"
                    name="deliveryType"
                    value="Store Pickup (Udgir Showroom)"
                    checked={formData.deliveryType.includes('Store Pickup')}
                    onChange={handleInputChange}
                    className="mt-0.5 accent-[#D4AF37]"
                  />
                  <div>
                    <strong className="text-xs font-bold text-white block">Local Store Pickup (Free)</strong>
                    <p className="text-[11px] text-gray-400 mt-0.5">
                      Pickup instantly at our showroom opposite Nagar Parishad, Udgir.
                    </p>
                  </div>
                </label>

                <label 
                  className={`p-4 rounded-xl border cursor-pointer transition flex items-start gap-3 ${
                    formData.deliveryType.includes('Doorstep') 
                      ? 'border-[#D4AF37] bg-[#D4AF37]/10' 
                      : 'border-[#2E2E2E] bg-[#0E0E0E] hover:border-gray-500'
                  }`}
                >
                  <input
                    type="radio"
                    name="deliveryType"
                    value="Udgir Doorstep Delivery"
                    checked={formData.deliveryType.includes('Doorstep')}
                    onChange={handleInputChange}
                    className="mt-0.5 accent-[#D4AF37]"
                  />
                  <div>
                    <strong className="text-xs font-bold text-white block">Udgir Doorstep Express (Free)</strong>
                    <p className="text-[11px] text-gray-400 mt-0.5">
                      Hand-delivered to your door within Udgir city on the same day.
                    </p>
                  </div>
                </label>
              </div>
            </div>

            {/* 3. Payment Mode */}
            <div className="bg-[#141414] border border-[#262626] rounded-2xl p-6 space-y-4">
              <h2 className="text-sm font-bold text-white uppercase tracking-wider flex items-center gap-2 border-b border-[#222222] pb-3">
                <span className="w-6 h-6 rounded-full bg-[#D4AF37]/20 text-[#D4AF37] flex items-center justify-center text-xs font-black">3</span>
                <span>Payment Method</span>
              </h2>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <label 
                  className={`p-4 rounded-xl border cursor-pointer transition flex items-start gap-3 ${
                    formData.paymentMethod === 'Cash on Delivery (COD)' 
                      ? 'border-[#D4AF37] bg-[#D4AF37]/10' 
                      : 'border-[#2E2E2E] bg-[#0E0E0E] hover:border-gray-500'
                  }`}
                >
                  <input
                    type="radio"
                    name="paymentMethod"
                    value="Cash on Delivery (COD)"
                    checked={formData.paymentMethod === 'Cash on Delivery (COD)'}
                    onChange={handleInputChange}
                    className="mt-0.5 accent-[#D4AF37]"
                  />
                  <div>
                    <strong className="text-xs font-bold text-white block">Cash on Delivery / Pickup</strong>
                    <p className="text-[11px] text-gray-400 mt-0.5">
                      Pay cash at the shop counter or upon delivery after checking device seal.
                    </p>
                  </div>
                </label>

                <label 
                  className={`p-4 rounded-xl border cursor-pointer transition flex items-start gap-3 ${
                    formData.paymentMethod === 'UPI / QR Code on Delivery' 
                      ? 'border-[#D4AF37] bg-[#D4AF37]/10' 
                      : 'border-[#2E2E2E] bg-[#0E0E0E] hover:border-gray-500'
                  }`}
                >
                  <input
                    type="radio"
                    name="paymentMethod"
                    value="UPI / QR Code on Delivery"
                    checked={formData.paymentMethod === 'UPI / QR Code on Delivery'}
                    onChange={handleInputChange}
                    className="mt-0.5 accent-[#D4AF37]"
                  />
                  <div>
                    <strong className="text-xs font-bold text-white block">UPI / Scan & Pay on Delivery</strong>
                    <p className="text-[11px] text-gray-400 mt-0.5">
                      Google Pay, PhonePe, Paytm, or BHIM QR code when you receive your order.
                    </p>
                  </div>
                </label>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                  Order Instructions / Color Preference (Optional)
                </label>
                <input
                  type="text"
                  name="notes"
                  value={formData.notes}
                  onChange={handleInputChange}
                  placeholder="e.g. Call before coming, prefer Black color"
                  className="w-full bg-[#0D0D0D] border border-[#2E2E2E] rounded-xl px-3.5 py-2.5 text-xs text-white focus:outline-none focus:border-[#D4AF37]"
                />
              </div>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={isPlacingOrder}
              className="gold-btn w-full !py-4 !text-sm flex items-center justify-center gap-2 shadow-xl shadow-[#D4AF37]/20 font-black cursor-pointer disabled:opacity-50"
            >
              <Lock size={16} />
              <span>{isPlacingOrder ? 'Confirming & Placing Order...' : `Confirm & Place Order (₹${totalAmount.toLocaleString('en-IN')})`}</span>
            </button>
          </form>
        </div>

        {/* Order Summary Column */}
        <div className="space-y-4">
          <div className="p-6 rounded-2xl bg-[#141414] border border-[#2E2E2E] space-y-6 sticky top-24">
            <h2 className="text-base font-black text-white border-b border-[#222222] pb-3">
              Order Items ({cart.length})
            </h2>

            {/* List */}
            <div className="space-y-3 max-h-72 overflow-y-auto divide-y divide-[#1F1F1F] pr-1">
              {cart.map((item) => {
                const p = typeof item.price === 'number' ? item.price : (parseFloat(item.price) || 0);
                const q = typeof item.quantity === 'number' ? item.quantity : 1;
                return (
                  <div key={item.id} className="pt-3 first:pt-0 flex items-center justify-between gap-3 text-xs">
                    <div className="flex-1 min-w-0">
                      <h4 className="font-bold text-white truncate">{item.name}</h4>
                      <span className="text-[11px] text-gray-500">Qty: {q} × ₹{p.toLocaleString('en-IN')}</span>
                    </div>
                    <span className="font-black text-[#D4AF37] shrink-0">
                      ₹{(p * q).toLocaleString('en-IN')}
                    </span>
                  </div>
                );
              })}
            </div>

            {/* Price Calculations */}
            <div className="space-y-2.5 pt-3 border-t border-[#262626] text-xs">
              <div className="flex justify-between text-gray-400">
                <span>Subtotal</span>
                <span className="text-white font-semibold">₹{subtotal.toLocaleString('en-IN')}</span>
              </div>
              <div className="flex justify-between text-gray-400">
                <span>Shipping / Pickup</span>
                <span className="text-emerald-400 font-bold">FREE</span>
              </div>
              <div className="flex justify-between text-gray-400">
                <span>Taxes & GST</span>
                <span className="text-gray-400">Included</span>
              </div>

              <div className="pt-3 border-t border-[#262626] flex justify-between items-baseline">
                <span className="text-sm font-bold text-white">Final Total</span>
                <span className="text-2xl font-black text-[#D4AF37]">
                  ₹{totalAmount.toLocaleString('en-IN')}
                </span>
              </div>
            </div>

            {/* Guarantee Callout */}
            <div className="p-3.5 rounded-xl bg-[#0E0E0E] border border-[#222222] space-y-2 text-[11px] text-gray-400">
              <div className="flex items-center gap-2 text-white font-semibold">
                <ShieldCheck size={15} className="text-[#D4AF37]" />
                <span>SK Mobile Verified Showroom</span>
              </div>
              <p>
                Opposite Municipal Complex, Nagar Parishad, Udgir. All orders verified by phone prior to delivery.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
