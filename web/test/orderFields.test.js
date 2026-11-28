import assert from 'node:assert/strict';
import test from 'node:test';
import { Timestamp } from 'firebase/firestore';
import { formatOrderDate, fulfillmentStatusUpdate, normalizeOrderFields, orderCreatedAtMillis } from '../src/orderFields.js';

test('Android numeric and web Firestore timestamps sort and display consistently', () => {
  const millis = 1764072000123;
  const android = { createdAt: millis };
  const web = { createdAt: Timestamp.fromMillis(millis) };
  assert.equal(orderCreatedAtMillis(android), millis);
  assert.equal(orderCreatedAtMillis(web), millis);
  assert.equal(formatOrderDate(android), formatOrderDate(web));
  assert.notEqual(formatOrderDate(android), 'Recent Order');
  assert.equal(orderCreatedAtMillis({ timestamp: millis }), millis);
  assert.equal(formatOrderDate({ orderDate: 'Legacy date' }), 'Legacy date');
});

test('both delivery schemas normalize without losing a leading zero in the PIN', () => {
  const android = { pinCode: '012345', deliveryMethod: 'Doorstep' };
  const web = { pincode: '012345', deliveryType: 'Doorstep' };
  for (const order of [android, web, { ...android, pincode: '', deliveryType: ' ' }]) {
    assert.equal(normalizeOrderFields(order).pincode, '012345');
    assert.equal(normalizeOrderFields(order).deliveryType, 'Doorstep');
  }
});

test('a delivered update cannot modify payment or settlement fields', () => {
  const updatedAt = Timestamp.fromMillis(1764072000123);
  const updates = fulfillmentStatusUpdate('Delivered', 'admin@example.com', updatedAt, 'Handed over');
  assert.deepEqual(updates, {
    orderStatus: 'Delivered', status: 'Delivered', lastUpdatedBy: 'admin@example.com',
    updatedAt, adminNote: 'Handed over'
  });
});
