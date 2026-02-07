# Financial Tools Implementation

Implement the `FinancialTools` component in `DebugTools.js` to allow admins to adjust club finances.

**Requirements:**

1.  **Club Selection:** Add a dropdown or search to select a club.
2.  **Adjust Balance:** Add an input field for the amount to add (positive) or remove (negative).
3.  **Transaction Type:** Optional selector for transaction type (e.g., "Correction", "Bonus").
4.  **Submit:** Add a "Process Transaction" button that calls `POST /api/admin/debug/finances/adjust`.
    *   Payload structure: `AdjustFinancesRequest` (check backend DTO for fields).

**Files to Modify:**
*   `fm-admin/src/pages/DebugTools.js` (Implement `FinancialTools` component).
*   `fm-admin/src/services/api.js` (Ensure `adjustFinances` is correctly implemented).

**Verification:**
*   Verify that you can add funds to a club and the change is reflected.
