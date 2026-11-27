import React from 'react';
import { DEFAULT_SHOP_INFO, formatShopInfo } from '../firebase';
import { 
  MapPin, 
  Phone, 
  Mail, 
  Clock, 
  ShieldCheck, 
  Wrench, 
  Award, 
  CheckCircle2,
  ChevronRight,
  Lock,
  MessageCircle,
  ExternalLink
} from 'lucide-react';

export default function Footer({ setActivePage, shopInfo = DEFAULT_SHOP_INFO }) {
  const info = formatShopInfo(shopInfo);
  const handleNav = (page) => {
    setActivePage(page);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <footer className="bg-[#0C0C0C] border-t border-[#262626] text-gray-300 pt-12 pb-8">
      <div className="container">
        {/* Trust Badges Bar */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 pb-10 border-b border-[#222222]">
          <div className="flex items-center gap-3 p-3 rounded-xl bg-[#141414] border border-[#222222]">
            <div className="p-2.5 rounded-lg bg-[#D4AF37]/10 text-[#D4AF37]">
              <ShieldCheck size={22} />
            </div>
            <div>
              <h4 className="text-sm font-bold text-white">Quality Guarantee</h4>
              <p className="text-[11px] text-gray-400">Tested & certified repairs</p>
            </div>
          </div>

          <div className="flex items-center gap-3 p-3 rounded-xl bg-[#141414] border border-[#222222]">
            <div className="p-2.5 rounded-lg bg-emerald-500/10 text-emerald-400">
              <Award size={22} />
            </div>
            <div>
              <h4 className="text-sm font-bold text-white">100% Genuine Parts</h4>
              <p className="text-[11px] text-gray-400">OEM Grade replacement</p>
            </div>
          </div>

          <div className="flex items-center gap-3 p-3 rounded-xl bg-[#141414] border border-[#222222]">
            <div className="p-2.5 rounded-lg bg-blue-500/10 text-blue-400">
              <Wrench size={22} />
            </div>
            <div>
              <h4 className="text-sm font-bold text-white">Express 30-Min Fix</h4>
              <p className="text-[11px] text-gray-400">Doorstep & shop service</p>
            </div>
          </div>

          <div className="flex items-center gap-3 p-3 rounded-xl bg-[#141414] border border-[#222222]">
            <div className="p-2.5 rounded-lg bg-purple-500/10 text-purple-400">
              <CheckCircle2 size={22} />
            </div>
            <div>
              <h4 className="text-sm font-bold text-white">32-Point Inspection</h4>
              <p className="text-[11px] text-gray-400">Certified pre-owned phones</p>
            </div>
          </div>
        </div>

        {/* Main Footer Links */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8 py-10">
          {/* Column 1: Store Brand & Info */}
          <div>
            <div className="flex items-center gap-2 mb-3">
              <div className="w-8 h-8 rounded-lg bg-[#D4AF37] flex items-center justify-center font-bold text-black text-sm">
                SK
              </div>
              <span className="font-extrabold text-white text-base">{info.shopName}</span>
            </div>
            <p className="text-xs text-gray-400 leading-relaxed mb-4">
              {info.shortLocationLabel}'s trusted smartphone showroom and device repair service center. Genuine devices, verified pre-owned phones, and reliable customer care.
            </p>
            <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-md bg-[#1C1C1C] border border-[#333333] text-xs text-[#D4AF37]">
              <span>{info.landmark || info.shortLocationLabel}</span>
            </div>
          </div>

          {/* Column 2: Quick Links */}
          <div>
            <h4 className="text-sm font-bold text-white uppercase tracking-wider mb-4 flex items-center gap-1.5">
              <span className="w-1.5 h-1.5 rounded-full bg-[#D4AF37]"></span>
              Quick Navigation
            </h4>
            <ul className="space-y-2 text-xs">
              <li>
                <button onClick={() => handleNav('products')} className="hover:text-[#D4AF37] transition flex items-center gap-1">
                  <ChevronRight size={12} className="text-[#D4AF37]" />
                  Latest Smartphones
                </button>
              </li>
              <li>
                <button onClick={() => handleNav('old-phones')} className="hover:text-[#D4AF37] transition flex items-center gap-1">
                  <ChevronRight size={12} className="text-[#D4AF37]" />
                  Certified Pre-Owned Phones
                </button>
              </li>
              <li>
                <button onClick={() => handleNav('repairs')} className="hover:text-[#D4AF37] transition flex items-center gap-1">
                  <ChevronRight size={12} className="text-[#D4AF37]" />
                  Mobile Repair Booking (Udgir)
                </button>
              </li>
              <li>
                <button onClick={() => handleNav('accessories')} className="hover:text-[#D4AF37] transition flex items-center gap-1">
                  <ChevronRight size={12} className="text-[#D4AF37]" />
                  Fast Chargers & Data Cables
                </button>
              </li>
              <li>
                <button onClick={() => handleNav('cart')} className="hover:text-[#D4AF37] transition flex items-center gap-1">
                  <ChevronRight size={12} className="text-[#D4AF37]" />
                  Shopping Cart & Checkout
                </button>
              </li>
              <li>
                <button onClick={() => handleNav('my-orders')} className="hover:text-[#D4AF37] transition flex items-center gap-1">
                  <ChevronRight size={12} className="text-[#D4AF37]" />
                  Track Orders (My Orders)
                </button>
              </li>
              <li>
                <button onClick={() => handleNav('app-download')} className="hover:text-[#D4AF37] transition flex items-center gap-1 font-semibold text-amber-300">
                  <ChevronRight size={12} className="text-[#D4AF37]" />
                  Download Android App (APK)
                </button>
              </li>
              <li>
                <button onClick={() => handleNav('contact')} className="hover:text-[#D4AF37] transition flex items-center gap-1">
                  <ChevronRight size={12} className="text-[#D4AF37]" />
                  Store Location & Directions
                </button>
              </li>
              <li className="pt-1 border-t border-[#1C1C1C]">
                <button onClick={() => handleNav('admin')} className="text-amber-400 hover:text-[#D4AF37] transition flex items-center gap-1 font-semibold">
                  <ShieldCheck size={12} className="text-[#D4AF37]" />
                  Store Admin Portal
                </button>
              </li>
            </ul>
          </div>

          {/* Column 3: Business Services */}
          <div>
            <h4 className="text-sm font-bold text-white uppercase tracking-wider mb-4 flex items-center gap-1.5">
              <span className="w-1.5 h-1.5 rounded-full bg-blue-500"></span>
              Our Services
            </h4>
            <ul className="space-y-2 text-xs text-gray-400">
              {(info.services || []).map((svc, idx) => (
                <li key={idx}>• {svc}</li>
              ))}
            </ul>
          </div>

          {/* Column 4: Contact & Hours */}
          <div>
            <h4 className="text-sm font-bold text-white uppercase tracking-wider mb-4 flex items-center gap-1.5">
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
              Store Details
            </h4>
            <div className="space-y-3 text-xs">
              <a
                href={info.googleMapsUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-start gap-2.5 hover:text-[#D4AF37] transition"
              >
                <MapPin size={16} className="text-[#D4AF37] shrink-0 mt-0.5" />
                <span>{info.fullAddress}</span>
              </a>
              <div className="flex items-center gap-2.5">
                <Clock size={16} className="text-[#D4AF37] shrink-0" />
                <span>{info.openingHours}</span>
              </div>
              <div className="flex items-center gap-2.5">
                <Phone size={16} className="text-[#D4AF37] shrink-0" />
                <a href={`tel:${info.dialPhone}`} className="hover:text-[#D4AF37] font-semibold">
                  {info.phoneNumber}
                </a>
              </div>
              <div className="flex items-center gap-2.5">
                <MessageCircle size={16} className="text-emerald-400 shrink-0" />
                <a
                  href={`https://wa.me/${info.whatsappDigits}`}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="hover:text-emerald-300 font-semibold"
                >
                  WhatsApp: {info.whatsappNumber}
                </a>
              </div>
              <div className="flex items-center gap-2.5">
                <Mail size={16} className="text-[#D4AF37] shrink-0" />
                <a href={`mailto:${info.email}`} className="hover:text-[#D4AF37] break-all">
                  {info.email}
                </a>
              </div>
              <div className="pt-1 flex flex-wrap gap-2">
                <a
                  href={`tel:${info.dialPhone}`}
                  className="px-3 py-1.5 rounded-lg bg-[#D4AF37] text-black font-bold text-[11px] inline-flex items-center gap-1"
                >
                  <Phone size={12} />
                  <span>Call Shop</span>
                </a>
                <a
                  href={`https://wa.me/${info.whatsappDigits}`}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="px-3 py-1.5 rounded-lg bg-emerald-600 text-white font-bold text-[11px] inline-flex items-center gap-1"
                >
                  <MessageCircle size={12} />
                  <span>WhatsApp</span>
                </a>
                <a
                  href={info.googleMapsUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="px-3 py-1.5 rounded-lg bg-[#1A1A1A] border border-[#333333] text-gray-200 hover:border-[#D4AF37] font-semibold text-[11px] inline-flex items-center gap-1"
                >
                  <ExternalLink size={12} />
                  <span>Maps</span>
                </a>
              </div>
            </div>
          </div>
        </div>

        {/* Bottom Copyright & Admin Access */}
        <div className="pt-6 border-t border-[#1C1C1C] flex flex-col sm:flex-row items-center justify-between text-[11px] text-gray-500 gap-3">
          <p>© {new Date().getFullYear()} {info.shopName} • All Rights Reserved.</p>
          <div className="flex items-center gap-3">
            <button
              onClick={() => handleNav('admin')}
              className="text-gray-400 hover:text-[#D4AF37] transition flex items-center gap-1 font-semibold"
            >
              <Lock size={11} className="text-[#D4AF37]" />
              <span>Admin Portal</span>
            </button>
            <span className="text-gray-600">|</span>
            <p className="text-gray-400">
              {info.shortLocationLabel}
            </p>
          </div>
        </div>
      </div>
    </footer>
  );
}
