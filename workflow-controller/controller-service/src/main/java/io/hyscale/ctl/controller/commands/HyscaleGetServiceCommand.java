package io.hyscale.ctl.controller.commands;

import org.springframework.stereotype.Component;
import picocli.CommandLine;

/**
 *  This class executes  'hyscale get service' command
 *  It is a sub-command of the 'hyscale get ' command
 *  @see HyscaleGetCommand .
 *  Every command/sub-command has to implement the Runnable so tha
 *  whenever the command is executed the {@link #run()}
 *  method will be invoked
 *
 * The sub-commands of are handled by @Command annotation
 *
 */
@CommandLine.Command(name = "service", subcommands = { HyscaleServiceLogsCommand.class,
		HyscaleServiceStatusCommand.class }, description = "Performs action on the service")
@Component
public class HyscaleGetServiceCommand implements Runnable {

	@CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Displays the help information of the specified command")
	private boolean helpRequested = false;

	/**
	 * Executes the 'hyscale get service' command
	 * Provides usage of this command to the user
	 */
	@Override
	public void run() {
		new CommandLine(new HyscaleGetServiceCommand()).usage(System.out);
	}
}
