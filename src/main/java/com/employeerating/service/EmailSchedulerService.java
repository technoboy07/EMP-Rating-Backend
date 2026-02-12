package com.employeerating.service;

import java.util.List;

public interface EmailSchedulerService {
    void sendEmailToTl();
	void sendEmailParticular();
	void sendEmailToPm();
	void sendEmailToPmo();
	void sendEmailToHr();
    List<String> deletePreviousRatings(); 
    void sendMonthlyReports();
    void sendWeeklyReports();
}
