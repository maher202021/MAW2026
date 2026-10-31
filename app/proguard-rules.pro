# Proguard rules
-keepattributes Signature,Annotation,*Annotation*,InnerClasses,EnclosingMethod
-keep class com.google.firebase.** { *; }
-keep class com.Serviseyem.models.** { *; }
-keepclassmembers class com.Serviseyem.models.** { *; }
-dontwarn com.google.firebase.**
-dontnote com.google.firebase.**
