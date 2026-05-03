package com.release.klinikgaharumedika.model;

public class DashboardRecentVisit {

    private final String queueNumber;
    private final String patientName;
    private final String doctorName;
    private final String status;

    public DashboardRecentVisit(String queueNumber, String patientName, String doctorName, String status) {
        this.queueNumber = queueNumber;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.status = status;
    }

    public String getQueueNumber() {
        return queueNumber;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getStatus() {
        return status;
    }
}
