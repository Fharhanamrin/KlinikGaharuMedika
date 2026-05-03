package com.release.klinikgaharumedika.model;

public class DashboardQueueEntry {

    private final String queueNumber;
    private final String patientName;
    private final String doctorSummary;
    private final String status;

    public DashboardQueueEntry(String queueNumber, String patientName, String doctorSummary, String status) {
        this.queueNumber = queueNumber;
        this.patientName = patientName;
        this.doctorSummary = doctorSummary;
        this.status = status;
    }

    public String getQueueNumber() {
        return queueNumber;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getDoctorSummary() {
        return doctorSummary;
    }

    public String getStatus() {
        return status;
    }
}
