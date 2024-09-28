package com.example.d308vacationplanner.UI;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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


// This activity displays and allows updating of vacation details.
// It interacts with the database through DAOs and the Repository, demonstrating encapsulation and data validation.

public class VacationDetails extends AppCompatActivity {

    private EditText titleEditText, hotelEditText, startDateEditText, endDateEditText;
    private VacationRepository vacationRepository;
    private int vacationId = -1;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacation_details);

        // Initialize views
        titleEditText = findViewById(R.id.vacation_title);
        hotelEditText = findViewById(R.id.hotel);
        startDateEditText = findViewById(R.id.start_date);
        endDateEditText = findViewById(R.id.end_date);
        Button saveButton = findViewById(R.id.button_save);

        // Initialize the calendar instance
        calendar = Calendar.getInstance();

        // Initialize the VacationRepository
        vacationRepository = new VacationRepository(getApplication());

        // Determine if we're viewing or editing/adding
        if (getIntent().hasExtra("vacationId")) {
            vacationId = getIntent().getIntExtra("vacationId", -1);
            loadVacationDetails(vacationId);
        }

        // Set up DatePickers for start and end dates
        startDateEditText.setOnClickListener(v -> showDatePickerDialog((date) -> startDateEditText.setText(date)));
        endDateEditText.setOnClickListener(v -> showDatePickerDialog((date) -> endDateEditText.setText(date)));

        // Set up save button action
        saveButton.setOnClickListener(v -> saveVacation());

        // Setup toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void showDatePickerDialog(OnDateSetListener listener) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String date = String.format(Locale.getDefault(), "%02d-%02d-%04d", dayOfMonth, month + 1, year);
                    listener.onDateSet(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private interface OnDateSetListener {
        void onDateSet(String date);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.update) {
            updateVacation();  // Handle vacation updates
            return true;
        } else if (itemId == R.id.delete) {
            deleteVacation();  // Handle vacation deletion
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadVacationDetails(int vacationId) {
        Log.d("VacationDetails", "loadVacationDetails called with ID: " + vacationId);
        new Thread(() -> {
            Vacation vacation = vacationRepository.getVacationById(vacationId);
            runOnUiThread(() -> {
                if (vacation != null) {
                    Log.d("VacationDetails", "Vacation loaded: " + vacation.getVacationName());
                    titleEditText.setText(vacation.getVacationName());
                    hotelEditText.setText(vacation.getHotel());
                    startDateEditText.setText(vacation.getStartDate());
                    endDateEditText.setText(vacation.getEndDate());
                } else {
                    Log.d("VacationDetails", "Vacation not found for ID: " + vacationId);
                    Toast.makeText(VacationDetails.this, "Vacation not found", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity if vacation not found
                }
            });
        }).start();
    }

    private boolean validateDates(String startDate, String endDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        dateFormat.setLenient(false);
        try {
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);

            if (start != null && end != null) {
                if (end.before(start)) {
                    Toast.makeText(this, "End date must be after start date", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } else {
                Toast.makeText(this, "Invalid date(s) entered", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (ParseException e) {
            Toast.makeText(this, "Please enter dates in the correct format: DD-MM-YYYY", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void saveVacation() {
        String title = titleEditText.getText().toString().trim();
        String hotel = hotelEditText.getText().toString().trim();
        String startDate = startDateEditText.getText().toString().trim();
        String endDate = endDateEditText.getText().toString().trim();

        if (validateDates(startDate, endDate)) {
            Vacation vacation;

            if (vacationId == -1) {
                // New vacation - don't include vacationId, let Room handle it
                vacation = new Vacation(title, hotel, startDate, endDate);
            } else {
                // Existing vacation - include the vacationId
                vacation = new Vacation(vacationId, title, hotel, startDate, endDate);
            }

            new Thread(() -> {
                if (vacationId == -1) {
                    // Insert new vacation
                    vacationRepository.insertVacation(vacation);
                    runOnUiThread(() -> Toast.makeText(VacationDetails.this, "Vacation saved", Toast.LENGTH_SHORT).show());
                } else {
                    // Update existing vacation
                    vacationRepository.updateVacation(vacation);
                    runOnUiThread(() -> Toast.makeText(VacationDetails.this, "Vacation updated", Toast.LENGTH_SHORT).show());
                }

                // Schedule the alarms for start and end dates
                scheduleVacationAlarms(startDate, endDate);

                runOnUiThread(this::finish); // Close the activity after save/update
            }).start();
        }
    }

    private void deleteVacation() {
        if (vacationId != -1) {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Vacation")
                    .setMessage("Are you sure you want to delete this vacation?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> new Thread(() -> {
                        Vacation vacation = vacationRepository.getVacationById(vacationId);
                        if (vacation != null) {
                            vacationRepository.deleteVacation(vacation, new VacationRepository.DeleteVacationCallback() {
                                @Override
                                public void onSuccess() {
                                    runOnUiThread(() -> {
                                        Toast.makeText(VacationDetails.this, "Vacation deleted", Toast.LENGTH_SHORT).show();
                                        finish(); // Close the activity
                                    });
                                }

                                @Override
                                public void onExcursionEdit(Excursion excursion) {
                                    // Handle excursion edit if needed
                                }

                                @Override
                                public void onFailure(String message) {
                                    runOnUiThread(() -> Toast.makeText(VacationDetails.this, message, Toast.LENGTH_SHORT).show());
                                }
                            });
                        } else {
                            runOnUiThread(() -> Toast.makeText(VacationDetails.this, "No vacation to delete", Toast.LENGTH_SHORT).show());
                        }
                    }).start())
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }
    }

    private void updateVacation() {
        String title = titleEditText.getText().toString().trim();
        String hotel = hotelEditText.getText().toString().trim();
        String startDate = startDateEditText.getText().toString().trim();
        String endDate = endDateEditText.getText().toString().trim();

        if (validateDates(startDate, endDate)) {
            // Create a new Vacation object with the updated details
            Vacation vacation = new Vacation(vacationId, title, hotel, startDate, endDate);

            // Update the vacation in the repository
            new Thread(() -> {
                vacationRepository.updateVacation(vacation);
                runOnUiThread(() -> {
                    Toast.makeText(VacationDetails.this, "Vacation updated", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity after update
                });
            }).start();
        }
        Log.d("AlarmDebug", "Scheduling alarms for vacation with start date: " + startDate + " and end date: " + endDate);
        scheduleVacationAlarms(startDate, endDate);
    }

    private void scheduleVacationAlarms(String startDate, String endDate) {
        long timeInMillisStart = convertDateToMillis(startDate);
        long timeInMillisEnd = convertDateToMillis(endDate);

        // Schedule alarm for start date
        if (timeInMillisStart > 0) {
            scheduleExactAlarm(timeInMillisStart, "Vacation Start", "Your vacation is starting today!");
        }

        // Schedule alarm for end date
        if (timeInMillisEnd > 0) {
            scheduleExactAlarm(timeInMillisEnd, "Vacation End", "Your vacation is ending today!");
        }

    }

    // Helper method to convert date string to milliseconds
    private long convertDateToMillis(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        try {
            Date parsedDate = dateFormat.parse(date);
            if (parsedDate != null) {
                return parsedDate.getTime();
            } else {
                throw new ParseException("Unable to parse date", 0);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Invalid date format. Please use DD-MM-YYYY", Toast.LENGTH_SHORT).show();
            return -1; // Returning -1 to indicate an error in conversion
        }
    }

    private void scheduleExactAlarm(long timeInMillis, String title, String message) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            // Check if API level is 31 or higher
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    try {
                        Intent intent = new Intent(this, VacationAlarmReceiver.class);
                        intent.putExtra("title", title);
                        intent.putExtra("message", message);

                        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                        Log.d("AlarmDebug", "Alarm scheduled at " + timeInMillis + " for title: " + title);
                    } catch (SecurityException e) {
                        Log.e("AlarmDebug", "Failed to schedule exact alarm: " + e.getMessage());
                        Toast.makeText(this, "Unable to schedule exact alarm. Please enable this in settings.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e("AlarmDebug", "Exact alarm permission not granted.");
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(intent);
                }
            } else {
                // For devices with API level < 31
                Intent intent = new Intent(this, VacationAlarmReceiver.class);
                intent.putExtra("title", title);
                intent.putExtra("message", message);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                Log.d("AlarmDebug", "Alarm scheduled at " + timeInMillis + " for title: " + title);
            }
        } else {
            Log.e("AlarmDebug", "AlarmManager is null, cannot schedule alarm.");
        }
    }
}
