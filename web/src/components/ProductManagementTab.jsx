import React, { useState, useEffect, useMemo } from 'react';
import { 
  fetchAdminProducts, 
  saveAdminProductToFirestore, 
  deleteAdminProductFromFirestore, 
  seedDefaultProductsToFirestore,
  quickUpdateProductWarranty,
  fetchCategoriesFromFirestore,
  subscribeToFirestoreProducts
} from '../firebase';
import { REAL_PRODUCTS, ACCESSORIES, DEFAULT_CATEGORIES } from '../data/storeData';
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

export const SPEC_TEMPLATES = {
  smartphones: [
    { key: 'Display', value: '6.7" FHD+ 120Hz Fluid AMOLED' },
    { key: 'Processor', value: 'Qualcomm Snapdragon 7 Gen 3 (4nm)' },
    { key: 'RAM', value: '8GB LPDDR4X' },
    { key: 'Storage', value: '128GB / 256GB UFS 3.1' },
    { key: 'Camera', value: '50MP Sony LYT-600 (OIS) + 8MP' },
    { key: 'Battery', value: '5500 mAh, 100W SUPERVOOC' },
    { key: 'Operating System', value: 'Android 14' },
    { key: 'Color', value: 'Midnight Black' },
    { key: 'Connectivity', value: '5G Dual SIM, Wi-Fi 6, BT 5.3' }
  ],
  'old-phones': [
    { key: 'Condition Grade', value: 'Grade A+ (Like New, zero scratches)' },
    { key: 'Battery Health', value: '92% Original Tested Battery' },
    { key: 'Display', value: '6.1" Super Retina OLED' },
    { key: 'Processor', value: 'Apple A14 Bionic / Snapdragon Flagship' },
    { key: 'Testing', value: '32-Point Technician Passed (SK Certified)' },
    { key: 'Included', value: 'Device + Fast Charging Cable + Store Warranty Slip' }
  ],
  chargers: [
    { key: 'Charging Power', value: '65W Max Power Delivery (GaN III)' },
    { key: 'Input', value: '100-240V ~ 50/60Hz 1.5A Universal' },
    { key: 'Output', value: 'Type-C: 65W Max, USB-A: 18W' },
    { key: 'Port Type', value: '1x Type-C + 1x USB-A Dual Port' },
    { key: 'Fast Charging', value: 'Supports VOOC, Warp, QC 4.0, PD 3.0 & PPS' },
    { key: 'Compatibility', value: 'OnePlus, Samsung, iPhone, Vivo, Xiaomi, Laptops' },
    { key: 'Cable Included', value: 'Adapter Only (Optional Type-C Cable)' }
  ],
  cables: [
    { key: 'Length', value: '1.5 Meters (Extra Long)' },
    { key: 'Connector Type', value: 'USB Type-C to Type-C' },
    { key: 'Charging Speed', value: '100W (20V/5A) Power Delivery' },
    { key: 'Data Transfer Speed', value: '480 Mbps High Speed Sync' },
    { key: 'Cable Material', value: 'Double Ballistic Nylon Braided' },
    { key: 'Bend Lifespan', value: '30,000+ Bends Lab Tested' }
  ],
  audio: [
    { key: 'Type', value: 'Wireless In-Ear Neckband / TWS Earbuds' },
    { key: 'Driver Size', value: '12.4mm Deep Bass Dynamic Drivers' },
    { key: 'Connectivity', value: 'Bluetooth v5.3 (10m Range)' },
    { key: 'Battery Life', value: 'Up to 30 Hours Playtime' },
    { key: 'Fast Charge', value: '10 mins charge = 20 hours playback' },
    { key: 'Latency', value: '45ms Ultra-Low Gaming Latency' },
    { key: 'Microphone', value: 'Dual Mic AI Noise Cancellation' },
    { key: 'Water Resistance', value: 'IP55 Water & Sweat Resistance' }
  ],
  speakers: [
    { key: 'Output Power', value: '16W RMS High-Fidelity Audio' },
    { key: 'Battery Capacity', value: '2600 mAh Built-in Rechargeable' },
    { key: 'Playtime', value: '12 Hours Continuous Music' },
    { key: 'Bluetooth Version', value: 'Bluetooth 5.3 + AUX Support' },
    { key: 'Waterproof Rating', value: 'IPX7 Waterproof Protection' }
  ],
  'power-banks': [
    { key: 'Battery Capacity', value: '20,000 mAh Li-Polymer' },
    { key: 'Number of Ports', value: '2x USB-A + 1x Type-C (3 Ports)' },
    { key: 'Output Power', value: '22.5W Fast Charging (QC + PD)' },
    { key: 'Fast Charging Protocols', value: 'QC 3.0, PD 3.0, AFC' },
    { key: 'Dimensions / Weight', value: 'Compact Pocket Size / 380g' }
  ],
  covers: [
    { key: 'Material', value: 'Shock-Resistant TPU + Acrylic Hybrid' },
    { key: 'Finish', value: 'Matte Frosted Anti-Fingerprint' },
    { key: 'Shock Protection', value: 'Air-Cushion Impact Absorbing Corners' },
    { key: 'Raised Edges', value: '1.5mm Screen Lip & 2.0mm Camera Ring' },
    { key: 'Wireless Charging', value: '100% Compatible with Qi & MagSafe' }
  ],
  'screen-protectors': [
    { key: 'Hardness', value: '9H Hardness Shatterproof Tempered Glass' },
    { key: 'Glass Type', value: '0.33mm 2.5D Curved Edge-to-Edge' },
    { key: 'Anti-Fingerprint Coating', value: 'Oleophobic Coating' },
    { key: 'Edge-to-Edge Protection', value: 'Complete Screen Coverage' },
    { key: 'Clarity', value: '99.9% Ultra HD Transparency & True Touch' }
  ]
};

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
  const [categoriesList, setCategoriesList] = useState(DEFAULT_CATEGORIES);

  // Active categories loaded dynamically from Firestore categories collection
  const activeCategories = useMemo(() => {
    return categoriesList.filter(c => c.isActive !== false);
  }, [categoriesList]);

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

  // Specs Editor View Mode: 'builder' | 'raw'
  const [specsEditorMode, setSpecsEditorMode] = useState('builder');

  // Form State - category is required and defaults to empty
  const [formData, setFormData] = useState({
    name: '',
    brand: 'OnePlus',
    category: '',
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
    specsList: [],
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

    try {
      const catRes = await fetchCategoriesFromFirestore();
      if (catRes.success && catRes.data && catRes.data.length > 0) {
        setCategoriesList(catRes.data);
      }
    } catch (e) {
      // fallback
    }
  };

  useEffect(() => {
    loadProducts();
    const unsubscribe = subscribeToFirestoreProducts((res) => {
      if (res && res.success && Array.isArray(res.data)) {
        setProducts(res.data);
      }
    });
    return () => {
      if (unsubscribe) unsubscribe();
    };
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

      const pCat = (p.category || '').toLowerCase().replace(/_/g, '-');
      let matchesCat = true;
      if (categoryFilter !== 'ALL') {
        const filterSlug = categoryFilter.toLowerCase().replace(/_/g, '-');
        if (filterSlug === 'smartphones') {
          matchesCat = pCat === 'smartphones' || pCat.includes('smart') || pCat.includes('phone') || pCat.includes('mobile');
        } else if (filterSlug === 'accessories') {
          matchesCat = pCat === 'accessories' || pCat.includes('accessori');
        } else if (filterSlug === 'old-phones') {
          matchesCat = pCat.includes('old') || p.isPreOwned;
        } else if (filterSlug === 'chargers') {
          matchesCat = pCat.includes('charger');
        } else if (filterSlug === 'cables') {
          matchesCat = pCat.includes('cable');
        } else if (filterSlug === 'audio') {
          matchesCat = pCat.includes('audio') || pCat.includes('headphone') || pCat.includes('earphone');
        } else {
          matchesCat = pCat === filterSlug;
        }
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

  const getTemplateForCategory = (catSlug) => {
    if (!catSlug) return [];
    const norm = catSlug.toLowerCase().replace(/_/g, '-');
    if (norm.includes('charger')) return SPEC_TEMPLATES.chargers;
    if (norm.includes('cable')) return SPEC_TEMPLATES.cables;
    if (norm.includes('audio') || norm.includes('headphone') || norm.includes('earphone')) return SPEC_TEMPLATES.audio;
    if (norm.includes('speaker')) return SPEC_TEMPLATES.speakers;
    if (norm.includes('power-bank') || norm.includes('powerbank') || norm.includes('battery')) return SPEC_TEMPLATES['power-banks'];
    if (norm.includes('cover') || norm.includes('case')) return SPEC_TEMPLATES.covers;
    if (norm.includes('screen') || norm.includes('guard') || norm.includes('protector')) return SPEC_TEMPLATES['screen-protectors'];
    if (norm.includes('old') || norm.includes('pre-owned')) return SPEC_TEMPLATES['old-phones'];
    if (norm.includes('phone') || norm.includes('mobile') || norm.includes('smart')) return SPEC_TEMPLATES.smartphones;
    return [];
  };

  const handleOpenAdd = () => {
    setEditingProduct(null);
    setFormData({
      name: '',
      brand: 'OnePlus',
      category: '', // Required: must be selected before saving
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
      specsList: [],
      specsText: ''
    });
    setSpecsEditorMode('builder');
    setShowModal(true);
  };

  const handleOpenEdit = (prod) => {
    setEditingProduct(prod);
    let list = [];
    if (prod.specs && typeof prod.specs === 'object') {
      list = Object.entries(prod.specs).map(([k, v]) => ({ key: k, value: String(v) }));
    }
    const specsStr = list.map(item => `${item.key}: ${item.value}`).join('\n');

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

    // Preserve product's exact category slug (or empty if unassigned)
    const exactCategory = (prod.category || prod.categorySlug || '').toLowerCase().trim();

    setFormData({
      name: prod.name || '',
      brand: prod.brand || 'OnePlus',
      category: exactCategory,
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
      specsList: list,
      specsText: specsStr
    });
    setSpecsEditorMode('builder');
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
    if (!formData.category || !formData.category.trim()) {
      onShowToast?.('Please select a category for this product.', 'error');
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

      // Resolve specifications dynamically from builder rows or text
      let finalSpecs = {};
      if (specsEditorMode === 'builder' && formData.specsList && formData.specsList.length > 0) {
        formData.specsList.forEach(item => {
          if (item.key && item.key.trim()) {
            finalSpecs[item.key.trim()] = (item.value || '').trim();
          }
        });
      } else if (formData.specsText) {
        finalSpecs = parseSpecs(formData.specsText);
      }

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
        specs: finalSpecs
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
            {categoriesList.map(cat => (
              <option key={cat.id} value={cat.slug}>{cat.name}</option>
            ))}
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
                      {categoriesList.find(c => c.slug === (p.category || '').toLowerCase())?.name || p.category || 'Unassigned'}
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
                    Product Category * <span className="text-[#D4AF37] font-normal">(Required)</span>
                  </label>
                  <select
                    required
                    value={formData.category}
                    onChange={(e) => {
                      const newCat = e.target.value;
                      setFormData(prev => {
                        const updated = { ...prev, category: newCat };
                        if ((!prev.specsList || prev.specsList.length === 0) && newCat) {
                          const tmpl = getTemplateForCategory(newCat);
                          if (tmpl.length > 0) {
                            updated.specsList = tmpl.map(t => ({ ...t }));
                            updated.specsText = tmpl.map(t => `${t.key}: ${t.value}`).join('\n');
                          }
                        }
                        return updated;
                      });
                    }}
                    className={`w-full bg-[#0E0E0E] border rounded-lg px-3.5 py-2.5 text-xs text-white focus:outline-none transition ${
                      !formData.category 
                        ? 'border-amber-500/70 focus:border-amber-400' 
                        : 'border-[#2E2E2E] focus:border-[#D4AF37]'
                    }`}
                  >
                    <option value="">-- Select Product Category * --</option>
                    {activeCategories.map(cat => (
                      <option key={cat.id || cat.slug} value={cat.slug}>
                        {cat.name} ({cat.slug})
                      </option>
                    ))}
                    {/* If editing an existing product whose category is currently inactive, display it */}
                    {formData.category && !activeCategories.some(c => c.slug === formData.category) && (
                      <option value={formData.category}>
                        {formData.category} (Current)
                      </option>
                    )}
                  </select>
                  {!formData.category && (
                    <p className="text-[11px] text-amber-400 mt-1.5 flex items-center gap-1 font-medium">
                      <AlertCircle size={13} className="shrink-0" />
                      <span>Please select a category for this product.</span>
                    </p>
                  )}
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

              {/* Category-Aware Specifications Editor */}
              <div className="p-4 rounded-xl bg-[#0E0E0E] border border-[#2E2E2E] space-y-3">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-[#222222] pb-2.5">
                  <div>
                    <label className="text-xs font-bold text-white flex items-center gap-1.5">
                      <Layers size={14} className="text-[#D4AF37]" />
                      <span>Category-Aware Specifications</span>
                    </label>
                    <p className="text-[11px] text-gray-400">
                      Customize technical specifications tailored to this product category (not only smartphones).
                    </p>
                  </div>

                  <div className="flex items-center gap-1.5 self-start sm:self-auto">
                    <button
                      type="button"
                      onClick={() => {
                        if (specsEditorMode === 'builder') {
                          // sync to text
                          const text = (formData.specsList || []).map(i => `${i.key}: ${i.value}`).join('\n');
                          setFormData(prev => ({ ...prev, specsText: text }));
                          setSpecsEditorMode('raw');
                        } else {
                          // sync to builder
                          const parsed = parseSpecs(formData.specsText);
                          const list = Object.entries(parsed).map(([k, v]) => ({ key: k, value: String(v) }));
                          setFormData(prev => ({ ...prev, specsList: list }));
                          setSpecsEditorMode('builder');
                        }
                      }}
                      className="text-[10px] font-semibold px-2 py-1 rounded bg-[#202020] text-gray-300 hover:text-white border border-[#333333] transition"
                    >
                      {specsEditorMode === 'builder' ? 'Switch to Quick Text' : 'Switch to Row Builder'}
                    </button>
                  </div>
                </div>

                {/* Quick Preset Templates */}
                <div className="space-y-1.5">
                  <span className="text-[10px] uppercase font-bold text-gray-500 tracking-wider block">
                    Load Category Template:
                  </span>
                  <div className="flex flex-wrap gap-1.5">
                    {[
                      { label: 'Smartphone', key: 'smartphones' },
                      { label: 'Charger', key: 'chargers' },
                      { label: 'Data Cable', key: 'cables' },
                      { label: 'Audio / Buds', key: 'audio' },
                      { label: 'BT Speaker', key: 'speakers' },
                      { label: 'Power Bank', key: 'power-banks' },
                      { label: 'Mobile Cover', key: 'covers' },
                      { label: 'Screen Glass', key: 'screen-protectors' },
                      { label: 'Old Phone', key: 'old-phones' }
                    ].map(tmpl => (
                      <button
                        key={tmpl.key}
                        type="button"
                        onClick={() => {
                          const t = SPEC_TEMPLATES[tmpl.key] || [];
                          const cloned = t.map(item => ({ ...item }));
                          const text = cloned.map(item => `${item.key}: ${item.value}`).join('\n');
                          setFormData(prev => ({
                            ...prev,
                            specsList: cloned,
                            specsText: text
                          }));
                          onShowToast?.(`Loaded ${tmpl.label} specifications template!`, 'info');
                        }}
                        className="text-[10px] font-medium px-2 py-0.5 rounded-md bg-[#181818] hover:bg-[#252525] text-gray-300 hover:text-[#D4AF37] border border-[#2D2D2D] transition"
                      >
                        {tmpl.label}
                      </button>
                    ))}
                    <button
                      type="button"
                      onClick={() => {
                        setFormData(prev => ({ ...prev, specsList: [], specsText: '' }));
                      }}
                      className="text-[10px] font-medium px-2 py-0.5 rounded-md bg-red-950/20 text-red-400 hover:bg-red-900/30 border border-red-900/30 transition"
                    >
                      Clear
                    </button>
                  </div>
                </div>

                {/* Structured Builder Mode */}
                {specsEditorMode === 'builder' ? (
                  <div className="space-y-2 pt-1">
                    {(formData.specsList || []).length === 0 ? (
                      <div className="p-3 rounded-lg bg-[#141414] border border-dashed border-[#333333] text-center space-y-1">
                        <p className="text-xs text-gray-400">No specifications added yet.</p>
                        <p className="text-[10px] text-gray-500">
                          Click a template button above or click "+ Add Custom Specification Row" below.
                        </p>
                      </div>
                    ) : (
                      <div className="space-y-1.5 max-h-56 overflow-y-auto pr-1">
                        {(formData.specsList || []).map((row, idx) => (
                          <div key={idx} className="flex items-center gap-2">
                            <input
                              type="text"
                              placeholder="Spec Name (e.g. Charging Power)"
                              value={row.key}
                              onChange={(e) => {
                                const newKey = e.target.value;
                                setFormData(prev => {
                                  const list = [...(prev.specsList || [])];
                                  list[idx] = { ...list[idx], key: newKey };
                                  return { ...prev, specsList: list };
                                });
                              }}
                              className="w-1/3 bg-[#141414] border border-[#2C2C2C] rounded-lg px-2.5 py-1.5 text-xs text-white focus:outline-none focus:border-[#D4AF37]"
                            />
                            <input
                              type="text"
                              placeholder="Value (e.g. 65W GaN III Fast Charge)"
                              value={row.value}
                              onChange={(e) => {
                                const newVal = e.target.value;
                                setFormData(prev => {
                                  const list = [...(prev.specsList || [])];
                                  list[idx] = { ...list[idx], value: newVal };
                                  return { ...prev, specsList: list };
                                });
                              }}
                              className="flex-1 bg-[#141414] border border-[#2C2C2C] rounded-lg px-2.5 py-1.5 text-xs text-white focus:outline-none focus:border-[#D4AF37]"
                            />
                            <button
                              type="button"
                              onClick={() => {
                                setFormData(prev => {
                                  const list = (prev.specsList || []).filter((_, i) => i !== idx);
                                  return { ...prev, specsList: list };
                                });
                              }}
                              className="p-1.5 rounded-lg text-gray-500 hover:text-red-400 hover:bg-[#202020] transition"
                              title="Delete Row"
                            >
                              <Trash2 size={13} />
                            </button>
                          </div>
                        ))}
                      </div>
                    )}

                    <div className="pt-1 flex items-center justify-between">
                      <button
                        type="button"
                        onClick={() => {
                          setFormData(prev => ({
                            ...prev,
                            specsList: [...(prev.specsList || []), { key: '', value: '' }]
                          }));
                        }}
                        className="py-1.5 px-3 rounded-lg bg-[#181818] hover:bg-[#222222] text-[#D4AF37] border border-[#333333] hover:border-[#D4AF37] text-xs font-semibold flex items-center gap-1.5 transition"
                      >
                        <Plus size={13} />
                        <span>Add Custom Specification Row</span>
                      </button>

                      <span className="text-[10px] text-gray-500">
                        {(formData.specsList || []).length} specifications configured
                      </span>
                    </div>
                  </div>
                ) : (
                  /* Raw Text Mode */
                  <div className="space-y-1.5">
                    <textarea
                      rows={4}
                      placeholder="Charging Power: 65W GaN III&#10;Port Type: Dual Port Type-C + USB-A&#10;Compatibility: Universal Smartphones & Laptops"
                      value={formData.specsText}
                      onChange={(e) => setFormData({ ...formData, specsText: e.target.value })}
                      className="w-full font-mono bg-[#141414] border border-[#2A2A2A] rounded-lg px-3 py-2 text-xs text-white focus:outline-none focus:border-[#D4AF37]"
                    />
                    <p className="text-[10px] text-gray-500">
                      Enter each specification as <code className="text-[#D4AF37]">Key: Value</code> on a new line.
                    </p>
                  </div>
                )}
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
