package com.example.flowerai.ui

enum class WrappingOption(
    val title: String,
    val colorHex: Long
) {
    White("Белая", 0xFFF7F7F7),
    Black("Чёрная", 0xFF2D2730),
    SoftPink("Розовая", 0xFFF4D5E1),
    SkyBlue("Голубая", 0xFFD8E7F7)
}

data class Bouquet(
    val id: Int,
    val name: String,
    val price: String,
    val imageRes: Int,
    val imageVariants: List<Int> = emptyList(),
    val description: String = "",
    val tags: List<String> = emptyList(),
    val composition: List<FlowerMeaning> = emptyList(),
    val occasions: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val isInCart: Boolean = false,
    val quantity: Int = 1,
    val wrappingOption: WrappingOption = WrappingOption.White
)

fun Bouquet.displayImages(): List<Int> =
    imageVariants.ifEmpty { listOf(imageRes) }

fun Bouquet.imageForSelectedWrapping(): Int =
    if (id == 5) {
        when (wrappingOption) {
            WrappingOption.Black -> imageVariants.getOrNull(0) ?: imageRes
            WrappingOption.SoftPink -> imageVariants.getOrNull(1) ?: imageRes
            WrappingOption.White -> imageVariants.getOrNull(2) ?: imageRes
            WrappingOption.SkyBlue -> imageVariants.getOrNull(2) ?: imageRes
        }
    } else {
        imageRes
    }

data class FlowerMeaning(
    val flower: String,
    val meaning: String
)

fun defaultBouquetDescription(id: Int): String =
    defaultBouquetDetails(id).description

fun defaultBouquetTags(id: Int): List<String> =
    defaultBouquetDetails(id).tags

fun defaultBouquetComposition(id: Int): List<FlowerMeaning> =
    defaultBouquetDetails(id).composition

fun defaultBouquetOccasions(id: Int): List<String> =
    defaultBouquetDetails(id).occasions

private data class BouquetDetails(
    val description: String,
    val tags: List<String>,
    val composition: List<FlowerMeaning>,
    val occasions: List<String>
)

private fun defaultBouquetDetails(id: Int): BouquetDetails =
    when (id) {
        1 -> BouquetDetails(
            description = "Глубокий лавандовый букет в вечерней эстетике: мягкая эустома, выразительные розы и воздушная гипсофила создают нежный, но запоминающийся подарок.",
            tags = listOf("Романтичный", "Ароматный", "Тёмная эстетика", "Нежный акцент"),
            composition = listOf(
                FlowerMeaning("Лаванда", "Спокойствие, тонкий аромат и ощущение заботы."),
                FlowerMeaning("Эустома", "Нежность, благодарность и мягкость композиции."),
                FlowerMeaning("Роза", "Восхищение, чувства и классический цветочный акцент."),
                FlowerMeaning("Гипсофила", "Лёгкость, воздух и деликатное завершение букета.")
            ),
            occasions = listOf("Свидание", "Годовщина", "Девушке", "Без повода")
        )
        2 -> BouquetDetails(
            description = "Драматичная композиция в винно-красной гамме с протеей и амарантом. Букет выглядит смело, дорого и подходит для подарка, который должен произвести впечатление.",
            tags = listOf("Премиум", "Выразительный", "Марсала", "Вау-эффект"),
            composition = listOf(
                FlowerMeaning("Роза", "Страсть, уважение и сильный эмоциональный акцент."),
                FlowerMeaning("Протея", "Редкость, характер и премиальная фактура."),
                FlowerMeaning("Амарант", "Движение, глубина и необычная форма."),
                FlowerMeaning("Эвкалипт", "Свежесть, объём и благородная зелень.")
            ),
            occasions = listOf("Юбилей", "Годовщина", "Любимой", "Статусный подарок")
        )
        3 -> BouquetDetails(
            description = "Свежий голубой букет с гортензией, ирисами и эустомой. Он выглядит чисто, спокойно и воздушно, поэтому хорошо подходит для тёплого, аккуратного подарка.",
            tags = listOf("Нежный", "Свежий", "Пастельный", "Воздушный"),
            composition = listOf(
                FlowerMeaning("Гортензия", "Гармония, объём и мягкая облачная форма."),
                FlowerMeaning("Ирис", "Доверие, надежда и элегантная линия."),
                FlowerMeaning("Эустома", "Нежность и спокойный женственный акцент."),
                FlowerMeaning("Дельфиниум", "Высота, лёгкость и прохладная голубая гамма.")
            ),
            occasions = listOf("Маме", "Благодарность", "День рождения", "Для врача")
        )
        4 -> BouquetDetails(
            description = "Элегантный бело-зелёный букет с орхидеями и каллами. Минималистичный, чистый и статусный вариант для случаев, где важны вкус и сдержанная роскошь.",
            tags = listOf("Элегантный", "Статусный", "Минимализм", "Белая гамма"),
            composition = listOf(
                FlowerMeaning("Орхидея", "Изысканность, красота и премиальный характер."),
                FlowerMeaning("Калла", "Чистота, форма и строгая элегантность."),
                FlowerMeaning("Эвкалипт", "Свежесть и современная зелёная база."),
                FlowerMeaning("Эустома", "Мягкость, чтобы букет не выглядел слишком холодно.")
            ),
            occasions = listOf("Свадьба", "Начальнице", "Юбилей", "Деловой подарок")
        )
        5 -> BouquetDetails(
            description = "Нежная розовая композиция с ранункулюсами, тюльпанами и эвкалиптом. Один и тот же букет можно выбрать в черной, пудрово-розовой или белой обертке, чтобы настроение подарка стало более драматичным, мягким или классическим.",
            tags = listOf("Три обертки", "Нежный", "Розовая гамма", "Весенний"),
            composition = listOf(
                FlowerMeaning("Ранункулюс", "Многослойная нежность, романтика и дорогая фактура."),
                FlowerMeaning("Тюльпан", "Свежесть, легкость и ощущение весеннего подарка."),
                FlowerMeaning("Эвкалипт", "Спокойная зелень, объем и современный вид."),
                FlowerMeaning("Лента и обертка", "Меняют характер букета без изменения состава.")
            ),
            occasions = listOf("Маме", "Девушке", "Свидание", "День рождения")
        )
        else -> BouquetDetails(
            description = "Авторский букет с продуманной цветовой гаммой, свежими цветами и аккуратной упаковкой.",
            tags = listOf("Авторский", "Свежий", "Ручная сборка"),
            composition = emptyList(),
            occasions = listOf("Подарок", "День рождения")
        )
    }
