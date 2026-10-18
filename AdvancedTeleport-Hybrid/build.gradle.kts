plugins {
    id("java")
    id("bukkit-common-conventions")
    id("slimjar-conventions")
    id("spigot-conventions")
}

group = "io.github.niestrat99"
version = "6.2.0"

repositories {
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "Paper, PaperLib and Adventure"
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    compileOnly(libs.paper)

    implementation(project(":AdvancedTeleport-Spigot")) {
        exclude("org.spigotmc:.*")
    }

    implementation(project(":AdvancedTeleport-Paper"))
    implementation(project(":AdvancedTeleport-Common"))

    implementation(libs.bstats.bukkit)
    implementation(libs.bstats.base)
}

tasks{
    test {
        useJUnitPlatform()
    }

    shadowJar {
        dependencies {
            include(project(":AdvancedTeleport-Spigot"))
            include(project(":AdvancedTeleport-Paper"))
            include(project(":AdvancedTeleport-Common"))
            
            project.configurations.implementation.get().dependencies.forEach {
                include(dependency(it))
            }
        }
    }
}

configurations.configureEach {
    resolutionStrategy {
        capabilitiesResolution {
            force("com.google.code.gson:gson:2.13.2")
        }
    }
}

bukkit.main = "io.github.niestrat99.advancedteleport.HybridAdvancedTeleport"
