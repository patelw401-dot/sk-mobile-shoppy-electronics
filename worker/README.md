# Cloudflare backend development
This branch is not production ready. The health endpoint reports ready:false and all business operations fail closed.
Before rollout: implement Firebase ID token signature and claims validation, restricted server credentials, Firestore REST transactions with idempotency and stock validation, cancellation authorization, and tests. Assess Storage availability separately. Never commit private keys. Do not switch production rules or clients until staging tests pass.
