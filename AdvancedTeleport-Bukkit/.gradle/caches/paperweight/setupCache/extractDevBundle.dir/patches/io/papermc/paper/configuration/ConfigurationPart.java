package io.papermc.paper.configuration;

abstract class ConfigurationPart {

    public static abstract class Post extends ConfigurationPart {

        public abstract void postProcess();
    }

}
