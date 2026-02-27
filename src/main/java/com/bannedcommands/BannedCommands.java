package com.bannedcommands;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BannedCommands implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("banned-commands");

	@Override
	public void onInitialize() {
		BannedCommandsConfig.load();
		BannedCommandsCommand.register();
		LOGGER.info("BannedCommands loaded.");
	}
}