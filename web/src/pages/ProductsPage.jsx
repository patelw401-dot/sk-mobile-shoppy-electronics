import React, { useState, useMemo, useEffect } from 'react';
import { REAL_PRODUCTS, STORE_INFO } from '../data/storeData';
import { fetchFirestoreProducts, FIREBASE_PROJECT_ID } from '../firebase';
import ProductCard from '../components/ProductCard';
import { 
  Search, 
  Filter, 
  Phone, 
  CheckCircle2, 
  ShieldCheck, 
  X, 
  Database, 
  RefreshCw, 
  AlertCircle
} from 'lucide-react';

export default function ProductsPage({ onShowToast }) {
  const [selectedBrand, setSelectedBrand] = useState('All');
  const [searchQuery, setSearchQuery] = useState('');
  const [sortBy, setSortBy] = useState('featured');
  const [selectedProduct, setSelectedProduct] = useState(null);

  // Firestore integration state
  const [rawProducts, setRawProducts] = useState(
    REAL_PRODUCTS.map(p => ({ ...p, isSample: true, isLive: false }))
  );
  const [dataSource, setDataSource] = useState('loading'); // 'loading' | 'live' | 'sample-empty' | 'sample-error'
  const [firestoreError, setFirestoreError] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  const loadProducts = async () => {
    setIsLoading(true);
    setFirestoreError(null);
    try {
      const result = await fetchFirestoreProducts();
      if (result.success && !result.isEmpty && result.data.length > 0) {
        // Filter products that belong to smartphones/mobile category
        const phones = result.data.filter(p => 
          p.category.includes('mobile') || 
          p.category.includes('smartphone') || 
          p.category.includes('phone') ||
          p.category === 'all'
        );
        if (phones.length > 0) {
          setRawProducts(phones.map(p => ({ ...p, isLive: true, isSample: false })));
          setDataSource('live');
          onShowToast?.(`Loaded ${phones.length} smartphones live from Cloud Firestore!`, 'success');
        } else {
          // Firestore connected and has items, but no smartphone category found
          setRawProducts(REAL_PRODUCTS.map(p => ({ ...p, isSample: true, isLive: false })));
          setDataSource('sample-empty');
        }
      } else if (result.success && result.isEmpty) {
        // Firestore connected, but products collection is empty in Cloud Firestore
        setRawProducts(REAL_PRODUCTS.map(p => ({ ...p, isSample: true, isLive: false })));
        setDataSource('sample-empty');
      } else {
        // Firestore returned error (e.g. offline, permission error)
        setRawProducts(REAL_PRODUCTS.map(p => ({ ...p, isSample: true, isLive: false })));
        setDataSource('sample-error');
        setFirestoreError(result.error);
      }
    } catch (err) {
      setRawProducts(REAL_PRODUCTS.map(p => ({ ...p, isSample: true, isLive: false })));
      setDataSource('sample-error');
      setFirestoreError(err.message || 'Unknown network error');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadProducts();
  }, []);

  const brands = useMemo(() => {
    const defaultBrands = ['All', 'OnePlus', 'Samsung', 'Xiaomi', 'Realme', 'Apple'];
    const dynamicSet = new Set(defaultBrands);
    rawProducts.forEach(p => {
      if (p.brand && typeof p.brand === 'string') {
        dynamicSet.add(p.brand);
      }
    });
    return Array.from(dynamicSet);
  }, [rawProducts]);

  const filteredProducts = useMemo(() => {
    let result = [...rawProducts];

    if (selectedBrand !== 'All') {
      result = result.filter(p => p.brand.toLowerCase() === selectedBrand.toLowerCase());
    }

    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      result = result.filter(p => 
        p.name.toLowerCase().includes(q) || 
        p.brand.toLowerCase().includes(q) ||
        p.description.toLowerCase().includes(q)
      );
    }

    if (sortBy === 'price-low') {
      result.sort((a, b) => a.price - b.price);
    } else if (sortBy === 'price-high') {
      result.sort((a, b) => b.price - a.price);
    } else if (sortBy === 'rating') {
      result.sort((a, b) => b.rating - a.rating);
    }

    return result;
  }, [rawProducts, selectedBrand, searchQuery, sortBy]);

  return (
    <div className="container py-8 space-y-8">
      {/* Header */}
      <div className="border-b border-[#222222] pb-6">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-[#D4AF37] mb-1">
              <span>Brand New Smartphones</span>
              <span>•</span>
              <span>Official Warranty</span>
            </div>
            <h1 className="text-3xl font-black text-white">Smartphones & Flagship Handsets</h1>
            <p className="text-xs sm:text-sm text-gray-400 mt-1 max-w-2xl">
              Browse brand new devices available for direct store pickup at Nagar Parishad, Udgir or express doorstep delivery with billing warranty.
            </p>
          </div>

          {/* Cloud Firestore Status Badge & Sync CTA */}
          <div className="flex items-center gap-3">
            <button
              onClick={loadProducts}
              disabled={isLoading}
              className="inline-flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-semibold bg-[#1C1C1C] border border-[#333333] text-gray-300 hover:text-white hover:border-[#D4AF37] transition disabled:opacity-50"
              title="Query Firebase Firestore 'products' collection"
            >
              <RefreshCw size={13} className={isLoading ? 'animate-spin text-[#D4AF37]' : ''} />
              <span>{isLoading ? 'Checking Firestore...' : 'Sync Cloud Data'}</span>
            </button>
          </div>
        </div>

        {/* Data Source Notice Banner (Required: Never silently swap without clear label) */}
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
                  <strong>Live Firestore Active:</strong> Showing real-time catalog from Firebase project <code>{FIREBASE_PROJECT_ID}</code>.
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
                  <strong>Firestore Connected (Empty Collection):</strong> The Firestore collection <code>products</code> in project <code>{FIREBASE_PROJECT_ID}</code> contains 0 items. Displaying <strong>Labeled Sample Catalog</strong> for store browsing.
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
                  <strong>Firestore Notice:</strong> Unable to fetch cloud documents ({firestoreError || 'Connection unavailable'}). Displaying <strong>Labeled Sample Catalog</strong>.
                </span>
              </div>
              <span className="text-[10px] font-bold uppercase px-2 py-0.5 rounded bg-red-500/20 text-red-300 border border-red-500/30 self-start sm:self-auto shrink-0">
                Sample Data Fallback
              </span>
            </div>
          )}
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="p-4 rounded-xl bg-[#141414] border border-[#222222] flex flex-col md:flex-row items-stretch md:items-center justify-between gap-4">
        {/* Search */}
        <div className="relative flex-1">
          <Search size={16} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            placeholder="Search by model, brand, Snapdragon, camera..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full bg-[#0E0E0E] border border-[#2D2D2D] rounded-lg pl-9 pr-4 py-2 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-[#D4AF37]"
          />
        </div>

        {/* Brand Filter Buttons */}
        <div className="flex items-center gap-1.5 overflow-x-auto pb-1 md:pb-0 scrollbar-none">
          {brands.map((b) => (
            <button
              key={b}
              onClick={() => setSelectedBrand(b)}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold whitespace-nowrap transition ${
                selectedBrand === b
                  ? 'bg-[#D4AF37] text-black font-bold'
                  : 'bg-[#1C1C1C] text-gray-300 hover:text-white border border-[#2C2C2C]'
              }`}
            >
              {b}
            </button>
          ))}
        </div>

        {/* Sort Options */}
        <div className="flex items-center gap-2 shrink-0">
          <span className="text-xs text-gray-400">Sort:</span>
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
            className="bg-[#0E0E0E] border border-[#2D2D2D] rounded-lg px-3 py-1.5 text-xs text-gray-200 focus:outline-none focus:border-[#D4AF37]"
          >
            <option value="featured">Featured / Best</option>
            <option value="price-low">Price: Low to High</option>
            <option value="price-high">Price: High to Low</option>
            <option value="rating">Highest Rated</option>
          </select>
        </div>
      </div>

      {/* Product Results Count */}
      <div className="flex items-center justify-between text-xs text-gray-400">
        <span>Showing {filteredProducts.length} smartphones available in Udgir</span>
        {searchQuery && (
          <button 
            onClick={() => setSearchQuery('')}
            className="text-[#D4AF37] hover:underline"
          >
            Clear Search
          </button>
        )}
      </div>

      {/* Products Grid */}
      {filteredProducts.length > 0 ? (
        <div className="grid-cards">
          {filteredProducts.map((product) => (
            <ProductCard
              key={product.id}
              product={product}
              onSelect={(p) => setSelectedProduct(p)}
              onAction={(p) => setSelectedProduct(p)}
            />
          ))}
        </div>
      ) : (
        <div className="text-center py-16 bg-[#141414] rounded-xl border border-[#222222]">
          <p className="text-gray-400 text-sm mb-2">No smartphones found matching your search.</p>
          <button
            onClick={() => { setSelectedBrand('All'); setSearchQuery(''); }}
            className="text-xs text-[#D4AF37] font-semibold hover:underline"
          >
            Reset Filters
          </button>
        </div>
      )}

      {/* Product Details Modal */}
      {selectedProduct && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-in fade-in">
          <div className="bg-[#141414] border border-[#D4AF37]/40 rounded-2xl max-w-lg w-full p-6 text-white relative max-h-[90vh] overflow-y-auto shadow-2xl">
            <button
              onClick={() => setSelectedProduct(null)}
              className="absolute right-4 top-4 p-1.5 rounded-lg bg-[#222222] text-gray-400 hover:text-white"
            >
              <X size={18} />
            </button>

            <div className="flex items-center gap-2 mb-2">
              <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-[#D4AF37]/20 text-[#D4AF37]">
                {selectedProduct.brand}
              </span>
              <span className="text-[10px] text-emerald-400 font-semibold flex items-center gap-1">
                <CheckCircle2 size={12} />
                In Stock at Nagar Parishad Store
              </span>
            </div>

            <h3 className="text-xl font-black mb-2 text-white">{selectedProduct.name}</h3>

            <div className="flex items-baseline gap-2 mb-4">
              <span className="text-2xl font-black text-[#D4AF37]">
                ₹{selectedProduct.price.toLocaleString('en-IN')}
              </span>
              {selectedProduct.originalPrice && (
                <span className="text-sm text-gray-500 line-through">
                  ₹{selectedProduct.originalPrice.toLocaleString('en-IN')}
                </span>
              )}
            </div>

            <p className="text-xs text-gray-300 leading-relaxed mb-6">
              {selectedProduct.description}
            </p>

            {/* Specifications List */}
            <div className="space-y-2 mb-6 text-xs bg-[#0E0E0E] p-4 rounded-xl border border-[#222222]">
              <div className="font-bold text-gray-200 border-b border-[#222222] pb-1.5 mb-2">
                Technical Specifications
              </div>
              {selectedProduct.specs && Object.entries(selectedProduct.specs).map(([k, v]) => (
                <div key={k} className="flex justify-between py-1 border-b border-[#1A1A1A] last:border-none">
                  <span className="text-gray-400">{k}</span>
                  <span className="text-gray-200 font-medium text-right">{v}</span>
                </div>
              ))}
            </div>

            {/* Store Guarantee */}
            <div className="flex items-center gap-2 p-3 rounded-lg bg-[#181818] border border-[#262626] text-xs text-gray-300 mb-6">
              <ShieldCheck size={18} className="text-[#D4AF37] shrink-0" />
              <span>100% Original Brand Packed with 1 Year Official India Manufacturer Warranty.</span>
            </div>

            {/* Action Buttons */}
            <div className="flex gap-3">
              <a
                href={`tel:${STORE_INFO.phone}`}
                className="gold-btn flex-1 !py-2.5 text-center"
              >
                <Phone size={16} />
                <span>Call Store to Reserve / Inquire</span>
              </a>
              <button
                onClick={() => {
                  setSelectedProduct(null);
                  onShowToast?.(`Inquiry noted for ${selectedProduct.name}. Call +91 9823109313 for instant store hold.`);
                }}
                className="outline-gold-btn px-4"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
