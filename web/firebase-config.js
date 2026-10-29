// Firebase Web Integration for SK Mobile Shoppy and Electronic, Udgir
import { initializeApp } from 'https://www.gstatic.com/firebasejs/10.13.0/firebase-app.js';
import { 
  getAuth, 
  signInWithPopup, 
  GoogleAuthProvider, 
  signOut, 
  onAuthStateChanged 
} from 'https://www.gstatic.com/firebasejs/10.13.0/firebase-auth.js';
import { 
  getFirestore, 
  collection, 
  doc, 
  getDocs, 
  getDoc, 
  setDoc, 
  addDoc, 
  updateDoc, 
  deleteDoc, 
  query, 
  where, 
  orderBy, 
  limit, 
  onSnapshot 
} from 'https://www.gstatic.com/firebasejs/10.13.0/firebase-firestore.js';

export const firebaseConfig = {
  apiKey: "AIzaSyAtyJz_sSW1NXQuy5vUtQRUsrfgZsG9A1I",
  authDomain: "gen-lang-client-0232823199.firebaseapp.com",
  projectId: "gen-lang-client-0232823199",
  storageBucket: "gen-lang-client-0232823199.firebasestorage.app",
  messagingSenderId: "23597341071",
  appId: "1:23597341071:web:skmobile-web-udgir"
};

export const AUTHORIZED_ADMIN_EMAILS = [
  'skmobileshoppyandelectronics@gmail.com',
  'patelw401@gmail.com',
  'admin@skmobile.com',
  'admin.skmobile@gmail.com'
];

let app = null;
let auth = null;
let db = null;
let isFirebaseInitialized = false;

try {
  app = initializeApp(firebaseConfig);
  auth = getAuth(app);
  // Target existing Firestore named database instance matching firebase.json
  db = getFirestore(app, "ai-studio-android-skmobile-a123a057-9048-4d09-88d8-cfb38e18df87");
  isFirebaseInitialized = true;
  console.log("SK Mobile Firebase initialized successfully with database: ai-studio-android-skmobile-a123a057-9048-4d09-88d8-cfb38e18df87");
} catch (err) {
  console.warn("Firebase default database initialization fallback: ", err);
  try {
    if (app) db = getFirestore(app);
    isFirebaseInitialized = true;
  } catch (innerErr) {
    console.error("Firebase unavailable, falling back to secure local state:", innerErr);
    isFirebaseInitialized = false;
  }
}

export function isUserAdmin(email) {
  if (!email) return false;
  return AUTHORIZED_ADMIN_EMAILS.some(adminEmail => adminEmail.toLowerCase() === email.toLowerCase());
}

export async function loginWithGoogle() {
  if (!auth) {
    throw new Error("Authentication service is currently offline. Please try again later.");
  }
  const provider = new GoogleAuthProvider();
  provider.setCustomParameters({ prompt: 'select_account' });
  const result = await signInWithPopup(auth, provider);
  return result.user;
}

export async function logoutUser() {
  if (auth) {
    await signOut(auth);
  }
}

export function subscribeAuthState(callback) {
  if (!auth) {
    callback(null);
    return () => {};
  }
  return onAuthStateChanged(auth, callback);
}

// ----------------- FIRESTORE DATA LAYER WITH LOCAL STORAGE FALLBACK -----------------

const STORAGE_KEYS = {
  PRODUCTS: 'skm_products_cache',
  ORDERS: 'skm_orders_cache',
  REPAIRS: 'skm_repairs_cache',
  SELLS: 'skm_sells_cache',
  NOTIFICATIONS: 'skm_notifications_cache'
};

function getLocal(key, defaultVal = []) {
  try {
    const raw = localStorage.getItem(key);
    return raw ? JSON.parse(raw) : defaultVal;
  } catch (e) {
    return defaultVal;
  }
}

function setLocal(key, data) {
  try {
    localStorage.setItem(key, JSON.stringify(data));
  } catch (e) {
    console.warn("Local storage write error: ", e);
  }
}

// Products
export async function getProducts(initialFallback = []) {
  if (isFirebaseInitialized && db) {
    try {
      const snap = await getDocs(collection(db, 'products'));
      if (!snap.empty) {
        const list = snap.docs.map(d => ({ id: d.id, ...d.data() }));
        setLocal(STORAGE_KEYS.PRODUCTS, list);
        return list;
      }
    } catch (e) {
      console.warn("Firestore getProducts fallback to local: ", e);
    }
  }
  const cached = getLocal(STORAGE_KEYS.PRODUCTS, null);
  if (cached && cached.length > 0) return cached;
  setLocal(STORAGE_KEYS.PRODUCTS, initialFallback);
  return initialFallback;
}

export async function saveProduct(product) {
  if (isFirebaseInitialized && db) {
    try {
      const docRef = doc(db, 'products', product.id);
      await setDoc(docRef, { ...product, updatedAt: new Date().toISOString() }, { merge: true });
    } catch (e) {
      console.warn("Firestore saveProduct error, syncing locally: ", e);
    }
  }
  const list = getLocal(STORAGE_KEYS.PRODUCTS, []);
  const idx = list.findIndex(p => p.id === product.id);
  if (idx !== -1) list[idx] = product;
  else list.unshift(product);
  setLocal(STORAGE_KEYS.PRODUCTS, list);
  return product;
}

export async function deleteProduct(productId) {
  if (isFirebaseInitialized && db) {
    try {
      await deleteDoc(doc(db, 'products', productId));
    } catch (e) {
      console.warn("Firestore deleteProduct error: ", e);
    }
  }
  const list = getLocal(STORAGE_KEYS.PRODUCTS, []).filter(p => p.id !== productId);
  setLocal(STORAGE_KEYS.PRODUCTS, list);
  return true;
}

// Orders
export async function getCustomerOrders(customerId) {
  if (!customerId) return [];
  if (isFirebaseInitialized && db) {
    try {
      const q = query(collection(db, 'orders'), where('customerId', '==', customerId));
      const snap = await getDocs(q);
      const list = snap.docs.map(d => ({ orderId: d.id, ...d.data() }));
      return list.sort((a, b) => (b.createdAt || 0) - (a.createdAt || 0));
    } catch (e) {
      console.warn("Firestore getCustomerOrders error: ", e);
    }
  }
  const all = getLocal(STORAGE_KEYS.ORDERS, []);
  return all.filter(o => o.customerId === customerId || o.customerUid === customerId);
}

export async function getAllOrders() {
  if (isFirebaseInitialized && db) {
    try {
      const snap = await getDocs(collection(db, 'orders'));
      const list = snap.docs.map(d => ({ orderId: d.id, ...d.data() }));
      return list.sort((a, b) => (b.createdAt || 0) - (a.createdAt || 0));
    } catch (e) {
      console.warn("Firestore getAllOrders error: ", e);
    }
  }
  return getLocal(STORAGE_KEYS.ORDERS, []);
}

export async function createOrder(order) {
  if (isFirebaseInitialized && db) {
    try {
      await setDoc(doc(db, 'orders', order.orderId), order);
    } catch (e) {
      console.warn("Firestore createOrder error: ", e);
    }
  }
  const list = getLocal(STORAGE_KEYS.ORDERS, []);
  list.unshift(order);
  setLocal(STORAGE_KEYS.ORDERS, list);
  return order;
}

export async function updateOrderStatusWithAudit(orderId, newStatus, note = '', adminEmail = 'Store Admin') {
  const auditEntry = {
    status: newStatus,
    timestamp: Date.now(),
    note: note || `Status updated to ${newStatus}`,
    updatedBy: adminEmail
  };

  if (isFirebaseInitialized && db) {
    try {
      const docRef = doc(db, 'orders', orderId);
      const snap = await getDoc(docRef);
      if (snap.exists()) {
        const data = snap.data();
        const history = data.statusHistory || [];
        history.push(auditEntry);
        await updateDoc(docRef, {
          orderStatus: newStatus,
          status: newStatus,
          statusHistory: history,
          lastUpdated: Date.now()
        });
      }
    } catch (e) {
      console.warn("Firestore updateOrderStatus error: ", e);
    }
  }

  const list = getLocal(STORAGE_KEYS.ORDERS, []);
  const order = list.find(o => o.orderId === orderId);
  if (order) {
    order.status = newStatus;
    order.orderStatus = newStatus;
    order.statusHistory = order.statusHistory || [];
    order.statusHistory.push(auditEntry);
    order.lastUpdated = Date.now();
    setLocal(STORAGE_KEYS.ORDERS, list);
    return order;
  }
  return null;
}

export async function cancelCustomerOrder(orderId, reason, customerId) {
  const auditEntry = {
    status: 'Cancelled',
    timestamp: Date.now(),
    note: `Cancelled by customer. Reason: ${reason}`,
    updatedBy: 'Customer'
  };

  if (isFirebaseInitialized && db) {
    try {
      const docRef = doc(db, 'orders', orderId);
      await updateDoc(docRef, {
        orderStatus: 'Cancelled',
        status: 'Cancelled',
        cancellationReason: reason,
        updatedAt: Date.now()
      });
    } catch (e) {
      console.warn("Firestore cancelCustomerOrder error: ", e);
    }
  }

  const list = getLocal(STORAGE_KEYS.ORDERS, []);
  const order = list.find(o => o.orderId === orderId);
  if (order) {
    order.status = 'Cancelled';
    order.orderStatus = 'Cancelled';
    order.cancellationReason = reason;
    order.statusHistory = order.statusHistory || [];
    order.statusHistory.push(auditEntry);
    setLocal(STORAGE_KEYS.ORDERS, list);
    return order;
  }
  return null;
}

// Repair Bookings
export async function getRepairBookings(customerId = null) {
  if (isFirebaseInitialized && db) {
    try {
      let q = collection(db, 'repairBookings');
      if (customerId) {
        q = query(q, where('customerId', '==', customerId));
      }
      const snap = await getDocs(q);
      const list = snap.docs.map(d => ({ bookingId: d.id, ...d.data() }));
      return list.sort((a, b) => (b.bookingTimestamp || b.createdAt || 0) - (a.bookingTimestamp || a.createdAt || 0));
    } catch (e) {
      console.warn("Firestore getRepairBookings error: ", e);
    }
  }
  const all = getLocal(STORAGE_KEYS.REPAIRS, []);
  if (customerId) return all.filter(r => r.customerId === customerId || r.customerUid === customerId);
  return all;
}

export async function createRepairBooking(booking) {
  if (isFirebaseInitialized && db) {
    try {
      await setDoc(doc(db, 'repairBookings', booking.bookingId), booking);
    } catch (e) {
      console.warn("Firestore createRepairBooking error: ", e);
    }
  }
  const list = getLocal(STORAGE_KEYS.REPAIRS, []);
  list.unshift(booking);
  setLocal(STORAGE_KEYS.REPAIRS, list);
  return booking;
}

export async function updateRepairStatus(bookingId, status, estimatedCost = null) {
  const updates = { repairStatus: status, status: status, lastUpdated: Date.now() };
  if (estimatedCost) updates.estimatedCost = estimatedCost;

  if (isFirebaseInitialized && db) {
    try {
      await updateDoc(doc(db, 'repairBookings', bookingId), updates);
    } catch (e) {
      console.warn("Firestore updateRepairStatus error: ", e);
    }
  }
  const list = getLocal(STORAGE_KEYS.REPAIRS, []);
  const item = list.find(r => r.bookingId === bookingId);
  if (item) {
    Object.assign(item, updates);
    setLocal(STORAGE_KEYS.REPAIRS, list);
    return item;
  }
  return null;
}

// Old Phone Requests
export async function getOldPhoneRequests(customerId = null) {
  if (isFirebaseInitialized && db) {
    try {
      let q = collection(db, 'oldPhoneRequests');
      if (customerId) q = query(q, where('customerId', '==', customerId));
      const snap = await getDocs(q);
      return snap.docs.map(d => ({ requestId: d.id, ...d.data() }));
    } catch (e) {
      console.warn("Firestore getOldPhoneRequests error: ", e);
    }
  }
  const all = getLocal(STORAGE_KEYS.SELLS, []);
  if (customerId) return all.filter(s => s.customerId === customerId || s.customerUid === customerId);
  return all;
}

export async function createOldPhoneRequest(request) {
  if (isFirebaseInitialized && db) {
    try {
      await setDoc(doc(db, 'oldPhoneRequests', request.requestId), request);
    } catch (e) {
      console.warn("Firestore createOldPhoneRequest error: ", e);
    }
  }
  const list = getLocal(STORAGE_KEYS.SELLS, []);
  list.unshift(request);
  setLocal(STORAGE_KEYS.SELLS, list);
  return request;
}
