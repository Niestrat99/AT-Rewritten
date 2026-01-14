plugins {
    id("java")
    id("bukkit-common-conventions")
}

group = "io.github.niestrat99"
version = "6.2.0"

repositories {
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "Paper, PaperLib and Adventure"
    }

    maven("https://repo.bsdevelopment.org/releases") {
        name = "Slimjar"
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
}

tasks.test {
    useJUnitPlatform()
}

configurations.configureEach {
    resolutionStrategy {
        capabilitiesResolution {
            force("com.google.code.gson:gson:2.13.2")
        }
    }
}

bukkit.main = "io.github.niestrat99.advancedteleport.HybridAdvancedTeleport"
