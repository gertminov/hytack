package software.heim.hytack.data.domain


@JvmInline
value class Milliliter(
    val value: Int
) {
    fun format() = if (value >= 1000) "${this.round(value.toFloat() / 1000f, 1)}l" else "${value}ml"
    private fun round(value: Float, decimals: Int) = "%.${decimals}f".format(value)

    operator fun div(other: Milliliter) = value / other.value
}

fun MapperScope<Int>.milliliter() = Milliliter(value)
