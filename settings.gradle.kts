enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "AdvancedTeleport"

include(":AdvancedTeleport-Bukkit")
include(":AdvancedTP-Core")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://papermc.io/repo/repository/maven-public/")
    }
}
