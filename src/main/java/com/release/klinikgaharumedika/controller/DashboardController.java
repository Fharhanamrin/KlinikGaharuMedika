package com.release.klinikgaharumedika.controller;

import com.release.klinikgaharumedika.model.DashboardActiveQueuePage;
import com.release.klinikgaharumedika.model.DashboardData;
import com.release.klinikgaharumedika.model.DashboardRecentVisitPage;
import com.release.klinikgaharumedika.repository.DashboardRepository;
import java.sql.SQLException;

public class DashboardController {

    private final DashboardRepository dashboardRepository;

    public DashboardController() {
        this.dashboardRepository = new DashboardRepository();
    }

    public DashboardData loadDashboardStats() throws SQLException {
        return dashboardRepository.loadDashboardStats();
    }

    public DashboardRecentVisitPage loadRecentVisitPage(int page, int pageSize, String searchQuery) throws SQLException {
        return dashboardRepository.loadRecentVisitPage(page, pageSize, searchQuery);
    }

    public DashboardActiveQueuePage loadActiveQueuePage(int page, int pageSize) throws SQLException {
        return dashboardRepository.loadActiveQueuePage(page, pageSize);
    }
}
