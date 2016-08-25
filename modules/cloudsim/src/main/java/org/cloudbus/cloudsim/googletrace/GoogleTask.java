package org.cloudbus.cloudsim.googletrace;

public class GoogleTask {

	private int id;
	private double submitTime, runtime, cpuReq, memReq, startTime, finishTime;
	
	public GoogleTask(int id, double submitTime, double runTime,
			double cpuReq, double memReq) {
		this.id= id;
		this.submitTime = submitTime;
		this.runtime = runTime;
		this.cpuReq = cpuReq;
		this.memReq = memReq;
	}

	public int getId() {
		return id;
	}

	public double getSubmitTime() {
		return submitTime;
	}

	public double getRuntime() {
		return runtime;
	}

	public double getCpuReq() {
		return cpuReq;
	}

	public double getMemReq() {
		return memReq;
	}

	public double getStartTime() {
		return startTime;
	}

	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}

	public double getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(double finishTime) {
		this.finishTime = finishTime;
	}
	
}
