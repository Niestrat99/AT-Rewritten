plugins {
    id("java")
    id("bukkit-common-conventions")
    id("slimjar-conventions")
    id("spigot-conventions")
}

group = "io.github.niestrat99"
version = "6.1.1"

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation(project(":AdvancedTeleport-Common"))

    compileOnly(libs.spigot)
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
