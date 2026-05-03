package com.release.klinikgaharumedika.model;

import java.math.BigDecimal;

public class DashboardData {

    private final int visitsToday;
    private final int visitsYesterday;
    private final int totalPatients;
    private final int newPatientsThisMonth;
    private final BigDecimal todayRevenue;
    private final BigDecimal averageDailyRevenue;
    private final int lowStockItems;

    public DashboardData(
            int visitsToday,
            int visitsYesterday,
            int totalPatients,
            int newPatientsThisMonth,
            BigDecimal todayRevenue,
            BigDecimal averageDailyRevenue,
            int lowStockItems
    ) {
        this.visitsToday = visitsToday;
        this.visitsYesterday = visitsYesterday;
        this.totalPatients = totalPatients;
        this.newPatientsThisMonth = newPatientsThisMonth;
        this.todayRevenue = todayRevenue;
        this.averageDailyRevenue = averageDailyRevenue;
        this.lowStockItems = lowStockItems;
    }

    public static DashboardData empty() {
        return new DashboardData(
                0,
                0,
                0,
                0,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0
        );
    }

    public int getVisitsToday() {
        return visitsToday;
    }

    public int getVisitsYesterday() {
        return visitsYesterday;
    }

    public int getTotalPatients() {
        return totalPatients;
    }

    public int getNewPatientsThisMonth() {
        return newPatientsThisMonth;
    }

    public BigDecimal getTodayRevenue() {
        return todayRevenue;
    }

    public BigDecimal getAverageDailyRevenue() {
        return averageDailyRevenue;
    }

    public int getLowStockItems() {
        return lowStockItems;
    }
}
