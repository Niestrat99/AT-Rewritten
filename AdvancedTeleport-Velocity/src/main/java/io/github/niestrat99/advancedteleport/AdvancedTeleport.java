package io.github.niestrat99.advancedteleport;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;
import org.sfl4j.Logger;

@Plugin(id = "advancedteleport", name = "AdvancedTeleport", version = "0.0.1", authors = {"Thatsmusic99"})
public class AdvancedTeleport {

	private final @NotNull ProxyServer server;
	private final @NotNull Logger logger;

	@Inject
	public AdvancedTeleport(ProxyServer server, Logger logger) {
		this.server = server;
		this.logger = logger;

		// We're live!
		logger.info("AdvancedTeleport has started successfully.");
	}
}

