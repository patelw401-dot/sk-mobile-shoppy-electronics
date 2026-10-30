import React, { useState, useEffect } from 'react';
import { 
  fetchCustomerOrders, 
  cancelCustomerOrder, 
  signInWithGooglePopup 
} from '../firebase';
import { STORE_INFO } from '../data/storeData';
import { 
  Package, 
  Clock, 
  CheckCircle2, 
  AlertCircle, 
  XCircle, 
  RefreshCw, 
  Phone, 
  MapPin, 
  ArrowLeft,
  Truck,
  User,
  ShoppingBag
} from 'lucide-react';

export default function MyOrdersPage({ currentUser, setActivePage, onShowToast }) {
  const [orders, setOrders] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [cancellingId, setCancellingId] = useState(null);

  const loadOrders = async () => {
    if (!currentUser) return;
    setIsLoading(true);
    try {
      const list = await fetchCustomerOrders(currentUser);
      setOrders(list);
    } catch (err) {
      console.error('Failed to load customer orders:', err);
      onShowToast?.('Error loading order history.', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadOrders();
  }, [currentUser]);

  const handleCancelOrder = async (orderId) => {
    if (!window.confirm(`Are you sure you want to cancel order #${orderId}?`)) return;
    setCancellingId(orderId);
    try {
      await cancelCustomerOrder(orderId, currentUser, 'Cancelled by customer via portal');
      onShowToast?.(`Order #${orderId} was cancelled.`, 'info');
      await loadOrders();
    } catch (err) {
      console.error('Cancellation error:', err);
      onShowToast?.(err.message || 'Failed to cancel order.', 'error');
    } finally {
      setCancellingId(null);
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'Pending':
      case 'Order Placed':
        return <span className="px-2.5 py-0.5 rounded-full bg-amber-500/20 text-amber-300 font-bold border border-amber-500/30 text-[11px]">Pending</span>;
      case 'Order Confirmed':
      case 'Confirmed':
        return <span className="px-2.5 py-0.5 rounded-full bg-blue-500/20 text-blue-300 font-bold border border-blue-500/30 text-[11px]">Confirmed</span>;
      case 'Processing':
        return <span className="px-2.5 py-0.5 rounded-full bg-purple-500/20 text-purple-300 font-bold border border-purple-500/30 text-[11px]">Processing</span>;
      case 'Shipped':
        return <span className="px-2.5 py-0.5 rounded-full bg-cyan-500/20 text-cyan-300 font-bold border border-cyan-500/30 text-[11px]">Out for Delivery</span>;
      case 'Delivered':
        return <span className="px-2.5 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 font-bold border border-emerald-500/30 text-[11px]">Delivered</span>;
      case 'Cancelled':
        return <span className="px-2.5 py-0.5 rounded-full bg-red-500/20 text-red-300 font-bold border border-red-500/30 text-[11px]">Cancelled</span>;
      default:
        return <span className="px-2.5 py-0.5 rounded-full bg-gray-500/20 text-gray-300 font-bold border border-gray-500/30 text-[11px]">{status || 'Submitted'}</span>;
    }
  };

  if (!currentUser) {
    return (
      <div className="container py-16">
        <div className="max-w-md mx-auto text-center bg-[#141414] border border-[#222222] rounded-2xl p-8 sm:p-12 space-y-6">
          <div className="w-16 h-16 rounded-full bg-[#1F1F1F] text-[#D4AF37] flex items-center justify-center mx-auto">
            <User size={32} />
          </div>
          <div className="space-y-2">
            <h2 className="text-xl font-black text-white">Sign In to View Your Orders</h2>
            <p className="text-xs text-gray-400 leading-relaxed">
              Log in with your customer account to check order status, delivery progress, and past receipts.
            </p>
          </div>
          <button
            onClick={() => signInWithGooglePopup()}
            className="gold-btn w-full !py-3 !text-xs font-bold"
          >
            <span>Sign In with Google</span>
          </button>
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
            <Package size={14} />
            <span>Order History</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-black text-white">
            My Orders ({orders.length})
          </h1>
          <p className="text-xs text-gray-400 mt-0.5">
            Account: <strong>{currentUser.email}</strong>
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={loadOrders}
            disabled={isLoading}
            className="py-2 px-3 rounded-xl bg-[#1C1C1C] hover:bg-[#262626] border border-[#333333] text-xs font-semibold text-gray-300 hover:text-white transition flex items-center gap-1.5"
          >
            <RefreshCw size={13} className={isLoading ? 'animate-spin text-[#D4AF37]' : ''} />
            <span>Refresh</span>
          </button>
          <button
            onClick={() => setActivePage('products')}
            className="gold-btn !text-xs !py-2 !px-3.5 flex items-center gap-1.5 font-bold"
          >
            <ShoppingBag size={14} />
            <span>Shop More</span>
          </button>
        </div>
      </div>

      {isLoading ? (
        <div className="text-center py-16 text-xs text-gray-400 flex items-center justify-center gap-2">
          <RefreshCw size={16} className="animate-spin text-[#D4AF37]" />
          <span>Fetching your store orders...</span>
        </div>
      ) : orders.length === 0 ? (
        <div className="text-center py-16 bg-[#141414] rounded-2xl border border-[#222222] space-y-4 max-w-lg mx-auto p-8">
          <Package size={40} className="text-gray-500 mx-auto" />
          <h3 className="text-lg font-bold text-white">No Orders Found Yet</h3>
          <p className="text-xs text-gray-400 leading-relaxed">
            You haven't placed any store orders yet. Browse our smartphones and original accessories.
          </p>
          <button
            onClick={() => setActivePage('products')}
            className="gold-btn !text-xs !py-2.5 !px-5 inline-flex items-center gap-2 font-bold"
          >
            <span>Start Shopping</span>
          </button>
        </div>
      ) : (
        <div className="space-y-6">
          {orders.map((ord) => {
            const formattedDate = ord.createdAt?.toDate 
              ? ord.createdAt.toDate().toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
              : 'Recent Order';

            const canCancel = (ord.orderStatus === 'Pending' || ord.orderStatus === 'Order Placed');

            return (
              <div 
                key={ord.id}
                className="bg-[#141414] border border-[#262626] rounded-2xl p-5 sm:p-6 space-y-4 transition hover:border-[#333333]"
              >
                {/* Top Info Bar */}
                <div className="flex flex-wrap items-center justify-between gap-3 border-b border-[#222222] pb-3">
                  <div>
                    <span className="text-[10px] text-gray-500 uppercase font-bold block">Order ID</span>
                    <span className="text-sm font-black text-[#D4AF37] font-mono">#{ord.id}</span>
                  </div>
                  <div>
                    <span className="text-[10px] text-gray-500 uppercase font-bold block">Placed On</span>
                    <span className="text-xs text-gray-300">{formattedDate}</span>
                  </div>
                  <div>
                    <span className="text-[10px] text-gray-500 uppercase font-bold block">Fulfillment</span>
                    <span className="text-xs text-gray-300">{ord.deliveryType || 'Store Pickup'}</span>
                  </div>
                  <div>
                    <span className="text-[10px] text-gray-500 uppercase font-bold block">Total Amount</span>
                    <span className="text-sm font-black text-white">₹{ord.total?.toLocaleString('en-IN')}</span>
                  </div>
                  <div>
                    <span className="text-[10px] text-gray-500 uppercase font-bold block mb-0.5">Status</span>
                    {getStatusBadge(ord.orderStatus)}
                  </div>
                </div>

                {/* Items List */}
                <div className="space-y-2">
                  <span className="text-[11px] font-bold text-gray-400 uppercase tracking-wider block">
                    Items in this Order
                  </span>
                  <div className="divide-y divide-[#1C1C1C]">
                    {ord.items?.map((it, i) => (
                      <div key={i} className="py-2 flex items-center justify-between gap-3 text-xs">
                        <div className="flex items-center gap-3">
                          <span className="text-white font-bold">{it.name}</span>
                          <span className="text-[10px] text-gray-500 px-1.5 py-0.5 rounded bg-[#1C1C1C]">
                            Qty: {it.quantity}
                          </span>
                        </div>
                        <span className="font-bold text-gray-200">
                          ₹{(it.price * it.quantity).toLocaleString('en-IN')}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Footer Controls & Cancellation */}
                <div className="flex flex-wrap items-center justify-between gap-3 pt-3 border-t border-[#222222] text-xs">
                  <div className="text-gray-400 text-[11px]">
                    <strong>Delivery Address:</strong> {ord.address}, {ord.city} ({ord.pincode}) • Phone: {ord.phone}
                  </div>

                  <div className="flex items-center gap-2">
                    <a
                      href={`tel:${STORE_INFO.phone}`}
                      className="px-3 py-1.5 rounded-lg bg-[#222222] hover:bg-[#2A2A2A] text-gray-300 hover:text-white transition flex items-center gap-1.5 text-xs"
                    >
                      <Phone size={12} />
                      <span>Inquire: {STORE_INFO.phone}</span>
                    </a>

                    {canCancel && (
                      <button
                        onClick={() => handleCancelOrder(ord.id)}
                        disabled={cancellingId === ord.id}
                        className="px-3 py-1.5 rounded-lg bg-red-500/10 hover:bg-red-500/20 text-red-400 border border-red-500/30 text-xs font-semibold transition"
                      >
                        {cancellingId === ord.id ? 'Cancelling...' : 'Cancel Order'}
                      </button>
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
