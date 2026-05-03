package com.release.klinikgaharumedika.model;

import java.util.List;

public class DashboardRecentVisitPage {

    private final List<DashboardRecentVisit> visits;
    private final int currentPage;
    private final int pageSize;
    private final int totalItems;
    private final int totalPages;

    public DashboardRecentVisitPage(
            List<DashboardRecentVisit> visits,
            int currentPage,
            int pageSize,
            int totalItems,
            int totalPages
    ) {
        this.visits = visits;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
    }

    public List<DashboardRecentVisit> getVisits() {
        return visits;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
