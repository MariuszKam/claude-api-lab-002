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

No API key is needed for the Gradle build or tests. The SDK test sends a request only to a local HTTP mock. `Main` prints instructions and does not read credentials or make HTTP requests.

Never commit API keys or secrets. LAB 001 remains a separate record of the hand-written HTTP approach.

## Progress

- [x] 1.7-01 — create separate public repository
- [x] 1.7-02 — initialize Java/Gradle with Anthropic SDK (build and dependency smoke test passed)
- [ ] 1.7-03 — secure API key configuration
- [ ] 1.7-04 — initialize Anthropic SDK client
- [ ] 1.1-01 — build first Messages API request

Tracking: [Trello CCDV-F](https://trello.com/b/9QnhiOcN/ccdv-f)

Official SDK: https://github.com/anthropics/anthropic-sdk-java

## LAB 002 — lokalny test Anthropic Java SDK

Test: `src/test/java/dev/claudefoundations/lab/OfflineMessagesTest.java`.
MockWebServer `4.12.0` pasuje do OkHttp `4.12.0` używanego przez SDK.

Uruchomienie tylko tego ćwiczenia:

```powershell
.\gradlew.bat test --offline --tests "dev.claudefoundations.lab.OfflineMessagesTest"
```

Cały build wraz z testami:

```powershell
.\gradlew.bat build --offline
```

`--offline` wyłącza pobieranie zależności przez Gradle; wymaga ich wcześniejszej
obecności w lokalnym cache. Nie blokuje połączeń sieciowych kodu testowego.
Adres połączenia SDK ustawia tutaj `baseUrl` lokalnego mocka.

### Co robi test i dlaczego

1. MockWebServer nasłuchuje wyłącznie na `127.0.0.1`. Port `0` oznacza wybór
   wolnego portu przez system, dzięki czemu test nie wymaga stałego portu.
2. `enqueue(...)` przygotowuje odpowiedź HTTP 200 z JSON-em wiadomości.
   To stała odpowiedź testowa; żaden model nie jest uruchamiany.
3. Builder klienta otrzymuje `baseUrl(server.url("/").toString())` oraz fikcyjny
   `offline-test-key`. Nie korzysta z `fromEnv()`, więc nie wczytuje prawdziwego
   klucza ani adresu z konfiguracji środowiska. Mock nie weryfikuje uwierzytelnienia.
4. `MessageCreateParams` opisuje model, limit tokenów i wiadomość użytkownika.
   `offline-test-model` jest umowną nazwą na potrzeby mocka.
   `client.messages().create(params)` serializuje parametry i wysyła
   `POST /v1/messages` do lokalnego serwera, a następnie odczytuje JSON jako `Message`.
5. `takeRequest(...)` pozwala sprawdzić rzeczywiście wysłane żądanie: metodę,
   ścieżkę, fikcyjny nagłówek `Authorization: Bearer offline-test-key` (SDK 3.12.0)
   i pola JSON. Asercje odpowiedzi sprawdzają identyfikator oraz tekst z mocka.
6. Limit czasu i wyłączone ponowienia skracają oczekiwanie przy błędzie.
   `finally` zamyka klienta, a `try-with-resources` zamyka serwer także wtedy,
   gdy asercja się nie powiedzie. Klient ma metodę `close()`, ale w tej wersji
   nie implementuje `AutoCloseable`.

Przepływ: **JUnit → SDK → HTTP na loopback → MockWebServer → JSON → Message**.
Test weryfikuje konfigurację i transport SDK oraz odczyt odpowiedzi. Nie sprawdza
poprawności prawdziwego klucza, dostępności modelu ani jakości jego odpowiedzi.

### Samodzielne wyjaśnienie i review

Część techniczna jest gotowa, gdy test przechodzi i mock odbiera oczekiwane żądanie.
Edukacyjny warunek ukończenia pozostaje do samodzielnego potwierdzenia:

- Skąd SDK wie, na jaki adres wysłać żądanie?
- Dlaczego fikcyjny klucz i umowna nazwa modelu tutaj wystarczają?
- Co dowodzi odebranie żądania przez mock, a co sprawdzają asercje odpowiedzi?
- Dlaczego samo `--offline` nie gwarantuje lokalnego ruchu HTTP?

W razie problemu pytanie do review powinno zawierać komendę uruchomienia,
oczekiwane zachowanie, komunikat błędu i własną hipotezę przyczyny.

Źródła:

- [Konfiguracja Anthropic Java SDK](https://platform.claude.com/docs/en/cli-sdks-libraries/sdks/java)
- [Messages API](https://platform.claude.com/docs/en/api/java/messages/create)
- [MockWebServer 4.12.0](https://github.com/square/okhttp/tree/parent-4.12.0/mockwebserver)
