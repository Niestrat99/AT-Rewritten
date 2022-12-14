enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "AdvancedTeleport"

include(":AdvancedTeleport-Bukkit")
include(":AdvancedTP-Core")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://repo.racci.dev/releases") { mavenContent { releasesOnly() } }
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories.maven("https://repo.racci.dev/releases") { mavenContent { releasesOnly() } }

    versionCatalogs.create("libMinix") {
        val minixVersion: String by settings
        val kotlinVersion: String by settings
        val conventions = kotlinVersion.plus("-").plus(minixVersion.substringAfterLast('.'))
        from("dev.racci:catalog:$conventions")
    }
}
