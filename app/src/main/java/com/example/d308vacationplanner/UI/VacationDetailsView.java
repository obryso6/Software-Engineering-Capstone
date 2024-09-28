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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.d308vacationplanner.R;
import com.example.d308vacationplanner.adapters.ExcursionAdapter;
import com.example.d308vacationplanner.database.VacationRepository;
import com.example.d308vacationplanner.entities.Excursion;
import com.example.d308vacationplanner.entities.Vacation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


// This activity provides a read-only view of the vacation details.
// It implements encapsulation by managing how data is displayed to the user.

public class VacationDetailsView extends AppCompatActivity implements VacationRepository.DeleteVacationCallback {

    private EditText vacationTitleEditText, hotelNameEditText, startDateEditText, endDateEditText;
    private VacationRepository vacationRepository;
    private int vacationId;
    private ExcursionAdapter excursionAdapter;
    private Calendar calendar;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacation_details_view);

        // Initialize views as EditText
        vacationTitleEditText = findViewById(R.id.textView_vacation_title);
        hotelNameEditText = findViewById(R.id.textView_hotel_name);
        startDateEditText = findViewById(R.id.textView_start_date);
        endDateEditText = findViewById(R.id.textView_end_date);
        calendar = Calendar.getInstance();

        // Set date picker dialog on the start date and end date EditTexts
        startDateEditText.setOnClickListener(v -> showDatePickerDialog(date -> startDateEditText.setText(date)));
        endDateEditText.setOnClickListener(v -> showDatePickerDialog(date -> endDateEditText.setText(date)));

        // Initialize the VacationRepository
        vacationRepository = new VacationRepository(getApplication());

        if (getIntent().hasExtra("vacationId")) {
            vacationId = getIntent().getIntExtra("vacationId", -1); // Assign vacationId here
        }

        // Initialize the RecyclerView for excursions
        RecyclerView recyclerViewExcursions = findViewById(R.id.recyclerView_excursions);
        recyclerViewExcursions.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the ExcursionAdapter with the action listener
        excursionAdapter = new ExcursionAdapter(new ExcursionAdapter.OnExcursionActionListener() {
            @Override
            public void onExcursionEdit(Excursion excursion) {
                showEditExcursionDialog(excursion);
            }

            @Override
            public void onExcursionDelete(Excursion excursion) {
                showDeleteExcursionDialog(excursion);
            }
        });

        // Set the adapter to the RecyclerView
        recyclerViewExcursions.setAdapter(excursionAdapter);

        // Load vacation details and excursions
        if (vacationId != -1) {
            loadVacationDetails(vacationId);
            loadExcursions(vacationId); // Load excursions for the specific vacation
        } else {
            // Handle the case where vacationId is invalid
            Toast.makeText(this, "Invalid vacation ID", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Enable the "Up" button in the ActionBar/Toolbar
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
        } else if (itemId == R.id.action_add_excursion) {  // This ID should match your menu item
            showAddExcursionDialog();  // This calls the method
            return true;
        } else if (itemId == R.id.action_alerts) {
            showAlertDialog();  // Show alert options for setting alarms
            return true;
        } else if (itemId == R.id.share_vacation) {  // Handle the share option
            shareVacationDetails();  // Call the method to share details
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadVacationDetails(int vacationId) {
        new Thread(() -> {
            Vacation vacation = vacationRepository.getVacationById(vacationId);
            runOnUiThread(() -> {
                if (vacation != null) {
                    vacationTitleEditText.setText(vacation.getVacationName());
                    hotelNameEditText.setText(vacation.getHotel());
                    startDateEditText.setText(vacation.getStartDate());
                    endDateEditText.setText(vacation.getEndDate());
                }
            });
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_vacation_details, menu);
        return true;
    }

    private void updateVacation() {
        String title = vacationTitleEditText.getText().toString().trim();
        String hotel = hotelNameEditText.getText().toString().trim();
        String startDate = startDateEditText.getText().toString().trim();
        String endDate = endDateEditText.getText().toString().trim();

        if (validateDates(startDate, endDate)) {
            Vacation vacation = new Vacation(vacationId, title, hotel, startDate, endDate);
            new Thread(() -> {
                vacationRepository.updateVacation(vacation);
                runOnUiThread(() -> Toast.makeText(VacationDetailsView.this, "Vacation updated", Toast.LENGTH_SHORT).show());
                finish();
            }).start();
        }
    }

    @Override
    public void onSuccess() {
        runOnUiThread(() -> {
            Toast.makeText(VacationDetailsView.this, "Vacation deleted successfully", Toast.LENGTH_SHORT).show();
            finish();
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExcursions(vacationId);  // Refresh the list of excursions
    }

    @Override
    public void onExcursionEdit(Excursion excursion) {
        // Logic to edit the excursion
        Intent intent = new Intent(this, ExcursionDetails.class);
        intent.putExtra("excursionId", excursion.getExcursionID());
        intent.putExtra("excursionName", excursion.getExcursionName());
        intent.putExtra("excursionDate", excursion.getExcursionDate());
        intent.putExtra("vacationId", excursion.getVacationID()); // Pass vacation ID as well
        startActivity(intent);
    }

    @Override
    public void onFailure(String message) {
        runOnUiThread(() -> Toast.makeText(VacationDetailsView.this, "Failed to delete vacation: " + message, Toast.LENGTH_SHORT).show());
    }

    private void shareVacationDetails() {
        String vacationDetails = "Vacation Title: " + vacationTitleEditText.getText().toString() + "\n"
                + "Hotel: " + hotelNameEditText.getText().toString() + "\n"
                + "Start Date: " + startDateEditText.getText().toString() + "\n"
                + "End Date: " + endDateEditText.getText().toString();

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, vacationDetails);
        shareIntent.setType("text/plain");

        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    private boolean validateDates(String startDate, String endDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        dateFormat.setLenient(false);
        try {
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);
            if (end != null && end.before(start)) {
                Toast.makeText(this, "End date must be after start date", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (ParseException e) {
            Toast.makeText(this, "Please enter dates in the correct format: DD-MM-YYYY", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void showAddExcursionDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_excursion, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView)
                .setTitle("Add Excursion")
                .setPositiveButton("Add", (dialog, which) -> {
                    EditText editTextExcursionName = dialogView.findViewById(R.id.editText_excursion_name);
                    EditText editTextExcursionDate = dialogView.findViewById(R.id.editText_excursion_date);

                    String excursionName = editTextExcursionName.getText().toString().trim();
                    String excursionDate = editTextExcursionDate.getText().toString().trim();

                    if (!excursionName.isEmpty() && !excursionDate.isEmpty()) {
                        validateExcursionDateWithinVacation(excursionDate, isValid -> {
                            if (isValid) {
                                Excursion excursion = new Excursion(excursionName, excursionDate, vacationId);
                                executorService.execute(() -> {
                                    vacationRepository.insertExcursion(excursion);
                                    runOnUiThread(() -> {
                                        Toast.makeText(VacationDetailsView.this, "Excursion added", Toast.LENGTH_SHORT).show();
                                        loadExcursions(vacationId);
                                        dialog.dismiss();
                                    });
                                });
                            } else {
                                Toast.makeText(VacationDetailsView.this, "Excursion date must be within the vacation period", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(VacationDetailsView.this, "Please enter both an excursion name and date", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void validateExcursionDateWithinVacation(String excursionDate, ValidationCallback callback) {
        executorService.execute(() -> {
            Vacation vacation = vacationRepository.getVacationById(vacationId);
            boolean isValid = false;
            if (vacation != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                try {
                    Date startDate = dateFormat.parse(vacation.getStartDate());
                    Date endDate = dateFormat.parse(vacation.getEndDate());
                    Date excursionParsedDate = dateFormat.parse(excursionDate);

                    if (excursionParsedDate != null && startDate != null && endDate != null) {
                        isValid = !excursionParsedDate.before(startDate) && !excursionParsedDate.after(endDate);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            final boolean result = isValid;
            runOnUiThread(() -> callback.onValidationResult(result));
        });
    }

    public interface ValidationCallback {
        void onValidationResult(boolean isValid);
    }


    private void setExcursionAlarm(String excursionDate, String excursionTitle) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        try {
            Date date = dateFormat.parse(excursionDate);
            if (date != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                Intent intent = new Intent(this, VacationAlarmReceiver.class);
                intent.putExtra("title", "Excursion Reminder");
                intent.putExtra("message", "Excursion: " + excursionTitle);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this,
                        (int) calendar.getTimeInMillis(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                if (alarmManager != null) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            } else {
                Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Date parsing failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadExcursions(int vacationId) {
        vacationRepository.getExcursionsForVacation(vacationId).observe(this, excursions -> {
            if (excursions != null && !excursions.isEmpty()) {
                Log.d("VacationDetailsView", "Excursions loaded: " + excursions.size());
                excursionAdapter.setExcursions(excursions);
            } else {
                Log.d("VacationDetailsView", "No excursions found");
                excursionAdapter.setExcursions(new ArrayList<>()); // Clear the list if no excursions are found
            }
        });
    }

    private void showEditExcursionDialog(Excursion excursion) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_excursion, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView)
                .setTitle("Edit Excursion")
                .setPositiveButton("Update", (dialog, which) -> {
                    EditText editTextExcursionName = dialogView.findViewById(R.id.editText_excursion_name);
                    EditText editTextExcursionDate = dialogView.findViewById(R.id.editText_excursion_date);

                    String newName = editTextExcursionName.getText().toString().trim();
                    String newDate = editTextExcursionDate.getText().toString().trim();

                    if (!newName.isEmpty()) {
                        excursion.setExcursionName(newName);
                        excursion.setExcursionDate(newDate);

                        // Update the excursion in the repository
                        new Thread(() -> {
                            vacationRepository.updateExcursion(excursion);
                            runOnUiThread(() -> {
                                Toast.makeText(VacationDetailsView.this, "Excursion updated", Toast.LENGTH_SHORT).show();
                                setExcursionAlarm(newDate, newName);  // Update the alarm for the excursion
                                loadExcursions(vacationId);  // Refresh excursions after update
                            });
                        }).start();
                    } else {
                        Toast.makeText(this, "Please enter an excursion name", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Delete", (dialog, which) -> {
                    showDeleteConfirmationDialog(excursion);  // Show confirmation dialog before deletion
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
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
                                        Toast.makeText(VacationDetailsView.this, "Vacation deleted", Toast.LENGTH_SHORT).show();
                                        finish(); // Close the activity
                                    });
                                }

                                @Override
                                public void onExcursionEdit(Excursion excursion) {
                                    // Handle excursion edit if needed
                                }

                                @Override
                                public void onFailure(String message) {
                                    runOnUiThread(() -> Toast.makeText(VacationDetailsView.this, message, Toast.LENGTH_SHORT).show());
                                }
                            });
                        } else {
                            runOnUiThread(() -> Toast.makeText(VacationDetailsView.this, "No vacation to delete", Toast.LENGTH_SHORT).show());
                        }
                    }).start())
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }
    }

    private void showDeleteConfirmationDialog(Excursion excursion) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Excursion")
                .setMessage("Are you sure you want to delete this excursion?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    vacationRepository.deleteExcursion(excursion);
                    loadExcursions(vacationId);  // Refresh the list after deletion
                    Toast.makeText(VacationDetailsView.this, "Excursion deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteExcursionDialog(Excursion excursion) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Excursion")
                .setMessage("Are you sure you want to delete this excursion?")
                .setPositiveButton("Delete", (dialog, which) -> new Thread(() -> {
                    vacationRepository.deleteExcursion(excursion);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Excursion deleted", Toast.LENGTH_SHORT).show();
                        loadExcursions(vacationId); // Refresh the list after deletion
                    });
                }).start())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Vacation Alerts");

        builder.setItems(new CharSequence[]
                        {"Set Start Date Alert", "Set End Date Alert"},
                (dialog, which) -> {
                    switch (which) {
                        case 0:
                            setAlarmForDate(startDateEditText.getText().toString(), "Vacation Start Alert", "Your vacation starts today!");
                            break;
                        case 1:
                            setAlarmForDate(endDateEditText.getText().toString(), "Vacation End Alert", "Your vacation ends today!");
                            break;
                    }
                });

        builder.create().show();
    }

    private void setAlarmForDate(String dateStr, String title, String message) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        try {
            Date date = dateFormat.parse(dateStr);
            if (date != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                setAlarm(calendar, title, message);
                Toast.makeText(this, "Alarm set for " + dateStr, Toast.LENGTH_SHORT).show();
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
                (int) calendar.getTimeInMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    checkAndRequestAlarmPermission();
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "Cannot schedule exact alarms without permission", Toast.LENGTH_LONG).show();
        }
    }

    private void checkAndRequestAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }

}
