/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;

/**
 * AppCloudlet class represents an application which user submit for execution within a datacenter. It
 * consist of several {@link NetworkCloudlet NetworkCloudlets}. The solely purpose of this class is to function
 * as a wrapper to keep track of the cloudlets which model a certain workflow, the owner of the app and to check
 * for deadline misses.
 *
 * @author Remo Andreoli
 * @since CloudSim Toolkit 7.o
 *
 * 
 * //TODO The attributes have to be defined as private.
 */
public class AppCloudlet {

	public int type;

	public int appID;

	public int userId;

        /**
         * The list of {@link NetworkCloudlet} that this AppCloudlet represents.
         */
	public ArrayList<NetworkCloudlet> cList;

	public double deadline;

	public static final int APP_MC = 1;

	public static final int APP_Workflow = 3;

	public AppCloudlet(int type, int appID, double deadline, int userId) {
		// Access these parameters from within cList, NOT the appCloudlet object
		this.type = type;
		this.appID = appID;
		this.deadline = deadline;
		this.userId = userId;

		cList = new ArrayList<>();
	}

	/**
	 * Get all the cloudlets that starts the worklow activity
	 * Assumption: the stages are in order of (sequential) execution
	 */
	public List<NetworkCloudlet> getSourceCloudlets() {
		return cList.stream()
					.filter(networkCloudlet -> networkCloudlet.stages.get(0).getType() != TaskStage.TaskStageStatus.WAIT_RECV)
					.filter(networkCloudlet -> networkCloudlet.stages.get(0).getType() != TaskStage.TaskStageStatus.WAIT_SEND)
					.toList();
	}

	/**
	 * Get all the cloudlets that ends the worklow activity
	 * Assumption: the stages are in order of (sequential) execution
	 */
	public List<NetworkCloudlet> getSinkCloudlets() {
		return cList.stream()
				.filter(networkCloudlet -> networkCloudlet.stages.get(networkCloudlet.stages.size()-1).getType() != TaskStage.TaskStageStatus.WAIT_RECV)
				.filter(networkCloudlet -> networkCloudlet.stages.get(networkCloudlet.stages.size()-1).getType() != TaskStage.TaskStageStatus.WAIT_SEND)
				.toList();
	}

	/**
	 * Compute the lateness of the appCloudlet in function of the specified deadline.
	 * A negative value express a deadline miss
	 * @return lateness
	 */
	public double getLateness() {
		double worstFinishTime = getSinkCloudlets().stream()
				.mapToDouble(Cloudlet::getFinishTime).max().orElse(0.0);

		return Math.min(0, deadline - worstFinishTime);
	}

	public boolean isDeadlineMissed() {
		return getLateness() < 0;
	}
}
