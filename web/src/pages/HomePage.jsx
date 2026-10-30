import React from 'react';
import { STORE_INFO, REAL_PRODUCTS, PRE_OWNED_PHONES, REPAIR_SERVICES } from '../data/storeData';
import ProductCard from '../components/ProductCard';
import { 
  Smartphone, 
  Wrench, 
  ShieldCheck, 
  RefreshCw, 
  Clock, 
  MapPin, 
  Phone, 
  CheckCircle2, 
  Sparkles,
  ArrowRight,
  Headphones
} from 'lucide-react';

export default function HomePage({ setActivePage, setSelectedProduct, onAddToCart, onBuyNow }) {
  const featuredPhones = REAL_PRODUCTS.slice(0, 4);
  const featuredOldPhones = PRE_OWNED_PHONES.slice(0, 3);

  return (
    <div className="space-y-16 pb-16">
      {/* Hero Banner Section */}
      <section className="relative overflow-hidden pt-12 pb-16 md:pt-20 md:pb-24 border-b border-[#222222] bg-gradient-to-b from-[#141414] via-[#0D0D0D] to-[#0A0A0A]">
        {/* Decorative Gold Radial Glow */}
        <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[550px] h-[350px] bg-[#D4AF37]/10 rounded-full blur-[120px] pointer-events-none"></div>

        <div className="container relative z-10 text-center max-w-4xl mx-auto">
          {/* Badge */}
          <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-[#D4AF37]/10 border border-[#D4AF37]/30 text-xs font-bold text-[#D4AF37] mb-6 animate-pulse">
            <Sparkles size={14} />
            <span>Nagar Parishad, Udgir Store • Certified Service Center</span>
          </div>

          <h1 className="text-3xl sm:text-5xl md:text-6xl font-black tracking-tight text-white mb-6 leading-tight">
            Premium Tech, Certified Repairs & <span className="gold-text">Best Mobile Deals</span> in Udgir
          </h1>

          <p className="text-gray-300 text-sm sm:text-base md:text-lg mb-8 max-w-2xl mx-auto leading-relaxed">
            Welcome to <strong>SK Mobile Shoppy and Electronic</strong>. We offer brand-new smartphones, tested pre-owned devices, original accessories, and expert 30-minute mobile repairs with a 90-day warranty.
          </p>

          {/* Action CTAs */}
          <div className="flex flex-wrap items-center justify-center gap-4">
            <button
              onClick={() => setActivePage('repairs')}
              className="gold-btn !text-sm !py-3 !px-6 shadow-lg shadow-[#D4AF37]/20"
            >
              <Wrench size={18} />
              <span>Book Mobile Repair Now</span>
            </button>

            <button
              onClick={() => setActivePage('products')}
              className="outline-gold-btn !text-sm !py-3 !px-6"
            >
              <Smartphone size={18} />
              <span>Explore Smartphones</span>
            </button>

            <button
              onClick={() => setActivePage('old-phones')}
              className="px-5 py-3 rounded-lg text-sm font-semibold bg-[#1F1F1F] text-gray-200 border border-[#333333] hover:border-gray-400 hover:text-white transition flex items-center gap-2"
            >
              <RefreshCw size={16} />
              <span>Buy / Sell Old Phone</span>
            </button>
          </div>

          {/* Store Quick Telemetry */}
          <div className="mt-12 pt-8 border-t border-[#222222] grid grid-cols-2 md:grid-cols-4 gap-4 text-left">
            <div className="p-3 rounded-xl bg-[#141414]/80 border border-[#222222]">
              <div className="text-xs text-gray-400 mb-1">Store Address</div>
              <div className="text-xs font-bold text-gray-200">Nagar Parishad, Udgir</div>
              <div className="text-[10px] text-gray-500">Opposite Municipal Complex</div>
            </div>
            <div className="p-3 rounded-xl bg-[#141414]/80 border border-[#222222]">
              <div className="text-xs text-gray-400 mb-1">Service Warranty</div>
              <div className="text-xs font-bold text-[#D4AF37]">90-Day Full Warranty</div>
              <div className="text-[10px] text-gray-500">On displays, batteries & ports</div>
            </div>
            <div className="p-3 rounded-xl bg-[#141414]/80 border border-[#222222]">
              <div className="text-xs text-gray-400 mb-1">Pre-Owned Quality</div>
              <div className="text-xs font-bold text-emerald-400">32-Point Inspected</div>
              <div className="text-[10px] text-gray-500">With 6-month shop warranty</div>
            </div>
            <div className="p-3 rounded-xl bg-[#141414]/80 border border-[#222222]">
              <div className="text-xs text-gray-400 mb-1">Store Helpline</div>
              <div className="text-xs font-bold text-white">{STORE_INFO.phone}</div>
              <div className="text-[10px] text-gray-500">Open all 7 days (9:30 AM–9:30 PM)</div>
            </div>
          </div>
        </div>
      </section>

      {/* Featured Smartphones Section */}
      <section className="container">
        <div className="flex flex-col sm:flex-row items-start sm:items-end justify-between mb-8 gap-4">
          <div>
            <div className="text-xs font-bold uppercase tracking-widest text-[#D4AF37] mb-1">
              Brand New In Stock
            </div>
            <h2 className="text-2xl sm:text-3xl font-black text-white">
              Trending Smartphones
            </h2>
            <p className="text-xs sm:text-sm text-gray-400 mt-1">
              Genuine Indian warranty handsets available for instant pickup or same-day Udgir delivery.
            </p>
          </div>
          <button
            onClick={() => setActivePage('products')}
            className="outline-gold-btn !text-xs !py-2 !px-4 self-start sm:self-auto"
          >
            <span>View All Smartphones</span>
            <ArrowRight size={14} />
          </button>
        </div>

        <div className="grid-cards">
          {featuredPhones.map((phone) => (
            <ProductCard
              key={phone.id}
              product={phone}
              onAddToCart={onAddToCart}
              onBuyNow={onBuyNow}
              onAction={() => setActivePage('products')}
            />
          ))}
        </div>
      </section>

      {/* Mobile Repair Booking Spotlight */}
      <section className="container">
        <div className="rounded-2xl p-6 sm:p-10 bg-gradient-to-br from-[#1A1810] via-[#141414] to-[#0D0D0D] border border-[#D4AF37]/30 shadow-xl relative overflow-hidden">
          <div className="max-w-2xl">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#D4AF37]/15 border border-[#D4AF37]/40 text-xs font-bold text-[#D4AF37] mb-4">
              <Wrench size={14} />
              <span>Express Repair Service in Udgir</span>
            </div>
            <h2 className="text-2xl sm:text-4xl font-black text-white mb-4">
              Broken Screen or Draining Battery? <br />
              <span className="gold-text">Fix It in 30 Minutes</span>
            </h2>
            <p className="text-xs sm:text-sm text-gray-300 leading-relaxed mb-6">
              Skip the long waiting times at external service centers. Our certified technicians in Nagar Parishad, Udgir use genuine parts with a 90-day replacement warranty. Doorstep pickup or fast 30-min counter service available.
            </p>

            <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 mb-8">
              {REPAIR_SERVICES.slice(0, 6).map((service) => (
                <div key={service.id} className="p-3 rounded-lg bg-[#0E0E0E]/80 border border-[#262626]">
                  <div className="text-xs font-bold text-white mb-0.5">{service.title}</div>
                  <div className="text-[11px] text-[#D4AF37] font-semibold">{service.priceRange}</div>
                </div>
              ))}
            </div>

            <div className="flex flex-wrap gap-4 items-center">
              <button
                onClick={() => setActivePage('repairs')}
                className="gold-btn !text-sm !py-2.5 !px-6"
              >
                <Wrench size={16} />
                <span>Book Repair Appointment</span>
              </button>
              <a
                href={`tel:${STORE_INFO.phone}`}
                className="outline-gold-btn !text-sm !py-2.5 !px-5"
              >
                <Phone size={16} />
                <span>Call Helpline: {STORE_INFO.displayPhone}</span>
              </a>
            </div>
          </div>
        </div>
      </section>

      {/* Certified Pre-Owned Phones Section */}
      <section className="container">
        <div className="flex flex-col sm:flex-row items-start sm:items-end justify-between mb-8 gap-4">
          <div>
            <div className="text-xs font-bold uppercase tracking-widest text-emerald-400 mb-1">
              Budget Flagships with Warranty
            </div>
            <h2 className="text-2xl sm:text-3xl font-black text-white">
              Certified Pre-Owned Phones
            </h2>
            <p className="text-xs sm:text-sm text-gray-400 mt-1">
              Inspected with 32-point hardware check. 100% original parts with 6-month shop warranty.
            </p>
          </div>
          <button
            onClick={() => setActivePage('old-phones')}
            className="outline-gold-btn !text-xs !py-2 !px-4 self-start sm:self-auto"
          >
            <span>Explore Pre-Owned Handsets</span>
            <ArrowRight size={14} />
          </button>
        </div>

        <div className="grid-cards">
          {featuredOldPhones.map((phone) => (
            <ProductCard
              key={phone.id}
              product={phone}
              onAction={() => setActivePage('old-phones')}
            />
          ))}
        </div>
      </section>

      {/* Accessories Quick Preview */}
      <section className="container">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h3 className="text-xl font-bold text-white">Fast Chargers & Audio Accessories</h3>
            <p className="text-xs text-gray-400">High-efficiency GaN adapters, tough braided cables & wireless neckbands</p>
          </div>
          <button
            onClick={() => setActivePage('accessories')}
            className="text-xs text-[#D4AF37] hover:underline font-semibold flex items-center gap-1"
          >
            <span>View All</span>
            <ArrowRight size={12} />
          </button>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          <div className="p-4 rounded-xl bg-[#141414] border border-[#222222] flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="p-2.5 rounded-lg bg-[#D4AF37]/10 text-[#D4AF37]">
                <Smartphone size={20} />
              </div>
              <div>
                <h4 className="text-sm font-bold text-white">65W GaN Dual Port Fast Charger</h4>
                <p className="text-[11px] text-gray-400">Supports Type-C + USB-A • ₹1,299</p>
              </div>
            </div>
            <button 
              onClick={() => setActivePage('accessories')}
              className="text-xs font-bold text-[#D4AF37] hover:underline"
            >
              View
            </button>
          </div>

          <div className="p-4 rounded-xl bg-[#141414] border border-[#222222] flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="p-2.5 rounded-lg bg-emerald-500/10 text-emerald-400">
                <CheckCircle2 size={20} />
              </div>
              <div>
                <h4 className="text-sm font-bold text-white">100W Ballistic Nylon Type-C Cable</h4>
                <p className="text-[11px] text-gray-400">30,000+ Bends Tested • ₹349</p>
              </div>
            </div>
            <button 
              onClick={() => setActivePage('accessories')}
              className="text-xs font-bold text-[#D4AF37] hover:underline"
            >
              View
            </button>
          </div>

          <div className="p-4 rounded-xl bg-[#141414] border border-[#222222] flex items-center justify-between sm:col-span-2 lg:col-span-1">
            <div className="flex items-center gap-3">
              <div className="p-2.5 rounded-lg bg-blue-500/10 text-blue-400">
                <Headphones size={20} />
              </div>
              <div>
                <h4 className="text-sm font-bold text-white">Wireless Bluetooth Neckbands</h4>
                <p className="text-[11px] text-gray-400">Deep bass, 30h battery • From ₹1,699</p>
              </div>
            </div>
            <button 
              onClick={() => setActivePage('accessories')}
              className="text-xs font-bold text-[#D4AF37] hover:underline"
            >
              View
            </button>
          </div>
        </div>
      </section>
    </div>
  );
}
