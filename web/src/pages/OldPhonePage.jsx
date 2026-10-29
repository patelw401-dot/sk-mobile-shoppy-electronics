import React, { useState, useEffect } from 'react';
import { PRE_OWNED_PHONES, STORE_INFO } from '../data/storeData';
import ProductCard from '../components/ProductCard';
import { saveOldPhoneRequestToFirestore, signInWithGooglePopup } from '../firebase';
import { 
  RefreshCw, 
  ShieldCheck, 
  BatteryCharging, 
  CheckCircle2, 
  Calculator, 
  DollarSign, 
  Truck,
  Phone,
  Sparkles,
  Lock,
  UserCheck,
  AlertCircle,
  Database,
  ArrowRight
} from 'lucide-react';

export default function OldPhonePage({ currentUser, setActivePage, onShowToast }) {
  const [activeTab, setActiveTab] = useState('buy'); // 'buy' or 'sell'

  // Sell Phone form state
  const [sellBrand, setSellBrand] = useState('Apple');
  const [sellModel, setSellModel] = useState('');
  const [sellStorage, setSellStorage] = useState('128GB');
  const [sellCondition, setSellCondition] = useState('Good (Minor wear, 100% working)');
  const [sellerName, setSellerName] = useState(currentUser?.displayName || '');
  const [sellerPhone, setSellerPhone] = useState('');
  const [sellerAddress, setSellerAddress] = useState('');

  const [generatedQuote, setGeneratedQuote] = useState(null);
  const [isCalculating, setIsCalculating] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isSigningIn, setIsSigningIn] = useState(false);
  const [errors, setErrors] = useState({});
  const [firestoreError, setFirestoreError] = useState(null);
  const [confirmedRequest, setConfirmedRequest] = useState(null);
  const [sellSubmitted, setSellSubmitted] = useState(false);

  // Sync seller name from auth when user signs in
  useEffect(() => {
    if (currentUser?.displayName && !sellerName) {
      setSellerName(currentUser.displayName);
    }
  }, [currentUser]);

  const handleQuickGoogleSignIn = async () => {
    setIsSigningIn(true);
    setFirestoreError(null);
    try {
      const result = await signInWithGooglePopup();
      const user = result.user;
      if (user.displayName) setSellerName(user.displayName);
      onShowToast?.(`Signed in as ${user.displayName || user.email}! You can now submit your sell request.`, 'success');
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

  const calculateQuote = (e) => {
    e.preventDefault();
    if (!sellModel.trim()) {
      onShowToast?.('Please enter your phone model to calculate value.', 'error');
      return;
    }

    setIsCalculating(true);
    setTimeout(() => {
      let baseVal = 12000;
      if (sellBrand === 'Apple') baseVal = 26000;
      else if (sellBrand === 'Samsung') baseVal = 18000;
      else if (sellBrand === 'OnePlus') baseVal = 16000;

      if (sellStorage === '256GB') baseVal += 2500;
      if (sellStorage === '512GB') baseVal += 5000;

      if (sellCondition.includes('Flawless')) baseVal = Math.round(baseVal * 1.15);
      else if (sellCondition.includes('Heavy')) baseVal = Math.round(baseVal * 0.7);

      const minQuote = Math.round(baseVal * 0.9);
      const maxQuote = Math.round(baseVal * 1.05);

      setGeneratedQuote({
        min: minQuote,
        max: maxQuote,
        model: `${sellBrand} ${sellModel} (${sellStorage})`,
        condition: sellCondition
      });
      setIsCalculating(false);
      onShowToast?.('Instant valuation generated! Review your quote below.', 'info');
    }, 500);
  };

  const handleSellRequest = async (e) => {
    e.preventDefault();
    if (isSubmitting) return;
    setFirestoreError(null);

    // Rule 1: Zero-trust authentication enforcement
    if (!currentUser || !currentUser.uid) {
      const authErr = 'Customer authentication required: Firestore security rules require an authenticated user with customerId matching request.auth.uid.';
      setFirestoreError(authErr);
      onShowToast?.('Please sign in with your customer account before requesting device inspection.', 'error');
      return;
    }

    const newErrors = {};
    if (!sellModel.trim()) newErrors.sellModel = 'Please enter your phone model in Step 1';
    if (!sellerName.trim()) newErrors.sellerName = 'Please enter your full name';
    if (!sellerPhone.trim() || sellerPhone.trim().length < 10) {
      newErrors.sellerPhone = 'Please enter a valid 10-digit mobile number';
    }
    if (!sellerAddress.trim()) {
      newErrors.sellerAddress = 'Please enter your doorstep address or landmark in Udgir';
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      onShowToast?.('Please fill all required fields correctly before submitting.', 'error');
      return;
    }

    setErrors({});
    setIsSubmitting(true);

    try {
      const requestId = `SKM-OLD-${Date.now().toString().slice(-6)}`;
      const quoteString = generatedQuote 
        ? `₹${generatedQuote.min.toLocaleString('en-IN')} – ₹${generatedQuote.max.toLocaleString('en-IN')}`
        : 'Market Valuation Pending';

      const requestPayload = {
        requestId,
        customerName: sellerName.trim(),
        phone: sellerPhone.trim(),
        brand: sellBrand,
        model: sellModel.trim(),
        storage: sellStorage,
        condition: sellCondition,
        expectedPrice: quoteString,
        estimatedQuote: quoteString,
        requestType: 'SELL',
        status: 'Submitted',
        address: sellerAddress.trim()
      };

      const result = await saveOldPhoneRequestToFirestore(requestPayload, currentUser);

      setConfirmedRequest({
        requestId: result.requestId,
        customerName: sellerName.trim(),
        phone: sellerPhone.trim(),
        brand: sellBrand,
        model: sellModel.trim(),
        storage: sellStorage,
        condition: sellCondition,
        estimatedQuote: quoteString,
        address: sellerAddress.trim(),
        status: 'Submitted',
        date: new Date().toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' }),
        customerId: currentUser.uid,
        isCloudSaved: true
      });

      setSellSubmitted(true);
      onShowToast?.(`Sell request ${result.requestId} successfully saved to Firestore!`, 'success');
    } catch (err) {
      console.error('Firestore saveOldPhoneRequest error:', err);
      const errMsg = err.code === 'permission-denied'
        ? 'Permission denied: Please ensure your account has customer permissions.'
        : (err.message || 'Failed to save sell request to Firestore. Please check your connection.');
      setFirestoreError(errMsg);
      onShowToast?.(`Request submission error: ${errMsg}`, 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="container py-8 space-y-10">
      {/* Header */}
      <div className="border-b border-[#222222] pb-6">
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-500/15 border border-emerald-500/30 text-xs font-bold text-emerald-400 mb-2">
          <RefreshCw size={13} />
          <span>SK Mobile Certified Re-Commerce • Udgir</span>
        </div>
        <h1 className="text-3xl font-black text-white">Buy & Sell Pre-Owned Phones</h1>
        <p className="text-xs sm:text-sm text-gray-400 mt-1 max-w-2xl">
          Upgrade your phone for less or sell your old device for instant cash at our Nagar Parishad, Udgir store. Thorough 32-point inspection with 6 months warranty.
        </p>

        {/* Tab Switcher */}
        <div className="flex items-center gap-3 mt-6">
          <button
            onClick={() => setActiveTab('buy')}
            className={`px-5 py-2.5 rounded-xl text-xs sm:text-sm font-bold transition flex items-center gap-2 ${
              activeTab === 'buy'
                ? 'bg-[#D4AF37] text-black shadow-lg shadow-[#D4AF37]/20'
                : 'bg-[#141414] text-gray-400 hover:text-white border border-[#262626]'
            }`}
          >
            <ShieldCheck size={16} />
            <span>Buy Certified Phones ({PRE_OWNED_PHONES.length})</span>
          </button>

          <button
            onClick={() => setActiveTab('sell')}
            className={`px-5 py-2.5 rounded-xl text-xs sm:text-sm font-bold transition flex items-center gap-2 ${
              activeTab === 'sell'
                ? 'bg-[#D4AF37] text-black shadow-lg shadow-[#D4AF37]/20'
                : 'bg-[#141414] text-gray-400 hover:text-white border border-[#262626]'
            }`}
          >
            <Calculator size={16} />
            <span>Sell Old Phone (Instant Cash)</span>
          </button>
        </div>
      </div>

      {/* Tab 1: Buy Pre-Owned Phones */}
      {activeTab === 'buy' && (
        <div className="space-y-8">
          {/* Trust Banner */}
          <div className="p-4 rounded-xl bg-[#141414] border border-[#262626] flex flex-wrap items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-lg bg-emerald-500/10 text-emerald-400">
                <ShieldCheck size={20} />
              </div>
              <p className="text-xs text-gray-300">
                All pre-owned phones pass a <strong>32-point technician diagnosis</strong> covering display touch, original battery health, face ID/fingerprint, cameras, and network antennas.
              </p>
            </div>
            <span className="text-xs font-bold text-[#D4AF37] px-3 py-1 rounded bg-[#1F1F1F] border border-[#333333]">
              6-Month Store Warranty Included
            </span>
          </div>

          {/* Cards Grid */}
          <div className="grid-cards">
            {PRE_OWNED_PHONES.map((phone) => (
              <ProductCard
                key={phone.id}
                product={phone}
                onAction={(p) => {
                  onShowToast?.(`Inquiry for ${p.name} noted. Visit Nagar Parishad store for physical inspection!`);
                }}
              />
            ))}
          </div>
        </div>
      )}

      {/* Tab 2: Sell Your Old Phone */}
      {activeTab === 'sell' && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
          {/* Left: Instant Valuation Calculator */}
          <div className="lg:col-span-7 bg-[#141414] border border-[#262626] rounded-2xl p-6 sm:p-8">
            <h2 className="text-lg font-bold text-white mb-2 flex items-center gap-2">
              <Calculator size={20} className="text-[#D4AF37]" />
              <span>Step 1: Calculate Your Phone's Resale Value</span>
            </h2>
            <p className="text-xs text-gray-400 mb-6">
              Get an accurate estimation in 10 seconds based on current Udgir market trade-in prices.
            </p>

            <form onSubmit={calculateQuote} className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                    Brand *
                  </label>
                  <select
                    value={sellBrand}
                    onChange={(e) => setSellBrand(e.target.value)}
                    className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-[#D4AF37]"
                  >
                    <option value="Apple">Apple iPhone</option>
                    <option value="Samsung">Samsung</option>
                    <option value="OnePlus">OnePlus</option>
                    <option value="Xiaomi">Xiaomi / Redmi</option>
                    <option value="Realme">Realme</option>
                    <option value="Vivo">Vivo</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                    Model Name *
                  </label>
                  <input
                    type="text"
                    placeholder="e.g. iPhone 12, Galaxy S21, Nord CE"
                    value={sellModel}
                    onChange={(e) => {
                      setSellModel(e.target.value);
                      if (errors.sellModel) setErrors((prev) => ({ ...prev, sellModel: null }));
                    }}
                    className={`w-full bg-[#0E0E0E] border rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-[#D4AF37] ${
                      errors.sellModel ? 'border-red-500' : 'border-[#2E2E2E]'
                    }`}
                  />
                  {errors.sellModel && (
                    <p className="text-[11px] text-red-400 mt-1">{errors.sellModel}</p>
                  )}
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                    Storage Capacity:
                  </label>
                  <select
                    value={sellStorage}
                    onChange={(e) => setSellStorage(e.target.value)}
                    className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-[#D4AF37]"
                  >
                    <option value="64GB">64GB</option>
                    <option value="128GB">128GB</option>
                    <option value="256GB">256GB</option>
                    <option value="512GB">512GB</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                    Physical & Working Condition:
                  </label>
                  <select
                    value={sellCondition}
                    onChange={(e) => setSellCondition(e.target.value)}
                    className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-[#D4AF37]"
                  >
                    <option value="Flawless (No scratches, 100% working)">Flawless (Like New)</option>
                    <option value="Good (Minor wear, 100% working)">Good (Minor hairline wear)</option>
                    <option value="Average (Noticeable scratches, working)">Average (Noticeable scratches)</option>
                    <option value="Heavy Scratches / Cracked Glass">Heavy Scratches / Glass cracked</option>
                  </select>
                </div>
              </div>

              <button
                type="submit"
                disabled={isCalculating}
                className="gold-btn w-full !py-2.5 !text-sm mt-2"
              >
                {isCalculating ? 'Evaluating Market Price...' : 'Get Instant Cash Valuation'}
              </button>
            </form>

            {/* Generated Quote Box */}
            {generatedQuote && (
              <div className="mt-6 p-4 rounded-xl bg-gradient-to-r from-[#1C1808] to-[#121212] border border-[#D4AF37] animate-in fade-in">
                <div className="text-xs text-gray-400 mb-1">Estimated Shop Exchange / Cash Quote:</div>
                <div className="text-2xl font-black text-[#D4AF37] mb-1">
                  ₹{generatedQuote.min.toLocaleString('en-IN')} – ₹{generatedQuote.max.toLocaleString('en-IN')}
                </div>
                <div className="text-[11px] text-gray-300">
                  For: <strong>{generatedQuote.model}</strong> • {generatedQuote.condition}
                </div>
                <div className="text-[10px] text-emerald-400 mt-2 font-medium">
                  ✓ Instant Cash payout or upgrade credit toward any brand new phone in stock.
                </div>
              </div>
            )}
          </div>

          {/* Right: Submission Form */}
          <div className="lg:col-span-5 bg-[#141414] border border-[#262626] rounded-2xl p-6 sm:p-8">
            <h2 className="text-lg font-bold text-white mb-2 flex items-center gap-2">
              <Truck size={20} className="text-emerald-400" />
              <span>Step 2: Request Free Doorstep Pickup</span>
            </h2>
            <p className="text-xs text-gray-400 mb-5">
              Our Udgir technician will visit your location, verify device functionality, and hand over cash on the spot.
            </p>

            {/* Customer Authentication Status Banner */}
            <div className="mb-5">
              {currentUser ? (
                <div className="p-3 rounded-xl bg-[#0E0E0E] border border-emerald-500/30 flex items-center justify-between gap-3 text-xs">
                  <div className="flex items-center gap-2 text-emerald-400">
                    <UserCheck size={16} className="shrink-0" />
                    <div>
                      <span className="font-semibold text-white">Signed in as:</span>{' '}
                      {currentUser.displayName || currentUser.email}
                    </div>
                  </div>
                  <span className="px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-400 font-mono text-[10px] border border-emerald-500/30 shrink-0">
                    UID: {currentUser.uid.slice(0, 6)}…
                  </span>
                </div>
              ) : (
                <div className="p-3.5 rounded-xl bg-[#1C1500]/70 border border-[#D4AF37]/40 space-y-2.5">
                  <div className="flex items-start gap-2 text-[#D4AF37] text-xs">
                    <Lock size={15} className="shrink-0 mt-0.5" />
                    <span className="text-gray-300">
                      Sign-in required to submit a sell request and schedule pickup.
                    </span>
                  </div>
                  <div className="flex items-center gap-2 pt-1">
                    <button
                      type="button"
                      onClick={handleQuickGoogleSignIn}
                      disabled={isSigningIn}
                      className="px-3 py-1.5 rounded-lg bg-[#D4AF37] text-black font-bold text-xs hover:bg-[#F3E5AB] transition flex items-center gap-1.5 shrink-0"
                    >
                      {isSigningIn ? 'Connecting...' : 'Sign In with Google'}
                    </button>
                    {setActivePage && (
                      <button
                        type="button"
                        onClick={() => setActivePage('login')}
                        className="px-3 py-1.5 rounded-lg bg-[#1F1F1F] text-gray-300 hover:text-white font-medium text-xs border border-[#333333] transition"
                      >
                        Account Page
                      </button>
                    )}
                  </div>
                </div>
              )}
            </div>

            {/* Firestore Write Error Alert */}
            {firestoreError && (
              <div className="mb-4 p-3 rounded-lg bg-red-950/40 border border-red-800/60 text-red-400 text-xs flex items-start gap-2">
                <AlertCircle size={16} className="shrink-0 mt-0.5" />
                <div>
                  <p className="font-semibold">Submission Error</p>
                  <p className="text-[11px] text-gray-300 mt-0.5">{firestoreError}</p>
                </div>
              </div>
            )}

            {sellSubmitted && confirmedRequest ? (
              <div className="p-6 rounded-xl bg-[#0E0E0E] border border-emerald-500/40 space-y-4">
                <div className="w-12 h-12 rounded-full bg-emerald-500/20 text-emerald-400 flex items-center justify-center mx-auto">
                  <CheckCircle2 size={28} />
                </div>
                <div className="text-center">
                  <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 text-[10px] font-bold mb-2">
                    <Database size={11} /> Saved to Cloud Firestore
                  </span>
                  <h3 className="text-base font-bold text-white">Sell Request Confirmed!</h3>
                  <p className="text-xs text-gray-400 mt-1">
                    Request ID: <span className="font-mono text-[#D4AF37] font-bold">{confirmedRequest.requestId}</span>
                  </p>
                </div>

                <div className="p-3.5 rounded-lg bg-[#141414] border border-[#262626] text-xs space-y-2">
                  <div className="flex justify-between text-gray-400">
                    <span>Device:</span>
                    <span className="text-white font-medium">{confirmedRequest.brand} {confirmedRequest.model} ({confirmedRequest.storage})</span>
                  </div>
                  <div className="flex justify-between text-gray-400">
                    <span>Condition:</span>
                    <span className="text-white font-medium">{confirmedRequest.condition}</span>
                  </div>
                  <div className="flex justify-between text-gray-400">
                    <span>Valuation:</span>
                    <span className="text-[#D4AF37] font-bold">{confirmedRequest.estimatedQuote}</span>
                  </div>
                  <div className="flex justify-between text-gray-400">
                    <span>Pickup Landmark:</span>
                    <span className="text-white font-medium text-right max-w-[60%] truncate">{confirmedRequest.address}</span>
                  </div>
                  <div className="flex justify-between text-gray-400">
                    <span>Status:</span>
                    <span className="text-emerald-400 font-semibold">{confirmedRequest.status}</span>
                  </div>
                </div>

                <p className="text-[11px] text-gray-400 text-center">
                  Our technician from SK Mobile will call you at <strong>{confirmedRequest.phone}</strong> to confirm the inspection appointment.
                </p>

                <button
                  onClick={() => {
                    setSellSubmitted(false);
                    setConfirmedRequest(null);
                  }}
                  className="outline-gold-btn !text-xs !py-1.5 w-full mt-2"
                >
                  Submit Another Request
                </button>
              </div>
            ) : (
              <form onSubmit={handleSellRequest} className="space-y-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-300 mb-1">
                    Your Name *
                  </label>
                  <input
                    type="text"
                    placeholder="Full Name"
                    value={sellerName}
                    onChange={(e) => {
                      setSellerName(e.target.value);
                      if (errors.sellerName) setErrors((prev) => ({ ...prev, sellerName: null }));
                    }}
                    className={`w-full bg-[#0E0E0E] border rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-[#D4AF37] ${
                      errors.sellerName ? 'border-red-500' : 'border-[#2E2E2E]'
                    }`}
                  />
                  {errors.sellerName && (
                    <p className="text-[11px] text-red-400 mt-1">{errors.sellerName}</p>
                  )}
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-300 mb-1">
                    Contact Phone (10 digits) *
                  </label>
                  <input
                    type="tel"
                    placeholder="Mobile Number"
                    maxLength={10}
                    value={sellerPhone}
                    onChange={(e) => {
                      setSellerPhone(e.target.value);
                      if (errors.sellerPhone) setErrors((prev) => ({ ...prev, sellerPhone: null }));
                    }}
                    className={`w-full bg-[#0E0E0E] border rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-[#D4AF37] ${
                      errors.sellerPhone ? 'border-red-500' : 'border-[#2E2E2E]'
                    }`}
                  />
                  {errors.sellerPhone && (
                    <p className="text-[11px] text-red-400 mt-1">{errors.sellerPhone}</p>
                  )}
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-300 mb-1">
                    Address / Landmark in Udgir *
                  </label>
                  <textarea
                    rows={2}
                    placeholder="e.g. Near Nagar Parishad, Station Road, Udgir"
                    value={sellerAddress}
                    onChange={(e) => {
                      setSellerAddress(e.target.value);
                      if (errors.sellerAddress) setErrors((prev) => ({ ...prev, sellerAddress: null }));
                    }}
                    className={`w-full bg-[#0E0E0E] border rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-[#D4AF37] ${
                      errors.sellerAddress ? 'border-red-500' : 'border-[#2E2E2E]'
                    }`}
                  />
                  {errors.sellerAddress && (
                    <p className="text-[11px] text-red-400 mt-1">{errors.sellerAddress}</p>
                  )}
                </div>

                <button
                  type="submit"
                  disabled={isSubmitting || isSigningIn}
                  className="gold-btn w-full !py-2.5 !text-sm font-bold flex items-center justify-center gap-2"
                >
                  {isSubmitting ? (
                    <>
                      <div className="w-4 h-4 border-2 border-black border-t-transparent rounded-full animate-spin" />
                      <span>Saving Request to Firestore...</span>
                    </>
                  ) : (
                    <>
                      <Database size={16} />
                      <span>Request Device Inspection & Cash</span>
                    </>
                  )}
                </button>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
