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
**CI:** GitHub Actions — APK собирается автоматически после каждого коммита (3–5 минут)

Приложение для игры в дартс с ботами. 6 игровых режимов, статистика,
настройки игрока, тренировочный раздел (в разработке).

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
- Давай код **целиком**, не разбивай на части.
- Не переписывай без нужды. Меняем минимально.
- Если автор прислал не тот файл — вежливо скажи и попроси нужный.
- Автор часто присылает **скриншоты** вместо кода — это нормально.
- **ВАЖНО:** сборка в GitHub Actions запускается автоматически при сохранении.
  Нельзя давать два шага в одном сообщении — первый закоммитится, соберётся
  с ошибкой (потому что второй ещё не готов).
- Если автор говорит «сначала соберём все правки, потом код» — веди
  нумерованный список правок, а код выдавай только когда он скажет «начинаем».
- Если автор просит «обнови AI_CONTEXT» — перепиши файл целиком.
- Автор задаёт вопросы по одному и просит отвечать так же.
- Автор часто уточняет логику — не торопись с кодом, сначала согласуй.

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
- SharedPreferences (настройки)
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
- **MainActivity.kt** — точка входа, вся навигация
- **theme/** — цвета, стили
- **data/** — данные (Room + репозитории)
- **ui/** — экраны (Compose)

### data/game501/
- BotPreferences.kt
- CheckoutTable.kt — таблица чекаутов 2–170
- Game501BotAI.kt — ИИ ботов
- **Game501Database.kt** — Room БД (Entity/Dao/DB/Repository), версия 5
- **Game501EntityConverter.kt** — Game501Entity → Game501 (для отчёта)
- Game501Logic.kt — логика игры
- Game501Models.kt — модели (Game501, Player501 и др.)
- **Game501Serializer.kt** — JSON-сериализация состояния партии
- Game501SettingsStorage.kt
- Game501Simulator.kt — прогон бот-бот (для калибровки)

### data/cricket/
- **CricketDatabase.kt** — Room БД, версия 4
- **CricketEntityConverter.kt** — CricketGameEntity → CricketGame
- CricketLogic.kt — логика игры
- CricketModels.kt — модели (CricketGame, CricketPlayer, CricketSector,
  CricketType, LegSnapshot, LegPlayerSnapshot)
- **CricketSerializer.kt** — JSON-сериализация состояния партии
- CricketBotAI.kt
- CricketSettingsStorage.kt
- PlayerNamesStorage.kt

### data/sector/, data/aroundclock/, data/biground/, data/scoreset/
- Каждый — своя БД (Entity + Dao + DB + Repository в одном файле).

### data/training/ (НОВОЕ — тренировочный раздел, в разработке)
- **TrainingModels.kt** — модели данных (TrainingSession, ExerciseResult,
  PostponedDouble, WeaknessSnapshot, PotentialSnapshot, ControlMatch,
  enum WeaknessType, BlockType, TrainingMode, ControlGameType)
- **TrainingDatabase.kt** — Room БД, версия 1
- **DartsNorms.kt** — таблица 16 уровней с нормативами
- **LevelCalculator.kt** — расчёт уровня, потенциала, трендов
- (дальше) WeaknessDetector.kt, TrainingPlanner.kt, TrainingSerializer.kt

### ui/
- WelcomeScreen, OnboardingScreen, LoadingScreen, MainMenuScreen,
  GameSelectScreen, SettingsScreen
- **StatsAggregates.kt** — «математика» статистики: enums StatPeriod и
  GameVariant, data-классы (CricketAggregate, Game501Aggregate, BotStat,
  MatchInfo, ChartSpec), объекты StatsParse (парсинг строк) и StatsCompute
  (агрегаты, графики, чекауты).
- **StatsScreen.kt** — экран статистики с вкладками Крикет / 501.
- **StatsMenuScreen.kt** — меню статистики (сетка 2×3 с карточками 6 игр).

### ui/game501/
- Game501SetupScreen.kt — настройка
- Game501Screen.kt — игровой экран (автосохранение)
- Game501StatsScreen.kt — отчёт о матче
- MatchesListDialog501.kt — диалог списка всех матчей 501

### ui/cricket/
- CricketSetupScreen.kt — настройка
- CricketGameScreen.kt — игровой экран
- CricketStatsScreen.kt — отчёт о матче
- MatchesListDialogCricket.kt — диалог списка матчей

### ui/sector/, ui/aroundclock/, ui/biground/, ui/scoreset/
- По 3 экрана (Setup/Game/Stats) в каждой.

---

## 5. АРХИТЕКТУРА

### Навигация
Ручная, без Navigation Compose:
```kotlin
var stage by remember { mutableStateOf("loading") }
// welcome / onboarding / loading / main
var screen by remember { mutableStateOf("main") }
// экран внутри stage=main
