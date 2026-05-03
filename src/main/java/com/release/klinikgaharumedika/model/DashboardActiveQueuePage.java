package com.release.klinikgaharumedika.model;

import java.util.List;

public class DashboardActiveQueuePage {

    private final List<DashboardQueueEntry> queues;
    private final int currentPage;
    private final int pageSize;
    private final int totalItems;
    private final int totalPages;

    public DashboardActiveQueuePage(
            List<DashboardQueueEntry> queues,
            int currentPage,
            int pageSize,
            int totalItems,
            int totalPages
    ) {
        this.queues = queues;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
    }

    public List<DashboardQueueEntry> getQueues() {
        return queues;
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
