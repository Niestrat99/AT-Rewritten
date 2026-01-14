// enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "AdvancedTeleport"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://repo.bsdevelopment.org/releases")
    }
}
include("AdvancedTeleport-Paper")
include("AdvancedTeleport-Common")
include("AdvancedTeleport-Spigot")
include("AdvancedTeleport-Hybrid")