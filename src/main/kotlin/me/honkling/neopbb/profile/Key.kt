package me.honkling.neopbb.profile

import me.honkling.commonlib.lib.CaseType
import me.honkling.commonlib.lib.convertCase
import me.honkling.neopbb.lib.isClassPrimitive
import me.honkling.neopbb.lib.namespace
import me.honkling.neopbb.profile.type.EnumDataType
import org.bukkit.NamespacedKey
import org.bukkit.persistence.ListPersistentDataType
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.typeOf

inline fun <reified T> createKey(defaultValue: T? = null, noinline onSet: (T & Any) -> Unit = {}): KeyProvider<T> {
    val clazz = T::class
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
        clazz == List::class -> getListType<T>()
        clazz.isSubclassOf(Enum::class) -> EnumDataType(T::class.java)
        else ->
            throw IllegalStateException("Unknown type ${clazz.qualifiedName}")
    }

    return KeyProvider(type as PersistentDataType<*, T>, null !is T, defaultValue, onSet)
}

inline fun <reified T> getListType(): ListPersistentDataType<*, *> {
    val type = PersistentDataType.LIST
    val clazz = typeOf<T>()

    return when {
        isClassPrimitive(clazz, Byte::class) -> type.bytes()
        isClassPrimitive(clazz, Short::class) -> type.shorts()
        isClassPrimitive(clazz, Int::class) -> type.integers()
        isClassPrimitive(clazz, Long::class) -> type.longs()
        isClassPrimitive(clazz, Float::class) -> type.floats()
        isClassPrimitive(clazz, Double::class) -> type.doubles()
        isClassPrimitive(clazz, Boolean::class) -> type.booleans()
        clazz == String::class -> type.strings()
        clazz == IntArray::class -> type.integerArrays()
        clazz == LongArray::class -> type.longArrays()
        clazz == ByteArray::class -> type.byteArrays()
        (clazz.classifier as? KClass<out Enum<*>>)?.let { Enum::class.isSuperclassOf(it) } == true
            -> type.listTypeFrom(EnumDataType(T::class.java))
        else ->
            throw IllegalStateException("Unknown type $clazz")
    }
}

class KeyProvider<T>(
    private val type: PersistentDataType<*, T>,
    private val isRequired: Boolean,
    private val defaultValue: T?,
    private val onSet: (T & Any) -> Unit
) {
    operator fun provideDelegate(thisRef: Profile, property: KProperty<*>): Key<T> {
        val name = property.name.convertCase(CaseType.Snake)
        val key = NamespacedKey(namespace, name)
        return Key(
            thisRef.player.persistentDataContainer,
            key,
            type as PersistentDataType<*, T & Any>,
            isRequired,
            defaultValue,
            onSet
        )
    }
}

class Key<T>(
    private val container: PersistentDataContainer,
    private val key: NamespacedKey,
    private val type: PersistentDataType<*, T & Any>,
    private val isRequired: Boolean,
    private val defaultValue: T?,
    private val onSet: (T & Any) -> Unit
) {
    operator fun getValue(thisRef: Profile, property: KProperty<*>): T {
        val value = container.get(key, type)

        if (isRequired && value == null)
            return defaultValue
                ?: throw IllegalStateException("Required key '${key}' doesn't have a default value")

        return value as T
    }

    operator fun setValue(thisRef: Profile, property: KProperty<*>, value: T & Any) {
        container.set(key, type, value)
        onSet(value)
    }
}