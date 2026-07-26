package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AttendanceDao
import com.example.data.local.entities.AttendanceRecordEntity
import com.example.data.local.entities.LeaveRequestEntity
import com.example.data.local.entities.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [UserEntity::class, AttendanceRecordEntity::class, LeaveRequestEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "udhayam_attendance_db"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.attendanceDao())
                }
            }
        }

        private suspend fun populateInitialData(dao: AttendanceDao) {
            if (dao.getUsersCount() > 0) return

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = dateFormat.format(Date())
            val currentTime = System.currentTimeMillis()

            // Seed Default Users
            val ownerId = dao.insertUser(
                UserEntity(
                    fullName = "Ramesh Kumar (Owner)",
                    email = "owner@udhayam.com",
                    pin = "1111",
                    role = "OWNER",
                    department = "Executive",
                    shiftHours = "08:00 AM - 07:00 PM",
                    phone = "+91 98765 43210",
                    hourlyRate = "350.0".toDouble()
                )
            )

            val managerId = dao.insertUser(
                UserEntity(
                    fullName = "Anitha Sundaram (Manager)",
                    email = "manager@udhayam.com",
                    pin = "2222",
                    role = "MANAGER",
                    department = "Operations",
                    shiftHours = "09:00 AM - 06:00 PM",
                    phone = "+91 98765 43211",
                    hourlyRate = "250.0".toDouble()
                )
            )

            val staff1Id = dao.insertUser(
                UserEntity(
                    fullName = "Karthik Raja",
                    email = "staff@udhayam.com",
                    pin = "3333",
                    role = "STAFF",
                    department = "Logistics",
                    shiftHours = "09:00 AM - 06:00 PM",
                    phone = "+91 98765 43212",
                    hourlyRate = "150.0".toDouble()
                )
            )

            val staff2Id = dao.insertUser(
                UserEntity(
                    fullName = "Priya Sharma",
                    email = "priya@udhayam.com",
                    pin = "4444",
                    role = "STAFF",
                    department = "Quality Control",
                    shiftHours = "09:00 AM - 06:00 PM",
                    phone = "+91 98765 43213",
                    hourlyRate = "160.0".toDouble()
                )
            )

            val staff3Id = dao.insertUser(
                UserEntity(
                    fullName = "Senthil Nathan",
                    email = "senthil@udhayam.com",
                    pin = "5555",
                    role = "STAFF",
                    department = "Operations",
                    shiftHours = "09:00 AM - 06:00 PM",
                    phone = "+91 98765 43214",
                    hourlyRate = "140.0".toDouble()
                )
            )

            // Seed Sample Attendance for Today
            val nineAM = currentTime - (3 * 3600 * 1000) // 3 hrs ago
            dao.insertAttendanceRecord(
                AttendanceRecordEntity(
                    userId = managerId,
                    userName = "Anitha Sundaram (Manager)",
                    date = todayStr,
                    punchInTime = nineAM - (15 * 60 * 1000), // Came 15 mins early
                    status = "PRESENT",
                    siteLocation = "Main Office",
                    notes = "Shift started on time"
                )
            )

            dao.insertAttendanceRecord(
                AttendanceRecordEntity(
                    userId = staff1Id,
                    userName = "Karthik Raja",
                    date = todayStr,
                    punchInTime = nineAM + (12 * 60 * 1000), // 12 mins late
                    status = "LATE",
                    siteLocation = "Warehouse A",
                    notes = "Traffic delay"
                )
            )

            dao.insertAttendanceRecord(
                AttendanceRecordEntity(
                    userId = staff2Id,
                    userName = "Priya Sharma",
                    date = todayStr,
                    punchInTime = nineAM,
                    status = "PRESENT",
                    siteLocation = "Main Office"
                )
            )

            // Seed Sample Leave Request
            dao.insertLeaveRequest(
                LeaveRequestEntity(
                    userId = staff3Id,
                    userName = "Senthil Nathan",
                    leaveType = "CASUAL",
                    startDate = todayStr,
                    endDate = todayStr,
                    reason = "Family function in hometown",
                    status = "PENDING",
                    requestDate = todayStr
                )
            )
        }
    }
}
