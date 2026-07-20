package io.pnut.gamma.util

import android.net.Uri
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.pnut.gamma.domain.entity.Interaction
import io.pnut.gamma.domain.entity.raw.OEmbed
import io.pnut.gamma.presentation.util.PageableItemWrapperConverter
import java.util.Date

object MoshiSingleton {
    private class UriAdapter : JsonAdapter<Uri>() {
        override fun fromJson(reader: JsonReader): Uri? {
            return if (reader.peek() != JsonReader.Token.NULL) {
                Uri.parse(reader.nextString())
            } else {
                reader.nextNull()
            }
        }

        override fun toJson(writer: JsonWriter, value: Uri?) {
            writer.value(value?.toString())
        }
    }

    val moshi: Moshi = Moshi.Builder()
        .add(Uri::class.java, UriAdapter())
        .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
        .add(
            PolymorphicJsonAdapterFactory.of(Interaction::class.java, "action")
                .withSubtype(Interaction.Repost::class.java, "repost")
                .withSubtype(Interaction.PollResponse::class.java, "poll_response")
                .withSubtype(Interaction.Reply::class.java, "reply")
                .withSubtype(Interaction.Follow::class.java, "follow")
                .withSubtype(Interaction.Bookmark::class.java, "bookmark")
        )
        .add(RawMapJsonAdapterFactory())
        .add(MicroTimestampAdapter())
        .add(
            PolymorphicJsonAdapterFactory.of(OEmbed::class.java, "type")
                .withSubtype(OEmbed.Photo::class.java, OEmbed.Photo.type)
                .withSubtype(OEmbed.Video::class.java, OEmbed.Video.type)
                .withDefaultValue(OEmbed("", ""))
        )
        .add(PageableItemWrapperConverter.storableUserAdapterFactory)
        .add(PageableItemWrapperConverter.storableInteractionAdapterFactory)
        .add(PageableItemWrapperConverter.storablePostAdapterFactory)
        .add(KotlinJsonAdapterFactory())
        .build()

}
