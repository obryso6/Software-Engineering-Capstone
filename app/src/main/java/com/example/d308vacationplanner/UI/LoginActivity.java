package com.example.d308vacationplanner.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.d308vacationplanner.R;
import com.example.d308vacationplanner.database.VacationDatabaseBuilder;
import com.example.d308vacationplanner.entities.User;
import com.example.d308vacationplanner.dao.UserDao;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private UserDao userDao;
    private Executor executor = Executors.newSingleThreadExecutor();  // Single thread executor

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Button loginButton = findViewById(R.id.loginButton);
        TextView signUpTextView = findViewById(R.id.signUpTextView);

        // Initialize database and DAO
        userDao = VacationDatabaseBuilder.getDatabase(this).userDao();

        // Set click listener for login button
        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Perform database operation in a background thread
            executor.execute(() -> {
                boolean isAuthenticated = authenticateUser(username, password);
                runOnUiThread(() -> {
                    if (isAuthenticated) {
                        // If login is successful, start the main activity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // If login fails, show an error message
                        Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        // Set click listener for sign-up link
        signUpTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private boolean authenticateUser(String username, String password) {
        User user = userDao.findByUsername(username);
        Log.d("Login", "Retrieved user: " + (user != null ? user.getUsername() : "null"));
        Log.d("Login", "Stored password: " + (user != null ? user.getPassword() : "null"));
        Log.d("Login", "Entered password: " + password);

        boolean isAuthenticated = user != null && user.getPassword().equals(password);
        Log.d("Login", "Authentication result: " + isAuthenticated);

        if (isAuthenticated) {
            // Set the isLoggedIn flag in SharedPreferences
            Log.d("LoginActivity", "Setting isLoggedIn to true in SharedPreferences");
            SharedPreferences sharedPreferences = getSharedPreferences("userSession", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", true);
            editor.apply();
        }

        return isAuthenticated;
    }
}
