# Transit Trip Processor

Transit Trip Processor is a Spring Boot command-line application that reads passenger tap events from a CSV file, matches tap-ons with tap-offs, calculates the fare for each journey, and writes the resulting trips to another CSV file.

## Requirements

- Java 25
- No separate Gradle installation is required; the Gradle Wrapper is included.

### Fare table

Fares are the same in both travel directions.

| From   | To     | Fare  |
| -------| ------ | ----: |
| Stop 1 | Stop 2 | $3.25 |
| Stop 2 | Stop 3 | $5.50 |
| Stop 1 | Stop 3 | $7.30 |

```text
Stop 1 <---- $3.25 ----> Stop 2 <---- $5.50 ----> Stop 3
   ^                                                 |
   +--------------------- $7.30 ---------------------+
```

### Trip types

- `COMPLETED`: the passenger taps on and later taps off at a different stop. The fare is taken from the table above.
- `INCOMPLETE`: the passenger taps on but has no matching tap-off. The passenger is charged the highest possible fare from the starting stop. The maximum incomplete-trip fares are therefore:

    | Starting stop | Maximum fare |
    | --------------| -----------: |
    | Stop 1        | $7.30        |
    | Stop 2        | $5.50        |
    | Stop 3        | $7.30        |
- `CANCELLED`: the passenger taps on and off at the same stop. The charge is `$0.00`.


## Run the application

Run all commands below from the repository root.

### 1. Prepare the input CSV

Create `input.csv` in the repository root. The header must use this exact
column order:

```csv
ID, DateTimeUTC, TapType, StopId, CompanyId, BusID, PAN
```

Example input:

```csv
ID, DateTimeUTC, TapType, StopId, CompanyId, BusID, PAN
1, 22-01-2023 13:00:00, ON, Stop1, Company1, Bus37, 5500005555555559
2, 22-01-2023 13:05:00, OFF, Stop2, Company1, Bus37, 5500005555555559
3, 22-01-2023 09:20:00, ON, Stop3, Company1, Bus36, 4111111111111111
4, 23-01-2023 08:00:00, ON, Stop1, Company1, Bus37, 4111111111111111
5, 23-01-2023 08:02:00, OFF, Stop1, Company1, Bus37, 4111111111111111
6, 24-01-2023 16:30:00, OFF, Stop2, Company1, Bus37, 5500005555555559
```

Input columns:

| Column        | Description                                            |
| --------------| ------------------------------------------------------ |
| `ID`          | Unique tap-event ID; also used to break timestamp ties |
| `DateTimeUTC` | Event time in `dd-MM-yyyy HH:mm:ss` format             |
| `TapType`     | Enum: `ON` or `OFF`                                    |
| `StopId`      | Enum: `Stop1`, `Stop2`, or `Stop3`                     |
| `CompanyId`   | Transit company identifier                             |
| `BusID`       | Bus identifier                                         |
| `PAN`         | Payment-card identifier used to match taps             |

### 2. Run the CLI command

On macOS or Linux:

```bash
./gradlew bootRun --args='generate-trips --input=input.csv --output=output.csv'
```

On Windows:

```bat
gradlew.bat bootRun --args="generate-trips --input=input.csv --output=output.csv"
```

Both CLI options are required and must appear exactly once:

| Option     | Description                                         |
| -----------| --------------------------------------------------- |
| `--input`  | File-system path to the tap CSV                     |
| `--output` | File-system path where the trip CSV will be written |

### 3. Check the generated output

With the command above, the generated file is:

```text
<repository-root>/output.csv
```

For the example input, the expected content is:

```csv
Started, Finished, DurationSecs, FromStopId, ToStopId, ChargeAmount, CompanyId, BusID, PAN, Status
22-01-2023 09:20:00, , 0, Stop3, , $7.30, Company1, Bus36, 4111111111111111, INCOMPLETE
22-01-2023 13:00:00, 22-01-2023 13:05:00, 300, Stop1, Stop2, $3.25, Company1, Bus37, 5500005555555559, COMPLETED
23-01-2023 08:00:00, 23-01-2023 08:02:00, 120, Stop1, Stop1, $0.00, Company1, Bus37, 4111111111111111, CANCELLED
```

The same example input and expected result are used by the integration test at
`src/test/resources/input.csv` and `src/test/resources/output.csv`.

To write the result somewhere else, change `--output`. The destination parent
directory must already exist.

### Run the packaged JAR

Build the executable JAR:

```bash
./gradlew bootJar
```

Run it:

```bash
java -jar build/libs/transit-trip-processor-0.0.1-SNAPSHOT.jar \
  generate-trips \
  --input=/absolute/path/to/input.csv \
  --output=/absolute/path/to/output.csv
```

The application reads normal file-system paths. A CSV bundled inside a packaged
JAR cannot be passed as `classpath:input.csv`; provide an external file path.

## Matching rules and assumptions

- Input rows are processed by `DateTimeUTC`, with `ID` used as a tie-breaker.
- An `ON` and `OFF` match when their PAN, company ID, and bus ID are equal.
- A second matching `ON` makes the previous unmatched `ON` incomplete and starts
  a new trip.
- An unmatched `OFF` is ignored because its starting stop, start time, duration,
  and charge cannot be determined.
- Blank CSV lines are ignored. A non-blank malformed row stops processing and
  reports its line number.
- Trips are written in start-time order. PAN, company ID, and bus ID are used as
  deterministic tie-breakers.
- The input is assumed to fit in memory and to contain only the three supported
  stops.

## Project structure

Including the project structure is useful for this exercise because it makes the
separation between CLI input, application flow, business logic, domain data, and
CSV I/O explicit.

```text
src/main/resources/
└── application.yaml           Spring Boot configuration

src/test/resources/
├── input.csv                  integration-test input
└── output.csv                 expected integration-test output

input.csv                      runtime input, ignored by Git
output.csv                     runtime output, ignored by Git
```

```text
src/main/java/com/example/transittripprocessor
├── TransitTripProcessorApplication.java    Spring Boot entry point
├── cli
│   ├── CliCommand.java                     CLI command contract
│   ├── CliApplicationRunner.java           command dispatcher
│   └── command
│       └── GenerateTripsFromTapsCommand.java
│                                             validates CLI arguments
├── csv
│   ├── TapCsvReader.java                   reads Tap records
│   └── TripCsvWriter.java                  writes Trip records
├── model                                   domain data and value objects
│   ├── StopId.java
│   ├── StopPair.java
│   ├── Tap.java
│   ├── TapType.java
│   ├── Trip.java
│   ├── TripKey.java
│   └── TripStatus.java
└── service
    ├── FareCalculator.java                 calculates fares
    ├── TapToTripMatcher.java               matches taps into trips
    └── TapToTripProcessor.java             read-match-write workflow
```

Processing flow:

```text
GenerateTripsFromTapsCommand
             |
             v
    TapToTripProcessor
       /      |      \
      v       v       v
TapCsvReader  TapToTripMatcher  TripCsvWriter
                    |
                    v
              FareCalculator
```

## Test the application

Run the complete test harness:

```bash
./gradlew test
```

The test suite includes unit tests for CLI dispatch and argument validation,
CSV parsing and writing, stop and fare rules, tap matching, workflow orchestration,
and an integration test covering the complete CLI-to-output-file flow.
