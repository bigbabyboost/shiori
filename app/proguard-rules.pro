-dontobfuscate

# Extensions may require methods unused in the core app
-dontwarn eu.kanade.tachiyomi.**
-keep class eu.kanade.tachiyomi.** { public protected private *; }

-keep class org.jsoup.** { *; }
-keep class kotlin.** { *; }
-keep class okhttp3.** { *; }
-keep class com.google.gson.** { *; }
-keep class com.github.salomonbrys.kotson.** { *; }
-keep class com.squareup.duktape.** { *; }

# === Keep EH classes
-keep class exh.** { *; }
-keep class xyz.nulldev.** { *; }

# === Keep RxAndroid, https://github.com/ReactiveX/RxAndroid/issues/350
-keep class rx.android.** { *; }

# Design library
-dontwarn com.google.android.material.**
-keep class com.google.android.material.** { *; }
-keep interface com.google.android.material.** { *; }
-keep public class com.google.android.material.R$* { *; }
-keep class androidx.appcompat.graphics.drawable.DrawerArrowDrawable { *; }

-keep class com.hippo.image.** { *; }
-keep interface com.hippo.image.** { *; }
-keepclassmembers class * extends nucleus.presenter.Presenter {
    <init>();
}

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt # core serialization annotations

# kotlinx-serialization-json specific. Add this if you have java.lang.NoClassDefFoundError kotlinx.serialization.json.JsonObjectSerializer
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class eu.kanade.tachiyomi.**$$serializer { *; }
-keepclassmembers class eu.kanade.tachiyomi.** {
    *** Companion;
}
-keepclasseswithmembers class eu.kanade.tachiyomi.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class exh.**$$serializer { *; }
-keepclassmembers class exh.** {
    *** Companion;
}
-keepclasseswithmembers class exh.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class xyz.nulldev.ts.api.http.serializer.**$$serializer { *; }
-keepclassmembers class xyz.nulldev.ts.api.http.serializer.** {
    *** Companion;
}
-keepclasseswithmembers class xyz.nulldev.ts.api.http.serializer.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Madokami extension username and password crash fix
-keepclassmembers class androidx.preference.EditTextPreference {
      *** mOnBindEditTextListener;
      *** mText;
      public *;
}

# Hitomi extension crash fix
-keepclassmembers class rx.Single {
      *** onSubscribe;
      final *;
      protected *;
      public *;
}

# RxJava 1.1.0
-dontwarn sun.misc.**

-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

-dontnote rx.internal.util.PlatformDependent

# === Reactive network: https://github.com/pwittchen/ReactiveNetwork/tree/v0.12.4#proguard-configuration
-dontwarn com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
-dontwarn io.reactivex.functions.Function
-dontwarn rx.internal.util.**
-dontwarn sun.misc.Unsafe

# === Okhttp: https://github.com/square/okhttp/blob/3637fc56f70f87da696847defd311dbfb28e87b5/okhttp/src/main/resources/META-INF/proguard/okhttp3.pro
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*
# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform

# === Okio: https://github.com/square/okio/tree/9b8545e7fa267c9d89753283990f24a35cd69cd6#proguard
-dontwarn okio.**

# === GSON: https://raw.githubusercontent.com/google/gson/master/examples/android-proguard-example/proguard.cfg
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { <fields>; }

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# == Nucleus
-keepclassmembers class * extends nucleus.presenter.Presenter {
    <init>();
}

# TODO Changeloglib? Does it need proguard?

# === Injekt
## From original config: "Attempt to fix: java.lang.NoClassDefFoundError: uy.kohesive.injekt.registry.default.DefaultRegistrar$NOKEY$1"
-keep class uy.kohesive.injekt.** { *; }


# === Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder

# === Glide-transformations: https://github.com/wasabeef/glide-transformations/blob/3aa8e53c6a51b8351d312f802ba1354c5b115168/transformations/proguard-rules.txt
-dontwarn jp.co.cyberagent.android.gpuimage.**

# === Conductor
# This isn't in the consumer proguard rules yet: https://github.com/bluelinelabs/Conductor/pull/550/files
-keepclassmembers public class * extends com.bluelinelabs.conductor.ControllerChangeHandler {
   public <init>();
}

# === RxBinding
-dontwarn com.google.auto.value.AutoValue

# === Crashlytics
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

# === Humanize + Guava: https://github.com/google/guava/wiki/UsingProGuardWithGuava
-dontwarn javax.lang.model.element.Modifier
-keep class org.ocpsoft.prettytime.i18n.**

# Note: We intentionally don't add the flags we'd need to make Enums work.
# That's because the Proguard configuration required to make it work on
# optimized code would preclude lots of optimization, like converting enums
# into ints.

# Throwables uses internal APIs for lazy stack trace resolution
-dontnote sun.misc.SharedSecrets
-keep class sun.misc.SharedSecrets {
  *** getJavaLangAccess(...);
}
-dontnote sun.misc.JavaLangAccess
-keep class sun.misc.JavaLangAccess {
  *** getStackTraceElement(...);
  *** getStackTraceDepth(...);
}

# FinalizableReferenceQueue calls this reflectively
# Proguard is intelligent enough to spot the use of reflection onto this, so we
# only need to keep the names, and allow it to be stripped out if
# FinalizableReferenceQueue is unused.
-keepnames class com.google.common.base.internal.Finalizer {
  *** startFinalizer(...);
}
# However, it cannot "spot" that this method needs to be kept IF the class is.
-keepclassmembers class com.google.common.base.internal.Finalizer {
  *** startFinalizer(...);
}
-keepnames class com.google.common.base.FinalizableReference {
  void finalizeReferent();
}
-keepclassmembers class com.google.common.base.FinalizableReference {
  void finalizeReferent();
}

# Striped64, LittleEndianByteArray, UnsignedBytes, AbstractFuture
-dontwarn sun.misc.Unsafe

# Striped64 appears to make some assumptions about object layout that
# really might not be safe. This should be investigated.
-keepclassmembers class com.google.common.cache.Striped64 {
  *** base;
  *** busy;
}
-keepclassmembers class com.google.common.cache.Striped64$Cell {
  <fields>;
}

-dontwarn java.lang.SafeVarargs

-keep class java.lang.Throwable {
  *** addSuppressed(...);
}

# Futures.getChecked, in both of its variants, is incompatible with proguard.

# Used by AtomicReferenceFieldUpdater and sun.misc.Unsafe
-keepclassmembers class com.google.common.util.concurrent.AbstractFuture** {
  *** waiters;
  *** value;
  *** listeners;
  *** thread;
  *** next;
}
-keepclassmembers class com.google.common.util.concurrent.AtomicDouble {
  *** value;
}
-keepclassmembers class com.google.common.util.concurrent.AggregateFutureState {
  *** remaining;
  *** seenExceptions;
}

# Since Unsafe is using the field offsets of these inner classes, we don't want
# to have class merging or similar tricks applied to these classes and their
# fields. It's safe to allow obfuscation, since the by-name references are
# already preserved in the -keep statement above.
-keep,allowshrinking,allowobfuscation class com.google.common.util.concurrent.AbstractFuture** {
  <fields>;
}

# Futures.getChecked (which often won't work with Proguard anyway) uses this. It
# has a fallback, but again, don't use Futures.getChecked on Android regardless.
-dontwarn java.lang.ClassValue

# MoreExecutors references AppEngine
-dontnote com.google.appengine.api.ThreadManager
-keep class com.google.appengine.api.ThreadManager {
  static *** currentRequestThreadFactory(...);
}
-dontnote com.google.apphosting.api.ApiProxy
-keep class com.google.apphosting.api.ApiProxy {
  static *** getCurrentEnvironment (...);
}
