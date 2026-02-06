# Bug Report: Missing @JsonIgnore on Entity Relationships

## Description
The `MaintenanceRecord` entity has a `@ManyToOne` relationship with `Club` that is missing the `@JsonIgnore` annotation.
```java
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    @ToString.Exclude
    private Club club;
```
When `MaintenanceRecord` is serialized (as currently happens in `InfrastructureController`), it will attempt to serialize the `Club` object. If `Club` has other relationships that link back or are large, this causes performance issues (N+1 queries) and potential infinite recursion (if the chain loops back).
Similar missing ignores were found in `Finance.transactions` and `User.sessions/activities/etc`.

## Location
`src/main/java/com/lollito/fm/model/MaintenanceRecord.java`

## Expected Behavior
Child entities should not automatically serialize their parent entities to prevent recursion and massive payloads.

## Proposed Solution
Add `@JsonIgnore` to the `club` field in `MaintenanceRecord`.
```java
    @JsonIgnore
    private Club club;
```
(And similarly for other bidirectional relationships identified).
