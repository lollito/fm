# Server Management UI Enhancement

The goal is to enhance the `ServerManagement.js` page in `fm-admin` to provide complete control over game servers.

**Requirements:**

1.  **Force Next Day:** Add a button "Force Next Day" next to each server in the list (or a main action button if applicable to the current context).
    *   This button should trigger a POST request to `/api/server/next`.
    *   Show a loading state while processing.
    *   Refresh the server list upon success.

2.  **Delete Server:** Add a "Delete" button for each server in the list.
    *   This should trigger a DELETE request to `/api/server/` with query param `serverId={id}`.
    *   Add a confirmation dialog before deletion.
    *   Refresh the list upon success.

3.  **Maintenance Mode (Optional):** If the backend supports it, add a toggle for maintenance mode. (Currently, backend support is not confirmed, so focus on 1 and 2).

**Files to Modify:**
*   `fm-admin/src/pages/ServerManagement.js`
*   `fm-admin/src/services/api.js` (Add `forceNextDay` and `deleteServer` functions).

**Verification:**
*   Ensure the buttons appear and trigger the correct API calls.
*   Ensure the UI updates correctly after actions.
