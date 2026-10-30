import React, { useState } from 'react';
import { STORE_INFO } from '../data/storeData';
import { 
  Smartphone, 
  Wrench, 
  RefreshCw, 
  Headphones, 
  Phone, 
  MapPin, 
  Menu, 
  X, 
  User, 
  ShoppingBag,
  ShoppingCart,
  Sparkles,
  ShieldCheck,
  Package
} from 'lucide-react';
import { isUserAdmin } from '../firebase';

export default function Navbar({ activePage, setActivePage, currentUser, cartCount = 0 }) {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const navItems = [
    { id: 'home', label: 'Home', icon: Sparkles },
    { id: 'products', label: 'Smartphones', icon: Smartphone },
    { id: 'accessories', label: 'Accessories', icon: Headphones },
    { id: 'repairs', label: 'Repair Booking', icon: Wrench, badge: '90-Day Warranty' },
    { id: 'old-phones', label: 'Old Phones (Buy/Sell)', icon: RefreshCw },
    { id: 'contact', label: 'Store & Contact', icon: MapPin },
    { id: 'admin', label: 'Admin Portal', icon: ShieldCheck, badge: 'Admin' }
  ];

  const handleNavClick = (pageId) => {
    setActivePage(pageId);
    setMobileMenuOpen(false);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <header className="sticky top-0 z-50 bg-[#0B0B0B]/95 backdrop-blur-md border-b border-[#262626]">
      {/* Top Banner with Store Hotline */}
      <div className="bg-[#121212] border-b border-[#1F1F1F] text-xs py-1.5 px-4 text-gray-300">
        <div className="container flex justify-between items-center text-[11px] sm:text-xs">
          <div className="flex items-center gap-2">
            <span className="inline-block w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
            <span>Udgir Service Center & Store Open Today • 9:30 AM – 9:30 PM</span>
          </div>
          <div className="flex items-center gap-4">
            <a 
              href={`tel:${STORE_INFO.phone}`}
              className="text-[#D4AF37] hover:underline flex items-center gap-1 font-semibold"
            >
              <Phone size={12} />
              <span>Call: {STORE_INFO.displayPhone}</span>
            </a>
            <span className="hidden md:inline text-gray-500">|</span>
            <span className="hidden md:inline text-gray-400">Nagar Parishad, Udgir</span>
          </div>
        </div>
      </div>

      {/* Main Navbar */}
      <div className="container py-3 flex items-center justify-between gap-4">
        {/* Brand Logo */}
        <button 
          onClick={() => handleNavClick('home')}
          className="flex items-center gap-3 text-left focus:outline-none"
        >
          <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-[#D4AF37] to-[#8C6B0D] p-0.5 shadow-lg shadow-[#D4AF37]/10 flex items-center justify-center">
            <div className="w-full h-full bg-[#0E0E0E] rounded-[10px] flex items-center justify-center">
              <span className="font-extrabold text-[#D4AF37] text-lg tracking-wider">SK</span>
            </div>
          </div>
          <div>
            <div className="flex items-center gap-1.5">
              <span className="font-black text-lg tracking-tight text-white">SK MOBILE</span>
              <span className="text-[10px] font-bold px-1.5 py-0.5 rounded bg-[#D4AF37]/20 text-[#D4AF37] border border-[#D4AF37]/30">
                UDGIR
              </span>
            </div>
            <p className="text-[10px] text-gray-400 uppercase tracking-widest font-semibold">
              Shoppy & Electronic
            </p>
          </div>
        </button>

        {/* Desktop Nav Items */}
        <nav className="hidden lg:flex items-center gap-1">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = activePage === item.id;
            return (
              <button
                key={item.id}
                onClick={() => handleNavClick(item.id)}
                className={`relative px-3.5 py-2 rounded-lg text-sm font-medium transition-all flex items-center gap-1.5 ${
                  isActive
                    ? 'text-[#D4AF37] bg-[#D4AF37]/10 font-bold'
                    : 'text-gray-300 hover:text-white hover:bg-white/5'
                }`}
              >
                <Icon size={15} className={isActive ? 'text-[#D4AF37]' : 'text-gray-400'} />
                <span>{item.label}</span>
                {item.badge && (
                  <span className="ml-1 text-[9px] bg-amber-500/20 text-amber-300 px-1.5 py-0.2 rounded font-bold border border-amber-500/30">
                    {item.badge}
                  </span>
                )}
              </button>
            );
          })}
        </nav>

        {/* Action Buttons */}
        <div className="flex items-center gap-2">
          {currentUser ? (
            <button
              onClick={() => handleNavClick('login')}
              className={`hidden sm:flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold border transition-all ${
                activePage === 'login'
                  ? 'bg-[#D4AF37] text-black border-[#D4AF37]'
                  : 'border-[#D4AF37]/40 bg-[#D4AF37]/10 text-[#D4AF37] hover:border-[#D4AF37]'
              }`}
            >
              <User size={14} />
              <span className="max-w-[120px] truncate">
                {currentUser.displayName || currentUser.email || 'My Account'}
              </span>
            </button>
          ) : (
            <button
              onClick={() => handleNavClick('login')}
              className={`hidden sm:flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold border transition-all ${
                activePage === 'login'
                  ? 'bg-[#D4AF37] text-black border-[#D4AF37]'
                  : 'border-[#333333] text-gray-300 hover:border-[#D4AF37] hover:text-[#D4AF37]'
              }`}
            >
              <User size={14} />
              <span>Customer Login</span>
            </button>
          )}

          <button
            onClick={() => handleNavClick('admin')}
            className={`hidden md:flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-bold border transition-all ${
              activePage === 'admin'
                ? 'bg-amber-500 text-black border-amber-400 shadow-md shadow-amber-500/20'
                : 'border-amber-500/40 bg-amber-500/10 text-amber-300 hover:border-amber-400 hover:bg-amber-500/20'
            }`}
          >
            <ShieldCheck size={14} className="text-amber-400" />
            <span>Admin Portal</span>
          </button>

          {/* Cart Button */}
          <button
            onClick={() => handleNavClick('cart')}
            className={`relative flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-bold border transition-all ${
              activePage === 'cart' || activePage === 'checkout'
                ? 'bg-[#D4AF37] text-black border-[#D4AF37] shadow-md shadow-[#D4AF37]/20'
                : 'border-[#333333] bg-[#141414] text-gray-200 hover:border-[#D4AF37] hover:text-[#D4AF37]'
            }`}
            title="View Shopping Cart"
          >
            <ShoppingCart size={15} />
            <span className="hidden sm:inline">Cart</span>
            {cartCount > 0 && (
              <span className={`px-1.5 py-0.2 rounded-full text-[10px] font-black ${
                activePage === 'cart' ? 'bg-black text-[#D4AF37]' : 'bg-[#D4AF37] text-black'
              }`}>
                {cartCount}
              </span>
            )}
          </button>

          <button
            onClick={() => handleNavClick('repairs')}
            className="gold-btn !text-xs !py-2 !px-3.5 shadow-sm"
          >
            <Wrench size={14} />
            <span className="hidden sm:inline">Book Repair</span>
            <span className="sm:hidden">Repair</span>
          </button>

          {/* Mobile Menu Button */}
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="lg:hidden p-2 text-gray-300 hover:text-white rounded-lg hover:bg-white/5"
            aria-label="Toggle Navigation"
          >
            {mobileMenuOpen ? <X size={22} /> : <Menu size={22} />}
          </button>
        </div>
      </div>

      {/* Mobile Drawer Menu */}
      {mobileMenuOpen && (
        <div className="lg:hidden bg-[#111111] border-b border-[#262626] px-4 py-4 space-y-2 animate-in slide-in-from-top">
          <div className="text-xs text-gray-400 uppercase tracking-wider px-3 pb-1 font-semibold">
            Store Navigation
          </div>
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = activePage === item.id;
            return (
              <button
                key={item.id}
                onClick={() => handleNavClick(item.id)}
                className={`w-full text-left px-3.5 py-2.5 rounded-lg text-sm font-medium flex items-center justify-between ${
                  isActive
                    ? 'bg-[#D4AF37]/15 text-[#D4AF37] font-bold border border-[#D4AF37]/30'
                    : 'text-gray-300 hover:bg-white/5'
                }`}
              >
                <div className="flex items-center gap-2.5">
                  <Icon size={16} className={isActive ? 'text-[#D4AF37]' : 'text-gray-400'} />
                  <span>{item.label}</span>
                </div>
                {item.badge && (
                  <span className="text-[10px] bg-amber-500/20 text-amber-300 px-1.5 py-0.5 rounded font-bold">
                    {item.badge}
                  </span>
                )}
              </button>
            );
          })}

          {/* Admin Portal Entry Point in Mobile Hamburger Menu */}
          <div className="pt-2 border-t border-[#262626]">
            <button
              onClick={() => handleNavClick('admin')}
              className={`w-full text-left px-3.5 py-2.5 rounded-lg text-sm font-bold flex items-center justify-between transition-all ${
                activePage === 'admin'
                  ? 'bg-amber-500/20 text-amber-300 border border-amber-500/50'
                  : 'bg-[#18150C] text-amber-400 border border-amber-500/30 hover:bg-amber-500/10'
              }`}
            >
              <div className="flex items-center gap-2.5">
                <ShieldCheck size={18} className="text-amber-400" />
                <span>Admin Portal</span>
              </div>
              <span className="text-[10px] bg-amber-400/20 text-amber-300 px-2 py-0.5 rounded font-extrabold border border-amber-400/30">
                {isUserAdmin(currentUser) ? 'Verified' : 'Portal'}
              </span>
            </button>
          </div>

          {/* Cart & Orders Quick Access in Mobile Menu */}
          <div className="grid grid-cols-2 gap-2 pt-2 border-t border-[#262626]">
            <button
              onClick={() => handleNavClick('cart')}
              className={`p-2.5 rounded-lg text-xs font-bold flex items-center justify-center gap-2 border transition-all ${
                activePage === 'cart' || activePage === 'checkout'
                  ? 'bg-[#D4AF37] text-black border-[#D4AF37]'
                  : 'bg-[#181818] text-white border-[#333333]'
              }`}
            >
              <ShoppingCart size={15} />
              <span>Cart {cartCount > 0 ? `(${cartCount})` : ''}</span>
            </button>

            <button
              onClick={() => handleNavClick('my-orders')}
              className={`p-2.5 rounded-lg text-xs font-bold flex items-center justify-center gap-2 border transition-all ${
                activePage === 'my-orders'
                  ? 'bg-[#D4AF37] text-black border-[#D4AF37]'
                  : 'bg-[#181818] text-white border-[#333333]'
              }`}
            >
              <Package size={15} />
              <span>My Orders</span>
            </button>
          </div>

          <div className="pt-2 border-t border-[#222222] flex flex-col gap-2">
            <button
              onClick={() => handleNavClick('login')}
              className="w-full py-2.5 text-center text-sm font-semibold rounded-lg bg-[#1C1C1C] text-gray-200 border border-[#333333] hover:border-[#D4AF37] flex items-center justify-center gap-2"
            >
              <User size={16} />
              <span>
                {currentUser ? `Account (${currentUser.displayName || currentUser.email})` : 'Customer / Admin Login'}
              </span>
            </button>
            <a
              href={`tel:${STORE_INFO.phone}`}
              className="w-full py-2.5 text-center text-sm font-bold rounded-lg bg-emerald-600/20 text-emerald-300 border border-emerald-500/40 flex items-center justify-center gap-2"
            >
              <Phone size={16} />
              <span>Call Store: {STORE_INFO.phone}</span>
            </a>
          </div>
        </div>
      )}
    </header>
  );
}
