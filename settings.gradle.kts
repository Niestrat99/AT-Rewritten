enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "AdvancedTeleport"

include(":AdvancedTeleport-Bukkit")
include(":AdvancedTP-Core")

pluginManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://repo.bsdevelopment.org/releases")
    }
}
