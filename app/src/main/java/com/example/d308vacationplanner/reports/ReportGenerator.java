package com.example.d308vacationplanner.reports;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.d308vacationplanner.dao.VacationDao;
import com.example.d308vacationplanner.entities.Excursion;
import com.example.d308vacationplanner.entities.Vacation;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// This class is responsible for generating reports that include multiple columns, rows, dates, and titles.
// It demonstrates the use of encapsulation and the Singleton pattern to ensure only one instance of report generation is active at a time.

public class ReportGenerator {

    public static void generateVacationListReport(Context context, List<Vacation> vacationList) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            if (vacationList == null || vacationList.isEmpty()) {
                Log.e("ReportGenerator", "No vacations available to export.");
                mainHandler.post(() -> Toast.makeText(context, "No vacations to export.", Toast.LENGTH_SHORT).show());
                return;
            }

            StringBuilder data = new StringBuilder();
            data.append("Vacation Name, Start Date, End Date, Hotel\n");

            for (Vacation vacation : vacationList) {
                data.append(vacation.getVacationName()).append(",");
                data.append(vacation.getStartDate()).append(",");
                data.append(vacation.getEndDate()).append(",");
                data.append(vacation.getHotel()).append("\n");
            }

            try {
                FileOutputStream out = context.openFileOutput("vacation_list_report.csv", Context.MODE_PRIVATE);
                out.write(data.toString().getBytes());
                out.close();

                mainHandler.post(() -> Toast.makeText(context, "Vacation List Report exported!", Toast.LENGTH_SHORT).show());
            } catch (IOException e) {
                e.printStackTrace();
                mainHandler.post(() -> Toast.makeText(context, "Failed to export Vacation List Report!", Toast.LENGTH_SHORT).show());
            }
        });

        executor.shutdown();
    }

    public static void generateExcursionDetailsReport(Context context, List<Excursion> excursionList, VacationDao vacationDao) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            if (excursionList == null || excursionList.isEmpty()) {
                Log.e("ReportGenerator", "No excursions available to export.");
                mainHandler.post(() -> Toast.makeText(context, "No excursions to export.", Toast.LENGTH_SHORT).show());
                return;
            }

            StringBuilder data = new StringBuilder();
            data.append("Excursion Title, Excursion Date, Vacation Name\n");

            for (Excursion excursion : excursionList) {
                String vacationName = vacationDao.getVacationNameById(excursion.getVacationID());
                data.append(excursion.getExcursionName()).append(",");
                data.append(excursion.getExcursionDate()).append(",");
                data.append(vacationName).append("\n");
            }

            try {
                FileOutputStream out = context.openFileOutput("excursion_details_report.csv", Context.MODE_PRIVATE);
                out.write(data.toString().getBytes());
                out.close();

                mainHandler.post(() -> Toast.makeText(context, "Excursion Details Report exported!", Toast.LENGTH_SHORT).show());
            } catch (IOException e) {
                e.printStackTrace();
                mainHandler.post(() -> Toast.makeText(context, "Failed to export Excursion Details Report!", Toast.LENGTH_SHORT).show());
            }
        });

        executor.shutdown();
    }
}