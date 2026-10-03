package com.lodkin.dartstrainer.data.game501

// Один дротик в пути чекаута
data class CheckoutThrow(
    val sector: Int,        // 1..20, 25 (Bull)
    val multiplier: Int     // 1=S, 2=D, 3=T
)

// Вариант пути чекаута
data class CheckoutPath(
    val label: String,
    val throws: List<CheckoutThrow>
)

object CheckoutTable {

    private fun parseThrow(s: String): CheckoutThrow {
        return when (s) {
            "BULL" -> CheckoutThrow(25, 2)
            "25" -> CheckoutThrow(25, 1)
            else -> {
                val mult = when (s[0]) {
                    'S' -> 1
                    'D' -> 2
                    'T' -> 3
                    else -> 1
                }
                val num = s.substring(1).toIntOrNull() ?: 0
                CheckoutThrow(num, mult)
            }
        }
    }

    private fun path(label: String, vararg s: String): CheckoutPath =
        CheckoutPath(label, s.map { parseThrow(it) })

    val TABLE: Map<Int, List<CheckoutPath>> = mapOf(
        2 to listOf(path("Основной", "D1")),
        3 to listOf(path("Основной", "S1", "D1")),
        4 to listOf(path("Основной", "D2")),
        5 to listOf(path("Основной", "S1", "D2")),
        6 to listOf(path("Основной", "D3")),
        7 to listOf(path("Основной", "S3", "D2")),
        8 to listOf(path("Основной", "D4")),
        9 to listOf(path("Основной", "S1", "D4")),
        10 to listOf(path("Основной", "D5")),
        11 to listOf(path("Основной", "S3", "D4")),
        12 to listOf(path("Основной", "D6")),
        13 to listOf(path("Основной", "S5", "D4")),
        14 to listOf(path("Основной", "D7")),
        15 to listOf(path("Основной", "S7", "D4")),
        16 to listOf(path("Основной", "D8")),
        17 to listOf(path("Основной", "S1", "D8")),
        18 to listOf(path("Основной", "D9")),
        19 to listOf(path("Основной", "S3", "D8")),
        20 to listOf(path("Основной", "D10")),
        21 to listOf(path("Основной", "S5", "D8")),
        22 to listOf(path("Основной", "D11")),
        23 to listOf(path("Основной", "S7", "D8")),
        24 to listOf(path("Основной", "D12")),
        25 to listOf(
            path("Основной", "S1", "D12"),
            path("Альтернативный", "S5", "D10")
        ),
        26 to listOf(path("Основной", "D13")),
        27 to listOf(path("Основной", "S3", "D12")),
        28 to listOf(path("Основной", "D14")),
        29 to listOf(path("Основной", "S13", "D8")),
        30 to listOf(path("Основной", "D15")),
        31 to listOf(path("Основной", "S15", "D8")),
        32 to listOf(path("Основной", "D16")),
        33 to listOf(path("Основной", "S1", "D16")),
        34 to listOf(path("Основной", "D17")),
        35 to listOf(path("Основной", "S3", "D16")),
        36 to listOf(path("Основной", "D18")),
        37 to listOf(path("Основной", "S5", "D16")),
        38 to listOf(path("Основной", "D19")),
        39 to listOf(path("Основной", "S7", "D16")),
        40 to listOf(path("Основной", "D20")),
        41 to listOf(path("Основной", "S9", "D16")),
        42 to listOf(path("Основной", "S10", "D16")),
        43 to listOf(path("Основной", "S3", "D20")),
        44 to listOf(path("Основной", "S12", "D16")),
        45 to listOf(path("Основной", "S13", "D16")),
        46 to listOf(path("Основной", "S6", "D20")),
        47 to listOf(path("Основной", "S15", "D16")),
        48 to listOf(path("Основной", "S8", "D20")),
        49 to listOf(path("Основной", "S9", "D20")),
        50 to listOf(path("Основной", "BULL")),
        51 to listOf(path("Основной", "S11", "D20")),
        52 to listOf(path("Основной", "S12", "D20")),
        53 to listOf(path("Основной", "S13", "D20")),
        54 to listOf(path("Основной", "S14", "D20")),
        55 to listOf(path("Основной", "S15", "D20")),
        56 to listOf(path("Основной", "S16", "D20")),
        57 to listOf(path("Основной", "S17", "D20")),
        58 to listOf(path("Основной", "S18", "D20")),
        59 to listOf(path("Основной", "S19", "D20")),

        60 to listOf(path("Основной", "S20", "D20")),
        61 to listOf(
            path("Основной", "T15", "D8"),
            path("Промах T15", "S15", "S6", "D20"),
            path("Альтернативный", "25", "D18"),
            path("Запасной", "T11", "D14"),
            path("Промах T11", "S11", "S10", "D20")
        ),
        62 to listOf(
            path("Основной", "T10", "D16"),
            path("Промах T10", "S10", "S12", "D20"),
            path("Альтернативный", "T18", "D4"),
            path("Промах T18", "S18", "S4", "D20")
        ),
        63 to listOf(
            path("Основной", "T13", "D12"),
            path("Промах T13", "S13", "S10", "D20"),
            path("Альтернативный", "T17", "D6"),
            path("Промах T17", "S17", "S6", "D20")
        ),
        64 to listOf(
            path("Основной", "T16", "D8"),
            path("Промах T16", "S16", "S8", "D20"),
            path("Альтернативный", "T8", "D20"),
            path("Промах T8", "S8", "S16", "D20")
        ),
        65 to listOf(
            path("Основной", "T19", "D4"),
            path("Промах T19", "S19", "S6", "D20"),
            path("Альтернативный", "25", "D20"),
            path("Запасной", "T11", "D16"),
            path("Промах T11", "S11", "S14", "D20")
        ),
        66 to listOf(
            path("Основной", "T14", "D12"),
            path("Промах T14", "S14", "S12", "D20"),
            path("Альтернативный", "T10", "D18"),
            path("Промах T10", "S10", "S16", "D20")
        ),
        67 to listOf(
            path("Основной", "T17", "D8"),
            path("Промах T17", "S17", "S10", "D20"),
            path("Альтернативный", "T9", "D20"),
            path("Промах T9", "S9", "S18", "D20")
        ),
        68 to listOf(
            path("Основной", "T20", "D4"),
            path("Промах T20", "S20", "S8", "D20"),
            path("Альтернативный", "T16", "D10"),
            path("Промах T16", "S16", "S12", "D20")
        ),
        69 to listOf(
            path("Основной", "T19", "D6"),
            path("Промах T19", "S19", "S10", "D20"),
            path("Альтернативный", "T15", "D12"),
            path("Промах T15", "S15", "S14", "D20")
        ),
        70 to listOf(
            path("Основной", "T18", "D8"),
            path("Промах T18", "S18", "S12", "D20"),
            path("Альтернативный", "T10", "D20"),
            path("Промах T10", "S10", "S20", "D20")
        ),
        71 to listOf(
            path("Основной", "T13", "D16"),
            path("Промах T13", "S13", "S18", "D20"),
            path("Альтернативный", "T17", "D10"),
            path("Промах T17", "S17", "S14", "D20")
        ),
        72 to listOf(
            path("Основной", "T16", "D12"),
            path("Промах T16", "S16", "S16", "D20"),
            path("Альтернативный", "T12", "D18"),
            path("Промах T12", "S12", "S20", "D20")
        ),
        73 to listOf(
            path("Основной", "T19", "D8"),
            path("Промах T19", "S19", "S14", "D20"),
            path("Альтернативный", "T15", "D14"),
            path("Промах T15", "S15", "S18", "D20")
        ),
        74 to listOf(
            path("Основной", "T14", "D16"),
            path("Промах T14", "S14", "S20", "D20"),
            path("Альтернативный", "T18", "D10"),
            path("Промах T18", "S18", "S16", "D20")
        ),
        75 to listOf(
            path("Основной", "T17", "D12"),
            path("Промах T17", "S17", "S18", "D20"),
            path("Альтернативный", "T13", "D18"),
            path("Промах T13", "S13", "T14", "D10")
        ),
        76 to listOf(
            path("Основной", "T20", "D8"),
            path("Промах T20", "S20", "S16", "D20"),
            path("Альтернативный", "T16", "D14"),
            path("Промах T16", "S16", "S20", "D20")
        ),
        77 to listOf(
            path("Основной", "T19", "D10"),
            path("Промах T19", "S19", "S18", "D20"),
            path("Альтернативный", "T15", "D16"),
            path("Промах T15", "S15", "T14", "D10")
        ),
        78 to listOf(
            path("Основной", "T18", "D12"),
            path("Промах T18", "S18", "S20", "D20"),
            path("Альтернативный", "T14", "D18"),
            path("Промах T14", "S14", "T16", "D8")
        ),
        79 to listOf(
            path("Основной", "T19", "D11"),
            path("Промах T19", "S19", "S20", "D20"),
            path("Альтернативный", "T13", "D20"),
            path("Промах T13", "S13", "T14", "D12")
        ),
        80 to listOf(
            path("Основной", "T20", "D10"),
            path("Промах T20", "S20", "S20", "D20"),
            path("Альтернативный", "T16", "D16"),
            path("Промах T16", "S16", "T16", "D8")
        ),
        81 to listOf(
            path("Основной", "T19", "D12"),
            path("Промах T19", "S19", "T14", "D10"),
            path("Альтернативный", "T15", "D18"),
            path("Промах T15", "S15", "T14", "D12")
        ),
        82 to listOf(
            path("Основной", "T14", "D20"),
            path("Промах T14", "S14", "T20", "D4"),
            path("Альтернативный", "BULL", "D16")
        ),
        83 to listOf(
            path("Основной", "T17", "D16"),
            path("Промах T17", "S17", "T14", "D12")
        ),
        84 to listOf(
            path("Основной", "T20", "D12"),
            path("Промах T20", "S20", "T16", "D8"),
            path("Альтернативный", "T16", "D18"),
            path("Промах T16", "S16", "T20", "D4")
        ),
        85 to listOf(
            path("Основной", "T15", "D20"),
            path("Промах T15", "S15", "T18", "D8"),
            path("Альтернативный", "T19", "D14"),
            path("Промах T19", "S19", "T14", "D12")
        ),
        86 to listOf(
            path("Основной", "T18", "D16"),
            path("Промах T18", "S18", "T20", "D4")
        ),
        87 to listOf(
            path("Основной", "T17", "D18"),
            path("Промах T17", "S17", "T18", "D8")
        ),
        88 to listOf(
            path("Основной", "T16", "D20"),
            path("Промах T16", "S16", "T16", "D12"),
            path("Альтернативный", "T20", "D14"),
            path("Промах T20", "S20", "T20", "D4")
        ),
        89 to listOf(
            path("Основной", "T19", "D16"),
            path("Промах T19", "S19", "T18", "D8"),
            path("Альтернативный", "T17", "D19"),
            path("Промах T17", "S17", "T16", "D12")
        ),
        90 to listOf(
            path("Основной", "T20", "D15"),
            path("Промах T20", "S20", "T18", "D8"),
            path("Альтернативный", "T18", "D18"),
            path("Промах T18", "S18", "T16", "D12")
        ),
        91 to listOf(
            path("Основной", "T17", "D20"),
            path("Промах T17", "S17", "T14", "D16"),
            path("Альтернативный", "T19", "D17"),
            path("Промах T19", "S19", "T16", "D12")
        ),
        92 to listOf(
            path("Основной", "T20", "D16"),
            path("Промах T20", "S20", "T16", "D12")
        ),
        93 to listOf(
            path("Основной", "T19", "D18"),
            path("Промах T19", "S19", "T14", "D16")
        ),
        94 to listOf(
            path("Основной", "T18", "D20"),
            path("Промах T18", "S18", "T20", "D8")
        ),
        95 to listOf(
            path("Основной", "T19", "D19"),
            path("Промах T19", "S19", "T20", "D8")
        ),
        96 to listOf(
            path("Основной", "T20", "D18"),
            path("Промах T20", "S20", "T20", "D8")
        ),
        97 to listOf(
            path("Основной", "T19", "D20"),
            path("Промах T19", "S19", "T18", "D12")
        ),
        98 to listOf(
            path("Основной", "T20", "D19"),
            path("Промах T20", "S20", "T18", "D12")
        ),
        99 to listOf(
            path("Основной", "T19", "S10", "D16"),
            path("Промах T19", "S19", "T20", "D10"),
            path("Альтернативный", "T20", "S7", "D16"),
            path("Промах T20", "S20", "T19", "D11")
        ),
        100 to listOf(
            path("Основной", "T20", "D20"),
            path("Промах T20", "S20", "T20", "D10")
        ),
        101 to listOf(
            path("Основной", "T17", "BULL"),
            path("Промах T17", "S17", "T20", "D12"),
            path("Альтернативный", "T20", "S9", "D16"),
            path("Промах T20", "S20", "T19", "D12")
        ),
        102 to listOf(
            path("Основной", "T20", "S10", "D16"),
            path("Промах T20", "S20", "T14", "D20")
        ),
        103 to listOf(
            path("Основной", "T20", "S11", "D16"),
            path("Промах T20", "S20", "T17", "D16")
        ),
        104 to listOf(
            path("Основной", "T20", "S12", "D16"),
            path("Промах T20", "S20", "T20", "D12"),
            path("Альтернативный", "T18", "S18", "D16"),
            path("Промах T18", "S18", "T18", "D16")
        ),
        105 to listOf(
            path("Основной", "T20", "S13", "D16"),
            path("Промах T20", "S20", "T15", "D20")
        ),
        106 to listOf(
            path("Основной", "T20", "S14", "D16"),
            path("Промах T20", "S20", "T18", "D16")
        ),
        107 to listOf(
            path("Основной", "T20", "S15", "D16"),
            path("Промах T20", "S20", "T17", "D18"),
            path("Альтернативный", "T19", "S18", "D16"),
            path("Промах T19", "S19", "T16", "D20")
        ),
        108 to listOf(
            path("Основной", "T20", "S16", "D16"),
            path("Промах T20", "S20", "T16", "D20")
        ),
        109 to listOf(
            path("Основной", "T20", "S17", "D16"),
            path("Промах T20", "S20", "T19", "D16"),
            path("Альтернативный", "T19", "S20", "D16"),
            path("Промах T19", "S19", "T20", "D15")
        ),
        110 to listOf(
            path("Основной", "T20", "S18", "D16"),
            path("Промах T20", "S20", "T20", "D15"),
            path("Альтернативный", "T20", "BULL")
        ),
        111 to listOf(
            path("Основной", "T20", "S19", "D16"),
            path("Промах T20", "S20", "T17", "D20")
        ),
        112 to listOf(
            path("Основной", "T20", "S20", "D16"),
            path("Промах T20", "S20", "T20", "D16"),
            path("Альтернативный", "T20", "T12", "D8")
        ),
        113 to listOf(
            path("Основной", "T20", "S13", "D20"),
            path("Промах T20", "S20", "T19", "D18"),
            path("Альтернативный", "T20", "T13", "D7")
        ),
        114 to listOf(
            path("Основной", "T20", "S14", "D20"),
            path("Промах T20", "S20", "T18", "D20"),
            path("Альтернативный", "T20", "T14", "D6")
        ),
        115 to listOf(
            path("Основной", "T20", "S15", "D20"),
            path("Промах T20", "S20", "T19", "D19"),
            path("Альтернативный", "T20", "T15", "D5")
        ),
        116 to listOf(
            path("Основной", "T20", "S16", "D20"),
            path("Промах T20", "S20", "T20", "D18"),
            path("Альтернативный", "T20", "T16", "D4")
        ),
        117 to listOf(
            path("Основной", "T20", "S17", "D20"),
            path("Промах T20", "S20", "T19", "D20"),
            path("Альтернативный", "T20", "T17", "D3")
        ),
        118 to listOf(
            path("Основной", "T20", "S18", "D20"),
            path("Промах T20", "S20", "T20", "D19"),
            path("Альтернативный", "T20", "T18", "D2")
        ),
        119 to listOf(
            path("Основной", "T19", "T12", "D13"),
            path("Промах T19", "S19", "T20", "D20"),
            path("Альтернативный", "T20", "S19", "D20")
        ),
        120 to listOf(
            path("Основной", "T20", "S20", "D20"),
            path("Промах T20", "S20", "T20", "D20")
        ),
        121 to listOf(
            path("Основной", "T20", "T11", "D14"),
            path("Промах T20", "S20", "T17", "BULL"),
            path("Альтернативный", "T17", "T20", "D5"),
            path("Промах T17", "S17", "T18", "BULL")
        ),
        122 to listOf(
            path("Основной", "T18", "T18", "D7"),
            path("Промах T18", "S18", "T18", "BULL"),
            path("Альтернативный", "T20", "T18", "D4"),
            path("Запасной", "S18", "T18", "BULL")
        ),
        123 to listOf(
            path("Основной", "T19", "T16", "D9"),
            path("Промах T19", "S19", "T18", "BULL"),
            path("Альтернативный", "T20", "T13", "D12")
        ),
        124 to listOf(
            path("Основной", "T20", "T16", "D8"),
            path("Промах T20", "S20", "T18", "BULL"),
            path("Альтернативный", "T20", "T20", "D2")
        ),
        125 to listOf(
            path("Основной", "T20", "T19", "D4"),
            path("Промах T20", "S20", "T20", "S13", "D16"),
            path("Альтернативный", "BULL", "T17", "D12"),
            path("Запасной", "25", "T20", "D20")
        ),
        126 to listOf(
            path("Основной", "T19", "T19", "D6"),
            path("Промах T19", "S19", "T19", "BULL"),
            path("Альтернативный", "T20", "T16", "D9")
        ),
        127 to listOf(
            path("Основной", "T20", "T17", "D8"),
            path("Промах T20", "S20", "T19", "BULL")
        ),
        128 to listOf(
            path("Основной", "T18", "T18", "D10"),
            path("Промах T18", "S18", "T20", "BULL"),
            path("Альтернативный", "T20", "T20", "D4")
        ),
        129 to listOf(
            path("Основной", "T19", "T16", "D12"),
            path("Промах T19", "S19", "T20", "BULL"),
            path("Альтернативный", "T19", "T20", "D6")
        ),
        130 to listOf(
            path("Основной", "T20", "T20", "D5"),
            path("Промах T20", "S20", "T20", "BULL"),
            path("Альтернативный", "T20", "T18", "D8"),
            path("Запасной", "T19", "T19", "D8")
        ),
        131 to listOf(path("Основной", "T20", "T13", "D16")),
        132 to listOf(
            path("Основной", "BULL", "BULL", "D16"),
            path("Альтернативный", "T20", "T16", "D12")
        ),
        133 to listOf(path("Основной", "T20", "T19", "D8")),
        134 to listOf(path("Основной", "T20", "T14", "D16")),
        135 to listOf(
            path("Основной", "BULL", "T15", "D20"),
            path("Альтернативный", "T20", "T17", "D12")
        ),
        136 to listOf(path("Основной", "T20", "T20", "D8")),
        137 to listOf(
            path("Основной", "T20", "T19", "D10"),
            path("Альтернативный", "T19", "T18", "D13")
        ),
        138 to listOf(path("Основной", "T20", "T18", "D12")),
        139 to listOf(path("Основной", "T20", "T13", "D20")),
        140 to listOf(path("Основной", "T20", "T20", "D10")),
        141 to listOf(
            path("Основной", "T20", "T19", "D12"),
            path("Альтернативный", "T19", "T18", "D15")
        ),
        142 to listOf(path("Основной", "T20", "T14", "D20")),
        143 to listOf(path("Основной", "T20", "T17", "D16")),
        144 to listOf(path("Основной", "T20", "T20", "D12")),
        145 to listOf(path("Основной", "T20", "T15", "D20")),
        146 to listOf(path("Основной", "T20", "T18", "D16")),
        147 to listOf(path("Основной", "T20", "T17", "D18")),
        148 to listOf(path("Основной", "T20", "T16", "D20")),
        149 to listOf(path("Основной", "T20", "T19", "D16")),
        150 to listOf(
            path("Основной", "T20", "T20", "D15"),
            path("Альтернативный", "T20", "T18", "D18")
        ),
        151 to listOf(path("Основной", "T20", "T17", "D20")),
        152 to listOf(path("Основной", "T20", "T20", "D16")),
        153 to listOf(path("Основной", "T20", "T19", "D18")),
        154 to listOf(path("Основной", "T20", "T18", "D20")),
        155 to listOf(path("Основной", "T20", "T19", "D19")),
        156 to listOf(path("Основной", "T20", "T20", "D18")),
        157 to listOf(
            path("Основной", "T20", "T19", "D20"),
            path("Альтернативный", "T19", "T20", "D20")
        ),
        158 to listOf(path("Основной", "T20", "T20", "D19")),
        160 to listOf(path("Основной", "T20", "T20", "D20")),
        161 to listOf(path("Основной", "T20", "T17", "BULL")),
        164 to listOf(path("Основной", "T20", "T18", "BULL")),
        167 to listOf(path("Основной", "T20", "T19", "BULL")),
        170 to listOf(path("Основной", "T20", "T20", "BULL"))
    )

    fun pathsFor(score: Int): List<CheckoutPath>? = TABLE[score]

    fun isCheckoutPossible(score: Int): Boolean = TABLE.containsKey(score)
}
