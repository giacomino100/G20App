package it.polito.g20app


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TimeSlot::class], version = 1)
abstract class TimeSlotDB: RoomDatabase() {
    abstract fun TimeSlotDAO(): TimeSlotDAO
    companion object {
        @Volatile
        private var INSTANCE: TimeSlotDB? = null

        fun getDatabase(context: Context): TimeSlotDB =
            (
                    INSTANCE?:
                    synchronized(this) {
                        val i = INSTANCE ?: Room.databaseBuilder(
                            context.applicationContext,
                            TimeSlotDB::class.java,
                            "items"
                        ).build()
                        INSTANCE = i
                        INSTANCE
                    }
                    )!!


    }

}