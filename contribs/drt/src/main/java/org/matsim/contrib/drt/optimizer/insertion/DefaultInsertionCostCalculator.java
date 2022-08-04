/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2017 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.contrib.drt.optimizer.insertion;

import static org.matsim.contrib.drt.optimizer.insertion.InsertionGenerator.Insertion;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.drt.optimizer.Waypoint;
import org.matsim.contrib.drt.optimizer.insertion.InsertionDetourTimeCalculator.DetourTimeInfo;
import org.matsim.contrib.drt.passenger.AcceptedDrtRequest;
import org.matsim.contrib.drt.passenger.DrtRequest;
import org.matsim.contrib.dvrp.optimizer.Request;

import java.util.List;
import java.util.Map;

/**
 * @author michalm
 */
public class DefaultInsertionCostCalculator implements InsertionCostCalculator {
	private final CostCalculationStrategy costCalculationStrategy;

	public DefaultInsertionCostCalculator(CostCalculationStrategy costCalculationStrategy) {
		this.costCalculationStrategy = costCalculationStrategy;
	}

	/**
	 * As the main goal is to minimise bus operation time, this method calculates how much longer the bus will operate
	 * after insertion. By returning INFEASIBLE_SOLUTION_COST, the insertion is considered infeasible
	 * <p>
	 * The insertion is invalid if some maxTravel/Wait constraints for the already scheduled requests are not fulfilled.
	 * This is denoted by returning INFEASIBLE_SOLUTION_COST.
	 * <p>
	 *
	 * @param drtRequest the request
	 * @param insertion  the insertion to be considered here
	 * @return cost of insertion (INFEASIBLE_SOLUTION_COST represents an infeasible insertion)
	 */
	@Override
	public double calculate(DrtRequest drtRequest, Insertion insertion, DetourTimeInfo detourTimeInfo) {
		var vEntry = insertion.vehicleEntry;

		//@kaghog include a checker for reserved seats such that vehicles with not enough seats are not considered
		List<Id<Person>> reservedPassengers = drtRequest.getReservedPassengerIds();
		int requestedSeats = reservedPassengers == null ? 0 : reservedPassengers.size();

		//Get the occupancy of the vehicles taking account of their reserved passengers
		int occupancy = 0;
		for(Waypoint.Stop stop: vEntry.stops){

			//get the number of passengers actually in the vehicle at this point
			if (stop.task.getDropoffRequests() != null) {
				for (AcceptedDrtRequest request : stop.task.getDropoffRequests().values()){
					DrtRequest initialDrtRequest = request.getRequest();
					occupancy += initialDrtRequest.getReservedPassengerIds() == null ? 1 : initialDrtRequest.getReservedPassengerIds().size();
				}
			}

			//@kaghog is it possible that on the way to pickup a request can come in and the vehicle becomes full?
			//needs further investigation as considering this duplicates the occupancy

		}
		//System.out.println("@kaghog the start occupancy, DefaultInsertionCostCalculator: " + occupancy);

		//toDo how can one account for no rejection scenario so that this trip is tried again like is done for maxWait time and detour time?

		if (vEntry.getSlackTime(insertion.pickup.index) < detourTimeInfo.pickupDetourInfo.pickupTimeLoss
				|| vEntry.getSlackTime(insertion.dropoff.index) < detourTimeInfo.getTotalTimeLoss()
				|| (requestedSeats > 1 && vEntry.vehicle.getCapacity() - occupancy < requestedSeats)
			) {
			//System.out.println("@kaghog rejecting a vehicle, DefaultInsertionCostCalculator");
			return INFEASIBLE_SOLUTION_COST;
		}

		//System.out.println("@kaghog found a vehicle, DefaultInsertionCostCalculator");
		return costCalculationStrategy.calcCost(drtRequest, insertion, detourTimeInfo);
	}
}
