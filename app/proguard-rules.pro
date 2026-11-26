# Proguard rules
-keepattributes Signature,Annotation,*Annotation*,InnerClasses,EnclosingMethod
-keep class com.google.firebase.** { *; }
-keep class com.serviseyem.models.** { *; }
-keepclassmembers class com.serviseyem.models.** { *; }
-keep class com.serviseyem.data.** { *; }
-keepclassmembers class com.serviseyem.data.** { *; }
-keep class com.serviseyem.domain.** { *; }
-keepclassmembers class com.serviseyem.domain.** { *; }
-keep class com.serviseyem.utils.Wallet { *; }
-keepclassmembers class com.serviseyem.utils.Wallet { *; }
-keep class com.serviseyem.navigation.** { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keep class io.github.rallista.valhalla.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn com.google.firebase.**
-dontnote com.google.firebase.**
-dontwarn io.github.rallista.valhalla.**
