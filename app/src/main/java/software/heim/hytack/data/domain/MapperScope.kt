package software.heim.hytack.data.domain

abstract class MapperScope<T>(val value: T)

fun Int.mapper() = object :MapperScope<Int>(this){}