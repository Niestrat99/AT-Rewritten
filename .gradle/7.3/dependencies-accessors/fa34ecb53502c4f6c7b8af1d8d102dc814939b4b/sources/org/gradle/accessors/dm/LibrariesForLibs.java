package org.gradle.accessors.dm;

import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.MinimalExternalModuleDependency;
import org.gradle.plugin.use.PluginDependency;
import org.gradle.api.artifacts.ExternalModuleDependencyBundle;
import org.gradle.api.artifacts.MutableVersionConstraint;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.internal.catalog.AbstractExternalDependencyFactory;
import org.gradle.api.internal.catalog.DefaultVersionCatalog;
import java.util.Map;
import javax.inject.Inject;

/**
 * A catalog of dependencies accessible via the `libs` extension.
*/
@NonNullApi
public class LibrariesForLibs extends AbstractExternalDependencyFactory {

    private final AbstractExternalDependencyFactory owner = this;
    private final EssentialsLibraryAccessors laccForEssentialsLibraryAccessors = new EssentialsLibraryAccessors(owner);
    private final KyoriLibraryAccessors laccForKyoriLibraryAccessors = new KyoriLibraryAccessors(owner);
    private final VersionAccessors vaccForVersionAccessors = new VersionAccessors(providers, config);
    private final BundleAccessors baccForBundleAccessors = new BundleAccessors(providers, config);
    private final PluginAccessors paccForPluginAccessors = new PluginAccessors(providers, config);

    @Inject
    public LibrariesForLibs(DefaultVersionCatalog config, ProviderFactory providers) {
        super(config, providers);
    }

        /**
         * Creates a dependency provider for annotations (org.jetbrains:annotations)
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getAnnotations() { return create("annotations"); }

        /**
         * Creates a dependency provider for authlib (com.mojang:authlib)
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getAuthlib() { return create("authlib"); }

        /**
         * Creates a dependency provider for chunkyborder (com.github.pop4959:ChunkyBorder)
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getChunkyborder() { return create("chunkyborder"); }

        /**
         * Creates a dependency provider for configuration (com.github.thatsmusic99:ConfigurationMaster-API)
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getConfiguration() { return create("configuration"); }

        /**
         * Creates a dependency provider for dynmap (us.dynmap:dynmap-api)
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getDynmap() { return create("dynmap"); }

        /**
         * Creates a dependency provider for floodgate (org.geysermc.floodgate:api)
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getFloodgate() { return create("floodgate"); }

        /**
         * Creates a dependency provider for griefprevention (com.github.TechFortress:GriefPrevention)
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getGriefprevention() { return create("griefprevention"); }

        /**
         * Creates a dependency provider for lands (com.github.angeschossen:LandsAPI)
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getLands() { return create("lands"); }

        /**
         * Creates a dependency provider for paperlib (io.papermc:paperlib)
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getPaperlib() { return create("paperlib"); }

        /**
         * Creates a dependency provider for squaremap (xyz.jpenilla:squaremap-api)
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getSquaremap() { return create("squaremap"); }

        /**
         * Creates a dependency provider for vault (net.milkbowl.vault:VaultAPI)
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getVault() { return create("vault"); }

        /**
         * Creates a dependency provider for worldborder (com.github.PryPurity:WorldBorder)
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getWorldborder() { return create("worldborder"); }

        /**
         * Creates a dependency provider for worldguard (com.sk89q.worldguard:worldguard-bukkit)
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getWorldguard() { return create("worldguard"); }

    /**
     * Returns the group of libraries at essentials
     */
    public EssentialsLibraryAccessors getEssentials() { return laccForEssentialsLibraryAccessors; }

    /**
     * Returns the group of libraries at kyori
     */
    public KyoriLibraryAccessors getKyori() { return laccForKyoriLibraryAccessors; }

    /**
     * Returns the group of versions at versions
     */
    public VersionAccessors getVersions() { return vaccForVersionAccessors; }

    /**
     * Returns the group of bundles at bundles
     */
    public BundleAccessors getBundles() { return baccForBundleAccessors; }

    /**
     * Returns the group of plugins at plugins
     */
    public PluginAccessors getPlugins() { return paccForPluginAccessors; }

    public static class EssentialsLibraryAccessors extends SubDependencyFactory implements DependencyNotationSupplier {

        public EssentialsLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for essentials (net.ess3:EssentialsX)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> asProvider() { return create("essentials"); }

            /**
             * Creates a dependency provider for spawn (net.ess3:EssentialsXSpawn)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getSpawn() { return create("essentials.spawn"); }

    }

    public static class KyoriLibraryAccessors extends SubDependencyFactory {

        public KyoriLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for examination (net.kyori:examination-api)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getExamination() { return create("kyori.examination"); }

            /**
             * Creates a dependency provider for nbt (net.kyori:adventure-nbt)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getNbt() { return create("kyori.nbt"); }

    }

    public static class VersionAccessors extends VersionFactory  {

        private final EssentialsVersionAccessors vaccForEssentialsVersionAccessors = new EssentialsVersionAccessors(providers, config);
        private final KyoriVersionAccessors vaccForKyoriVersionAccessors = new KyoriVersionAccessors(providers, config);
        public VersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: annotations (23.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getAnnotations() { return getVersion("annotations"); }

            /**
             * Returns the version associated to this alias: authlib (2.3.31)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getAuthlib() { return getVersion("authlib"); }

            /**
             * Returns the version associated to this alias: chunkyborder (52034550ef)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getChunkyborder() { return getVersion("chunkyborder"); }

            /**
             * Returns the version associated to this alias: configuration (v2.0.0-BETA-3)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getConfiguration() { return getVersion("configuration"); }

            /**
             * Returns the version associated to this alias: dynmap (3.4)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getDynmap() { return getVersion("dynmap"); }

            /**
             * Returns the version associated to this alias: floodgate (2.1.0-SNAPSHOT)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getFloodgate() { return getVersion("floodgate"); }

            /**
             * Returns the version associated to this alias: griefprevention (16.18)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getGriefprevention() { return getVersion("griefprevention"); }

            /**
             * Returns the version associated to this alias: lands (6.0.2)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getLands() { return getVersion("lands"); }

            /**
             * Returns the version associated to this alias: paperlib (1.0.7)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getPaperlib() { return getVersion("paperlib"); }

            /**
             * Returns the version associated to this alias: paperweight (1.3.8)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getPaperweight() { return getVersion("paperweight"); }

            /**
             * Returns the version associated to this alias: pluginYML (0.5.2)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getPluginYML() { return getVersion("pluginYML"); }

            /**
             * Returns the version associated to this alias: shadow (7.1.2)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getShadow() { return getVersion("shadow"); }

            /**
             * Returns the version associated to this alias: squaremap (1.1.3)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getSquaremap() { return getVersion("squaremap"); }

            /**
             * Returns the version associated to this alias: vault (1.7)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getVault() { return getVersion("vault"); }

            /**
             * Returns the version associated to this alias: worldborder (v2.1.2)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getWorldborder() { return getVersion("worldborder"); }

            /**
             * Returns the version associated to this alias: worldguard (7.0.7)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getWorldguard() { return getVersion("worldguard"); }

        /**
         * Returns the group of versions at versions.essentials
         */
        public EssentialsVersionAccessors getEssentials() { return vaccForEssentialsVersionAccessors; }

        /**
         * Returns the group of versions at versions.kyori
         */
        public KyoriVersionAccessors getKyori() { return vaccForKyoriVersionAccessors; }

    }

    public static class EssentialsVersionAccessors extends VersionFactory  implements VersionNotationSupplier {

        public EssentialsVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the version associated to this alias: essentials (2.18.2)
         * If the version is a rich version and that its not expressible as a
         * single version string, then an empty string is returned.
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> asProvider() { return getVersion("essentials"); }

            /**
             * Returns the version associated to this alias: essentials.spawn (2.16.1)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getSpawn() { return getVersion("essentials.spawn"); }

    }

    public static class KyoriVersionAccessors extends VersionFactory  {

        public KyoriVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: kyori.adventure (4.11.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getAdventure() { return getVersion("kyori.adventure"); }

            /**
             * Returns the version associated to this alias: kyori.examination (1.3.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getExamination() { return getVersion("kyori.examination"); }

    }

    public static class BundleAccessors extends BundleFactory {

        public BundleAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

    }

    public static class PluginAccessors extends PluginFactory {

        public PluginAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Creates a plugin provider for bukkitYML to the plugin id 'net.minecrell.plugin-yml.bukkit'
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getBukkitYML() { return createPlugin("bukkitYML"); }

            /**
             * Creates a plugin provider for shadow to the plugin id 'com.github.johnrengelman.shadow'
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getShadow() { return createPlugin("shadow"); }

            /**
             * Creates a plugin provider for userdev to the plugin id 'io.papermc.paperweight.userdev'
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getUserdev() { return createPlugin("userdev"); }

    }

}
