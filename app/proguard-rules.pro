# Regras de Otimização e Ofuscação ProGuard / R8 - Ciclo

# 1. Entidades de Banco de Dados Room
-keep class me.muriloscolari.ciclo.data.history.AlarmHistoryEntity { *; }
-keep interface me.muriloscolari.ciclo.data.history.AlarmHistoryDao { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-dontwarn androidx.room.**

# 2. Modelos de Domínio e Enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class me.muriloscolari.ciclo.domain.** { *; }
-keep class me.muriloscolari.ciclo.data.settings.** { *; }

# 3. Injeção de Dependências Hilt & Dagger
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep class me.muriloscolari.ciclo.di.** { *; }
-dontwarn dagger.hilt.**

# 4. Coroutines & Flow
-dontwarn kotlinx.coroutines.**

# 5. Otimizações específicas para Jetpack Compose
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}
