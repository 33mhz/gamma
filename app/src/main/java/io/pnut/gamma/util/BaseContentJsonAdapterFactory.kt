package io.pnut.gamma.util

import com.squareup.moshi.*
import io.pnut.gamma.domain.entity.entities.BaseContent
import java.lang.reflect.Type

class BaseContentJsonAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (type != BaseContent::class.java) return null

        val delegate = moshi.nextAdapter<BaseContent>(this, type, annotations)

        return object : JsonAdapter<BaseContent>() {
            override fun fromJson(reader: JsonReader): BaseContent? {
                return if (reader.peek() == JsonReader.Token.STRING) {
                    BaseContent(text = reader.nextString())
                } else {
                    delegate.fromJson(reader)
                }
            }

            override fun toJson(writer: JsonWriter, value: BaseContent?) {
                delegate.toJson(writer, value)
            }
        }.nullSafe()
    }
}
