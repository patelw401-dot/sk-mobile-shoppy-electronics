import React, { useState } from 'react';
import { APP_DOWNLOAD_CONFIG, STORE_INFO } from '../data/storeData';
import { 
  Download, 
  Smartphone, 
  ShieldCheck, 
  CheckCircle2, 
  AlertCircle, 
  X, 
  Info, 
  ExternalLink, 
  Phone, 
  Sparkles, 
  ArrowRight,
  Layers,
  HelpCircle,
  Clock,
  Cpu,
  FileCheck
} from 'lucide-react';

// Official Android robot SVG icon
function AndroidIcon({ className = "w-5 h-5", size = 20 }) {
  return (
    <svg 
      width={size} 
      height={size} 
      viewBox="0 0 24 24" 
      fill="currentColor" 
      className={className}
      aria-hidden="true"
    >
      <path d="M17.523 15.3414c-.5511 0-.9993-.4486-.9993-1.0003s.4482-1.0003.9993-1.0003c.5516 0 .9998.4486.9998 1.0003s-.4482 1.0003-.9998 1.0003m-11.046 0c-.5511 0-.9993-.4486-.9993-1.0003s.4482-1.0003.9993-1.0003c.5516 0 .9998.4486.9998 1.0003s-.4482 1.0003-.9998 1.0003m11.4045-6.02l1.9973-3.4592a.416.416 0 00-.1521-.5676.416.416 0 00-.5676.1521l-2.0223 3.503C15.5902 8.4116 13.8533 8.125 12 8.125c-1.8533 0-3.5902.2866-5.1328.8247L4.8449 5.4467a.416.416 0 00-.5676-.1521.416.416 0 00-.1521.5676l1.9973 3.4592C2.6889 11.1867.3438 14.6589 0 18.75h24c-.3438-4.0911-2.6889-7.5633-6.1185-9.4286" />
    </svg>
  );
}

export default function AppDownloadSection({ onShowToast }) {
  const [showConfigModal, setShowConfigModal] = useState(false);
  const [showGuideModal, setShowGuideModal] = useState(false);

  const hasRealUrl = Boolean(
    APP_DOWNLOAD_CONFIG.downloadUrl && 
    APP_DOWNLOAD_CONFIG.downloadUrl.trim().length > 0 &&
    (APP_DOWNLOAD_CONFIG.downloadUrl.startsWith('http://') || 
     APP_DOWNLOAD_CONFIG.downloadUrl.startsWith('https://') || 
     APP_DOWNLOAD_CONFIG.downloadUrl.startsWith('/'))
  );

  const handleDownloadClick = () => {
    if (hasRealUrl) {
      onShowToast?.("Starting SK Mobile Android APK download...", "success");
      // Create temporary anchor to trigger native download
      const a = document.createElement('a');
      a.href = APP_DOWNLOAD_CONFIG.downloadUrl;
      a.download = APP_DOWNLOAD_CONFIG.fallbackFilename || 'SK-Mobile.apk';
      a.target = '_blank';
      a.rel = 'noopener noreferrer';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
    } else {
      setShowConfigModal(true);
    }
  };

  return (
    <section id="app-download" className="relative overflow-hidden py-16 border-y border-[#262626] bg-gradient-to-b from-[#0F0F0F] via-[#141414] to-[#0A0A0A]">
      {/* Background Decorative Gold Accents */}
      <div className="absolute top-1/2 left-0 w-96 h-96 bg-[#D4AF37]/5 rounded-full blur-3xl pointer-events-none -translate-y-1/2"></div>
      <div className="absolute bottom-0 right-0 w-80 h-80 bg-emerald-500/5 rounded-full blur-3xl pointer-events-none"></div>

      <div className="container relative z-10">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 items-center">
          
          {/* Left Column: Copy & Download Actions (7 cols) */}
          <div className="lg:col-span-7 space-y-6 text-left">
            {/* Top Chip */}
            <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-[#D4AF37]/10 border border-[#D4AF37]/30 text-xs font-bold text-[#D4AF37]">
              <AndroidIcon className="w-4 h-4 text-[#D4AF37]" size={16} />
              <span>Official SK Mobile Android Application</span>
            </div>

            {/* Headline */}
            <div className="space-y-2">
              <h2 className="text-3xl sm:text-4xl md:text-5xl font-black text-white tracking-tight leading-tight">
                Get the <span className="gold-text">SK Mobile App</span> on Your Android Phone
              </h2>
              <p className="text-gray-300 text-xs sm:text-sm md:text-base leading-relaxed">
                Experience Udgir's premier tech store directly on your home screen. Track real-time repair status, get instant cash quotes for old phones, and unlock secret in-store member discounts.
              </p>
            </div>

            {/* Technical Metadata Chips */}
            <div className="flex flex-wrap items-center gap-2.5 text-[11px] text-gray-300">
              <span className="px-2.5 py-1 rounded-lg bg-[#1C1C1C] border border-[#2E2E2E] font-mono text-[#D4AF37] font-semibold flex items-center gap-1.5">
                <FileCheck size={13} />
                {APP_DOWNLOAD_CONFIG.version}
              </span>
              <span className="px-2.5 py-1 rounded-lg bg-[#1C1C1C] border border-[#2E2E2E] font-medium flex items-center gap-1.5">
                <Cpu size={13} className="text-gray-400" />
                APK Size: <strong>{APP_DOWNLOAD_CONFIG.fileSize}</strong>
              </span>
              <span className="px-2.5 py-1 rounded-lg bg-[#1C1C1C] border border-[#2E2E2E] font-medium flex items-center gap-1.5">
                <AndroidIcon className="w-3 h-3 text-emerald-400" size={12} />
                {APP_DOWNLOAD_CONFIG.androidRequirement}
              </span>
              <span className="px-2.5 py-1 rounded-lg bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 font-bold flex items-center gap-1.5">
                <ShieldCheck size={13} />
                Safe & Verified APK
              </span>
            </div>

            {/* App Highlights Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-2">
              {APP_DOWNLOAD_CONFIG.features.slice(0, 4).map((feat, idx) => (
                <div key={idx} className="flex items-start gap-2.5 p-2.5 rounded-xl bg-[#141414]/90 border border-[#222222]">
                  <CheckCircle2 size={16} className="text-[#D4AF37] shrink-0 mt-0.5" />
                  <span className="text-xs text-gray-300 leading-snug">{feat}</span>
                </div>
              ))}
            </div>

            {/* CTAs */}
            <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3.5 pt-3">
              <button
                onClick={handleDownloadClick}
                className="gold-btn !py-3.5 !px-6 !text-sm flex items-center justify-center gap-2.5 shadow-xl shadow-[#D4AF37]/20 font-black cursor-pointer group"
                title="Download Android APK"
              >
                <AndroidIcon className="w-5 h-5 text-black group-hover:scale-110 transition-transform" size={20} />
                <span>Download SK Mobile APK</span>
                <Download size={16} className="text-black" />
              </button>

              <button
                onClick={() => setShowGuideModal(true)}
                className="outline-gold-btn !py-3.5 !px-5 !text-xs font-bold flex items-center justify-center gap-2"
              >
                <HelpCircle size={15} />
                <span>How to Install APK</span>
              </button>
            </div>

            {/* Note on Play Store vs Direct APK */}
            <p className="text-[11px] text-gray-500 flex items-center gap-1.5">
              <ShieldCheck size={13} className="text-emerald-400" />
              <span>Official standalone package for Android devices • Direct Udgir showroom release</span>
            </p>
          </div>

          {/* Right Column: Visual Mockup Showcase (5 cols) */}
          <div className="lg:col-span-5 flex justify-center">
            <div className="relative w-full max-w-sm">
              {/* Gold Ring Halo */}
              <div className="absolute inset-0 bg-gradient-to-tr from-[#D4AF37]/20 to-transparent rounded-3xl blur-2xl transform scale-95 pointer-events-none"></div>

              {/* Smartphone Frame Mockup */}
              <div className="relative bg-[#0E0E0E] border-2 border-[#2C2C2C] rounded-[36px] p-4 shadow-2xl shadow-black/80 space-y-4">
                {/* Speaker Notch */}
                <div className="flex justify-center">
                  <div className="w-20 h-3 bg-[#1F1F1F] rounded-full"></div>
                </div>

                {/* Simulated Screen Content */}
                <div className="bg-[#141414] border border-[#262626] rounded-2xl p-4 space-y-3.5">
                  {/* App Bar */}
                  <div className="flex items-center justify-between border-b border-[#222222] pb-2.5">
                    <div className="flex items-center gap-2">
                      <div className="w-7 h-7 rounded-lg bg-[#D4AF37] flex items-center justify-center text-black font-black text-xs">
                        SK
                      </div>
                      <div>
                        <div className="text-xs font-black text-white">SK MOBILE</div>
                        <div className="text-[9px] text-gray-400">Udgir Showroom App</div>
                      </div>
                    </div>
                    <span className="text-[9px] font-bold px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
                      LIVE
                    </span>
                  </div>

                  {/* App Quick Stats Card */}
                  <div className="p-3 rounded-xl bg-gradient-to-br from-[#1C1A10] to-[#121212] border border-[#D4AF37]/30 space-y-1">
                    <span className="text-[9px] uppercase font-bold text-[#D4AF37] tracking-wider block">
                      Fast Counter Services
                    </span>
                    <div className="text-xs font-black text-white">
                      30-Min Mobile Repairs • 90-Day Warranty
                    </div>
                    <div className="text-[10px] text-gray-400">
                      Nagar Parishad, Udgir Store
                    </div>
                  </div>

                  {/* App Feature Tiles */}
                  <div className="grid grid-cols-2 gap-2 text-[10px]">
                    <div className="p-2.5 rounded-xl bg-[#1A1A1A] border border-[#2E2E2E] text-center space-y-1">
                      <Smartphone size={16} className="text-[#D4AF37] mx-auto" />
                      <span className="font-bold text-white block">Smartphones</span>
                      <span className="text-gray-500 text-[9px]">Best Udgir Price</span>
                    </div>

                    <div className="p-2.5 rounded-xl bg-[#1A1A1A] border border-[#2E2E2E] text-center space-y-1">
                      <Clock size={16} className="text-blue-400 mx-auto" />
                      <span className="font-bold text-white block">Repair Track</span>
                      <span className="text-gray-500 text-[9px]">Live Status Alerts</span>
                    </div>
                  </div>

                  {/* Bottom App Download CTA inside mockup */}
                  <button
                    onClick={handleDownloadClick}
                    className="w-full py-2.5 rounded-xl bg-gradient-to-r from-[#D4AF37] to-[#B8860B] text-black font-extrabold text-xs flex items-center justify-center gap-1.5 shadow-md shadow-[#D4AF37]/20"
                  >
                    <Download size={13} />
                    <span>Install v1.2.0 (28.5 MB)</span>
                  </button>
                </div>

                {/* Bottom Home Indicator */}
                <div className="flex justify-center pt-1">
                  <div className="w-24 h-1 bg-gray-600 rounded-full"></div>
                </div>
              </div>
            </div>
          </div>

        </div>
      </div>

      {/* ----------------- MODAL 1: APK CONFIGURATION / PLACEHOLDER GUIDE ----------------- */}
      {showConfigModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-in fade-in">
          <div className="bg-[#141414] border border-[#D4AF37]/50 rounded-2xl max-w-lg w-full p-6 text-white relative shadow-2xl space-y-5">
            <button
              onClick={() => setShowConfigModal(false)}
              className="absolute right-4 top-4 p-1.5 rounded-lg bg-[#222222] text-gray-400 hover:text-white"
            >
              <X size={18} />
            </button>

            <div className="flex items-center gap-3 border-b border-[#262626] pb-3">
              <div className="w-10 h-10 rounded-xl bg-[#D4AF37]/10 border border-[#D4AF37]/30 flex items-center justify-center text-[#D4AF37]">
                <AndroidIcon className="w-6 h-6 text-[#D4AF37]" size={24} />
              </div>
              <div>
                <h3 className="text-base font-black text-white">SK Mobile Android APK Release</h3>
                <span className="text-[11px] text-[#D4AF37] font-semibold">{APP_DOWNLOAD_CONFIG.version} • {APP_DOWNLOAD_CONFIG.fileSize}</span>
              </div>
            </div>

            <div className="space-y-3 text-xs text-gray-300">
              <div className="p-3.5 rounded-xl bg-amber-500/10 border border-amber-500/30 text-amber-200 space-y-1">
                <div className="font-bold flex items-center gap-1.5 text-amber-300">
                  <Info size={14} />
                  <span>Configurable Download URL</span>
                </div>
                <p className="text-[11px] text-gray-300 leading-relaxed">
                  The APK download feature is connected and ready. A public download URL has not been attached yet in the configuration file.
                </p>
              </div>

              <div className="p-4 rounded-xl bg-[#0E0E0E] border border-[#262626] space-y-2">
                <span className="text-[11px] uppercase font-bold text-gray-400 block">
                  How to configure your public APK URL:
                </span>
                <ol className="list-decimal list-inside space-y-1.5 text-[11px] text-gray-300 leading-relaxed">
                  <li>
                    Open <strong className="text-white font-mono">web/src/data/storeData.js</strong>.
                  </li>
                  <li>
                    Locate <strong className="text-[#D4AF37] font-mono">APP_DOWNLOAD_CONFIG.downloadUrl</strong>.
                  </li>
                  <li>
                    Paste your direct APK URL (e.g., GitHub Releases, Firebase Storage, Google Drive direct download, or upload <code className="text-gray-300">sk-mobile.apk</code> to <code className="text-gray-300">web/public/</code>).
                  </li>
                  <li>
                    Alternatively, set environment variable <strong className="text-white font-mono">VITE_SK_MOBILE_APK_URL</strong> in your hosting settings (e.g. Vercel).
                  </li>
                </ol>
              </div>

              <div className="flex items-center justify-between text-[11px] text-gray-400 pt-1">
                <span>Want to test in-store?</span>
                <a 
                  href={`tel:${STORE_INFO.phone}`} 
                  className="text-[#D4AF37] hover:underline font-semibold flex items-center gap-1"
                >
                  <Phone size={12} />
                  <span>Call {STORE_INFO.phone}</span>
                </a>
              </div>
            </div>

            <div className="pt-2 border-t border-[#222222] flex gap-3">
              <button
                onClick={() => {
                  setShowConfigModal(false);
                  setShowGuideModal(true);
                }}
                className="outline-gold-btn flex-1 !text-xs !py-2.5 font-bold"
              >
                <span>View Installation Steps</span>
              </button>
              <button
                onClick={() => setShowConfigModal(false)}
                className="gold-btn !text-xs !py-2.5 !px-5 font-bold"
              >
                <span>Understood</span>
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ----------------- MODAL 2: STEP-BY-STEP INSTALLATION GUIDE ----------------- */}
      {showGuideModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-in fade-in">
          <div className="bg-[#141414] border border-[#2E2E2E] rounded-2xl max-w-lg w-full p-6 text-white relative shadow-2xl space-y-5">
            <button
              onClick={() => setShowGuideModal(false)}
              className="absolute right-4 top-4 p-1.5 rounded-lg bg-[#222222] text-gray-400 hover:text-white"
            >
              <X size={18} />
            </button>

            <div className="flex items-center gap-3 border-b border-[#262626] pb-3">
              <div className="w-10 h-10 rounded-xl bg-emerald-500/10 border border-emerald-500/30 flex items-center justify-center text-emerald-400">
                <HelpCircle size={22} />
              </div>
              <div>
                <h3 className="text-base font-black text-white">How to Install SK Mobile APK</h3>
                <span className="text-[11px] text-gray-400">Simple 3-step setup on any Android device</span>
              </div>
            </div>

            <div className="space-y-3.5">
              {APP_DOWNLOAD_CONFIG.installationSteps.map((s, idx) => (
                <div key={idx} className="flex items-start gap-3 p-3.5 rounded-xl bg-[#0E0E0E] border border-[#242424]">
                  <div className="w-7 h-7 rounded-lg bg-[#D4AF37]/20 border border-[#D4AF37]/40 flex items-center justify-center text-[#D4AF37] font-black text-xs shrink-0">
                    {s.step}
                  </div>
                  <div className="space-y-0.5">
                    <h4 className="text-xs font-bold text-white">{s.title}</h4>
                    <p className="text-[11px] text-gray-400 leading-relaxed">{s.desc}</p>
                  </div>
                </div>
              ))}
            </div>

            <div className="p-3 rounded-xl bg-[#1C180C] border border-[#D4AF37]/30 text-[11px] text-gray-300 flex items-center gap-2">
              <ShieldCheck size={16} className="text-[#D4AF37] shrink-0" />
              <span>SK Mobile APK is 100% clean, virus-free, and OEM certified.</span>
            </div>

            <button
              onClick={() => setShowGuideModal(false)}
              className="gold-btn w-full !text-xs !py-3 font-bold"
            >
              <span>Done</span>
            </button>
          </div>
        </div>
      )}
    </section>
  );
}
