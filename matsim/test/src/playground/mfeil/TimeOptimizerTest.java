/* *********************************************************************** *
 * project: org.matsim.*
 * TimeOptimizerTest.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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

package playground.mfeil;

import org.matsim.testcases.MatsimTestCase;

public class TimeOptimizerTest extends MatsimTestCase{
	/*
	private static final Logger log = Logger.getLogger(TimeOptimizerTest.class);
	private NetworkLayer network = null;
	private Facilities facilities = null;
	private Population population = null;
	final String TEST_PERSON_ID = "1";
	private PlansCalcRouteLandmarks router;
	private PreProcessLandmarks	preProcessRoutingData;
	private TravelTimeCalculator tTravelEstimator;
	private TravelCost travelCostEstimator;
	private DepartureDelayAverageCalculator depDelayCalc;
	private Events events;
	private LegTravelTimeEstimator ltte;
	private TimeOptimizer testee;

	protected void setUp() throws Exception {

		super.setUp();
	
		super.loadConfig("input/config_chessboard.xml");
		
		log.info("Reading facilities xml file...");
		facilities = (Facilities)Gbl.getWorld().createLayer(Facilities.LAYER_TYPE,null);
		new MatsimFacilitiesReader(facilities).readFile(Gbl.getConfig().facilities().getInputFile());
		log.info("Reading facilities xml file...done.");

		log.info("Reading network xml file...");
		network = (NetworkLayer)Gbl.getWorld().createLayer(NetworkLayer.LAYER_TYPE, null);
		new MatsimNetworkReader(network).readFile(Gbl.getConfig().network().getInputFile());
		log.info("Reading network xml file...done.");

		log.info("Reading plans xml file...");
		population = new Population(Population.NO_STREAMING);
		PopulationReader plansReader = new MatsimPopulationReader(population);
		plansReader.readFile(Gbl.getConfig().plans().getInputFile());
		population.printPlansCount();
		log.info("Reading plans xml file...done.");
		
		this.preProcessRoutingData 	= new PreProcessLandmarks(new FreespeedTravelTimeCost());
		this.preProcessRoutingData.run(network);
	
		this.router = new PlansCalcRouteLandmarks (network, this.preProcessRoutingData, new FreespeedTravelTimeCost(), new FreespeedTravelTimeCost());
	
		this.tTravelEstimator = new TravelTimeCalculator(network, 900);
		this.travelCostEstimator = new TravelTimeDistanceCostCalculator(this.tTravelEstimator);
		this.depDelayCalc = new DepartureDelayAverageCalculator(network, 900);

		this.events = new Events();
		events.addHandler(tTravelEstimator);
		events.addHandler(depDelayCalc);

		this.ltte = new CetinCompatibleLegTravelTimeEstimator(this.tTravelEstimator, this.travelCostEstimator, this.depDelayCalc, this.network);
		
		this.testee = new TimeOptimizer (ltte, new PlanScorer(new JohScoringFunctionFactory()));
		

	}
	
	public void testCleanSchedule (){
		
		Plan origPlan = new Plan (population.getPerson(this.TEST_PERSON_ID));
		origPlan.copyPlan(population.getPerson(this.TEST_PERSON_ID).getPlans().get(0));
		this.router.run(origPlan);
		Plan newPlan = new Plan (population.getPerson(this.TEST_PERSON_ID));
		newPlan.copyPlan(population.getPerson(this.TEST_PERSON_ID).getPlans().get(0));
		this.router.run(newPlan);
		
		this.testee.cleanSchedule(((Act)origPlan.getActsLegs().get(0)).getEndTime(), origPlan);
		
		for (int i=1;i<newPlan.getActsLegs().size();i++){
			if (i%2==0){
				((Act)newPlan.getActsLegs().get(i)).setStartTime(MatsimRandom.random.nextDouble());
				((Act)newPlan.getActsLegs().get(i)).setEndTime(MatsimRandom.random.nextDouble());
			}
			else {
				((Leg)newPlan.getActsLegs().get(i)).setDepartureTime(MatsimRandom.random.nextDouble());
				((Leg)newPlan.getActsLegs().get(i)).setArrivalTime(MatsimRandom.random.nextDouble());
			}
		}
		
		this.testee.cleanSchedule(((Act)newPlan.getActsLegs().get(0)).getEndTime(), newPlan);
		for (int i=0;i<newPlan.getActsLegs().size();i++){
			if (i%2==0){
				assertEquals(((Act)origPlan.getActsLegs().get(i)).getStartTime(), ((Act)newPlan.getActsLegs().get(i)).getStartTime());
				assertEquals(((Act)origPlan.getActsLegs().get(i)).getStartTime(), ((Act)newPlan.getActsLegs().get(i)).getStartTime());
			}
			else{
				assertEquals(((Leg)origPlan.getActsLegs().get(i)).getDepartureTime(), ((Leg)newPlan.getActsLegs().get(i)).getDepartureTime());
				assertEquals(((Leg)origPlan.getActsLegs().get(i)).getArrivalTime(), ((Leg)newPlan.getActsLegs().get(i)).getArrivalTime());
			
			}
		}
	}
	
	public void testCopyActslegs (){
		
		Plan plan = new Plan (population.getPerson(this.TEST_PERSON_ID));
		plan.copyPlan(population.getPerson(this.TEST_PERSON_ID).getPlans().get(0));
		
		NodeCarRoute route = new NodeCarRoute();
		route.setNodes("1 2 3");
		
		// but flat copy of leg routes so that change in plan does also affect newPlan
		((Leg)(plan.getActsLegs().get(1))).setRoute(route);
		
		double planActTime = ((Act)(plan.getActsLegs().get(0))).getEndTime();
		
		ArrayList<Object> newPlanActsLegs = this.testee.copyActsLegs(plan.getActsLegs()); 
		
		// deep copy of acts (complete act) and leg times (only times!) so that time change in newPlan does not affect plan
		((Act)(newPlanActsLegs.get(0))).setEndTime(0.0);
		
		// but flat copy of leg routes so that change in plan does also affect newPlan
		route.setNodes("3 2 1");
		
		assertEquals(((Act)plan.getActsLegs().get(0)).getEndTime(), planActTime);
		assertEquals(((Act)(newPlanActsLegs.get(0))).getEndTime(), 0.0);
		assertEquals(((Leg)(plan.getActsLegs().get(1))).getRoute(), route);
		assertEquals(((Leg)(newPlanActsLegs.get(1))).getRoute(), route);
	}
	
	public void testIncreaseTime (){
		
		PlanomatXPlan plan = new PlanomatXPlan (population.getPerson(this.TEST_PERSON_ID));
		plan.copyPlan(population.getPerson(this.TEST_PERSON_ID).getPlans().get(0));
		
		this.router.run(plan);	// conducts routing and sets travel times
		this.testee.cleanActs(plan.getActsLegs());	// adjusts the act durations according to travel times
		
		ArrayList<Object> alIn = this.testee.copyActsLegs(plan.getActsLegs()); 
		ArrayList<Object> alCheck = this.testee.copyActsLegs(plan.getActsLegs()); 
		
		this.testee.increaseTime(plan, alIn, 2, 4);
		this.testee.cleanActs(alIn);
		
		((Act)(alCheck.get(2))).setDuration(((Act)(alCheck.get(2))).getDuration()+this.testee.getOffset());
		((Act)(alCheck.get(2))).setEndTime(((Act)(alCheck.get(2))).getEndTime()+this.testee.getOffset());
		
		((Leg)(alCheck.get(3))).setTravelTime(this.ltte.getLegTravelTimeEstimation(plan.getPerson().getId(), ((Act)(alCheck.get(2))).getEndTime(), (Act)(alCheck.get(2)), (Act)(alCheck.get(4)), (Leg)(alCheck.get(3))));
		((Act)(alCheck.get(4))).setDuration(((Act)(alCheck.get(4))).getEndTime()-(((Act)(alCheck.get(2))).getEndTime()+((Leg)(alCheck.get(3))).getTravelTime()));
	
		for (int i=0;i<alIn.size();i+=2){
			assertEquals(((Act)(alIn.get(i))).getDuration(), ((Act)(alCheck.get(i))).getDuration());
		}
		
		
	}
	
	private void testDecreaseTime (){
		
		PlanomatXPlan plan = new PlanomatXPlan (population.getPerson(this.TEST_PERSON_ID));
		plan.copyPlan(population.getPerson(this.TEST_PERSON_ID).getPlans().get(0));
		
		this.router.run(plan);	// conducts routing and sets travel times
		this.testee.cleanActs(plan.getActsLegs());	// adjusts the act durations according to travel times
		
		ArrayList<Object> alIn = this.testee.copyActsLegs(plan.getActsLegs()); 
		ArrayList<Object> alCheck = this.testee.copyActsLegs(plan.getActsLegs()); 
		
		this.testee.decreaseTime(plan, alIn, 2, 4);
		this.testee.cleanActs(alIn);
		
		((Act)(alCheck.get(2))).setDuration(((Act)(alCheck.get(2))).getDuration()-this.testee.getOffset());
		((Act)(alCheck.get(2))).setEndTime(((Act)(alCheck.get(2))).getEndTime()-this.testee.getOffset());
		
		((Leg)(alCheck.get(3))).setTravelTime(this.ltte.getLegTravelTimeEstimation(plan.getPerson().getId(), ((Act)(alCheck.get(2))).getEndTime(), (Act)(alCheck.get(2)), (Act)(alCheck.get(4)), (Leg)(alCheck.get(3))));
		((Act)(alCheck.get(4))).setDuration(((Act)(alCheck.get(4))).getEndTime()-(((Act)(alCheck.get(2))).getEndTime()+((Leg)(alCheck.get(3))).getTravelTime()));
	
		for (int i=0;i<alIn.size();i+=2){
			log.info(((Act)(alCheck.get(i))).getDuration());
		}
		
		for (int i=0;i<alIn.size();i+=2){
			log.warn("Iteration "+i);
			assertEquals(((Act)(alIn.get(i))).getDuration(), ((Act)(alCheck.get(i))).getDuration());
		}
	}
	
	public void testRun (){
		Plan plan = new Plan (population.getPerson(this.TEST_PERSON_ID));
		plan.copyPlan(population.getPerson(this.TEST_PERSON_ID).getPlans().get(0));
		
		this.router.run(plan);
		
		Plan targetPlan = population.getPerson(this.TEST_PERSON_ID).getPlans().get(1);
		
		this.testee.run(plan);
		
		// Test whether differences are smaller than 10 sec because leg estimator causes minor time differences
		for (int i=0;i<plan.getActsLegs().size();i++){
			if (i%2==0){
				assert(java.lang.Math.abs(((Act)(plan.getActsLegs().get(i))).getDuration()-((Act)(targetPlan.getActsLegs().get(i))).getDuration())<1);
				assert(java.lang.Math.abs(((Act)(plan.getActsLegs().get(i))).getStartTime()-((Act)(targetPlan.getActsLegs().get(i))).getStartTime())<1);
				assert(java.lang.Math.abs(((Act)(plan.getActsLegs().get(i))).getEndTime()-((Act)(targetPlan.getActsLegs().get(i))).getEndTime())<1);
			}
			else {
				assert(java.lang.Math.abs(((Leg)(plan.getActsLegs().get(i))).getTravelTime()-((Leg)(targetPlan.getActsLegs().get(i))).getTravelTime())<1);
				assert(java.lang.Math.abs(((Leg)(plan.getActsLegs().get(i))).getDepartureTime()-((Leg)(targetPlan.getActsLegs().get(i))).getDepartureTime())<1);
				assert(java.lang.Math.abs(((Leg)(plan.getActsLegs().get(i))).getArrivalTime()-((Leg)(targetPlan.getActsLegs().get(i))).getArrivalTime())<1);
			}
		}
	}
	
	public void testCheck () {
		Plan plan = new Plan (population.getPerson(this.TEST_PERSON_ID));
		plan.copyPlan(population.getPerson(this.TEST_PERSON_ID).getPlans().get(0));
		for (int i=0;i<plan.getActsLegs().size();i++){
			if (i%2==1) {
				System.out.print(((Leg)(plan.getActsLegs().get(i))).getTravelTime()+" "); 
				System.out.print(((Leg)(plan.getActsLegs().get(i))).getRoute().getTravelTime()+" ");
				System.out.print(((Leg)(plan.getActsLegs().get(i))).getRoute().getDist()+" ");
				System.out.print(((Leg)(plan.getActsLegs().get(i))).getMode()+", "); 
				System.out.print(((Leg)(plan.getActsLegs().get(i))).getRoute().getStartLinkId()+", ");
				System.out.print(((Leg)(plan.getActsLegs().get(i))).getRoute().getEndLinkId()+", ");
				for (int j=0;j<((Leg)(plan.getActsLegs().get(i))).getRoute().getLinkIds().size();j++ ){
					System.out.print(((Leg)(plan.getActsLegs().get(i))).getRoute().getLinkIds().get(j)+" "); 
				}
				System.out.println();
			}
		}
	}*/
}
