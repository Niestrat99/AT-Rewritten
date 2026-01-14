import io.github.slimjar.func.slimjar

plugins {
    id("java")
    id("bukkit-common-conventions")
}

group = "io.github.niestrat99"
version = "6.1.1"

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation(project(":AdvancedTeleport-Common"))

    compileOnly(libs.spigot)

    slim(libs.bundles.adventure)
    slim(libs.adventure.platform.bukkit)
    slim(libs.kyori.examination)
}

tasks.test {
    useJUnitPlatform()
}

configurations.configureEach {
    resolutionStrategy {
        capabilitiesResolution {
            withCapability("org.spigotmc:spigot-api") {
                selectHighestVersion()
            }
            force("com.google.code.gson:gson:2.13.2")
        }
    }
}

bukkit.main = "io.github.niestrat99.advancedteleport.SpigotAdvancedTeleport"
