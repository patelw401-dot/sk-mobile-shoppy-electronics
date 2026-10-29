import React, { useEffect } from 'react';
import { CheckCircle2, AlertCircle, Info, X } from 'lucide-react';

export default function Toast({ message, type = 'success', onClose, duration = 4000 }) {
  useEffect(() => {
    if (!message) return;
    const timer = setTimeout(() => {
      onClose();
    }, duration);
    return () => clearTimeout(timer);
  }, [message, duration, onClose]);

  if (!message) return null;

  const typeConfig = {
    success: {
      bg: 'bg-[#122A1A] border-emerald-500/40 text-emerald-200',
      icon: CheckCircle2,
      iconColor: 'text-emerald-400'
    },
    error: {
      bg: 'bg-[#2A1212] border-red-500/40 text-red-200',
      icon: AlertCircle,
      iconColor: 'text-red-400'
    },
    info: {
      bg: 'bg-[#1C1808] border-[#D4AF37]/50 text-[#F5E6B3]',
      icon: Info,
      iconColor: 'text-[#D4AF37]'
    }
  };

  const config = typeConfig[type] || typeConfig.info;
  const Icon = config.icon;

  return (
    <div className="fixed bottom-6 right-6 z-50 max-w-md animate-in slide-in-from-bottom duration-300">
      <div className={`flex items-start gap-3 p-4 rounded-xl border shadow-xl ${config.bg} backdrop-blur-md`}>
        <Icon size={20} className={`${config.iconColor} shrink-0 mt-0.5`} />
        <div className="flex-1 text-sm font-medium">
          {message}
        </div>
        <button
          onClick={onClose}
          className="text-gray-400 hover:text-white transition p-1"
          aria-label="Close"
        >
          <X size={16} />
        </button>
      </div>
    </div>
  );
}
