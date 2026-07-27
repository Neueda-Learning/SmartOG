# SmartOG

This project includes a **presentation-ready demo dataset** so the team can tell a clear story during live demos.

## Demo data file

- `src/test/resources/data-demo-h2.sql`

## What the demo dataset covers

The file seeds around 10 payments and linked status-history records with mixed outcomes:

- Happy path payments (`CREATED -> VALIDATED -> SENT -> COMPLETED`)
- In-progress payments (`CREATED`, `VALIDATED`, `SENT`)
- Failure cases with explicit error details:
  - `INVALID_ACCOUNT`
  - `INSUFFICIENT_FUNDS`
  - `INVALID_CURRENCY`
  - `NETWORK_ERROR`
  - `PROCESSING_ERROR`

## How to load it for demo/testing

Option A (H2 console):

```sql
RUNSCRIPT FROM 'src/test/resources/data-demo-h2.sql';
```

Option B (Spring SQL init in test profile):

- Add this line under `spring.sql.init` in `src/test/resources/application.yml` when needed:
  - `data-locations: classpath:data-demo-h2.sql`

## Presentation reminder (speaker notes)

When presenting, mention these points:

1. We intentionally seeded multiple lifecycle outcomes, not only happy path.
2. Every payment has status transitions recorded in `payment_status_history`.
3. Error-code examples show how failures can be tracked and explained clearly.
