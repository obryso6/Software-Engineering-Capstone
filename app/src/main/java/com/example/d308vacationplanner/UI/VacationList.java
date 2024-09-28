package com.example.d308vacationplanner.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.d308vacationplanner.R;
import com.example.d308vacationplanner.adapters.VacationAdapter;
import com.example.d308vacationplanner.database.VacationRepository;
import com.example.d308vacationplanner.entities.Excursion;
import com.example.d308vacationplanner.entities.Vacation;
import com.example.d308vacationplanner.reports.ReportGenerator;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

// This activity displays a list of vacations and allows users to add or select vacations for detailed view.
// It uses an adapter to manage list items and demonstrates encapsulation in handling vacation data.

public class VacationList extends AppCompatActivity {

    private static final String TAG = "VacationList";
    private VacationAdapter vacationAdapter;
    private static final int REQUEST_CODE_ADD_VACATION = 1;
    private VacationRepository vacationRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacation_list);

        Log.d(TAG, "onCreate: Initializing RecyclerView and Adapter");

        // Initialize the RecyclerView and Adapter
        RecyclerView recyclerView = findViewById(R.id.recyclerView_vacations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        vacationAdapter = new VacationAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(vacationAdapter);

        vacationRepository = new VacationRepository(getApplication());

        // Observe the vacation data
        vacationRepository.getAllVacations().observe(this, vacations -> {
            if (vacations != null) {
                Log.d(TAG, "onCreate: Received " + vacations.size() + " vacations");
                vacationAdapter.setVacations(vacations);
            } else {
                Log.d(TAG, "onCreate: No vacations found");
            }
        });
        // Set up the FAB to navigate to the VacationDetails activity (which acts as the form)
        FloatingActionButton fab = findViewById(R.id.fab_add_vacation);
        fab.setOnClickListener(view -> {
            Log.d(TAG, "onCreate: FAB clicked, navigating to VacationDetails");
            Intent intent = new Intent(VacationList.this, VacationDetails.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_VACATION);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_vacation_list, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle search submission if needed
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter the vacation list as the user types
                vacationAdapter.getFilter().filter(newText);
                return true;
            }
        });

        return true;  // Should return true here to display the menu
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.export_vacation_report) {
            Log.d(TAG, "onOptionsItemSelected: Export vacation report selected");
            exportVacationReport();  // Generate vacation report
            return true;
        } else if (itemId == R.id.export_excursion_report) {
            Log.d(TAG, "onOptionsItemSelected: Export excursion report selected");
            exportExcursionReport();  // Generate excursion report
            return true;
        } else if (itemId == R.id.logout) {
            Log.d(TAG, "onOptionsItemSelected: Log out selected");
            logOutUser();  // Handle user log out
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void logOutUser() {
        SharedPreferences preferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();

        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    private void exportVacationReport() {
        new Thread(() -> {
            Log.d(TAG, "exportVacationReport: Starting report generation");
            List<Vacation> vacationList = vacationRepository.getAllVacationsSync(); // Sync method
            if (vacationList != null) {
                Log.d(TAG, "exportVacationReport: Vacation list size: " + vacationList.size());
                if (!vacationList.isEmpty()) {
                    ReportGenerator.generateVacationListReport(this, vacationList);
                    Log.d(TAG, "exportVacationReport: Vacation report generated successfully");
                } else {
                    Log.d(TAG, "exportVacationReport: No vacations to export");
                    runOnUiThread(() -> Toast.makeText(this, "No vacations to export", Toast.LENGTH_SHORT).show());
                }
            } else {
                Log.d(TAG, "exportVacationReport: Vacation list is null");
                runOnUiThread(() -> Toast.makeText(this, "No vacations to export", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void exportExcursionReport() {
        new Thread(() -> {
            Log.d(TAG, "exportExcursionReport: Starting report generation");
            List<Excursion> excursionList = vacationRepository.getAllExcursionsSync(); // Assuming you have a sync method for all excursions
            if (excursionList != null && !excursionList.isEmpty()) {
                Log.d(TAG, "exportExcursionReport: Excursion list size: " + excursionList.size());
                ReportGenerator.generateExcursionDetailsReport(this, excursionList, vacationRepository.getVacationDao());
                Log.d(TAG, "exportExcursionReport: Excursion report generated successfully");
            } else {
                Log.d(TAG, "exportExcursionReport: No excursions to export");
                runOnUiThread(() -> Toast.makeText(this, "No excursions to export", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_VACATION && resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult: Vacation added, refreshing vacation list");
            // Refresh the vacation list after a new vacation is added
            vacationRepository.getAllVacations().observe(this, vacations -> {
                if (vacations != null) {
                    Log.d(TAG, "onActivityResult: Received " + vacations.size() + " vacations");
                    vacationAdapter.setVacations(vacations);
                } else {
                    Log.d(TAG, "onActivityResult: No vacations found");
                }
            });
        }
    }
}
