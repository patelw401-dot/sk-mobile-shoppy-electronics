import React, { useState, useMemo, useEffect } from 'react';
import { ACCESSORIES, STORE_INFO } from '../data/storeData';
import { fetchFirestoreProducts, FIREBASE_PROJECT_ID } from '../firebase';
import ProductCard from '../components/ProductCard';
import { 
  Zap, 
  Headphones, 
  Cable, 
  Battery, 
  Shield, 
  Phone, 
  Sparkles,
  Database,
  RefreshCw,
  CheckCircle2,
  AlertCircle
} from 'lucide-react';

export default function AccessoriesPage({ onShowToast }) {
  const [selectedCategory, setSelectedCategory] = useState('all');

  // Firestore integration state
  const [rawAccessories, setRawAccessories] = useState(
    ACCESSORIES.map(item => ({ ...item, isSample: true, isLive: false }))
  );
  const [dataSource, setDataSource] = useState('loading'); // 'loading' | 'live' | 'sample-empty' | 'sample-error'
  const [firestoreError, setFirestoreError] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  const loadAccessories = async () => {
    setIsLoading(true);
    setFirestoreError(null);
    try {
      const result = await fetchFirestoreProducts();
      if (result.success && !result.isEmpty && result.data.length > 0) {
        // Filter for accessory products
        const accessoriesList = result.data.filter(p => {
          const cat = (p.category || '').toLowerCase();
          return cat.includes('charger') || 
                 cat.includes('cable') || 
                 cat.includes('audio') || 
                 cat.includes('headphone') || 
                 cat.includes('bluetooth') || 
                 cat.includes('accessori');
        });

        if (accessoriesList.length > 0) {
          setRawAccessories(accessoriesList.map(item => ({ ...item, isLive: true, isSample: false })));
          setDataSource('live');
          onShowToast?.(`Loaded ${accessoriesList.length} accessories live from Cloud Firestore!`, 'success');
        } else {
          setRawAccessories(ACCESSORIES.map(item => ({ ...item, isSample: true, isLive: false })));
          setDataSource('sample-empty');
        }
      } else if (result.success && result.isEmpty) {
        setRawAccessories(ACCESSORIES.map(item => ({ ...item, isSample: true, isLive: false })));
        setDataSource('sample-empty');
      } else {
        setRawAccessories(ACCESSORIES.map(item => ({ ...item, isSample: true, isLive: false })));
        setDataSource('sample-error');
        setFirestoreError(result.error);
      }
    } catch (err) {
      setRawAccessories(ACCESSORIES.map(item => ({ ...item, isSample: true, isLive: false })));
      setDataSource('sample-error');
      setFirestoreError(err.message || 'Unknown network error');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadAccessories();
  }, []);

  const categories = [
    { id: 'all', label: 'All Accessories', icon: Sparkles },
    { id: 'chargers', label: 'Fast Chargers & GaN', icon: Zap },
    { id: 'cables', label: 'Type-C & Data Cables', icon: Cable },
    { id: 'audio', label: 'Bluetooth & Neckbands', icon: Headphones }
  ];

  const filteredAccessories = useMemo(() => {
    if (selectedCategory === 'all') return rawAccessories;
    return rawAccessories.filter(item => {
      const cat = (item.category || '').toLowerCase();
      if (selectedCategory === 'chargers') return cat.includes('charger');
      if (selectedCategory === 'cables') return cat.includes('cable');
      if (selectedCategory === 'audio') return cat.includes('audio') || cat.includes('headphone') || cat.includes('bluetooth');
      return cat === selectedCategory;
    });
  }, [rawAccessories, selectedCategory]);

  return (
    <div className="container py-8 space-y-8">
      {/* Header */}
      <div className="border-b border-[#222222] pb-6">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-[#D4AF37] mb-1">
              <span>Official Store Inventory</span>
              <span>•</span>
              <span>Tested & Verified</span>
            </div>
            <h1 className="text-3xl font-black text-white">Genuine Mobile Accessories</h1>
            <p className="text-xs sm:text-sm text-gray-400 mt-1 max-w-2xl">
              High-efficiency GaN power adapters, ballistic braided charging cables, and high-fidelity Bluetooth audio earphones backed by store replacement guarantee in Udgir.
            </p>
          </div>

          {/* Cloud Firestore Status Badge & Sync CTA */}
          <div className="flex items-center gap-3">
            <button
              onClick={loadAccessories}
              disabled={isLoading}
              className="inline-flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-semibold bg-[#1C1C1C] border border-[#333333] text-gray-300 hover:text-white hover:border-[#D4AF37] transition disabled:opacity-50"
              title="Query Firebase Firestore 'products' collection"
            >
              <RefreshCw size={13} className={isLoading ? 'animate-spin text-[#D4AF37]' : ''} />
              <span>{isLoading ? 'Checking Firestore...' : 'Sync Cloud Data'}</span>
            </button>
          </div>
        </div>

        {/* Data Source Notice Banner */}
        <div className="mt-4">
          {isLoading ? (
            <div className="p-3 rounded-xl bg-[#141414] border border-[#262626] flex items-center gap-2 text-xs text-gray-400">
              <RefreshCw size={14} className="animate-spin text-[#D4AF37]" />
              <span>Connecting to Firebase Firestore (<code>{FIREBASE_PROJECT_ID}</code> / <code>products</code> collection)...</span>
            </div>
          ) : dataSource === 'live' ? (
            <div className="p-3 rounded-xl bg-emerald-950/30 border border-emerald-500/40 flex items-center justify-between gap-2 text-xs text-emerald-300">
              <div className="flex items-center gap-2">
                <CheckCircle2 size={16} className="text-emerald-400 shrink-0" />
                <span>
                  <strong>Live Firestore Active:</strong> Showing real-time accessories from Firebase project <code>{FIREBASE_PROJECT_ID}</code>.
                </span>
              </div>
              <span className="text-[10px] font-bold uppercase px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-300 border border-emerald-500/40">
                Cloud Connected
              </span>
            </div>
          ) : dataSource === 'sample-empty' ? (
            <div className="p-3.5 rounded-xl bg-amber-950/20 border border-amber-500/30 flex flex-col sm:flex-row sm:items-center justify-between gap-2 text-xs text-amber-200/90">
              <div className="flex items-center gap-2">
                <Database size={16} className="text-[#D4AF37] shrink-0" />
                <span>
                  <strong>Firestore Connected (Empty Collection):</strong> The Firestore collection <code>products</code> in project <code>{FIREBASE_PROJECT_ID}</code> contains 0 accessory items. Displaying <strong>Labeled Sample Accessories</strong>.
                </span>
              </div>
              <span className="text-[10px] font-bold uppercase px-2 py-0.5 rounded bg-amber-500/20 text-amber-300 border border-amber-500/30 self-start sm:self-auto shrink-0">
                Sample Data Fallback
              </span>
            </div>
          ) : (
            <div className="p-3.5 rounded-xl bg-red-950/20 border border-red-500/30 flex flex-col sm:flex-row sm:items-center justify-between gap-2 text-xs text-red-200">
              <div className="flex items-center gap-2">
                <AlertCircle size={16} className="text-red-400 shrink-0" />
                <span>
                  <strong>Firestore Notice:</strong> Unable to fetch cloud accessories ({firestoreError || 'Connection unavailable'}). Displaying <strong>Labeled Sample Accessories</strong>.
                </span>
              </div>
              <span className="text-[10px] font-bold uppercase px-2 py-0.5 rounded bg-red-500/20 text-red-300 border border-red-500/30 self-start sm:self-auto shrink-0">
                Sample Data Fallback
              </span>
            </div>
          )}
        </div>
      </div>

      {/* Category Tabs */}
      <div className="flex items-center gap-2 overflow-x-auto pb-2 scrollbar-none">
        {categories.map((cat) => {
          const Icon = cat.icon;
          const isActive = selectedCategory === cat.id;
          return (
            <button
              key={cat.id}
              onClick={() => setSelectedCategory(cat.id)}
              className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs sm:text-sm font-semibold transition whitespace-nowrap ${
                isActive
                  ? 'bg-[#D4AF37] text-black font-bold shadow-md shadow-[#D4AF37]/20'
                  : 'bg-[#141414] text-gray-300 hover:text-white border border-[#262626] hover:border-gray-500'
              }`}
            >
              <Icon size={16} />
              <span>{cat.label}</span>
            </button>
          );
        })}
      </div>

      {/* Accessories Grid */}
      <div className="grid-cards">
        {filteredAccessories.map((item) => (
          <ProductCard
            key={item.id}
            product={item}
            onAction={(p) => {
              onShowToast?.(`Inquiry for ${p.name} noted. Available in-store at Nagar Parishad, Udgir.`);
            }}
          />
        ))}
      </div>

      {/* Trust & In-store Advice Banner */}
      <div className="p-6 rounded-2xl bg-[#141414] border border-[#222222] flex flex-col md:flex-row items-center justify-between gap-6">
        <div className="flex items-start gap-4">
          <div className="p-3 rounded-xl bg-[#D4AF37]/10 text-[#D4AF37] shrink-0">
            <Shield size={28} />
          </div>
          <div>
            <h3 className="text-base font-bold text-white mb-1">
              Need Screen Protectors or Custom Phone Covers?
            </h3>
            <p className="text-xs text-gray-400 max-w-xl leading-relaxed">
              We provide laser-cut 11D UV tempered glass installation and shockproof matte/transparent back covers for all Samsung, iPhone, OnePlus, Xiaomi, and Vivo handsets directly at our shop counter.
            </p>
          </div>
        </div>

        <a
          href={`tel:${STORE_INFO.phone}`}
          className="gold-btn whitespace-nowrap !text-xs !py-2.5 !px-5"
        >
          <Phone size={14} />
          <span>Call Udgir Shop: {STORE_INFO.displayPhone}</span>
        </a>
      </div>
    </div>
  );
}
