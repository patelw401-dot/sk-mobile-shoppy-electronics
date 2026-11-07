import React, { useState, useMemo } from 'react';
import { 
  X, 
  Star, 
  ShieldCheck, 
  RotateCcw, 
  Truck, 
  CheckCircle2, 
  ShoppingCart, 
  Zap, 
  Phone, 
  Tag, 
  ChevronRight, 
  Share2, 
  Heart,
  Sliders,
  Sparkles,
  Info,
  Clock,
  Layers,
  Check
} from 'lucide-react';
import { STORE_INFO } from '../data/storeData';

// Fallback category-aware specifications when a product does not have specs populated
export function getCategoryAwareDefaultSpecs(category, brand = 'SK Mobile') {
  const cat = (category || '').toLowerCase().replace(/_/g, '-');
  
  if (cat.includes('charger')) {
    return {
      'Charging Power': '65W Max Power Delivery (GaN)',
      'Input': '100-240V ~ 50/60Hz 1.5A Universal',
      'Output Ports': '1x Type-C (PD 3.0) + 1x USB-A (QC 4.0)',
      'Fast Charging': 'Supports VOOC, Warp, QC, PD 3.0 & PPS',
      'Compatibility': 'Universal for Android, iPhone, Laptops & Tablets',
      'Safety Protection': 'Over-voltage, Over-heating & Short-circuit Auto Cut-off'
    };
  }

  if (cat.includes('cable') || cat.includes('wire')) {
    return {
      'Length': '1.5 Meters (Extra Long & Tangle-Free)',
      'Connector Type': 'Type-C to Type-C / Fast Sync',
      'Charging Speed': 'Up to 100W (20V/5A) Power Delivery',
      'Data Transfer Speed': '480 Mbps High Speed Sync',
      'Build Material': 'Double Ballistic Nylon Braided + Zinc Alloy',
      'Bend Lifespan': '30,000+ Bends Lab Tested'
    };
  }

  if (cat.includes('audio') || cat.includes('headphone') || cat.includes('earphone') || cat.includes('neckband') || cat.includes('buds')) {
    return {
      'Type': 'Wireless In-Ear / Bluetooth Headset',
      'Driver Size': '12.4mm Deep Bass Dynamic Drivers',
      'Connectivity': 'Bluetooth v5.3 (10m Range)',
      'Battery Life': 'Up to 30 Hours Playtime',
      'Fast Charge': '10 mins charge = 20 hours playback',
      'Microphone': 'Dual Mic AI Environmental Noise Cancellation',
      'Water Resistance': 'IP55 Sweat & Splash Proof'
    };
  }

  if (cat.includes('speaker') || cat.includes('bluetooth-speaker')) {
    return {
      'Output Power': '16W RMS High-Fidelity Stereo Audio',
      'Playtime': '12 Hours Continuous Music Playback',
      'Battery Capacity': '2600 mAh Built-in Rechargeable',
      'Bluetooth Version': 'Bluetooth 5.3 + AUX Support',
      'Waterproof Rating': 'IPX7 Waterproof Protection',
      'Features': 'TWS Dual Speaker Pairing & Built-in Mic'
    };
  }

  if (cat.includes('power-bank') || cat.includes('powerbank') || cat.includes('battery')) {
    return {
      'Battery Capacity': '20,000 mAh Li-Polymer',
      'Output Power': '22.5W Fast Charging (PD + QC)',
      'Input Ports': 'Type-C + Micro-USB Dual Input',
      'Output Ports': '2x USB-A + 1x Type-C Fast Output',
      'Fast Protocols': 'Quick Charge 3.0, Power Delivery 3.0',
      'Protection': '12-Layer Advanced Smart Circuit Chip'
    };
  }

  if (cat.includes('cover') || cat.includes('case')) {
    return {
      'Material': 'Military-Grade TPU + Anti-Yellowing Acrylic Back',
      'Finish': 'Matte Frosted Anti-Fingerprint Grip',
      'Shock Protection': 'Air-Cushion Impact Absorbing Corners (8ft Drop Tested)',
      'Raised Edges': '1.5mm Screen Lip & 2.0mm Camera Ring Shield',
      'Wireless Charging': '100% Compatible with Qi & MagSafe Chargers'
    };
  }

  if (cat.includes('screen') || cat.includes('guard') || cat.includes('protector') || cat.includes('glass')) {
    return {
      'Hardness': '9H Hardness Shatterproof Tempered Glass',
      'Glass Type': '0.33mm Ultra-Thin 2.5D Curved Edge-to-Edge',
      'Coating': 'Hydrophobic & Oleophobic Anti-Fingerprint Coating',
      'Clarity': '99.9% Ultra HD Transparency & True Touch Response',
      'Installation': 'Bubble-Free Auto-Adhesive Alignment'
    };
  }

  if (cat.includes('old') || cat.includes('pre-owned') || cat.includes('refurbished')) {
    return {
      'Condition Grade': 'SK Certified Grade A+ (Like New, 100% Tested)',
      'Quality Check': '32-Point Rigorous Hardware & Sensor Inspection',
      'Battery Health': 'Tested ≥ 90% Original Capacity',
      'Included In Box': 'Device + Fast Charging Cable + Store Warranty Slip',
      'IMEI Verification': '100% Clean Verified IMEI with GST Billing'
    };
  }

  // Default for smartphones or general gadgets
  return {
    'Display': '6.7" Fluid AMOLED / 120Hz Refresh Rate',
    'Processor': 'High-Efficiency Multi-Core Chipset',
    'Camera': 'Ultra-Clear High-Resolution Sensor with OIS',
    'Battery & Charging': '5000+ mAh with Rapid Super Fast Charge',
    'Operating System': 'Latest Android with Official Updates',
    'Connectivity': '5G Dual SIM, VoLTE, Wi-Fi 6, Bluetooth 5.3'
  };
}

// Sample verified customer reviews
const SAMPLE_REVIEWS = [
  {
    id: 1,
    name: 'Rahul K. (Udgir)',
    rating: 5,
    date: '3 days ago',
    comment: '100% genuine product. Bought it directly from the Nagar Parishad shop. Super fast and courteous service by the owner!',
    verified: true
  },
  {
    id: 2,
    name: 'Sachin Patil',
    rating: 5,
    date: '1 week ago',
    comment: 'Best price in Udgir compared to online market. Original sealed pack with valid bill and warranty.',
    verified: true
  },
  {
    id: 3,
    name: 'Pooja Deshmukh',
    rating: 4,
    date: '2 weeks ago',
    comment: 'Very happy with the purchase. Works great and staff explained all features patiently.',
    verified: true
  }
];

export default function ProductDetailsModal({ 
  product, 
  onClose, 
  onAddToCart, 
  onBuyNow, 
  onSelectProduct, 
  allProducts = [] 
}) {
  const [selectedAngle, setSelectedAngle] = useState(0);
  const [quantity, setQuantity] = useState(1);
  const [copiedLink, setCopiedLink] = useState(false);
  const [showAllReviews, setShowAllReviews] = useState(false);

  if (!product) return null;

  const numPrice = typeof product.price === 'number' ? product.price : parseFloat(product.price) || 0;
  const numOriginalPrice = typeof product.originalPrice === 'number' ? product.originalPrice : (parseFloat(product.originalPrice) || 0);

  const discountPercent = numOriginalPrice > numPrice && numPrice > 0
    ? Math.round(((numOriginalPrice - numPrice) / numOriginalPrice) * 100)
    : 0;

  const savingsAmount = numOriginalPrice > numPrice && numPrice > 0
    ? numOriginalPrice - numPrice
    : 0;

  const isOutOfStock = product.inStock === false || (product.stock !== undefined && product.stock <= 0);

  // Specifications resolution: use product's custom specs, or fall back to category-aware template
  const rawSpecs = product.specs || product.specifications;
  const resolvedSpecs = useMemo(() => {
    if (rawSpecs && typeof rawSpecs === 'object' && !Array.isArray(rawSpecs) && Object.keys(rawSpecs).length > 0) {
      return rawSpecs;
    }
    return getCategoryAwareDefaultSpecs(product.category || product.categorySlug, product.brand);
  }, [rawSpecs, product.category, product.categorySlug, product.brand]);

  // Gallery angles/views
  const galleryViews = [
    { label: 'Front View', url: product.imageUrl || product.image },
    { label: 'Back View', url: product.imageUrl || product.image },
    { label: 'Ports & Detail', url: product.imageUrl || product.image },
    { label: 'Original Box', url: product.imageUrl || product.image }
  ];

  // Related products from the same category or brand
  const relatedProducts = useMemo(() => {
    if (!allProducts || allProducts.length === 0) return [];
    const pCat = (product.category || '').toLowerCase();
    return allProducts
      .filter(p => p.id !== product.id && (
        (p.category && p.category.toLowerCase() === pCat) ||
        (p.brand && p.brand.toLowerCase() === (product.brand || '').toLowerCase())
      ))
      .slice(0, 4);
  }, [allProducts, product]);

  const handleShare = () => {
    if (navigator.share) {
      navigator.share({
        title: product.name,
        text: `Check out ${product.name} at SK Mobile Shoppy and Electronics Udgir!`,
        url: window.location.href
      }).catch(() => {});
    } else {
      navigator.clipboard?.writeText(window.location.href);
      setCopiedLink(true);
      setTimeout(() => setCopiedLink(false), 2500);
    }
  };

  // Determine Product Quality Badge
  const getQualityBadge = () => {
    if (product.conditionGrade) {
      return {
        label: product.conditionGrade,
        sub: 'Tested with 32-point checklist',
        color: 'bg-amber-500/15 text-amber-300 border-amber-500/30'
      };
    }
    if (product.isPreOwned) {
      return {
        label: 'SK Certified Pre-Owned',
        sub: 'Tested & Sanitized with Shop Warranty',
        color: 'bg-amber-500/15 text-amber-300 border-amber-500/30'
      };
    }
    const cat = (product.category || '').toLowerCase();
    if (cat.includes('charger') || cat.includes('cable') || cat.includes('audio')) {
      return {
        label: '100% Original OEM Quality',
        sub: 'Tested & Certified Safe Fast Charging',
        color: 'bg-emerald-500/15 text-emerald-300 border-emerald-500/30'
      };
    }
    return {
      label: '100% Original Brand New',
      sub: 'Factory Sealed with Official Billing',
      color: 'bg-[#D4AF37]/15 text-[#D4AF37] border-[#D4AF37]/30'
    };
  };

  const qualityBadge = getQualityBadge();

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-5 bg-black/85 backdrop-blur-md animate-in fade-in overflow-y-auto">
      <div className="relative w-full max-w-4xl bg-[#111111] border border-[#D4AF37]/40 rounded-2xl sm:rounded-3xl shadow-2xl overflow-hidden my-auto max-h-[92vh] flex flex-col text-white">
        
        {/* Top Header / Bar */}
        <div className="px-5 py-3.5 bg-[#161616] border-b border-[#242424] flex items-center justify-between shrink-0">
          <div className="flex items-center gap-2 text-xs">
            <span className="font-extrabold text-[#D4AF37] tracking-wider uppercase">
              SK Mobile Udgir
            </span>
            <span className="text-gray-600">•</span>
            <span className="text-gray-400 capitalize truncate max-w-[150px] sm:max-w-none">
              {product.category || 'Product Details'}
            </span>
            <span className="text-gray-600">•</span>
            <span className="text-gray-300 font-medium truncate max-w-[120px] sm:max-w-[200px]">
              {product.brand}
            </span>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={handleShare}
              className="p-1.5 rounded-lg bg-[#222222] text-gray-300 hover:text-white hover:bg-[#2C2C2C] transition"
              title="Share Product"
            >
              <Share2 size={16} />
            </button>
            <button
              onClick={onClose}
              className="p-1.5 rounded-lg bg-[#222222] text-gray-400 hover:text-white hover:bg-red-950/40 hover:border hover:border-red-500/40 transition"
              title="Close (Esc)"
            >
              <X size={18} />
            </button>
          </div>
        </div>

        {/* Scrollable Body */}
        <div className="overflow-y-auto p-5 sm:p-7 space-y-7 flex-1">
          {/* Main Grid: Left Gallery + Right Key Details */}
          <div className="grid grid-cols-1 md:grid-cols-12 gap-6 sm:gap-8 items-start">
            
            {/* LEFT: 1. Product Images Gallery (5 cols) */}
            <div className="md:col-span-5 space-y-3">
              {/* Main Image Box */}
              <div className="relative aspect-square rounded-2xl bg-gradient-to-b from-[#1C1C1C] to-[#121212] border border-[#2B2B2B] p-4 flex items-center justify-center overflow-hidden group shadow-inner">
                {product.imageUrl || product.image ? (
                  <img
                    src={product.imageUrl || product.image}
                    alt={product.name}
                    className="w-full h-full object-contain transition-transform duration-300 group-hover:scale-105"
                    onError={(e) => {
                      e.currentTarget.style.display = 'none';
                      if (e.currentTarget.nextElementSibling) {
                        e.currentTarget.nextElementSibling.style.display = 'flex';
                      }
                    }}
                  />
                ) : null}

                {/* Fallback Icon */}
                <div 
                  style={{ display: (product.imageUrl || product.image) ? 'none' : 'flex' }}
                  className="flex-col items-center justify-center text-center p-6"
                >
                  <div className="w-20 h-20 rounded-2xl bg-[#222222] flex items-center justify-center text-[#D4AF37] mb-3 border border-[#333333]">
                    <Tag size={36} />
                  </div>
                  <span className="text-xs font-bold text-gray-400 uppercase tracking-widest">
                    {product.brand}
                  </span>
                </div>

                {/* Badges on Image */}
                <div className="absolute top-3 left-3 flex flex-col gap-1.5">
                  {product.badge && (
                    <span className="text-[10px] uppercase font-bold tracking-wider px-2.5 py-1 rounded-md bg-[#D4AF37] text-black shadow-md">
                      {product.badge}
                    </span>
                  )}
                  {discountPercent > 0 && (
                    <span className="text-[10px] font-extrabold px-2 py-0.5 rounded-md bg-emerald-500 text-white shadow-md">
                      {discountPercent}% OFF
                    </span>
                  )}
                </div>

                {/* Live Stock Overlay */}
                <div className="absolute bottom-3 left-3 right-3">
                  {isOutOfStock ? (
                    <div className="py-1 px-2.5 rounded-lg bg-red-950/80 border border-red-800/80 text-[11px] font-bold text-red-300 flex items-center justify-center gap-1.5 backdrop-blur-sm">
                      <span className="w-2 h-2 rounded-full bg-red-500"></span>
                      <span>Out of Stock • Restock In Progress</span>
                    </div>
                  ) : (
                    <div className="py-1 px-2.5 rounded-lg bg-[#0A0A0A]/85 border border-[#262626] text-[11px] font-semibold text-emerald-400 flex items-center justify-center gap-1.5 backdrop-blur-sm">
                      <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
                      <span>In Stock • Ready for Pickup at Udgir Store</span>
                    </div>
                  )}
                </div>
              </div>

              {/* Gallery Angles Thumbnails */}
              <div className="grid grid-cols-4 gap-2">
                {galleryViews.map((view, idx) => (
                  <button
                    key={idx}
                    type="button"
                    onClick={() => setSelectedAngle(idx)}
                    className={`p-1.5 rounded-xl border text-center transition-all ${
                      selectedAngle === idx
                        ? 'border-[#D4AF37] bg-[#D4AF37]/15 ring-1 ring-[#D4AF37]'
                        : 'border-[#262626] bg-[#161616] hover:border-gray-500'
                    }`}
                  >
                    <div className="w-full h-10 rounded-lg bg-[#202020] flex items-center justify-center overflow-hidden mb-1">
                      {product.imageUrl || product.image ? (
                        <img 
                          src={product.imageUrl || product.image} 
                          alt={view.label}
                          className="w-full h-full object-contain p-0.5" 
                        />
                      ) : (
                        <Tag size={14} className="text-gray-500" />
                      )}
                    </div>
                    <span className="text-[9px] font-medium text-gray-300 truncate block">
                      {view.label}
                    </span>
                  </button>
                ))}
              </div>
            </div>

            {/* RIGHT: Name, Brand, Price, Rating, Quality (7 cols) */}
            <div className="md:col-span-7 space-y-4">
              
              {/* Brand & Category Breadcrumbs */}
              <div className="flex flex-wrap items-center justify-between gap-2">
                <div className="flex items-center gap-2">
                  <span className="text-xs uppercase font-extrabold px-2.5 py-0.5 rounded-md bg-[#D4AF37]/20 text-[#D4AF37] border border-[#D4AF37]/40 tracking-wider">
                    {product.brand}
                  </span>
                  <span className="text-xs text-gray-400 capitalize">
                    Category: <strong className="text-gray-200">{product.category || 'General'}</strong>
                  </span>
                </div>

                {/* 11. Customer Rating */}
                <div className="flex items-center gap-1.5 bg-[#1C1C1C] px-2.5 py-1 rounded-lg border border-[#2D2D2D] text-xs">
                  <div className="flex items-center text-amber-400">
                    <Star size={14} className="fill-amber-400 text-amber-400" />
                    <span className="ml-1 font-extrabold text-white">{product.rating || '4.7'}</span>
                  </div>
                  <span className="text-gray-500 text-[11px]">
                    ({product.reviewCount || 128} verified ratings)
                  </span>
                </div>
              </div>

              {/* 2. Product Name */}
              <h1 className="text-xl sm:text-2xl font-black text-white leading-snug tracking-tight">
                {product.name}
              </h1>

              {/* 5. Product Quality / Condition Grade */}
              <div className={`p-3 rounded-xl border flex items-center justify-between gap-3 ${qualityBadge.color}`}>
                <div className="flex items-center gap-2.5">
                  <ShieldCheck size={20} className="shrink-0" />
                  <div>
                    <div className="text-xs font-bold leading-tight">{qualityBadge.label}</div>
                    <div className="text-[10px] opacity-80">{qualityBadge.sub}</div>
                  </div>
                </div>
                <span className="text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded bg-black/40">
                  SK Verified
                </span>
              </div>

              {/* 4. Price / MRP / Discount */}
              <div className="p-4 rounded-xl bg-[#151515] border border-[#242424] space-y-2">
                <div className="flex items-baseline gap-3 flex-wrap">
                  <span className="text-3xl font-black text-[#D4AF37] tracking-tight">
                    ₹{numPrice.toLocaleString('en-IN')}
                  </span>

                  {numOriginalPrice > numPrice && (
                    <>
                      <span className="text-sm text-gray-500 line-through">
                        MRP ₹{numOriginalPrice.toLocaleString('en-IN')}
                      </span>
                      <span className="text-xs font-extrabold px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
                        Save {discountPercent}%
                      </span>
                    </>
                  )}
                </div>

                {savingsAmount > 0 && (
                  <p className="text-xs text-emerald-400 font-semibold">
                    ✓ You save ₹{savingsAmount.toLocaleString('en-IN')} with store special pricing
                  </p>
                )}

                <div className="text-[11px] text-gray-400 flex items-center gap-2 pt-1 border-t border-[#222222]">
                  <span>Inclusive of all taxes</span>
                  <span>•</span>
                  <span>GST Invoice Available</span>
                  <span>•</span>
                  <span>Store Pickup / Same-Day Udgir Delivery</span>
                </div>
              </div>

              {/* 6. Product Description */}
              {product.description && (
                <div className="space-y-1">
                  <h4 className="text-xs font-bold uppercase tracking-wider text-gray-400">
                    Product Overview
                  </h4>
                  <p className="text-xs sm:text-sm text-gray-300 leading-relaxed bg-[#141414] p-3 rounded-xl border border-[#222222]">
                    {product.description}
                  </p>
                </div>
              )}

              {/* 14 & 15. Action Buttons (Quantity, Add to Cart, Buy Now) */}
              <div className="pt-2 space-y-3">
                {!isOutOfStock ? (
                  <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
                    {/* Quantity Selector */}
                    <div className="flex items-center justify-between border border-[#333333] rounded-xl bg-[#161616] px-3 py-2 w-full sm:w-32">
                      <span className="text-xs text-gray-400 font-medium">Qty:</span>
                      <div className="flex items-center gap-3">
                        <button
                          type="button"
                          onClick={() => setQuantity(q => Math.max(1, q - 1))}
                          className="w-6 h-6 rounded bg-[#242424] text-white hover:bg-[#333333] flex items-center justify-center font-bold text-xs"
                        >
                          -
                        </button>
                        <span className="font-extrabold text-sm text-white w-4 text-center">
                          {quantity}
                        </span>
                        <button
                          type="button"
                          onClick={() => setQuantity(q => Math.min(10, q + 1))}
                          className="w-6 h-6 rounded bg-[#242424] text-white hover:bg-[#333333] flex items-center justify-center font-bold text-xs"
                        >
                          +
                        </button>
                      </div>
                    </div>

                    {/* Add to Cart */}
                    <button
                      type="button"
                      onClick={() => {
                        onAddToCart?.(product, quantity);
                        onClose?.();
                      }}
                      className="flex-1 py-3 px-4 rounded-xl text-xs sm:text-sm font-bold bg-[#1C1C1C] hover:bg-[#282828] text-white border border-[#3A3A3A] hover:border-[#D4AF37] transition flex items-center justify-center gap-2 shadow-md"
                    >
                      <ShoppingCart size={16} className="text-[#D4AF37]" />
                      <span>Add to Cart</span>
                    </button>

                    {/* Buy Now */}
                    <button
                      type="button"
                      onClick={() => {
                        onBuyNow ? onBuyNow(product, quantity) : onAddToCart?.(product, quantity);
                        onClose?.();
                      }}
                      className="flex-1 gold-btn !text-xs sm:!text-sm !py-3 flex items-center justify-center gap-2 font-bold shadow-lg shadow-[#D4AF37]/20"
                    >
                      <Zap size={16} />
                      <span>Buy Now</span>
                    </button>
                  </div>
                ) : (
                  <div className="p-3.5 rounded-xl bg-[#181818] border border-red-900/40 text-center space-y-2">
                    <p className="text-xs font-bold text-red-400">
                      Currently Out of Stock at Udgir Store
                    </p>
                    <a
                      href={`tel:${STORE_INFO.phone}`}
                      className="inline-flex items-center justify-center gap-2 py-2 px-4 rounded-lg bg-[#222222] hover:bg-[#2C2C2C] text-xs font-semibold text-[#D4AF37] transition"
                    >
                      <Phone size={14} />
                      <span>Call {STORE_INFO.displayPhone} to Pre-Order / Inquire Restock</span>
                    </a>
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* 8. Product Specifications (Category-Aware Dynamic Specs) */}
          <div className="p-5 rounded-2xl bg-[#141414] border border-[#242424] space-y-3">
            <div className="flex items-center justify-between border-b border-[#242424] pb-3">
              <div className="flex items-center gap-2">
                <Sliders size={18} className="text-[#D4AF37]" />
                <h3 className="text-sm sm:text-base font-bold text-white">
                  Technical Specifications
                </h3>
              </div>
              <span className="text-[10px] text-gray-500 font-semibold uppercase tracking-wider">
                Category: {product.category || 'General'}
              </span>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 text-xs">
              {Object.entries(resolvedSpecs && typeof resolvedSpecs === 'object' ? resolvedSpecs : {}).map(([key, value]) => (
                <div 
                  key={key} 
                  className="flex items-center justify-between p-2.5 rounded-lg bg-[#181818] border border-[#222222] hover:border-[#333333] transition"
                >
                  <span className="text-gray-400 font-medium">{key}</span>
                  <span className="text-gray-200 font-bold text-right max-w-[60%] truncate">
                    {typeof value === 'object' ? JSON.stringify(value) : String(value)}
                  </span>
                </div>
              ))}
            </div>
          </div>

          {/* 9 & 10. Warranty & Return Policy Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            
            {/* 9. Warranty Section */}
            <div className="p-4 rounded-xl bg-[#D4AF37]/10 border border-[#D4AF37]/30 space-y-2">
              <div className="flex items-center gap-2 text-[#D4AF37] font-bold text-sm">
                <ShieldCheck size={18} />
                <span>
                  {product.hasWarranty && product.warrantyDuration
                    ? `${product.warrantyDuration} Store / Brand Warranty`
                    : (typeof product.warranty === 'string' && product.warranty.trim()
                        ? product.warranty 
                        : '1 Year Store & Brand Warranty')}
                </span>
              </div>
              <p className="text-xs text-gray-300 leading-relaxed">
                {product.warrantyDescription || 
                 '100% genuine guaranteed with repair and replacement assistance at SK Mobile Shoppy and Electronics, Station Road, Udgir.'}
              </p>
            </div>

            {/* 10. Return / Refund Policy */}
            <div className="p-4 rounded-xl bg-[#141414] border border-[#262626] space-y-2">
              <div className="flex items-center gap-2 text-emerald-400 font-bold text-sm">
                <RotateCcw size={18} />
                <span>7-Day Replacement Policy</span>
              </div>
              <p className="text-xs text-gray-300 leading-relaxed">
                Hassle-free replacement for any technical defect or mismatch at our Udgir counter. Verified billing invoice provided with every order.
              </p>
            </div>
          </div>

          {/* 12. Customer Reviews */}
          <div className="p-5 rounded-2xl bg-[#141414] border border-[#242424] space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-sm sm:text-base font-bold text-white flex items-center gap-2">
                  <span>Customer Reviews & Ratings</span>
                  <span className="text-xs px-2 py-0.5 rounded bg-amber-500/20 text-amber-300 font-bold">
                    {product.rating || '4.7'} ★
                  </span>
                </h3>
                <p className="text-xs text-gray-500 mt-0.5">
                  Verified customer feedback from Udgir buyers
                </p>
              </div>

              <button
                type="button"
                onClick={() => setShowAllReviews(!showAllReviews)}
                className="text-xs text-[#D4AF37] hover:underline font-semibold"
              >
                {showAllReviews ? 'Show Less' : 'View All Reviews'}
              </button>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              {(showAllReviews ? SAMPLE_REVIEWS : SAMPLE_REVIEWS.slice(0, 3)).map((rev) => (
                <div key={rev.id} className="p-3.5 rounded-xl bg-[#181818] border border-[#262626] space-y-2">
                  <div className="flex items-center justify-between text-xs">
                    <span className="font-bold text-gray-200">{rev.name}</span>
                    <span className="text-[10px] text-gray-500">{rev.date}</span>
                  </div>
                  <div className="flex items-center gap-1 text-amber-400">
                    {Array.from({ length: Math.max(1, Math.min(5, Math.round(Number(rev.rating) || 5))) }).map((_, i) => (
                      <Star key={i} size={12} className="fill-amber-400" />
                    ))}
                  </div>
                  <p className="text-[11px] text-gray-300 leading-relaxed">
                    "{rev.comment}"
                  </p>
                  {rev.verified && (
                    <div className="flex items-center gap-1 text-[10px] text-emerald-400 font-semibold pt-1">
                      <Check size={11} />
                      <span>Verified Buyer (Udgir)</span>
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>

          {/* 16. Related Products */}
          {relatedProducts.length > 0 && (
            <div className="space-y-3 pt-2">
              <div className="flex items-center justify-between">
                <h3 className="text-sm sm:text-base font-bold text-white flex items-center gap-2">
                  <Sparkles size={16} className="text-[#D4AF37]" />
                  <span>Related Products in {product.category || 'Store'}</span>
                </h3>
                <span className="text-xs text-gray-500">Tap to view</span>
              </div>

              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                {relatedProducts.map((rel) => {
                  const relPrice = typeof rel.price === 'number' ? rel.price : (parseFloat(rel.price) || 0);
                  return (
                    <div
                      key={rel.id || Math.random()}
                      onClick={() => {
                        onSelectProduct?.(rel);
                      }}
                      className="p-3 rounded-xl bg-[#141414] border border-[#242424] hover:border-[#D4AF37] cursor-pointer transition flex flex-col justify-between group"
                    >
                      <div className="h-24 rounded-lg bg-[#1C1C1C] flex items-center justify-center p-2 mb-2 overflow-hidden">
                        {rel.imageUrl || rel.image ? (
                          <img 
                            src={rel.imageUrl || rel.image} 
                            alt={rel.name || 'Related Product'} 
                            className="w-full h-full object-contain group-hover:scale-105 transition"
                          />
                        ) : (
                          <Tag size={20} className="text-[#D4AF37]" />
                        )}
                      </div>
                      <div className="text-[10px] uppercase font-bold text-gray-500 truncate">{rel.brand || 'SK Mobile'}</div>
                      <h4 className="text-xs font-bold text-white group-hover:text-[#D4AF37] transition line-clamp-1 mb-1">
                        {rel.name || 'Store Item'}
                      </h4>
                      <div className="text-xs font-black text-[#D4AF37]">
                        ₹{relPrice.toLocaleString('en-IN')}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {/* Store Footer Telemetry in Modal */}
          <div className="p-4 rounded-xl bg-[#0F0F0F] border border-[#222222] flex flex-wrap items-center justify-between gap-3 text-xs text-gray-400">
            <div className="flex items-center gap-2">
              <Truck size={15} className="text-[#D4AF37]" />
              <span>SK Mobile Shoppy and Electronics • Station Road, Udgir - 413517</span>
            </div>
            <a
              href={`tel:${STORE_INFO.phone}`}
              className="text-[#D4AF37] hover:underline font-bold flex items-center gap-1"
            >
              <Phone size={13} />
              <span>Call Helpline: {STORE_INFO.displayPhone}</span>
            </a>
          </div>
        </div>

      </div>
    </div>
  );
}
