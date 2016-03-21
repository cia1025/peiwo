# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Applications/adt-bundle-mac-x86_64/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

-keepattributes *Annotation*
-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService
-keep public class * extends android.content.BroadcastReceiver

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclassmembers class ** {
    public void onEvent*(**);
    void onEvent*(**);
}

# keep setters in Views so that animations can still work.
# see http://proguard.sourceforge.net/manual/examples.html#beans
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

# We want to keep methods in Activity that could be used in the XML attribute onClick
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

#model
-dontwarn me.peiwo.peiwo.model.**
-keep class me.peiwo.peiwo.model.**{*;}

#gson
-dontwarn com.google.gson.**
-keep class com.google.gson.**{*;}
# Gson uses generic type information stored in a class file when working with fields. Proguard

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

-keepclassmembers class * {
    public void set*(**);
	public void openFileChooser(android.webkit.ValueCallback,java.lang.String,java.lang.String);
	public void openFileChooser(android.webkit.ValueCallback,java.lang.String);
	public void openFileChooser(android.webkit.ValueCallback);
}
-keep public class android.webkit.**{*;}
-keep public class android.net.http.SslError.**{*;}
-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}
-keep public class me.peiwo.peiwo.R$*{
public static final int *;
}

# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**

-keepclassmembers class * {
  public <init>(android.content.Context);
}

-keepattributes Signature


-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}



-keep class com.tencent.**{*;}

-keep class sun.misc.Unsafe { *; }
-keep class com.alibaba.** {*;}
-keep class com.alipay.** {*;}
-dontwarn com.alibaba.**
-dontwarn com.alipay.**

-keep class com.ut.** {*;}
-dontwarn com.ut.**

-keep class com.ta.** {*;}
-dontwarn com.ta.**

-keep class com.amap.** {*; }
-dontwarn com.amap.**

-keep class com.umeng.** {*; }
-dontwarn com.umeng.**

-dontwarn org.webrtc.**
-keep class org.webrtc.** {*;}

-dontwarn org.apache.log4j.**
-keep class org.apache.log4j.** { *;}

-dontwarn org.apache.**
-keep class org.apache.**{*;}

-dontwarn com.sohucs.**
-keep class com.sohucs.**{*;}

-dontwarn com.google.**
-keep class com.google.**{*;}

-dontwarn com.baidu.**
-keep class com.baidu.**{*;}

-dontwarn com.xiaomi.**
-keep class com.xiaomi.**{*;}

-dontwarn demo.**
-keep class demo.**{*;}

-dontwarn com.fasterxml.**
-keep class com.fasterxml.**{*;}

-dontwarn net.sourceforge.**
-keep class net.sourceforge.**{*;}

-dontwarn com.sina.**
-keep class com.sina.**{*;}

-dontwarn com.qiniu.**
-keep class com.qiniu.**{*;}

-dontwarn com.nostra13.universalimageloader.**
-keep class com.nostra13.universalimageloader.**{*;}


-dontwarn com.loopj.android.http.**
-keep class com.loopj.android.http.**{*;}


-dontwarn cz.msebera.**
-keep class cz.msebera.**{*;}

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.content.Context {
   public void *(android.view.View);
   public void *(android.view.MenuItem);
}

-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# OkHttp
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**
-dontwarn rx.**
-dontwarn okio.**

-dontwarn com.alibaba.fastjson.**
-keep class com.alibaba.fastjson.** {*;}

# rxjava
-keep class rx.schedulers.Schedulers {
    public static <methods>;
}
-keep class rx.schedulers.ImmediateScheduler {
    public <methods>;
}
-keep class rx.schedulers.TestScheduler {
    public <methods>;
}
-keep class rx.schedulers.Schedulers {
    public static ** test();
}
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    long producerNode;
    long consumerNode;
}

-keepclassmembers class fqcn.of.javascript.interface.for.webview {
 public *;
}

-keepattributes Exceptions,InnerClasses

-keep class io.rong.** {*;}

-keep class * implements io.rong.imlib.model.MessageContent{*;}

-keepattributes Signature

-keepattributes *Annotation*

-keep class sun.misc.Unsafe { *; }

-keep class com.google.gson.examples.android.model.** { *; }

-keepclassmembers class * extends com.sea_monster.dao.AbstractDao {
 public static java.lang.String TABLENAME;
}
-keep class **$Properties
-dontwarn org.eclipse.jdt.annotation.**

-keep class com.ultrapower.** {*;}

-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

-keep public class pl.droidsonroids.gif.GifIOException{<init>(int);}
-keep class pl.droidsonroids.gif.GifInfoHandle{<init>(long,int,int,int);}


-dontwarn java.nio.file.Files
-dontwarn java.nio.file.Path
-dontwarn java.nio.file.OpenOption
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

-dontwarn com.jakewharton.rxbinding.**
-keep class com.jakewharton.rxbinding.**{*;}