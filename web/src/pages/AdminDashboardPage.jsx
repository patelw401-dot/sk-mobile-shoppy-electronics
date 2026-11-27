import React, { useState, useEffect } from 'react';
import { REAL_PRODUCTS, ACCESSORIES } from '../data/storeData';
import { 
  isUserAdmin, 
  AUTHORIZED_ADMIN_EMAILS,
  fetchAdminRepairBookings, 
  fetchAdminOldPhoneRequests,
  updateAdminRepairBookingStatus,
  updateAdminOldPhoneStatus,
  signInWithGooglePopup,
  logoutUser,
  DEFAULT_REPAIR_PRICING,
  saveAdminRepairPricingToFirestore,
  DEFAULT_SHOP_INFO,
  formatShopInfo,
  saveAdminShopInfoToFirestore
} from '../firebase';
import { 
  ShieldCheck, 
  ShieldAlert, 
  Lock, 
  Unlock, 
  Wrench, 
  Smartphone, 
  RefreshCw, 
  Users, 
  CheckCircle2, 
  Clock, 
  AlertCircle, 
  Search, 
  Filter, 
  ExternalLink, 
  Phone, 
  LogOut, 
  ArrowLeft,
  ChevronRight,
  Sparkles,
  Layers,
  Calendar,
  DollarSign,
  Package,
  ShoppingBag,
  Store,
  MapPin,
  Mail,
  Save,
  MessageCircle,
  Globe
} from 'lucide-react';
import ProductManagementTab from '../components/ProductManagementTab';
import OrdersManagementTab from '../components/OrdersManagementTab';
import CategoryManagementTab from '../components/CategoryManagementTab';

export default function AdminDashboardPage({
  currentUser,
  setActivePage,
  onShowToast,
  repairServices = DEFAULT_REPAIR_PRICING,
  shopInfo = DEFAULT_SHOP_INFO
}) {
  const normalizedShopInfo = formatShopInfo(shopInfo);
  const [activeTab, setActiveTab] = useState('repairs'); // 'repairs' | 'old-phones' | 'orders' | 'products' | 'categories' | 'shop-info' | 'system'
  const [repairs, setRepairs] = useState([]);
  const [oldPhones, setOldPhones] = useState([]);
  const [isLoadingData, setIsLoadingData] = useState(false);
  const [isUpdating, setIsUpdating] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [isAuthLoading, setIsAuthLoading] = useState(false);
  const [repairPriceInputs, setRepairPriceInputs] = useState(() =>
    (Array.isArray(repairServices) && repairServices.length > 0 ? repairServices : DEFAULT_REPAIR_PRICING).reduce((acc, s) => {
      acc[s.id] = String(s.estimatedStartingPrice ?? 0);
      return acc;
    }, {})
  );
  const [isSavingRepairPrices, setIsSavingRepairPrices] = useState(false);

  const [shopInfoForm, setShopInfoForm] = useState(() => ({
    ...normalizedShopInfo,
    servicesText: (normalizedShopInfo.services || []).join('\n'),
    deliveryCharge: String(normalizedShopInfo.deliveryCharge ?? 49),
    freeDeliveryThreshold: String(normalizedShopInfo.freeDeliveryThreshold ?? 499)
  }));
  const [isSavingShopInfo, setIsSavingShopInfo] = useState(false);
  const [shopInfoFeedback, setShopInfoFeedback] = useState(null);

  const isAdmin = isUserAdmin(currentUser);

  // Sync editable shop info fields when live Firestore shopInfo updates
  useEffect(() => {
    const formatted = formatShopInfo(shopInfo);
    setShopInfoForm({
      ...formatted,
      servicesText: (formatted.services || []).join('\n'),
      deliveryCharge: String(formatted.deliveryCharge ?? 49),
      freeDeliveryThreshold: String(formatted.freeDeliveryThreshold ?? 499)
    });
  }, [shopInfo]);

  // Sync editable price fields when live Firestore repairServices updates
  useEffect(() => {
    const source = Array.isArray(repairServices) && repairServices.length > 0 ? repairServices : DEFAULT_REPAIR_PRICING;
    setRepairPriceInputs(
      source.reduce((acc, s) => {
        acc[s.id] = String(s.estimatedStartingPrice ?? 0);
        return acc;
      }, {})
    );
  }, [repairServices]);

  // Load data whenever admin is authorized
  useEffect(() => {
    if (isAdmin) {
      loadAdminData();
    }
  }, [isAdmin]);

  const loadAdminData = async () => {
    setIsLoadingData(true);
    try {
      const [bookingsData, requestsData] = await Promise.allSettled([
        fetchAdminRepairBookings(),
        fetchAdminOldPhoneRequests()
      ]);

      if (bookingsData.status === 'fulfilled') {
        setRepairs(bookingsData.value);
      } else {
        console.warn("Could not load Firestore repair bookings:", bookingsData.reason);
      }

      if (requestsData.status === 'fulfilled') {
        setOldPhones(requestsData.value);
      } else {
        console.warn("Could not load Firestore old phone requests:", requestsData.reason);
      }
    } catch (err) {
      console.error("Error loading admin data:", err);
      onShowToast?.("Error loading administrative records.", "error");
    } finally {
      setIsLoadingData(false);
    }
  };

  const handleAdminSignIn = async () => {
    setIsAuthLoading(true);
    try {
      const result = await signInWithGooglePopup();
      const user = result.user;
      if (isUserAdmin(user)) {
        onShowToast?.(`Welcome Store Administrator (${user.email})!`, 'success');
      } else {
        onShowToast?.(`Signed in as ${user.email}, but this account is not in the Admin allowlist.`, 'error');
      }
    } catch (err) {
      console.error("Admin Sign-In failed:", err);
      onShowToast?.(err.message || 'Failed to sign in.', 'error');
    } finally {
      setIsAuthLoading(false);
    }
  };

  const handleAdminLogout = async () => {
    try {
      await logoutUser();
      onShowToast?.("Admin signed out successfully.", "info");
    } catch (err) {
      onShowToast?.(err.message || "Sign out error", "error");
    }
  };

  const handleUpdateRepairStatus = async (bookingId, newStatus) => {
    setIsUpdating(true);
    try {
      await updateAdminRepairBookingStatus(bookingId, newStatus, currentUser?.email);
      setRepairs(prev => prev.map(item => item.id === bookingId ? { ...item, repairStatus: newStatus } : item));
      onShowToast?.(`Repair booking #${bookingId} updated to ${newStatus}`, 'success');
    } catch (err) {
      console.error("Failed to update repair status:", err);
      onShowToast?.("Failed to update status in Firestore.", "error");
    } finally {
      setIsUpdating(false);
    }
  };

  const handleSaveRepairPrices = async () => {
    if (isSavingRepairPrices) return;
    const source = Array.isArray(repairServices) && repairServices.length > 0 ? repairServices : DEFAULT_REPAIR_PRICING;
    const updatedList = [];
    for (const service of source) {
      const parsed = Number.parseInt(repairPriceInputs[service.id], 10);
      if (Number.isNaN(parsed) || parsed < 0) {
        onShowToast?.(`Enter a valid ₹ price for ${service.title}`, 'error');
        return;
      }
      updatedList.push({
        ...service,
        estimatedStartingPrice: parsed
      });
    }

    setIsSavingRepairPrices(true);
    try {
      await saveAdminRepairPricingToFirestore(updatedList, currentUser);
      onShowToast?.('Repair prices updated and synced across Web & Android!', 'success');
    } catch (err) {
      console.error('Failed to save repair prices:', err);
      onShowToast?.(err.message || 'Failed to save repair prices to Firestore.', 'error');
    } finally {
      setIsSavingRepairPrices(false);
    }
  };

  const handleUpdateOldPhoneStatus = async (requestId, newStatus) => {
    setIsUpdating(true);
    try {
      await updateAdminOldPhoneStatus(requestId, newStatus);
      setOldPhones(prev => prev.map(item => item.id === requestId ? { ...item, status: newStatus } : item));
      onShowToast?.(`Request #${requestId} status set to ${newStatus}`, 'success');
    } catch (err) {
      console.error("Failed to update old phone status:", err);
      onShowToast?.("Failed to update status in Firestore.", "error");
    setIsUpdating(false);
    }
  };

  const handleSaveShopInfo = async (e) => {
    e?.preventDefault?.();
    if (isSavingShopInfo) return;
    setShopInfoFeedback(null);

    if (!shopInfoForm.shopName?.trim()) {
      const msg = 'Shop Name is required.';
      setShopInfoFeedback({ type: 'error', message: msg });
      onShowToast?.(msg, 'error');
      return;
    }
    if (!shopInfoForm.phoneNumber?.trim() || shopInfoForm.phoneNumber.replace(/\D/g, '').length < 10) {
      const msg = 'Please enter a valid 10-digit Calling Mobile Number.';
      setShopInfoFeedback({ type: 'error', message: msg });
      onShowToast?.(msg, 'error');
      return;
    }
    if (!shopInfoForm.address?.trim()) {
      const msg = 'Full Shop Address is required.';
      setShopInfoFeedback({ type: 'error', message: msg });
      onShowToast?.(msg, 'error');
      return;
    }

    const parsedServices = (shopInfoForm.servicesText || '')
      .split('\n')
      .map((line) => line.trim())
      .filter(Boolean);

    const payload = {
      ...shopInfoForm,
      whatsappNumber: shopInfoForm.whatsappNumber?.trim() || shopInfoForm.phoneNumber.trim(),
      services: parsedServices.length > 0 ? parsedServices : DEFAULT_SHOP_INFO.services,
      deliveryCharge: Math.max(0, Number.parseInt(shopInfoForm.deliveryCharge, 10) || 0),
      freeDeliveryThreshold: Math.max(0, Number.parseInt(shopInfoForm.freeDeliveryThreshold, 10) || 0)
    };

    setIsSavingShopInfo(true);
    try {
      await saveAdminShopInfoToFirestore(payload, currentUser);
      const okMsg = 'Shop information saved and synced to Web & Android customers in real time!';
      setShopInfoFeedback({ type: 'success', message: okMsg });
      onShowToast?.(okMsg, 'success');
    } catch (err) {
      console.error('Failed to save shop info:', err);
      const errMsg = err.message || 'Failed to save shop information to Firestore.';
      setShopInfoFeedback({ type: 'error', message: errMsg });
      onShowToast?.(errMsg, 'error');
    } finally {
      setIsSavingShopInfo(false);
    }
  };

  // -------------------------------------------------------------
  // STATE 1: User is NOT logged in
  // -------------------------------------------------------------
  if (!currentUser) {
    return (
      <div className="container py-16 flex justify-center items-center min-h-[75vh]">
        <div className="w-full max-w-lg bg-[#141414] border border-[#2E2E2E] rounded-2xl p-8 shadow-2xl relative overflow-hidden text-center space-y-6">
          <div className="absolute top-0 left-0 right-0 h-1.5 bg-gradient-to-r from-red-500 via-[#D4AF37] to-red-500"></div>

          <div className="w-16 h-16 rounded-2xl bg-amber-500/10 text-[#D4AF37] border border-[#D4AF37]/30 flex items-center justify-center mx-auto">
            <Lock size={30} />
          </div>

          <div>
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-red-500/10 border border-red-500/30 text-[11px] font-bold text-red-400 uppercase tracking-wider mb-2">
              <ShieldAlert size={14} />
              <span>Restricted Store Admin Portal</span>
            </div>
            <h1 className="text-2xl font-black text-white">SK Mobile Administrator Login</h1>
            <p className="text-xs text-gray-400 mt-2 max-w-sm mx-auto leading-relaxed">
              This area is restricted to authorized store management. Please sign in with an approved store owner Google account.
            </p>
          </div>

          <div className="p-4 rounded-xl bg-[#0E0E0E] border border-[#262626] text-left text-xs space-y-2">
            <p className="font-semibold text-gray-300 flex items-center gap-1.5">
              <ShieldCheck size={14} className="text-[#D4AF37]" />
              <span>Authorized Store Admin Emails:</span>
            </p>
            <ul className="text-gray-400 space-y-1 font-mono text-[11px] pl-5 list-disc">
              {AUTHORIZED_ADMIN_EMAILS.slice(0, 2).map((email, idx) => (
                <li key={idx} className="text-gray-300">{email}</li>
              ))}
            </ul>
          </div>

          <div className="space-y-3">
            <button
              onClick={handleAdminSignIn}
              disabled={isAuthLoading}
              className="w-full py-3.5 px-4 rounded-xl bg-[#D4AF37] hover:bg-[#C29F2F] text-black font-extrabold text-xs sm:text-sm flex items-center justify-center gap-2.5 transition shadow-lg shadow-[#D4AF37]/20 disabled:opacity-50"
            >
              {isAuthLoading ? (
                <span>Authenticating with Google...</span>
              ) : (
                <>
                  <svg className="w-4 h-4" viewBox="0 0 24 24">
                    <path fill="#4285F4" d="M23.745 12.27c0-.7-.06-1.4-.19-2.07H12v4.51h6.6c-.29 1.52-1.14 2.8-2.4 3.66v3.05h3.88c2.27-2.09 3.66-5.17 3.66-9.15z"/>
                    <path fill="#34A853" d="M12 24c3.24 0 5.95-1.08 7.93-2.91l-3.88-3.05c-1.08.72-2.45 1.16-4.05 1.16-3.12 0-5.77-2.1-6.72-4.94H1.27v3.15C3.25 21.27 7.31 24 12 24z"/>
                    <path fill="#FBBC05" d="M5.28 14.26c-.25-.72-.38-1.49-.38-2.26s.13-1.54.38-2.26V6.59H1.27C.46 8.21 0 10.05 0 12s.46 3.79 1.27 5.41l4.01-3.15z"/>
                    <path fill="#EA4335" d="M12 4.75c1.77 0 3.35.61 4.6 1.8l3.42-3.42C17.95 1.19 15.24 0 12 0 7.31 0 3.25 2.73 1.27 6.59l4.01 3.15c.95-2.84 3.6-4.99 6.72-4.99z"/>
                  </svg>
                  <span>Sign In with Store Admin Google Account</span>
                </>
              )}
            </button>

            <button
              onClick={() => setActivePage?.('home')}
              className="w-full py-2.5 text-xs text-gray-400 hover:text-white transition flex items-center justify-center gap-1.5"
            >
              <ArrowLeft size={14} />
              <span>Return to Customer Store Front</span>
            </button>
          </div>
        </div>
      </div>
    );
  }

  // -------------------------------------------------------------
  // STATE 2: Logged in, but NOT an authorized admin (ACCESS DENIED)
  // -------------------------------------------------------------
  if (!isAdmin) {
    return (
      <div className="container py-16 flex justify-center items-center min-h-[75vh]">
        <div className="w-full max-w-lg bg-[#141414] border border-red-500/40 rounded-2xl p-8 shadow-2xl relative overflow-hidden text-center space-y-6">
          <div className="absolute top-0 left-0 right-0 h-1.5 bg-red-600"></div>

          <div className="w-16 h-16 rounded-2xl bg-red-500/10 text-red-400 border border-red-500/30 flex items-center justify-center mx-auto">
            <ShieldAlert size={32} />
          </div>

          <div>
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-red-500/20 border border-red-500/40 text-[11px] font-bold text-red-300 uppercase tracking-wider mb-2">
              <span>Access Denied</span>
            </div>
            <h1 className="text-2xl font-black text-white">Unauthorized Account</h1>
            <p className="text-xs text-gray-400 mt-2 max-w-sm mx-auto leading-relaxed">
              Your account is signed in as <span className="text-white font-semibold">{currentUser.email}</span>, but this email is not in the SK Mobile Store Administrator allowlist.
            </p>
          </div>

          <div className="p-4 rounded-xl bg-[#0E0E0E] border border-[#222222] text-left text-xs space-y-2 text-gray-300">
            <p className="text-amber-400 font-semibold flex items-center gap-1.5">
              <AlertCircle size={15} />
              <span>Zero-Trust Security Rule:</span>
            </p>
            <p className="text-[11px] text-gray-400 leading-relaxed">
              Customer accounts cannot view administrative repair queues, customer inquiries, or change store catalog records.
            </p>
          </div>

          <div className="space-y-3 pt-2">
            <button
              onClick={handleAdminLogout}
              className="w-full py-3 px-4 rounded-xl bg-red-600 hover:bg-red-700 text-white font-bold text-xs flex items-center justify-center gap-2 transition"
            >
              <LogOut size={15} />
              <span>Sign Out / Switch to Store Admin Account</span>
            </button>

            <button
              onClick={() => setActivePage?.('home')}
              className="w-full py-2.5 px-4 rounded-xl border border-[#333333] hover:border-[#D4AF37] text-xs font-semibold text-gray-300 hover:text-white transition flex items-center justify-center gap-2"
            >
              <ArrowLeft size={14} />
              <span>Return to Customer Shopping Experience</span>
            </button>
          </div>
        </div>
      </div>
    );
  }

  // -------------------------------------------------------------
  // STATE 3: Logged in AND Verified Administrator
  // -------------------------------------------------------------
  const filteredRepairs = repairs.filter(item => {
    const matchesSearch = 
      (item.customerName || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
      (item.phone || '').includes(searchQuery) ||
      (item.model || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
      (item.id || '').toLowerCase().includes(searchQuery.toLowerCase());

    const matchesStatus = statusFilter === 'ALL' || item.repairStatus === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const filteredOldPhones = oldPhones.filter(item => {
    return (
      (item.customerName || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
      (item.phone || '').includes(searchQuery) ||
      (item.model || '').toLowerCase().includes(searchQuery.toLowerCase())
    );
  });

  const countPendingRepairs = repairs.filter(r => r.repairStatus !== 'Completed').length;
  const countCompletedRepairs = repairs.filter(r => r.repairStatus === 'Completed').length;

  return (
    <div className="container py-8 space-y-8">
      {/* Top Admin Header Bar */}
      <div className="bg-[#141414] border border-[#2E2E2E] rounded-2xl p-6 sm:p-8 relative overflow-hidden">
        <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-[#D4AF37] via-[#FFF0B3] to-[#B8860B]"></div>

        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div className="space-y-1.5">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#D4AF37]/15 border border-[#D4AF37]/30 text-xs font-bold text-[#D4AF37]">
              <ShieldCheck size={14} />
              <span>Verified Store Administrator</span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-black text-white">{normalizedShopInfo.shopName} Console</h1>
            <p className="text-xs text-gray-400">
              Logged in as: <strong className="text-white font-mono">{currentUser.email}</strong> • {normalizedShopInfo.shortLocationLabel}
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-2.5">
            <button
              onClick={loadAdminData}
              disabled={isLoadingData}
              className="py-2 px-3.5 rounded-xl bg-[#222222] hover:bg-[#2A2A2A] text-xs font-semibold text-gray-300 hover:text-white transition flex items-center gap-1.5"
            >
              <RefreshCw size={14} className={isLoadingData ? 'animate-spin text-[#D4AF37]' : ''} />
              <span>Refresh Records</span>
            </button>

            <button
              onClick={() => setActivePage?.('home')}
              className="py-2 px-3.5 rounded-xl border border-[#333333] hover:border-[#D4AF37] text-xs font-semibold text-gray-300 hover:text-white transition flex items-center gap-1.5"
            >
              <ArrowLeft size={14} />
              <span>Customer Front</span>
            </button>

            <button
              onClick={handleAdminLogout}
              className="py-2 px-3.5 rounded-xl bg-red-500/10 hover:bg-red-500/20 text-red-400 border border-red-500/30 text-xs font-semibold transition flex items-center gap-1.5"
            >
              <LogOut size={14} />
              <span>Sign Out</span>
            </button>
          </div>
        </div>

        {/* Quick KPI Counters */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3.5 mt-6 pt-6 border-t border-[#222222]">
          <div className="p-3.5 rounded-xl bg-[#0E0E0E] border border-[#222222]">
            <span className="text-[10px] uppercase font-bold text-gray-400 block mb-1">
              Active Repairs
            </span>
            <div className="text-2xl font-black text-[#D4AF37]">{countPendingRepairs}</div>
            <span className="text-[10px] text-gray-500">In shop queue</span>
          </div>

          <div className="p-3.5 rounded-xl bg-[#0E0E0E] border border-[#222222]">
            <span className="text-[10px] uppercase font-bold text-gray-400 block mb-1">
              Repairs Completed
            </span>
            <div className="text-2xl font-black text-emerald-400">{countCompletedRepairs}</div>
            <span className="text-[10px] text-gray-500">Finished & Delivered</span>
          </div>

          <div className="p-3.5 rounded-xl bg-[#0E0E0E] border border-[#222222]">
            <span className="text-[10px] uppercase font-bold text-gray-400 block mb-1">
              Old Phone Inquiries
            </span>
            <div className="text-2xl font-black text-blue-400">{oldPhones.length}</div>
            <span className="text-[10px] text-gray-500">Valuation requests</span>
          </div>

          <div className="p-3.5 rounded-xl bg-[#0E0E0E] border border-[#222222]">
            <span className="text-[10px] uppercase font-bold text-gray-400 block mb-1">
              Catalog Items
            </span>
            <div className="text-2xl font-black text-purple-400">
              {REAL_PRODUCTS.length + ACCESSORIES.length}
            </div>
            <span className="text-[10px] text-gray-500">Phones & accessories</span>
          </div>
        </div>
      </div>

      {/* Tabs Bar */}
      <div className="flex border-b border-[#262626] overflow-x-auto gap-2">
        <button
          onClick={() => setActiveTab('repairs')}
          className={`py-3 px-4 font-bold text-xs sm:text-sm flex items-center gap-2 border-b-2 transition whitespace-nowrap ${
            activeTab === 'repairs'
              ? 'border-[#D4AF37] text-[#D4AF37] bg-[#D4AF37]/5'
              : 'border-transparent text-gray-400 hover:text-white'
          }`}
        >
          <Wrench size={16} />
          <span>Repair Bookings ({repairs.length})</span>
        </button>

        <button
          onClick={() => setActiveTab('old-phones')}
          className={`py-3 px-4 font-bold text-xs sm:text-sm flex items-center gap-2 border-b-2 transition whitespace-nowrap ${
            activeTab === 'old-phones'
              ? 'border-[#D4AF37] text-[#D4AF37] bg-[#D4AF37]/5'
              : 'border-transparent text-gray-400 hover:text-white'
          }`}
        >
          <RefreshCw size={16} />
          <span>Old Phone Requests ({oldPhones.length})</span>
        </button>

        <button
          onClick={() => setActiveTab('orders')}
          className={`py-3 px-4 font-bold text-xs sm:text-sm flex items-center gap-2 border-b-2 transition whitespace-nowrap ${
            activeTab === 'orders'
              ? 'border-[#D4AF37] text-[#D4AF37] bg-[#D4AF37]/5'
              : 'border-transparent text-gray-400 hover:text-white'
          }`}
        >
          <ShoppingBag size={16} />
          <span>Orders Management</span>
        </button>

        <button
          onClick={() => setActiveTab('products')}
          className={`py-3 px-4 font-bold text-xs sm:text-sm flex items-center gap-2 border-b-2 transition whitespace-nowrap ${
            activeTab === 'products'
              ? 'border-[#D4AF37] text-[#D4AF37] bg-[#D4AF37]/5'
              : 'border-transparent text-gray-400 hover:text-white'
          }`}
        >
          <Package size={16} />
          <span>Product Management (CRUD)</span>
        </button>

        <button
          onClick={() => setActiveTab('categories')}
          className={`py-3 px-4 font-bold text-xs sm:text-sm flex items-center gap-2 border-b-2 transition whitespace-nowrap ${
            activeTab === 'categories'
              ? 'border-[#D4AF37] text-[#D4AF37] bg-[#D4AF37]/5'
              : 'border-transparent text-gray-400 hover:text-white'
          }`}
        >
          <Layers size={16} />
          <span>Category Management</span>
        </button>

        <button
          onClick={() => setActiveTab('shop-info')}
          className={`py-3 px-4 font-bold text-xs sm:text-sm flex items-center gap-2 border-b-2 transition whitespace-nowrap ${
            activeTab === 'shop-info'
              ? 'border-[#D4AF37] text-[#D4AF37] bg-[#D4AF37]/5'
              : 'border-transparent text-gray-400 hover:text-white'
          }`}
        >
          <Store size={16} />
          <span>Shop Info &amp; Contact</span>
        </button>

        <button
          onClick={() => setActiveTab('system')}
          className={`py-3 px-4 font-bold text-xs sm:text-sm flex items-center gap-2 border-b-2 transition whitespace-nowrap ${
            activeTab === 'system'
              ? 'border-[#D4AF37] text-[#D4AF37] bg-[#D4AF37]/5'
              : 'border-transparent text-gray-400 hover:text-white'
          }`}
        >
          <ShieldCheck size={16} />
          <span>Security & Admins</span>
        </button>
      </div>

      {/* Search & Filter bar for list tabs */}
      {(activeTab === 'repairs' || activeTab === 'old-phones') && (
        <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 bg-[#111111] p-3 rounded-xl border border-[#222222]">
          <div className="relative flex-1">
            <Search size={15} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-500" />
            <input
              type="text"
              placeholder="Search by customer name, phone, or model..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full bg-[#0A0A0A] border border-[#262626] rounded-lg pl-9 pr-4 py-2 text-xs text-white focus:outline-none focus:border-[#D4AF37]"
            />
          </div>

          {activeTab === 'repairs' && (
            <div className="flex items-center gap-2">
              <Filter size={14} className="text-gray-400" />
              <select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                className="bg-[#0A0A0A] border border-[#262626] rounded-lg px-3 py-2 text-xs text-white focus:outline-none focus:border-[#D4AF37]"
              >
                <option value="ALL">All Statuses</option>
                <option value="Appointment Booked">Appointment Booked</option>
                <option value="Pickup Scheduled">Pickup Scheduled</option>
                <option value="In Progress">In Progress</option>
                <option value="Completed">Completed</option>
                <option value="Pending">Pending</option>
              </select>
            </div>
          )}
        </div>
      )}

      {/* ----------------- TAB 1: REPAIRS ----------------- */}
      {activeTab === 'repairs' && (
        <div className="space-y-4">
          {/* Repair Price Management Card */}
          <div className="bg-[#141414] border border-[#D4AF37]/30 rounded-2xl p-5 space-y-4">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
              <div>
                <h3 className="text-sm font-black text-white flex items-center gap-2">
                  <DollarSign size={16} className="text-[#D4AF37]" />
                  <span>Repair Price Management</span>
                </h3>
                <p className="text-[11px] text-gray-400 mt-0.5">
                  Edit starting ₹ prices shown in Home &amp; Repair Booking across both Web and Android in real time.
                </p>
              </div>
              <button
                type="button"
                disabled={isSavingRepairPrices}
                onClick={handleSaveRepairPrices}
                className="gold-btn !text-xs !py-2 !px-4 self-start sm:self-auto disabled:opacity-50"
              >
                {isSavingRepairPrices ? 'Saving...' : 'Save Repair Prices'}
              </button>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
              {(Array.isArray(repairServices) && repairServices.length > 0 ? repairServices : DEFAULT_REPAIR_PRICING).map((service) => (
                <div key={service.id} className="p-3 rounded-xl bg-[#0E0E0E] border border-[#262626] space-y-1.5">
                  <div className="flex items-center justify-between gap-2">
                    <span className="text-xs font-bold text-white truncate">{service.title}</span>
                    <span className="text-[10px] text-gray-500 shrink-0">{service.turnaroundTime}</span>
                  </div>
                  <div className="relative">
                    <span className="absolute left-3 top-1/2 -translate-y-1/2 text-xs font-bold text-[#D4AF37]">₹</span>
                    <input
                      type="number"
                      min="0"
                      value={repairPriceInputs[service.id] ?? ''}
                      onChange={(e) => {
                        const val = e.target.value.replace(/[^0-9]/g, '');
                        setRepairPriceInputs((prev) => ({ ...prev, [service.id]: val }));
                      }}
                      className="w-full bg-[#141414] border border-[#2E2E2E] rounded-lg pl-7 pr-3 py-1.5 text-xs font-semibold text-white focus:outline-none focus:border-[#D4AF37]"
                      placeholder="Starting ₹"
                    />
                  </div>
                </div>
              ))}
            </div>
          </div>

          {isLoadingData ? (
            <div className="p-12 text-center text-gray-400 text-xs">
              <RefreshCw size={24} className="animate-spin text-[#D4AF37] mx-auto mb-2" />
              <span>Fetching repair bookings from Firestore...</span>
            </div>
          ) : filteredRepairs.length === 0 ? (
            <div className="p-12 text-center bg-[#141414] border border-[#222222] rounded-2xl space-y-2">
              <Wrench size={32} className="text-gray-600 mx-auto" />
              <h3 className="text-base font-bold text-white">No repair bookings found</h3>
              <p className="text-xs text-gray-400">
                {searchQuery || statusFilter !== 'ALL' 
                  ? "Try clearing your search filters."
                  : "New customer repair bookings booked via the website will appear here in real-time."}
              </p>
            </div>
          ) : (
            <div className="grid grid-cols-1 gap-4">
              {filteredRepairs.map((b) => (
                <div 
                  key={b.id} 
                  className="bg-[#141414] border border-[#262626] rounded-xl p-5 hover:border-[#D4AF37]/50 transition space-y-3"
                >
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-[#222222] pb-3">
                    <div className="flex items-center gap-2.5">
                      <div className="w-8 h-8 rounded-lg bg-[#D4AF37]/10 text-[#D4AF37] flex items-center justify-center font-bold text-xs">
                        {b.brand ? b.brand[0] : 'R'}
                      </div>
                      <div>
                        <span className="font-mono text-[11px] text-[#D4AF37] font-semibold">
                          #{b.id}
                        </span>
                        <h3 className="text-sm font-bold text-white">{b.customerName || 'Customer'}</h3>
                      </div>
                    </div>

                    <div className="flex items-center gap-2">
                      <span className={`text-[11px] font-bold px-2.5 py-1 rounded-full border ${
                        b.repairStatus === 'Completed'
                          ? 'bg-emerald-500/10 text-emerald-300 border-emerald-500/30'
                          : b.repairStatus === 'In Progress'
                          ? 'bg-blue-500/10 text-blue-300 border-blue-500/30'
                          : 'bg-amber-500/10 text-amber-300 border-amber-500/30'
                      }`}>
                        {b.repairStatus || 'Pending'}
                      </span>
                    </div>
                  </div>

                  <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-3 text-xs text-gray-300">
                    <div>
                      <span className="text-[10px] text-gray-500 uppercase block">Phone / Device</span>
                      <strong className="text-white">{b.brand} {b.model}</strong>
                    </div>

                    <div>
                      <span className="text-[10px] text-gray-500 uppercase block">Customer Contact</span>
                      <a href={`tel:${b.phone}`} className="text-[#D4AF37] hover:underline flex items-center gap-1 font-semibold">
                        <Phone size={12} />
                        <span>{b.phone}</span>
                      </a>
                    </div>

                    <div>
                      <span className="text-[10px] text-gray-500 uppercase block">Service Type</span>
                      <span>{b.pickupRequired ? 'Doorstep Pickup' : 'Shop Counter Walk-in'}</span>
                    </div>

                    <div>
                      <span className="text-[10px] text-gray-500 uppercase block">Preferred Time</span>
                      <span>{b.preferredSlot || 'Flexible'}</span>
                    </div>
                  </div>

                  <div className="p-2.5 rounded-lg bg-[#0E0E0E] border border-[#1E1E1E] text-xs text-gray-300">
                    <strong className="text-gray-400">Issue Reported: </strong>
                    <span>{b.problem}</span>
                    {b.notes && (
                      <div className="text-[11px] text-gray-400 mt-1">
                        <strong>Notes: </strong>{b.notes}
                      </div>
                    )}
                    {b.address && (
                      <div className="text-[11px] text-gray-400 mt-1">
                        <strong>Address: </strong>{b.address}
                      </div>
                    )}
                  </div>

                  {/* Admin Status Changer */}
                  <div className="pt-2 flex flex-wrap items-center justify-between gap-3 text-xs">
                    <span className="text-gray-500 text-[11px]">
                      Change Repair Status:
                    </span>
                    <div className="flex flex-wrap gap-1.5">
                      {['Appointment Booked', 'Pickup Scheduled', 'In Progress', 'Completed'].map((statusOption) => (
                        <button
                          key={statusOption}
                          disabled={isUpdating || b.repairStatus === statusOption}
                          onClick={() => handleUpdateRepairStatus(b.id, statusOption)}
                          className={`px-2.5 py-1 rounded text-[11px] font-semibold transition ${
                            b.repairStatus === statusOption
                              ? 'bg-[#D4AF37] text-black cursor-default'
                              : 'bg-[#222222] hover:bg-[#2E2E2E] text-gray-300 disabled:opacity-40'
                          }`}
                        >
                          {statusOption}
                        </button>
                      ))}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* ----------------- TAB 2: OLD PHONES ----------------- */}
      {activeTab === 'old-phones' && (
        <div className="space-y-4">
          {isLoadingData ? (
            <div className="p-12 text-center text-gray-400 text-xs">
              <RefreshCw size={24} className="animate-spin text-[#D4AF37] mx-auto mb-2" />
              <span>Fetching sell inquiries from Firestore...</span>
            </div>
          ) : filteredOldPhones.length === 0 ? (
            <div className="p-12 text-center bg-[#141414] border border-[#222222] rounded-2xl space-y-2">
              <RefreshCw size={32} className="text-gray-600 mx-auto" />
              <h3 className="text-base font-bold text-white">No old phone inquiries yet</h3>
              <p className="text-xs text-gray-400">
                Customer sell/exchange requests submitted on the website will be listed here.
              </p>
            </div>
          ) : (
            <div className="grid grid-cols-1 gap-4">
              {filteredOldPhones.map((req) => (
                <div key={req.id} className="bg-[#141414] border border-[#262626] rounded-xl p-5 space-y-3">
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-[#222222] pb-3">
                    <div>
                      <span className="font-mono text-[11px] text-[#D4AF37] font-semibold">#{req.id}</span>
                      <h3 className="text-sm font-bold text-white">{req.customerName}</h3>
                    </div>
                    <span className="text-[11px] font-bold px-2.5 py-1 rounded-full bg-blue-500/10 text-blue-300 border border-blue-500/30">
                      {req.status || 'Submitted'}
                    </span>
                  </div>

                  <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-3 text-xs text-gray-300">
                    <div>
                      <span className="text-[10px] text-gray-500 uppercase block">Device</span>
                      <strong className="text-white">{req.brand} {req.model} ({req.storage})</strong>
                    </div>
                    <div>
                      <span className="text-[10px] text-gray-500 uppercase block">Condition</span>
                      <span>{req.condition}</span>
                    </div>
                    <div>
                      <span className="text-[10px] text-gray-500 uppercase block">Contact</span>
                      <a href={`tel:${req.phone}`} className="text-[#D4AF37] hover:underline flex items-center gap-1 font-semibold">
                        <Phone size={12} />
                        <span>{req.phone}</span>
                      </a>
                    </div>
                    <div>
                      <span className="text-[10px] text-gray-500 uppercase block">Expected / Quote</span>
                      <strong className="text-emerald-400">{req.expectedPrice || req.estimatedQuote}</strong>
                    </div>
                  </div>

                  <div className="pt-2 flex items-center justify-between text-xs">
                    <span className="text-gray-500 text-[11px]">Set Inquiry Status:</span>
                    <div className="flex gap-2">
                      {['Submitted', 'Inspecting', 'Valuation Accepted', 'Completed'].map((s) => (
                        <button
                          key={s}
                          disabled={isUpdating || req.status === s}
                          onClick={() => handleUpdateOldPhoneStatus(req.id, s)}
                          className={`px-2.5 py-1 rounded text-[11px] font-semibold transition ${
                            req.status === s 
                              ? 'bg-[#D4AF37] text-black' 
                              : 'bg-[#222222] hover:bg-[#2A2A2A] text-gray-300'
                          }`}
                        >
                          {s}
                        </button>
                      ))}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* ----------------- TAB: ORDERS MANAGEMENT ----------------- */}
      {activeTab === 'orders' && (
        <OrdersManagementTab
          currentUser={currentUser}
          onShowToast={onShowToast}
        />
      )}

      {/* ----------------- TAB 3: PRODUCT MANAGEMENT (CRUD) ----------------- */}
      {activeTab === 'products' && (
        <ProductManagementTab
          currentUser={currentUser}
          onShowToast={onShowToast}
          setActivePage={setActivePage}
        />
      )}

      {/* ----------------- TAB 4: CATEGORY MANAGEMENT ----------------- */}
      {activeTab === 'categories' && (
        <CategoryManagementTab
          currentUser={currentUser}
          onShowToast={onShowToast}
        />
      )}

      {/* ----------------- TAB: SHOP INFO & CONTACT MANAGEMENT ----------------- */}
      {activeTab === 'shop-info' && (
        <form onSubmit={handleSaveShopInfo} className="bg-[#141414] border border-[#262626] rounded-2xl p-6 sm:p-8 space-y-6">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-[#222222] pb-4">
            <div>
              <h2 className="text-lg font-black text-white flex items-center gap-2">
                <Store size={20} className="text-[#D4AF37]" />
                <span>Shop Information Management</span>
              </h2>
              <p className="text-xs text-gray-400 mt-1">
                Changes saved here sync live to <code className="text-[#D4AF37]">settings/shop_info</code> and immediately update all Web &amp; Android customer screens (Call Shop, WhatsApp, Footer, Contact, Header &amp; Checkout).
              </p>
            </div>
            <button
              type="submit"
              disabled={isSavingShopInfo}
              className="gold-btn !text-xs !py-2.5 !px-5 flex items-center gap-2 self-start sm:self-auto disabled:opacity-50"
            >
              <Save size={15} />
              <span>{isSavingShopInfo ? 'Saving & Syncing...' : 'Save & Sync Shop Info'}</span>
            </button>
          </div>

          {shopInfoFeedback && (
            <div
              className={`p-3.5 rounded-xl border text-xs flex items-center gap-2.5 ${
                shopInfoFeedback.type === 'success'
                  ? 'bg-emerald-500/10 border-emerald-500/40 text-emerald-300'
                  : 'bg-red-500/10 border-red-500/40 text-red-300'
              }`}
            >
              {shopInfoFeedback.type === 'success' ? <CheckCircle2 size={16} /> : <AlertCircle size={16} />}
              <span className="font-semibold">{shopInfoFeedback.message}</span>
            </div>
          )}

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-gray-300 mb-1.5">Shop Name *</label>
              <input
                type="text"
                value={shopInfoForm.shopName || ''}
                onChange={(e) => setShopInfoForm((p) => ({ ...p, shopName: e.target.value }))}
                className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3.5 py-2.5 text-sm text-white focus:outline-none focus:border-[#D4AF37]"
                placeholder="SK Mobile Shoppy and Electronics"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-300 mb-1.5">Shop Email *</label>
              <input
                type="email"
                value={shopInfoForm.email || ''}
                onChange={(e) => setShopInfoForm((p) => ({ ...p, email: e.target.value }))}
                className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3.5 py-2.5 text-sm text-white focus:outline-none focus:border-[#D4AF37]"
                placeholder="skmobileshoppyandelectronics@gmail.com"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-300 mb-1.5">Calling Mobile Number (Call Shop) *</label>
              <input
                type="tel"
                value={shopInfoForm.phoneNumber || ''}
                onChange={(e) => setShopInfoForm((p) => ({ ...p, phoneNumber: e.target.value }))}
                className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3.5 py-2.5 text-sm text-white focus:outline-none focus:border-[#D4AF37]"
                placeholder="+91 9823109313"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-300 mb-1.5">WhatsApp Number *</label>
              <input
                type="tel"
                value={shopInfoForm.whatsappNumber || ''}
                onChange={(e) => setShopInfoForm((p) => ({ ...p, whatsappNumber: e.target.value }))}
                className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3.5 py-2.5 text-sm text-white focus:outline-none focus:border-[#D4AF37]"
                placeholder="+91 9823109313"
              />
            </div>

            <div className="md:col-span-2">
              <label className="block text-xs font-semibold text-gray-300 mb-1.5">Full Address *</label>
              <textarea
                rows={2}
                value={shopInfoForm.address || ''}
                onChange={(e) => setShopInfoForm((p) => ({ ...p, address: e.target.value }))}
                className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3.5 py-2.5 text-sm text-white focus:outline-none focus:border-[#D4AF37]"
                placeholder="Near Nagar Parishad Office, Station Road, Udgir, Maharashtra"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-300 mb-1.5">Pincode *</label>
              <input
                type="text"
                maxLength={10}
                value={shopInfoForm.pincode || ''}
                onChange={(e) => setShopInfoForm((p) => ({ ...p, pincode: e.target.value }))}
                className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3.5 py-2.5 text-sm text-white focus:outline-none focus:border-[#D4AF37]"
                placeholder="413517"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-300 mb-1.5">Landmark</label>
              <input
                type="text"
                value={shopInfoForm.landmark || ''}
                onChange={(e) => setShopInfoForm((p) => ({ ...p, landmark: e.target.value }))}
                className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3.5 py-2.5 text-sm text-white focus:outline-none focus:border-[#D4AF37]"
                placeholder="Opposite Nagar Parishad Municipal Complex"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-300 mb-1.5">Opening Time</label>
              <input
                type="text"
                value={shopInfoForm.openingTime || ''}
                onChange={(e) => setShopInfoForm((p) => ({ ...p, openingTime: e.target.value }))}
                className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3.5 py-2.5 text-sm text-white focus:outline-none focus:border-[#D4AF37]"
                placeholder="09:30 AM"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-300 mb-1.5">Closing Time</label>
              <input
                type="text"
                value={shopInfoForm.closingTime || ''}
                onChange={(e) => setShopInfoForm((p) => ({ ...p, closingTime: e.target.value }))}
                className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3.5 py-2.5 text-sm text-white focus:outline-none focus:border-[#D4AF37]"
                placeholder="09:30 PM"
              />
            </div>

            <div className="md:col-span-2">
              <label className="block text-xs font-semibold text-gray-300 mb-1.5">Opening Hours Label (Shown to Customers)</label>
              <input
                type="text"
                value={shopInfoForm.openingHours || ''}
                onChange={(e) => setShopInfoForm((p) => ({ ...p, openingHours: e.target.value }))}
                className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3.5 py-2.5 text-sm text-white focus:outline-none focus:border-[#D4AF37]"
                placeholder="9:30 AM – 9:30 PM (Open All 7 Days)"
              />
            </div>

            <div className="md:col-span-2">
              <label className="block text-xs font-semibold text-gray-300 mb-1.5">Google Maps URL</label>
              <input
                type="url"
                value={shopInfoForm.googleMapsUrl || ''}
                onChange={(e) => setShopInfoForm((p) => ({ ...p, googleMapsUrl: e.target.value }))}
                className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3.5 py-2.5 text-sm text-white focus:outline-none focus:border-[#D4AF37]"
                placeholder="https://maps.google.com/?q=..."
              />
            </div>

            <div className="md:col-span-2">
              <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                Business Services List (One service per line)
              </label>
              <textarea
                rows={4}
                value={shopInfoForm.servicesText || ''}
                onChange={(e) => setShopInfoForm((p) => ({ ...p, servicesText: e.target.value }))}
                className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3.5 py-2.5 text-sm text-white focus:outline-none focus:border-[#D4AF37]"
                placeholder="New Smartphones & 5G Mobiles&#10;Certified Pre-Owned Phones&#10;Express Mobile Repair"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-300 mb-1.5">Delivery Charge (₹)</label>
              <input
                type="number"
                min="0"
                value={shopInfoForm.deliveryCharge ?? ''}
                onChange={(e) => setShopInfoForm((p) => ({ ...p, deliveryCharge: e.target.value.replace(/[^0-9]/g, '') }))}
                className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3.5 py-2.5 text-sm text-white focus:outline-none focus:border-[#D4AF37]"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-300 mb-1.5">Free Delivery Above (₹)</label>
              <input
                type="number"
                min="0"
                value={shopInfoForm.freeDeliveryThreshold ?? ''}
                onChange={(e) => setShopInfoForm((p) => ({ ...p, freeDeliveryThreshold: e.target.value.replace(/[^0-9]/g, '') }))}
                className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3.5 py-2.5 text-sm text-white focus:outline-none focus:border-[#D4AF37]"
              />
            </div>
          </div>

          <div className="flex flex-wrap gap-4 pt-2 border-t border-[#222222] text-xs text-gray-300">
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                checked={Boolean(shopInfoForm.pickupAvailable)}
                onChange={(e) => setShopInfoForm((p) => ({ ...p, pickupAvailable: e.target.checked }))}
                className="accent-[#D4AF37]"
              />
              <span>Store Pickup Available</span>
            </label>
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                checked={Boolean(shopInfoForm.homeDeliveryAvailable)}
                onChange={(e) => setShopInfoForm((p) => ({ ...p, homeDeliveryAvailable: e.target.checked }))}
                className="accent-[#D4AF37]"
              />
              <span>Home Delivery Available</span>
            </label>
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                checked={Boolean(shopInfoForm.codEnabled)}
                onChange={(e) => setShopInfoForm((p) => ({ ...p, codEnabled: e.target.checked }))}
                className="accent-[#D4AF37]"
              />
              <span>Cash on Delivery</span>
            </label>
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                checked={Boolean(shopInfoForm.upiEnabled)}
                onChange={(e) => setShopInfoForm((p) => ({ ...p, upiEnabled: e.target.checked }))}
                className="accent-[#D4AF37]"
              />
              <span>UPI Payments</span>
            </label>
          </div>
        </form>
      )}

      {/* ----------------- TAB 4: SYSTEM & SECURITY ----------------- */}
      {activeTab === 'system' && (
        <div className="bg-[#141414] border border-[#262626] rounded-2xl p-6 sm:p-8 space-y-6">
          <div>
            <h2 className="text-lg font-black text-white flex items-center gap-2">
              <ShieldCheck size={20} className="text-[#D4AF37]" />
              <span>{normalizedShopInfo.shopName} Security &amp; Administrative Policy</span>
            </h2>
            <p className="text-xs text-gray-400 mt-1">
              Active configuration for {normalizedShopInfo.shortLocationLabel} showroom online operations
            </p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
            <div className="p-4 rounded-xl bg-[#0E0E0E] border border-[#222222] space-y-2">
              <h4 className="font-bold text-white">Authorized Administrator Allowlist</h4>
              <ul className="text-gray-300 font-mono space-y-1 text-[11px]">
                {AUTHORIZED_ADMIN_EMAILS.map((email, idx) => (
                  <li key={idx} className="flex items-center gap-1.5">
                    <CheckCircle2 size={12} className="text-emerald-400" />
                    <span>{email}</span>
                  </li>
                ))}
              </ul>
            </div>

            <div className="p-4 rounded-xl bg-[#0E0E0E] border border-[#222222] space-y-2">
              <h4 className="font-bold text-white">Firestore Security Rules</h4>
              <p className="text-gray-400 leading-relaxed text-[11px]">
                Zero-Trust rules enforce customer isolation: customers can only read/create their own records. Only verified admin emails listed above can update booking statuses, delete documents, or view all customer records.
              </p>
            </div>
          </div>

          <div className="pt-4 border-t border-[#222222] flex flex-col sm:flex-row justify-between items-center gap-3 text-xs text-gray-400">
            <span>Store Location: {normalizedShopInfo.fullAddress}</span>
            <div className="flex gap-2">
              <button
                onClick={() => setActivePage?.('home')}
                className="hover:text-white transition"
              >
                Customer Home
              </button>
              <span>•</span>
              <button
                onClick={handleAdminLogout}
                className="text-red-400 hover:text-red-300 transition"
              >
                Sign Out Admin
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
