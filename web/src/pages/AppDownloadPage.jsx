import React from 'react';
import AppDownloadSection from '../components/AppDownloadSection';
import { STORE_INFO, APP_DOWNLOAD_CONFIG } from '../data/storeData';
import { 
  ArrowLeft, 
  Smartphone, 
  ShieldCheck, 
  Phone, 
  MapPin, 
  Clock, 
  Download, 
  CheckCircle2,
  FileCheck
} from 'lucide-react';

export default function AppDownloadPage({ setActivePage, onShowToast }) {
  return (
    <div className="space-y-8 pb-16">
      {/* Top Breadcrumb Header */}
      <div className="bg-[#121212] border-b border-[#222222] py-4">
        <div className="container flex items-center justify-between">
          <div className="flex items-center gap-2 text-xs text-gray-400">
            <button 
              onClick={() => setActivePage('home')}
              className="hover:text-white transition flex items-center gap-1"
            >
              <ArrowLeft size={14} />
              <span>Home</span>
            </button>
            <span>/</span>
            <span className="text-[#D4AF37] font-semibold">SK Mobile Android App</span>
          </div>

          <div className="flex items-center gap-2 text-xs text-gray-400">
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
            <span>Version {APP_DOWNLOAD_CONFIG.version} Available</span>
          </div>
        </div>
      </div>

      {/* Main Hero Download Section */}
      <AppDownloadSection onShowToast={onShowToast} />

      {/* Additional Technical Details & Store Verification */}
      <div className="container">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="p-6 rounded-2xl bg-[#141414] border border-[#242424] space-y-3">
            <div className="w-10 h-10 rounded-xl bg-[#D4AF37]/10 text-[#D4AF37] flex items-center justify-center">
              <FileCheck size={22} />
            </div>
            <h3 className="text-sm font-bold text-white">Direct OEM Release</h3>
            <p className="text-xs text-gray-400 leading-relaxed">
              Standalone Android APK distributed directly by SK Mobile Shoppy and Electronics. No third-party bloatware, advertisements, or background tracking.
            </p>
          </div>

          <div className="p-6 rounded-2xl bg-[#141414] border border-[#242424] space-y-3">
            <div className="w-10 h-10 rounded-xl bg-emerald-500/10 text-emerald-400 flex items-center justify-center">
              <ShieldCheck size={22} />
            </div>
            <h3 className="text-sm font-bold text-white">Google Play Protect Verified</h3>
            <p className="text-xs text-gray-400 leading-relaxed">
              Compiled using modern Android SDK and signed with secure certificates. Passes Play Protect security scans seamlessly.
            </p>
          </div>

          <div className="p-6 rounded-2xl bg-[#141414] border border-[#242424] space-y-3">
            <div className="w-10 h-10 rounded-xl bg-blue-500/10 text-blue-400 flex items-center justify-center">
              <Phone size={22} />
            </div>
            <h3 className="text-sm font-bold text-white">In-Store Installation Support</h3>
            <p className="text-xs text-gray-400 leading-relaxed">
              Need help installing on your device? Visit our store opposite Nagar Parishad Municipal Complex, Udgir. Our technicians will set it up for free.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
