## 2026-02-01 - [Accessibility and Semantic HTML in Login Template]
**Learning:** Legacy templates like SB Admin 2 often use links (`<a>`) for buttons and rely on placeholders instead of labels. This breaks keyboard navigation (Enter key submission) and screen reader accessibility.
**Action:** Always replace anchor-based buttons with `<button type="submit">` in forms and add `.sr-only` labels to ensure WCAG compliance while maintaining the intended visual design.
