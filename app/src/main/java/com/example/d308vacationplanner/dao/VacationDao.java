package com.example.d308vacationplanner.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.d308vacationplanner.entities.Excursion;
import com.example.d308vacationplanner.entities.Vacation;

import java.util.List;


// This interface provides CRUD operations for managing vacation data using Room's DAO pattern.
// Encapsulation ensures that database operations are abstracted away from the rest of the application.

@Dao
public interface VacationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertVacation(Vacation vacation);

    @Update
    void update(Vacation vacation);

    @Delete
    void deleteVacation(Vacation vacation);

    @Query("SELECT * FROM vacation_table ORDER BY vacationID ASC")
    LiveData<List<Vacation>> getAllVacations();
    

    @Query("SELECT * FROM vacation_table WHERE vacationID = :vacationId")
    Vacation getVacationById(int vacationId);

    @Query("SELECT * FROM excursion_table WHERE vacationID = :vacationId")
    LiveData<List<Excursion>> getExcursionsForVacation(int vacationId);

    @Query("SELECT vacationName FROM vacation_table WHERE vacationID = :id")
    String getVacationNameById(int id);

    @Query("SELECT * FROM vacation_table")
    List<Vacation> getAllVacationsSync();


}
