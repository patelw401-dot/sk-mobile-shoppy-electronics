import React, { useState, useMemo, useEffect } from 'react';
import { REAL_PRODUCTS, ACCESSORIES, DEFAULT_CATEGORIES, STORE_INFO } from '../data/storeData';
import { fetchFirestoreProducts, fetchCategoriesFromFirestore, FIREBASE_PROJECT_ID } from '../firebase';
import ProductCard from '../components/ProductCard';
import ProductDetailsModal from '../components/ProductDetailsModal';
import { 
  Search, 
  Filter, 
  Phone, 
  CheckCircle2, 
  ShieldCheck, 
  X, 
  Database, 
  RefreshCw, 
  AlertCircle,
  ShoppingCart,
  Zap,
  Layers
} from 'lucide-react';

export default function ProductsPage({ 
  onShowToast, 
  onAddToCart, 
  onBuyNow, 
  selectedCategory: parentCategory, 
  setSelectedCategory: setParentCategory,
  onSelectProduct: parentOnSelectProduct
}) {
  const [internalCategory, setInternalCategory] = useState(parentCategory || 'ALL');
  const activeCategory = parentCategory || internalCategory;
  const [categories, setCategories] = useState(DEFAULT_CATEGORIES);
  const [selectedBrand, setSelectedBrand] = useState('All');
  const [searchQuery, setSearchQuery] = useState('');
  const [sortBy, setSortBy] = useState('featured');
  const [selectedProduct, setSelectedProduct] = useState(null);

  const handleSelectCategory = (slug) => {
    if (setParentCategory) setParentCategory(slug);
    setInternalCategory(slug);
  };

  // Firestore integration state
  const [rawProducts, setRawProducts] = useState(
    [...REAL_PRODUCTS, ...ACCESSORIES].map(p => ({ ...p, isSample: true, isLive: false }))
  );
  const [dataSource, setDataSource] = useState('loading'); // 'loading' | 'live' | 'sample-empty' | 'sample-error'
  const [firestoreError, setFirestoreError] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  const loadProducts = async () => {
    setIsLoading(true);
    setFirestoreError(null);
    try {
      const [prodRes, catRes] = await Promise.allSettled([
        fetchFirestoreProducts(),
        fetchCategoriesFromFirestore()
      ]);

      if (catRes.status === 'fulfilled' && catRes.value.success && catRes.value.data.length > 0) {
        setCategories(catRes.value.data.filter(c => c.isActive));
      } else {
        setCategories(DEFAULT_CATEGORIES.filter(c => c.isActive));
      }

      if (prodRes.status === 'fulfilled' && prodRes.value.success && !prodRes.value.isEmpty && prodRes.value.data.length > 0) {
        setRawProducts(prodRes.value.data.map(p => ({ ...p, isLive: true, isSample: false })));
        setDataSource('live');
      } else {
        setRawProducts([...REAL_PRODUCTS, ...ACCESSORIES].map(p => ({ ...p, isSample: true, isLive: false })));
        setDataSource('sample-empty');
      }
    } catch (err) {
      setRawProducts([...REAL_PRODUCTS, ...ACCESSORIES].map(p => ({ ...p, isSample: true, isLive: false })));
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

    // Filter by Active Category
    if (activeCategory && activeCategory !== 'ALL') {
      const catSlug = activeCategory.toLowerCase().replace(/_/g, '-');
      result = result.filter(p => {
        const pCat = (p.category || '').toLowerCase().replace(/_/g, '-');
        if (catSlug === 'smartphones') {
          return pCat === 'smartphones' || pCat.includes('smart') || pCat.includes('phone') || pCat.includes('mobile');
        }
        if (catSlug === 'accessories') {
          return pCat === 'accessories' || pCat.includes('accessori');
        }
        if (catSlug === 'old-phones') {
          return pCat.includes('old') || p.isPreOwned;
        }
        if (catSlug === 'chargers') {
          return pCat.includes('charger');
        }
        if (catSlug === 'cables') {
          return pCat.includes('cable');
        }
        if (catSlug === 'audio') {
          return pCat.includes('audio') || pCat.includes('headphone') || pCat.includes('earphone');
        }
        return pCat === catSlug || pCat.includes(catSlug);
      });
    }

    if (selectedBrand !== 'All') {
      result = result.filter(p => p.brand.toLowerCase() === selectedBrand.toLowerCase());
    }

    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      result = result.filter(p => {
        const nameMatch = (p.name || '').toLowerCase().includes(q);
        const brandMatch = (p.brand || '').toLowerCase().includes(q);
        const catMatch = (p.category || '').toLowerCase().includes(q);
        const descMatch = (p.description || '').toLowerCase().includes(q);
        const skuMatch = (p.sku || '').toLowerCase().includes(q);
        let specMatch = false;
        if (p.specs && typeof p.specs === 'object' && !Array.isArray(p.specs)) {
          specMatch = Object.entries(p.specs).some(([k, v]) =>
            k.toLowerCase().includes(q) || String(v).toLowerCase().includes(q)
          );
        }
        return nameMatch || brandMatch || catMatch || descMatch || skuMatch || specMatch;
      });
    }

    if (sortBy === 'price-low') {
      result.sort((a, b) => (Number(a.price) || 0) - (Number(b.price) || 0));
    } else if (sortBy === 'price-high') {
      result.sort((a, b) => (Number(b.price) || 0) - (Number(a.price) || 0));
    } else if (sortBy === 'rating') {
      result.sort((a, b) => (Number(b.rating) || 0) - (Number(a.rating) || 0));
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

      {/* Category Navigation Bar */}
      <div className="space-y-2">
        <div className="flex items-center justify-between">
          <span className="text-xs font-bold uppercase tracking-wider text-[#D4AF37] flex items-center gap-1.5">
            <Layers size={14} />
            <span>Categories</span>
          </span>
          {activeCategory !== 'ALL' && (
            <button
              onClick={() => handleSelectCategory('ALL')}
              className="text-xs text-gray-400 hover:text-white underline"
            >
              Show All Products
            </button>
          )}
        </div>

        <div className="flex items-center gap-2 overflow-x-auto pb-2 scrollbar-none">
          <button
            onClick={() => handleSelectCategory('ALL')}
            className={`px-3.5 py-2 rounded-xl text-xs font-bold whitespace-nowrap transition flex items-center gap-2 ${
              activeCategory === 'ALL'
                ? 'bg-[#D4AF37] text-black shadow-md'
                : 'bg-[#141414] text-gray-300 hover:text-white border border-[#2E2E2E]'
            }`}
          >
            <Layers size={14} />
            <span>All Products ({rawProducts.length})</span>
          </button>

          {categories.map((cat) => {
            const isSelected = activeCategory === cat.slug;
            return (
              <button
                key={cat.id}
                onClick={() => handleSelectCategory(cat.slug)}
                className={`px-3 py-1.5 rounded-xl text-xs font-bold whitespace-nowrap transition flex items-center gap-2 ${
                  isSelected
                    ? 'bg-[#D4AF37] text-black shadow-md'
                    : 'bg-[#141414] text-gray-300 hover:text-white border border-[#2E2E2E] hover:border-[#3E3E3E]'
                }`}
              >
                {cat.imageUrl && (
                  <img
                    src={cat.imageUrl}
                    alt={cat.name}
                    className="w-4 h-4 rounded-full object-cover"
                    onError={(e) => { e.target.style.display = 'none'; }}
                  />
                )}
                <span>{cat.name}</span>
              </button>
            );
          })}
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
              onSelect={(p) => (parentOnSelectProduct || setSelectedProduct)(p)}
              onAction={(p) => (parentOnSelectProduct || setSelectedProduct)(p)}
              onAddToCart={onAddToCart}
              onBuyNow={onBuyNow}
            />
          ))}
        </div>
      ) : (
        <div className="text-center py-16 bg-[#141414] rounded-xl border border-[#222222]">
          <p className="text-gray-400 text-sm mb-2">No products found matching your search.</p>
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
        <ProductDetailsModal
          product={selectedProduct}
          onClose={() => setSelectedProduct(null)}
          onAddToCart={onAddToCart}
          onBuyNow={onBuyNow}
          onSelectProduct={setSelectedProduct}
          allProducts={rawProducts}
        />
      )}
    </div>
  );
}
