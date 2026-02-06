## 2025-02-18 - Loading States & Accessibility in Auth Flows
**Learning:** Users often click submit buttons multiple times if there's no immediate visual feedback, leading to frustration or API errors. Also, form inputs without programmatic association (via `htmlFor` and `id`) are invisible to many screen reader users.
**Action:** Always wrap async form submissions with an `isLoading` state that disables the submit button and provides text or visual feedback (e.g., "Signing in..."). Ensure every `label` has a `htmlFor` attribute matching the input's `id`.
