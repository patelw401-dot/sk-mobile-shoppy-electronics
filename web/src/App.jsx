import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import Toast from './components/Toast';
import { subscribeToAuthChanges } from './firebase';

import HomePage from './pages/HomePage';
import ProductsPage from './pages/ProductsPage';
import AccessoriesPage from './pages/AccessoriesPage';
import RepairBookingPage from './pages/RepairBookingPage';
import OldPhonePage from './pages/OldPhonePage';
import LoginPage from './pages/LoginPage';
import ContactPage from './pages/ContactPage';

export default function App() {
  const [activePage, setActivePage] = useState('home');
  const [toast, setToast] = useState({ message: '', type: 'info' });
  const [currentUser, setCurrentUser] = useState(null);
  const [isAuthLoading, setIsAuthLoading] = useState(true);

  useEffect(() => {
    const unsubscribe = subscribeToAuthChanges((user) => {
      setCurrentUser(user);
      setIsAuthLoading(false);
    });
    return () => unsubscribe();
  }, []);

  const showToast = (message, type = 'info') => {
    setToast({ message, type });
  };

  const renderPage = () => {
    switch (activePage) {
      case 'home':
        return <HomePage setActivePage={setActivePage} onShowToast={showToast} />;
      case 'products':
        return <ProductsPage onShowToast={showToast} />;
      case 'accessories':
        return <AccessoriesPage onShowToast={showToast} />;
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
      default:
        return <HomePage setActivePage={setActivePage} onShowToast={showToast} />;
    }
  };

  return (
    <div className="min-h-screen flex flex-col bg-[#0A0A0A] text-[#F5F5F7] selection:bg-[#D4AF37] selection:text-black">
      {/* Navigation Header */}
      <Navbar 
        activePage={activePage} 
        setActivePage={setActivePage} 
        currentUser={currentUser}
        onShowToast={showToast}
      />

      {/* Main Page Body */}
      <main className="flex-1">
        {renderPage()}
      </main>

      {/* Footer */}
      <Footer setActivePage={setActivePage} />

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
