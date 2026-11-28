import { initializeApp, getApps, getApp } from 'firebase/app';
import { getFirestore, collection, getDocs, getDoc, doc, setDoc, updateDoc, deleteDoc, query, orderBy, where, serverTimestamp, onSnapshot } from 'firebase/firestore';
import { 
  getAuth, 
  onAuthStateChanged, 
  signInWithPopup, 
  GoogleAuthProvider, 
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
  sendPasswordResetEmail,
  updateProfile,
  signOut 
} from 'firebase/auth';

// Approved Web Client Configuration for SK Mobile Shoppy and Electronics
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
 * Sign in using Email & Password
 */
export async function signInWithEmail(email, password) {
  return await signInWithEmailAndPassword(auth, (email || '').trim(), password);
}

/**
 * Create a new account using Email & Password
 */
export async function signUpWithEmail(name, email, password) {
  const cred = await createUserWithEmailAndPassword(auth, (email || '').trim(), password);
  const cleanName = (name || '').trim();
  if (cleanName && cred?.user) {
    try {
      await updateProfile(cred.user, { displayName: cleanName });
    } catch (err) {
      console.warn('Could not set displayName after signup:', err);
    }
  }
  return cred;
}

/**
 * Send password reset email
 */
export async function resetPasswordWithEmail(email) {
  return await sendPasswordResetEmail(auth, (email || '').trim());
}

/**
 * Format Firebase Auth error into a user-friendly message
 */
export function formatFirebaseAuthError(err) {
  const code = err?.code || '';
  switch (code) {
    case 'auth/invalid-email':
      return 'Please enter a valid email address.';
    case 'auth/user-not-found':
      return 'No account found with this email address.';
    case 'auth/wrong-password':
    case 'auth/invalid-credential':
      return 'Incorrect email or password. Please verify your credentials.';
    case 'auth/email-already-in-use':
      return 'An account with this email already exists. Please sign in instead.';
    case 'auth/weak-password':
      return 'Password is too weak. Please enter at least 6 characters.';
    case 'auth/user-disabled':
      return 'This account has been disabled. Please contact store support.';
    case 'auth/operation-not-allowed':
      return 'Email/Password sign-in is not enabled in Firebase Console (Authentication > Sign-in method).';
    case 'auth/too-many-requests':
      return 'Too many failed attempts. Please wait a few minutes before trying again.';
    case 'auth/network-request-failed':
      return 'Network error. Please check your internet connection.';
    case 'auth/popup-closed-by-user':
      return 'Sign-in window was closed before completing.';
    case 'auth/unauthorized-domain':
      return 'Current preview domain is not yet whitelisted in Firebase Auth Authorized Domains.';
    default:
      return err?.message || 'Authentication failed. Please try again.';
  }
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
      const stockNum = typeof data.stock === 'number' ? data.stock : (data.stock !== undefined ? (parseInt(data.stock, 10) || 0) : 0);
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
        hasReturnPolicy: data.hasReturnPolicy !== undefined ? Boolean(data.hasReturnPolicy) : true,
        returnTitle: (data.hasReturnPolicy !== undefined ? Boolean(data.hasReturnPolicy) : true)
          ? (data.returnTitle || '7-Day Return')
          : '',
        returnDescription: (data.hasReturnPolicy !== undefined ? Boolean(data.hasReturnPolicy) : true)
          ? (data.returnDescription || 'Hassle-free replacement at Udgir counter')
          : '',
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
 * Real-time listener for Firestore collection 'products'.
 * Bidirectionally synchronizes product updates from Android APK or Web Admin immediately without reload.
 */
export function subscribeToFirestoreProducts(onSuccess, onError) {
  const productsRef = collection(db, 'products');
  return onSnapshot(productsRef, (querySnapshot) => {
    if (querySnapshot.empty) {
      if (onSuccess) onSuccess({ success: true, data: [], isLive: true, isEmpty: true, error: null });
      return;
    }

    const items = [];
    querySnapshot.forEach((docSnap) => {
      const data = docSnap.data();
      const priceNum = typeof data.price === 'number' ? data.price : (parseInt(data.price, 10) || 0);
      const origPriceNum = typeof data.originalPrice === 'number' ? data.originalPrice : (data.originalPrice ? parseInt(data.originalPrice, 10) : undefined);
      const stockNum = typeof data.stock === 'number' ? data.stock : (data.stock !== undefined ? (parseInt(data.stock, 10) || 0) : 0);
      const inStockBool = data.inStock !== undefined ? Boolean(data.inStock) : stockNum > 0;
      const catSlug = (data.categorySlug || data.category || 'smartphones').toLowerCase();
      const singleImg = data.imageUrl || data.image || data.primaryImage || '';
      const imgArray = Array.isArray(data.images) && data.images.length > 0 ? data.images : (singleImg ? [singleImg] : []);

      items.push({
        id: docSnap.id,
        ...data,
        category: catSlug,
        categorySlug: catSlug,
        price: priceNum,
        originalPrice: origPriceNum,
        stock: stockNum,
        inStock: inStockBool,
        rating: data.rating || 4.7,
        reviewCount: data.reviewCount || 12,
        brand: data.brand || 'SK Mobile',
        name: data.name || 'Product',
        description: data.description || '',
        imageUrl: singleImg || (imgArray[0] || ''),
        images: imgArray,
        badge: data.badge || (data.isFeatured ? 'Featured' : (data.isBestSeller ? 'Best Seller' : '')),
        isFeatured: Boolean(data.isFeatured || data.featured),
        isBestSeller: Boolean(data.isBestSeller),
        hasWarranty: Boolean(data.hasWarranty),
        warrantyDuration: data.hasWarranty ? (data.warrantyDuration || data.warranty || '') : '',
        warrantyDescription: data.hasWarranty ? (data.warrantyDescription || '') : '',
        hasReturnPolicy: data.hasReturnPolicy !== undefined ? Boolean(data.hasReturnPolicy) : true,
        returnTitle: (data.hasReturnPolicy !== undefined ? Boolean(data.hasReturnPolicy) : true)
          ? (data.returnTitle || '7-Day Return')
          : '',
        returnDescription: (data.hasReturnPolicy !== undefined ? Boolean(data.hasReturnPolicy) : true)
          ? (data.returnDescription || 'Hassle-free replacement at Udgir counter')
          : '',
        specs: data.specifications || data.specs || {},
        specifications: data.specifications || data.specs || {},
        createdAt: data.createdAt || null,
        updatedAt: data.updatedAt || null
      });
    });

    if (onSuccess) {
      onSuccess({
        success: true,
        data: items,
        isLive: true,
        isEmpty: false,
        error: null
      });
    }
  }, (err) => {
    console.error("Firestore onSnapshot error on 'products' collection:", err);
    if (onError) onError(err);
  });
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
  if (!user || !user.email || user.emailVerified !== true) return false;
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
      const stockNum = typeof data.stock === 'number' ? data.stock : (data.stock !== undefined ? (parseInt(data.stock, 10) || 0) : 0);
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
        hasReturnPolicy: data.hasReturnPolicy !== undefined ? Boolean(data.hasReturnPolicy) : true,
        returnTitle: (data.hasReturnPolicy !== undefined ? Boolean(data.hasReturnPolicy) : true)
          ? (data.returnTitle || '7-Day Return')
          : '',
        returnDescription: (data.hasReturnPolicy !== undefined ? Boolean(data.hasReturnPolicy) : true)
          ? (data.returnDescription || 'Hassle-free replacement at Udgir counter')
          : '',
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
  const inStock = stock > 0 && productData.inStock !== false;
  const discount = origPrice && origPrice > price ? Math.round(((origPrice - price) / origPrice) * 100) : 0;
  const hasWarranty = Boolean(productData.hasWarranty);
  const warrantyDuration = hasWarranty ? (productData.warrantyDuration || '').trim() : '';
  const warrantyDescription = hasWarranty ? (productData.warrantyDescription || '').trim() : '';
  const hasReturnPolicy = productData.hasReturnPolicy !== undefined ? Boolean(productData.hasReturnPolicy) : true;
  const returnTitle = hasReturnPolicy ? ((productData.returnTitle || '').trim() || '7-Day Return') : '';
  const returnDescription = hasReturnPolicy ? ((productData.returnDescription || '').trim() || 'Hassle-free replacement at Udgir counter') : '';

  const catSlug = (productData.category || productData.categorySlug || 'smartphones').toLowerCase();
  const singleImage = (productData.imageUrl || productData.image || '').trim();
  const imageList = Array.isArray(productData.images) && productData.images.length > 0
    ? productData.images
    : (singleImage ? [singleImage] : []);

  const payload = {
    id: docId,
    name,
    brand,
    category: catSlug,
    categorySlug: catSlug,
    price,
    stock,
    inStock,
    description: (productData.description || '').trim(),
    imageUrl: singleImage,
    images: imageList,
    badge: (productData.badge || '').trim(),
    isFeatured: Boolean(productData.isFeatured),
    featured: Boolean(productData.isFeatured),
    isBestSeller: Boolean(productData.isBestSeller),
    hasWarranty,
    warrantyDuration,
    warrantyDescription,
    warranty: hasWarranty ? (warrantyDuration || '1 Year Brand Warranty') : 'No Warranty',
    hasReturnPolicy,
    returnTitle,
    returnDescription,
    rating: productData.rating || 4.7,
    reviewCount: productData.reviewCount || 10,
    specs: productData.specs || productData.specifications || {},
    specifications: productData.specs || productData.specifications || {},
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
 *     request.resource.data.paymentStatus == 'Pending'
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
    imageUrl: item.imageUrl || item.primaryImage || item.image || (Array.isArray(item.images) && item.images[0]) || '',
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

/**
 * Fetch categories from Firestore 'categories' collection.
 * Public read access allowed by security rules.
 */
export async function fetchCategoriesFromFirestore() {
  try {
    const colRef = collection(db, 'categories');
    const snap = await getDocs(colRef);
    if (snap.empty) {
      return { success: true, data: [], isEmpty: true };
    }
    const list = [];
    snap.forEach(d => {
      const data = d.data();
      list.push({
        id: d.id,
        name: data.name || 'Unnamed Category',
        slug: (data.slug || d.id).toLowerCase(),
        imageUrl: data.imageUrl || '',
        description: data.description || '',
        isActive: data.isActive !== undefined ? Boolean(data.isActive) : true,
        displayOrder: typeof data.displayOrder === 'number' ? data.displayOrder : (parseInt(data.displayOrder, 10) || 99),
        createdAt: data.createdAt || null,
        updatedAt: data.updatedAt || null
      });
    });
    list.sort((a, b) => a.displayOrder - b.displayOrder);
    return { success: true, data: list, isEmpty: false };
  } catch (err) {
    console.error("fetchCategoriesFromFirestore error:", err);
    return { success: false, data: [], error: err.message };
  }
}

/**
 * Save (create or edit) category in Firestore 'categories' collection.
 * Restricted strictly to authorized store administrators.
 */
export async function saveAdminCategoryToFirestore(categoryData, currentUser) {
  if (!isUserAdmin(currentUser)) {
    throw new Error('Unauthorized: You must be an authorized store administrator to manage categories.');
  }

  const name = (categoryData.name || '').trim();
  if (!name) throw new Error('Category name is required.');

  const slug = (categoryData.slug || name.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, '')).trim();
  const docId = (categoryData.id || `cat-${slug}`).trim();
  const docRef = doc(db, 'categories', docId);

  const payload = {
    id: docId,
    name,
    slug,
    imageUrl: (categoryData.imageUrl || '').trim(),
    description: (categoryData.description || '').trim(),
    isActive: categoryData.isActive !== undefined ? Boolean(categoryData.isActive) : true,
    displayOrder: typeof categoryData.displayOrder === 'number' ? categoryData.displayOrder : (parseInt(categoryData.displayOrder, 10) || 1),
    lastUpdatedBy: currentUser.email || 'Store Admin',
    updatedAt: serverTimestamp()
  };

  if (!categoryData.id) {
    payload.createdAt = serverTimestamp();
    payload.createdBy = currentUser.email || 'Store Admin';
  }

  await setDoc(docRef, payload, { merge: true });

  return { success: true, id: docId, category: payload };
}

/**
 * Toggle category active/inactive status (Admin only).
 */
export async function toggleAdminCategoryActive(categoryId, currentActive, currentUser) {
  if (!isUserAdmin(currentUser)) {
    throw new Error('Unauthorized: Admin access required.');
  }
  const docRef = doc(db, 'categories', categoryId);
  const newActive = !currentActive;
  await updateDoc(docRef, {
    isActive: newActive,
    lastUpdatedBy: currentUser.email || 'Store Admin',
    updatedAt: serverTimestamp()
  });
  return { success: true, categoryId, isActive: newActive };
}

/**
 * Safe Category Deletion:
 * Checks if products belong to this category.
 * If reassignToSlug is provided, reassigns products to reassignToSlug.
 * Then deletes the category document.
 */
export async function deleteAdminCategoryFromFirestore(categoryId, categorySlug, reassignToSlug = null, currentUser) {
  if (!isUserAdmin(currentUser)) {
    throw new Error('Unauthorized: Admin access required to delete categories.');
  }
  if (!categoryId) {
    throw new Error('Category ID is required for deletion.');
  }

  // 1. If reassignment requested, update products assigned to this category
  if (reassignToSlug && reassignToSlug !== categorySlug) {
    try {
      const prodQuery = query(collection(db, 'products'));
      const prodSnap = await getDocs(prodQuery);
      const updates = [];
      prodSnap.forEach(pDoc => {
        const pData = pDoc.data();
        const pCat = (pData.category || '').toLowerCase();
        if (pCat === (categorySlug || '').toLowerCase() || pCat === (categoryId || '').toLowerCase()) {
          updates.push(updateDoc(pDoc.ref, {
            category: reassignToSlug.toLowerCase(),
            updatedAt: serverTimestamp(),
            lastUpdatedBy: currentUser.email || 'Store Admin'
          }));
        }
      });
      if (updates.length > 0) {
        await Promise.all(updates);
      }
    } catch (e) {
      console.warn("Could not reassign products before category deletion:", e);
    }
  }

  // 2. Delete the category document
  const docRef = doc(db, 'categories', categoryId);
  await deleteDoc(docRef);

  return { success: true, deletedId: categoryId, reassignedTo: reassignToSlug };
}

/**
 * Seed initial categories to Firestore if empty.
 */
export async function seedDefaultCategoriesToFirestore(currentUser, defaultCategories = []) {
  if (!isUserAdmin(currentUser)) {
    throw new Error('Unauthorized: Admin access required.');
  }
  let count = 0;
  for (const cat of defaultCategories) {
    const docRef = doc(db, 'categories', cat.id);
    await setDoc(docRef, {
      ...cat,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
      createdBy: currentUser.email || 'Store Admin'
    }, { merge: true });
    count++;
  }
  return { success: true, count };
}

/**
 * Default repair pricing matching Android MockData.repairServices (settings/repair_pricing)
 */
export const DEFAULT_REPAIR_PRICING = [
  {
    id: 'rep-1',
    title: 'Display Replacement',
    estimatedStartingPrice: 1499,
    turnaroundTime: '30 - 60 Minutes',
    description: 'Cracked glass, blank screen, lines, or touch responsiveness issues. Original OLED/IPS screens with genuine touch calibration.',
    iconType: 'display'
  },
  {
    id: 'rep-2',
    title: 'Battery Replacement',
    estimatedStartingPrice: 699,
    turnaroundTime: '20 - 45 Minutes',
    description: 'Quick battery drain, overheating, phone swelling, or unexpected shutdowns. Grade-A certified OEM batteries.',
    iconType: 'battery'
  },
  {
    id: 'rep-3',
    title: 'Charging Problem',
    estimatedStartingPrice: 349,
    turnaroundTime: '30 Minutes',
    description: 'Loose charging jack, slow charging, moisture detected, or complete charging failure. Precision Type-C / Micro-USB port replacement.',
    iconType: 'charging'
  },
  {
    id: 'rep-4',
    title: 'Speaker / Mic Problem',
    estimatedStartingPrice: 299,
    turnaroundTime: '30 - 45 Minutes',
    description: 'Crackling audio, low call volume, receiver issues, or caller cannot hear your voice. Original ear piece and bottom speaker replacement.',
    iconType: 'audio'
  },
  {
    id: 'rep-5',
    title: 'Software Problem & Flashing',
    estimatedStartingPrice: 249,
    turnaroundTime: '45 - 90 Minutes',
    description: 'Phone hanging, stuck on logo (boot loop), FRP / pattern unlock, OS update failures, or network unlocking.',
    iconType: 'software'
  },
  {
    id: 'rep-6',
    title: 'Water Damage Recovery',
    estimatedStartingPrice: 599,
    turnaroundTime: '2 - 4 Hours',
    description: 'Ultrasonic motherboard cleaning, chemical corrosion removal, and short circuit diagnosis with thermal camera inspection.',
    iconType: 'water'
  }
];

function extractNumericPriceFallback(rawText, fallback = 0) {
  const cleaned = String(rawText ?? '').replace(/,/g, '').trim();
  const match = cleaned.match(/\d+/);
  if (match) {
    const num = Number.parseInt(match[0], 10);
    if (Number.isFinite(num) && num >= 0) return num;
  }
  const fb = Number.parseInt(fallback, 10);
  return Number.isFinite(fb) && fb >= 0 ? fb : 0;
}

export function formatRepairPriceDisplay(service) {
  if (!service) return 'Quote on Diagnosis';
  const raw = String(service.priceText ?? '').trim();
  if (!raw) {
    const num = Number(service.estimatedStartingPrice || 0);
    return `From ₹${Number.isFinite(num) ? num.toLocaleString('en-IN') : '0'}`;
  }
  if (/^\d+$/.test(raw)) {
    const num = Number.parseInt(raw, 10);
    return `From ₹${num.toLocaleString('en-IN')}`;
  }
  return raw;
}

function mergeRepairPricingData(docData) {
  if (!docData || typeof docData !== 'object') {
    return DEFAULT_REPAIR_PRICING.map((d) => ({
      ...d,
      priceText: String(d.estimatedStartingPrice ?? 0)
    }));
  }
  const pricesById = (docData.pricesById && typeof docData.pricesById === 'object') ? docData.pricesById : {};
  const priceTextById = (docData.priceTextById && typeof docData.priceTextById === 'object') ? docData.priceTextById : {};
  const hasServicesList = Array.isArray(docData.services);

  if (hasServicesList) {
    return docData.services
      .filter((entry) => entry && entry.id && String(entry.id).trim())
      .map((entry) => {
        const id = String(entry.id).trim();
        const def = DEFAULT_REPAIR_PRICING.find((d) => d.id === id);
        const rawPrice = entry.estimatedStartingPrice ?? pricesById[id] ?? def?.estimatedStartingPrice ?? 0;
        const rawText = String(
          (entry.priceText !== undefined && entry.priceText !== null && String(entry.priceText).trim() !== '')
            ? entry.priceText
            : (priceTextById[id] !== undefined && priceTextById[id] !== null && String(priceTextById[id]).trim() !== '')
              ? priceTextById[id]
              : rawPrice
        ).trim();
        const parsedPrice = extractNumericPriceFallback(rawText, rawPrice);
        return {
          id,
          title: (entry.title && String(entry.title).trim()) || def?.title || id,
          estimatedStartingPrice: parsedPrice,
          priceText: rawText,
          turnaroundTime: (entry.turnaroundTime && String(entry.turnaroundTime).trim()) || def?.turnaroundTime || '30 - 60 Minutes',
          warrantyDays: Number.isFinite(Number(entry.warrantyDays)) ? Number(entry.warrantyDays) : (def?.warrantyDays ?? 90),
          description: (entry.description && String(entry.description).trim()) || def?.description || '',
          iconType: (entry.iconType && String(entry.iconType).trim()) || def?.iconType || 'repair'
        };
      });
  }

  return DEFAULT_REPAIR_PRICING.map((def) => {
    const rawNum = pricesById[def.id] !== undefined && pricesById[def.id] !== null
      ? pricesById[def.id]
      : def.estimatedStartingPrice;
    const rawText = String(
      priceTextById[def.id] !== undefined && priceTextById[def.id] !== null && String(priceTextById[def.id]).trim() !== ''
        ? priceTextById[def.id]
        : rawNum
    ).trim();
    return {
      ...def,
      estimatedStartingPrice: extractNumericPriceFallback(rawText, rawNum),
      priceText: rawText
    };
  });
}

/**
 * Subscribe to real-time repair pricing updates from Firestore settings/repair_pricing.
 * Public read access allowed by security rules; falls back to DEFAULT_REPAIR_PRICING when missing.
 */
export function subscribeToRepairPricing(onUpdate, onError) {
  try {
    const docRef = doc(db, 'settings', 'repair_pricing');
    return onSnapshot(
      docRef,
      (snapshot) => {
        if (snapshot.exists()) {
          onUpdate?.(mergeRepairPricingData(snapshot.data()));
        } else {
          onUpdate?.(DEFAULT_REPAIR_PRICING.map((d) => ({ ...d, priceText: String(d.estimatedStartingPrice ?? 0) })));
        }
      },
      (err) => {
        console.warn('subscribeToRepairPricing listener notice:', err);
        onError?.(err);
      }
    );
  } catch (err) {
    console.error('subscribeToRepairPricing setup error:', err);
    onError?.(err);
    return () => {};
  }
}

/**
 * Save repair service starting prices & categories to Firestore settings/repair_pricing (Admin only).
 */
export async function saveAdminRepairPricingToFirestore(services, currentUser) {
  if (!isUserAdmin(currentUser)) {
    throw new Error('Unauthorized: Store administrator privileges are required to update repair prices.');
  }
  const normalized = (Array.isArray(services) ? services : DEFAULT_REPAIR_PRICING).map((s) => {
    const priceText = String(
      s.priceText !== undefined && s.priceText !== null && String(s.priceText).trim() !== ''
        ? s.priceText
        : (s.estimatedStartingPrice ?? 0)
    ).trim();
    const numericPrice = extractNumericPriceFallback(priceText, s.estimatedStartingPrice ?? 0);
    return {
      id: String(s.id || `rep-${Date.now()}`).trim(),
      title: String(s.title || 'Repair Service').trim(),
      estimatedStartingPrice: numericPrice,
      priceText,
      turnaroundTime: String(s.turnaroundTime || '30 - 60 Minutes').trim(),
      warrantyDays: Number.isFinite(Number(s.warrantyDays)) ? Number(s.warrantyDays) : 90,
      description: String(s.description || '').trim(),
      iconType: String(s.iconType || 'repair').trim()
    };
  });
  const pricesById = {};
  const priceTextById = {};
  normalized.forEach((s) => {
    pricesById[s.id] = s.estimatedStartingPrice;
    priceTextById[s.id] = s.priceText;
  });

  const docRef = doc(db, 'settings', 'repair_pricing');
  await setDoc(
    docRef,
    {
      services: normalized,
      pricesById,
      priceTextById,
      lastUpdatedBy: currentUser?.email || 'Store Admin',
      updatedAt: serverTimestamp()
    },
    { merge: true }
  );

  return { success: true, services: normalized };
}

/**
 * Default Shop Information matching Android ShopSettings (settings/shop_info)
 */
export const DEFAULT_SERVICES_LIST = [
  'New Smartphones & 5G Mobiles',
  'Certified Pre-Owned Phones',
  'Express Mobile Repair & Chip-Level Service',
  'Original Chargers, Audio & Accessories',
  'New & Old Mobile Exchange (Coming Soon)'
];

export const DEFAULT_SHOP_INFO = {
  shopName: 'SK Mobile Shoppy and Electronics',
  phoneNumber: '+91 9823109313',
  whatsappNumber: '+91 9823109313',
  email: 'skmobileshoppyandelectronics@gmail.com',
  address: 'Near Nagar Parishad Office, Station Road, Udgir, Maharashtra',
  pincode: '413517',
  landmark: 'Opposite Nagar Parishad Municipal Complex',
  openingTime: '09:30 AM',
  closingTime: '09:30 PM',
  openingHours: '9:30 AM – 9:30 PM (Open All 7 Days)',
  services: DEFAULT_SERVICES_LIST,
  googleMapsUrl: 'https://maps.google.com/?q=SK+Mobile+Shoppy+and+Electronics+Nagar+Parishad+Udgir+413517',
  pickupAvailable: true,
  homeDeliveryAvailable: true,
  deliveryCharge: 49,
  freeDeliveryThreshold: 499,
  upiEnabled: true,
  codEnabled: true,
  onlinePaymentEnabled: true
};

function toDialablePhone(raw, fallback = '+919823109313') {
  const str = String(raw || '').trim();
  const digits = str.replace(/\D/g, '');
  if (!digits) return fallback;
  if (str.startsWith('+')) return `+${digits}`;
  if (digits.length === 10) return `+91${digits}`;
  return `+${digits}`;
}

function toWhatsappDigits(raw, fallback = '919823109313') {
  const digits = String(raw || '').replace(/\D/g, '');
  if (!digits) return fallback;
  if (digits.length === 10) return `91${digits}`;
  return digits;
}

export function formatShopInfo(raw = {}) {
  const src = raw && typeof raw === 'object' ? raw : {};
  const shopName = String(src.shopName || DEFAULT_SHOP_INFO.shopName).trim() || DEFAULT_SHOP_INFO.shopName;
  const phoneNumber = String(src.phoneNumber || DEFAULT_SHOP_INFO.phoneNumber).trim() || DEFAULT_SHOP_INFO.phoneNumber;
  const whatsappNumber = String(src.whatsappNumber || phoneNumber || DEFAULT_SHOP_INFO.whatsappNumber).trim() || phoneNumber;
  const email = String(src.email || DEFAULT_SHOP_INFO.email).trim() || DEFAULT_SHOP_INFO.email;
  const address = String(src.address || DEFAULT_SHOP_INFO.address).trim() || DEFAULT_SHOP_INFO.address;
  const pincode = String(src.pincode || DEFAULT_SHOP_INFO.pincode).trim() || DEFAULT_SHOP_INFO.pincode;
  const landmark = String(src.landmark || DEFAULT_SHOP_INFO.landmark).trim() || DEFAULT_SHOP_INFO.landmark;
  const openingTime = String(src.openingTime || DEFAULT_SHOP_INFO.openingTime).trim() || DEFAULT_SHOP_INFO.openingTime;
  const closingTime = String(src.closingTime || DEFAULT_SHOP_INFO.closingTime).trim() || DEFAULT_SHOP_INFO.closingTime;
  const openingHours = String(src.openingHours || `${openingTime} – ${closingTime} (Open All 7 Days)`).trim();
  const services = Array.isArray(src.services) && src.services.length > 0
    ? src.services.map((s) => String(s || '').trim()).filter(Boolean)
    : DEFAULT_SERVICES_LIST;
  const googleMapsUrl = String(src.googleMapsUrl || DEFAULT_SHOP_INFO.googleMapsUrl).trim() || DEFAULT_SHOP_INFO.googleMapsUrl;

  const fullAddress = !pincode
    ? address
    : address.includes(pincode)
      ? address
      : `${address} - ${pincode}`;

  const shortLocation = address
    .split(',')
    .map((p) => p.trim())
    .find((p) => p.toLowerCase().includes('udgir'))
    ?.split('-')[0]
    ?.trim() || 'Udgir';

  return {
    ...DEFAULT_SHOP_INFO,
    ...src,
    shopName,
    phoneNumber,
    whatsappNumber,
    email,
    address,
    pincode,
    landmark,
    openingTime,
    closingTime,
    openingHours,
    services,
    googleMapsUrl,
    fullAddress,
    shortLocationLabel: pincode ? `${shortLocation} ${pincode}` : shortLocation,
    locationSlug: landmark ? `${shortLocation}, ${pincode}` : shortLocation,
    dialPhone: toDialablePhone(phoneNumber),
    whatsappDigits: toWhatsappDigits(whatsappNumber, toWhatsappDigits(phoneNumber)),
    deliveryCharge: Number.isFinite(Number(src.deliveryCharge)) ? Number(src.deliveryCharge) : DEFAULT_SHOP_INFO.deliveryCharge,
    freeDeliveryThreshold: Number.isFinite(Number(src.freeDeliveryThreshold)) ? Number(src.freeDeliveryThreshold) : DEFAULT_SHOP_INFO.freeDeliveryThreshold
  };
}

/**
 * Subscribe to real-time shop info updates from Firestore settings/shop_info
 * (with one-time fallback check on settings/store_config if shop_info doesn't exist yet).
 */
export function subscribeToShopInfo(onUpdate, onError) {
  try {
    const shopInfoRef = doc(db, 'settings', 'shop_info');
    return onSnapshot(
      shopInfoRef,
      async (snapshot) => {
        if (snapshot.exists()) {
          onUpdate?.(formatShopInfo(snapshot.data()));
        } else {
          try {
            const legacySnap = await getDoc(doc(db, 'settings', 'store_config'));
            if (legacySnap.exists()) {
              onUpdate?.(formatShopInfo(legacySnap.data()));
              return;
            }
          } catch (_) {
            // ignore legacy fallback error
          }
          onUpdate?.(formatShopInfo(DEFAULT_SHOP_INFO));
        }
      },
      (err) => {
        console.warn('subscribeToShopInfo listener notice:', err);
        onError?.(err);
      }
    );
  } catch (err) {
    console.error('subscribeToShopInfo setup error:', err);
    onError?.(err);
    return () => {};
  }
}

/**
 * Save shop information to Firestore settings/shop_info (and mirror to store_config) (Admin only).
 */
export async function saveAdminShopInfoToFirestore(shopData, currentUser) {
  if (!isUserAdmin(currentUser)) {
    throw new Error('Unauthorized: Store administrator privileges are required to update shop information.');
  }
  const formatted = formatShopInfo(shopData);
  const payload = {
    shopName: formatted.shopName,
    phoneNumber: formatted.phoneNumber,
    whatsappNumber: formatted.whatsappNumber,
    email: formatted.email,
    address: formatted.address,
    pincode: formatted.pincode,
    landmark: formatted.landmark,
    openingTime: formatted.openingTime,
    closingTime: formatted.closingTime,
    openingHours: formatted.openingHours,
    services: formatted.services,
    googleMapsUrl: formatted.googleMapsUrl,
    pickupAvailable: Boolean(formatted.pickupAvailable),
    homeDeliveryAvailable: Boolean(formatted.homeDeliveryAvailable),
    deliveryCharge: Math.max(0, Number.parseInt(formatted.deliveryCharge, 10) || 0),
    freeDeliveryThreshold: Math.max(0, Number.parseInt(formatted.freeDeliveryThreshold, 10) || 0),
    upiEnabled: Boolean(formatted.upiEnabled),
    codEnabled: Boolean(formatted.codEnabled),
    onlinePaymentEnabled: Boolean(formatted.onlinePaymentEnabled),
    lastUpdatedBy: currentUser?.email || 'Store Admin',
    updatedAt: serverTimestamp()
  };

  await setDoc(doc(db, 'settings', 'shop_info'), payload, { merge: true });
  await setDoc(doc(db, 'settings', 'store_config'), payload, { merge: true });

  return { success: true, shopInfo: formatShopInfo(payload) };
}


