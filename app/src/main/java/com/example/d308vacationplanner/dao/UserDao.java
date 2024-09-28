package com.example.d308vacationplanner.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.d308vacationplanner.entities.User;

@Dao
public interface UserDao {
    @Insert
    void insert(User user);

    @Query("SELECT * FROM user_table WHERE username = :username LIMIT 1")
    User findByUsername(String username);

    @Query("SELECT * FROM user_table WHERE email = :email LIMIT 1")
    User findByEmail(String email);
}
