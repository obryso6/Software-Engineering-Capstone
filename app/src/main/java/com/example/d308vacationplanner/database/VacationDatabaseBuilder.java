package com.example.d308vacationplanner.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.d308vacationplanner.dao.ExcursionDao;
import com.example.d308vacationplanner.dao.UserDao;
import com.example.d308vacationplanner.dao.VacationDao;
import com.example.d308vacationplanner.entities.Excursion;
import com.example.d308vacationplanner.entities.User;
import com.example.d308vacationplanner.entities.Vacation;

// This class handles the creation of the database using Room Database Builder.
// It provides an instance of the database, ensuring scalability and centralized access to the database.

@Database(entities = {Vacation.class, Excursion.class, User.class}, version = 3, exportSchema = false)
public abstract class VacationDatabaseBuilder extends RoomDatabase {
    public abstract VacationDao vacationDao();
    public abstract ExcursionDao excursionDao();
    public abstract UserDao userDao();  // Add this line

    private static volatile VacationDatabaseBuilder INSTANCE;

    public static VacationDatabaseBuilder getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (VacationDatabaseBuilder.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    VacationDatabaseBuilder.class, "vacation_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
