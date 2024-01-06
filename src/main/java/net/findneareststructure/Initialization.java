package net.findneareststructure;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Initialization implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("find-nearest-structure");

	@Override
	public void onInitialize() {
		FindNearestStructureCommand.register();
		LOGGER.info("Find Nearest Structure has successfully loaded!");
	}
}