package io.pnut.gamma.util

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import io.pnut.gamma.domain.entity.raw.LongPost

class LongPostJsonAdapter(moshi: Moshi) : JsonAdapter<LongPost>() {
    private val stringAdapter: JsonAdapter<String> = moshi.adapter(String::class.java)
    private val nullableStringAdapter: JsonAdapter<String?> = moshi.adapter(String::class.java)
    private val longAdapter: JsonAdapter<Long> = moshi.adapter(Long::class.java, MicroTimestamp::class.java)

    private val options: JsonReader.Options = JsonReader.Options.of("body", "title", "tstamp")

    override fun fromJson(reader: JsonReader): LongPost {
        var body: String? = null
        var title: String? = null
        var tstamp = 0L

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.selectName(options)) {
                0 -> body = stringAdapter.fromJson(reader)
                1 -> title = nullableStringAdapter.fromJson(reader)
                2 -> tstamp = longAdapter.fromJson(reader) ?: 0L
                -1 -> {
                    reader.skipName()
                    reader.skipValue()
                }
            }
        }
        reader.endObject()

        return LongPost(
            body = body ?: throw JsonDataException("Missing required field: body"),
            title = title,
            tstamp = tstamp
        )
    }

    override fun toJson(writer: JsonWriter, value: LongPost?) {
        if (value == null) {
            writer.nullValue()
            return
        }
        writer.beginObject()
        writer.name("body")
        stringAdapter.toJson(writer, value.body)
        writer.name("title")
        nullableStringAdapter.toJson(writer, value.title)
        writer.name("tstamp")
        longAdapter.toJson(writer, value.tstamp)
        writer.endObject()
    }
}
