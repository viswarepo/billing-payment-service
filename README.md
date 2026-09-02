# Billing & Payment Service

Subscription billing and payments for the platform, backed by Razorpay.
Java 21, Spring Boot 4.0.7, PostgreSQL.

## What it does

- **Plans** — pricing plans (amount, currency, billing cycle).
- **Subscriptions** — a customer's active subscription to a plan, tracks
  the current billing period and when the next one is due.
- **Invoices** — generated automatically by the billing cycle job for each
  subscription period.
- **Payments** — Razorpay Orders created against an invoice; verified via
  Checkout's client-side signature callback and reconciled again via
  server-to-server webhooks.
- **Refunds** — full or partial, issued against a captured payment.

## Setup

1. **Database.** Create a Postgres database and point
   `spring.datasource.*` in `application.properties` at it (or override via
   `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` env vars). Schema is
   auto-managed via `ddl-auto=update` for local dev — switch to Flyway/Liquibase
   before production.

2. **Razorpay keys.** Grab test-mode keys from
   https://dashboard.razorpay.com/#/app/keys and set:
   ```
   RAZORPAY_KEY_ID=rzp_test_...
   RAZORPAY_KEY_SECRET=...
   ```

3. **Webhook secret.** In the Razorpay dashboard under Settings → Webhooks,
   register `POST https://<your-host>/api/v1/webhooks/razorpay` (use a tool
   like `ngrok` for local testing), subscribe to at least `payment.captured`
   and `payment.failed`, and set the secret it gives you as:
   ```
   RAZORPAY_WEBHOOK_SECRET=...
   ```

4. **Run.**
   ```bash
   ./mvnw clean package -DskipTests
   java -jar target/billing-payment-service-0.0.1-SNAPSHOT.jar
   ```
   Runs on port **9093** by default, matching the `subscription-service`
   route already configured in the API gateway.

## Typical flow

```
1. POST /api/v1/plans                      create a pricing plan (once)
2. POST /api/v1/subscriptions              customer subscribes to a plan
3. POST /api/v1/subscriptions/process-billing-cycle   (or wait for the 2 AM job)
                                            generates an invoice for the period
4. POST /api/v1/payments/orders?invoiceId=..   create a Razorpay order for the invoice
5. (frontend opens Razorpay Checkout with the returned order id/key)
6. POST /api/v1/payments/verify            client posts back the signature after payment
   -- OR --
   Razorpay webhook fires payment.captured  → same result, reconciled server-side
7. POST /api/v1/payments/{id}/refund       if needed
```

## Gateway routing — heads up

The existing API gateway routes `/api/v1/plans/**` to `plan-catalog-service`
(product/feature catalog on port 9092) — a **different** kind of "plan" than
this service's billing plans. If you add this service's routes to the
gateway, avoid a path collision, e.g.:

```yaml
- id: billing-payment-service
  uri: http://localhost:9093
  predicates:
    - Path=/api/v1/customers/**,/api/v1/subscriptions/**,/api/v1/billing-plans/**,/api/v1/invoices/**,/api/v1/payments/**
```

...and rename this service's `/api/v1/plans` mapping to `/api/v1/billing-plans`
if both services sit behind the same gateway. `/api/v1/subscriptions/**` and
`/api/v1/customers/**` already match the gateway's existing
`subscription-service` route, so those work as-is.

The `/api/v1/webhooks/razorpay` endpoint should generally bypass the
gateway's JWT auth filter entirely (Razorpay isn't sending a bearer token) —
expose it directly, or add it to the gateway's auth-exempt path list.
