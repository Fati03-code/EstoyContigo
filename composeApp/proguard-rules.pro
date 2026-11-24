# kotlinx.serialization: conservar serializers generados de los DTO de Firebase REST
-keepattributes *Annotation*, InnerClasses, Signature
-dontnote kotlinx.serialization.**
-keepclassmembers @kotlinx.serialization.Serializable class mx.ipn.upiicsa.estoycontigo.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class mx.ipn.upiicsa.estoycontigo.data.remote.dto.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep class mx.ipn.upiicsa.estoycontigo.data.local.** { *; }

# Ktor / OkHttp
-dontwarn org.slf4j.**
-dontwarn io.ktor.**
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
