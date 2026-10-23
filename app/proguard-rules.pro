# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Puente JavaScript del WebView (AndroidBridge en MainActivity): el HTML llama a estos
# métodos por nombre (AndroidApp.getSerialNumber(), etc.) — si R8 los renombra o elimina
# por no ver referencias Java, el HTML deja de poder invocarlos en tiempo de ejecución.
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes *Annotation*,Signature,InnerClasses
-keep class com.azalogistics.pda.MainActivity$AndroidBridge { *; }

# SDK de Datalogic (número de serie, AppManager): usa reflexión/JNI desde su propio código.
# Sin esto, R8 puede quitar clases o campos en release y SYSTEM.SERIAL_NUMBER falla; el
# código lo captura (Throwable) y cae en silencio a ANDROID_ID, con lo que la PDA cambiaría de
# ID y dejaría de coincidir con su registro en el backend.
-keep class com.datalogic.** { *; }
-dontwarn com.datalogic.**

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile