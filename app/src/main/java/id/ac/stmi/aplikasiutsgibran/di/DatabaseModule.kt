package id.ac.stmi.aplikasiutsgibran.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import id.ac.stmi.aplikasiutsgibran.data.local.GearaDao
import id.ac.stmi.aplikasiutsgibran.data.local.GearaDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideGearaDatabase(
        @ApplicationContext context: Context
    ): GearaDatabase {
        return Room.databaseBuilder(
            context,
            GearaDatabase::class.java,
            "geara_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideGearaDao(database: GearaDatabase): GearaDao {
        return database.gearaDao()
    }
}
