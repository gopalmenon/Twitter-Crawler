package controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import file.io.FileWriter;

public class RestartController {
	
	public static final String RESTART_STATUS_FILE = "RestartStatusFile.txt";
	public static final String SEED_SET_FILE = "SeedSet.txt";
	public static final int SEED_ID_LEVEL = 0;

	private static RestartController restartControllerInstance = null;
	
	private RestartController() {
	}
	
	public static RestartController getInstance() {
		if (restartControllerInstance == null) {
			restartControllerInstance = new RestartController();
		}
		return restartControllerInstance;
	}
	
	public ArrayDeque<RestartQueueEntry> getStartingSet() {
		
		FileWriter fileOperations = new FileWriter();
		if (!fileOperations.fileExists(RESTART_STATUS_FILE)) {
			System.out.println("Starting with seed set");
			return getSeedSet();
		}
		
		try {
			List<String> restartFileContents = fileOperations.getFileContents(RESTART_STATUS_FILE);
			if (restartFileContents.isEmpty()) {
				System.out.println("Starting with seed set");
				return getSeedSet();
			} else {
				System.out.println("Restarting from last point");
				return getRestartSet(restartFileContents);
			}
		
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		return null;
		
	}
	
	private ArrayDeque<RestartQueueEntry> getSeedSet() {
		
		FileWriter fileOperations = new FileWriter();
		ArrayDeque<RestartQueueEntry> returnValue = new ArrayDeque<RestartQueueEntry>();
		try {
			List<String> seedFileContents = fileOperations.getFileContents(SEED_SET_FILE);
			for (String twitterSeedId : seedFileContents) {
				if (twitterSeedId.trim().length() > 0) {
					returnValue.add(new RestartQueueEntry(SEED_ID_LEVEL, Long.parseLong(twitterSeedId.trim())));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		return returnValue;
		
	}
	
	private ArrayDeque<RestartQueueEntry> getRestartSet(List<String> restartFileContents) {
		
		FileWriter fileOperations = new FileWriter();
		ArrayDeque<RestartQueueEntry> returnValue = new ArrayDeque<RestartQueueEntry>();
		try {
			List<String> seedFileContents = fileOperations.getFileContents(RESTART_STATUS_FILE);
			for (String twitterSeedId : seedFileContents) {
				if (twitterSeedId.trim().length() > 0) {
					returnValue.add(new RestartQueueEntry(twitterSeedId.trim()));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}		return returnValue;
		
	}
	
	public void saveCurrentState(ArrayDeque<RestartQueueEntry> crawlSet) {
		
		List<String> crawlSetEntries = new ArrayList<String>();
		while (!crawlSet.isEmpty()) {
			crawlSetEntries.add(crawlSet.remove().toString());
		}
		FileWriter fileOperations = new FileWriter();
		try {
			fileOperations.writeListToFile(RESTART_STATUS_FILE, crawlSetEntries);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
	}
}
