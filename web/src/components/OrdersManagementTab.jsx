import React, { useState, useEffect, useMemo } from 'react';
import { 
  fetchAdminOrders, 
  updateAdminOrderStatus, 
  isUserAdmin 
} from '../firebase';
import { 
  Package, 
  Search, 
  Filter, 
  RefreshCw, 
  Phone, 
  MapPin, 
  Clock, 
  CheckCircle2, 
  AlertCircle, 
  XCircle, 
  Truck, 
  DollarSign, 
  ShieldCheck, 
  ChevronRight,
  User,
  Calendar,
  Layers
} from 'lucide-react';

export default function OrdersManagementTab({ currentUser, onShowToast }) {
  const [orders, setOrders] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isUpdating, setIsUpdating] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [selectedOrder, setSelectedOrder] = useState(null);

  const loadOrders = async () => {
    setIsLoading(true);
    try {
      const list = await fetchAdminOrders();
      setOrders(list);
    } catch (err) {
      console.error('Failed to load admin orders:', err);
      onShowToast?.('Failed to load orders from Firestore.', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadOrders();
  }, []);

  const handleStatusUpdate = async (orderId, newStatus) => {
    if (!isUserAdmin(currentUser)) {
      onShowToast?.('Unauthorized action.', 'error');
      return;
    }

    setIsUpdating(true);
    try {
      await updateAdminOrderStatus(orderId, newStatus, currentUser?.email);
      setOrders(prev => prev.map(o => o.id === orderId ? { ...o, orderStatus: newStatus, status: newStatus } : o));
      onShowToast?.(`Order #${orderId} marked as ${newStatus}`, 'success');
    } catch (err) {
      console.error('Status update failed:', err);
      onShowToast?.(err.message || 'Error updating order status.', 'error');
    } finally {
      setIsUpdating(false);
    }
  };

  const filteredOrders = useMemo(() => {
    return orders.filter(o => {
      const q = searchQuery.toLowerCase().trim();
      const matchesSearch = !q ||
        (o.id || '').toLowerCase().includes(q) ||
        (o.customerName || '').toLowerCase().includes(q) ||
        (o.phone || '').includes(q) ||
        (o.customerEmail || '').toLowerCase().includes(q);

      const matchesStatus = statusFilter === 'ALL' || o.orderStatus === statusFilter;
      return matchesSearch && matchesStatus;
    });
  }, [orders, searchQuery, statusFilter]);

  // Quick KPI Counters
  const countPending = orders.filter(o => o.orderStatus === 'Pending' || o.orderStatus === 'Order Placed').length;
  const countConfirmed = orders.filter(o => o.orderStatus === 'Confirmed' || o.orderStatus === 'Order Confirmed').length;
  const countProcessing = orders.filter(o => o.orderStatus === 'Processing').length;
  const countShipped = orders.filter(o => o.orderStatus === 'Shipped').length;
  const countDelivered = orders.filter(o => o.orderStatus === 'Delivered').length;
  const countCancelled = orders.filter(o => o.orderStatus === 'Cancelled').length;

  const totalRevenue = orders
    .filter(o => o.orderStatus !== 'Cancelled')
    .reduce((sum, o) => sum + (o.total || 0), 0);

  const getStatusBadge = (status) => {
    switch (status) {
      case 'Pending':
      case 'Order Placed':
        return <span className="px-2.5 py-0.5 rounded-full bg-amber-500/20 text-amber-300 font-bold border border-amber-500/40 text-[11px]">Pending</span>;
      case 'Order Confirmed':
      case 'Confirmed':
        return <span className="px-2.5 py-0.5 rounded-full bg-blue-500/20 text-blue-300 font-bold border border-blue-500/40 text-[11px]">Confirmed</span>;
      case 'Processing':
        return <span className="px-2.5 py-0.5 rounded-full bg-purple-500/20 text-purple-300 font-bold border border-purple-500/40 text-[11px]">Processing</span>;
      case 'Shipped':
        return <span className="px-2.5 py-0.5 rounded-full bg-cyan-500/20 text-cyan-300 font-bold border border-cyan-500/40 text-[11px]">Shipped</span>;
      case 'Delivered':
        return <span className="px-2.5 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 font-bold border border-emerald-500/40 text-[11px]">Delivered</span>;
      case 'Cancelled':
        return <span className="px-2.5 py-0.5 rounded-full bg-red-500/20 text-red-300 font-bold border border-red-500/40 text-[11px]">Cancelled</span>;
      default:
        return <span className="px-2.5 py-0.5 rounded-full bg-gray-500/20 text-gray-300 font-bold border border-gray-500/40 text-[11px]">{status || 'Pending'}</span>;
    }
  };

  return (
    <div className="space-y-6">
      {/* KPI Counters */}
      <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-7 gap-3">
        <div className="p-3.5 rounded-xl bg-[#141414] border border-[#242424]">
          <span className="text-[10px] uppercase font-bold text-gray-400 block mb-1">Total Orders</span>
          <div className="text-xl font-black text-white">{orders.length}</div>
        </div>

        <div className="p-3.5 rounded-xl bg-[#141414] border border-[#242424]">
          <span className="text-[10px] uppercase font-bold text-amber-400 block mb-1">Pending</span>
          <div className="text-xl font-black text-amber-300">{countPending}</div>
        </div>

        <div className="p-3.5 rounded-xl bg-[#141414] border border-[#242424]">
          <span className="text-[10px] uppercase font-bold text-blue-400 block mb-1">Confirmed</span>
          <div className="text-xl font-black text-blue-300">{countConfirmed}</div>
        </div>

        <div className="p-3.5 rounded-xl bg-[#141414] border border-[#242424]">
          <span className="text-[10px] uppercase font-bold text-purple-400 block mb-1">Processing</span>
          <div className="text-xl font-black text-purple-300">{countProcessing}</div>
        </div>

        <div className="p-3.5 rounded-xl bg-[#141414] border border-[#242424]">
          <span className="text-[10px] uppercase font-bold text-cyan-400 block mb-1">Shipped</span>
          <div className="text-xl font-black text-cyan-300">{countShipped}</div>
        </div>

        <div className="p-3.5 rounded-xl bg-[#141414] border border-[#242424]">
          <span className="text-[10px] uppercase font-bold text-emerald-400 block mb-1">Delivered</span>
          <div className="text-xl font-black text-emerald-300">{countDelivered}</div>
        </div>

        <div className="p-3.5 rounded-xl bg-[#141414] border border-[#242424] col-span-2 sm:col-span-1">
          <span className="text-[10px] uppercase font-bold text-[#D4AF37] block mb-1">Gross Value</span>
          <div className="text-base font-black text-[#D4AF37] truncate">₹{totalRevenue.toLocaleString('en-IN')}</div>
        </div>
      </div>

      {/* Search & Status Filter Bar */}
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 bg-[#111111] p-3 rounded-xl border border-[#222222]">
        <div className="relative flex-1">
          <Search size={15} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-500" />
          <input
            type="text"
            placeholder="Search by Order ID, customer name, mobile, email..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full bg-[#0A0A0A] border border-[#262626] rounded-lg pl-9 pr-4 py-2 text-xs text-white focus:outline-none focus:border-[#D4AF37]"
          />
        </div>

        <div className="flex items-center gap-2">
          <Filter size={14} className="text-gray-400" />
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="bg-[#0A0A0A] border border-[#262626] rounded-lg px-3 py-2 text-xs text-white focus:outline-none focus:border-[#D4AF37]"
          >
            <option value="ALL">All Statuses ({orders.length})</option>
            <option value="Pending">Pending ({countPending})</option>
            <option value="Confirmed">Confirmed ({countConfirmed})</option>
            <option value="Processing">Processing ({countProcessing})</option>
            <option value="Shipped">Shipped ({countShipped})</option>
            <option value="Delivered">Delivered ({countDelivered})</option>
            <option value="Cancelled">Cancelled ({countCancelled})</option>
          </select>

          <button
            onClick={loadOrders}
            disabled={isLoading}
            className="p-2 rounded-lg bg-[#1C1C1C] hover:bg-[#262626] border border-[#2E2E2E] text-gray-300 hover:text-white transition"
            title="Refresh orders"
          >
            <RefreshCw size={14} className={isLoading ? 'animate-spin text-[#D4AF37]' : ''} />
          </button>
        </div>
      </div>

      {/* Orders List */}
      {isLoading ? (
        <div className="text-center py-16 text-xs text-gray-400 flex items-center justify-center gap-2">
          <RefreshCw size={16} className="animate-spin text-[#D4AF37]" />
          <span>Loading orders from Cloud Firestore...</span>
        </div>
      ) : filteredOrders.length === 0 ? (
        <div className="text-center py-16 bg-[#141414] rounded-2xl border border-[#222222] p-8 space-y-2">
          <Package size={36} className="text-gray-500 mx-auto" />
          <p className="text-sm font-bold text-white">No Orders Found</p>
          <p className="text-xs text-gray-400">
            {searchQuery || statusFilter !== 'ALL' 
              ? 'Try adjusting your search query or filter.' 
              : 'Customer orders placed through the website will appear here in real time.'}
          </p>
        </div>
      ) : (
        <div className="space-y-4">
          {filteredOrders.map((ord) => {
            const formattedDate = ord.createdAt?.toDate 
              ? ord.createdAt.toDate().toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
              : 'Recent Order';

            return (
              <div 
                key={ord.id}
                className="bg-[#141414] border border-[#262626] rounded-2xl p-5 sm:p-6 space-y-4 transition hover:border-[#333333]"
              >
                {/* Header Row */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-[#222222] pb-3">
                  <div className="flex items-center gap-3">
                    <span className="font-mono text-xs font-black text-[#D4AF37]">#{ord.id}</span>
                    <span className="text-xs text-gray-500">•</span>
                    <span className="text-xs text-gray-400">{formattedDate}</span>
                  </div>

                  <div className="flex items-center gap-3">
                    <span className="text-xs font-bold text-gray-300">
                      Total: <span className="text-white font-black text-sm">₹{ord.total?.toLocaleString('en-IN')}</span>
                    </span>
                    {getStatusBadge(ord.orderStatus)}
                  </div>
                </div>

                {/* Grid: Customer + Items */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
                  {/* Customer & Delivery Column */}
                  <div className="space-y-2.5 p-3.5 rounded-xl bg-[#0D0D0D] border border-[#222222]">
                    <div className="flex items-center justify-between">
                      <span className="text-[10px] uppercase font-bold text-gray-500">Customer Details</span>
                      <span className="text-[10px] text-gray-400">{ord.deliveryType || 'Store Pickup'}</span>
                    </div>

                    <div className="space-y-1">
                      <div className="font-bold text-white text-sm">{ord.customerName}</div>
                      <div className="flex items-center gap-2 text-gray-300">
                        <Phone size={12} className="text-[#D4AF37]" />
                        <a href={`tel:${ord.phone}`} className="text-[#D4AF37] hover:underline font-semibold font-mono">
                          {ord.phone}
                        </a>
                        {ord.alternatePhone && (
                          <span className="text-gray-500 font-mono">/ {ord.alternatePhone}</span>
                        )}
                      </div>
                      {ord.customerEmail && (
                        <div className="text-[11px] text-gray-400 truncate">
                          Email: {ord.customerEmail}
                        </div>
                      )}
                    </div>

                    <div className="pt-2 border-t border-[#1C1C1C] text-[11px] text-gray-300">
                      <div className="flex items-start gap-1.5">
                        <MapPin size={13} className="text-emerald-400 shrink-0 mt-0.5" />
                        <span>{ord.address}, {ord.city} ({ord.pincode})</span>
                      </div>
                    </div>

                    <div className="pt-1.5 border-t border-[#1C1C1C] flex justify-between text-[11px] text-gray-400">
                      <span>Payment: <strong className="text-white">{ord.paymentMethod}</strong></span>
                      <span>Status: <strong className={ord.paymentStatus === 'Paid' ? 'text-emerald-400' : 'text-amber-400'}>{ord.paymentStatus || 'Pending'}</strong></span>
                    </div>

                    {ord.notes && (
                      <div className="text-[11px] text-amber-300 bg-amber-500/10 p-2 rounded border border-amber-500/20">
                        <strong>Note:</strong> {ord.notes}
                      </div>
                    )}
                  </div>

                  {/* Items Ordered Column */}
                  <div className="space-y-2 p-3.5 rounded-xl bg-[#0D0D0D] border border-[#222222] flex flex-col justify-between">
                    <div>
                      <span className="text-[10px] uppercase font-bold text-gray-500 block mb-2">
                        Ordered Products ({ord.items?.length || 0})
                      </span>

                      <div className="divide-y divide-[#1C1C1C] max-h-40 overflow-y-auto pr-1">
                        {ord.items?.map((it, idx) => (
                          <div key={idx} className="py-1.5 flex items-center justify-between gap-2 text-xs">
                            <div className="min-w-0">
                              <span className="text-white font-bold block truncate">{it.name}</span>
                              <span className="text-gray-500 text-[11px]">
                                Qty: {it.quantity} × ₹{it.price.toLocaleString('en-IN')}
                              </span>
                            </div>
                            <span className="font-mono text-gray-200 font-bold shrink-0">
                              ₹{(it.price * it.quantity).toLocaleString('en-IN')}
                            </span>
                          </div>
                        ))}
                      </div>
                    </div>

                    <div className="pt-2 border-t border-[#1C1C1C] flex items-center justify-between text-xs">
                      <span className="text-gray-400">Total Bill:</span>
                      <span className="text-[#D4AF37] font-black text-sm">
                        ₹{ord.total?.toLocaleString('en-IN')}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Status Action Buttons */}
                <div className="pt-3 border-t border-[#222222] flex flex-wrap items-center justify-between gap-3 text-xs">
                  <div className="flex items-center gap-2">
                    <span className="text-gray-400 text-[11px] font-semibold">Update Status:</span>
                    <div className="flex flex-wrap gap-1.5">
                      {['Pending', 'Confirmed', 'Processing', 'Shipped', 'Delivered', 'Cancelled'].map((st) => (
                        <button
                          key={st}
                          disabled={isUpdating || ord.orderStatus === st}
                          onClick={() => handleStatusUpdate(ord.id, st)}
                          className={`px-2.5 py-1 rounded text-[11px] font-bold transition ${
                            ord.orderStatus === st
                              ? 'bg-[#D4AF37] text-black shadow-sm'
                              : 'bg-[#222222] hover:bg-[#2E2E2E] text-gray-300 hover:text-white'
                          }`}
                        >
                          {st}
                        </button>
                      ))}
                    </div>
                  </div>

                  <a
                    href={`tel:${ord.phone}`}
                    className="py-1 px-3 rounded-lg bg-emerald-600/20 text-emerald-300 border border-emerald-500/40 hover:bg-emerald-600/30 font-bold transition flex items-center gap-1.5"
                  >
                    <Phone size={12} />
                    <span>Call Customer</span>
                  </a>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
