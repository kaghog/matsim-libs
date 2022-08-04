package zurich.ridepooling;

import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
import org.matsim.contrib.drt.run.DrtControlerCreator;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.core.config.CommandLine;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;

import java.io.IOException;


public class RunSimulation {
	public static void run(String configFile, CommandLine cmd) throws CommandLine.ConfigurationException, IOException {
		Config config = ConfigUtils.loadConfig(configFile, new MultiModeDrtConfigGroup(), new DvrpConfigGroup(), new SwissRailRaptorConfigGroup());
		cmd.applyConfiguration(config); //updates the config from the command line

		config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);

		Controler controler = DrtControlerCreator.createControler(config, false);

		controler.run();
	}


	public static void main(String[] args) throws CommandLine.ConfigurationException, IOException {

		CommandLine cmd = new CommandLine.Builder(args) //
				.allowOptions("identifier", "fleet-size","rebalancing") //
				.requireOptions("config-path") //
				.allowPrefixes("mode-parameter", "cost-parameter") //
				.build();


		RunSimulation.run(cmd.getOptionStrict("config-path"), cmd);

	}
}
