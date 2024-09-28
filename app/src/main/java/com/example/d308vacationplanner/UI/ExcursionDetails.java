package com.example.d308vacationplanner.UI;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.d308vacationplanner.R;
import com.example.d308vacationplanner.database.VacationRepository;
import com.example.d308vacationplanner.entities.Excursion;
import com.example.d308vacationplanner.entities.Vacation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


// This activity displays details of a selected excursion and allows for editing and deletion.
// It uses encapsulation to manage excursion details and interacts with the database through the DAO and Repository patterns.


public class ExcursionDetails extends AppCompatActivity {
    private EditText editTextExcursionName;
    private EditText editTextExcursionDate;
    private VacationRepository vacationRepository;
    private int excursionId;
    private int vacationId;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_excursion);

        // Initialize views
        editTextExcursionName = findViewById(R.id.editText_excursion_name);
        editTextExcursionDate = findViewById(R.id.editText_excursion_date);

        // Initialize the repository
        vacationRepository = new VacationRepository(getApplication());

        // Retrieve data from intent
        if (getIntent().hasExtra("excursionId")) {
            excursionId = getIntent().getIntExtra("excursionId", -1);
            editTextExcursionName.setText(getIntent().getStringExtra("excursionName"));
            editTextExcursionDate.setText(getIntent().getStringExtra("excursionDate"));
            vacationId = getIntent().getIntExtra("vacationId", -1);
        }

        // Set up the ActionBar or Toolbar if necessary
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Set up DatePickerDialog for excursion date entry
        editTextExcursionDate.setOnClickListener(v -> showDatePickerDialog());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_excursion_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        Log.d("ExcursionDetails", "Menu item selected: " + itemId);
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.action_update_excursion) {
            Log.d("ExcursionDetails", "Update excursion selected");
            updateExcursion();
            return true;
        } else if (itemId == R.id.action_delete_excursion) {
            Log.d("ExcursionDetails", "Delete excursion selected");
            deleteExcursion();
            return true;
        } else if (itemId == R.id.action_set_alarm) {
            // Set alarm without showing DatePicker
            setAlarmForExcursion(editTextExcursionDate.getText().toString(), editTextExcursionName.getText().toString());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, month1, dayOfMonth) -> {
                    // Update the excursion date EditText with the selected date
                    String dateStr = String.format(Locale.getDefault(), "%02d-%02d-%04d", dayOfMonth, month1 + 1, year1);
                    editTextExcursionDate.setText(dateStr);
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void updateExcursion() {
        String excursionName = editTextExcursionName.getText().toString().trim();
        String excursionDate = editTextExcursionDate.getText().toString().trim();

        executorService.execute(() -> {
            boolean isValid = validateExcursionDateWithinVacation(excursionDate);
            runOnUiThread(() -> {
                if (isValid) {
                    Excursion excursion = new Excursion(excursionName, excursionDate, vacationId);
                    excursion.setExcursionID(excursionId);

                    Log.d("ExcursionDetails", "Updating Excursion: " + excursion);

                    executorService.execute(() -> {
                        vacationRepository.updateExcursion(excursion);
                        runOnUiThread(() -> {
                            Log.d("ExcursionDetails", "Excursion updated in DB");
                            Toast.makeText(ExcursionDetails.this, "Excursion updated", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    });
                } else {
                    Toast.makeText(ExcursionDetails.this, "Excursion date must be within the vacation period", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private boolean validateExcursionDateWithinVacation(String excursionDate) {
        try {
            // Fetch vacation start and end dates
            Vacation vacation = vacationRepository.getVacationById(vacationId);
            if (vacation != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                Date startDate = dateFormat.parse(vacation.getStartDate());
                Date endDate = dateFormat.parse(vacation.getEndDate());
                Date excursionParsedDate = dateFormat.parse(excursionDate);

                if (excursionParsedDate != null && startDate != null && endDate != null) {
                    return !excursionParsedDate.before(startDate) && !excursionParsedDate.after(endDate);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void deleteExcursion() {
        executorService.execute(() -> {
            Excursion excursion = vacationRepository.getExcursionById(excursionId);
            if (excursion != null) {
                vacationRepository.deleteExcursion(excursion);
                runOnUiThread(() -> {
                    Toast.makeText(ExcursionDetails.this, "Excursion deleted", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } else {
                runOnUiThread(() -> Toast.makeText(ExcursionDetails.this, "Excursion not found", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private boolean canScheduleExactAlarms() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || (alarmManager != null && alarmManager.canScheduleExactAlarms());
    }

    private void requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivity(intent);
        }
    }

    private void setAlarmForExcursion(String excursionDate, String excursionTitle) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        try {
            Date date = dateFormat.parse(excursionDate);
            if (date != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                if (canScheduleExactAlarms()) {
                    setAlarm(calendar, "Excursion Reminder", excursionTitle);
                } else {
                    requestExactAlarmPermission();
                }
            } else {
                Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Date parsing failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void setAlarm(Calendar calendar, String title, String message) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, VacationAlarmReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) calendar.getTimeInMillis(),  // Unique request code to avoid collision
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE  // Apply the correct flags
        );

        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }
}
