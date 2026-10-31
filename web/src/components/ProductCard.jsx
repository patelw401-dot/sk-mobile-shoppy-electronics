import React from 'react';
import { Star, ShieldCheck, BatteryCharging, Tag, ShoppingCart, Zap } from 'lucide-react';

export default function ProductCard({ product, onSelect, onAction, onAddToCart, onBuyNow }) {
  const discountPercent = product.originalPrice && product.originalPrice > product.price
    ? Math.round(((product.originalPrice - product.price) / product.originalPrice) * 100)
    : 0;

  return (
    <div className="luxury-card flex flex-col justify-between group hover:border-[#D4AF37]/50 relative">
      {/* Top Badges */}
      <div className="flex items-center justify-between mb-3 gap-2">
        {product.badge ? (
          <span className="text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded bg-[#D4AF37]/20 text-[#D4AF37] border border-[#D4AF37]/30">
            {product.badge}
          </span>
        ) : (
          <span className="text-[10px] uppercase font-semibold text-gray-500">
            {product.brand}
          </span>
        )}

        {discountPercent > 0 && (
          <span className="text-[10px] font-extrabold px-1.5 py-0.5 rounded bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
            {discountPercent}% OFF
          </span>
        )}
      </div>

      {/* Product Image / Icon placeholder with sleek modern backdrop */}
      <div 
        onClick={() => onSelect?.(product)}
        className="h-44 rounded-xl bg-gradient-to-b from-[#1C1C1C] to-[#121212] border border-[#222222] flex flex-col items-center justify-center p-3 text-center mb-4 relative overflow-hidden group-hover:border-[#D4AF37]/30 transition-all cursor-pointer"
      >
        {product.imageUrl ? (
          <img 
            src={product.imageUrl} 
            alt={product.name} 
            className="w-full h-28 object-contain mb-1 transition-transform group-hover:scale-105 duration-300"
            onError={(e) => {
              e.currentTarget.style.display = 'none';
              if (e.currentTarget.nextElementSibling) {
                e.currentTarget.nextElementSibling.style.display = 'flex';
              }
            }}
          />
        ) : null}
        <div 
          style={{ display: product.imageUrl ? 'none' : 'flex' }}
          className="w-16 h-16 rounded-2xl bg-[#222222] items-center justify-center mb-2 border border-[#333333] shadow-inner"
        >
          <Tag className="text-[#D4AF37]" size={28} />
        </div>
        <span className="text-[11px] font-semibold text-gray-400 uppercase tracking-widest truncate max-w-full">
          {product.brand}
        </span>
        {product.conditionGrade && (
          <div className="absolute bottom-2 left-2 right-2 flex items-center justify-center gap-1 bg-[#0A0A0A]/90 py-1 px-2 rounded-md border border-[#2C2C2C] text-[10px] text-amber-300 font-medium">
            <ShieldCheck size={12} className="text-[#D4AF37]" />
            <span>{product.conditionGrade}</span>
          </div>
        )}
      </div>

      {/* Info Section */}
      <div className="flex-1 flex flex-col">
        <h3 
          onClick={() => onSelect?.(product)}
          className="text-sm font-bold text-white group-hover:text-[#D4AF37] transition-colors line-clamp-2 mb-1 cursor-pointer"
        >
          {product.name}
        </h3>

        {/* Rating */}
        <div className="flex items-center gap-1.5 text-xs text-amber-400 mb-2">
          <div className="flex items-center">
            <Star size={13} className="fill-amber-400 text-amber-400" />
            <span className="ml-1 font-bold text-gray-200">{product.rating || '4.6'}</span>
          </div>
          <span className="text-gray-500 text-[11px]">({product.reviewCount || 120} reviews)</span>
        </div>

        {/* Product Warranty Badge (displayed only when enabled by admin with exact duration) */}
        {product.hasWarranty && product.warrantyDuration && (
          <div className="flex items-center gap-1.5 text-[11px] text-[#D4AF37] mb-2 bg-[#D4AF37]/10 px-2 py-0.5 rounded border border-[#D4AF37]/30 self-start">
            <ShieldCheck size={13} className="text-[#D4AF37] shrink-0" />
            <span className="font-semibold">{product.warrantyDuration} Warranty</span>
          </div>
        )}

        {/* Pre-owned specific indicators */}
        {product.batteryHealth && (
          <div className="flex items-center gap-1.5 text-[11px] text-emerald-400 mb-2 bg-emerald-950/30 px-2 py-0.5 rounded border border-emerald-800/30 self-start">
            <BatteryCharging size={13} />
            <span>Battery Health: {product.batteryHealth}</span>
          </div>
        )}

        {/* Specs Highlights */}
        {product.specs && (
          <div className="space-y-1 mb-4 text-[11px] text-gray-400">
            {Object.entries(product.specs).slice(0, 2).map(([key, value]) => (
              <div key={key} className="flex justify-between border-b border-[#1F1F1F] pb-0.5">
                <span className="text-gray-500">{key}:</span>
                <span className="text-gray-300 font-medium truncate max-w-[170px]">{value}</span>
              </div>
            ))}
          </div>
        )}

        {/* Price & Action */}
        <div className="mt-auto pt-3 border-t border-[#222222] space-y-2.5">
          <div className="flex items-center justify-between gap-2">
            <div>
              <div className="flex items-baseline gap-1.5">
                <span className="text-base font-extrabold text-[#D4AF37]">
                  ₹{product.price.toLocaleString('en-IN')}
                </span>
                {product.originalPrice && product.originalPrice > product.price && (
                  <span className="text-xs text-gray-500 line-through">
                    ₹{product.originalPrice.toLocaleString('en-IN')}
                  </span>
                )}
              </div>
              {product.inStock === false || (product.stock !== undefined && product.stock <= 0) ? (
                <p className="text-[10px] text-red-400 font-bold flex items-center gap-1">
                  <span className="w-1.5 h-1.5 rounded-full bg-red-500"></span>
                  <span>Out of Stock</span>
                </p>
              ) : (
                <p className="text-[10px] text-emerald-400 font-medium flex items-center gap-1">
                  <span className="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
                  <span>In Stock • Udgir Store</span>
                </p>
              )}
            </div>

            <button
              type="button"
              onClick={() => onSelect?.(product)}
              className="text-[11px] text-gray-400 hover:text-white underline underline-offset-2"
            >
              Details
            </button>
          </div>

          {/* Action Buttons: Add to Cart & Buy Now */}
          {product.inStock === false || (product.stock !== undefined && product.stock <= 0) ? (
            <button
              type="button"
              onClick={() => onAction ? onAction(product) : onSelect?.(product)}
              className="w-full py-2 px-3 rounded-lg text-xs font-semibold bg-[#1C1C1C] text-gray-400 border border-[#2E2E2E] hover:border-gray-500 transition text-center"
            >
              Out of Stock • Inquire Restock
            </button>
          ) : (
            <div className="grid grid-cols-2 gap-2">
              <button
                type="button"
                onClick={(e) => {
                  e.stopPropagation();
                  onAddToCart ? onAddToCart(product) : onSelect?.(product);
                }}
                className="py-2 px-2.5 rounded-lg text-xs font-bold bg-[#1A1A1A] hover:bg-[#252525] text-gray-200 border border-[#333333] hover:border-[#D4AF37] transition flex items-center justify-center gap-1.5"
                title="Add to Cart"
              >
                <ShoppingCart size={13} className="text-[#D4AF37]" />
                <span>Add to Cart</span>
              </button>

              <button
                type="button"
                onClick={(e) => {
                  e.stopPropagation();
                  onBuyNow ? onBuyNow(product) : (onAddToCart ? onAddToCart(product) : onSelect?.(product));
                }}
                className="gold-btn !text-xs !py-2 !px-2.5 flex items-center justify-center gap-1 shadow-sm"
                title="Buy Now"
              >
                <Zap size={13} />
                <span>Buy Now</span>
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
