package com.contoh.eticketbus2025.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.contoh.eticketbus2025.data.model.BusModel
import com.contoh.eticketbus2025.data.model.TicketHistoryModel
import com.contoh.eticketbus2025.data.model.NotificationEntity
import com.contoh.eticketbus2025.data.model.CityEntity


@Dao
interface AppDao {

    // --- BUS OPERATIONS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBuses(buses: List<BusModel>)

    @Query("SELECT * FROM buses WHERE origin = :from AND destination = :to")
    suspend fun searchBuses(from: String, to: String): List<BusModel>

    @Query("SELECT * FROM buses")
    suspend fun getAllBuses(): List<BusModel>

    // --- TICKET OPERATIONS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: TicketHistoryModel)

    @Query("SELECT * FROM tickets ORDER BY timestamp DESC")
    suspend fun getAllTickets(): List<TicketHistoryModel>

    // --- NOTIFICATION OPERATIONS ---
    @Insert
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert
    suspend fun insertAllNotifications(notifications: List<NotificationEntity>)

    @Query("SELECT * FROM notifications ORDER BY id DESC")
    suspend fun getAllNotifications(): List<NotificationEntity>

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Int)

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    suspend fun getUnreadNotificationCount(): Int

    // --- OPERATOR OPERATIONS ---
    // Mengambil nama operator unik dari tabel buses
    @Query("SELECT DISTINCT operatorName FROM buses")
    suspend fun getUniqueOperators(): List<String>

    // --- CITY OPERATIONS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCities(cities: List<CityEntity>)

    @Query("SELECT * FROM cities ORDER BY name ASC")
    suspend fun getAllCities(): List<CityEntity>
}