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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile