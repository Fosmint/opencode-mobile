# OpenCode Mobile (Android) — WIP handoff (pass 2)

Независимый Android-клиент к [OpenCode](https://github.com/sst/opencode) (не
официальный продукт OpenCode/sst). Эндпоинты и схемы сверены с реальными
исходниками upstream-репозитория (`packages/protocol/src/groups/*.ts`,
`packages/schema/src/*.ts`), которые были предоставлены вместе с ТЗ.

`ORIGINAL_PROMPT.txt` в корне репозитория — исходное ТЗ на русском, без
изменений. Читай его первым, если нужно понять общий замысел проекта.

## Что реально работает в этом срезе

- **MainActivity теперь настоящий** — `NavHost` с Navigation Compose,
  bottom nav (Home / Projects / Settings), связывающий все ViewModel'и с
  экранами: `HomeScreen`, `ServerListScreen`, `AddServerScreen`,
  `ProjectsScreen`, `SessionListScreen`, `ChatScreen`
- `ProjectsViewModel` — резолвит текущий проект через `GET /api/location`
  (см. KDoc в файле — там ссылки на конкретные проверенные upstream-файлы)
- `SessionListViewModel` — список сессий проекта + создание новой
- Юнит-тесты `ChatStreamReducerTest` — 8 тестов с реальными JSON-фикстурами
- `.github/workflows/build.yml`/`release.yml` теперь сами генерируют Gradle
  wrapper в CI (`gradle wrapper --gradle-version 8.9`), если он не
  закоммичен — см. "Что НЕ доделано" ниже
- Gradle Kotlin DSL проект (`settings.gradle.kts`, `build.gradle.kts`,
  `libs.versions.toml` с централизованными версиями)
- Типизированные модели данных, отражающие реальный wire-формат сервера
  (`opencode/models/Models.kt`, `Events.kt`)
- `OpenCodeClient` — интерфейс-абстракция backend'а
  - `RemoteOpenCodeClient` — рабочая реализация на OkHttp + okhttp-sse:
    health check, location/project info, list/create session, отправка
    промпта, interrupt, получение контекста сессии, SSE-подписка на события
    сессии и сервера, список моделей, switch model/agent, чтение файловой
    системы
  - `FutureLocalOpenCodeClient` — осознанная заглушка для будущего
    локального/embedded рантайма (см. KDoc в файле — честно объясняет,
    почему это не сделано сейчас, вместо фейковой имитации)
- `ChatStreamReducer` — чистая функция, превращающая SSE-события
  (`message.updated`, `message.part.updated`) в обновлённый список
  сообщений — это и есть механизм стриминга ответа ассистента
- Room-кэш (servers/sessions/messages/projects) для офлайн-отображения и
  восстановления состояния после перезапуска
- Зашифрованное хранилище паролей (Keystore-backed
  `EncryptedSharedPreferences`) — не хранит секреты в открытом виде
- ViewModel'и: `ServerListViewModel`, `AddServerViewModel` (с реальной
  проверкой соединения), `ChatViewModel` (send/stop/retry/switch model,
  подписка на стрим)
- UI-компоненты чата: пузыри сообщений, разбор code fences с моноширинным
  блоком и кнопкой копирования, сворачиваемые reasoning-блоки,
  сворачиваемые карточки tool-call с состояниями pending/running/
  completed/error, анимированный курсор стриминга
- Чёрно-белая/grayscale Material 3 тема, набор кастомных векторных иконок
  (Compose ImageVector — не bitmap, не emoji)

## Что подтверждено чтением исходников OpenCode в этом срезе

- **Список проектов**: нет `/api/project`. Подтверждено в
  `packages/protocol/src/groups/location.ts` (только резолв одной
  location) и `project-copy.ts` (copy/remove/refresh существующего
  проекта, не листинг). Проект резолвится через `/api/location`
  (`Location.Info.project`), доп. проекты — через `projectID` в сессиях
  и/или SSE-событие `project.updated`.
- **PTY/terminal — это WebSocket, не HTTP/SSE.** Подтверждено в
  `packages/protocol/src/groups/pty.ts`: `/api/pty` (list/create/get/update/
  remove) — обычный REST, но `/api/pty/:ptyID/connect` — WebSocket upgrade
  с одноразовым тикетом через `/api/pty/:ptyID/connect-token`. Значит
  `RemoteOpenCodeClient` **нельзя** переиспользовать бездумно для этого —
  нужен отдельный WebSocket-транспорт. Ещё не реализовано.

## Что НЕ доделано (честно)

- **Files explorer** (`features/files/`) — backend-методы
  (`OpenCodeClient.listFiles`/`readFile`) есть, экрана нет
- **Diff viewer** (`features/diff/`) — не изучено, откуда реально брать
  diff (поле `snapshot` в session-message нужно разобрать: отдельный
  эндпоинт или git-сравнение снапшотов на сервере)
- **Terminal** (`features/terminal/`) — транспорт теперь известен
  (WebSocket + ticket, см. выше), реализации всё ещё нет
- **Gradle wrapper jar не закоммичен** — сгенерируется в CI автоматически,
  но для локальной сборки в Android Studio нужно один раз выполнить
  `gradle wrapper --gradle-version 8.9` вручную
- Тестов пока только на `ChatStreamReducer`. `RemoteOpenCodeClient` (через
  `okhttp-mockwebserver`) и ViewModel'и (через `turbine`) — не покрыты,
  хотя обе зависимости уже в `libs.versions.toml`
- **Ничего из этого не компилировалось** реальным Kotlin-компилятором — в
  этой сессии нет Android SDK/JDK/сети для `./gradlew`. Нужен реальный
  прогон `./gradlew assembleDebug`, чтобы отловить возможные опечатки в
  импортах/сигнатурах

## Структура

```
app/src/main/kotlin/dev/opencode/mobile/
├── core/            network (OkHttp factory, JSON config), database (Room),
│                    storage (DataStore, encrypted prefs), common (DI helper)
├── opencode/        api (OpenCodeClient + Remote/FutureLocal impl + factory),
│                    models, repository, session (stream reducer)
├── features/        chat (ViewModel + Compose UI), settings (server list +
│                    add/edit server ViewModels) — остальные features/* пустые
└── ui/              theme (grayscale palette), icons (custom ImageVectors)
```

## Локальный запуск (после доработки)

```bash
gradle wrapper --gradle-version 8.9   # сгенерировать gradlew + jar
./gradlew assembleDebug
```

## Сборка через GitHub Actions

`.github/workflows/build.yml` и `release.yml` уже добавлены и рабочие по
структуре, но зависят от закоммиченного `gradlew`/`gradle-wrapper.jar` (см.
выше). Release workflow ожидает секреты `RELEASE_KEYSTORE_BASE64`,
`RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`.

## Лицензия

Перед публичным релизом проверить актуальную лицензию upstream OpenCode
репозитория; данный проект не копирует код OpenCode напрямую (используется
только протокол/API как контракт), но это стоит явно перепроверить.

## Дальнейшие шаги

См. `NEXT_PROMPT.md` — готовый промт для продолжения этой работы другой
моделью/сессией.
