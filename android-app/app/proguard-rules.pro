# ProGuard rules for DB3K Blog

# Keep FCM service
-keep class com.google.firebase.messaging.** { *; }

# Keep Moshi adapters
-keep class com.squareup.moshi.** { *; }

# Keep Room entities
-keep class com.db3k.blog.data.** { *; }

# Keep Retrofit interfaces
-keep class com.db3k.blog.network.** { *; }

# Keep ViewBinding
-keep class com.db3k.blog.databinding.** { *; }

# Keep WorkManager
-keep class androidx.work.** { *; }

# Keep DataStore
-keep class androidx.datastore.** { *; }

# Keep Coil
-keep class coil.** { *; }

# Keep Navigation
-keep class androidx.navigation.** { *; }

# Suppress warnings
-dontwarn com.google.firebase.**
-dontwarn com.squareup.moshi.**
-dontwarn coil.**
-dontwarn okio.**