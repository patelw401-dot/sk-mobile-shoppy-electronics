const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const ADMIN_UID = "admin_999";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
    // Pre-populate an admin user record in /admins/admin_999 with security context bypass
    await testEnv.withSecurityRulesDisabled(async (context) => {
      await context.firestore().collection("admins").doc(ADMIN_UID).set({
        uid: ADMIN_UID,
        email: "admin@skmobile.com",
        role: "admin",
        isAdmin: true,
        active: true,
      });
      // Pre-populate a product
      await context.firestore().collection("products").doc("prod_1").set({
        id: "prod_1",
        name: "iPhone 15",
        price: 79999,
        category: "smartphones",
      });
      // Pre-populate an order for Alice
      await context.firestore().collection("orders").doc("order_alice").set({
        id: "order_alice",
        customerId: ALICE_UID,
        customerName: "Alice",
        phone: "9823109313",
        address: "Udgir",
        items: [{ id: "item_1", name: "iPhone 15", price: 79999, quantity: 1 }],
        total: 79999,
        orderStatus: "Order Confirmed",
        paymentStatus: "Pending",
      });
    });
  }
});

test("Public unauthenticated access: can read products, categories, settings; cannot write", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(unauthDb.collection("products").doc("prod_1").get());
  await assertFails(unauthDb.collection("products").doc("prod_2").set({ name: "Fake Phone" }));
  await assertFails(unauthDb.collection("orders").doc("order_alice").get());
  await assertFails(unauthDb.collection("orders").doc("order_unauth").set({ customerName: "Hacker" }));
});

test("Customer cannot create order for another user", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  // Alice tries to create order with Bob's customerId
  await assertFails(
    aliceDb.collection("orders").doc("order_for_bob").set({
      id: "order_for_bob",
      customerId: BOB_UID, // Mismatched customerId!
      customerName: "Bob",
      phone: "9823109313",
      address: "Udgir Market",
      items: [{ id: "p1", name: "Case", price: 499, quantity: 1 }],
      total: 499,
      orderStatus: "Pending",
      paymentStatus: "Pending",
    })
  );
});

test("Customer can create order associated with their own Firebase UID", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("orders").doc("order_alice_new").set({
      id: "order_alice_new",
      customerId: ALICE_UID,
      customerName: "Alice",
      phone: "9823109313",
      address: "Udgir Market",
      items: [{ id: "p1", name: "Case", price: 499, quantity: 1 }],
      total: 499,
      orderStatus: "Order Confirmed",
      paymentStatus: "Pending",
    })
  );
});

test("Customer cannot create order with unpermitted initial status or missing required fields", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  // Trying to set initial status to Delivered
  await assertFails(
    aliceDb.collection("orders").doc("order_invalid_status").set({
      id: "order_invalid_status",
      customerId: ALICE_UID,
      customerName: "Alice",
      phone: "9823109313",
      address: "Udgir Market",
      items: [{ id: "p1", name: "Case", price: 499, quantity: 1 }],
      total: 499,
      orderStatus: "Delivered", // Invalid initial status!
      paymentStatus: "Pending",
    })
  );

  // Missing address or items
  await assertFails(
    aliceDb.collection("orders").doc("order_missing_items").set({
      id: "order_missing_items",
      customerId: ALICE_UID,
      customerName: "Alice",
      phone: "9823109313",
      address: "",
      items: [],
      total: 499,
      orderStatus: "Pending",
      paymentStatus: "Pending",
    })
  );
});

test("Customer CANNOT update or delete their order (cannot change payment status or order fields)", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  // Alice tries to mark payment as Paid or change order status
  await assertFails(
    aliceDb.collection("orders").doc("order_alice").update({
      paymentStatus: "Paid",
    })
  );
  await assertFails(aliceDb.collection("orders").doc("order_alice").delete());
});

test("Customer can only read their own orders; other customer cannot read", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(aliceDb.collection("orders").doc("order_alice").get());
  await assertFails(bobDb.collection("orders").doc("order_alice").get());
});

test("Customer cannot create repair booking for another user", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("repairBookings").doc("rep_bob").set({
      id: "rep_bob",
      customerId: BOB_UID, // Mismatch
      customerName: "Bob",
      phone: "9823109313",
      brand: "Apple",
      model: "iPhone 13",
      problem: "Battery",
      repairStatus: "Requested",
    })
  );
});

test("Customer can create repair booking for themselves with valid fields", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("repairBookings").doc("rep_alice").set({
      id: "rep_alice",
      customerId: ALICE_UID,
      customerName: "Alice",
      phone: "9823109313",
      brand: "Apple",
      model: "iPhone 13",
      problem: "Battery",
      repairStatus: "Appointment Booked",
    })
  );
});

test("Customer CANNOT update or delete repair bookings (protected repair fields)", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("repairBookings").doc("rep_alice").update({
      repairStatus: "Completed",
      estimatedCost: "₹0",
    })
  );
  await assertFails(aliceDb.collection("repairBookings").doc("rep_alice").delete());
});

test("Customer profile: Alice can manage own profile, but cannot tamper with Bob or set isAdmin", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  // Alice updates her own profile
  await assertSucceeds(
    aliceDb.collection("customers").doc(ALICE_UID).set({
      uid: ALICE_UID,
      name: "Alice Patil",
      phoneNumber: "+919823109313",
    })
  );

  // Alice attempts privilege escalation to admin
  await assertFails(
    aliceDb.collection("customers").doc(ALICE_UID).set({
      uid: ALICE_UID,
      name: "Alice Patil",
      isAdmin: true, // Forbidden!
    })
  );

  // Alice attempts to write to Bob's customer doc
  await assertFails(
    aliceDb.collection("customers").doc(BOB_UID).set({
      uid: BOB_UID,
      name: "Fake Bob",
    })
  );

  // Alice attempts to write to /admins collection
  await assertFails(
    aliceDb.collection("admins").doc(ALICE_UID).set({
      uid: ALICE_UID,
      email: "alice@hacker.com",
      role: "admin",
      isAdmin: true,
      active: true,
    })
  );
});

test("Admin user can read and manage orders, repair bookings, and catalog", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID).firestore();
  await assertSucceeds(adminDb.collection("orders").doc("order_alice").get());
  await assertSucceeds(
    adminDb.collection("orders").doc("order_alice").update({
      orderStatus: "Delivered",
      paymentStatus: "Paid",
    })
  );
  await assertSucceeds(
    adminDb.collection("products").doc("prod_new").set({
      id: "prod_new",
      name: "Samsung Galaxy S24",
      price: 69999,
    })
  );
});

test("Google-Only Auth: Authorized Gmail allowlist grants admin; normal customer Google login denied admin", async () => {
  // Authorized Gmail allowlist user: patelw401@gmail.com
  const authorizedGoogleAdminDb = testEnv.authenticatedContext("patel_uid", { email: "patelw401@gmail.com" }).firestore();
  await assertSucceeds(authorizedGoogleAdminDb.collection("orders").doc("order_alice").get());
  await assertSucceeds(
    authorizedGoogleAdminDb.collection("products").doc("prod_patel").set({
      id: "prod_patel",
      name: "Pixel 9 Pro",
      price: 109999,
    })
  );

  // Authorized Store Owner account: skmobileshoppyandelectronics@gmail.com
  const storeOwnerDb = testEnv.authenticatedContext("owner_uid", { email: "skmobileshoppyandelectronics@gmail.com" }).firestore();
  await assertSucceeds(storeOwnerDb.collection("orders").doc("order_alice").get());
  await assertSucceeds(
    storeOwnerDb.collection("products").doc("prod_owner").set({
      id: "prod_owner",
      name: "Samsung Galaxy S24 Ultra",
      price: 129999,
    })
  );

  // Normal Customer Google user: customer@gmail.com (NOT in allowlist)
  const regularCustomerGoogleDb = testEnv.authenticatedContext("customer_uid", { email: "customer@gmail.com" }).firestore();
  // Cannot read Alice's orders
  await assertFails(regularCustomerGoogleDb.collection("orders").doc("order_alice").get());
  // Cannot write products or admin documents
  await assertFails(
    regularCustomerGoogleDb.collection("products").doc("prod_hacker").set({
      id: "prod_hacker",
      name: "Malicious Product",
      price: 1,
    })
  );
  await assertFails(
    regularCustomerGoogleDb.collection("admins").doc("customer_uid").set({
      role: "admin",
      isAdmin: true,
    })
  );
});

