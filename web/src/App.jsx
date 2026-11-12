import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import Toast from './components/Toast';
import ProductDetailsModal from './components/ProductDetailsModal';
import { REAL_PRODUCTS, ACCESSORIES, PRE_OWNED_PHONES } from './data/storeData';
import { subscribeToAuthChanges, fetchFirestoreProducts, subscribeToFirestoreProducts } from './firebase';

import HomePage from './pages/HomePage';
import ProductsPage from './pages/ProductsPage';
import AccessoriesPage from './pages/AccessoriesPage';
import RepairBookingPage from './pages/RepairBookingPage';
import OldPhonePage from './pages/OldPhonePage';
import LoginPage from './pages/LoginPage';
import ContactPage from './pages/ContactPage';
import AdminDashboardPage from './pages/AdminDashboardPage';
import CartPage from './pages/CartPage';
import CheckoutPage from './pages/CheckoutPage';
import MyOrdersPage from './pages/MyOrdersPage';
import AppDownloadPage from './pages/AppDownloadPage';

export default function App() {
  const [activePage, setActivePage] = useState('home');
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [toast, setToast] = useState({ message: '', type: 'info' });
  const [currentUser, setCurrentUser] = useState(null);
  const [isAuthLoading, setIsAuthLoading] = useState(true);
  const [selectedProductForDetails, setSelectedProductForDetails] = useState(null);
  const [allStoreProducts, setAllStoreProducts] = useState(() => [
    ...(Array.isArray(REAL_PRODUCTS) ? REAL_PRODUCTS : []),
    ...(Array.isArray(ACCESSORIES) ? ACCESSORIES : []),
    ...(Array.isArray(PRE_OWNED_PHONES) ? PRE_OWNED_PHONES : [])
  ]);

  // Cart State with LocalStorage Persistence
  const [cart, setCart] = useState(() => {
    try {
      const saved = localStorage.getItem('sk_mobile_cart');
      return saved ? JSON.parse(saved) : [];
    } catch (e) {
      console.warn('Failed to load cart from storage:', e);
      return [];
    }
  });

  useEffect(() => {
    try {
      localStorage.setItem('sk_mobile_cart', JSON.stringify(cart));
    } catch (e) {
      console.warn('Failed to persist cart to storage:', e);
    }
  }, [cart]);

  useEffect(() => {
    const unsubscribe = subscribeToAuthChanges((user) => {
      setCurrentUser(user);
      setIsAuthLoading(false);
    });
    return () => unsubscribe();
  }, []);

  useEffect(() => {
    const unsubscribe = subscribeToFirestoreProducts((res) => {
      if (res && res.success && Array.isArray(res.data) && res.data.length > 0) {
        const validLiveProds = res.data.filter(p => p && p.id);
        const liveIds = new Set(validLiveProds.map(p => p.id));
        const allDefaults = [
          ...(Array.isArray(REAL_PRODUCTS) ? REAL_PRODUCTS : []),
          ...(Array.isArray(ACCESSORIES) ? ACCESSORIES : []),
          ...(Array.isArray(PRE_OWNED_PHONES) ? PRE_OWNED_PHONES : [])
        ];
        const nonDupDefaults = allDefaults.filter(d => d && d.id && !liveIds.has(d.id));
        setAllStoreProducts([...validLiveProds, ...nonDupDefaults]);
      } else if (res && res.success && res.isEmpty) {
        setAllStoreProducts([]);
      }
    }, (err) => {
      console.warn('Real-time products subscription notice:', err);
    });
    return () => {
      if (unsubscribe) unsubscribe();
    };
  }, []);

  const showToast = (message, type = 'info') => {
    setToast({ message, type });
  };

  const handleAddToCart = (product, qty = 1) => {
    if (!product) return;
    if (product.inStock === false || (product.stock !== undefined && product.stock <= 0)) {
      showToast(`${product.name} is currently out of stock.`, 'error');
      return;
    }

    setCart((prev) => {
      const existingIndex = prev.findIndex((item) => item.id === product.id);
      if (existingIndex !== -1) {
        const updated = [...prev];
        updated[existingIndex] = {
          ...updated[existingIndex],
          quantity: (updated[existingIndex].quantity || 1) + qty
        };
        return updated;
      }
      return [
        ...prev,
        {
          id: product.id,
          name: product.name,
          brand: product.brand || 'SK Mobile',
          category: product.category || 'smartphones',
          price: product.price,
          originalPrice: product.originalPrice,
          imageUrl: product.imageUrl || product.image || '',
          quantity: qty,
          stock: product.stock,
          inStock: product.inStock
        }
      ];
    });
    showToast(`Added "${product.name}" to cart!`, 'success');
  };

  const handleUpdateCartQuantity = (productId, delta) => {
    setCart((prev) => {
      return prev
        .map((item) => {
          if (item.id === productId) {
            const newQty = (item.quantity || 1) + delta;
            return newQty > 0 ? { ...item, quantity: newQty } : null;
          }
          return item;
        })
        .filter(Boolean);
    });
  };

  const handleRemoveFromCart = (productId) => {
    setCart((prev) => prev.filter((item) => item.id !== productId));
    showToast('Item removed from cart.', 'info');
  };

  const handleClearCart = () => {
    setCart([]);
  };

  const handleBuyNow = (product, qty = 1) => {
    if (!product) return;
    if (product.inStock === false || (product.stock !== undefined && product.stock <= 0)) {
      showToast(`${product.name} is currently out of stock.`, 'error');
      return;
    }

    // Ensure the item exists in the cart
    setCart((prev) => {
      const existingIndex = prev.findIndex((item) => item.id === product.id);
      if (existingIndex !== -1) {
        return prev;
      }
      return [
        ...prev,
        {
          id: product.id,
          name: product.name,
          brand: product.brand || 'SK Mobile',
          category: product.category || 'smartphones',
          price: product.price,
          originalPrice: product.originalPrice,
          imageUrl: product.imageUrl || product.image || '',
          quantity: qty || 1,
          stock: product.stock,
          inStock: product.inStock
        }
      ];
    });

    setActivePage('checkout');
    window.scrollTo({ top: 0, behavior: 'smooth' });
    showToast(`Proceeding to checkout for ${product.name}`, 'info');
  };

  const totalCartCount = cart.reduce((sum, item) => sum + (item.quantity || 1), 0);

  const renderPage = () => {
    switch (activePage) {
      case 'home':
        return (
          <HomePage 
            setActivePage={setActivePage} 
            onSelectCategory={(catSlug) => {
              setSelectedCategory(catSlug);
              setActivePage('products');
            }}
            onShowToast={showToast} 
            onAddToCart={handleAddToCart}
            onBuyNow={handleBuyNow}
            onSelectProduct={setSelectedProductForDetails}
            setSelectedProduct={setSelectedProductForDetails}
          />
        );
      case 'products':
        return (
          <ProductsPage 
            selectedCategory={selectedCategory}
            setSelectedCategory={setSelectedCategory}
            onShowToast={showToast} 
            onAddToCart={handleAddToCart}
            onBuyNow={handleBuyNow}
            onSelectProduct={setSelectedProductForDetails}
          />
        );
      case 'accessories':
        return (
          <AccessoriesPage 
            onShowToast={showToast} 
            onAddToCart={handleAddToCart}
            onBuyNow={handleBuyNow}
            onSelectProduct={setSelectedProductForDetails}
          />
        );
      case 'cart':
        return (
          <CartPage 
            cart={cart}
            onUpdateQuantity={handleUpdateCartQuantity}
            onRemoveItem={handleRemoveFromCart}
            onClearCart={handleClearCart}
            setActivePage={setActivePage}
          />
        );
      case 'checkout':
        return (
          <CheckoutPage 
            cart={cart}
            currentUser={currentUser}
            onClearCart={handleClearCart}
            setActivePage={setActivePage}
            onShowToast={showToast}
          />
        );
      case 'my-orders':
        return (
          <MyOrdersPage 
            currentUser={currentUser} 
            setActivePage={setActivePage} 
            onShowToast={showToast} 
          />
        );
      case 'app-download':
        return (
          <AppDownloadPage 
            setActivePage={setActivePage}
            onShowToast={showToast}
          />
        );
      case 'repairs':
        return (
          <RepairBookingPage 
            currentUser={currentUser} 
            setActivePage={setActivePage} 
            onShowToast={showToast} 
          />
        );
      case 'old-phones':
        return (
          <OldPhonePage 
            currentUser={currentUser} 
            setActivePage={setActivePage} 
            onShowToast={showToast} 
            onSelectProduct={setSelectedProductForDetails}
            onAddToCart={handleAddToCart}
            onBuyNow={handleBuyNow}
          />
        );
      case 'login':
        return (
          <LoginPage 
            currentUser={currentUser} 
            setActivePage={setActivePage} 
            onShowToast={showToast} 
          />
        );
      case 'contact':
        return (
          <ContactPage 
            currentUser={currentUser} 
            onShowToast={showToast} 
          />
        );
      case 'admin':
        return (
          <AdminDashboardPage 
            currentUser={currentUser} 
            setActivePage={setActivePage} 
            onShowToast={showToast} 
          />
        );
      default:
        return (
          <HomePage 
            setActivePage={setActivePage} 
            onSelectCategory={(catSlug) => {
              setSelectedCategory(catSlug);
              setActivePage('products');
            }}
            onShowToast={showToast} 
            onAddToCart={handleAddToCart}
            onBuyNow={handleBuyNow}
            onSelectProduct={setSelectedProductForDetails}
            setSelectedProduct={setSelectedProductForDetails}
          />
        );
    }
  };

  return (
    <div className="min-h-screen flex flex-col bg-[#0A0A0A] text-[#F5F5F7] selection:bg-[#D4AF37] selection:text-black">
      {/* Navigation Header with Universal Search */}
      <Navbar 
        activePage={activePage} 
        setActivePage={setActivePage} 
        currentUser={currentUser}
        cartCount={totalCartCount}
        onShowToast={showToast}
        allProducts={allStoreProducts}
        onSelectProduct={setSelectedProductForDetails}
      />

      {/* Main Page Body */}
      <main className="flex-1">
        {renderPage()}
      </main>

      {/* Footer */}
      <Footer setActivePage={setActivePage} />

      {/* Universal Product Details Modal (16-point feature set for EVERY product category) */}
      {selectedProductForDetails && (
        <ProductDetailsModal
          product={selectedProductForDetails}
          onClose={() => setSelectedProductForDetails(null)}
          onAddToCart={handleAddToCart}
          onBuyNow={handleBuyNow}
          onSelectProduct={setSelectedProductForDetails}
          allProducts={allStoreProducts}
        />
      )}

      {/* Global Toast Notification */}
      {toast.message && (
        <Toast
          message={toast.message}
          type={toast.type}
          onClose={() => setToast({ message: '', type: 'info' })}
        />
      )}
    </div>
  );
}
