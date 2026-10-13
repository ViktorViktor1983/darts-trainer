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
настройки игрока, полноценный тренировочный раздел с ИИ-планировщиком,
система рекордов с праздничной анимацией.

**Автор:** Виктор Лодкин (ViktorViktor1983).

**Второе приложение (не трогаем):**
Darts Checkout — справочник чекаутов.
Репозиторий: github.com/ViktorViktor1983/DARTS-checkout

---

## 2. КАК ОБЩАТЬСЯ С АВТОРОМ

Автор — не программист, изучает Kotlin/Android по ходу дела.
Правила:

- Объясняй простыми словами, без жаргона.
- **ОДИН ФАЙЛ — ОДНО СООБЩЕНИЕ.** Железное правило.
- **Формат выдачи кода:**
  1. Путь к файлу (отдельным блоком)
  2. Действие: «создать новый» / «заменить целиком»
  3. Сам код целиком одним блоком
- Давай код **целиком**, не разбивай на части.
- **Даже для правки в 2 строки автор просит: «путь и файл целиком».**
  Это его стандартный запрос — не удивляйся, давай весь файл.
- Если файл уже собран — не переписывай, меняем минимально.
- Автор часто присылает **скриншоты** — это нормально.
- **ВАЖНО:** сборка в GitHub Actions запускается автоматически после сохранения.
  Нельзя давать два шага в одном сообщении.
- Если автор просит «обнови AI_CONTEXT» — перепиши файл целиком.
- **НЕ использовать вложенные блоки кода (тройные бэктики внутри тройных)** —
  телеграм обрывает копирование.
- Автор задаёт вопросы **по одному**, отвечай так же.
- Сначала согласуй логику, потом давай код.
- Если автор прислал не тот файл — вежливо скажи и попроси нужный.
- **Автор может не понимать техническую часть** — ты решаешь, как лучше
  технически, а он задаёт логику «как должно работать». Доверяй ему в логике,
  он доверяет тебе в технике.

**Что автор УМЕЕТ:**
читать текст, скриншоты, фото; писать код Kotlin/Compose/Room
(копирует в GitHub вручную); собирать APK через GitHub Actions.

**Что автор НЕ УМЕЕТ:**
открывать ссылки/GitHub/интернет за ИИ, генерировать картинки,
загружать файлы в GitHub за ИИ.

**Что раздражает автора:**
когда ИИ просит «найди строку X в файле» — он предпочитает получить
файл целиком и заменить его тоже целиком. Экономит время и исключает ошибки.

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

## 4. ПОЛНАЯ СТРУКТУРА ПРОЕКТА

### Корень репозитория
- .github/workflows/ — GitHub Actions
- app/ — модуль приложения
- docs/ — документация
- keystore/ — ключи подписи
- README.md, build.gradle.kts, settings.gradle.kts, gradle.properties

### Пакет com.lodkin.dartstrainer/

**Корень:**
- MainActivity.kt — точка входа, вся навигация (~1600 строк)
- theme/ — цвета, стили

### data/ — данные

**data/SettingsStorage.kt** — общие настройки игрока.
Имя владельца телефона — `SettingsStorage.getPlayerName(context)`.
Используется для фильтрации рекордов.

#### data/game501/
- Game501Models.kt, Game501Logic.kt, Game501BotAI.kt
- Game501Database.kt (версия 5), Game501EntityConverter.kt
- Game501Serializer.kt, Game501SettingsStorage.kt
- CheckoutTable.kt, Game501Simulator.kt, BotPreferences.kt

#### data/cricket/
- CricketModels.kt, CricketLogic.kt (checkLegWinner исправлен)
- CricketBotAI.kt (исправлен), CricketPlayers.kt (CRICKET_BOTS + PlayerNamesStorage)
- CricketDatabase.kt (версия 4), CricketEntityConverter.kt
- CricketSerializer.kt, CricketSettingsStorage.kt

#### data/sector/
- SectorDatabase.kt — SectorGameEntity, Dao, DB, Repository.
  Есть getGamesBySector(sector) для чтения рекорда.

#### data/biground/
- BigRoundDatabase.kt — BigRoundGameEntity, Dao, DB, Repository.
  Есть getAllGames().

#### data/scoreset/
- ScoreSetDatabase.kt — ScoreSetGameEntity, Dao, DB, Repository.
  Есть getAllGames().

#### data/aroundclock/
- AroundClockDatabase.kt — AroundClockGameEntity, Dao, DB, Repository.
  Есть getAllGames(), фильтрация по target/order/hitsRequired.

#### data/freerecords/ (НОВОЕ — 14.10.2026)
- **FreePlayRecordsDatabase.kt** — рекорды свободной игры.
  Одна таблица `free_records`: recordKey (PK), bestInt, bestFloat, extraInt, achievedAt.
  Ключи: game501_leg, game501_match, cricket_leg, cricket_match.
  Репозиторий `FreePlayRecordsRepository` с методами:
  - get(key), hasRecord(key), getAll()
  - tryUpdate501Leg(darts, ppr): Boolean
  - tryUpdate501Match(ppr, doublesPercent): Boolean
  - tryUpdateCricketLeg(mpr): Boolean
  - tryUpdateCricketMatch(mpr): Boolean
  - clearAll(), clear501(), clearCricket()

#### data/training/ — тренировочный раздел

- **TrainingModels.kt** — модели:
  TrainingSession, ExerciseResult (с durationSeconds),
  PostponedDouble, WeaknessSnapshot, PotentialSnapshot, ControlMatch.
  Enums: WeaknessType (SCORE, DOUBLES, ACCURACY, TREBLES), BlockType,
  TrainingMode, ControlGameType.

- **TrainingDatabase.kt** — **версия 4** (14.10.2026).
  Таблицы: training_sessions, exercise_results, postponed_doubles,
  weakness_snapshots, potential_snapshots, control_matches, training_records.
  Миграции: 1→2 (durationSeconds), 2→3 (training_records), 3→4 (bestFloat, extraInt).
  TrainingRecordEntity поля: exerciseId (PK), bestScore, bestDarts, bestFloat, extraInt, achievedAt.
  Методы репозитория:
  - getTrainingRecord(exerciseId)
  - updateTrainingRecordIfBetter(exerciseId, score, darts, lowerDartsBetter) — для старых упражнений
  - update501LegRecord(darts, ppr): Boolean
  - update501MatchRecord(ppr, doublesPercent): Boolean
  - updateCricketLegRecord(mpr): Boolean
  - updateCricketMatchRecord(mpr): Boolean
  - clearAllTrainingData() — чистит всё, включая training_records.

- **DartsNorms.kt** — 16 уровней (LevelNorm).
- **LevelCalculator.kt** — расчёт уровня, потенциала, трендов.
- **WeaknessDetector.kt** — определение слабых мест.
- **TrainingPlanner.kt** — генератор программы.
  Шкала тяжести: Крикет 1, 501 2, Кругосветка-Сектора 3, Набор 4,
  Сектор 20 = 5, Большой 6, Удвоения 7, Утроения 8.
  Порог «тяжёлое»: difficulty >= 7. Максимум 1 тяжёлое за тренировку.
  Первые 5 тренировок — вводный режим.
- **TrainingBlocksStorage.kt** — флаги блоков (SharedPreferences training_blocks).
- **TrainingStateStorage.kt** — автосохранение тренировки. Правило 3 часа.
- **TrainingBotLevelStorage.kt** — уровни ботов для тренировочных партий.
- **TrainingStatsAnalyzer.kt** — анализ данных (5 последних записей).

### ui/common/ — общие компоненты

- **CelebrationOverlay.kt** — праздничный диалог с анимацией.
  Параметры: visible, isNewRecord, onConfirm, onUndo, content.
  Если isNewRecord = false — анимации нет, но диалог показывается.
  Внутри Dialog. Анимация: 12 ракет + 90 конфетти, длительность 4,5 сек.

### ui/ — экраны

**Общие:**
- WelcomeScreen, OnboardingScreen, LoadingScreen
- MainMenuScreen — главное меню.
- GameSelectScreen — сетка 6 игр.
- SettingsScreen — общие настройки (сейчас: полный сброс через factoryReset()).
  factoryReset() удаляет ВСЕ БД приложения через context.databaseList() —
  включая free_records.db.

**Статистика:**
- StatsAggregates.kt, StatsScreen.kt, StatsMenuScreen.kt
- StatsScreen принимает `freePlayRecordsRepository: FreePlayRecordsRepository?`.
  При сбросе статистики Крикета вызывается repository.clearAll() + clearCricket(),
  при сбросе 501 — game501Repo.clearAll() + clear501().

**Игровые:**
- ui/game501/, ui/cricket/, ui/sector/, ui/biground/, ui/scoreset/,
  ui/aroundclock/ — по 3 экрана (Setup/Game/Stats) в каждой.

### ui/training/ — тренировочный раздел

- TrainingMenuScreen.kt — главный экран тренировки.
- TrainingSettingsScreen.kt — 4 переключателя блоков + полный сброс.
- TrainingProgressScreen.kt — уровень, потенциал, 4 параметра, темп.
- TrainingWarmupScreen.kt — 4 фазы разминки с таймером.
- TrainingBlockScreen.kt — список упражнений блока.
- TrainingSummaryScreen.kt — итоги блока.
- TrainingTimer.kt — TrainingTimerOverlay.

---

## 5. АРХИТЕКТУРА

### Навигация (MainActivity.kt)
Ручная, без Navigation Compose.
- var stage: welcome / onboarding / loading / main
- var screen: экран внутри stage=main
- PARENT_SCREEN — карта для системной «Назад»

### Тренировочные экраны
training, training_settings, training_progress, training_warmup,
training_block, training_exercise, training_summary,
training_block_exercise, training_extra_game.

### TRAINING_SCREENS (где таймер)
training_warmup, training_block, training_exercise,
training_block_exercise, training_extra_game.

### TrainingBlockPhase (enum)
WARMUP, MAIN, PSYCHOLOGY, STRENGTH, FINISHED.

### Данные
Room. Для каждой игры — отдельная база. Тренировка — TrainingDatabase (v4).
Рекорды свободной игры — FreePlayRecordsDatabase (v1).

### Незавершённые партии
Только 501 и Крикет. isFinished, stateBlob, lastUpdateMillis.

### Сохранение в БД — через scope MainActivity

### Бросок в Bull при розыгрыше
pRed = (0.05 + (lvl - 1) * 0.027) * 0.8
pGreen = (0.25 + (lvl - 1) * 0.013) * 0.8

---

## 6. СИСТЕМА РЕКОРДОВ

### Что считается рекордом

| Игра | Рекорд |
|---|---|
| Кругосветка × 3 режима | Отдельно по каждому: минимум дротиков |
| Сектор 20 | Максимум очков |
| Большой раунд | Максимум очков |
| Набор очков | Максимум очков |
| **501** | **Лег: минимум дротиков. Матч: лучший PPR.** |
| **Крикет** | **Лег: лучший MPR. Матч: лучший MPR.** |

### Где хранятся

**Старые игры (Кругосветка, Сектор, Большой, Набор):**
Рекорд НЕ хранится отдельно — вычисляется из игровых партий при чтении.
Сброс статистики чистит партии → рекорд пропадает.

**Новые игры (501, Крикет):**
Отдельные таблицы, потому что партии — это blob.

- **Тренировка** → таблица `training_records` в TrainingDatabase (v4).
  Ключи: game501_leg, game501_match, cricket_leg, cricket_match.
  Поля: bestInt не используется (для 501/Крикета), bestFloat = PPR/MPR,
  bestDarts (для лега 501) = дротики, extraInt = % удвоений для матча 501.
- **Свободная игра** → таблица `free_records` в FreePlayRecordsDatabase (v1).
  Те же ключи. Поля: bestInt (дротики лега 501), bestFloat (PPR/MPR),
  extraInt (% удвоений).

### Логика фиксации рекордов 501 и Крикета

- **Парные игры (isPairGame = true) — не фиксируются.**
- 1×1 (с ботом или с человеком) — фиксируются.
- «Свой» игрок определяется по имени из `SettingsStorage.getPlayerName()`.
  Если имени в партии нет (играют друзья) — не фиксируем.
- Только **выигранные** леги и **выигранные** матчи.
- **Рекорд лега** — сразу после лега.
  У 501 — при `game.legHistory.size` меняется.
  У Крикета — при нажатии «Продолжить» в диалоге лега
  (там ещё дротики корректируются).
- **Рекорд матча** — после окончания матча.
  У 501 — при «Продолжить» в финальном диалоге.
  У Крикета — то же.
- Салюты показываются **последовательно**: сначала за лег, потом за матч.
- Салют матча — **только после** него вызывается `onGameFinish(game, id)`.

### Порог «почти рекорд»
- Очки/PPR/MPR (больше = лучше): (рекорд − результат) / рекорд × 100% ≤ 10%.
- Дротики (меньше = лучше): (результат − рекорд) / рекорд × 100% ≤ 10%.
- Текст: «До нового рекорда не хватило N дротиков / X.X PPR / X.X MPR».
  С правильным склонением: 1 дротик, 2–4 дротика, 5+ дротиков.

### Форматы цифр
- PPR/MPR — 1 знак после запятой (65.3, 3.5).
- Дротики — целое (12).
- % удвоений — целое (32%).

### Праздничный диалог — три состояния
1. Новый рекорд → 🏆 крупный + салют + конфетти.
2. Почти рекорд → 💪 голубой фон, без салюта, «не хватило N».
3. Первый результат → тихое «Твой результат сохранён».

### Сбросы рекордов
- **Тренировочные** рекорды (включая 501/Крикет) — через TrainingSettingsScreen
  (кнопка ⚙ → полный сброс) → clearAllTrainingData().
- **Свободная игра 501/Крикет** — через StatsScreen (кнопка «Сбросить статистику»
  на вкладке 501 или Крикет) → clear501() / clearCricket().
- **Полный заводской сброс** — SettingsScreen → factoryReset() → удаляет все БД.
- **Старые игры** (Кругосветка, Сектор, Большой, Набор) — рекорды исчезают
  вместе с партиями при их удалении.

---

## 7. ТАБЛИЦА БОТОВ 501 (PPR и удвоения)

1. Новичок — 22–26 / 5–8%
2. Ученик — 28–32 / 8–10%
3. Любитель — 33–37 / 9–12%
4. Уверенный — 38–42 / 11–14%
5. Опытный — 43–47 / 14–17%
6. Практик — 49–53 / 16–19%
7. Разрядник — 52–55 / 17–20%
8. Турнирный — 55–59 / 18–21%
9. Сильный — 61–66 / 21–24%
10. Крепкий — 66–71 / 24–27%
11. КМС — 71–77 / 26–29%
12. Почти мастер — 80–86 / 29–33%
13. Мастер — 87–93 / 32–35%
14. Чемпион — 93–98 / 36–40%
15. Профи — 99–103 / 40–44%
16. Легенда — 104–110 / 44–48%

**Привязки к разрядам:**
7 (Разрядник) = I взрослый разряд
11 (КМС) = КМС
13 (Мастер) = МС
16 (Легенда) = уровень Люка Литтлера

---

## 8. ТАБЛИЦА БОТОВ КРИКЕТА (MPR, десятые доли)

1. Новичок — 5–8
2. Ученик — 8–11
3. Любитель — 11–14
4. Уверенный — 14–17
5. Опытный — 17–20
6. Практик — 20–22
7. Разрядник — 22–25
8. Турнирный — 25–28
9. Сильный — 28–31
10. Крепкий — 31–34
11. КМС — 34–37
12. Почти мастер — 37–40
13. Мастер — 40–43
14. Чемпион — 43–47
15. Профи — 47–52
16. Легенда — 52–60

---

## 9. ТРЕНИРОВОЧНЫЙ РАЗДЕЛ — КОНЦЕПЦИЯ

### Суть
Не инструмент, а тренер. Даёт программу на день, подстраивается под игрока.

### ЧЕТЫРЕ параметра
1. Набор очков — 501 (первые 9), Сектор 20/19, Набор очков
2. Точность в сектор — Кругосветка-Сектора, Большой раунд
3. Удвоения — 501 (удвоения), Кругосветка-Удвоения
4. Утроения — Кругосветка-Утроения, Большой раунд

### Логика уровня — два комплекса
- Комплекс 1: 501 (PPR).
- Комплекс 2: упражнения (среднее 4 параметров, вверх).
- Итоговый = максимум из двух.
- Расхождение = сигнал психологии.

### Потенциал = максимум из 4 параметров.
### Экзамен = каждые 10 тренировок.
### Тренды = по 3 последним тренировкам.
### Средний темп = по 5 последним записям с durationSeconds > 0.

### Правило «не более 50% на слабое место».

### Правило 3 часов
Выход → сохранение. Возврат <3ч → диалог «Продолжить / Начать заново».

### Адаптивная сложность ботов
Первая партия — на 1 уровень слабее. Выиграл → +1, проиграл → −1.

### Кругосветка-Утроения
- Уровень <9 (Сильный) → укороченная T10–T20 + Bull (12 целей).
- Уровень ≥9 → полная T1–T20 + Bull (21 цель).

### Кругосветка-Удвоения
Все D1–D20 + Bull (21 цель). Не сокращать.

### Баланс тяжести
Максимум 1 тяжёлое упражнение за тренировку. Первые 5 тренировок —
вводный режим. Игры (501, Крикет) разбавляют тяжёлые.

### Структура тренировки
- Разминка (4 фазы)
- Основная часть (до 8 упражнений)
- Психология (3 заглушки)
- Силовая (5 заглушек)

### Свободная игра vs Тренировка
Раздельная статистика. Сравнение = индикатор психологического давления.

---

## 10. ТАБЛИЦА НОРМАТИВОВ (DartsNorms.kt)

Официальные точки (Минспорт РФ, мужские):
- I взрослый: Сектор 20 = 600, Набор = 660, Большой = 660
- КМС: 720 / 760 / 750
- МС: 780 / 820 / 810 (примерно)
- Легенда: 900 / 940 / 930 (примерно)

Удвоения: Разрядник 18%, КМС 27%, Мастер 33%, Легенда 46%
Утроения: Разрядник 17%, КМС 26%, Мастер 32%, Легенда 44%

---

## 11. ЧТО УЖЕ СДЕЛАНО

### Инфраструктура
Проект, GitHub Actions, ключи подписи.

### Базовые экраны
Welcome, Onboarding, Loading, MainMenu, Settings, GameSelect.

### Статистика
StatsMenuScreen (сетка 2×3), StatsScreen (вкладки, графики, чекауты).

### Игровые режимы
501, Крикет (исправлены 2 бага), Сектор, Большой раунд,
Набор очков, Кругосветка (3 режима).

### Тренировочный раздел — ПОЛНОСТЬЮ РАБОТАЕТ
- Модели, БД (версия 4), DartsNorms, LevelCalculator, WeaknessDetector.
- TrainingBlocksStorage, TrainingStateStorage, TrainingBotLevelStorage.
- TrainingStatsAnalyzer, TrainingPlanner.
- TrainingMenuScreen (⚙ + 📊), TrainingSettingsScreen (со сбросом),
  TrainingProgressScreen, TrainingWarmupScreen, TrainingBlockScreen,
  TrainingSummaryScreen, TrainingTimerOverlay.
- 8 упражнений основной части пишут в тренировочную БД.
- 501 и Крикет в тренировке с адаптивным ботом.
- Автосохранение тренировки (правило 3 часа).

### Система рекордов — полностью готова (14.10.2026)
- **Готово:** Кругосветка ×3, Сектор 20, Большой раунд, Набор очков
  (через игровые БД).
- **Готово:** 501 и Крикет (лег + матч) в тренировке и свободной игре
  (через training_records и free_records).
- Раздельные рекорды (тренировка / свободная игра).
- Праздничный диалог с салютом и конфетти — CelebrationOverlay.kt.
- Все экраны (Sector, BigRound, ScoreSet, AroundClock, Game501, Cricket)
  подключены.
- Порог «почти рекорд» ≤10%.
- Сброс статистики чистит соответствующие рекорды.

### Свободная игра «Просто поиграть»
Все 6 игр с полной статистикой.

---

## 12. БЭКЛОГ (задачи на будущее)

1. **Показатели 501 на кнопке «Тренировка»** — PPR + % удвоений.
   Только тренировочные данные. Отложено «напоследок».
2. **Раздел «О программе»** в настройках — пояснения для игрока.
3. **Реальные психологические упражнения** — вместо заглушек.
4. **Реальные силовые упражнения** — вместо заглушек.
5. **Кнопка «Статистика» на главном экране** — сделать отдельной
   большой карточкой, как «Тренировка» и «Просто поиграть».
6. **Диагностическое первое упражнение** — для не-новичков.
7. **Облегчённые игры для новичков.**
8. **Экран статистики тренировок** — прогресс по 4 параметрам.
9. **Полное сохранение середины упражнения** (не только позиции в цепочке).

---

## 13. ИЗВЕСТНЫЕ БАГИ И ИСПРАВЛЕНИЯ

- **10.10.2026** — Крикет: бот не переключал сектор в подходе.
- **10.10.2026** — Крикет: бот побеждал без учёта счёта.
- **10.10.2026** — MPR ботов Крикета были завышены.
- **11.10.2026** — диалог времени повторно открывался после настроек.
- **12.10.2026** — matchDartsThrown vs matchDarts в Player501.
- **13.10.2026** — рекорды не сбрасывались при полном сбросе.
- **14.10.2026** — при сбросе статистики 501/Крикета рекорды свободной игры
  не стирались. Исправлено: добавлены clear501() / clearCricket().

---

## 14. ПРАВИЛА ПО КОДУ

- Язык интерфейса — русский, комментарии — на русском.
- Форматирование — 4 пробела, KDoc где уместно.
- При замене файла целиком — копировать ВСЁ.
- При изменении @Entity — версия +1, миграция с ALTER TABLE.
  НЕ использовать fallbackToDestructiveMigration().
- Все модели тренировок — в data/training/, экраны — в ui/training/,
  общие компоненты — в ui/common/, рекорды свободной игры — в data/freerecords/.
- При обновлении AI_CONTEXT — НЕ использовать вложенные блоки кода.

---

## 15. ДОКУМЕНТАЦИЯ В docs/

00_Оглавление, 01_Концепция, 02_Анкета, 03_Режимы_тренировок,
04_Структура_тренировки, 05_Уровни_и_разряды, 06_Удвоения, 07_Набор,
08_Сектора, 09_Чекауты, 10_Силовые, 11_Свободная_игра, 12_Статистика,
13_Подсказки, 14_Тестирование, 15_План_разработки,
16_Контрольная_тренировка, 17_Психология, 18_Разминка, 19_Игра_501,
20_Крикет.

ВАЖНО: при вопросах по концепции — сначала смотри в этих файлах.

---

## 16. ЧАСТЫЕ ОШИБКИ АВТОРА

1. Код попадает не в тот файл. Проверяй первую строку package.
2. Копируется не весь файл → Unresolved reference.
3. Забыл сохранить — сборка не меняется.
4. Слишком много ошибок в логе. Первых 30–40 строк достаточно.
5. Один файл в одном сообщении — критично.
6. При копировании больших файлов — иногда обрывается.

---

## 17. ЖУРНАЛ ИЗМЕНЕНИЙ

- **2026-10-07** — создан. Структура, архитектура, таблица ботов.
- **2026-10-08** — статистика, незавершённые партии.
- **2026-10-09** — StatsAggregates, размеры шрифтов, Bull.
- **2026-10-10** — Тренировочный раздел. 4 параметра. 2 комплекса.
  TrainingModels, TrainingDatabase (v1), DartsNorms, LevelCalculator,
  WeaknessDetector, TrainingPlanner.
- **2026-10-11** — TrainingMenuScreen, TrainingSettingsScreen,
  TrainingProgressScreen, TrainingWarmupScreen, TrainingBlockScreen,
  TrainingSummaryScreen, TrainingTimerOverlay.
  TrainingBlocksStorage, TrainingStateStorage, TrainingBotLevelStorage,
  TrainingStatsAnalyzer.
  8 упражнений основной части.
  Кругосветка-Утроения сокращена до уровня <9.
  Правило 3 часов.
- **2026-10-12** — durationSeconds в ExerciseResult, миграция БД 1→2.
  TrainingPlanner: учёт completedTrainingCount, вводный режим (5 тренировок),
  максимум 1 тяжёлое.
- **2026-10-13** — Система рекордов:
  training_records (миграция 2→3).
  CelebrationOverlay.kt (ракеты + конфетти + салют).
  SectorGameScreen, BigRoundGameScreen, ScoreSetGameScreen,
  AroundClockGameScreen — праздничный диалог.
  Раздельные рекорды (тренировка / свободная игра).
  Формула «почти рекорд» ≤10%.
- **2026-10-14** — Рекорды 501 и Крикета (лег + матч).
  TrainingDatabase v4: миграция 3→4, поля bestFloat и extraInt.
  Методы update501LegRecord, update501MatchRecord,
  updateCricketLegRecord, updateCricketMatchRecord.
  Новая БД: FreePlayRecordsDatabase (v1) + FreePlayRecordsRepository.
  Game501Screen.kt и CricketGameScreen.kt:
    - параметры freePlayRecordsRepository, isTrainingMode, trainingRepository
    - фиксация рекордов (только выигранные леги и матчи)
    - парные игры не фиксируются
    - «свой» игрок по SettingsStorage.getPlayerName()
    - салюты за лег и матч последовательно
  MainActivity.kt: создан freePlayRecordsRepository, передан в Game501Screen,
  CricketGameScreen, StatsScreen.
  StatsScreen.kt: при сбросе статистики Крикета/501 — clearCricket()/clear501().
