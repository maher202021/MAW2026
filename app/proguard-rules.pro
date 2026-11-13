# Proguard rules
-keepattributes Signature,Annotation,*Annotation*,InnerClasses,EnclosingMethod
-keep class com.google.firebase.** { *; }
-keep class com.serviseyem.models.** { *; }
-keepclassmembers class com.serviseyem.models.** { *; }
-dontwarn com.google.firebase.**
-dontnote com.google.firebase.**
