# UML Class Diagrams

Source files use [diagrams.net](https://app.diagrams.net) (Draw.io). Open a `.drawio` file there if you need PNG/PDF for hand-in.

## 1. Data Storage System

[Data Storage System (drawio)](data_storage_system.drawio) · [PNG](data_storage_system.png) *(export from drawio if missing)*

### Design Rationale

The data storage subsystem is built around `DataStorage` as the central class. It holds a `Map<Integer, Patient>` to organise patient records by ID, and exposes methods like `addPatientData()`, `getRecords()`, and `getAllPatients()` for the rest of the system to use.

The storage hierarchy has three layers: `DataStorage` at the top manages all patients, `Patient` groups all records belonging to one individual, and `PatientRecord` represents a single measurement — its type (e.g. blood pressure), value, and timestamp. This reflects a natural one-to-many ownership: one storage holds many patients, and one patient holds many records.

The `DataReader` interface separates the concern of reading data from the storage itself. Any class that supplies patient data — whether from a file, TCP stream, or WebSocket — implements `readData(DataStorage)`. `FileDataReader` is the concrete implementation that handles file-based input, using `parseLine()` to process each line from the simulator's output directory and load it into storage. This keeps the storage layer unaware of where data physically comes from.

`DataRetriever` is a separate query class for retrieving records on behalf of medical staff. Separating read queries (`DataRetriever`) from data ingestion (`DataReader`) keeps each class focused on a single responsibility, and makes it easier to extend either side independently in the future.

---

## 2. Patient Identification System

[Patient Identification System (drawio)](patient_identification_system.drawio) · [PNG](patient_identification_system.png) *(export from drawio if missing)*

### Design Rationale

The patient identification subsystem is responsible for verifying that incoming data is correctly linked to a real hospital patient before it enters the system. `IdentityManager` is the central class that coordinates this process. It holds references to both `PatientIdentifier` and `DataStorage`, and exposes `verifyAndLink()` to validate a record, `handleMismatch()` for cases where no match is found, and `getAuditLog()` to keep a trace of identity checks.

`PatientIdentifier` handles the actual matching logic. It maintains a `Map<Integer, HospitalPatient>` of known hospital records and provides `matchPatient()` to look up a patient by ID, `isValidId()` for a quick pre-check, and `registerPatient()` to add new patients. Keeping this logic in a separate class means `IdentityManager` stays focused on orchestration rather than getting involved in lookup details.

`HospitalPatient` models the real hospital record for a patient, containing fields like name, date of birth, and medical history. This is intentionally kept separate from the `Patient` class in the data management package — `HospitalPatient` represents verified identity information from the hospital's database, while `Patient` is just a runtime container for simulator measurements.

`DataStorage` and `PatientRecord` appear in grey in the diagram to indicate they are external to this subsystem — they are defined in the Data Storage subsystem and included here only to show that `IdentityManager` depends on them when routing validated data into storage.

---

## 3. Alert Generation System

[Alert Generation System (drawio)](alert_generation_system.drawio)

### Design Rationale

The alert subsystem turns streamed vitals into notifications clinicians can trust and respond to. `AlertGenerator` gathers recent `PatientRecord` readings from `DataStorage` (or accepts records already gated by an identity check), walks the active rules for that patient, and materializes an `Alert` only when a breach is real under the configured bounds. Centralizing the comparison makes audits and regression tests far simpler than duplicating logic at every consumer.

Per-patient policy lives in `PatientAlertProfile`, which owns an ordered list of `AlertRule` objects—metric name, numeric window, optional severity label, and helper text for what “out of range” means clinically. When cardiology adds a vital or tightens post-operative limits, teams change profiles and rules without rewriting dispatch logic. The generator can later add rate limiting or multifactor flags while `AlertRule` stays the vocabulary for thresholds.

`AlertManager` is the outbound face: it accepts completed `Alert` instances, routes them to the hospital’s channels (pager, in-app queue, audit log), and records acknowledgements when policy requires proof of delivery. Separating evaluation from routing avoids blocking ingestion on slow notification APIs and clarifies where retries belong.

`Alert` itself is deliberately tiny: a stable patient identifier, a short human-readable condition string, and an epoch timestamp. Dashboards, legal discovery, and shift handover all need that triple without dragging entire rule graphs into every row.

Grey classes—`DataStorage`, `PatientRecord`, `IdentityManager`—belong elsewhere but appear here because alert quality depends on fresh data, correct IDs, and verified identities before anyone is paged. The diagram makes gaps such as alerting on unmatched simulator IDs visible in architecture review.

---

## 4. Data Access Layer (ECG / simulator ingress)

[Data Access Layer (drawio)](data_access_layer.drawio)

### Design Rationale

This layer is the intake boundary between the cardio simulator’s output and the rest of the CHMS. The same logical stream might leave the generator as TCP frames, WebSocket messages, or lines in rolling logs, yet storage and alerting should see one predictable shape afterward. `DataListener` captures that contract: `start(DataStorage)` attaches the transport, `stop()` tears it down cleanly, and `onRawMessage(String)` gives the adapter one payload chunk so upper layers never import socket or NIO APIs.

Concrete listeners—`TCPDataListener`, `WebSocketDataListener`, and `FileDataListener`—hold only what their medium needs (port, URI, watched directory) and push each decoded line to a shared `DataSourceAdapter`. They never write to `DataStorage` directly, so reconnects, partial frames, and protocol quirks stay next to the socket or file watcher instead of leaking into persistence.

`DataParser` turns raw strings into domain objects: it parses simulator rows into a `PatientRecord`, checks basic sanity (numeric fields, plausible timestamps), and rejects corrupt lines. One parser implementation means one place to update when the generator’s format changes.

`DataSourceAdapter` composes parser plus storage: `ingestRaw(line, storage)` normalizes errors, logs for operators when needed, and invokes the same `addPatientData` entry point batch loaders use. Replay jobs, live feeds, and offline file imports therefore land identical rows in storage.

`DataStorage` is grey because the canonical repository lives in the storage subsystem; this diagram only shows write-through. Rate limiting, reconnect backoff, and credentials stay inside each listener so the core stays thin and testable while operators can roll out new network paths without refactoring the adapter stack.

