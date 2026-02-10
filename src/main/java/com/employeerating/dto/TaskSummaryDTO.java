package com.employeerating.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskSummaryDTO {

	 private Long taskId;
	private String taskName;
	private LocalDate workDate;
}
