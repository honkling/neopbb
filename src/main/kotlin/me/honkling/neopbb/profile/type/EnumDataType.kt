package me.honkling.neopbb.profile.type

import me.honkling.commonlib.lib.CaseType
import me.honkling.commonlib.lib.convertCase
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType

class EnumDataType<T>(
    private val enumClass: Class<T>,
    private val caseType: CaseType = CaseType.Snake
) : PersistentDataType<String, T> {
    private val entries = enumClass.enumConstants.toList() as List<Enum<*>>

    override fun fromPrimitive(primitive: String, context: PersistentDataAdapterContext): T & Any {
        return entries.find { it.name.equals(primitive, true) } as (T & Any)
    }

    override fun toPrimitive(complex: T & Any, context: PersistentDataAdapterContext): String {
        return (complex as Enum<*>).name.convertCase(caseType)
    }

    override fun getPrimitiveType(): Class<String> {
        return String::class.java
    }

    override fun getComplexType(): Class<T> {
        return enumClass
    }
}