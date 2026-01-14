
import io.github.slimjar.func.slimjar
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("java-library")
    id("maven-publish")
    id("bukkit-common-conventions")
//    alias(libs.plugins.shadow)
//    alias(libs.plugins.slimjar)
}

bukkit.main = "NO_CLASS"

dependencies {
    testImplementation("org.mockito:mockito-core:5.+")

    compileOnly(libs.spigot)

    implementation(slimjar("2.1.9"))

    compileOnly(libs.annotations)
    compileOnly(libs.bundles.adventure)
    compileOnly(libs.adventure.platform.bukkit)
    compileOnly(libs.bstats.bukkit)
}

configurations.configureEach {
    resolutionStrategy {
        capabilitiesResolution {
            withCapability("io.papermc.paper:paper-api") {
                selectHighestVersion()
            }
            withCapability("org.spigotmc:spigot-api") {
                selectHighestVersion()
            }

            force("com.google.code.gson:gson:2.13.2")
        }
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    withType<ProcessResources> {
        val currentDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(Date())
        inputs.property("version", project.version)
        inputs.property("timestamp", currentDate)

        filesMatching("update.properties") {
            expand(mutableMapOf("timestamp" to currentDate))
        }
    }

    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        dependencies {
            project.configurations.implementation.get().dependencies.forEach {
                include(dependency(it))
            }
            relocate("io.github.slimjar", "io.github.niestrat99.advancedteleport.libs.slimjar")
        }
    }

    this.slimJar {
        dependsOn(sourcesJar)
    }

    shadowJar {
        dependsOn(slimJar)
    }
}

