
package com.example.d308vacationplanner.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.d308vacationplanner.entities.Excursion;

import java.util.List;


// This interface defines database operations for Excursions using Room's DAO (Data Access Object) pattern.
// It encapsulates the database operations and ensures secure interaction with the SQLite database.

@Dao
public interface ExcursionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Excursion excursion);

    @Update
    void update(Excursion excursion);

    @Delete
    void delete(Excursion excursion);

    @Query("SELECT * FROM excursion_table WHERE vacationID = :vacationId")
    LiveData<List<Excursion>> getExcursionsForVacation(int vacationId);

    @Query("SELECT * FROM excursion_table WHERE excursionID = :excursionId")
    Excursion getExcursionById(int excursionId);

    @Query("SELECT * FROM excursion_table WHERE vacationID = :vacationId")
    List<Excursion> getExcursionsForVacationSync(int vacationId);

    @Query("SELECT * FROM excursion_table")
    List<Excursion> getAllExcursionsSync(); // Synchronous method for all excursions


}
