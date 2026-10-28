package com.example.vpn.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.vpn.model.ConnectionHistoryItem
import com.example.vpn.model.VpnProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface VpnProfileDao {
    @Query("SELECT * FROM vpn_profiles ORDER BY isFavorite DESC, name ASC")
    fun getAllProfiles(): Flow<List<VpnProfile>>

    @Query("SELECT * FROM vpn_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Long): VpnProfile?

    @Query("SELECT * FROM vpn_profiles ORDER BY lastUsed DESC LIMIT 1")
    suspend fun getLastUsedProfile(): VpnProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: VpnProfile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<VpnProfile>)

    @Update
    suspend fun updateProfile(profile: VpnProfile)

    @Delete
    suspend fun deleteProfile(profile: VpnProfile)

    @Query("UPDATE vpn_profiles SET pingMs = :pingMs WHERE id = :id")
    suspend fun updatePing(id: Long, pingMs: Long?)

    @Query("UPDATE vpn_profiles SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE vpn_profiles SET lastUsed = :timestamp WHERE id = :id")
    suspend fun updateLastUsed(id: Long, timestamp: Long)

    @Query("SELECT COUNT(*) FROM vpn_profiles")
    suspend fun getCount(): Int
}

@Dao
interface ConnectionHistoryDao {
    @Query("SELECT * FROM connection_history ORDER BY connectedAt DESC LIMIT 100")
    fun getHistory(): Flow<List<ConnectionHistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: ConnectionHistoryItem): Long

    @Query("DELETE FROM connection_history")
    suspend fun clearHistory()
}
