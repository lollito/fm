# Bug Report: Backend Test Configuration Failure

## Description
The backend tests fail with an `IllegalStateException` related to the embedded MongoDB configuration.
The error message is: `Set the de.flapdoodle.mongodb.embedded.version property or define your own IFeatureAwareVersion bean to use embedded MongoDB`.

While this property is present in the main `src/main/resources/application.properties`, it is missing from `src/test/resources/application.properties`. The test environment prioritizes the test resource file, leading to the missing configuration.

## Location
`src/test/resources/application.properties`

## Expected Behavior
Tests should run successfully using the embedded MongoDB version specified in the project requirements (4.0.25).

## Proposed Solution
Add the following line to `src/test/resources/application.properties`:
```properties
de.flapdoodle.mongodb.embedded.version=4.0.25
```
