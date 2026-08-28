# Промт для продолжения проекта OpenCode Mobile (Android)

Скопируй всё, что ниже, как первое сообщение следующей модели вместе с
приложенным zip-архивом текущего состояния проекта.

---

Ты — senior Android engineer. Продолжи разработку Android-приложения
OpenCode Mobile (клиент к OpenCode server API).

**Сначала прочитай `ORIGINAL_PROMPT.txt` в корне репозитория** — это
исходное ТЗ целиком, без сокращений. Это и есть источник истины по общему
замыслу, стеку, дизайну и приоритетам проекта. Потом прочитай этот файл —
он описывает, что конкретно уже сделано и что осталось.

Проект уже частично реализован (уже второй проход). Распакуй архив и
**сначала изучи, что уже есть**, прежде чем писать новый код. Не переписывай
существующую архитектуру без необходимости.

## Что уже сделано (не переделывай с нуля)

Слой данных/сети (pass 1):
- `opencode/api/OpenCodeClient.kt` / `RemoteOpenCodeClient.kt` (OkHttp +
  okhttp-sse) / `FutureLocalOpenCodeClient.kt` (осознанная заглушка)
- `opencode/models/*.kt` — под реальный wire-формат сервера
- `opencode/session/ChatStreamReducer.kt` — чистый reducer стрима, теперь
  с юнит-тестами (`app/src/test/.../ChatStreamReducerTest.kt`, 8 тестов)
- `opencode/repository/ServerRepository.kt`, `SessionRepository.kt`
- `core/database/*.kt` (Room), `core/storage/*.kt` (DataStore + Keystore)

UI/навигация (pass 2 — новое в этом срезе):
- `MainActivity.kt` — реальный `NavHost` + bottom nav (Home/Projects/
  Settings), связывает всё ниже
- `features/home/HomeScreen.kt`
- `features/projects/ProjectsViewModel.kt` + `ProjectsScreen.kt` — см. их
  KDoc: проект резолвится через `/api/location`, отдельного list-эндпоинта
  нет (подтверждено чтением `packages/protocol/src/groups/location.ts` и
  `project-copy.ts`)
- `features/sessions/SessionListViewModel.kt` + `SessionListScreen.kt`
- `features/chat/ChatViewModel.kt` (pass 1) + `ChatScreen.kt` (новое) +
  `ChatComponents.kt` (pass 1, bubbles/code fences/reasoning/tool calls)
- `features/settings/ServerListViewModel.kt`/`ServerListScreen.kt`,
  `AddServerViewModel.kt`/`AddServerScreen.kt`
- `ui/components/OpenCodeBottomBar.kt`, `ui/theme/*`, `ui/icons/*`

CI:
- `.github/workflows/build.yml`/`release.yml` теперь сами бутстрапят
  Gradle wrapper через `setup-gradle` action, если он не закоммичен
  (`gradle wrapper --gradle-version 8.9`) — рабочий, но непроверенный
  реальным прогоном подход

## Твоя задача — по приоритету

1. **САМОЕ ВАЖНОЕ: собери проект и почини все ошибки компиляции.**
   Ни один файл в этом срезе не прогонялся через реальный Kotlin-компилятор
   (в сессии, где это писалось, не было Android SDK/сети). Вероятны мелкие
   несостыковки импортов, сигнатур, неиспользуемых импортов. Пройдись
   `./gradlew assembleDebug` / `./gradlew compileDebugKotlin` и исправь
   построчно. Особое внимание:
   - `MainActivity.kt` — сложная связка NavHost + LaunchedEffect для
     резолва `SessionRepository` по активному серверу; проверь корректность
     `collectAsState`/типов
   - `NavRoute.Sessions`/`Chat`/`AddServer` — параметризованные роуты,
     проверь что `backStackEntry.arguments?.getString(...)` реально
     возвращает значение для путей вида `sessions/{projectId}` без явного
     `navArgument()` — если Navigation Compose в используемой версии (2.8.5)
     это требует явного объявления аргументов, добавь его

2. **Сгенерируй и закоммить Gradle wrapper** (`gradlew`, `gradlew.bat`,
   `gradle/wrapper/gradle-wrapper.jar`) через
   `gradle wrapper --gradle-version 8.9`, если предпочитаешь закоммиченный
   вариант вместо бутстрапа в CI. Оба варианта валидны, но закоммиченный
   надёжнее и проще для локальной разработки в Android Studio.

3. **Files explorer** (`features/files/`) — пусто. Backend уже есть
   (`OpenCodeClient.listFiles`/`readFile`), нужен ViewModel + Compose-экран
   с деревом папок и простым просмотром содержимого.

4. **Diff viewer** (`features/diff/`) — пусто. Сначала разберись в
   исходниках OpenCode, как реально получить diff (поле `snapshot` в
   `session-message.ts` — отдельный эндпоинт или git-сравнение снапшотов на
   сервере?). Не выдумывай API.

5. **Terminal** (`features/terminal/`) — пусто. Транспорт уже известен и
   задокументирован в README: `/api/pty/:ptyID/connect` — WebSocket с
   одноразовым тикетом через `connect-token` (см.
   `packages/protocol/src/groups/pty.ts`). НЕ переиспользуй
   `RemoteOpenCodeClient`/OkHttp SSE-клиент бездумно — нужен отдельный
   WebSocket-клиент (OkHttp поддерживает WebSocket из коробки, но это
   отдельный код).

6. **Тесты**: `ChatStreamReducer` уже покрыт. Дальше —
   `RemoteOpenCodeClient` через `okhttp-mockwebserver` (уже в зависимостях),
   затем ViewModel'и через `turbine` + `kotlinx-coroutines-test` (тоже уже
   в зависимостях). Без fake-тестов вида `assertTrue(true)`.

7. **GitHub Actions**: проверь workflow реальным прогоном после того, как
   проект собирается локально — особенно шаг бутстрапа wrapper.

## Важные ограничения — не нарушай

- Не изобретай API endpoints. Если чего-то не знаешь — сверься с
  `ORIGINAL_PROMPT.txt` не поможет тут, нужны исходники OpenCode (spрashи в
  `packages/protocol/src/groups/` и `packages/schema/src/`); если их нет
  под рукой — попроси у пользователя, явно скажи что нужно, как это уже
  делалось в предыдущих проходах.
- Не используй mock/fake responses в финальном коде.
- Не добавляй TODO вместо ключевой функциональности MVP.
- Секреты (пароли Basic Auth) — только через `CredentialStore`
  (Keystore-backed), никогда в Room/DataStore в открытом виде.
- Дизайн — строго grayscale, иконки только Compose ImageVector, никаких
  PNG/emoji.
- После каждого крупного изменения — собери проект и почини ошибки сразу.

## Формат ответа

Работай поэтапно. После каждого завершённого пункта коротко резюмируй, что
сделано и что реально протестировано (собралось / прошли тесты), а что
осталось предположением, требующим проверки.
