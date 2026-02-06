# Bug Report: Frontend Test Environment Incompatibility

## Description
Frontend tests fail to run due to an incompatibility between `react-scripts` v5 (which uses Jest 27) and newer React 19 dependencies.
Specifically, `Login.test.js` fails with: `Cannot find module 'react-router-dom'`.
This is a known issue where Jest 27 has trouble resolving exports from newer packages or working with the React 19 test utilities.

## Location
`fm-web/package.json` (dependencies)

## Expected Behavior
Running `pnpm test` should execute the test suite without crashing on module resolution.

## Proposed Solution
There are two potential paths:
1. **Downgrade React:** Downgrade to React 18 if React 19 features are not explicitly needed yet (React 19 is very new).
2. **Upgrade Test Environment:** Eject from `react-scripts` or override the Jest configuration to use a newer Jest version (29+) that supports current module resolution standards. Alternatively, use a different test runner like Vitest.
   - A temporary fix might involve adding `axios` and `react-router-dom` to `transformIgnorePatterns` in `package.json` if possible, but `react-scripts` limits configuration.
