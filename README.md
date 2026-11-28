# SK Mobile Shoppy & Electronics

React/Vite storefront and admin panel, Kotlin Android app, and Firebase backend.

## Local checks

Use Node.js 22, Java 21, Android SDK 36, and Gradle 9.3.1. The Android module uses the store's Firebase configuration at `app/google-services.json`. Keep signing material and local Firebase configuration outside version control.

```sh
npm ci --prefix web
npm run build --prefix web
npm ci
npm ci --prefix functions
npx --yes firebase-tools@15.37.0 emulators:exec --only firestore --project demo-no-project 'npm test && node --test functions/orders.test.js'
gradle :app:testDebugUnitTest
```

## Checkout release

The callable Firebase backend creates checkout orders from live catalog prices. It reserves stock at order creation and restores stock on cancellation. Checkout offers payment on delivery or at pickup. Staff record payment separately in the web admin panel after receiving it.

Deploy `createStoreOrder` and `cancelStoreOrder` to the Firebase environment before releasing the matching web and Android clients. They use the named Firestore database configured in `firebase.json`. Firebase Functions deployment requires a project with Functions enabled and an authorized deploy identity. Check that each sellable product has a positive integer price and an accurate integer stock value. For existing reels that should remain public, set `isPublished` to true before the rules change.

Deploy the updated Firestore and Storage rules as part of the coordinated checkout cutover. Older Android APKs and older storefront builds write orders directly and cannot place or cancel orders after the rules are switched. Use a staging Firebase project to exercise the checkout, then coordinate the production cutover and client update. Avoid rolling back only the rules after stock-reserved orders exist, because older client cancellation does not restore stock.

Legacy order and payment records remain in place. Verify historical payment records against the shop's receipts before relying on them for fulfillment.

The signed APK and version metadata currently publish through the website release workflow. Moving the APK to another distribution origin requires coordinating the web download and Android update paths.
