# UML Class Diagrams

## 1. Data Storage System

[Data Storage System](data_storage_system.png)

### Design Rationale

The data storage subsystem is built around `DataStorage` as the central class. It holds a `Map<Integer, Patient>` to organise patient records by ID, and exposes methods like `addPatientData()`, `getRecords()`, and `getAllPatients()` for the rest of the system to use.

The storage hierarchy has three layers: `DataStorage` at the top manages all patients, `Patient` groups all records belonging to one individual, and `PatientRecord` represents a single measurement — its type (e.g. blood pressure), value, and timestamp. This reflects a natural one-to-many ownership: one storage holds many patients, and one patient holds many records.

The `DataReader` interface separates the concern of reading data from the storage itself. Any class that supplies patient data — whether from a file, TCP stream, or WebSocket — implements `readData(DataStorage)`. `FileDataReader` is the concrete implementation that handles file-based input, using `parseLine()` to process each line from the simulator's output directory and load it into storage. This keeps the storage layer unaware of where data physically comes from.

`DataRetriever` is a separate query class for retrieving records on behalf of medical staff. Separating read queries (`DataRetriever`) from data ingestion (`DataReader`) keeps each class focused on a single responsibility, and makes it easier to extend either side independently in the future.

---

## 2. Patient Identification System

[Patient Identification System](patient_identification_system.png)

### Design Rationale

The patient identification subsystem is responsible for verifying that incoming data is correctly linked to a real hospital patient before it enters the system. `IdentityManager` is the central class that coordinates this process. It holds references to both `PatientIdentifier` and `DataStorage`, and exposes `verifyAndLink()` to validate a record, `handleMismatch()` for cases where no match is found, and `getAuditLog()` to keep a trace of identity checks.

`PatientIdentifier` handles the actual matching logic. It maintains a `Map<Integer, HospitalPatient>` of known hospital records and provides `matchPatient()` to look up a patient by ID, `isValidId()` for a quick pre-check, and `registerPatient()` to add new patients. Keeping this logic in a separate class means `IdentityManager` stays focused on orchestration rather than getting involved in lookup details.

`HospitalPatient` models the real hospital record for a patient, containing fields like name, date of birth, and medical history. This is intentionally kept separate from the `Patient` class in the data management package — `HospitalPatient` represents verified identity information from the hospital's database, while `Patient` is just a runtime container for simulator measurements.

`DataStorage` and `PatientRecord` appear in grey in the diagram to indicate they are external to this subsystem — they are defined in the Data Storage subsystem and included here only to show that `IdentityManager` depends on them when routing validated data into storage.
