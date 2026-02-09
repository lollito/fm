## 2025-02-18 - Loading States & Accessibility in Auth Flows
**Learning:** Users often click submit buttons multiple times if there's no immediate visual feedback, leading to frustration or API errors. Also, form inputs without programmatic association (via `htmlFor` and `id`) are invisible to many screen reader users.
**Action:** Always wrap async form submissions with an `isLoading` state that disables the submit button and provides text or visual feedback (e.g., "Signing in..."). Ensure every `label` has a `htmlFor` attribute matching the input's `id`.

## 2025-02-18 - Loading States in Destructive Actions
**Learning:** Confirmation modals often close immediately upon confirmation, leaving users unsure if the action (e.g., delete) actually succeeded, especially on slow networks.
**Action:** Enhance all confirmation modals to handle async actions by keeping the modal open, showing a spinner, and disabling buttons until the promise resolves. This prevents double-submissions and provides reassurance.

## 2025-02-19 - Missing Utility Classes
**Learning:** Projects ported from frameworks like Bootstrap might leave behind class names (e.g., `spinner-border`) without the underlying CSS, leading to broken UI elements that are hard to spot visually (invisible spinners).
**Action:** When auditing UI, check if "standard" utility classes are actually defined. Re-implementing them in the global theme can fix widespread issues instantly.
