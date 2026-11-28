const { randomUUID } = require('node:crypto');
const assert = require('node:assert/strict');
const { test } = require('node:test');
const { getFirestore } = require('firebase-admin/firestore');

if (!process.env.FIRESTORE_EMULATOR_HOST) throw new Error('Run against the Firestore emulator.');
process.env.GCLOUD_PROJECT = 'demo-no-project';
const { createStoreOrder, cancelStoreOrder } = require('./index');
const db = getFirestore('ai-studio-android-skmobile-a123a057-9048-4d09-88d8-cfb38e18df87');

test('catalog pricing, last-unit stock, idempotent order creation and cancellation', async () => {
  const productId = randomUUID();
  const product = db.collection('products').doc(productId);
  const uid = randomUUID();
  const orderIds = [randomUUID(), randomUUID()];
  await product.set({ name: 'Test accessory', price: 499, stock: 1, inStock: true });
  try {
    const orderData = orderId => ({
      orderId, customerName: 'Test customer', phone: '9999999999', address: 'Udgir',
      total: 1, paymentStatus: 'Paid', items: [{ id: productId, quantity: 1, price: 1 }]
    });
    const request = orderId => ({ auth: { uid, token: {} }, data: orderData(orderId) });
    const outcomes = await Promise.allSettled(orderIds.map(id => createStoreOrder.run(request(id))));
    const orders = outcomes.filter(result => result.status === 'fulfilled');
    assert.equal(orders.length, 1);
    assert.equal(outcomes.find(result => result.status === 'rejected').reason.code, 'failed-precondition');
    const order = orders[0].value;
    assert.equal(order.total, 499);
    assert.equal(order.paymentStatus, 'Pending');
    assert.equal((await product.get()).get('stock'), 0);
    assert.equal((await createStoreOrder.run(request(order.id))).id, order.id);
    const cancel = { auth: { uid, token: {} }, data: { orderId: order.id } };
    await assert.rejects(cancelStoreOrder.run({ ...cancel, auth: { uid: 'other', token: {} } }),
      { code: 'permission-denied' });
    await cancelStoreOrder.run(cancel);
    await cancelStoreOrder.run(cancel);
    assert.equal((await product.get()).get('stock'), 1);
    assert.equal((await db.collection('orders').doc(order.id).get()).get('orderStatus'), 'Cancelled');
  } finally {
    await Promise.all([product.delete(), ...orderIds.map(id => db.collection('orders').doc(id).delete())]);
  }
});
