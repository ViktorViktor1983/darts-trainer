package com.lodkin.dartstrainer.data.game501

// Любимые даблы ботов по уровням.
// Используется Game501BotAI.chooseTarget для приоритизации путей чекаута:
// если из текущего остатка доступно несколько путей, бот предпочтёт тот,
// что закрывается его любимым даблом.
//
// На точность это НЕ влияет (p_double не меняется). На PPR — тоже.
// Это делает поведение узнаваемым: как у реальных игроков разных уровней.
object BotPreferences {

    // Возвращает список любимых даблов (sector: 1..20, 25 = Bull D25).
    // Порядок = приоритет: первый в списке самый предпочитаемый.
    fun favouriteDoubles(botLevel: Int): List<Int> {
        val lvl = botLevel.coerceIn(1, 16)
        return when (lvl) {
            // Новички — только самые простые и популярные.
            1 -> listOf(20, 16)
            2 -> listOf(20, 16)
            3 -> listOf(16, 20)
            4 -> listOf(16, 20, 8)

            // Любители — добавляется D8, D10.
            5 -> listOf(16, 20, 8, 10)
            6 -> listOf(16, 20, 8, 10)
            7 -> listOf(20, 16, 10, 8)
            8 -> listOf(16, 20, 10, 8, 12)

            // Полупрофи — широкий выбор.
            9 -> listOf(16, 20, 8, 10, 12, 18)
            10 -> listOf(20, 16, 8, 12, 10, 18)
            11 -> listOf(16, 20, 8, 12, 10, 18, 4)
            12 -> listOf(16, 20, 8, 12, 10, 18, 4, 19)

            // Элита — все даблы 2..20 в порядке предпочтения.
            13 -> buildFullList(listOf(16, 20, 8, 12, 18, 10, 4, 19))
            14 -> buildFullList(listOf(20, 16, 18, 12, 10, 8, 4, 19))
            15 -> buildFullList(listOf(20, 18, 16, 12, 10, 8, 4, 19))
            16 -> buildFullList(listOf(20, 18, 16, 12, 10, 8, 4, 19))

            else -> listOf(16, 20)
        }
    }

    // Строит список: приоритетные даблы первыми, потом остальные по порядку 2..20.
    private fun buildFullList(priority: List<Int>): List<Int> {
        val rest = (2..20).filter { it !in priority }
        return priority + rest
    }

    // Проверка: является ли дабл-цель любимой для этого уровня.
    fun isFavouriteDouble(botLevel: Int, sector: Int): Boolean {
        return sector in favouriteDoubles(botLevel)
    }

    // Приоритет дабла: 0 = самый любимый, больше = менее предпочитаемый.
    // Если дабл вне списка — возвращает Int.MAX_VALUE (самый низкий приоритет).
    fun doublePriority(botLevel: Int, sector: Int): Int {
        val idx = favouriteDoubles(botLevel).indexOf(sector)
        return if (idx < 0) Int.MAX_VALUE else idx
    }
}
