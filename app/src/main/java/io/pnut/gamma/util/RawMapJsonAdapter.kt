package io.pnut.gamma.util

import com.squareup.moshi.*
import io.pnut.gamma.domain.entity.raw.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class RawMapJsonAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (Types.getRawType(type) != Map::class.java) return null
        val types = (type as ParameterizedType).actualTypeArguments
        val keyType = types[0]
        val valueType = types[1]
        if (keyType != String::class.java) return null
        if (Types.getRawType(valueType) != List::class.java) return null
        val listValueType = (valueType as ParameterizedType).actualTypeArguments[0]
        if (listValueType != RawValue::class.java) return null

        return RawMapJsonAdapter(moshi)
    }

    private class RawMapJsonAdapter(val moshi: Moshi) : JsonAdapter<Map<String, List<RawValue>>>() {
        private val typeToClass = mapOf(
            OEmbed.TYPE to OEmbed::class.java,
            Spoiler.TYPE to Spoiler::class.java,
            LongPost.TYPE to LongPost::class.java,
            PollNotice.TYPE to PollNotice::class.java,
            ChannelInvite.TYPE to ChannelInvite::class.java,
            ChatSettings.TYPE to ChatSettings::class.java,
            CrossPost.TYPE to CrossPost::class.java,
            Language.TYPE to Language::class.java
        )

        override fun fromJson(reader: JsonReader): Map<String, List<RawValue>> {
            val result = mutableMapOf<String, List<RawValue>>()
            reader.beginObject()
            while (reader.hasNext()) {
                val type = reader.nextName()
                val clazz = typeToClass[type] ?: RawImpl::class.java
                val adapter = moshi.adapter<List<RawValue>>(
                    Types.newParameterizedType(List::class.java, clazz)
                )
                val list = adapter.fromJson(reader)
                if (list != null) {
                    result[type] = list
                }
            }
            reader.endObject()
            return result
        }

        override fun toJson(writer: JsonWriter, value: Map<String, List<RawValue>>?) {
            if (value == null) {
                writer.nullValue()
                return
            }
            writer.beginObject()
            value.forEach { (type, list) ->
                writer.name(type)
                writer.beginArray()
                list.forEach { item ->
                    val adapter = moshi.adapter<RawValue>(item.javaClass)
                    adapter.toJson(writer, item)
                }
                writer.endArray()
            }
            writer.endObject()
        }
    }
}
