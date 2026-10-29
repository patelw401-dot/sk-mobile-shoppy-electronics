import { initializeApp, getApps, getApp } from 'firebase/app';
import { getFirestore, collection, getDocs, doc, setDoc, serverTimestamp } from 'firebase/firestore';
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
      items.push({
        id: doc.id,
        ...data,
        category: (data.category || '').toLowerCase(),
        price: typeof data.price === 'number' ? data.price : parseInt(data.price, 10) || 0,
        originalPrice: typeof data.originalPrice === 'number' ? data.originalPrice : (parseInt(data.originalPrice, 10) || undefined),
        rating: data.rating || 4.5,
        reviewCount: data.reviewCount || 0,
        brand: data.brand || 'Generic',
        name: data.name || 'Product',
        description: data.description || '',
        specs: data.specifications || data.specs || {},
        inStock: data.stock !== undefined ? data.stock > 0 : true
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
