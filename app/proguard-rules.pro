# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleAnnotations, RuntimeInvisibleParameterAnnotations
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}

# Moshi
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}
-keep @com.squareup.moshi.JsonClass class *
-keep class * extends com.squareup.moshi.JsonAdapter
-keep class io.pnut.gamma.domain.entity.** { *; }
-keep class io.pnut.gamma.domain.model.** { *; }
-keep class io.pnut.gamma.data.** { *; }
-keep class io.pnut.gamma.util.** { *; }

# OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
