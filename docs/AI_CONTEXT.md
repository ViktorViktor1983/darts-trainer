# AI_CONTEXT — шпаргалка для нового ИИ-ассистента

Этот файл — краткая инструкция для любого ИИ (Claude, ChatGPT, Gemini и т.д.),
который подхватит работу над проектом Darts Trainer. Прочитай целиком,
прежде чем задавать вопросы.

---

## 1. ПРОЕКТ

**Название:** Darts Trainer (Персональный тренер по дартсу)
**Платформа:** Android, нативный Kotlin + Jetpack Compose + Room
**Репозиторий:** github.com/ViktorViktor1983/darts-trainer (ветка main)
**Пакет:** com.lodkin.dartstrainer
**Сборка:** Gradle Kotlin DSL (build.gradle.kts)
**CI:** GitHub Actions — сборка APK 3–5 минут после каждого коммита

Приложение для игры в дартс с ботами. 6 игровых режимов, статистика,
настройки игрока, тренировки.

**Автор:** Виктор Лодкин (ViktorViktor1983).

**Второе приложение (не трогаем):**
Darts Checkout — справочник чекаутов.
Репозиторий: github.com/ViktorViktor1983/DARTS-checkout
Статус: на модерации RuStore.

---

## 2. КАК ОБЩАТЬСЯ С АВТОРОМ

Автор — не программист, изучает Kotlin/Android по ходу дела.
Правила:

- Объясняй простыми словами, без жаргона.
- **ОДИН ФАЙЛ — ОДНО СООБЩЕНИЕ.** Это железное правило.
  Не давать 2+ файлов в одном ответе. Если нужно обновить 2 файла —
  сначала один, потом автор напишет «Готово», потом второй.
- **Формат выдачи кода:**
  1. Путь к файлу (отдельным блоком)
  2. Действие: «создать новый» / «заменить целиком» / «найти строку X»
  3. Сам код целиком одним блоком
- Давай код **целиком**, не разбивай на части. Автор копирует всё.
- Не переписывай без нужды. Меняем минимально.
- Если автор прислал не тот файл — вежливо скажи и попроси нужный.
- Автор часто присылает **скриншоты** вместо кода — это нормально.
- **ВАЖНО:** сборка в GitHub Actions запускается АВТОМАТИЧЕСКИ при
  сохранении файла. Нельзя давать два шага в одном сообщении — первый
  закоммитится, соберётся с ошибкой (потому что второй ещё не готов).
- Если автор просит «обнови AI_CONTEXT» — перепиши файл целиком.

**Что автор УМЕЕТ:**
читать текст, скриншоты, фото; писать код Kotlin/Compose/Room
(копирует в GitHub вручную); собирать APK через GitHub Actions.

**Что автор НЕ УМЕЕТ:**
открывать ссылки/GitHub/интернет за ИИ, генерировать картинки,
загружать файлы в GitHub за ИИ.

---

## 3. ТЕХНИЧЕСКИЙ СТЕК

- Kotlin + Jetpack Compose
- Room (SQLite) с миграциями
- SharedPreferences (для настроек)
- GitHub Actions
- Тёмная тема

**Цвета темы (theme/):**
- Accent = #4FC3F7 (голубой)
- GoldAccent = #FFD54F (золотой)
- DarkBg = #121212
- TileBg = #4A6572
- TileBgDark = #37474F
- ErrorColor (для ошибок/удаления)

---

## 4. СТРУКТУРА ПРОЕКТА

### Корень репозитория
- .github/workflows — GitHub Actions
- app — модуль приложения
- docs — документация (21 файл + AI_CONTEXT.md)
- keystore — ключи подписи
- README.md, build.gradle.kts, settings.gradle.kts, gradle.properties

### Внутри app/
- app/build.gradle.kts
- app/src/main/AndroidManifest.xml
- app/src/main/java/com/lodkin/dartstrainer/

### Пакет com.lodkin.dartstrainer/
- MainActivity.kt — точка входа, вся навигация
- theme/ — цвета, стили
- data/ — данные (Room + репозитории)
- ui/ — экраны (Compose)

### data/game501/
- BotPreferences.kt
- CheckoutTable.kt — таблица чекаутов 2–170
- Game501BotAI.kt — ИИ ботов
- **Game501Database.kt** — Room БД (Entity/Dao/DB/Repository), версия 5
- Game501EntityConverter.kt — Game501Entity → Game501 (для отчёта матча)
- Game501Logic.kt — логика игры
- Game501Models.kt — модели
- Game501Serializer.kt — JSON сериализация состояния партии
- Game501SettingsStorage.kt
- Game501Simulator.kt — прогон бот-бот

### data/cricket/
- CricketDatabase.kt — Room БД, версия 4
- CricketEntityConverter.kt — CricketGameEntity → CricketGame
- CricketLogic.kt — логика игры
- CricketModels.kt — модели (CricketGame, CricketPlayer и др.)
- CricketSerializer.kt — JSON сериализация состояния партии
- CricketBotAI.kt
- CricketSettingsStorage.kt
- PlayerNamesStorage.kt

### data/sector/, data/aroundclock/, data/biground/, data/scoreset/
- Каждый — своя БД (Entity + Dao + DB + Repository в одном файле).

### ui/
- WelcomeScreen, OnboardingScreen, LoadingScreen, MainMenuScreen,
  GameSelectScreen, SettingsScreen
- **StatsScreen.kt** — большой экран статистики (вкладки Крикет / 501).
  Параметры: `initialTab`, `showTabs`, `onResumeGame501`, `onResumeCricket`.
  Внутри: кнопка «📋 Список матчей», диалоги, графики, чекауты.
  ⚠️ Файл ~1600 строк — в планах разбить.
- **StatsMenuScreen.kt** — меню статистики (сетка 2×3 с карточками 6 игр).
  На карточках 501 и Крикета — красный значок `⏸ N` при незавершённых.

### ui/game501/
- Game501SetupScreen.kt — настройка (карточки игроков с PPR: своё `ср. 75.4`,
  у бота `ср. 52–55`), кнопка «🧪 Тест бота»
- Game501Screen.kt — игровой экран (автосохранение, диалог «Прервать игру?»)
- Game501StatsScreen.kt — отчёт о матче
- **MatchesListDialog501.kt** — диалог списка всех матчей 501

### ui/cricket/
- CricketSetupScreen.kt — настройка (карточки с MPR)
- CricketGameScreen.kt — игровой экран (автосохранение)
- CricketStatsScreen.kt — отчёт о матче
- **MatchesListDialogCricket.kt** — диалог списка всех матчей крикета

### ui/sector/, ui/aroundclock/, ui/biground/, ui/scoreset/
- По 3 экрана (Setup/Game/Stats) в каждой.

---

## 5. АРХИТЕКТУРА

### Навигация
Ручная, без Navigation Compose:
```kotlin
var stage by remember { mutableStateOf("loading") } // welcome / onboarding / loading / main
var screen by remember { mutableStateOf("main") }   // экран внутри stage=main
