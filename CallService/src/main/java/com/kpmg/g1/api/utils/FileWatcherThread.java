package com.kpmg.g1.api.utils;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileWatcherThread extends Thread {

	// add logger of log4j
	final static Logger log = LogManager.getLogger(FileWatcherThread.class.getName());

	@Override
	public void run() {
		watchFile();
	}

	private static void watchFile() {
		try {
			// get watch service of the current file system
			WatchService watchService = FileSystems.getDefault().newWatchService();
			// Path of Directory to watch
			Path path = Paths.get(System.getenv(Constants.CONFIGURATION_DIR_PATH));
			// register watch service with type of ENTRY_MODIFY
			path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
			WatchKey key;
			try {
				while ((key = watchService.take()) != null) {
					if (Thread.currentThread().isInterrupted()) {
						watchService.close();
						return;
					}
					// upon update two events will be emitted - one for actual change and second for the timestamp
					// workaround can be sleep operation... not that relevant in our case (we will have configuration updated twice..)
					for (WatchEvent<?> event : key.pollEvents()) {
						// log that configuration was changed
						log.info("configuration was changed. event: " + event.context().toString());
						JSONConfigurations.updateConfigurations();
					}
					key.reset();
				}
			} catch (InterruptedException e) {
				log.error("InterruptedException in file watcher Thread (ignore this message if application was shutdown willingly): "
							+ e.getMessage() + e.getCause());
				watchService.close();
				return;
			}
		} catch (IOException e) {
			log.error("IOException in file watcher Thread: " + e.getMessage() + e.getCause());
		}
	}
}
