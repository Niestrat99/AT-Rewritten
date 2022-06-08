package com.destroystokyo.paper.profile;

import com.mojang.authlib.*;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilEnvironment;

import java.net.Proxy;

public class PaperAuthenticationService extends YggdrasilAuthenticationService {
    private final Environment environment;
    public PaperAuthenticationService(Proxy proxy) {
        super(proxy);
        this.environment = EnvironmentParser.getEnvironmentFromProperties().orElse(YggdrasilEnvironment.PROD.getEnvironment());
    }

    @Override
    public UserAuthentication createUserAuthentication(Agent agent) {
        return new PaperUserAuthentication(this, agent);
    }

    @Override
    public MinecraftSessionService createMinecraftSessionService() {
        return new PaperMinecraftSessionService(this, this.environment);
    }

    @Override
    public GameProfileRepository createProfileRepository() {
        return new PaperGameProfileRepository(this, this.environment);
    }
}
