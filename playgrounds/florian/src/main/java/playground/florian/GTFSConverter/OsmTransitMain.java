package playground.florian.GTFSConverter;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.matsim.core.api.experimental.network.NetworkWriter;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.pt.transitSchedule.api.TransitScheduleWriter;
import org.openstreetmap.osmosis.core.filter.common.IdTrackerType;
import org.openstreetmap.osmosis.core.xml.common.CompressionMethod;

import playground.mzilske.osm.JOSMTolerantFastXMLReader;
import playground.mzilske.osm.NetworkSink;

public class OsmTransitMain {
	
	private final static Logger log = Logger.getLogger(OsmTransitMain.class);
	
	String inFile;
	String fromCoordSystem;
	String toCoordSystem;
	String networkOutFile;
	String transitScheduleOutFile;
	
	public OsmTransitMain(String inFile, String fromCoordSystem, String toCoordSystem, String networkOutFile, String transitScheduleOutFile){
		this.inFile = inFile;
		this.fromCoordSystem = fromCoordSystem;
		this.toCoordSystem = toCoordSystem;
		this.networkOutFile = networkOutFile;
		this.transitScheduleOutFile = transitScheduleOutFile;
	}
	
	public static void main(String[] args) throws IOException {
		new OsmTransitMain("../../osm/torino.osm", TransformationFactory.WGS84, TransformationFactory.WGS84, "../../osm/torino/network.xml", "../../osm/torino/transitSchedule_tram.xml").convertOsm2Matsim();
	}
	
	public void convertOsm2Matsim(){
//		convertOsm2Matsim(new String[]{"bus","light_rail","subway","train","tram"});
		convertOsm2Matsim(new String[]{"tram"});
	}
		
	public void convertOsm2Matsim(String[] transitFilter){
		
		log.info("Start...");		
		ScenarioImpl scenario = (ScenarioImpl) ScenarioUtils.createScenario(ConfigUtils.createConfig());
		scenario.getConfig().scenario().setUseTransit(true);
		JOSMTolerantFastXMLReader reader = new JOSMTolerantFastXMLReader(new File(inFile), false, CompressionMethod.None);		

		CoordinateTransformation coordinateTransformation = TransformationFactory.getCoordinateTransformation(this.fromCoordSystem, this.toCoordSystem);
		NetworkSink networkGenerator = new NetworkSink(scenario.getNetwork(), coordinateTransformation);
		
		// Anmerkung trunk, primary und secondary sollten in Bln als ein Typ behandelt werden
		
		// Autobahn
		networkGenerator.setHighwayDefaults(1, "motorway",      2,  100.0/3.6, 1.2, 2000, true); // 70
		networkGenerator.setHighwayDefaults(1, "motorway_link", 1,  60.0/3.6, 1.2, 1500, true); // 60
		// Pseudoautobahn
		networkGenerator.setHighwayDefaults(2, "trunk",         2,  50.0/3.6, 0.5, 1000, false); // 45
		networkGenerator.setHighwayDefaults(2, "trunk_link",    1,  50.0/3.6, 0.5, 1000, false); // 40
		// Durchgangsstrassen
		networkGenerator.setHighwayDefaults(3, "primary",       1,  50.0/3.6, 0.5, 1000, false); // 35
		networkGenerator.setHighwayDefaults(3, "primary_link",  1,  50.0/3.6, 0.5, 1000, false); // 30
		
		// Hauptstrassen
		networkGenerator.setHighwayDefaults(4, "secondary",     1,  50.0/3.6, 0.5, 1000, false); // 30
		// Weitere Hauptstrassen
		networkGenerator.setHighwayDefaults(5, "tertiary",      1,  30.0/3.6, 0.8,  600, false); // 25 
		// bis hier ca wip
		
		// Nebenstrassen
		networkGenerator.setHighwayDefaults(6, "minor",         1,  30.0/3.6, 0.8,  600, false); // nix
		// Alles Moegliche, vor allem Nebenstrassen auf dem Land, meist keine 30er Zone 
		networkGenerator.setHighwayDefaults(6, "unclassified",  1,  30.0/3.6, 0.8,  600, false);
		// Nebenstrassen, meist 30er Zone
		networkGenerator.setHighwayDefaults(6, "residential",   1,  30.0/3.6, 0.6,  600, false);
		// Spielstrassen
		networkGenerator.setHighwayDefaults(6, "living_street", 1,  15.0/3.6, 1.0,  300, false);
		
		log.info("Reading " + this.inFile);
		TransitNetworkSink transitNetworkSink = new TransitNetworkSink(scenario.getNetwork(), scenario.getTransitSchedule(), coordinateTransformation, IdTrackerType.BitSet);
		transitNetworkSink.setTransitModes(transitFilter);
		reader.setSink(networkGenerator);
		networkGenerator.setSink(transitNetworkSink);
		reader.run();
		log.info("Writing network to " + this.networkOutFile);
		new NetworkWriter(scenario.getNetwork()).write(this.networkOutFile);
		log.info("Writing transit schedule to " + this.transitScheduleOutFile);
		new TransitScheduleWriter(scenario.getTransitSchedule()).writeFile(this.transitScheduleOutFile);
		log.info("Done...");
	}

}
