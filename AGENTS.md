# AGENTS.md

## Cursor Cloud specific instructions

This repository is a JavaFX + Spring Boot desktop application (a matchmaking / competition generator, UI text in Portuguese). The Maven project lives in the `matchmaking/` subdirectory, not the repo root.

- Java: project targets Java 17; the VM has JDK 21, which builds and runs it fine.
- Build/test/run all use the Maven wrapper from inside `matchmaking/` (e.g. `cd matchmaking`). Build + test: `./mvnw -B test`. Lint: there is no separate linter configured; compilation (`./mvnw -B compile`) is the only static check.

### Running the GUI (non-obvious)
- This is a JavaFX desktop app. The entry point (`MatchmakingApplication`) launches JavaFX, which then boots Spring. It therefore needs an X display.
- A TigerVNC desktop is available at `DISPLAY=:1`. Run with: `DISPLAY=:1 ./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Dprism.order=sw -Djava.awt.headless=false"`. The `-Dprism.order=sw` forces software rendering and avoids GPU/Prism initialization issues in the headless VM.
- Even though it is a desktop app, it also starts an embedded Tomcat on port 8080 with the H2 console at `/h2-console` (`spring.h2.console.enabled=true`).

### Database
- Uses an H2 *file* database (`spring.datasource.url=jdbc:h2:file:./matchmakingdb`), so the DB file is created relative to the working directory. Running from `matchmaking/` creates `matchmaking/matchmakingdb.mv.db`; a separate seeded `matchmakingdb.mv.db` exists at the repo root. Do not commit the generated DB file.
- `ddl-auto=update` keeps data across restarts. `DataLoader` seeds the 3 game modes and 11 test players only when those tables are empty, so existing data is preserved.

### Encoding gotcha
- `matchmaking/src/main/resources/application.properties` must be UTF-8. It was originally ISO-8859 (accented Portuguese comments) which broke the Maven resources filtering step with `MalformedInputException` (Spring Boot parent filters resources as UTF-8). It has been converted to UTF-8; keep it UTF-8.
