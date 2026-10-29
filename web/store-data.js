// Default store dataset matching the SK Mobile Udgir catalog
export const initialCategories = [
  { id: 'all', name: 'All Products', icon: 'grid_view' },
  { id: 'MOBILE_PHONES', name: 'Smartphones', icon: 'smartphone' },
  { id: 'OLD_PHONES', name: 'Pre-Owned Phones', icon: 'autorenew' },
  { id: 'AUDIO', name: 'Headphones & Audio', icon: 'headphones' },
  { id: 'SMART_WATCHES', name: 'Smart Watches', icon: 'watch' },
  { id: 'ACCESSORIES', name: 'Accessories', icon: 'cable' },
  { id: 'POWER_BANKS', name: 'Power Banks', icon: 'battery_charging_full' }
];

export const initialProducts = [
  {
    id: 'mob-1',
    name: 'OnePlus Nord CE4 (8GB RAM, 128GB)',
    brand: 'OnePlus',
    category: 'MOBILE_PHONES',
    price: 24999,
    originalPrice: 27999,
    rating: 4.7,
    reviewCount: 342,
    badge: 'Best Seller',
    stock: 12,
    image: 'https://images.unsplash.com/photo-1598327105666-5b89351aff97?auto=format&fit=crop&w=600&q=80',
    description: 'Powered by Snapdragon 7 Gen 3 with 100W SUPERVOOC fast charging and 5500 mAh battery. Vibrant 120Hz Fluid AMOLED display with Sony LYT-600 OIS camera.',
    specs: {
      'Processor': 'Qualcomm Snapdragon 7 Gen 3 (4nm)',
      'RAM / Storage': '8GB LPDDR4X / 128GB UFS 3.1',
      'Display': '6.7" FHD+ 120Hz Fluid AMOLED',
      'Camera': '50MP Sony LYT-600 (OIS) + 8MP Ultra-wide',
      'Battery': '5500 mAh, 100W SUPERVOOC Fast Charge',
      'Warranty': '1 Year Brand Warranty in India'
    }
  },
  {
    id: 'mob-2',
    name: 'Samsung Galaxy M35 5G (6GB, 128GB)',
    brand: 'Samsung',
    category: 'MOBILE_PHONES',
    price: 16999,
    originalPrice: 21499,
    rating: 4.5,
    reviewCount: 289,
    badge: 'Hot Deal',
    stock: 18,
    image: 'https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?auto=format&fit=crop&w=600&q=80',
    description: 'Monster 6000 mAh battery with Knox Security and Samsung Wallet support. Stunning 120Hz Super AMOLED display and 50MP No Shake Camera.',
    specs: {
      'Processor': 'Exynos 1380 Octa-core',
      'RAM / Storage': '6GB RAM / 128GB Expandable',
      'Display': '6.6" 120Hz Super AMOLED (1000 nits)',
      'Camera': '50MP (OIS) + 8MP + 2MP',
      'Battery': '6000 mAh Mega Battery, 25W Fast Charge'
    }
  },
  {
    id: 'mob-3',
    name: 'Redmi Note 13 Pro 5G (8GB, 256GB)',
    brand: 'Xiaomi',
    category: 'MOBILE_PHONES',
    price: 23999,
    originalPrice: 28999,
    rating: 4.6,
    reviewCount: 412,
    badge: 'Top Rated',
    stock: 8,
    image: 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=600&q=80',
    description: 'Flagship 200MP OIS camera with 1.5K 120Hz curved display, Snapdragon 7s Gen 2, and 67W turbo charging.',
    specs: {
      'Processor': 'Snapdragon 7s Gen 2',
      'Display': '6.67" 1.5K 120Hz CrystalRes AMOLED',
      'Camera': '200MP Samsung ISOCELL HP3 with OIS',
      'Battery': '5100 mAh, 67W Turbo Charger in box'
    }
  },
  {
    id: 'mob-4',
    name: 'Vivo V30 5G (8GB, 128GB)',
    brand: 'Vivo',
    category: 'MOBILE_PHONES',
    price: 33999,
    originalPrice: 38999,
    rating: 4.8,
    reviewCount: 198,
    badge: 'Premium',
    stock: 5,
    image: 'https://images.unsplash.com/photo-1565849904461-04a58ad377e0?auto=format&fit=crop&w=600&q=80',
    description: 'Studio-grade Smart Aura Light portrait camera with 50MP Eye-AF selfie. Ultra slim 3D curved 1.5K display and 5000mAh battery with 80W FlashCharge.',
    specs: {
      'Processor': 'Snapdragon 7 Gen 3',
      'Display': '6.78" 120Hz 3D Curved AMOLED',
      'Camera': '50MP OIS + 50MP Ultra-wide with Smart Aura Light',
      'Charging': '80W FlashCharge'
    }
  },
  {
    id: 'old-1',
    name: 'Apple iPhone 13 (128GB, Midnight) - Certified Pre-Owned',
    brand: 'Apple',
    category: 'OLD_PHONES',
    price: 38999,
    originalPrice: 59900,
    rating: 4.9,
    reviewCount: 88,
    badge: 'Certified A+ Grade',
    stock: 2,
    image: 'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?auto=format&fit=crop&w=600&q=80',
    description: 'SK Mobile Certified pre-owned iPhone 13. 91% Battery Health, 100% original OLED screen, face ID working, 6-Month Store Warranty from Udgir center.',
    specs: {
      'Condition': 'Grade A+ (Flawless condition with zero scratches)',
      'Battery Health': '91% Maximum Capacity',
      'Storage': '128GB Internal',
      'Warranty': '6 Months Free Hardware Warranty by SK Mobile',
      'Includes': 'Original box, 20W adapter & charging cable'
    }
  },
  {
    id: 'old-2',
    name: 'OnePlus 10R 5G (8GB, 128GB) - Pre-Owned',
    brand: 'OnePlus',
    category: 'OLD_PHONES',
    price: 18499,
    originalPrice: 38999,
    rating: 4.6,
    reviewCount: 54,
    badge: 'Super Deal',
    stock: 3,
    image: 'https://images.unsplash.com/photo-1580910051074-3eb694886505?auto=format&fit=crop&w=600&q=80',
    description: 'Super fast Dimensity 8100-Max with 80W charging. Tested across 40+ hardware checkpoints at SK Mobile Udgir shop.',
    specs: {
      'Condition': 'Grade A (Minor cosmetic marks on frame)',
      'Battery Health': '89%',
      'Warranty': '3 Months Store Warranty'
    }
  },
  {
    id: 'aud-1',
    name: 'OnePlus Bullets Wireless Z2 (Magico Black)',
    brand: 'OnePlus',
    category: 'AUDIO',
    price: 1599,
    originalPrice: 2299,
    rating: 4.6,
    reviewCount: 620,
    badge: 'Popular',
    stock: 35,
    image: 'https://images.unsplash.com/photo-1546435770-a3e426bf472b?auto=format&fit=crop&w=600&q=80',
    description: '12.4mm bass drivers, 30-hour battery life, 10-minute quick charge for 20 hours listening, IP55 sweat & water resistance.',
    specs: {
      'Driver Size': '12.4mm Bass Drivers',
      'Playtime': 'Up to 30 Hours',
      'Fast Charge': '10 Mins = 20 Hours'
    }
  },
  {
    id: 'aud-2',
    name: 'boAt Airdopes 141 ANC TWS Earbuds',
    brand: 'boAt',
    category: 'AUDIO',
    price: 1299,
    originalPrice: 3990,
    rating: 4.4,
    reviewCount: 512,
    badge: 'Great Value',
    stock: 40,
    image: 'https://images.unsplash.com/photo-1590658268037-6bf12165a8df?auto=format&fit=crop&w=600&q=80',
    description: 'Active Noise Cancellation up to 32dB, 42 hours total playtime with ENx quad mics for crystal clear calls.',
    specs: {
      'Noise Cancellation': '32dB Active Noise Cancellation',
      'Playtime': '42 Hours with Case',
      'Microphones': 'Quad Mics with ENx Tech'
    }
  },
  {
    id: 'wat-1',
    name: 'Fire-Boltt Ninja Call Pro Plus 1.83" Bluetooth Calling',
    brand: 'Fire-Boltt',
    category: 'SMART_WATCHES',
    price: 1199,
    originalPrice: 7999,
    rating: 4.3,
    reviewCount: 420,
    badge: 'Trending',
    stock: 25,
    image: 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80',
    description: '1.83" HD display, Bluetooth calling with built-in dialpad, 100+ sports modes, SpO2 & 24/7 heart rate monitor.',
    specs: {
      'Display': '1.83" HD Display (280 Nits)',
      'Calling': 'Bluetooth Calling with Speaker & Mic',
      'Battery': 'Up to 8 Days Battery Life'
    }
  },
  {
    id: 'acc-1',
    name: '65W GaN SuperVOOC / Warp Fast Charger with Type-C Cable',
    brand: 'SK Mobile Originals',
    category: 'ACCESSORIES',
    price: 899,
    originalPrice: 1999,
    rating: 4.8,
    reviewCount: 154,
    badge: 'Store Choice',
    stock: 50,
    image: 'https://images.unsplash.com/photo-1583863788434-e58a36330cf0?auto=format&fit=crop&w=600&q=80',
    description: 'Super fast GaN charging compatible with OnePlus, Realme, Oppo, Samsung and Xiaomi devices. Dual thermal protection chip.',
    specs: {
      'Power': '65W Max Output',
      'Technology': 'Gallium Nitride (GaN)',
      'Included': '1-Meter 6A Braided Type-C Cable'
    }
  },
  {
    id: 'pb-1',
    name: 'Ambrane 20000mAh 22.5W Fast Charging Power Bank',
    brand: 'Ambrane',
    category: 'POWER_BANKS',
    price: 1499,
    originalPrice: 2499,
    rating: 4.7,
    reviewCount: 230,
    badge: 'High Capacity',
    stock: 30,
    image: 'https://images.unsplash.com/photo-1609592424368-b75d5a6bfa9f?auto=format&fit=crop&w=600&q=80',
    description: 'Triple output ports (2x USB-A + 1x Type-C PD 22.5W), charges iPhone 50% in 30 minutes, premium metallic finish.',
    specs: {
      'Capacity': '20,000 mAh Lithium Polymer',
      'Ports': '2 USB-A + 1 USB Type-C (Bidirectional)',
      'Fast Charge': '22.5W Power Delivery & Quick Charge 3.0'
    }
  }
];

export const repairServicesList = [
  { id: 'rep-1', name: 'Display Replacement', icon: 'smartphone', priceEst: '₹1,200 - ₹4,500', time: '30 - 60 Mins', warranty: '90 Days Warranty' },
  { id: 'rep-2', name: 'Battery Replacement', icon: 'battery_alert', priceEst: '₹600 - ₹1,800', time: '30 Mins', warranty: '180 Days Warranty' },
  { id: 'rep-3', name: 'Charging Port Repair', icon: 'power', priceEst: '₹350 - ₹900', time: '20 Mins', warranty: '90 Days Warranty' },
  { id: 'rep-4', name: 'Speaker & Mic Repair', icon: 'volume_up', priceEst: '₹400 - ₹850', time: '30 Mins', warranty: '90 Days Warranty' },
  { id: 'rep-5', name: 'Camera & Lens Glass', icon: 'photo_camera', priceEst: '₹500 - ₹1,600', time: '45 Mins', warranty: '90 Days Warranty' },
  { id: 'rep-6', name: 'Water Damage Treatment', icon: 'water_drop', priceEst: '₹800 - ₹2,500', time: '2 - 4 Hours', warranty: 'Inspection Guaranteed' },
  { id: 'rep-7', name: 'Software & Dead Boot', icon: 'settings_system_daydream', priceEst: '₹300 - ₹800', time: '40 Mins', warranty: 'Free Diagnosis' }
];

export const storeInfo = {
  name: 'SK Mobile Shoppy and Electronic',
  tagline: 'Your Trusted Mobile & Electronics Destination in Udgir',
  address: 'Nagar Parishad Office Road, Near Station Road, Udgir, Dist. Latur, Maharashtra - 413517',
  phone1: '+91 94213 77777',
  phone2: '+91 98900 12345',
  whatsapp: '919421377777',
  email: 'skmobileshoppyandelectronics@gmail.com',
  hours: 'Monday – Sunday: 9:30 AM – 9:30 PM',
  googleMapsUrl: 'https://maps.google.com/?q=Nagar+Parishad+Udgir+Maharashtra'
};
