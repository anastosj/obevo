# JDK 17 Migration Notes

## Phase 5 — Dependency upgrades (KAN-10)

Third-party libraries that broke under JDK 17 strong encapsulation or that
relied on JDK modules removed after Java 8. All versions are managed in
`obevo-dependencies/pom.xml` (or the root `pom.xml` for `eclipsecollections.version`);
`obevo-bom` only lists Obevo's own artifacts and needed no change.

| Dependency | Before | After | Reason |
|---|---|---|---|
| `org.eclipse.collections:*` | 7.0.2 | 11.1.0 | 7.x reflects into `java.util.ArrayList.elementData`; JDK 17 throws `InaccessibleObjectException` (`java.base` does not open `java.util`) |
| `org.mockito:mockito-core` | 1.8.2 | 4.11.0 | cglib-based proxying fails at class-init on JDK 17; 4.x uses Byte Buddy. Tests moved from `org.mockito.Matchers` to `org.mockito.ArgumentMatchers` |
| `junit:junit` | 4.11 | 4.13.2 | required by Mockito 4 / hamcrest alignment |
| `com.microsoft.sqlserver:mssql-jdbc` | 6.1.0.jre7 | 12.8.1.jre8 | 6.x predates JDK 9+ support |
| `javax.annotation:javax.annotation-api` | — | 1.3.2 | `javax.annotation.Resource` (used by `obevo-internal-comparer`) left the JDK with the `java.xml.ws.annotation` module |

## Encapsulation

No `--add-opens` / `--add-exports` were required in the surefire/failsafe
`argLine`; every illegal-access failure was resolved by a library upgrade.

## Build tooling

- `extra-enforcer-rules` `banDuplicateClasses` now ignores
  `META-INF/versions/*/module-info`: multi-release jars (Byte Buddy, JUnit
  transitive deps, etc.) each ship their own module descriptor.
- `NOTICE.txt` files were regenerated (`mvn notice:generate`) for the new
  dependency/license graph.

## Validation

- JDK 8: `mvn -B -T 1.0C clean verify -DskipITs -Djacoco.skip=true`
- JDK 17: same command on the JDK 17 toolchain branch (PR #32 overlay).

`RollbackScenarioTest` in `obevo-db-scenario-tests` is timing-flaky on master
(one-second audit timestamp precision) and is unrelated to these upgrades.
