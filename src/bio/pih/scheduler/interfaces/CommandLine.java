package bio.pih.scheduler.interfaces;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import bio.pih.scheduler.Scheduler;
import bio.pih.scheduler.Scheduler.Searching;
import bio.pih.search.AlignmentResult;

/**
 * A command line interface for perform search
 * 
 * @author albrecht
 * 
 */
public class CommandLine implements Runnable {

	static String SEARCH = "search";
	static String CHECK = "check";
	static String CLEAN = "clean";
	static String READ = "read";
	static String EXIT = "exit";

	private final Scheduler scheduler;
	private final InputStream is;
	private final boolean echo;

	/**
	 * @param scheduler
	 * @param is
	 */
	public CommandLine(Scheduler scheduler, InputStream is) {
		this(scheduler, is, false);
	}

	/**
	 * @param scheduler
	 * @param is
	 * @param echo
	 */
	public CommandLine(Scheduler scheduler, InputStream is, boolean echo) {
		this.scheduler = scheduler;
		this.is = is;
		this.echo = echo;

	}

	public void run() {
		BufferedReader lineReader = new BufferedReader(new InputStreamReader(is));
		String line;

		System.out.print("genoogle console> ");
		try {
			while ((line = lineReader.readLine()) != null) {
				line = line.trim();
				if (line.trim().equals("")) {
				//if (line.isEmpty()) { Java 6
					continue;
				}

				if (echo) {
					System.out.println(line);
				}

				if (line.equals(EXIT)) {
					System.out.println("Stoping scheduler.");
					while (scheduler.isWaitingSearch()) {
						System.out.print(".");
						Thread.sleep(200);
					}
					System.out.println("Stoping workers.");
					try {
						scheduler.stop();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return;
				}

				String[] commands = line.split(" ");
				if (commands[0].equals(SEARCH) && commands.length == 3) {
					String db = commands[1];
					String query = commands[1];
					try {
						Searching doSearch = scheduler.doSearch(db, query);
						System.out.println("Search: " + doSearch.getCode());
					} catch (IOException e) {
						e.printStackTrace();
					}

				} else if (commands[0].equals(CHECK) && commands.length == 2) {
					int code = Integer.parseInt(commands[1]);
					Searching search = scheduler.getSearches().get(code);
					if (search == null) {
						System.out.print("search " + code + " doesnt exist. ");
						continue;
					}
					System.out.print("search " + search.getCode() + " is ");
					if (!search.isDone()) {
						System.out.print(" NOT ");
					}
					System.out.println("DONE");

				} else if (commands[0].equals(READ) && commands.length == 2) {
					int code = Integer.parseInt(commands[1]);
					Searching searching = scheduler.getSearches().get(code);
					if (searching != null) {
						if (searching.isDone()) {
							AlignmentResult[] alignments = searching.getAlignments();
							for (AlignmentResult alignment : alignments) {
								System.out.println(alignment);
							}
						} else {
							System.out.println("Search " + searching.getCode() + "is not finished.");
						}
					} else {
						System.out.println("Search " + code + " do not exist.");
					}

				} else if (commands[0].equals(CLEAN) && commands.length == 2) {
					long code = Integer.parseInt(commands[1]);
					Searching searching = scheduler.getSearches().get(code);
					if (searching != null) {
						if (searching.isDone()) {
							scheduler.getSearches().put(code, null);
							System.out.println("Search " + searching.getCode() + " erased.");
						} else {
							System.out.println("Search " + searching.getCode() + "is not finished.");
						}
					} else {
						System.out.println("Search " + code + " do not exist.");
					}
				}

				System.out.print("genoogle console> ");
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}
