import { initializeApp, getApps, getApp } from 'firebase/app';
import { getFirestore, collection, getDocs, doc, setDoc, updateDoc, deleteDoc, query, orderBy, where, serverTimestamp } from 'firebase/firestore';
import { 
  getAuth, 
  onAuthStateChanged, 
  signInWithPopup, 
  GoogleAuthProvider, 
  signOut 
} from 'firebase/auth';

// Approved Web Client Configuration for SK Mobile Shoppy and Electronic
// Public client configuration derived from app/google-services.json
const firebaseConfig = {
  apiKey: "AIzaSyAtyJz_sSW1NXQuy5vUtQRUsrfgZsG9A1I",
  authDomain: "gen-lang-client-0232823199.firebaseapp.com",
  projectId: "gen-lang-client-0232823199",
  storageBucket: "gen-lang-client-0232823199.firebasestorage.app",
  messagingSenderId: "23597341071",
  appId: "1:23597341071:web:7256e8f0d4582d944093fa"
};

// Initialize Firebase App singleton
const app = getApps().length === 0 ? initializeApp(firebaseConfig) : getApp();

// Target database ID as defined in firebase_applet_config.xml and firebase.json
const CUSTOM_DB_ID = "ai-studio-android-skmobile-a123a057-9048-4d09-88d8-cfb38e18df87";

let dbInstance = null;
try {
  dbInstance = getFirestore(app, CUSTOM_DB_ID);
} catch (err) {
  console.warn("Could not bind to custom database ID, falling back to default:", err);
  dbInstance = getFirestore(app);
}

export const db = dbInstance;
export const auth = getAuth(app);
export const FIREBASE_PROJECT_ID = firebaseConfig.projectId;

/**
 * Listen to Firebase Auth state changes
 */
export function subscribeToAuthChanges(callback) {
  return onAuthStateChanged(auth, callback);
}

/**
 * Sign in using Google Auth Popup
 */
export async function signInWithGooglePopup() {
  const provider = new GoogleAuthProvider();
  provider.setCustomParameters({ prompt: 'select_account' });
  return await signInWithPopup(auth, provider);
}

/**
 * Sign out current customer
 */
export async function logoutUser() {
  return await signOut(auth);
}

/**
 * Fetch public products from Firestore collection 'products'.
 * Rules allow public read: match /products/{productId} { allow read: if true; }
 */
export async function fetchFirestoreProducts() {
  try {
    const productsRef = collection(db, 'products');
    const querySnapshot = await getDocs(productsRef);

    if (querySnapshot.empty) {
      return {
        success: true,
        data: [],
        isLive: true,
        isEmpty: true,
        error: null
      };
    }

    const items = [];
    querySnapshot.forEach((doc) => {
      const data = doc.data();
      const priceNum = typeof data.price === 'number' ? data.price : (parseInt(data.price, 10) || 0);
      const origPriceNum = typeof data.originalPrice === 'number' ? data.originalPrice : (data.originalPrice ? parseInt(data.originalPrice, 10) : undefined);
      const stockNum = typeof data.stock === 'number' ? data.stock : (data.stock !== undefined ? parseInt(data.stock, 10) : 10);
      const inStockBool = data.inStock !== undefined ? Boolean(data.inStock) : stockNum > 0;

      items.push({
        id: doc.id,
        ...data,
        category: (data.category || '').toLowerCase(),
        price: priceNum,
        originalPrice: origPriceNum,
        stock: stockNum,
        inStock: inStockBool,
        rating: data.rating || 4.7,
        reviewCount: data.reviewCount || 12,
        brand: data.brand || 'Generic',
        name: data.name || 'Product',
        description: data.description || '',
        imageUrl: data.imageUrl || data.image || '',
        badge: data.badge || (data.isFeatured ? 'Featured' : (data.isBestSeller ? 'Best Seller' : '')),
        isFeatured: Boolean(data.isFeatured),
        isBestSeller: Boolean(data.isBestSeller),
        hasWarranty: Boolean(data.hasWarranty),
        warrantyDuration: data.hasWarranty ? (data.warrantyDuration || '') : '',
        warrantyDescription: data.hasWarranty ? (data.warrantyDescription || '') : '',
        specs: data.specifications || data.specs || {},
        createdAt: data.createdAt || null,
        updatedAt: data.updatedAt || null
      });
    });

    return {
      success: true,
      data: items,
      isLive: true,
      isEmpty: false,
      error: null
    };
  } catch (error) {
    console.error("Firestore read error on 'products' collection:", error);
    return {
      success: false,
      data: [],
      isLive: false,
      isEmpty: false,
      error: error.message || 'Failed to read products from Firestore'
    };
  }
}

/**
 * Save repair booking to Firestore 'repairBookings' collection.
 * 
 * Strict Zero-Trust Security Rule:
 * match /repairBookings/{repairBookingId} {
 *   allow create: if isAuthenticated() &&
 *     request.resource.data.customerId == request.auth.uid &&
 *     request.resource.data.customerName is string && request.resource.data.customerName.size() > 0 &&
 *     request.resource.data.phone is string && request.resource.data.phone.size() >= 10 &&
 *     request.resource.data.brand is string && request.resource.data.brand.size() > 0 &&
 *     request.resource.data.model is string && request.resource.data.model.size() > 0 &&
 *     request.resource.data.problem is string && request.resource.data.problem.size() > 0 &&
 *     request.resource.data.repairStatus in ['Requested', 'Submitted Successfully', 'Appointment Booked', 'Pickup Scheduled', 'Pending']
 * }
 */
export async function saveRepairBookingToFirestore(bookingData, currentUser) {
  if (!currentUser || !currentUser.uid) {
    throw new Error('Authentication required: You must be logged in with a verified customer account to book a repair.');
  }

  // Permitted initial statuses in firestore.rules:
  // ['Requested', 'Submitted Successfully', 'Appointment Booked', 'Pickup Scheduled', 'Pending']
  const validStatus = bookingData.pickupRequired ? 'Pickup Scheduled' : 'Appointment Booked';

  const docId = bookingData.bookingId || `SKM-REP-${Date.now().toString().slice(-6)}`;
  const docRef = doc(db, 'repairBookings', docId);

  const payload = {
    id: docId,
    customerId: currentUser.uid,
    customerName: (bookingData.customerName || '').trim(),
    phone: (bookingData.mobileNumber || bookingData.phone || '').trim(),
    brand: (bookingData.brand || '').trim(),
    model: (bookingData.model || '').trim(),
    problem: (bookingData.problem || '').trim(),
    pickupRequired: Boolean(bookingData.pickupRequired),
    address: (bookingData.address || '').trim(),
    repairStatus: validStatus,
    notes: (bookingData.additionalDetails || bookingData.notes || '').trim(),
    preferredSlot: (bookingData.preferredSlot || '').trim(),
    estimatedCost: (bookingData.estimatedCost || '₹499 - ₹1,499 (Final quote on diagnosis)').trim(),
    createdAt: serverTimestamp()
  };

  await setDoc(docRef, payload);

  return {
    success: true,
    bookingId: docId,
    booking: payload
  };
}

/**
 * Save old phone buy/sell request to Firestore 'oldPhoneRequests' collection.
 * 
 * Strict Zero-Trust Security Rule:
 * match /oldPhoneRequests/{oldPhoneRequestId} {
 *   allow create: if isAdmin() || (
 *     isAuthenticated() &&
 *     request.resource.data.customerId == request.auth.uid &&
 *     request.resource.data.customerName is string && request.resource.data.customerName.size() > 0 &&
 *     request.resource.data.phone is string && request.resource.data.phone.size() >= 10 &&
 *     request.resource.data.brand is string && request.resource.data.brand.size() > 0 &&
 *     request.resource.data.model is string && request.resource.data.model.size() > 0 &&
 *     request.resource.data.status in ['Valuation Generated', 'Pending', 'Submitted']
 *   );
 * }
 */
export async function saveOldPhoneRequestToFirestore(requestData, currentUser) {
  if (!currentUser || !currentUser.uid) {
    throw new Error('Authentication required: You must be logged in with a verified customer account to submit a request.');
  }

  const customerName = (requestData.customerName || '').trim();
  const phone = (requestData.phone || requestData.mobileNumber || '').trim();
  const brand = (requestData.brand || '').trim();
  const model = (requestData.model || '').trim();

  if (!customerName) {
    throw new Error('Customer name is required.');
  }
  if (!phone || phone.length < 10) {
    throw new Error('A valid 10-digit mobile number is required.');
  }
  if (!brand) {
    throw new Error('Phone brand is required.');
  }
  if (!model) {
    throw new Error('Phone model name is required.');
  }

  const docId = requestData.requestId || `SKM-OLD-${Date.now().toString().slice(-6)}`;
  const docRef = doc(db, 'oldPhoneRequests', docId);

  const payload = {
    id: docId,
    customerId: currentUser.uid,
    customerName,
    phone,
    brand,
    model,
    storage: (requestData.storage || '128GB').trim(),
    condition: (requestData.condition || 'Good').trim(),
    expectedPrice: (requestData.expectedPrice || requestData.estimatedQuote || 'Pending physical inspection').trim(),
    estimatedQuote: (requestData.estimatedQuote || requestData.expectedPrice || 'Pending physical inspection').trim(),
    requestType: requestData.requestType || 'SELL',
    status: 'Submitted',
    address: (requestData.address || '').trim(),
    createdAt: serverTimestamp()
  };

  await setDoc(docRef, payload);

  return {
    success: true,
    requestId: docId,
    request: payload
  };
}

/**
 * Store Admin Authorization Allowlist
 * Matches firestore.rules isAdmin() definition exactly.
 */
export const AUTHORIZED_ADMIN_EMAILS = [
  'skmobileshoppyandelectronics@gmail.com',
  'patelw401@gmail.com',
  'admin@skmobile.com',
  'admin.skmobile@gmail.com'
];

/**
 * Check if a Firebase user is an authorized store administrator.
 */
export function isUserAdmin(user) {
  if (!user || !user.email) return false;
  return AUTHORIZED_ADMIN_EMAILS.some(
    adminEmail => adminEmail.toLowerCase() === user.email.toLowerCase()
  );
}

/**
 * Fetch all repair bookings for Store Admin review.
 */
export async function fetchAdminRepairBookings() {
  try {
    const colRef = collection(db, 'repairBookings');
    const snap = await getDocs(colRef);
    const bookings = [];
    snap.forEach((d) => {
      bookings.push({ id: d.id, ...d.data() });
    });
    return bookings.sort((a, b) => {
      const timeA = a.createdAt?.toMillis ? a.createdAt.toMillis() : (a.timestamp || 0);
      const timeB = b.createdAt?.toMillis ? b.createdAt.toMillis() : (b.timestamp || 0);
      return timeB - timeA;
    });
  } catch (err) {
    console.error("fetchAdminRepairBookings error:", err);
    throw err;
  }
}

/**
 * Fetch all old phone buy/sell inquiries for Store Admin review.
 */
export async function fetchAdminOldPhoneRequests() {
  try {
    const colRef = collection(db, 'oldPhoneRequests');
    const snap = await getDocs(colRef);
    const requests = [];
    snap.forEach((d) => {
      requests.push({ id: d.id, ...d.data() });
    });
    return requests.sort((a, b) => {
      const timeA = a.createdAt?.toMillis ? a.createdAt.toMillis() : (a.timestamp || 0);
      const timeB = b.createdAt?.toMillis ? b.createdAt.toMillis() : (b.timestamp || 0);
      return timeB - timeA;
    });
  } catch (err) {
    console.error("fetchAdminOldPhoneRequests error:", err);
    throw err;
  }
}

/**
 * Update repair booking status (Admin only).
 * Permitted statuses in firestore.rules:
 * ['Requested', 'Submitted Successfully', 'Appointment Booked', 'Pickup Scheduled', 'Pending', 'In Progress', 'Completed']
 */
export async function updateAdminRepairBookingStatus(bookingId, newStatus, adminEmail) {
  const docRef = doc(db, 'repairBookings', bookingId);
  await updateDoc(docRef, {
    repairStatus: newStatus,
    status: newStatus,
    lastUpdatedBy: adminEmail || 'Store Admin',
    updatedAt: serverTimestamp()
  });
  return true;
}

/**
 * Update old phone valuation status (Admin only).
 */
export async function updateAdminOldPhoneStatus(requestId, newStatus, adminNote) {
  const docRef = doc(db, 'oldPhoneRequests', requestId);
  const updates = {
    status: newStatus,
    updatedAt: serverTimestamp()
  };
  if (adminNote) {
    updates.adminNote = adminNote;
  }
  await updateDoc(docRef, updates);
  return true;
}

/**
 * -------------------------------------------------------------
 * PRODUCT MANAGEMENT (CRUD) - FIRESTORE PRODUCTS COLLECTION
 * -------------------------------------------------------------
 * Security Rule:
 * match /products/{productId} {
 *   allow read: if true;
 *   allow write: if isAdmin();
 * }
 */

/**
 * Fetch all products from Firestore for Admin Inventory Management.
 */
export async function fetchAdminProducts() {
  try {
    const productsRef = collection(db, 'products');
    const snap = await getDocs(productsRef);
    if (snap.empty) {
      return { success: true, data: [], isEmpty: true };
    }
    const items = [];
    snap.forEach((d) => {
      const data = d.data();
      const priceNum = typeof data.price === 'number' ? data.price : (parseInt(data.price, 10) || 0);
      const origPriceNum = typeof data.originalPrice === 'number' ? data.originalPrice : (data.originalPrice ? parseInt(data.originalPrice, 10) : undefined);
      const stockNum = typeof data.stock === 'number' ? data.stock : (data.stock !== undefined ? parseInt(data.stock, 10) : 10);
      const inStockBool = data.inStock !== undefined ? Boolean(data.inStock) : stockNum > 0;

      items.push({
        id: d.id,
        ...data,
        name: data.name || 'Unnamed Product',
        brand: data.brand || 'Generic',
        category: (data.category || 'smartphones').toLowerCase(),
        price: priceNum,
        originalPrice: origPriceNum,
        stock: stockNum,
        inStock: inStockBool,
        rating: data.rating || 4.7,
        reviewCount: data.reviewCount || 10,
        description: data.description || '',
        imageUrl: data.imageUrl || data.image || '',
        badge: data.badge || (data.isFeatured ? 'Featured' : (data.isBestSeller ? 'Best Seller' : '')),
        isFeatured: Boolean(data.isFeatured),
        isBestSeller: Boolean(data.isBestSeller),
        hasWarranty: Boolean(data.hasWarranty),
        warrantyDuration: data.hasWarranty ? (data.warrantyDuration || '') : '',
        warrantyDescription: data.hasWarranty ? (data.warrantyDescription || '') : '',
        specs: data.specs || data.specifications || {},
        createdAt: data.createdAt || null,
        updatedAt: data.updatedAt || null
      });
    });
    return { success: true, data: items, isEmpty: false };
  } catch (err) {
    console.error("fetchAdminProducts error:", err);
    return { success: false, data: [], error: err.message };
  }
}

/**
 * Save product (Create or Edit) in Firestore 'products' collection.
 * Restricted strictly to authorized store administrators.
 */
export async function saveAdminProductToFirestore(productData, currentUser) {
  if (!isUserAdmin(currentUser)) {
    throw new Error('Unauthorized: You must be an authorized store administrator to add or modify products.');
  }

  const name = (productData.name || '').trim();
  const brand = (productData.brand || '').trim();
  const price = typeof productData.price === 'number' ? productData.price : (parseFloat(productData.price) || 0);

  if (!name) throw new Error('Product name is required.');
  if (!brand) throw new Error('Brand name is required.');
  if (price <= 0) throw new Error('Selling price must be greater than zero.');

  const docId = (productData.id || '').trim() || `prod-${Date.now().toString()}`;
  const docRef = doc(db, 'products', docId);

  const origPrice = productData.originalPrice ? (typeof productData.originalPrice === 'number' ? productData.originalPrice : parseFloat(productData.originalPrice)) : null;
  const stock = typeof productData.stock === 'number' ? productData.stock : (parseInt(productData.stock, 10) >= 0 ? parseInt(productData.stock, 10) : 0);
  const inStock = stock > 0;
  const discount = origPrice && origPrice > price ? Math.round(((origPrice - price) / origPrice) * 100) : 0;
  const hasWarranty = Boolean(productData.hasWarranty);
  const warrantyDuration = hasWarranty ? (productData.warrantyDuration || '').trim() : '';
  const warrantyDescription = hasWarranty ? (productData.warrantyDescription || '').trim() : '';

  const payload = {
    id: docId,
    name,
    brand,
    category: (productData.category || 'smartphones').toLowerCase(),
    price,
    stock,
    inStock,
    description: (productData.description || '').trim(),
    imageUrl: (productData.imageUrl || productData.image || '').trim(),
    badge: (productData.badge || '').trim(),
    isFeatured: Boolean(productData.isFeatured),
    isBestSeller: Boolean(productData.isBestSeller),
    hasWarranty,
    warrantyDuration,
    warrantyDescription,
    rating: productData.rating || 4.7,
    reviewCount: productData.reviewCount || 10,
    specs: productData.specs || {},
    lastUpdatedBy: currentUser.email || 'Store Admin',
    updatedAt: serverTimestamp()
  };

  if (origPrice && origPrice > price) {
    payload.originalPrice = origPrice;
    payload.discount = discount;
  }

  // Preserve creation timestamp on edit, or set new
  if (productData.createdAt) {
    payload.createdAt = productData.createdAt;
  } else {
    payload.createdAt = serverTimestamp();
    payload.createdBy = currentUser.email || 'Store Admin';
  }

  await setDoc(docRef, payload, { merge: true });

  return {
    success: true,
    id: docId,
    product: payload
  };
}

/**
 * Quick update product warranty status (Admin only).
 */
export async function quickUpdateProductWarranty(productId, hasWarranty, warrantyDuration = '', warrantyDescription = '', currentUser) {
  if (!isUserAdmin(currentUser)) {
    throw new Error('Unauthorized: Admin access required to update product warranty.');
  }
  if (!productId) {
    throw new Error('Product ID is required.');
  }
  const docRef = doc(db, 'products', productId);
  const updates = {
    hasWarranty: Boolean(hasWarranty),
    warrantyDuration: hasWarranty ? (warrantyDuration || '').trim() : '',
    warrantyDescription: hasWarranty ? (warrantyDescription || '').trim() : '',
    lastUpdatedBy: currentUser.email || 'Store Admin',
    updatedAt: serverTimestamp()
  };
  await updateDoc(docRef, updates);
  return { success: true, ...updates };
}

/**
 * Delete a product from Firestore 'products' collection.
 * Restricted strictly to authorized store administrators.
 */
export async function deleteAdminProductFromFirestore(productId, currentUser) {
  if (!isUserAdmin(currentUser)) {
    throw new Error('Unauthorized: You must be an authorized store administrator to delete products.');
  }

  if (!productId) throw new Error('Product ID is required for deletion.');

  const docRef = doc(db, 'products', productId);
  await deleteDoc(docRef);

  return { success: true, deletedId: productId };
}

/**
 * Seed initial sample catalog from storeData into Firestore 'products' collection.
 * This populates the cloud database so the admin can edit existing store phones and accessories immediately!
 */
export async function seedDefaultProductsToFirestore(currentUser, initialItems = []) {
  if (!isUserAdmin(currentUser)) {
    throw new Error('Unauthorized: Admin access required to initialize store catalogue.');
  }

  const results = [];
  for (const item of initialItems) {
    try {
      const res = await saveAdminProductToFirestore(item, currentUser);
      results.push(res);
    } catch (e) {
      console.warn(`Failed to seed product ${item.name}:`, e);
    }
  }

  return { success: true, seededCount: results.length };
}

/**
 * -------------------------------------------------------------
 * E-COMMERCE ORDERS - FIRESTORE ORDERS COLLECTION
 * -------------------------------------------------------------
 * Rules:
 * match /orders/{orderId} {
 *   allow read: if isAdmin() || (isAuthenticated() && resource.data.customerId == request.auth.uid);
 *   allow create: if isAdmin() || (
 *     isAuthenticated() &&
 *     request.resource.data.customerId == request.auth.uid &&
 *     request.resource.data.customerName is string && request.resource.data.customerName.size() > 0 &&
 *     request.resource.data.phone is string && request.resource.data.phone.size() >= 10 &&
 *     request.resource.data.address is string && request.resource.data.address.size() > 0 &&
 *     request.resource.data.items is list && request.resource.data.items.size() > 0 &&
 *     request.resource.data.total is int && request.resource.data.total >= 0 &&
 *     request.resource.data.orderStatus in ['Pending', 'Order Confirmed', 'Processing', 'Order Placed'] &&
 *     request.resource.data.paymentStatus in ['Pending', 'Paid']
 *   );
 * }
 */

/**
 * Create a new customer order in Firestore 'orders' collection.
 */
export async function createCustomerOrderInFirestore(orderData, currentUser) {
  if (!currentUser || !currentUser.uid) {
    throw new Error('Authentication required: Please sign in with your customer account to place an order.');
  }

  const customerName = (orderData.customerName || currentUser.displayName || '').trim();
  const phone = (orderData.phone || orderData.mobileNumber || '').trim();
  const address = (orderData.address || '').trim();
  const pincode = (orderData.pincode || orderData.pinCode || '').trim();
  const items = Array.isArray(orderData.items) ? orderData.items : [];

  if (!customerName) {
    throw new Error('Customer full name is required.');
  }
  if (!phone || phone.replace(/\D/g, '').length < 10) {
    throw new Error('Please provide a valid 10-digit mobile number.');
  }
  if (!address) {
    throw new Error('Delivery address or store pickup location note is required.');
  }
  if (items.length === 0) {
    throw new Error('Your cart is empty. Please add items before placing an order.');
  }

  // Calculate integer total
  const calculatedTotal = items.reduce((sum, item) => {
    const p = typeof item.price === 'number' ? item.price : (parseFloat(item.price) || 0);
    const q = typeof item.quantity === 'number' ? item.quantity : (parseInt(item.quantity, 10) || 1);
    return sum + (p * q);
  }, 0);

  const finalTotal = Math.round(Number(orderData.total !== undefined ? orderData.total : calculatedTotal));

  // Generate unique order ID
  const orderDocId = orderData.orderId || `SKM-ORD-${Date.now().toString().slice(-6)}-${Math.floor(100 + Math.random() * 900)}`;
  const orderRef = doc(db, 'orders', orderDocId);

  // Clean items list
  const sanitizedItems = items.map(item => ({
    id: item.id || '',
    name: item.name || 'Product',
    brand: item.brand || '',
    category: item.category || 'smartphones',
    price: Math.round(Number(item.price) || 0),
    quantity: parseInt(item.quantity, 10) || 1,
    imageUrl: item.imageUrl || item.image || '',
    subtotal: Math.round((Number(item.price) || 0) * (parseInt(item.quantity, 10) || 1))
  }));

  const payload = {
    id: orderDocId,
    orderId: orderDocId,
    customerId: currentUser.uid,
    customerEmail: currentUser.email || '',
    customerName,
    phone,
    address,
    pincode: pincode || '413517',
    city: (orderData.city || 'Udgir').trim(),
    state: (orderData.state || 'Maharashtra').trim(),
    deliveryType: orderData.deliveryType || 'Store Pickup (Udgir Showroom)',
    paymentMethod: orderData.paymentMethod || 'Cash on Delivery (COD)',
    paymentStatus: 'Pending',
    orderStatus: 'Pending',
    items: sanitizedItems,
    total: finalTotal,
    notes: (orderData.notes || '').trim(),
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp()
  };

  await setDoc(orderRef, payload);

  return {
    success: true,
    orderId: orderDocId,
    order: payload
  };
}

/**
 * Fetch orders placed by the current customer.
 */
export async function fetchCustomerOrders(currentUser) {
  if (!currentUser || !currentUser.uid) return [];
  try {
    const ordersRef = collection(db, 'orders');
    const q = query(ordersRef, where('customerId', '==', currentUser.uid));
    const snap = await getDocs(q);
    const list = [];
    snap.forEach(d => {
      list.push({ id: d.id, ...d.data() });
    });
    return list.sort((a, b) => {
      const timeA = a.createdAt?.toMillis ? a.createdAt.toMillis() : (a.timestamp || 0);
      const timeB = b.createdAt?.toMillis ? b.createdAt.toMillis() : (b.timestamp || 0);
      return timeB - timeA;
    });
  } catch (err) {
    console.error("fetchCustomerOrders error:", err);
    return [];
  }
}

/**
 * Cancel an active customer order (before delivery).
 */
export async function cancelCustomerOrder(orderId, currentUser, reason = 'Cancelled by customer') {
  if (!currentUser || !currentUser.uid) {
    throw new Error('Authentication required.');
  }
  const orderRef = doc(db, 'orders', orderId);
  await updateDoc(orderRef, {
    orderStatus: 'Cancelled',
    cancellationReason: reason,
    updatedAt: serverTimestamp()
  });
  return true;
}

/**
 * Fetch all customer orders for Store Admin review.
 */
export async function fetchAdminOrders() {
  try {
    const ordersRef = collection(db, 'orders');
    const snap = await getDocs(ordersRef);
    const list = [];
    snap.forEach(d => {
      list.push({ id: d.id, ...d.data() });
    });
    return list.sort((a, b) => {
      const timeA = a.createdAt?.toMillis ? a.createdAt.toMillis() : (a.timestamp || 0);
      const timeB = b.createdAt?.toMillis ? b.createdAt.toMillis() : (b.timestamp || 0);
      return timeB - timeA;
    });
  } catch (err) {
    console.error("fetchAdminOrders error:", err);
    throw err;
  }
}

/**
 * Update order status by Store Administrator.
 * Permitted statuses: Pending, Confirmed, Processing, Shipped, Delivered, Cancelled
 */
export async function updateAdminOrderStatus(orderId, newStatus, adminEmail, adminNote = '') {
  const orderRef = doc(db, 'orders', orderId);
  const updates = {
    orderStatus: newStatus,
    status: newStatus,
    lastUpdatedBy: adminEmail || 'Store Admin',
    updatedAt: serverTimestamp()
  };
  if (adminNote) {
    updates.adminNote = adminNote;
  }
  if (newStatus === 'Delivered') {
    updates.paymentStatus = 'Paid';
  }
  await updateDoc(orderRef, updates);
  return true;
}



