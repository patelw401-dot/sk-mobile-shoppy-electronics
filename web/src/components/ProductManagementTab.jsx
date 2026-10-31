import React, { useState, useEffect, useMemo } from 'react';
import { 
  fetchAdminProducts, 
  saveAdminProductToFirestore, 
  deleteAdminProductFromFirestore, 
  seedDefaultProductsToFirestore,
  quickUpdateProductWarranty 
} from '../firebase';
import { REAL_PRODUCTS, ACCESSORIES } from '../data/storeData';
import { 
  Plus, 
  Edit2, 
  Trash2, 
  Search, 
  Filter, 
  Image as ImageIcon, 
  Tag, 
  CheckCircle2, 
  AlertCircle, 
  RefreshCw, 
  X, 
  Upload, 
  ExternalLink,
  Package,
  Layers,
  Sparkles,
  AlertTriangle,
  Database,
  ArrowRight,
  Shield,
  ShieldCheck,
  ShieldOff
} from 'lucide-react';

const WARRANTY_PRESETS = ['7 Days', '30 Days', '3 Months', '6 Months', '1 Year', '2 Years', 'Custom'];

const PRESET_IMAGES = [
  { name: 'OnePlus Flagship', url: 'https://images.unsplash.com/photo-1592899677977-9c10ca588bbd?w=500&auto=format&fit=crop&q=80', brand: 'OnePlus', category: 'smartphones' },
  { name: 'Samsung Galaxy', url: 'https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=500&auto=format&fit=crop&q=80', brand: 'Samsung', category: 'smartphones' },
  { name: 'Apple iPhone', url: 'https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?w=500&auto=format&fit=crop&q=80', brand: 'Apple', category: 'smartphones' },
  { name: 'Redmi / Xiaomi', url: 'https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=500&auto=format&fit=crop&q=80', brand: 'Xiaomi', category: 'smartphones' },
  { name: 'Fast 65W Charger', url: 'https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=500&auto=format&fit=crop&q=80', brand: 'SK Mobile', category: 'accessories' },
  { name: 'Braided Type-C Cable', url: 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500&auto=format&fit=crop&q=80', brand: 'boAt', category: 'accessories' },
  { name: 'Wireless Neckband', url: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&auto=format&fit=crop&q=80', brand: 'OnePlus', category: 'accessories' }
];

export default function ProductManagementTab({ currentUser, onShowToast, setActivePage }) {
  const [products, setProducts] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isSeeding, setIsSeeding] = useState(false);

  // Filters & Search
  const [searchQuery, setSearchQuery] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('ALL');
  const [brandFilter, setBrandFilter] = useState('ALL');
  const [stockFilter, setStockFilter] = useState('ALL');

  // Modals
  const [showModal, setShowModal] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null);
  const [deleteConfirmProduct, setDeleteConfirmProduct] = useState(null);
  const [warrantyRemoveProduct, setWarrantyRemoveProduct] = useState(null);
  const [quickWarrantyProduct, setQuickWarrantyProduct] = useState(null);
  const [quickWarrantyData, setQuickWarrantyData] = useState({
    preset: '1 Year',
    custom: '',
    description: ''
  });

  // Form State
  const [formData, setFormData] = useState({
    name: '',
    brand: 'OnePlus',
    category: 'smartphones',
    price: '',
    originalPrice: '',
    stock: 5,
    description: '',
    imageUrl: '',
    badge: '',
    isFeatured: false,
    isBestSeller: false,
    hasWarranty: false,
    warrantyPreset: '1 Year',
    customDuration: '',
    warrantyDuration: '',
    warrantyDescription: '',
    specsText: ''
  });

  const loadProducts = async () => {
    setIsLoading(true);
    try {
      const res = await fetchAdminProducts();
      if (res.success) {
        setProducts(res.data);
      } else {
        onShowToast?.(res.error || 'Failed to load products from Firestore.', 'error');
      }
    } catch (err) {
      console.error('Error loading products:', err);
      onShowToast?.('Error reading Firestore products collection.', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadProducts();
  }, []);

  // Compute available brands
  const availableBrands = useMemo(() => {
    const set = new Set(['OnePlus', 'Samsung', 'Apple', 'Xiaomi', 'Realme', 'boAt', 'SK Mobile', 'Vivo', 'Oppo']);
    products.forEach(p => {
      if (p.brand) set.add(p.brand);
    });
    return Array.from(set);
  }, [products]);

  // Filtered Products
  const filteredProducts = useMemo(() => {
    return products.filter(p => {
      const q = searchQuery.toLowerCase().trim();
      const matchesSearch = !q || 
        (p.name || '').toLowerCase().includes(q) ||
        (p.brand || '').toLowerCase().includes(q) ||
        (p.id || '').toLowerCase().includes(q);

      const pCat = (p.category || '').toLowerCase();
      let matchesCat = true;
      if (categoryFilter === 'smartphones') {
        matchesCat = pCat.includes('smart') || pCat.includes('phone') || pCat.includes('mobile');
      } else if (categoryFilter === 'accessories') {
        matchesCat = pCat.includes('accessori') || pCat.includes('charger') || pCat.includes('cable') || pCat.includes('audio');
      }

      const matchesBrand = brandFilter === 'ALL' || (p.brand || '').toLowerCase() === brandFilter.toLowerCase();

      let matchesStock = true;
      const stockCount = typeof p.stock === 'number' ? p.stock : (p.inStock ? 5 : 0);
      if (stockFilter === 'in_stock') {
        matchesStock = stockCount > 0 && p.inStock !== false;
      } else if (stockFilter === 'out_of_stock') {
        matchesStock = stockCount <= 0 || p.inStock === false;
      }

      return matchesSearch && matchesCat && matchesBrand && matchesStock;
    });
  }, [products, searchQuery, categoryFilter, brandFilter, stockFilter]);

  const handleOpenAdd = () => {
    setEditingProduct(null);
    setFormData({
      name: '',
      brand: 'OnePlus',
      category: 'smartphones',
      price: '',
      originalPrice: '',
      stock: 5,
      description: '',
      imageUrl: '',
      badge: '',
      isFeatured: false,
      isBestSeller: false,
      hasWarranty: false,
      warrantyPreset: '1 Year',
      customDuration: '',
      warrantyDuration: '',
      warrantyDescription: '',
      specsText: 'Display: 6.7" AMOLED\nProcessor: Snapdragon 7 Gen 3\nCamera: 50MP Sony OIS\nBattery: 5500 mAh'
    });
    setShowModal(true);
  };

  const handleOpenEdit = (prod) => {
    setEditingProduct(prod);
    let specsStr = '';
    if (prod.specs && typeof prod.specs === 'object') {
      specsStr = Object.entries(prod.specs).map(([k, v]) => `${k}: ${v}`).join('\n');
    }

    const hasW = Boolean(prod.hasWarranty);
    let preset = '1 Year';
    let custom = '';
    if (prod.warrantyDuration) {
      if (['7 Days', '30 Days', '3 Months', '6 Months', '1 Year', '2 Years'].includes(prod.warrantyDuration)) {
        preset = prod.warrantyDuration;
      } else {
        preset = 'Custom';
        custom = prod.warrantyDuration;
      }
    }

    setFormData({
      name: prod.name || '',
      brand: prod.brand || 'OnePlus',
      category: (prod.category || 'smartphones').toLowerCase().includes('accessori') ? 'accessories' : 'smartphones',
      price: prod.price || '',
      originalPrice: prod.originalPrice || '',
      stock: prod.stock !== undefined ? prod.stock : (prod.inStock ? 5 : 0),
      description: prod.description || '',
      imageUrl: prod.imageUrl || prod.image || '',
      badge: prod.badge || '',
      isFeatured: Boolean(prod.isFeatured),
      isBestSeller: Boolean(prod.isBestSeller),
      hasWarranty: hasW,
      warrantyPreset: preset,
      customDuration: custom,
      warrantyDuration: prod.warrantyDuration || '',
      warrantyDescription: prod.warrantyDescription || '',
      specsText: specsStr
    });
    setShowModal(true);
  };

  // Convert specsText (key: value per line) to JSON object
  const parseSpecs = (text) => {
    const specs = {};
    if (!text) return specs;
    const lines = text.split('\n');
    lines.forEach(line => {
      const idx = line.indexOf(':');
      if (idx !== -1) {
        const k = line.slice(0, idx).trim();
        const v = line.slice(idx + 1).trim();
        if (k && v) specs[k] = v;
      }
    });
    return specs;
  };

  // Handle local image file upload & convert to compact data URL
  const handleFileUpload = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (file.size > 2 * 1024 * 1024) {
      onShowToast?.('Please choose an image under 2MB for fast loading.', 'error');
      return;
    }

    const reader = new FileReader();
    reader.onload = (event) => {
      const img = new Image();
      img.onload = () => {
        // Resize canvas to max 400x400 for compact Firestore storage
        const canvas = document.createElement('canvas');
        let width = img.width;
        let height = img.height;
        const maxDim = 400;

        if (width > height && width > maxDim) {
          height = Math.round((height * maxDim) / width);
          width = maxDim;
        } else if (height > maxDim) {
          width = Math.round((width * maxDim) / height);
          height = maxDim;
        }

        canvas.width = width;
        canvas.height = height;
        const ctx = canvas.getContext('2d');
        ctx.drawImage(img, 0, 0, width, height);
        const compressedDataUrl = canvas.toDataURL('image/jpeg', 0.85);

        setFormData(prev => ({ ...prev, imageUrl: compressedDataUrl }));
        onShowToast?.('Product image loaded and optimized!', 'success');
      };
      img.src = event.target.result;
    };
    reader.readAsDataURL(file);
  };

  const handleSaveProduct = async (e) => {
    e.preventDefault();
    if (!formData.name.trim()) {
      onShowToast?.('Please enter a product name.', 'error');
      return;
    }
    if (!formData.brand.trim()) {
      onShowToast?.('Please specify a brand name.', 'error');
      return;
    }
    const priceNum = parseFloat(formData.price);
    if (isNaN(priceNum) || priceNum <= 0) {
      onShowToast?.('Please enter a valid selling price greater than ₹0.', 'error');
      return;
    }

    setIsSubmitting(true);
    try {
      const effectiveWarrantyDuration = formData.hasWarranty
        ? (formData.warrantyPreset === 'Custom' ? (formData.customDuration || '').trim() : formData.warrantyPreset)
        : '';

      const payload = {
        name: formData.name.trim(),
        brand: formData.brand.trim(),
        category: formData.category,
        price: priceNum,
        originalPrice: formData.originalPrice ? parseFloat(formData.originalPrice) : null,
        stock: parseInt(formData.stock, 10) >= 0 ? parseInt(formData.stock, 10) : 0,
        inStock: parseInt(formData.stock, 10) > 0,
        description: formData.description.trim(),
        imageUrl: formData.imageUrl.trim(),
        badge: formData.badge.trim(),
        isFeatured: formData.isFeatured,
        isBestSeller: formData.isBestSeller,
        hasWarranty: Boolean(formData.hasWarranty),
        warrantyDuration: effectiveWarrantyDuration,
        warrantyDescription: formData.hasWarranty ? (formData.warrantyDescription || '').trim() : '',
        specs: parseSpecs(formData.specsText)
      };

      if (editingProduct && editingProduct.id) {
        payload.id = editingProduct.id;
        payload.createdAt = editingProduct.createdAt || null;
      }

      const result = await saveAdminProductToFirestore(payload, currentUser);
      onShowToast?.(
        editingProduct 
          ? `Product "${payload.name}" updated successfully in Firestore!` 
          : `New product "${payload.name}" added to Firestore catalogue!`,
        'success'
      );
      setShowModal(false);
      await loadProducts();
    } catch (err) {
      console.error('Failed to save product:', err);
      onShowToast?.(err.message || 'Error saving product to Firestore.', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleOpenQuickWarranty = (prod) => {
    let preset = '1 Year';
    let custom = '';
    if (prod.warrantyDuration) {
      if (['7 Days', '30 Days', '3 Months', '6 Months', '1 Year', '2 Years'].includes(prod.warrantyDuration)) {
        preset = prod.warrantyDuration;
      } else {
        preset = 'Custom';
        custom = prod.warrantyDuration;
      }
    }
    setQuickWarrantyProduct(prod);
    setQuickWarrantyData({
      preset,
      custom,
      description: prod.warrantyDescription || ''
    });
  };

  const handleSaveQuickWarranty = async (e) => {
    if (e) e.preventDefault();
    if (!quickWarrantyProduct) return;
    const finalDuration = quickWarrantyData.preset === 'Custom'
      ? (quickWarrantyData.custom || '').trim()
      : quickWarrantyData.preset;

    if (!finalDuration) {
      onShowToast?.('Please specify a warranty duration.', 'error');
      return;
    }

    setIsSubmitting(true);
    try {
      await quickUpdateProductWarranty(
        quickWarrantyProduct.id,
        true,
        finalDuration,
        quickWarrantyData.description.trim(),
        currentUser
      );
      onShowToast?.(`Warranty (${finalDuration}) saved for "${quickWarrantyProduct.name}"!`, 'success');
      setQuickWarrantyProduct(null);
      await loadProducts();
    } catch (err) {
      console.error('Quick warranty save error:', err);
      onShowToast?.(err.message || 'Failed to update warranty in Firestore.', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleConfirmRemoveWarranty = async () => {
    if (!warrantyRemoveProduct) return;
    setIsSubmitting(true);
    try {
      await quickUpdateProductWarranty(
        warrantyRemoveProduct.id,
        false,
        '',
        '',
        currentUser
      );
      onShowToast?.(`Warranty removed from "${warrantyRemoveProduct.name}".`, 'info');
      setWarrantyRemoveProduct(null);
      await loadProducts();
    } catch (err) {
      console.error('Remove warranty error:', err);
      onShowToast?.(err.message || 'Failed to remove warranty in Firestore.', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleConfirmDelete = async () => {
    if (!deleteConfirmProduct) return;
    setIsSubmitting(true);
    try {
      await deleteAdminProductFromFirestore(deleteConfirmProduct.id, currentUser);
      onShowToast?.(`Deleted "${deleteConfirmProduct.name}" from store catalogue.`, 'info');
      setDeleteConfirmProduct(null);
      await loadProducts();
    } catch (err) {
      console.error('Failed to delete product:', err);
      onShowToast?.(err.message || 'Error deleting product from Firestore.', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleQuickToggleStock = async (prod) => {
    const currentStock = typeof prod.stock === 'number' ? prod.stock : (prod.inStock ? 5 : 0);
    const newStock = currentStock > 0 ? 0 : 5;
    try {
      await saveAdminProductToFirestore({
        ...prod,
        stock: newStock,
        inStock: newStock > 0
      }, currentUser);
      setProducts(prev => prev.map(item => item.id === prod.id ? { ...item, stock: newStock, inStock: newStock > 0 } : item));
      onShowToast?.(
        newStock > 0 ? `Marked "${prod.name}" as In Stock (5 units).` : `Marked "${prod.name}" as Out of Stock.`,
        'success'
      );
    } catch (err) {
      console.error('Failed to update stock:', err);
      onShowToast?.('Failed to update stock in Firestore.', 'error');
    }
  };

  const handleSeedDefaultCatalog = async () => {
    setIsSeeding(true);
    try {
      const combined = [
        ...REAL_PRODUCTS.map(p => ({ ...p, category: 'smartphones', stock: 5, inStock: true })),
        ...ACCESSORIES.map(a => ({ ...a, category: 'accessories', stock: 10, inStock: true }))
      ];
      const res = await seedDefaultProductsToFirestore(currentUser, combined);
      onShowToast?.(`Imported ${res.seededCount} initial store products into Firestore!`, 'success');
      await loadProducts();
    } catch (err) {
      console.error('Seeding error:', err);
      onShowToast?.('Error seeding default catalogue to Firestore.', 'error');
    } finally {
      setIsSeeding(false);
    }
  };

  // Metrics
  const countInStock = products.filter(p => (typeof p.stock === 'number' ? p.stock > 0 : p.inStock !== false)).length;
  const countOutOfStock = products.filter(p => (typeof p.stock === 'number' ? p.stock <= 0 : p.inStock === false)).length;

  return (
    <div className="space-y-6">
      {/* Top Banner & Action Controls */}
      <div className="bg-[#141414] border border-[#2E2E2E] rounded-2xl p-6 relative overflow-hidden space-y-4">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#D4AF37]/15 border border-[#D4AF37]/30 text-xs font-bold text-[#D4AF37] mb-1">
              <Database size={13} />
              <span>Firestore Collection: /products</span>
            </div>
            <h2 className="text-xl sm:text-2xl font-black text-white flex items-center gap-2">
              <Package size={22} className="text-[#D4AF37]" />
              <span>Store Product & Inventory Management</span>
            </h2>
            <p className="text-xs text-gray-400 mt-1">
              Add, edit, and organize smartphones and accessories. All changes sync in real-time to customer pages.
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-2.5">
            <button
              onClick={handleOpenAdd}
              className="py-2.5 px-4 rounded-xl bg-[#D4AF37] hover:bg-[#C29F2F] text-black font-extrabold text-xs sm:text-sm flex items-center gap-2 shadow-lg shadow-[#D4AF37]/20 transition"
            >
              <Plus size={16} />
              <span>Add New Product</span>
            </button>

            {products.length === 0 && (
              <button
                onClick={handleSeedDefaultCatalog}
                disabled={isSeeding}
                className="py-2.5 px-3.5 rounded-xl bg-purple-600/20 hover:bg-purple-600/30 text-purple-300 border border-purple-500/40 text-xs font-bold transition flex items-center gap-1.5"
              >
                <Sparkles size={14} className={isSeeding ? 'animate-spin' : ''} />
                <span>Seed Default Store Catalog</span>
              </button>
            )}

            <button
              onClick={loadProducts}
              disabled={isLoading}
              className="py-2.5 px-3 rounded-xl bg-[#222222] hover:bg-[#2A2A2A] text-xs font-semibold text-gray-300 transition flex items-center gap-1.5"
            >
              <RefreshCw size={14} className={isLoading ? 'animate-spin text-[#D4AF37]' : ''} />
              <span>Refresh</span>
            </button>

            <button
              onClick={() => setActivePage?.('products')}
              className="py-2.5 px-3 rounded-xl border border-[#333333] hover:border-[#D4AF37] text-xs font-semibold text-gray-300 hover:text-white transition flex items-center gap-1.5"
            >
              <ExternalLink size={14} />
              <span>View Customer Store</span>
            </button>
          </div>
        </div>

        {/* Quick KPI Badges */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-4 border-t border-[#222222]">
          <div className="p-3 rounded-xl bg-[#0E0E0E] border border-[#222222]">
            <span className="text-[10px] uppercase font-bold text-gray-400 block mb-0.5">Total Products</span>
            <span className="text-xl font-black text-white">{products.length}</span>
          </div>

          <div className="p-3 rounded-xl bg-[#0E0E0E] border border-[#222222]">
            <span className="text-[10px] uppercase font-bold text-gray-400 block mb-0.5">In Stock</span>
            <span className="text-xl font-black text-emerald-400">{countInStock}</span>
          </div>

          <div className="p-3 rounded-xl bg-[#0E0E0E] border border-[#222222]">
            <span className="text-[10px] uppercase font-bold text-gray-400 block mb-0.5">Out of Stock</span>
            <span className={`text-xl font-black ${countOutOfStock > 0 ? 'text-red-400' : 'text-gray-500'}`}>
              {countOutOfStock}
            </span>
          </div>

          <div className="p-3 rounded-xl bg-[#0E0E0E] border border-[#222222]">
            <span className="text-[10px] uppercase font-bold text-gray-400 block mb-0.5">Active Categories</span>
            <span className="text-xl font-black text-[#D4AF37]">Smartphones & Accessories</span>
          </div>
        </div>
      </div>

      {/* Search & Filter Toolbar */}
      <div className="bg-[#111111] p-3.5 rounded-xl border border-[#222222] flex flex-col md:flex-row items-stretch md:items-center justify-between gap-3">
        <div className="relative flex-1">
          <Search size={15} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-500" />
          <input
            type="text"
            placeholder="Search products by name, brand, or model ID..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full bg-[#0A0A0A] border border-[#262626] rounded-lg pl-9 pr-4 py-2 text-xs text-white focus:outline-none focus:border-[#D4AF37]"
          />
        </div>

        <div className="flex flex-wrap items-center gap-2">
          {/* Category Filter */}
          <select
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
            className="bg-[#0A0A0A] border border-[#262626] rounded-lg px-2.5 py-2 text-xs text-white focus:outline-none focus:border-[#D4AF37]"
          >
            <option value="ALL">All Categories</option>
            <option value="smartphones">Smartphones</option>
            <option value="accessories">Accessories</option>
          </select>

          {/* Brand Filter */}
          <select
            value={brandFilter}
            onChange={(e) => setBrandFilter(e.target.value)}
            className="bg-[#0A0A0A] border border-[#262626] rounded-lg px-2.5 py-2 text-xs text-white focus:outline-none focus:border-[#D4AF37]"
          >
            <option value="ALL">All Brands</option>
            {availableBrands.map(b => (
              <option key={b} value={b}>{b}</option>
            ))}
          </select>

          {/* Stock Filter */}
          <select
            value={stockFilter}
            onChange={(e) => setStockFilter(e.target.value)}
            className="bg-[#0A0A0A] border border-[#262626] rounded-lg px-2.5 py-2 text-xs text-white focus:outline-none focus:border-[#D4AF37]"
          >
            <option value="ALL">All Stock Status</option>
            <option value="in_stock">In Stock (&gt; 0)</option>
            <option value="out_of_stock">Out of Stock (= 0)</option>
          </select>
        </div>
      </div>

      {/* Product Inventory Table / Cards */}
      {isLoading ? (
        <div className="p-16 text-center text-gray-400 text-xs">
          <RefreshCw size={26} className="animate-spin text-[#D4AF37] mx-auto mb-2" />
          <span>Synchronizing live products from Firestore...</span>
        </div>
      ) : filteredProducts.length === 0 ? (
        <div className="p-12 text-center bg-[#141414] border border-[#222222] rounded-2xl space-y-4">
          <Package size={36} className="text-gray-600 mx-auto" />
          <div>
            <h3 className="text-base font-bold text-white">No products found</h3>
            <p className="text-xs text-gray-400 mt-1 max-w-md mx-auto">
              {searchQuery || categoryFilter !== 'ALL' || brandFilter !== 'ALL' || stockFilter !== 'ALL'
                ? "No products matched your search filters. Try clearing your filters."
                : "Your Firestore products collection is currently empty."}
            </p>
          </div>
          <div className="flex justify-center gap-3 pt-2">
            <button
              onClick={handleOpenAdd}
              className="gold-btn !text-xs !py-2 !px-4"
            >
              <Plus size={14} />
              <span>Add First Product</span>
            </button>
            {products.length === 0 && (
              <button
                onClick={handleSeedDefaultCatalog}
                disabled={isSeeding}
                className="py-2 px-4 rounded-xl bg-purple-600/20 hover:bg-purple-600/30 text-purple-300 border border-purple-500/40 text-xs font-bold transition flex items-center gap-1.5"
              >
                <Sparkles size={14} className={isSeeding ? 'animate-spin' : ''} />
                <span>Import Default Catalogue</span>
              </button>
            )}
          </div>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredProducts.map((p) => {
            const stockCount = typeof p.stock === 'number' ? p.stock : (p.inStock ? 5 : 0);
            const isOutOfStock = stockCount <= 0 || p.inStock === false;
            const discountPercent = p.originalPrice && p.originalPrice > p.price
              ? Math.round(((p.originalPrice - p.price) / p.originalPrice) * 100)
              : 0;

            return (
              <div 
                key={p.id}
                className={`bg-[#141414] border rounded-xl p-4 flex flex-col justify-between transition-all ${
                  isOutOfStock ? 'border-red-900/30 bg-[#160E0E]' : 'border-[#262626] hover:border-[#D4AF37]/40'
                }`}
              >
                {/* Header & Badges */}
                <div>
                  <div className="flex items-center justify-between gap-2 mb-2">
                    <span className="text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded bg-[#222222] text-[#D4AF37] border border-[#333333]">
                      {p.category.includes('smart') || p.category.includes('phone') ? 'Smartphone' : 'Accessory'}
                    </span>

                    <div className="flex items-center gap-1.5">
                      {p.badge && (
                        <span className="text-[9px] uppercase font-extrabold px-1.5 py-0.5 rounded bg-[#D4AF37]/20 text-[#D4AF37] border border-[#D4AF37]/30">
                          {p.badge}
                        </span>
                      )}
                      {discountPercent > 0 && (
                        <span className="text-[9px] font-extrabold px-1.5 py-0.5 rounded bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
                          {discountPercent}% OFF
                        </span>
                      )}
                    </div>
                  </div>

                  {/* Thumbnail & Title */}
                  <div className="flex items-start gap-3 mb-3">
                    <div className="w-16 h-16 rounded-lg bg-[#0E0E0E] border border-[#262626] flex items-center justify-center p-1 shrink-0 overflow-hidden">
                      {p.imageUrl ? (
                        <img 
                          src={p.imageUrl} 
                          alt={p.name} 
                          className="w-full h-full object-contain"
                          onError={(e) => {
                            e.currentTarget.style.display = 'none';
                            e.currentTarget.nextElementSibling?.classList.remove('hidden');
                          }}
                        />
                      ) : null}
                      <Tag size={20} className={`text-gray-500 ${p.imageUrl ? 'hidden' : ''}`} />
                    </div>

                    <div className="overflow-hidden flex-1">
                      <span className="text-[10px] text-gray-500 uppercase font-semibold block">{p.brand}</span>
                      <h4 className="text-sm font-bold text-white truncate" title={p.name}>{p.name}</h4>
                      <span className="text-[10px] font-mono text-gray-500">ID: {p.id}</span>
                    </div>
                  </div>

                  {/* Pricing & Stock Details */}
                  <div className="grid grid-cols-2 gap-2 p-2.5 rounded-lg bg-[#0E0E0E] border border-[#1E1E1E] text-xs mb-3">
                    <div>
                      <span className="text-[10px] text-gray-500 block">Selling Price</span>
                      <span className="font-extrabold text-[#D4AF37] text-sm">₹{p.price.toLocaleString('en-IN')}</span>
                      {p.originalPrice && p.originalPrice > p.price && (
                        <span className="text-[10px] text-gray-500 line-through block">
                          ₹{p.originalPrice.toLocaleString('en-IN')}
                        </span>
                      )}
                    </div>

                    <div>
                      <span className="text-[10px] text-gray-500 block">Inventory Stock</span>
                      {isOutOfStock ? (
                        <span className="text-xs font-bold text-red-400 flex items-center gap-1">
                          <span className="w-1.5 h-1.5 rounded-full bg-red-500 animate-pulse"></span>
                          <span>Out of Stock (0)</span>
                        </span>
                      ) : (
                        <span className="text-xs font-bold text-emerald-400 flex items-center gap-1">
                          <span className="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
                          <span>In Stock ({stockCount})</span>
                        </span>
                      )}
                      <button
                        onClick={() => handleQuickToggleStock(p)}
                        className="text-[10px] text-gray-400 hover:text-[#D4AF37] underline block mt-0.5"
                      >
                        {isOutOfStock ? 'Set in stock' : 'Set out of stock'}
                      </button>
                    </div>
                  </div>

                  {/* Warranty Status & Quick Action Row */}
                  <div className="flex items-center justify-between gap-2 p-2 rounded-lg bg-[#0E0E0E] border border-[#1E1E1E] mb-3 text-xs">
                    <div className="flex items-center gap-1.5 overflow-hidden">
                      {p.hasWarranty && p.warrantyDuration ? (
                        <span className="text-[10px] font-bold text-[#D4AF37] bg-[#D4AF37]/15 border border-[#D4AF37]/30 px-2 py-0.5 rounded flex items-center gap-1 truncate">
                          <ShieldCheck size={11} className="text-[#D4AF37] shrink-0" />
                          <span>{p.warrantyDuration}</span>
                        </span>
                      ) : (
                        <span className="text-[10px] text-gray-500 bg-[#141414] border border-[#222222] px-2 py-0.5 rounded flex items-center gap-1">
                          <ShieldOff size={11} className="text-gray-500 shrink-0" />
                          <span>No Warranty</span>
                        </span>
                      )}
                    </div>

                    <div className="flex items-center gap-1.5 shrink-0">
                      {p.hasWarranty ? (
                        <div className="flex items-center gap-1">
                          <button
                            type="button"
                            onClick={() => handleOpenQuickWarranty(p)}
                            className="text-[10px] text-[#D4AF37] hover:text-yellow-300 font-semibold px-1.5 py-0.5 rounded hover:bg-[#D4AF37]/10 transition flex items-center gap-0.5"
                            title="Edit Warranty"
                          >
                            <Shield size={10} />
                            <span>Edit</span>
                          </button>
                          <button
                            type="button"
                            onClick={() => setWarrantyRemoveProduct(p)}
                            className="text-[10px] text-red-400 hover:text-red-300 font-semibold px-1.5 py-0.5 rounded bg-red-500/10 hover:bg-red-500/20 border border-red-500/20 transition flex items-center gap-0.5"
                            title="Remove Warranty"
                          >
                            <ShieldOff size={10} />
                            <span>Remove</span>
                          </button>
                        </div>
                      ) : (
                        <button
                          type="button"
                          onClick={() => handleOpenQuickWarranty(p)}
                          className="text-[10px] text-emerald-400 hover:text-emerald-300 font-semibold px-2 py-0.5 rounded bg-emerald-500/10 hover:bg-emerald-500/20 border border-emerald-500/30 transition flex items-center gap-1"
                          title="Add Warranty"
                        >
                          <ShieldCheck size={10} />
                          <span>Add Warranty</span>
                        </button>
                      )}
                    </div>
                  </div>
                </div>

                {/* Card Action Buttons */}
                <div className="pt-2 border-t border-[#222222] flex items-center justify-between gap-2">
                  <div className="text-[11px] text-gray-500">
                    Rating: <strong className="text-gray-300">{p.rating || 4.7}★</strong>
                  </div>

                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => handleOpenEdit(p)}
                      className="py-1.5 px-3 rounded-lg bg-[#222222] hover:bg-[#2C2C2C] text-xs font-semibold text-gray-200 hover:text-white transition flex items-center gap-1"
                    >
                      <Edit2 size={12} className="text-[#D4AF37]" />
                      <span>Edit</span>
                    </button>

                    <button
                      onClick={() => setDeleteConfirmProduct(p)}
                      className="py-1.5 px-2.5 rounded-lg bg-red-500/10 hover:bg-red-500/20 text-red-400 border border-red-500/30 text-xs font-semibold transition flex items-center gap-1"
                      title="Delete Product"
                    >
                      <Trash2 size={12} />
                    </button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* ----------------- MODAL 1: ADD / EDIT PRODUCT ----------------- */}
      {showModal && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4 overflow-y-auto">
          <div className="bg-[#141414] border border-[#2E2E2E] rounded-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto shadow-2xl p-6 sm:p-8 space-y-6 relative">
            <div className="flex items-center justify-between border-b border-[#222222] pb-4">
              <div>
                <h3 className="text-lg font-black text-white flex items-center gap-2">
                  {editingProduct ? <Edit2 size={18} className="text-[#D4AF37]" /> : <Plus size={18} className="text-[#D4AF37]" />}
                  <span>{editingProduct ? `Edit Product: ${editingProduct.name}` : 'Add New Store Product'}</span>
                </h3>
                <p className="text-xs text-gray-400 mt-0.5">
                  Firestore Target: <code className="text-[#D4AF37]">/products/{editingProduct?.id || '(new ID)'}</code>
                </p>
              </div>

              <button
                onClick={() => setShowModal(false)}
                className="p-1.5 rounded-lg text-gray-400 hover:text-white hover:bg-white/5"
              >
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleSaveProduct} className="space-y-4">
              {/* Product Name */}
              <div>
                <label className="block text-xs font-semibold text-gray-300 mb-1">
                  Product Name *
                </label>
                <input
                  type="text"
                  required
                  placeholder="e.g. OnePlus 12R 5G (16GB RAM, 256GB)"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3.5 py-2.5 text-xs text-white focus:outline-none focus:border-[#D4AF37]"
                />
              </div>

              {/* Brand & Category */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-300 mb-1">
                    Brand *
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. OnePlus, Samsung, Apple, boAt"
                    value={formData.brand}
                    onChange={(e) => setFormData({ ...formData, brand: e.target.value })}
                    className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3.5 py-2.5 text-xs text-white focus:outline-none focus:border-[#D4AF37]"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-300 mb-1">
                    Store Category *
                  </label>
                  <select
                    value={formData.category}
                    onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                    className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3.5 py-2.5 text-xs text-white focus:outline-none focus:border-[#D4AF37]"
                  >
                    <option value="smartphones">Smartphones (Featured on Products Page)</option>
                    <option value="accessories">Accessories (Featured on Accessories Page)</option>
                  </select>
                </div>
              </div>

              {/* Price, Original Price, Stock */}
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-300 mb-1">
                    Selling Price (₹) *
                  </label>
                  <input
                    type="number"
                    required
                    min="1"
                    placeholder="39999"
                    value={formData.price}
                    onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                    className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3.5 py-2.5 text-xs text-white focus:outline-none focus:border-[#D4AF37]"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-300 mb-1">
                    Original Price (₹) <span className="text-gray-500 font-normal">(Optional)</span>
                  </label>
                  <input
                    type="number"
                    min="0"
                    placeholder="45999"
                    value={formData.originalPrice}
                    onChange={(e) => setFormData({ ...formData, originalPrice: e.target.value })}
                    className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3.5 py-2.5 text-xs text-white focus:outline-none focus:border-[#D4AF37]"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-300 mb-1">
                    Stock Quantity (Units) *
                  </label>
                  <input
                    type="number"
                    required
                    min="0"
                    placeholder="5"
                    value={formData.stock}
                    onChange={(e) => setFormData({ ...formData, stock: e.target.value })}
                    className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3.5 py-2.5 text-xs text-white focus:outline-none focus:border-[#D4AF37]"
                  />
                </div>
              </div>

              {/* Badges & Toggles */}
              <div className="p-3 rounded-xl bg-[#0E0E0E] border border-[#222222] space-y-3">
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 items-center">
                  <div>
                    <label className="block text-[11px] font-semibold text-gray-400 mb-1">
                      Custom Badge Label
                    </label>
                    <input
                      type="text"
                      placeholder="e.g. Best Seller, Hot Deal"
                      value={formData.badge}
                      onChange={(e) => setFormData({ ...formData, badge: e.target.value })}
                      className="w-full bg-[#141414] border border-[#2A2A2A] rounded-lg px-3 py-1.5 text-xs text-white focus:outline-none focus:border-[#D4AF37]"
                    />
                  </div>

                  <div className="flex items-center gap-2 pt-4">
                    <input
                      type="checkbox"
                      id="feat_toggle"
                      checked={formData.isFeatured}
                      onChange={(e) => setFormData({ ...formData, isFeatured: e.target.checked })}
                      className="rounded border-[#333333] text-[#D4AF37] focus:ring-[#D4AF37]"
                    />
                    <label htmlFor="feat_toggle" className="text-xs text-gray-300 cursor-pointer font-medium">
                      Featured Product
                    </label>
                  </div>

                  <div className="flex items-center gap-2 pt-4">
                    <input
                      type="checkbox"
                      id="bestseller_toggle"
                      checked={formData.isBestSeller}
                      onChange={(e) => setFormData({ ...formData, isBestSeller: e.target.checked })}
                      className="rounded border-[#333333] text-[#D4AF37] focus:ring-[#D4AF37]"
                    />
                    <label htmlFor="bestseller_toggle" className="text-xs text-gray-300 cursor-pointer font-medium">
                      Best Seller Flag
                    </label>
                  </div>
                </div>
              </div>

              {/* Warranty Management Section */}
              <div className="p-4 rounded-xl bg-[#0E0E0E] border border-[#2E2E2E] space-y-3">
                <div className="flex items-center justify-between border-b border-[#1E1E1E] pb-2.5">
                  <div className="flex items-center gap-2">
                    <ShieldCheck size={16} className="text-[#D4AF37]" />
                    <span className="text-xs font-bold text-white">Warranty Management</span>
                  </div>

                  <div className="flex items-center gap-1.5">
                    <button
                      type="button"
                      onClick={() => {
                        if (formData.hasWarranty) {
                          setFormData(prev => ({
                            ...prev,
                            hasWarranty: false,
                            warrantyDuration: '',
                            warrantyDescription: ''
                          }));
                          onShowToast?.('Warranty disabled for this product.', 'info');
                        } else {
                          setFormData(prev => ({
                            ...prev,
                            hasWarranty: true,
                            warrantyPreset: prev.warrantyPreset || '1 Year'
                          }));
                          onShowToast?.('Warranty enabled for this product.', 'success');
                        }
                      }}
                      className={`text-xs font-semibold px-3 py-1 rounded-lg border transition flex items-center gap-1.5 ${
                        formData.hasWarranty
                          ? 'bg-[#D4AF37]/20 border-[#D4AF37] text-[#D4AF37]'
                          : 'bg-[#1C1C1C] border-[#333333] text-gray-400 hover:text-white'
                      }`}
                    >
                      {formData.hasWarranty ? (
                        <>
                          <ShieldCheck size={13} />
                          <span>Warranty: Enabled</span>
                        </>
                      ) : (
                        <>
                          <ShieldOff size={13} />
                          <span>Warranty: Disabled</span>
                        </>
                      )}
                    </button>
                  </div>
                </div>

                {formData.hasWarranty ? (
                  <div className="space-y-3 pt-1">
                    {/* Duration Presets */}
                    <div>
                      <label className="block text-[11px] font-semibold text-gray-300 mb-1.5">
                        Warranty Duration *
                      </label>
                      <div className="flex flex-wrap gap-1.5">
                        {WARRANTY_PRESETS.map((preset) => (
                          <button
                            key={preset}
                            type="button"
                            onClick={() => setFormData(prev => ({ ...prev, warrantyPreset: preset }))}
                            className={`text-xs px-2.5 py-1 rounded-lg border font-medium transition ${
                              formData.warrantyPreset === preset
                                ? 'bg-[#D4AF37] text-black font-bold border-[#D4AF37] shadow-sm'
                                : 'bg-[#161616] border-[#2A2A2A] text-gray-300 hover:text-white hover:border-[#444444]'
                            }`}
                          >
                            {preset}
                          </button>
                        ))}
                      </div>
                    </div>

                    {/* Custom Duration Input */}
                    {formData.warrantyPreset === 'Custom' && (
                      <div>
                        <label className="block text-[11px] font-semibold text-gray-300 mb-1">
                          Custom Warranty Duration *
                        </label>
                        <input
                          type="text"
                          required
                          placeholder="e.g. 45 Days, 18 Months Replacement"
                          value={formData.customDuration}
                          onChange={(e) => setFormData({ ...formData, customDuration: e.target.value })}
                          className="w-full bg-[#141414] border border-[#2E2E2E] rounded-lg px-3 py-2 text-xs text-white focus:outline-none focus:border-[#D4AF37]"
                        />
                      </div>
                    )}

                    {/* Warranty Description */}
                    <div>
                      <label className="block text-[11px] font-semibold text-gray-300 mb-1">
                        Warranty Description / Coverage Terms <span className="text-gray-500 font-normal">(Optional)</span>
                      </label>
                      <textarea
                        rows={2}
                        placeholder="e.g. 100% official brand warranty covering manufacturing defects. Physical & liquid damage excluded."
                        value={formData.warrantyDescription}
                        onChange={(e) => setFormData({ ...formData, warrantyDescription: e.target.value })}
                        className="w-full bg-[#141414] border border-[#2E2E2E] rounded-lg px-3 py-1.5 text-xs text-white focus:outline-none focus:border-[#D4AF37]"
                      />
                    </div>

                    {/* Remove Warranty Button inside form */}
                    <div className="pt-1 flex items-center justify-between text-xs">
                      <span className="text-[11px] text-gray-400">
                        Active Coverage: <strong className="text-[#D4AF37]">{formData.warrantyPreset === 'Custom' ? (formData.customDuration || 'Custom') : formData.warrantyPreset}</strong>
                      </span>
                      <button
                        type="button"
                        onClick={() => {
                          setFormData(prev => ({
                            ...prev,
                            hasWarranty: false,
                            warrantyPreset: '1 Year',
                            customDuration: '',
                            warrantyDuration: '',
                            warrantyDescription: ''
                          }));
                          onShowToast?.('Warranty removed from this product.', 'info');
                        }}
                        className="text-[11px] text-red-400 hover:text-red-300 font-semibold flex items-center gap-1 hover:underline"
                      >
                        <Trash2 size={11} />
                        <span>Remove Warranty</span>
                      </button>
                    </div>
                  </div>
                ) : (
                  <div className="flex items-center justify-between p-2.5 rounded-lg bg-[#141414] border border-[#222222]">
                    <span className="text-[11px] text-gray-400">
                      No warranty configured. No warranty badge will appear on customer listings.
                    </span>
                    <button
                      type="button"
                      onClick={() => setFormData(prev => ({ ...prev, hasWarranty: true, warrantyPreset: '1 Year' }))}
                      className="text-xs font-bold text-[#D4AF37] hover:text-yellow-300 flex items-center gap-1 shrink-0"
                    >
                      <Plus size={13} />
                      <span>Add Warranty</span>
                    </button>
                  </div>
                )}
              </div>

              {/* Image Input Options */}
              <div className="space-y-2">
                <label className="block text-xs font-semibold text-gray-300">
                  Product Image (URL, Local Upload, or Preset)
                </label>
                <div className="flex gap-2">
                  <input
                    type="url"
                    placeholder="https://example.com/image.jpg"
                    value={formData.imageUrl}
                    onChange={(e) => setFormData({ ...formData, imageUrl: e.target.value })}
                    className="flex-1 bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3.5 py-2 text-xs text-white focus:outline-none focus:border-[#D4AF37]"
                  />
                  <label className="py-2 px-3 rounded-lg bg-[#222222] hover:bg-[#2C2C2C] text-xs font-semibold text-gray-300 hover:text-white cursor-pointer flex items-center gap-1.5 border border-[#333333]">
                    <Upload size={14} />
                    <span>Upload</span>
                    <input
                      type="file"
                      accept="image/*"
                      onChange={handleFileUpload}
                      className="hidden"
                    />
                  </label>
                </div>

                {/* Preset suggestions */}
                <div className="flex flex-wrap items-center gap-1.5 pt-1">
                  <span className="text-[10px] text-gray-500">Quick Presets:</span>
                  {PRESET_IMAGES.slice(0, 5).map((p, idx) => (
                    <button
                      key={idx}
                      type="button"
                      onClick={() => setFormData(prev => ({ ...prev, imageUrl: p.url, brand: prev.brand || p.brand }))}
                      className="text-[10px] px-2 py-0.5 rounded bg-[#181818] border border-[#2E2E2E] text-gray-400 hover:text-[#D4AF37] hover:border-[#D4AF37]/40 transition"
                    >
                      {p.name}
                    </button>
                  ))}
                </div>

                {/* Image Preview */}
                {formData.imageUrl && (
                  <div className="mt-2 p-2 rounded-lg bg-[#0E0E0E] border border-[#262626] flex items-center gap-3">
                    <img 
                      src={formData.imageUrl} 
                      alt="Preview" 
                      className="w-12 h-12 object-contain rounded bg-[#141414] border border-[#222222]" 
                    />
                    <div className="overflow-hidden flex-1 text-[11px] text-gray-400 truncate">
                      Image attached: <span className="text-gray-300">{formData.imageUrl.slice(0, 45)}...</span>
                    </div>
                    <button
                      type="button"
                      onClick={() => setFormData({ ...formData, imageUrl: '' })}
                      className="text-gray-500 hover:text-red-400 text-xs px-2"
                    >
                      Clear
                    </button>
                  </div>
                )}
              </div>

              {/* Description */}
              <div>
                <label className="block text-xs font-semibold text-gray-300 mb-1">
                  Product Description
                </label>
                <textarea
                  rows={2}
                  placeholder="Detailed highlights, warranty info, and box accessories..."
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3.5 py-2 text-xs text-white focus:outline-none focus:border-[#D4AF37]"
                />
              </div>

              {/* Specifications (key: value per line) */}
              <div>
                <label className="block text-xs font-semibold text-gray-300 mb-1">
                  Specifications <span className="text-gray-500 font-normal">(Format: Key: Value per line)</span>
                </label>
                <textarea
                  rows={3}
                  placeholder="Processor: Qualcomm Snapdragon 7 Gen 3&#10;Display: 6.7 inch AMOLED 120Hz&#10;Battery: 5500 mAh"
                  value={formData.specsText}
                  onChange={(e) => setFormData({ ...formData, specsText: e.target.value })}
                  className="w-full font-mono bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3.5 py-2 text-xs text-white focus:outline-none focus:border-[#D4AF37]"
                />
              </div>

              {/* Action Buttons */}
              <div className="pt-4 border-t border-[#222222] flex items-center justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="py-2.5 px-4 rounded-xl border border-[#333333] hover:border-[#555555] text-xs font-semibold text-gray-300 hover:text-white transition"
                >
                  Cancel
                </button>

                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="py-2.5 px-6 rounded-xl bg-[#D4AF37] hover:bg-[#C29F2F] text-black font-extrabold text-xs sm:text-sm flex items-center gap-2 shadow-lg shadow-[#D4AF37]/20 transition disabled:opacity-50"
                >
                  {isSubmitting ? (
                    <>
                      <RefreshCw size={14} className="animate-spin" />
                      <span>Saving to Firestore...</span>
                    </>
                  ) : (
                    <>
                      <CheckCircle2 size={16} />
                      <span>{editingProduct ? 'Save Changes' : 'Create Product'}</span>
                    </>
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ----------------- MODAL 2: CONFIRM DELETE ----------------- */}
      {deleteConfirmProduct && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-[#141414] border border-red-500/50 rounded-2xl w-full max-w-md p-6 sm:p-8 space-y-5 relative shadow-2xl">
            <div className="w-14 h-14 rounded-2xl bg-red-500/10 text-red-400 border border-red-500/30 flex items-center justify-center mx-auto">
              <AlertTriangle size={28} />
            </div>

            <div className="text-center space-y-1">
              <h3 className="text-lg font-black text-white">Delete Product Confirmation</h3>
              <p className="text-xs text-gray-400">
                Are you sure you want to permanently delete this product from the online store?
              </p>
            </div>

            <div className="p-3.5 rounded-xl bg-[#0E0E0E] border border-[#222222] text-xs space-y-1.5">
              <div className="flex justify-between text-gray-400">
                <span>Product Name:</span>
                <span className="text-white font-bold truncate max-w-[200px]">{deleteConfirmProduct.name}</span>
              </div>
              <div className="flex justify-between text-gray-400">
                <span>Firestore ID:</span>
                <span className="font-mono text-[#D4AF37]">{deleteConfirmProduct.id}</span>
              </div>
              <div className="flex justify-between text-gray-400">
                <span>Selling Price:</span>
                <span className="text-emerald-400 font-bold">₹{deleteConfirmProduct.price?.toLocaleString('en-IN')}</span>
              </div>
            </div>

            <div className="text-[11px] text-red-300/90 bg-red-950/20 p-2.5 rounded-lg border border-red-900/30">
              ⚠️ This will remove the product from customer searches and listings across the live website. This action cannot be undone.
            </div>

            <div className="flex items-center justify-end gap-3 pt-2">
              <button
                type="button"
                disabled={isSubmitting}
                onClick={() => setDeleteConfirmProduct(null)}
                className="py-2.5 px-4 rounded-xl border border-[#333333] hover:border-[#555555] text-xs font-semibold text-gray-300 hover:text-white transition"
              >
                Cancel
              </button>

              <button
                type="button"
                disabled={isSubmitting}
                onClick={handleConfirmDelete}
                className="py-2.5 px-5 rounded-xl bg-red-600 hover:bg-red-700 text-white font-bold text-xs flex items-center gap-1.5 transition disabled:opacity-50"
              >
                {isSubmitting ? (
                  <span>Deleting from Firestore...</span>
                ) : (
                  <>
                    <Trash2 size={14} />
                    <span>Delete Permanently</span>
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ----------------- MODAL 3: CONFIRM REMOVE WARRANTY ----------------- */}
      {warrantyRemoveProduct && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-[#141414] border border-amber-500/40 rounded-2xl w-full max-w-md p-6 sm:p-8 space-y-5 relative shadow-2xl">
            <div className="w-14 h-14 rounded-2xl bg-amber-500/10 text-amber-400 border border-amber-500/30 flex items-center justify-center mx-auto">
              <ShieldOff size={28} />
            </div>

            <div className="text-center space-y-1">
              <h3 className="text-lg font-black text-white">Remove Warranty Confirmation</h3>
              <p className="text-xs text-gray-400">
                Are you sure you want to remove warranty coverage from this product?
              </p>
            </div>

            <div className="p-3.5 rounded-xl bg-[#0E0E0E] border border-[#222222] text-xs space-y-1.5">
              <div className="flex justify-between text-gray-400">
                <span>Product Name:</span>
                <span className="text-white font-bold truncate max-w-[200px]">{warrantyRemoveProduct.name}</span>
              </div>
              <div className="flex justify-between text-gray-400">
                <span>Current Warranty:</span>
                <span className="text-[#D4AF37] font-semibold">{warrantyRemoveProduct.warrantyDuration || 'Active'}</span>
              </div>
              {warrantyRemoveProduct.warrantyDescription && (
                <div className="text-[11px] text-gray-500 border-t border-[#1E1E1E] pt-1">
                  Terms: {warrantyRemoveProduct.warrantyDescription}
                </div>
              )}
            </div>

            <div className="text-[11px] text-amber-300/90 bg-amber-950/20 p-2.5 rounded-lg border border-amber-900/30">
              ℹ️ The warranty badge and warranty text will be immediately hidden from customer product listings and detail pages.
            </div>

            <div className="flex items-center justify-end gap-3 pt-2">
              <button
                type="button"
                disabled={isSubmitting}
                onClick={() => setWarrantyRemoveProduct(null)}
                className="py-2.5 px-4 rounded-xl border border-[#333333] hover:border-[#555555] text-xs font-semibold text-gray-300 hover:text-white transition"
              >
                Keep Warranty
              </button>

              <button
                type="button"
                disabled={isSubmitting}
                onClick={handleConfirmRemoveWarranty}
                className="py-2.5 px-5 rounded-xl bg-red-600 hover:bg-red-700 text-white font-bold text-xs flex items-center gap-1.5 transition disabled:opacity-50"
              >
                {isSubmitting ? (
                  <span>Updating Firestore...</span>
                ) : (
                  <>
                    <ShieldOff size={14} />
                    <span>Confirm Remove Warranty</span>
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ----------------- MODAL 4: QUICK ADD / EDIT WARRANTY ----------------- */}
      {quickWarrantyProduct && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-[#141414] border border-[#2E2E2E] rounded-2xl w-full max-w-lg p-6 sm:p-8 space-y-5 relative shadow-2xl">
            <div className="flex items-center justify-between border-b border-[#222222] pb-3">
              <div className="flex items-center gap-2">
                <div className="p-2 rounded-lg bg-[#D4AF37]/15 text-[#D4AF37]">
                  <ShieldCheck size={20} />
                </div>
                <div>
                  <h3 className="text-base font-bold text-white">
                    {quickWarrantyProduct.hasWarranty ? 'Update Warranty' : 'Add Product Warranty'}
                  </h3>
                  <p className="text-xs text-gray-400 truncate max-w-xs">{quickWarrantyProduct.name}</p>
                </div>
              </div>
              <button
                onClick={() => setQuickWarrantyProduct(null)}
                className="p-1 rounded-lg text-gray-400 hover:text-white hover:bg-white/5"
              >
                <X size={18} />
              </button>
            </div>

            <form onSubmit={handleSaveQuickWarranty} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                  Select Warranty Duration *
                </label>
                <div className="flex flex-wrap gap-1.5">
                  {WARRANTY_PRESETS.map((preset) => (
                    <button
                      key={preset}
                      type="button"
                      onClick={() => setQuickWarrantyData(prev => ({ ...prev, preset }))}
                      className={`text-xs px-3 py-1.5 rounded-lg border font-medium transition ${
                        quickWarrantyData.preset === preset
                          ? 'bg-[#D4AF37] text-black font-bold border-[#D4AF37]'
                          : 'bg-[#181818] border-[#2A2A2A] text-gray-300 hover:text-white'
                      }`}
                    >
                      {preset}
                    </button>
                  ))}
                </div>
              </div>

              {quickWarrantyData.preset === 'Custom' && (
                <div>
                  <label className="block text-xs font-semibold text-gray-300 mb-1">
                    Custom Duration *
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. 45 Days, 18 Months Replacement"
                    value={quickWarrantyData.custom}
                    onChange={(e) => setQuickWarrantyData(prev => ({ ...prev, custom: e.target.value }))}
                    className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3 py-2 text-xs text-white focus:outline-none focus:border-[#D4AF37]"
                  />
                </div>
              )}

              <div>
                <label className="block text-xs font-semibold text-gray-300 mb-1">
                  Warranty Description / Coverage Details <span className="text-gray-500 font-normal">(Optional)</span>
                </label>
                <textarea
                  rows={2}
                  placeholder="e.g. 100% official brand warranty covering manufacturing defects. Physical & liquid damage excluded."
                  value={quickWarrantyData.description}
                  onChange={(e) => setQuickWarrantyData(prev => ({ ...prev, description: e.target.value }))}
                  className="w-full bg-[#0E0E0E] border border-[#2E2E2E] rounded-lg px-3 py-2 text-xs text-white focus:outline-none focus:border-[#D4AF37]"
                />
              </div>

              <div className="pt-3 border-t border-[#222222] flex items-center justify-end gap-3">
                <button
                  type="button"
                  disabled={isSubmitting}
                  onClick={() => setQuickWarrantyProduct(null)}
                  className="py-2.5 px-4 rounded-xl border border-[#333333] hover:border-[#555555] text-xs font-semibold text-gray-300 hover:text-white transition"
                >
                  Cancel
                </button>

                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="py-2.5 px-6 rounded-xl bg-[#D4AF37] hover:bg-[#C29F2F] text-black font-extrabold text-xs flex items-center gap-1.5 transition disabled:opacity-50"
                >
                  {isSubmitting ? (
                    <>
                      <RefreshCw size={13} className="animate-spin" />
                      <span>Saving...</span>
                    </>
                  ) : (
                    <>
                      <CheckCircle2 size={15} />
                      <span>Save Warranty</span>
                    </>
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
