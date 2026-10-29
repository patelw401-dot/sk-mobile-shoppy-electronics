import React, { useState, useEffect } from 'react';
import { STORE_INFO } from '../data/storeData';
import { 
  MapPin, 
  Phone, 
  Mail, 
  Clock, 
  Send,
  MessageSquare,
  ShieldCheck,
  AlertTriangle,
  ExternalLink,
  Info,
  CheckCircle2,
  Share2
} from 'lucide-react';

export default function ContactPage({ currentUser, onShowToast }) {
  const [name, setName] = useState(currentUser?.displayName || '');
  const [phone, setPhone] = useState('');
  const [message, setMessage] = useState('');
  const [errors, setErrors] = useState({});
  const [isPreparing, setIsPreparing] = useState(false);
  const [preparedDraft, setPreparedDraft] = useState(null);

  // Sync customer name from auth if signed in
  useEffect(() => {
    if (currentUser?.displayName && !name) {
      setName(currentUser.displayName);
    }
  }, [currentUser]);

  // Clean numerical string for WhatsApp API (e.g. 919823109313)
  const cleanStorePhone = STORE_INFO.phone.replace(/[^0-9]/g, '');

  const validateForm = () => {
    const newErrors = {};
    if (!name.trim() || name.trim().length < 2) {
      newErrors.name = 'Please enter your full name (at least 2 characters).';
    }
    const cleanPhone = phone.trim().replace(/\D/g, '');
    if (!cleanPhone || cleanPhone.length < 10) {
      newErrors.phone = 'Please enter a valid 10-digit mobile number.';
    }
    if (!message.trim() || message.trim().length < 5) {
      newErrors.message = 'Please enter your inquiry details (at least 5 characters).';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const buildWhatsAppUrl = () => {
    const text = `*SK Mobile Shoppy Store Inquiry*\n*Customer Name:* ${name.trim()}\n*Contact Mobile:* ${phone.trim()}\n\n*Inquiry Message:*\n${message.trim()}\n\n_(Sent via SK Mobile Web Portal • Udgir)_`;
    return `https://wa.me/${cleanStorePhone}?text=${encodeURIComponent(text)}`;
  };

  const buildMailtoUrl = () => {
    const subject = `Store Inquiry from ${name.trim()} (${phone.trim()})`;
    const body = `Dear SK Mobile Team,\n\nName: ${name.trim()}\nPhone: ${phone.trim()}\n\nInquiry Details:\n${message.trim()}\n\nStore: Nagar Parishad, Udgir`;
    return `mailto:${STORE_INFO.email}?subject=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!validateForm()) {
      onShowToast?.('Please resolve the highlighted errors before submitting.', 'error');
      return;
    }

    setIsPreparing(true);
    setTimeout(() => {
      setPreparedDraft({
        name: name.trim(),
        phone: phone.trim(),
        message: message.trim(),
        timestamp: new Date().toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' })
      });
      setIsPreparing(false);
      onShowToast?.('Inquiry drafted! Choose WhatsApp, Email, or Phone to deliver directly to the store.', 'info');
    }, 300);
  };

  const handleInstantWhatsApp = () => {
    if (!validateForm()) {
      onShowToast?.('Please fill out Name, Phone, and Inquiry to launch WhatsApp.', 'error');
      return;
    }
    window.open(buildWhatsAppUrl(), '_blank', 'noopener,noreferrer');
    onShowToast?.('Launching WhatsApp with your pre-filled inquiry...', 'success');
  };

  return (
    <div className="container py-8 space-y-10">
      {/* Header */}
      <div className="border-b border-[#222222] pb-6">
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#D4AF37]/15 border border-[#D4AF37]/30 text-xs font-bold text-[#D4AF37] mb-2">
          <MapPin size={13} />
          <span>Official Store Location • Udgir, Maharashtra</span>
        </div>
        <h1 className="text-3xl font-black text-white">Contact & Store Information</h1>
        <p className="text-xs sm:text-sm text-gray-400 mt-1 max-w-2xl">
          Visit our flagship store in Nagar Parishad, Udgir for new smartphone purchases, accessories, certified pre-owned phones, and instant on-counter repairs.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Left Column: Real Store Information */}
        <div className="lg:col-span-6 space-y-6">
          <div className="bg-[#141414] border border-[#2E2E2E] rounded-2xl p-6 sm:p-8 space-y-6">
            <h2 className="text-lg font-bold text-white flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-[#D4AF37]"></span>
              <span>Shop Address & Store Details</span>
            </h2>

            {/* Address */}
            <div className="flex items-start gap-3.5 p-3.5 rounded-xl bg-[#0E0E0E] border border-[#222222]">
              <div className="p-2 rounded-lg bg-[#D4AF37]/10 text-[#D4AF37] shrink-0 mt-0.5">
                <MapPin size={20} />
              </div>
              <div>
                <h4 className="text-xs font-bold text-gray-400 uppercase tracking-wider mb-1">
                  Physical Store Location
                </h4>
                <p className="text-sm font-bold text-white">{STORE_INFO.name}</p>
                <p className="text-xs text-gray-300 mt-0.5">{STORE_INFO.address}</p>
                <p className="text-[11px] text-[#D4AF37] font-semibold mt-1">
                  Landmark: {STORE_INFO.landmark}
                </p>
              </div>
            </div>

            {/* Timings */}
            <div className="flex items-start gap-3.5 p-3.5 rounded-xl bg-[#0E0E0E] border border-[#222222]">
              <div className="p-2 rounded-lg bg-emerald-500/10 text-emerald-400 shrink-0 mt-0.5">
                <Clock size={20} />
              </div>
              <div>
                <h4 className="text-xs font-bold text-gray-400 uppercase tracking-wider mb-1">
                  Working Hours
                </h4>
                <p className="text-sm font-bold text-white">{STORE_INFO.hours}</p>
                <p className="text-xs text-gray-400 mt-0.5">
                  Counter repair & sales open on all Sundays and public holidays.
                </p>
              </div>
            </div>

            {/* Helpline Phone */}
            <div className="flex items-start gap-3.5 p-3.5 rounded-xl bg-[#0E0E0E] border border-[#222222]">
              <div className="p-2 rounded-lg bg-blue-500/10 text-blue-400 shrink-0 mt-0.5">
                <Phone size={20} />
              </div>
              <div>
                <h4 className="text-xs font-bold text-gray-400 uppercase tracking-wider mb-1">
                  Helpline & WhatsApp Support
                </h4>
                <a 
                  href={`tel:${STORE_INFO.phone}`} 
                  className="text-sm font-bold text-white hover:text-[#D4AF37] transition inline-block"
                >
                  {STORE_INFO.displayPhone}
                </a>
                <p className="text-xs text-gray-400 mt-0.5">
                  Direct call for instant stock availability, repair status & inquiries.
                </p>
              </div>
            </div>

            {/* Email */}
            <div className="flex items-start gap-3.5 p-3.5 rounded-xl bg-[#0E0E0E] border border-[#222222]">
              <div className="p-2 rounded-lg bg-purple-500/10 text-purple-400 shrink-0 mt-0.5">
                <Mail size={20} />
              </div>
              <div>
                <h4 className="text-xs font-bold text-gray-400 uppercase tracking-wider mb-1">
                  Official Email Address
                </h4>
                <a 
                  href={`mailto:${STORE_INFO.email}`} 
                  className="text-xs font-semibold text-gray-300 hover:text-[#D4AF37] break-all"
                >
                  {STORE_INFO.email}
                </a>
              </div>
            </div>
          </div>
        </div>

        {/* Right Column: Send Inquiry Message */}
        <div className="lg:col-span-6 bg-[#141414] border border-[#2E2E2E] rounded-2xl p-6 sm:p-8">
          <div className="flex items-center justify-between mb-2">
            <h2 className="text-lg font-bold text-white flex items-center gap-2">
              <MessageSquare size={20} className="text-[#D4AF37]" />
              <span>Direct Store Inquiry</span>
            </h2>
            <span className="px-2.5 py-0.5 rounded-full bg-[#1F1F1F] text-gray-400 border border-[#333333] text-[10px] font-semibold">
              Live Direct Channels
            </span>
          </div>

          <p className="text-xs text-gray-400 mb-5">
            Need details about smartphone stock, express repair estimates, or accessories? Prepare your inquiry below and dispatch directly to our Udgir store team.
          </p>

          {/* Transparent Notice on Database Persistence */}
          <div className="mb-5 p-3 rounded-xl bg-[#171408] border border-[#D4AF37]/30 text-xs flex items-start gap-2.5 text-gray-300">
            <Info size={16} className="text-[#D4AF37] shrink-0 mt-0.5" />
            <div className="space-y-1">
              <p className="font-semibold text-white">Direct Dispatch Mode</p>
              <p className="text-[11px] text-gray-400 leading-relaxed">
                Cloud inbox database persistence for general inquiries is not active in store security rules. Inquiries are prepared on-device and forwarded directly via WhatsApp, Phone, or Email to prevent dropped messages.
              </p>
            </div>
          </div>

          {preparedDraft ? (
            <div className="p-6 rounded-xl bg-[#0E0E0E] border border-[#D4AF37]/50 space-y-4">
              <div className="w-12 h-12 rounded-full bg-[#D4AF37]/20 text-[#D4AF37] flex items-center justify-center mx-auto">
                <CheckCircle2 size={28} />
              </div>

              <div className="text-center">
                <h3 className="text-base font-bold text-white">Inquiry Draft Prepared!</h3>
                <p className="text-xs text-gray-400 mt-1">
                  Ready to send directly to SK Mobile Shoppy & Electronic
                </p>
              </div>

              <div className="p-3.5 rounded-lg bg-[#141414] border border-[#262626] text-xs space-y-2">
                <div className="flex justify-between text-gray-400">
                  <span>Customer:</span>
                  <span className="text-white font-medium">{preparedDraft.name}</span>
                </div>
                <div className="flex justify-between text-gray-400">
                  <span>Contact Mobile:</span>
                  <span className="text-white font-medium">{preparedDraft.phone}</span>
                </div>
                <div className="pt-2 border-t border-[#222222]">
                  <span className="text-gray-400 block mb-1">Inquiry Details:</span>
                  <p className="text-gray-200 bg-[#0A0A0A] p-2.5 rounded border border-[#1E1E1E] whitespace-pre-wrap text-[11px] leading-relaxed">
                    {preparedDraft.message}
                  </p>
                </div>
              </div>

              <p className="text-xs text-gray-400 text-center">
                Click below to deliver this message immediately to the store:
              </p>

              <div className="space-y-2.5 pt-1">
                <a
                  href={buildWhatsAppUrl()}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="w-full py-2.5 px-4 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs flex items-center justify-center gap-2 transition shadow-lg shadow-emerald-950/40"
                >
                  <Share2 size={15} />
                  <span>Forward via WhatsApp ({STORE_INFO.displayPhone})</span>
                  <ExternalLink size={13} />
                </a>

                <a
                  href={buildMailtoUrl()}
                  className="w-full py-2.5 px-4 rounded-lg bg-[#1F1F1F] hover:bg-[#2A2A2A] text-white font-medium text-xs flex items-center justify-center gap-2 border border-[#333333] transition"
                >
                  <Mail size={15} />
                  <span>Send via Email to Store Staff</span>
                </a>

                <a
                  href={`tel:${STORE_INFO.phone}`}
                  className="w-full py-2.5 px-4 rounded-lg bg-[#1F1F1F] hover:bg-[#2A2A2A] text-[#D4AF37] font-medium text-xs flex items-center justify-center gap-2 border border-[#333333] transition"
                >
                  <Phone size={15} />
                  <span>Call Store Helpline Directly</span>
                </a>
              </div>

              <button
                onClick={() => setPreparedDraft(null)}
                className="outline-gold-btn !text-xs !py-1.5 w-full mt-2"
              >
                Edit Message / Draft Another Inquiry
              </button>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                  Your Full Name *
                </label>
                <input
                  type="text"
                  placeholder="e.g. Anand Kulkarni"
                  value={name}
                  onChange={(e) => {
                    setName(e.target.value);
                    if (errors.name) setErrors((prev) => ({ ...prev, name: null }));
                  }}
                  className={`w-full bg-[#0E0E0E] border rounded-lg px-3.5 py-2.5 text-sm text-white focus:outline-none focus:border-[#D4AF37] ${
                    errors.name ? 'border-red-500' : 'border-[#2E2E2E]'
                  }`}
                />
                {errors.name && (
                  <p className="text-[11px] text-red-400 mt-1">{errors.name}</p>
                )}
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                  Mobile Number (10 digits) *
                </label>
                <input
                  type="tel"
                  placeholder="e.g. 9823109313"
                  maxLength={10}
                  value={phone}
                  onChange={(e) => {
                    setPhone(e.target.value);
                    if (errors.phone) setErrors((prev) => ({ ...prev, phone: null }));
                  }}
                  className={`w-full bg-[#0E0E0E] border rounded-lg px-3.5 py-2.5 text-sm text-white focus:outline-none focus:border-[#D4AF37] ${
                    errors.phone ? 'border-red-500' : 'border-[#2E2E2E]'
                  }`}
                />
                {errors.phone && (
                  <p className="text-[11px] text-red-400 mt-1">{errors.phone}</p>
                )}
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                  Inquiry or Message *
                </label>
                <textarea
                  rows={4}
                  placeholder="Tell us about the phone model you are searching for, repair query, or accessories..."
                  value={message}
                  onChange={(e) => {
                    setMessage(e.target.value);
                    if (errors.message) setErrors((prev) => ({ ...prev, message: null }));
                  }}
                  className={`w-full bg-[#0E0E0E] border rounded-lg px-3.5 py-2.5 text-sm text-white focus:outline-none focus:border-[#D4AF37] ${
                    errors.message ? 'border-red-500' : 'border-[#2E2E2E]'
                  }`}
                />
                {errors.message && (
                  <p className="text-[11px] text-red-400 mt-1">{errors.message}</p>
                )}
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-2">
                <button
                  type="button"
                  onClick={handleInstantWhatsApp}
                  className="py-2.5 px-4 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs flex items-center justify-center gap-2 transition"
                >
                  <Share2 size={15} />
                  <span>Direct WhatsApp</span>
                </button>

                <button
                  type="submit"
                  disabled={isPreparing}
                  className="gold-btn !py-2.5 !text-xs !font-bold flex items-center justify-center gap-2"
                >
                  {isPreparing ? (
                    <span>Preparing Draft...</span>
                  ) : (
                    <>
                      <Send size={15} />
                      <span>Prepare Inquiry</span>
                    </>
                  )}
                </button>
              </div>
            </form>
          )}

          {/* Quick Call Button */}
          <div className="mt-8 pt-6 border-t border-[#222222] flex items-center justify-between">
            <span className="text-xs text-gray-400">Need urgent assistance right now?</span>
            <a
              href={`tel:${STORE_INFO.phone}`}
              className="outline-gold-btn !text-xs !py-2 !px-4"
            >
              <Phone size={14} />
              <span>Call Now</span>
            </a>
          </div>
        </div>
      </div>
    </div>
  );
}
