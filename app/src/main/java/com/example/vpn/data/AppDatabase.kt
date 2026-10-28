package com.example.vpn.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.vpn.model.ConnectionHistoryItem
import com.example.vpn.model.VpnProfile
import com.example.vpn.model.VpnProtocol

class Converters {
    @TypeConverter
    fun fromProtocol(protocol: VpnProtocol): String = protocol.name

    @TypeConverter
    fun toProtocol(value: String): VpnProtocol = runCatching {
        VpnProtocol.valueOf(value)
    }.getOrDefault(VpnProtocol.ANDROID_PLATFORM)
}

@Database(
    entities = [VpnProfile::class, ConnectionHistoryItem::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vpnProfileDao(): VpnProfileDao
    abstract fun connectionHistoryDao(): ConnectionHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "omni_vpn_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
