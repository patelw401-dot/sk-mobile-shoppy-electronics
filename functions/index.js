const { initializeApp } = require('firebase-admin/app');
const { getFirestore, FieldValue } = require('firebase-admin/firestore');
const { onCall, HttpsError } = require('firebase-functions/v2/https');

const db = getFirestore(initializeApp(), 'ai-studio-android-skmobile-a123a057-9048-4d09-88d8-cfb38e18df87');

function text(value, max) {
  return typeof value === 'string' ? value.trim().slice(0, max) : '';
}

function documentId(value) {
  if (typeof value !== 'string' || !/^[\w.-]{1,128}$/.test(value) || value === '.' || value === '..') {
    throw new HttpsError('invalid-argument', 'Invalid document identifier.');
  }
  return value;
}

function authenticated(request) {
  if (!request.auth) throw new HttpsError('unauthenticated', 'Sign in first.');
  return request.auth;
}

exports.createStoreOrder = onCall(async request => {
  const { uid } = authenticated(request);
  const input = request.data || {};
  const orderId = documentId(input.orderId);
  const ref = db.collection('orders').doc(orderId);
  const customerName = text(input.customerName, 120);
  const phone = text(input.phone, 30);
  const address = text(input.address, 600);
  if (!customerName || phone.replace(/\D/g, '').length < 10 || !address ||
      !Array.isArray(input.items) || input.items.length < 1 || input.items.length > 50) {
    throw new HttpsError('invalid-argument', 'Check the cart and contact details.');
  }

  const quantities = new Map();
  const requestedItems = input.items.map(item => {
    const id = documentId(item?.id);
    const quantity = item?.quantity;
    if (!Number.isSafeInteger(quantity) || quantity < 1 || quantity > 99) {
      throw new HttpsError('invalid-argument', 'Invalid quantity.');
    }
    quantities.set(id, (quantities.get(id) || 0) + quantity);
    return { id, quantity, color: text(item.color, 80) };
  });

  return db.runTransaction(async transaction => {
    const existing = await transaction.get(ref);
    if (existing.exists) {
      if (existing.get('customerId') !== uid) throw new HttpsError('already-exists', 'Order ID unavailable.');
      return existing.data();
    }

    const products = new Map();
    for (const [id, quantity] of quantities) {
      const product = await transaction.get(db.collection('products').doc(id));
      const stock = Number(product.get('stock'));
      const price = Number(product.get('price'));
      if (!product.exists || product.get('isActive') === false || product.get('active') === false || product.get('inStock') === false ||
          !Number.isSafeInteger(stock) || stock < quantity ||
          !Number.isSafeInteger(price) || price < 1) {
        throw new HttpsError('failed-precondition', 'A product is unavailable or has insufficient stock.');
      }
      products.set(id, { product, stock, price });
    }

    const items = requestedItems.map(item => {
      const { product, price } = products.get(item.id);
      return {
        ...item, price, subtotal: price * item.quantity,
        name: product.get('name') || 'Product',
        imageUrl: product.get('imageUrl') || product.get('primaryImage') || '',
        brand: product.get('brand') || '',
        category: product.get('category') || ''
      };
    });
    const subtotal = items.reduce((sum, item) => sum + item.subtotal, 0);
    if (!Number.isSafeInteger(subtotal) || subtotal > 2147483647) {
      throw new HttpsError('invalid-argument', 'Order total is out of range.');
    }
    const deliveryMethod = text(input.deliveryMethod, 140);
    const pinCode = text(input.pinCode, 12);
    const now = Date.now();
    const order = {
      id: orderId, orderId, customerId: uid, customerName, phone, address, pinCode,
      pincode: pinCode, city: text(input.city, 100), state: text(input.state, 100),
      deliveryMethod, deliveryType: deliveryMethod, notes: text(input.notes, 800),
      paymentMethod: input.paymentMethod === 'UPI / QR Code on Delivery'
        ? 'UPI / QR Code on Delivery' : 'Cash on Delivery',
      paymentStatus: 'Pending', idempotencyKey: orderId, orderStatus: 'Order Placed', status: 'Order Placed',
      orderDate: new Intl.DateTimeFormat('en-IN', { dateStyle: 'medium', timeZone: 'Asia/Kolkata' }).format(now),
      items, subtotal, total: subtotal, deliveryCharge: 0,
      stockTracking: true, stockDeducted: true, createdAt: now, updatedAt: now,
      statusHistory: [{ status: 'Order Placed', timestamp: now, note: 'Order placed', updatedBy: 'Customer' }]
    };
    for (const [id, quantity] of quantities) {
      const { product, stock } = products.get(id);
      transaction.update(product.ref, { stock: stock - quantity, inStock: stock > quantity });
    }
    transaction.create(ref, order);
    return order;
  });
});

function activeAdminRecord(snapshot) {
  const record = snapshot.data();
  return record != null && record.active !== false &&
    (record.isAdmin ?? ['admin', 'superadmin'].includes(record.role)) === true;
}

exports.cancelStoreOrder = onCall(async request => {
  const { uid, token } = authenticated(request);
  const ref = db.collection('orders').doc(documentId(request.data?.orderId));
  const reason = text(request.data?.reason, 500);
  return db.runTransaction(async transaction => {
    const snapshot = await transaction.get(ref);
    if (!snapshot.exists) throw new HttpsError('not-found', 'Order unavailable.');
    const order = snapshot.data();
    let isAdmin = token.admin === true;
    if (!isAdmin && order.customerId !== uid) {
      const admin = await transaction.get(db.collection('admins').doc(uid));
      const allowed = ['skmobileshoppyandelectronics@gmail.com', 'patelw401@gmail.com',
        'admin@skmobile.com', 'admin.skmobile@gmail.com'];
      isAdmin = activeAdminRecord(admin) || (token.email_verified === true && allowed.includes(token.email));
      if (!isAdmin && token.email_verified === true && token.email) {
        isAdmin = activeAdminRecord(await transaction.get(db.collection('admins').doc(token.email)));
      }
    }
    if (order.customerId !== uid && !isAdmin) throw new HttpsError('permission-denied', 'Order unavailable.');
    const status = order.orderStatus || order.status;
    if (status === 'Cancelled') return { cancelled: true };
    if (status === 'Delivered') throw new HttpsError('failed-precondition', 'Delivered orders cannot be cancelled.');
    if (order.stockDeducted === true) {
      const quantities = new Map();
      for (const item of order.items) quantities.set(item.id, (quantities.get(item.id) || 0) + item.quantity);
      const products = await transaction.getAll(...Array.from(quantities.keys(), id => db.collection('products').doc(id)));
      for (const product of products) {
        if (product.exists) {
          transaction.update(product.ref, { stock: FieldValue.increment(quantities.get(product.id)), inStock: true });
        }
      }
    }
    const now = Date.now();
    transaction.update(ref, {
      orderStatus: 'Cancelled', status: 'Cancelled', cancellationReason: reason,
      updatedAt: now, stockDeducted: false,
      statusHistory: FieldValue.arrayUnion({
        status: 'Cancelled', timestamp: now, note: reason, updatedBy: isAdmin ? 'Admin' : 'Customer'
      })
    });
    return { cancelled: true };
  });
});
