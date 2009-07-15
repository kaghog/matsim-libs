/* *********************************************************************** *
 * project: org.matsim.*
 * Plansgenerator.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package playground.dgrether.daganzosignal;

import org.apache.log4j.Logger;

import org.matsim.api.basic.v01.Id;
import org.matsim.api.basic.v01.TransportMode;
import org.matsim.core.api.experimental.ScenarioImpl;
import org.matsim.core.api.experimental.ScenarioImpl;
import org.matsim.core.api.experimental.population.PopulationBuilder;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.config.groups.CharyparNagelScoringConfigGroup.ActivityParams;
import org.matsim.core.network.LinkImpl;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.LegImpl;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PlanImpl;
import org.matsim.core.population.PopulationImpl;
import org.matsim.core.population.PopulationWriter;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.NodeNetworkRoute;
import org.matsim.core.scenario.ScenarioLoader;
import org.matsim.core.utils.misc.NetworkUtils;
import org.matsim.lanes.MatsimLaneDefinitionsWriter;
import org.matsim.lanes.basic.BasicLane;
import org.matsim.lanes.basic.BasicLaneDefinitions;
import org.matsim.lanes.basic.BasicLaneDefinitionsBuilder;
import org.matsim.lanes.basic.BasicLanesToLinkAssignment;
import org.matsim.signalsystems.MatsimSignalSystemConfigurationsWriter;
import org.matsim.signalsystems.MatsimSignalSystemsWriter;
import org.matsim.signalsystems.basic.BasicSignalGroupDefinition;
import org.matsim.signalsystems.basic.BasicSignalSystemDefinition;
import org.matsim.signalsystems.basic.BasicSignalSystems;
import org.matsim.signalsystems.basic.BasicSignalSystemsBuilder;
import org.matsim.signalsystems.config.BasicAdaptiveSignalSystemControlInfo;
import org.matsim.signalsystems.config.BasicSignalSystemConfiguration;
import org.matsim.signalsystems.config.BasicSignalSystemConfigurations;
import org.matsim.signalsystems.config.BasicSignalSystemConfigurationsBuilder;

import playground.dgrether.DgPaths;
import playground.dgrether.utils.IdFactory;

/**
 * @author dgrether
 * 
 */
public class DaganzoScenarioGenerator {

	private static final Logger log = Logger
			.getLogger(DaganzoScenarioGenerator.class);

	private static final String DAGANZOBASEDIR = DgPaths.SHAREDSVN + "studies/dgrether/daganzo/";
	
	private static final String DAGANZONETWORKFILE = DAGANZOBASEDIR
			+ "daganzoNetwork.xml";

	public static final String NETWORKFILE = DAGANZONETWORKFILE;

	private static final String PLANS1OUT = DAGANZOBASEDIR
			+ "daganzoPlansNormalRoute.xml";

	private static final String PLANS2OUT = DAGANZOBASEDIR
			+ "daganzoPlansAlternativeRoute.xml";

	private static final String CONFIG1OUT = DAGANZOBASEDIR
			+ "daganzoConfigNormalRoute.xml";

	private static final String CONFIG2OUT = DAGANZOBASEDIR
			+ "daganzoConfigAlternativeRoute.xml";

	public static final String LANESOUTPUTFILE = DAGANZOBASEDIR
		+ "daganzoLaneDefinitions.xml";

	public static final String SIGNALSYSTEMSOUTPUTFILE = DAGANZOBASEDIR
		+ "daganzoSignalSystems.xml";
	
	public static final String SIGNALSYSTEMCONFIGURATIONSOUTPUTFILE = DAGANZOBASEDIR 
		+ "daganzoSignalSystemsConfigs.xml";
	
	private static final String OUTPUTDIRECTORYNORMALROUTE = DAGANZOBASEDIR
		+ "output/normalRoute/";
	
	private static final String OUTPUTDIRECTORYALTERNATIVEROUTE = DAGANZOBASEDIR
		+ "output/alternativeRoute/";
	
	
	public String configOut, plansOut, outputDirectory;

	private static final boolean isAlternativeRouteEnabled = true;
	
	private static final boolean isUseSignalSystems = true;

	private static final int iterations = 0;

	private static final int iterations2 = 0;

	private static final String controllerClass = AdaptiveController.class.getCanonicalName();

	private Id id1, id2, id4, id5, id6;
	
	public DaganzoScenarioGenerator() {
		init();
	}

	private void init() {
		if (isAlternativeRouteEnabled) {
			plansOut = PLANS2OUT;
			configOut = CONFIG2OUT;
			outputDirectory = OUTPUTDIRECTORYALTERNATIVEROUTE;
		}
		else {
			plansOut = PLANS1OUT;
			configOut = CONFIG1OUT;
			outputDirectory = OUTPUTDIRECTORYNORMALROUTE;
		}
	}
	
	private void createIds(ScenarioImpl sc){
		id1 = sc.createId("1");
		id2 = sc.createId("2");
		id4 = sc.createId("4");
		id5 = sc.createId("5");
		id6 = sc.createId("6");
	}

	private void createScenario() {
		//create a scenario
		ScenarioImpl scenario = new ScenarioImpl();
		//get the config
		Config config = scenario.getConfig();
		//set the network input file to the config and load it
		config.network().setInputFile(NETWORKFILE);
		ScenarioLoader loader = new ScenarioLoader(scenario);
		loader.loadNetwork();
		//create some ids as members of the class for convenience reasons
		createIds(scenario);
		//create the plans and write them
		createPlans(scenario);
		PopulationWriter pwriter = new PopulationWriter(scenario.getPopulation(), plansOut);
		pwriter.write();
		if (isUseSignalSystems) {
			//enable lanes and signal system feature in config
			config.scenario().setUseLanes(true);
			config.scenario().setUseSignalSystems(true);
			//create the lanes and write them
			BasicLaneDefinitions lanes = createLanes(scenario);
			MatsimLaneDefinitionsWriter laneWriter = new MatsimLaneDefinitionsWriter(lanes);
			laneWriter.writeFile(LANESOUTPUTFILE);
			//create the signal systems and write them
			BasicSignalSystems signalSystems = createSignalSystems(scenario);
			MatsimSignalSystemsWriter ssWriter = new MatsimSignalSystemsWriter(signalSystems);
			ssWriter.writeFile(SIGNALSYSTEMSOUTPUTFILE);
			//create the signal system's configurations and write them
			BasicSignalSystemConfigurations ssConfigs = createSignalSystemsConfig(scenario);
			MatsimSignalSystemConfigurationsWriter ssConfigsWriter = new MatsimSignalSystemConfigurationsWriter(ssConfigs);	
			ssConfigsWriter.writeFile(SIGNALSYSTEMCONFIGURATIONSOUTPUTFILE);
		}
		
		//create and write the config with the correct paths to the files created above
		createConfig(config);
		ConfigWriter configWriter = new ConfigWriter(config, configOut);
		configWriter.write();

		log.info("scenario written!");
	}


	private void createPlans(ScenarioImpl scenario) {
		NetworkLayer network = scenario.getNetwork();
		PopulationImpl population = scenario.getPopulation();
		int firstHomeEndTime = 0;// 6 * 3600;
		int homeEndTime = firstHomeEndTime;
		LinkImpl l1 = network.getLink(scenario.createId("1"));
		LinkImpl l7 = network.getLink(scenario.createId("7"));
		PopulationBuilder builder = population.getBuilder();

		for (int i = 1; i <= 3600; i++) {
			PersonImpl p = (PersonImpl) builder.createPerson(scenario.createId(Integer
					.toString(i)));
			PlanImpl plan = (PlanImpl) builder.createPlan(p);
			p.addPlan(plan);
			// home
			// homeEndTime = homeEndTime + ((i - 1) % 3);
			if ((i - 1) % 3 == 0) {
				homeEndTime++;
			}

			ActivityImpl act1 = (ActivityImpl) builder.createActivityFromLinkId("h", l1.getId());
			act1.setEndTime(homeEndTime);
			plan.addActivity(act1);
			// leg to home
			LegImpl leg = (LegImpl) builder.createLeg(TransportMode.car);
			// TODO check this
			NetworkRoute route = new NodeNetworkRoute(l1, l7);
			if (isAlternativeRouteEnabled) {
				route
						.setNodes(l1, NetworkUtils.getNodes(network, "2 3 4 5 6"), l7);
			}
			else {
				route.setNodes(l1, NetworkUtils.getNodes(network, "2 3 5 6"), l7);
			}
			leg.setRoute(route);

			plan.addLeg(leg);
			
			ActivityImpl act2 = (ActivityImpl) builder.createActivityFromLinkId("h", l7.getId());
			act2.setLink(l7);
			plan.addActivity(act2);
			population.addPerson(p);
		}
	}
	
	private void createConfig(Config config) {
	// set scenario
		config.network().setInputFile(NETWORKFILE);
		config.plans().setInputFile(plansOut);
		config.network().setLaneDefinitionsFile(LANESOUTPUTFILE);
		config.signalSystems().setSignalSystemFile(SIGNALSYSTEMSOUTPUTFILE);
		config.signalSystems().setSignalSystemConfigFile(SIGNALSYSTEMCONFIGURATIONSOUTPUTFILE);
		
		// configure scoring for plans
		config.charyparNagelScoring().setLateArrival(0.0);
		config.charyparNagelScoring().setPerforming(6.0);
		// this is unfortunately not working at all....
		ActivityParams homeParams = new ActivityParams("h");
		// homeParams.setOpeningTime(0);
		config.charyparNagelScoring().addActivityParams(homeParams);
		// set it with f. strings
		config.charyparNagelScoring().addParam("activityType_0", "h");
		config.charyparNagelScoring().addParam("activityTypicalDuration_0",
				"24:00:00");

		// configure controler
		config.travelTimeCalculator().setTraveltimeBinSize(1);
		config.controler().setLastIteration(iterations + iterations2);
		config.controler().setOutputDirectory(outputDirectory);

		
		// configure simulation and snapshot writing
		config.simulation().setSnapshotFormat("otfvis");
		config.simulation().setSnapshotFile("cmcf.mvi");
		config.simulation().setSnapshotPeriod(60.0);
		// configure strategies for replanning
		config.strategy().setMaxAgentPlanMemorySize(4);
		StrategyConfigGroup.StrategySettings selectExp = new StrategyConfigGroup.StrategySettings(
				IdFactory.get(1));
		selectExp.setProbability(0.9);
		selectExp.setModuleName("ChangeExpBeta");
		config.strategy().addStrategySettings(selectExp);

		StrategyConfigGroup.StrategySettings reRoute = new StrategyConfigGroup.StrategySettings(
				IdFactory.get(2));
		reRoute.setProbability(0.1);
		reRoute.setModuleName("ReRoute");
		reRoute.setDisableAfter(iterations);
		config.strategy().addStrategySettings(reRoute);
	}
	

	private BasicLaneDefinitions createLanes(ScenarioImpl scenario) {
		BasicLaneDefinitions lanes = scenario.getLaneDefinitions();
		BasicLaneDefinitionsBuilder builder = lanes.getLaneDefinitionBuilder();
		//lanes for link 4
		BasicLanesToLinkAssignment lanesForLink4 = builder.createLanesToLinkAssignment(id4);
		BasicLane link4lane1 = builder.createLane(id1);
		link4lane1.addToLinkId(id6);
		lanesForLink4.addLane(link4lane1);
		lanes.addLanesToLinkAssignment(lanesForLink4);
		//lanes for link 5
		BasicLanesToLinkAssignment lanesForLink5 = builder.createLanesToLinkAssignment(id5);
		BasicLane link5lane1 = builder.createLane(id1);
		link5lane1.addToLinkId(id6);
		lanesForLink5.addLane(link5lane1);
		lanes.addLanesToLinkAssignment(lanesForLink5);
		return lanes;
	}

	
	private BasicSignalSystems createSignalSystems(ScenarioImpl scenario) {
		BasicSignalSystems systems = scenario.getSignalSystems();
		BasicSignalSystemsBuilder builder = systems.getSignalSystemsBuilder();
		//create the signal system no 1
		BasicSignalSystemDefinition definition = builder.createSignalSystemDefinition(id1);
		systems.addSignalSystemDefinition(definition);
		
		//create signal group for traffic on link 4 on lane 1 with toLink 6
		BasicSignalGroupDefinition groupLink4 = builder.createSignalGroupDefinition(id4, id1);
		groupLink4.addLaneId(id1);
		groupLink4.addToLinkId(id6);
		//assing the group to the system
		groupLink4.setLightSignalSystemDefinitionId(id1);
		//add the signalGroupDefinition to the container
		systems.addSignalGroupDefinition(groupLink4);
		
		//create signal group  with id no 2 for traffic on link 5 on lane 1 with toLink 6
		BasicSignalGroupDefinition groupLink5 = builder.createSignalGroupDefinition(id5, id2);
		groupLink5.addLaneId(id1);
		groupLink5.addToLinkId(id6);
		//assing the group to the system
		groupLink5.setLightSignalSystemDefinitionId(id1);
		
		//add the signalGroupDefinition to the container
		systems.addSignalGroupDefinition(groupLink5);
		
		return systems;
	}

	private BasicSignalSystemConfigurations createSignalSystemsConfig(
			ScenarioImpl scenario) {
		BasicSignalSystemConfigurations configs = scenario.getSignalSystemConfigurations();
		BasicSignalSystemConfigurationsBuilder builder = configs.getBuilder();
		
		BasicSignalSystemConfiguration systemConfig = builder.createSignalSystemConfiguration(id1);
		BasicAdaptiveSignalSystemControlInfo controlInfo = builder.createAdaptiveSignalSystemControlInfo();
		controlInfo.addSignalGroupId(id1);
		controlInfo.addSignalGroupId(id2);
		controlInfo.setAdaptiveControlerClass(controllerClass);
		systemConfig.setSignalSystemControlInfo(controlInfo);
		
		configs.getSignalSystemConfigurations().put(systemConfig.getSignalSystemId(), systemConfig);
		
		return configs;
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		try {
			new DaganzoScenarioGenerator().createScenario();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
