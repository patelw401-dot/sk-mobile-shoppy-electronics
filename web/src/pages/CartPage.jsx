import React from 'react';
import { 
  ShoppingBag, 
  Trash2, 
  Plus, 
  Minus, 
  ArrowRight, 
  ArrowLeft, 
  ShieldCheck, 
  Truck, 
  Tag, 
  Sparkles,
  Smartphone,
  Headphones
} from 'lucide-react';
import { STORE_INFO } from '../data/storeData';

export default function CartPage({ 
  cart, 
  onUpdateQuantity, 
  onRemoveItem, 
  onClearCart, 
  setActivePage 
}) {
  const subtotal = cart.reduce((sum, item) => {
    const price = typeof item.price === 'number' ? item.price : (parseFloat(item.price) || 0);
    const qty = typeof item.quantity === 'number' ? item.quantity : (parseInt(item.quantity, 10) || 1);
    return sum + (price * qty);
  }, 0);

  const totalItems = cart.reduce((sum, item) => sum + (item.quantity || 1), 0);
  const deliveryCharge = 0; // Free Store Pickup or Free Udgir Delivery
  const totalAmount = subtotal + deliveryCharge;

  if (cart.length === 0) {
    return (
      <div className="container py-16">
        <div className="max-w-md mx-auto text-center bg-[#141414] border border-[#222222] rounded-2xl p-8 sm:p-12 space-y-6">
          <div className="w-20 h-20 rounded-full bg-[#1F1F1F] border border-[#333333] flex items-center justify-center mx-auto text-[#D4AF37]">
            <ShoppingBag size={36} />
          </div>
          <div className="space-y-2">
            <h2 className="text-xl sm:text-2xl font-black text-white">Your Shopping Cart is Empty</h2>
            <p className="text-xs text-gray-400 leading-relaxed">
              Looks like you haven't added any smartphones or accessories yet. Explore genuine devices and original mobile parts available at our Udgir store.
            </p>
          </div>

          <div className="flex flex-col sm:flex-row gap-3 pt-2">
            <button
              onClick={() => { setActivePage('products'); window.scrollTo({ top: 0, behavior: 'smooth' }); }}
              className="gold-btn flex-1 !text-xs !py-3 flex items-center justify-center gap-2"
            >
              <Smartphone size={15} />
              <span>Shop Smartphones</span>
            </button>
            <button
              onClick={() => { setActivePage('accessories'); window.scrollTo({ top: 0, behavior: 'smooth' }); }}
              className="outline-gold-btn flex-1 !text-xs !py-3 flex items-center justify-center gap-2"
            >
              <Headphones size={15} />
              <span>Shop Accessories</span>
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="container py-8 space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-4 border-b border-[#222222] gap-4">
        <div>
          <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-[#D4AF37] mb-1">
            <Sparkles size={14} />
            <span>Store Order Cart</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-black text-white">
            Shopping Cart ({totalItems} {totalItems === 1 ? 'item' : 'items'})
          </h1>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={() => { setActivePage('products'); window.scrollTo({ top: 0, behavior: 'smooth' }); }}
            className="text-xs font-semibold text-gray-400 hover:text-white transition flex items-center gap-1.5"
          >
            <ArrowLeft size={14} />
            <span>Continue Shopping</span>
          </button>
          <button
            onClick={onClearCart}
            className="text-xs text-red-400 hover:text-red-300 transition flex items-center gap-1 px-2.5 py-1 rounded bg-red-500/10 border border-red-500/20"
          >
            <Trash2 size={12} />
            <span>Clear Cart</span>
          </button>
        </div>
      </div>

      {/* Cart Content: Items + Summary */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Items List */}
        <div className="lg:col-span-2 space-y-4">
          {cart.map((item) => {
            const itemPrice = typeof item.price === 'number' ? item.price : (parseFloat(item.price) || 0);
            const itemQty = typeof item.quantity === 'number' ? item.quantity : 1;
            const itemSubtotal = itemPrice * itemQty;

            return (
              <div 
                key={item.id}
                className="p-4 sm:p-5 rounded-2xl bg-[#141414] border border-[#262626] flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 transition hover:border-[#383838]"
              >
                {/* Thumbnail & Title */}
                <div className="flex items-center gap-4 flex-1">
                  <div className="w-20 h-20 rounded-xl bg-[#1C1C1C] border border-[#2E2E2E] flex items-center justify-center p-2 shrink-0 overflow-hidden">
                    {item.imageUrl ? (
                      <img 
                        src={item.imageUrl} 
                        alt={item.name} 
                        className="w-full h-full object-contain"
                        onError={(e) => { e.currentTarget.style.display = 'none'; }}
                      />
                    ) : (
                      <Tag className="text-[#D4AF37]" size={24} />
                    )}
                  </div>

                  <div className="space-y-1">
                    <span className="text-[10px] uppercase font-bold text-gray-500 tracking-wider">
                      {item.brand || 'SK Mobile'}
                    </span>
                    <h3 className="text-sm font-bold text-white line-clamp-1">
                      {item.name}
                    </h3>
                    <div className="flex items-baseline gap-2">
                      <span className="text-sm font-black text-[#D4AF37]">
                        ₹{itemPrice.toLocaleString('en-IN')}
                      </span>
                      {item.originalPrice && item.originalPrice > itemPrice && (
                        <span className="text-xs text-gray-500 line-through">
                          ₹{Number(item.originalPrice).toLocaleString('en-IN')}
                        </span>
                      )}
                    </div>
                  </div>
                </div>

                {/* Quantity Controls & Subtotal */}
                <div className="flex items-center justify-between sm:justify-end gap-6 w-full sm:w-auto pt-3 sm:pt-0 border-t sm:border-t-0 border-[#222222]">
                  {/* Quantity Stepper */}
                  <div className="flex items-center gap-2 bg-[#0E0E0E] border border-[#2C2C2C] rounded-lg p-1">
                    <button
                      onClick={() => onUpdateQuantity(item.id, -1)}
                      className="p-1 rounded text-gray-400 hover:text-white hover:bg-[#222222] transition"
                      aria-label="Decrease quantity"
                    >
                      <Minus size={14} />
                    </button>
                    <span className="w-8 text-center text-xs font-bold text-white">
                      {itemQty}
                    </span>
                    <button
                      onClick={() => onUpdateQuantity(item.id, 1)}
                      className="p-1 rounded text-gray-400 hover:text-white hover:bg-[#222222] transition"
                      aria-label="Increase quantity"
                    >
                      <Plus size={14} />
                    </button>
                  </div>

                  {/* Subtotal */}
                  <div className="text-right min-w-[90px]">
                    <div className="text-xs text-gray-500">Subtotal</div>
                    <div className="text-sm font-black text-white">
                      ₹{itemSubtotal.toLocaleString('en-IN')}
                    </div>
                  </div>

                  {/* Remove Button */}
                  <button
                    onClick={() => onRemoveItem(item.id)}
                    className="p-2 text-gray-500 hover:text-red-400 hover:bg-red-500/10 rounded-lg transition"
                    title="Remove item"
                  >
                    <Trash2 size={16} />
                  </button>
                </div>
              </div>
            );
          })}
        </div>

        {/* Order Summary Sidebar */}
        <div className="space-y-4">
          <div className="p-6 rounded-2xl bg-[#141414] border border-[#2E2E2E] space-y-6 sticky top-24">
            <h2 className="text-base font-black text-white border-b border-[#222222] pb-3">
              Order Summary
            </h2>

            <div className="space-y-3 text-xs text-gray-300">
              <div className="flex justify-between">
                <span className="text-gray-400">Items Subtotal ({totalItems})</span>
                <span className="font-semibold text-white">₹{subtotal.toLocaleString('en-IN')}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-400">Store Pickup / Delivery</span>
                <span className="font-bold text-emerald-400">FREE (Udgir)</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-400">GST / All Taxes</span>
                <span className="font-semibold text-gray-400">Included</span>
              </div>

              <div className="pt-3 border-t border-[#262626] flex justify-between items-baseline">
                <span className="text-sm font-bold text-white">Estimated Total</span>
                <span className="text-xl font-black text-[#D4AF37]">
                  ₹{totalAmount.toLocaleString('en-IN')}
                </span>
              </div>
            </div>

            <button
              onClick={() => { setActivePage('checkout'); window.scrollTo({ top: 0, behavior: 'smooth' }); }}
              className="gold-btn w-full !py-3.5 !text-sm flex items-center justify-center gap-2 shadow-lg shadow-[#D4AF37]/20 font-bold"
            >
              <span>Proceed to Checkout</span>
              <ArrowRight size={16} />
            </button>

            {/* Udgir Local Assurance */}
            <div className="space-y-2 pt-2 border-t border-[#222222] text-[11px] text-gray-400">
              <div className="flex items-center gap-2 text-gray-300 font-medium">
                <ShieldCheck size={14} className="text-[#D4AF37] shrink-0" />
                <span>100% Genuine OEM Warranty</span>
              </div>
              <div className="flex items-center gap-2 text-gray-300 font-medium">
                <Truck size={14} className="text-emerald-400 shrink-0" />
                <span>Same-day Udgir delivery & instant pickup</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
