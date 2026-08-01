# Transit Trip Processor

Transit Trip Processor is a command-line Spring Boot application intended to convert transit tap events from a CSV file into passenger trip records in another CSV file.

> **Project status:** Early development. The CLI currently validates and prints the input and output paths; trip matching, fare calculation, and CSV file processing are still to be implemented.

## Requirements

- Java 25
- No separate Gradle installation is required; the Gradle Wrapper is included.

## Getting started

Clone the repository and verify the project:

```bash
./gradlew test
```

Run the application with input and output file paths:

```bash
./gradlew bootRun --args='generate-trips --input=input.csv --output=output.csv'
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

Both arguments are required:

| Argument | Description |
| --- | --- |
| `--input` | Path to the CSV file containing tap events |
| `--output` | Path where the generated trip CSV will be written |

Example fixture files are available in [`src/test/resources`](src/test/resources).

## Input format

The expected input is a CSV file with one tap event per row:

```csv
ID, DateTimeUTC, TapType, StopId, CompanyId, BusID, PAN
1, 22-01-2023 13:00:00, ON, Stop1, Company1, Bus37, 5500005555555559
2, 22-01-2023 13:05:00, OFF, Stop2, Company1, Bus37, 5500005555555559
```

Observed fields:

| Field | Description |
| --- | --- |
| `ID` | Unique tap-event identifier |
| `DateTimeUTC` | UTC event timestamp in `dd-MM-yyyy HH:mm:ss` format |
| `TapType` | Tap direction, currently shown as `ON` or `OFF` |
| `StopId` | Transit stop identifier |
| `CompanyId` | Transit company identifier |
| `BusID` | Bus identifier |
| `PAN` | Payment card identifier |

## Output format

The intended output contains one row per processed trip:

```csv
Started, Finished, DurationSecs, FromStopId, ToStopId, ChargeAmount, CompanyId, BusID, PAN, Status
22-01-2023 13:00:00, 22-01-2023 13:05:00, 300, Stop1, Stop2, $3.25, Company1, Bus37, 5500005555555559, COMPLETED
```

The supported trip statuses and their precise matching rules are still to be defined.

## Build and test

Run the test suite:

```bash
./gradlew test
```

Build an executable Spring Boot JAR:

```bash
./gradlew bootJar
```

Then run it with:

```bash
java -jar build/libs/transit-trip-processor-0.0.1-SNAPSHOT.jar \
  --input=path/to/input.csv \
  --output=path/to/output.csv
```

## Configuration

The application currently has no project-specific runtime configuration beyond its Spring application name. Add configuration details here as they are introduced.

## Business rules

TODO: Document the following once confirmed:

- How `ON` and `OFF` taps are matched into trips
- Fare amounts between stops
- Handling for incomplete or cancelled trips
- Ordering and validation rules for input events
- Treatment and protection of payment card data

