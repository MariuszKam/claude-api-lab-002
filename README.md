# Claude API Lab 002

Learning project for **Claude Certified Developer – Foundations** — Week 1, **Applications and Integration**.

We use the **official Anthropic Java SDK** rather than hand-written Messages API DTOs and HTTP transport.

**Task tracking and acceptance criteria:** [Trello CCDV-F](https://trello.com/b/9QnhiOcN/ccdv-f). The README covers the application, its configuration and how to run it; task status belongs only in Trello.

Official SDK: https://github.com/anthropics/anthropic-sdk-java

## Stack

- Java 25
- Gradle (wrapper 9.6.0)
- Anthropic Java SDK `com.anthropic:anthropic-java:3.12.0`
- JUnit Jupiter and MockWebServer for local SDK tests

## Uruchomienie z czystego repozytorium

Wymagania: Git, zainstalowany **JDK 25** oraz `JAVA_HOME` wskazujący ten JDK
(lub `java` dostępne w `PATH`). Używaj dołączonego Gradle Wrappera;
nie musisz instalować Gradle osobno. Poniższe komendy są dla PowerShell na Windows.

```powershell
git clone https://github.com/MariuszKam/claude-api-lab-002.git
cd claude-api-lab-002
java -version
```

### Pierwsze uruchomienie — przygotowanie cache

Na komputerze bez cache potrzebny jest jednorazowo dostęp do internetu,
aby Wrapper pobrał Gradle, a Gradle pobrał zależności z Maven Central.
Uruchom:

```powershell
.\gradlew.bat build
```

`build` kompiluje kod, tworzy dystrybucje aplikacji i uruchamia testy.
Już na tym etapie test SDK komunikuje się wyłącznie z lokalnym mockiem;
pobieranie narzędzi i zależności nie jest wywołaniem API Anthropic.

### Kolejne uruchomienia — bez pobierania zależności

Po przygotowaniu cache:

```powershell
.\gradlew.bat test --offline --rerun-tasks
.\gradlew.bat build --offline
.\gradlew.bat run --offline
```

- `test` uruchamia testy JUnit, w tym test SDK z lokalnym MockWebServer.
  `--rerun-tasks` wymusza wykonanie także wtedy, gdy Gradle uzna poprzednie wyniki za aktualne.
- `build` wykonuje pełny build wraz z testami; aktualne zadania mogą zostać pominięte.
- `run` uruchamia `Main`, który tylko wypisuje nazwę laboratorium i instrukcję testowania.
  Nie wysyła żądań HTTP.

**Prawdziwy klucz API nie jest wymagany** do żadnej z powyższych komend.
Nie trzeba ustawiać `ANTHROPIC_API_KEY`, tworzyć `.env` ani posiadać konta Anthropic.
Test SDK sam ustawia fikcyjny klucz i lokalny `baseUrl`.

Świeży klon bez cache nie wystarcza do uruchomienia całkowicie bez internetu.
Jeśli środowisko musi być odłączone od sieci od początku, wcześniej zapewnij
JDK 25, dystrybucję Wrappera i komplet zależności w cache Gradle.
Samo `--offline` nie dostarcza brakujących plików; Wrapper może próbować pobrać
własną dystrybucję jeszcze przed uruchomieniem Gradle.

Raport z rzeczywistego wykonania testów znajdziesz w `build/reports/tests/test/index.html`.
README nie podaje przykładowego wyniku jako dowodu wykonania testów.

### Przyszły tryb LIVE — warunki wdrożenia

**Tryb LIVE nie jest obecnie zaimplementowany.** Poniższe punkty są wymaganiami
przyszłej implementacji, a nie instrukcją uruchomienia istniejącej funkcji:

- Wywołanie API będzie wymagało jawnego włączenia LIVE. Samo ustawienie klucza
  nie może uruchamiać połączeń ani zmieniać domyślnego lokalnego trybu testów.
- Klucz będzie przekazywany poza repozytorium, np. przez `ANTHROPIC_API_KEY`
  lub magazyn sekretów CI. Nie wolno zapisywać go w kodzie, logach ani commitach.
  Brak klucza przy żądaniu LIVE powinien dawać czytelny błąd przed połączeniem.
- Potrzebne będą dostęp do API Anthropic, uprawnienia do wybranego modelu
  i środki na ewentualne koszty. Nazwa `offline-test-model` służy tylko mockowi.
- Implementacja będzie musiała jawnie określić docelowy adres HTTPS API,
  model, limit tokenów, timeout oraz zasady ponowień i obsługi błędów.
- Testy LIVE będą oddzielone od domyślnych `test` i `build`, również w CI.
  Zwykłe testy zachowają fikcyjny klucz i lokalny mock.
- Wyniki LIVE będzie można dokumentować dopiero po rzeczywistym wykonaniu,
  bez ujawniania sekretów. W ramach tego laboratorium nie wykonujemy wywołań LIVE.

Konfigurację klienta opisuje [dokumentacja Java SDK](https://platform.claude.com/docs/en/cli-sdks-libraries/sdks/java),
a parametry żądania opisuje [Messages API](https://platform.claude.com/docs/en/api/java/messages/create).

Never commit API keys or secrets. LAB 001 remains a separate record of the hand-written HTTP approach.

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

### Źródła

- [Konfiguracja Anthropic Java SDK](https://platform.claude.com/docs/en/cli-sdks-libraries/sdks/java)
- [Messages API](https://platform.claude.com/docs/en/api/java/messages/create)
- [MockWebServer 4.12.0](https://github.com/square/okhttp/tree/parent-4.12.0/mockwebserver)
