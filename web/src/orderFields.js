function timestampMillis(value) {
  const millis = typeof value?.toMillis === 'function' ? value.toMillis() : value;
  return Number.isFinite(millis) ? millis : null;
}

function firstText(...values) {
  return values.find(value => typeof value === 'string' && value.trim()) ?? '';
}

export function orderCreatedAtMillis(order) {
  return timestampMillis(order.createdAt) ?? timestampMillis(order.timestamp);
}

export function normalizeOrderFields(order) {
  return {
    ...order,
    pincode: firstText(order.pinCode, order.pincode),
    deliveryType: firstText(order.deliveryMethod, order.deliveryType)
  };
}

export function formatOrderDate(order) {
  const millis = orderCreatedAtMillis(order);
  if (millis === null) return firstText(order.orderDate) || 'Recent Order';
  return new Date(millis).toLocaleDateString('en-IN', {
    day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'
  });
}

export function fulfillmentStatusUpdate(newStatus, adminEmail, updatedAt, adminNote = '') {
  const updates = {
    orderStatus: newStatus,
    status: newStatus,
    lastUpdatedBy: adminEmail || 'Store Admin',
    updatedAt
  };
  if (adminNote) updates.adminNote = adminNote;
  return updates;
}
