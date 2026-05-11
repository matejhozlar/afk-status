package com.saunhardy.afkstatus.platform;

import com.saunhardy.afkstatus.AFKStatus;

import java.nio.file.Path;
import java.util.ServiceLoader;

public interface Services {
    Services INSTANCE = load();

    Path getConfigDir();

    private static Services load() {
        Services service = ServiceLoader.load(Services.class)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No " + Services.class.getName() + " implementation found on classpath"));
        AFKStatus.LOGGER.debug("Loaded platform services: {}", service.getClass().getName());
        return service;
    }
}
