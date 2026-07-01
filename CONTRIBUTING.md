# Contributing to konfigyr-mail

## Building

Requires JDK 21 and a network connection for the initial dependency download.

```bash
./gradlew build
```

This compiles all modules, runs Checkstyle, executes the test suite, and generates JaCoCo coverage reports under each module's `build/reports/jacoco/`.

## Running tests

```bash
./gradlew check
```

The SMTP integration tests use [GreenMail](https://greenmail-mail-test.github.io/greenmail/) and start an in-process SMTP server on port 2500 — no external mail server required.

## Code style

Checkstyle is enforced on every build using the configuration in `config/checkstyle/`. The rules follow standard Java conventions with a few project-specific additions:

- Javadoc is required on all public types and methods in production code; test classes are exempt.
- `@author` and `@since` tags are required on every type.

To check style independently:

```bash
./gradlew checkstyleMain
```

## Branch and PR workflow

1. Fork the repository and create a feature branch from `main`.
2. Keep commits focused — one logical change per commit.
3. Open a pull request against `main`. The PR checks matrix runs on JDK 21 and 25.
4. All checks must pass and at least one maintainer review is required before merging.

## Reporting bugs

Please open an issue at <https://github.com/konfigyr/konfigyr-mail/issues> with a minimal reproducer. For security vulnerabilities, follow the process in [SECURITY.md](SECURITY.md) instead.
