import React, { useState } from 'react';
import { RefreshCw, ArrowRight, X, Sparkles } from 'lucide-react';

export default function ExchangeComingSoonCard({ onShowToast, className = '' }) {
  const [isModalOpen, setIsModalOpen] = useState(false);

  const handleOpen = () => {
    setIsModalOpen(true);
    onShowToast?.('New & Old Mobile Exchange is Coming Soon at SK Mobile, Udgir!', 'info');
  };

  return (
    <>
      <div
        role="button"
        tabIndex={0}
        onClick={handleOpen}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            handleOpen();
          }
        }}
        className={`group cursor-pointer rounded-2xl p-4 sm:p-5 bg-gradient-to-r from-[#1F1909] via-[#151515] to-[#121212] border border-[#D4AF37]/50 hover:border-[#D4AF37] transition shadow-lg hover:shadow-[#D4AF37]/10 flex items-center justify-between gap-4 ${className}`}
      >
        <div className="flex items-center gap-3.5 sm:gap-4 min-w-0">
          <div className="w-12 h-12 rounded-full bg-[#D4AF37]/15 border border-[#D4AF37]/40 text-[#D4AF37] flex items-center justify-center shrink-0">
            <RefreshCw size={22} />
          </div>
          <div className="min-w-0">
            <div className="flex flex-wrap items-center gap-2 mb-1">
              <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-[#D4AF37] text-black text-[10px] font-black uppercase tracking-wider">
                <Sparkles size={10} />
                Coming Soon
              </span>
              <h3 className="text-sm sm:text-base font-bold text-white group-hover:text-[#D4AF37] transition">
                New &amp; Old Mobile Exchange
              </h3>
            </div>
            <p className="text-xs text-gray-300 leading-relaxed">
              Upgrade your old smartphone toward a brand-new 5G phone or certified pre-owned device — launching soon at SK Mobile Shoppy and Electronics, Udgir.
            </p>
          </div>
        </div>

        <div className="p-2 rounded-lg bg-[#1F1F1F] border border-[#333333] text-[#D4AF37] group-hover:bg-[#D4AF37] group-hover:text-black transition shrink-0">
          <ArrowRight size={16} />
        </div>
      </div>

      {isModalOpen && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm"
          onClick={() => setIsModalOpen(false)}
        >
          <div
            className="bg-[#141414] border border-[#D4AF37]/50 rounded-2xl max-w-md w-full p-6 shadow-2xl space-y-4 text-center relative"
            onClick={(e) => e.stopPropagation()}
          >
            <button
              type="button"
              onClick={() => setIsModalOpen(false)}
              className="absolute top-4 right-4 text-gray-400 hover:text-white p-1"
              aria-label="Close modal"
            >
              <X size={18} />
            </button>

            <div className="w-14 h-14 rounded-full bg-[#D4AF37]/15 border border-[#D4AF37]/40 text-[#D4AF37] flex items-center justify-center mx-auto">
              <RefreshCw size={28} />
            </div>

            <div>
              <span className="inline-block px-3 py-0.5 rounded-full bg-[#D4AF37] text-black text-[10px] font-black uppercase tracking-wider mb-2">
                Coming Soon
              </span>
              <h3 className="text-lg font-black text-white">
                New &amp; Old Mobile Exchange
              </h3>
            </div>

            <p className="text-xs sm:text-sm text-gray-300 leading-relaxed">
              We are launching direct <strong>New Mobile &amp; Old Mobile Exchange</strong> soon at SK Mobile Shoppy and Electronics, Nagar Parishad, Udgir!
            </p>
            <p className="text-xs text-gray-400 leading-relaxed">
              Soon you&apos;ll be able to trade in your old phone for instant upgrade credit toward any brand-new 5G phone or certified pre-owned handset.
            </p>

            <button
              type="button"
              onClick={() => setIsModalOpen(false)}
              className="gold-btn w-full !py-2.5 !text-sm font-bold"
            >
              OK, Got It
            </button>
          </div>
        </div>
      )}
    </>
  );
}
