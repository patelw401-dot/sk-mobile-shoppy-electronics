# Web ⇄ Android shared data contract

The website (`web/`) and the Android APK (`app/`) use **one** Firebase project and the **same**
Firestore database / Storage bucket. Both must read and write the shapes below.
Helpers: `web/src/data/syncSchema.js` (web) and `FirestoreDataService.kt` (Android).

## Timestamps
* `createdAt`, `updatedAt` (and `stockUpdatedAt`) on every top-level document are **Firestore Timestamps**
  written with `serverTimestamp()` / `FieldValue.serverTimestamp()`.
* Entries inside `statusHistory[]` use `timestamp` = epoch **milliseconds** (`serverTimestamp()` is not allowed in arrays).
* Readers accept Timestamp, epoch-millis number, `Date` and ISO string (old APK builds wrote numbers):
  web `toMillis()`, Android `extractMillis()`.

## products/{id}
`id, name, brand, category, categorySlug, price (int), originalPrice (omitted/deleted when no offer), discount (int %),
rating, reviewCount, description, stock (int), inStock (stock > 0), sku, active, featured/isFeatured, isBestSeller,
badge, warranty, hasWarranty, warrantyDuration, warrantyDescription, imageUrl, images[], specs/specifications,
createdAt, updatedAt`
* `images[0] == imageUrl`. Extra gallery images are preserved on edit by both platforms.
* `imageUrl` is an `https://` Firebase Storage URL. Inline base64 images are migrated to Storage by the website on next save.
* Storage path: `products/{productId}/{timestamp}.jpg` (public read, admin-only write, image/*, < 5 MB).

## orders/{id}
`id, customerId, customerName, phone, alternatePhone?, address, city, state, pinCode, deliveryMethod, paymentMethod,
paymentStatus ('Pending'|'Paid'), orderStatus, items[], subtotal, deliveryCharge, total (int), notes?, statusHistory[],
stockTracking, stockDeducted, stockShortfall?, createdAt, updatedAt`
* `items[]`: `{ id, name, brand, category, price, quantity, imageUrl, color, subtotal }`.
* `orderStatus` vocabulary (same as Android `OrderTrackingStage`): `Order Placed → Order Confirmed → Preparing / Packed →
  Shipped / Out for Delivery → Delivered`, or `Cancelled`. Legacy web values (`Pending`, `Confirmed`, `Processing`, `Shipped`)
  are still understood by both platforms.
* Legacy field names still read by both platforms: `pincode` (→ `pinCode`), `deliveryType` (→ `deliveryMethod`).

### Stock rules
Customers cannot write `products`, so stock is reconciled by an **admin session** (website admin or APK admin):
* `orderStatus` ≥ Confirmed and `stockDeducted != true` → subtract quantities, set `stockDeducted = true`.
* `orderStatus` = Cancelled and `stockDeducted == true` → add quantities back, set `stockDeducted = false`.
* Done in a Firestore **transaction** (safe against two admins / web + APK acting together); stock never goes below 0
  (`stockShortfall = true` when clamped); products missing from the catalog are skipped.
* Only orders with `stockTracking == true` are touched, so historic orders never change stock.
* Triggered on every admin status change and automatically when an admin listener sees an order that needs it.
* Firestore rules reject customer-created orders that claim `stockDeducted: true`.

## repairBookings/{id}
`id, customerId, customerName, phone, brand, model, problem, pickupRequired, address, preferredDate, preferredSlot,
estimatedCost, repairStatus, notes, createdAt, updatedAt` — `preferredDate` and `preferredSlot` are kept equal by the APK;
the website sends both (date and slot label).
Statuses: `Requested, Appointment Booked, Pickup Scheduled, Received, Diagnosing, Estimate Sent, Repairing, Ready, Delivered, Cancelled`
(legacy `In Progress` → Repairing, `Completed` → Delivered).

## oldPhoneRequests/{id}
`id, customerId, customerName, phone, brand, model, storage, condition, expectedPrice, estimatedQuote (text),
minQuote, maxQuote (int rupees, 0 = no numeric valuation), requestType ('SELL'), address, status, createdAt, updatedAt`
Statuses: `New Request, Inspection, Offer Sent, Accepted, Rejected, Purchased`
(initial `Submitted` / `Valuation Generated` / `Pending` all mean *New Request*; legacy `Inspecting` → Inspection,
`Valuation Accepted` → Accepted, `Completed` → Purchased).

## settings/repair_pricing
`services[]: { id, title, estimatedStartingPrice (int), turnaroundTime, description, iconType }`, `pricesById: { [id]: int }`, `updatedAt`.
* Canonical service IDs: `rep-1` (Display Replacement), `rep-2` (Battery Replacement), `rep-3` (Charging Problem),
  `rep-4` (Speaker / Mic Problem), `rep-5` (Software Problem & Flashing), `rep-6` (Water Damage Recovery).
* Public read (`allow read: if true`), admin-only write (`allow write: if isAdmin()`). Both platforms merge Firestore values onto default prices as fallback.

## settings/shop_info
`shopName, phoneNumber, whatsappNumber, email, address, pincode, landmark, openingTime, closingTime, openingHours,
services[], googleMapsUrl, pickupAvailable, homeDeliveryAvailable, deliveryCharge (int), freeDeliveryThreshold (int),
upiEnabled, codEnabled, onlinePaymentEnabled, updatedAt`
* Public read (`allow read: if true`), admin-only write (`allow write: if isAdmin()`). Both Web and Android subscribe via real-time `onSnapshot` / `addSnapshotListener`.


