package com.example.d308vacationplanner.UnitTest;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class DateValidationTest {

    // Test for when the start date is after the end date
    @Test
    public void testStartDateAfterEndDate() {
        String startDate = "2024-08-28";
        String endDate = "2024-08-27";
        String result = validateDates(startDate, endDate);
        assertEquals("Start date must be before the end date.", result);
    }

    // Test for when the date is outside the vacation period
    @Test
    public void testDateOutsideVacationPeriod() {
        String date = "2024-09-01";
        String vacationStartDate = "2024-08-25";
        String vacationEndDate = "2024-08-30";
        String result = validateDateWithinVacation(date, vacationStartDate, vacationEndDate);
        assertEquals("Date must be within the vacation period.", result);
    }

    // Test for valid dates
    @Test
    public void testValidDates() {
        String startDate = "2024-08-25";
        String endDate = "2024-08-30";
        String result = validateDates(startDate, endDate);
        assertEquals("Success", result);
    }

    public String validateDates(String startDate, String endDate) {
        if (startDate.compareTo(endDate) > 0) {
            return "Start date must be before the end date.";
        }
        return "Success";
    }

    public String validateDateWithinVacation(String date, String vacationStartDate, String vacationEndDate) {
        if (date.compareTo(vacationStartDate) < 0 || date.compareTo(vacationEndDate) > 0) {
            return "Date must be within the vacation period.";
        }
        return "Success";
    }
}
