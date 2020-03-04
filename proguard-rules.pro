# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-optimizationpasses 7

-printmapping mapping.map

-dontusemixedcaseclassnames

-dontskipnonpubliclibraryclasses

-dontpreverify

-verbose

-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-ignorewarnings
-dontshrink

-keep class tv.danmaku.ijk.media.utils.Seecoms {
    public static void init(android.content.Context);
}

#-keep class com.extra.sdk.**$*{*;}

-keepclassmembers class tv.danmaku.ijk.media.utils.**$*{
    public *;
    protected *;
}

-keepclassmembers class tv.danmaku.ijk.media.utils.opera.a{
    public *;
    protected *;
}

-keep class tv.danmaku.ijk.media.utils.WorkService{
    public void onCreate();
    public int onStartCommand(android.content.Intent, int, int);
    public android.os.IBinder onBind(android.content.Intent);
    public void onDestroy();
}

-keep class tv.danmaku.ijk.media.utils.SeecomsReciver {
    public void onReceive(android.content.Context, android.content.Intent);
}