package me.honkling.prisonbutbad.profile

import me.honkling.commonlib.lib.CaseType
import me.honkling.commonlib.lib.convertCase
import me.honkling.prisonbutbad.lib.isClassPrimitive
import me.honkling.prisonbutbad.profile.type.EnumDataType
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.lang.IllegalStateException
import kotlin.reflect.KProperty
import kotlin.reflect.full.isSubclassOf

inline fun <reified T> createKey(defaultValue: T? = null): KeyProvider<T> {
    val clazz = T::class
    val isRequired = null !is T
    val type = when {
        isClassPrimitive(clazz, Byte::class) -> PersistentDataType.BYTE
        isClassPrimitive(clazz, Short::class) -> PersistentDataType.SHORT
        isClassPrimitive(clazz, Int::class) -> PersistentDataType.INTEGER
        isClassPrimitive(clazz, Long::class) -> PersistentDataType.LONG
        isClassPrimitive(clazz, Float::class) -> PersistentDataType.FLOAT
        isClassPrimitive(clazz, Double::class) -> PersistentDataType.DOUBLE
        isClassPrimitive(clazz, Boolean::class) -> PersistentDataType.BOOLEAN
        clazz == String::class -> PersistentDataType.STRING
        clazz == IntArray::class -> PersistentDataType.INTEGER_ARRAY
        clazz == LongArray::class -> PersistentDataType.LONG_ARRAY
        clazz == ByteArray::class -> PersistentDataType.BYTE_ARRAY
        clazz.isSubclassOf(Enum::class) -> EnumDataType(T::class.java)
        else ->
            throw IllegalStateException("Unknown type ${clazz.qualifiedName}")
    }

    return KeyProvider(type as PersistentDataType<*, T>, null !is T, defaultValue)
}

class KeyProvider<T>(
    private val type: PersistentDataType<*, T>,
    private val isRequired: Boolean,
    private val defaultValue: T?
) {
    operator fun provideDelegate(thisRef: Profile, property: KProperty<*>): Key<T> {
        val name = property.name.convertCase(CaseType.Snake)
        val key = NamespacedKey("prisonbutbad", name)
        return Key(
            thisRef.player.persistentDataContainer,
            key,
            type as PersistentDataType<*, T & Any>,
            isRequired,
            defaultValue
        )
    }
}

class Key<T>(
    private val container: PersistentDataContainer,
    private val key: NamespacedKey,
    private val type: PersistentDataType<*, T & Any>,
    private val isRequired: Boolean,
    private val defaultValue: T?
) {
    operator fun getValue(thisRef: Profile, property: KProperty<*>): T {
        val value = container.get(key, type)

        if (isRequired)
            return defaultValue
                ?: throw IllegalStateException("Required key '${key}' doesn't have a default value")

        return value as T
    }

    operator fun setValue(thisRef: Profile, property: KProperty<*>, value: T & Any) {
        container.set(key, type, value)
    }
}