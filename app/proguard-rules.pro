# Proguard and R8 optimization rules for Release Builds
-keepattributes Signature,Annotation,*Annotation*,InnerClasses,EnclosingMethod

# Keep Firebase dependencies
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-dontnote com.google.firebase.**

# Keep Application models and entities
-keep class com.serviseyem.data.models.** { *; }
-keepclassmembers class com.serviseyem.data.models.** { *; }
-keep class com.serviseyem.data.** { *; }
-keepclassmembers class com.serviseyem.data.** { *; }
-keep class com.serviseyem.domain.** { *; }
-keepclassmembers class com.serviseyem.domain.** { *; }

# Keep Material Components and AndroidX UI
-keep class com.google.android.material.** { *; }
-keep class androidx.compose.** { *; }
-keep class androidx.core.view.WindowInsetsCompat** { *; }
