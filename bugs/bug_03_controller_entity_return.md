# Bug Report: InfrastructureController Returns Entity

## Description
The `InfrastructureController` exposes the `MaintenanceRecord` Entity directly in the `getMaintenanceSchedule` method.
```java
    @GetMapping("/club/{clubId}/maintenance/schedule")
    public ResponseEntity<List<MaintenanceRecord>> getMaintenanceSchedule(@PathVariable Long clubId) {
        List<MaintenanceRecord> maintenance = infrastructureService.getMaintenanceSchedule(clubId);
        return ResponseEntity.ok(maintenance);
    }
```
This violates the architectural standard of returning Data Transfer Objects (DTOs) from controllers. Returning Entities directly couples the API to the database schema and risks exposing sensitive data or causing serialization issues.

## Location
`src/main/java/com/lollito/fm/controller/InfrastructureController.java`

## Expected Behavior
The controller should return `ResponseEntity<List<MaintenanceRecordDTO>>`.

## Proposed Solution
1. Create `MaintenanceRecordDTO`.
2. Create a Mapper (e.g., `InfrastructureMapper` or `MaintenanceMapper`) using MapStruct.
3. Update `InfrastructureController` to convert the Entity list to a DTO list before returning.
