package com.example.d308vacationplanner.UI;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.d308vacationplanner.R;
import com.example.d308vacationplanner.database.VacationDatabaseBuilder;
import com.example.d308vacationplanner.entities.User;
import com.example.d308vacationplanner.dao.UserDao;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SignUpActivity extends AppCompatActivity {

    private EditText usernameEditText, emailEditText, passwordEditText;
    private UserDao userDao;
    private Executor executor = Executors.newSingleThreadExecutor();  // Single thread executor

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize views
        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Button signUpButton = findViewById(R.id.signUpButton);

        // Initialize database and DAO
        userDao = VacationDatabaseBuilder.getDatabase(this).userDao();

        // Set click listener for sign-up button
        signUpButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Validate the password length
            if (password.length() < 6) {
                Toast.makeText(SignUpActivity.this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            } else {
                // Perform database operation in a background thread
                executor.execute(() -> {
                    User newUser = new User();
                    newUser.setUsername(username);
                    newUser.setEmail(email);
                    newUser.setPassword(password);
                    userDao.insert(newUser);

                    Log.d("SignUp", "User inserted: " + newUser.getUsername() + ", " + newUser.getPassword());

                    runOnUiThread(() -> {
                        Toast.makeText(SignUpActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                });
            }
        });
    }
}
