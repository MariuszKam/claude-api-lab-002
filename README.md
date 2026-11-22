# Claude API Lab 002

Learning project for **Claude Certified Developer – Foundations** — Week 1, **Applications and Integration**.

This repository will grow incrementally through small Trello tickets. We use the **official Anthropic Java SDK** rather than hand-written Messages API DTOs and HTTP transport.

## Stack

- Java 25
- Gradle (wrapper 9.6.0)
- Anthropic Java SDK `com.anthropic:anthropic-java:3.12.0`
- JUnit Jupiter for future exercises

## Run locally

On Windows:

```powershell
.\gradlew.bat build
.\gradlew.bat test
.\gradlew.bat run
```

No API key is needed for the Gradle build. **No request to Claude is implemented yet.** Future lab tickets will introduce SDK request builders, local HTTP mocks, response processing, and eventually optional live calls.

Never commit API keys or secrets. LAB 001 remains a separate record of the hand-written HTTP approach.

## Progress

- [x] 1.7-01 — create separate public repository
- [x] 1.7-02 — initialize Java/Gradle with Anthropic SDK (build and dependency smoke test passed)
- [ ] 1.7-03 — secure API key configuration
- [ ] 1.7-04 — initialize Anthropic SDK client
- [ ] 1.1-01 — build first Messages API request

Tracking: [Trello CCDV-F](https://trello.com/b/9QnhiOcN/ccdv-f)

Official SDK: https://github.com/anthropics/anthropic-sdk-java
