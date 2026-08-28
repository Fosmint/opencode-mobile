# Промт для продолжения проекта OpenCode Mobile (Android) — pass 3

Скопируй всё, что ниже, как первое сообщение следующей модели вместе с
приложенным zip-архивом текущего состояния проекта (или ссылкой на
репозиторий https://github.com/Fosmint/opencode-mobile, если он уже
запушен и актуален).

---

Ты — senior Android engineer. Продолжи разработку Android-приложения
OpenCode Mobile (клиент к OpenCode server API).

**Сначала прочитай `ORIGINAL_PROMPT.txt`** — исходное ТЗ целиком, без
сокращений, источник истины по замыслу/стеку/дизайну/приоритетам. Потом
прочитай этот файл — что уже сделано и что осталось.

Проект уже третий проход. Распакуй архив / склонируй репозиторий и
**сначала изучи, что уже есть**, прежде чем писать новый код. Не
переписывай существующую архитектуру без необходимости.

## Контекст диалога — как дошли до этой точки

Пользователь (ник Fosmint, репозиторий на GitHub — Fosmint/opencode-mobile)
работает **с телефона через Termux**, без Android Studio, без локального
Gradle/SDK. Значит:
- Единственный способ проверить, собирается ли проект — **запушить в
  GitHub и посмотреть на вкладку Actions**. Не проси пользователя
  прогнать `./gradlew` локально, скорее всего он физически не может.
- Компиляция самой моделью (в песочнице предыдущей сессии) тоже
  невозможна — нет Android SDK, JDK и сети для загрузки зависимостей.
  Проверка синтаксиса Kotlin возможна только чтением кода глазами,
  вдумчиво, а не запуском.
- Так что финальная валидация "точно ли собирается" происходит только
  через реальный прогон `build.yml` в GitHub Actions, после пуша.

## Что уже сделано (не переделывай с нуля)

Слой данных/сети (pass 1):
- `opencode/api/OpenCodeClient.kt` / `RemoteOpenCodeClient.kt` (OkHttp +
  okhttp-sse) / `FutureLocalOpenCodeClient.kt` (осознанная заглушка)
- `opencode/models/*.kt` — под реальный wire-формат сервера
- `opencode/session/ChatStreamReducer.kt` — чистый reducer стрима, с
  юнит-тестами (`ChatStreamReducerTest.kt`, 8 тестов)
- `opencode/repository/ServerRepository.kt`, `SessionRepository.kt`
- `core/database/*.kt` (Room), `core/storage/*.kt` (DataStore + Keystore)

UI/навигация (pass 2):
- `MainActivity.kt` — `NavHost` + bottom nav (Home/Projects/Settings)
- `features/home/HomeScreen.kt`
- `features/projects/ProjectsViewModel.kt` + `ProjectsScreen.kt`
- `features/sessions/SessionListViewModel.kt` + `SessionListScreen.kt`
- `features/chat/ChatViewModel.kt` + `ChatScreen.kt` + `ChatComponents.kt`
- `features/settings/ServerListViewModel.kt`/`ServerListScreen.kt`,
  `AddServerViewModel.kt`/`AddServerScreen.kt`
- `ui/components/OpenCodeBottomBar.kt`, `ui/theme/*`, `ui/icons/*`

**Pass 3 (предыдущая сессия) — что изменилось:**

1. **Критический фикс API-контракта.** Pass 1 придумал несуществующие
   эндпоинты `/api/fs/list` и `/api/fs/read/*`. По реальным исходникам
   OpenCode (`packages/opencode/src/server/routes/instance/httpapi/groups/file.ts`)
   правильные эндпоинты — `GET /file` (список, query: `path`,
   `directory`) и `GET /file/content` (чтение, тот же query), плюс
   `GET /file/status` (git-статус). Это исправлено в:
   - `opencode/models/Models.kt` — `FileSystemEntry` теперь с полями
     `absolute`/`ignored`; добавлены `FileContentResponse` (`type`:
     text/binary, `encoding`, `mimeType`) и `FileStatusEntry`
   - `opencode/api/OpenCodeClient.kt` — сигнатуры `listFiles`/`readFile`
     обновлены, добавлен `fileStatus()`
   - `opencode/api/RemoteOpenCodeClient.kt` — реализация бьёт в реальные
     пути
   - `opencode/api/FutureLocalOpenCodeClient.kt` — заглушка обновлена под
     новые сигнатуры

2. **Files explorer реализован** (`features/files/`):
   - `FilesViewModel.kt` — навигация по папкам через стек путей (без
     Room-кэша: файловая система живая, кэш дал бы устаревшие данные),
     открытие файла на превью
   - `FilesScreen.kt` — список файлов/папок, bottom sheet с превью
     (моноширинный текст для текстовых файлов, отдельная ветка для binary)
   - Новая иконка `OpenCodeIcons.File`
   - `NavRoute.Files` изменён: раньше был `projectId` (бессмысленно, т.к.
     файловые эндпоинты не знают о projectId), теперь — `directory`
     (project's `worktree`), URL-encoded, т.к. содержит `/`
   - Подключено в `MainActivity.kt`, вход добавлен в `ProjectsScreen.kt`
     ("Browse files")

3. **Найден и исправлен блокер сборки: отсутствующая иконка приложения.**
   `AndroidManifest.xml` ссылался на `@mipmap/ic_launcher`, но
   `res/mipmap-anydpi-v26/` была физически пустой — ни одного файла.
   Это стопроцентно валит `aapt2` (сборку ресурсов) ещё до компиляции
   Kotlin. Добавлены:
   - `res/mipmap-anydpi-v26/ic_launcher.xml` (adaptive icon descriptor)
   - `res/drawable/ic_launcher_background.xml` (сплошной чёрный)
   - `res/drawable/ic_launcher_foreground.xml` (векторный глиф `< >`,
     white on transparent, в стиле остальных иконок проекта)
   Это НЕ финальный брендинг, а рабочая заглушка, чтобы сборка не падала
   на ресурсах — можно заменить на нормальный логотип позже, не срочно.

4. **Найден и исправлен второй блокер: `.github/workflows/` не попадала
   в zip-архивы, которые отдавались пользователю.** Причина — обычная
   команда `zip -r directory` без явного включения dotfiles не
   подхватывает файлы/папки, начинающиеся с точки. Из-за этого
   пользователь пушил репозиторий без `build.yml`/`release.yml`, и вкладка
   Actions на GitHub показывала "Get started" вместо реального workflow.
   Финальный архив пересобран через
   `find opencode-mobile -type f | zip -q archive.zip -@` — так что если
   пересобираешь архив для пользователя, **делай именно так, не
   `zip -r`**, и после сборки обязательно проверь
   `unzip -l archive.zip | grep -i github`, прежде чем отдавать файл.

**Статус на конец pass 3:** пользователь получил исправленный архив (с
`.github/workflows/` внутри), должен был перезалить его в Termux и
запушить. **Первое, что нужно сделать в этой сессии — спросить, что
показал прогон Actions** (зелёная галка / упавший шаг / какой лог) и
чинить по фактическому логу, а не гадать заново.

## Твоя задача — по приоритету

1. **Убедиться, что CI реально собирает APK.** Спроси у пользователя
   результат последнего прогона `build.yml`. Если упал — попроси
   скриншот/текст лога конкретного failed step (Actions → сам run →
   красный шаг → "View logs" или скрин). Чини по реальной ошибке, не
   переписывай наугад.
   Известные потенциальные проблемы, если дойдёт до Kotlin-компиляции:
   - `NavRoute.Sessions`/`Chat`/`Files` используют
     `backStackEntry.arguments?.getString(...)` без явного
     `navArgument()` — Navigation Compose 2.8.5 может принимать это
     неявно через синтаксис `{param}` в route, но если компилятор/рантайм
     ругнётся на null-аргумент, добавь `arguments = listOf(navArgument("...") {...})`
     в `composable(...)`.
   - Ничего в pass 1/2/3 не прогонялось через реальный Kotlin-компилятор
     до этого момента — возможны опечатки в импортах или несовпадения
     сигнатур, которые обнаружатся только при реальной сборке.

2. **Diff viewer** (`features/diff/`) — всё ещё пусто. Прежде чем
   писать код, разберись в исходниках OpenCode (архив `opencode-dev.zip`,
   если пользователь его снова приложит, или попроси заново): как
   реально получить diff — поле `snapshot` в `session-message.ts`
   (нужно проверить точный путь). Реальные серверные роуты в срезе,
   который проверялся в pass 3, лежат в
   `packages/opencode/src/server/routes/instance/httpapi/groups/*.ts` —
   смотри именно там в первую очередь, а не в `packages/protocol/` или
   `packages/schema/`, эти пути из старого ТЗ могли устареть или
   описывать другой слой. Проверь, есть ли отдельный `diff`-эндпоинт в
   файловой группе (`file.ts`, там уже есть `file.status`) или где-то
   ещё, или diff нужно строить самому из `file.status` + `file.content`
   до/после. Не выдумывай API — если не находишь, явно скажи
   пользователю, что нужен доступ к исходникам OpenCode ещё раз.

3. **Terminal** (`features/terminal/`) — транспорт задокументирован в
   README: `/api/pty/:ptyID/connect` — WebSocket с одноразовым тикетом
   через `/api/pty/:ptyID/connect-token` (подтверждено в pass 1/2 через
   `packages/protocol/src/groups/pty.ts`). Учитывая, что `file.ts`
   оказался в другом месте, чем ожидалось (см. выше), **перепроверь и
   путь к pty.ts тоже** против реального дерева
   `packages/opencode/src/server/routes/instance/httpapi/groups/`,
   прежде чем писать WebSocket-клиент. Нужен отдельный клиент поверх
   OkHttp (OkHttp поддерживает WebSocket из коробки), не переиспользуй
   SSE-клиент бездумно.

4. **Тесты**: `ChatStreamReducer` уже покрыт (8 тестов). Дальше —
   `RemoteOpenCodeClient` через `okhttp-mockwebserver` (зависимость уже
   есть) — особенно важно покрыть исправленные в pass 3 file-эндпоинты,
   раз именно там нашлась ошибка. Потом ViewModel'и через `turbine` +
   `kotlinx-coroutines-test` (тоже уже в зависимостях). Без
   fake-тестов вида `assertTrue(true)`.

5. **Gradle wrapper**: сейчас CI сам бутстрапит `gradlew` через
   `gradle wrapper --gradle-version 8.9`, если он не закоммичен (см.
   `build.yml`). Это рабочий подход, но ещё не проверен реальным
   прогоном на момент конца pass 3 — уточни у пользователя, отработал
   ли этот шаг. Если предпочтительнее закоммиченный wrapper — сгенерируй
   и закоммить `gradlew`/`gradlew.bat`/`gradle-wrapper.jar`.

## Важные практические уроки pass 3 — не наступай на те же грабли

- **Когда отдаёшь zip-архив пользователю, ВСЕГДА проверяй, что в архиве
  реально лежит то, что ты думаешь.** В pass 3 дважды была
  рассинхронизация между "что отредактировано на диске" и "что попало в
  архив, который получил пользователь" — один раз из-за `zip -r` молча
  пропустившего dotfiles (`.github/`), это осталось незамеченным до
  прямого запроса пользователя. Всегда делай
  `unzip -l archive.zip | grep -i <критичная_папка>` перед тем как
  отдать файл.
- **Не доверяй ТЗ и старым KDoc-комментариям слепо насчёт путей к
  исходникам upstream.** Комментарии в коде (pass 1/2) ссылались на
  `packages/protocol/src/groups/*.ts` и `packages/schema/src/*.ts`, но
  реальные серверные роуты в срезе `opencode-dev.zip`, который
  использовался в pass 3, лежат в
  `packages/opencode/src/server/routes/instance/httpapi/groups/*.ts`.
  Если пользователь снова пришлёт архив исходников OpenCode — сверяйся
  с ним напрямую через `grep`/`find`, а не полагайся на комментарии
  предыдущих проходов.
- Пользователь общается неформально ("бро", "чо это", много опечаток) —
  это не признак того, что он не разбирается; он явно проверяет
  реальный результат (скриншотами с телефона) и ловит реальные баги
  (обе находки pass 3 — иконка и отсутствующий workflow — обнаружены
  именно им, не проактивно моделью). Относись к его репортам серьёзно и
  проверяй именно то, на что он указывает.
- Пользователь работает **только через Termux на телефоне**. Не
  предлагай шаги, требующие Android Studio, локального эмулятора или
  desktop-окружения, если explicitly не попросит.

## Важные ограничения — не нарушай

- Не изобретай API endpoints. Сверяйся с реальными исходниками
  (`packages/opencode/src/server/routes/instance/httpapi/groups/*.ts` в
  первую очередь, если исходники приложены). Если исходников нет под
  рукой — прямо попроси у пользователя.
- Не используй mock/fake responses в финальном коде.
- Не добавляй TODO вместо ключевой функциональности MVP.
- Секреты (пароли Basic Auth) — только через `CredentialStore`
  (Keystore-backed), никогда в Room/DataStore в открытом виде.
- Дизайн — строго grayscale, иконки только Compose ImageVector, никаких
  PNG/emoji.
- После каждого крупного изменения — если есть возможность (реальный
  Gradle/SDK), собери и почини ошибки сразу; если нет — явно скажи
  пользователю, что сборка не проверялась инструментом, только чтением
  кода, и попроси прогнать через Actions.

## Формат ответа

Работай поэтапно. После каждого завершённого пункта коротко резюмируй,
что сделано и что реально протестировано (собралось в Actions / прошли
тесты), а что осталось предположением, требующим проверки через
реальный прогон CI.
