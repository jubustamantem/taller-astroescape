# Continuous Integration

This repository runs automated checks on every push and pull request to `main` or `ci-cd` via GitHub Actions (see [`.github/workflows/ci.yml`](.github/workflows/ci.yml)).

Two jobs run in parallel:

| Job | What it runs |
| --- | --- |
| Java | `mvn -B test` (JUnit 5 domain tests) |
| Python | `python -m pytest` under `python/` |

Tests cover domain rules only (no MySQL, Hibernate, or SQLAlchemy). That keeps CI fast and independent of a local database.

## Run tests locally

Java (JDK 17 + Maven), from the repo root:

```bash
mvn -B test
```

Python, from `python/`:

```bash
pip install -r requirements.txt -r requirements-dev.txt
python -m pytest
```

## Classroom demo notes

1. Open a PR and show both checks green.
2. Break one assertion on purpose, push, and show the red check.
3. Revert the break so `main` stays healthy.

CI here means “build and test on every change.” This lab has no deploy target, so Continuous Delivery / Deployment is out of scope.
