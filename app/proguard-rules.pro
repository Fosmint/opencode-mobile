# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> { static <1>$Companion Companion; }
