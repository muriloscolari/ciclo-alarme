package me.muriloscolari.ciclo.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.muriloscolari.ciclo.data.alarm.ActiveAlarmDao
import me.muriloscolari.ciclo.data.history.AlarmHistoryDao
import me.muriloscolari.ciclo.data.history.CicloDatabase
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideCicloDatabase(
        @ApplicationContext context: Context
    ): CicloDatabase {
        return Room.databaseBuilder(
            context,
            CicloDatabase::class.java,
            "ciclo.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideAlarmHistoryDao(database: CicloDatabase): AlarmHistoryDao {
        return database.alarmHistoryDao()
    }

    @Provides
    @Singleton
    fun provideActiveAlarmDao(database: CicloDatabase): ActiveAlarmDao {
        return database.activeAlarmDao()
    }

    @Provides
    @Singleton
    fun provideSystemClock(): Clock {
        return Clock.systemDefaultZone()
    }
}
