import io.github.slimjar.func.slimjar

plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.slimjar)
}

dependencies {
    implementation(slimjar("2.1.9"))
}

tasks {
    this.slimJar {
        dependsOn(jar, compileTestJava, processTestResources, test)
    }

    shadowJar {
        dependsOn(slimJar)
        dependencies {
            project.configurations.implementation.get().dependencies.forEach {
                include(dependency(it))
            }
            relocate("io.github.slimjar", "io.github.niestrat99.advancedteleport.libs.slimjar")
        }
    }
}

slimJar {
    val baseRelocation = "io.github.niestrat99.advancedteleport.libs"

    relocate("org.bstats", "$baseRelocation.bstats")
    relocate("io.papermc.lib", "$baseRelocation.paperlib")
    relocate("io.github.thatsmusic99.configurationmaster", "$baseRelocation.configurationmaster")
}
