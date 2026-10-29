package id.ac.stmi.aplikasiutsgibran.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CartEntity::class, ProductEntity::class], version = 3, exportSchema = false)
abstract class GearaDatabase : RoomDatabase() {

    abstract fun gearaDao(): GearaDao

    companion object {
        @Volatile
        private var INSTANCE: GearaDatabase? = null

        fun getDatabase(context: Context): GearaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GearaDatabase::class.java,
                    "geara_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
