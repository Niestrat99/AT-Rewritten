plugins {
    java
}

allprojects {
    apply(plugin = "java")

    java {
        withSourcesJar()

        toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    }

    repositories {
        maven("https://repo.bsdevelopment.org/releases") {
            name = "Slimjar"
        }

        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
            name = "Spigot"
        }

        maven("https://repo.papermc.io/repository/maven-public/") {
            name = "Paper, PaperLib and Adventure"
        }

        maven("https://repo.codemc.io/repository/maven-public/") {
            name = "Vault"
        }

        maven("https://jitpack.io") {
            name = "WorldBorder and Chunky"
        }

        maven("https://repo.essentialsx.net/releases/") {
            name = "Essentials"
            content {
                includeGroup("net.essentialsx")
            }
        }

        maven("https://repo.opencollab.dev/main/") {
            name = "Geyser"
        }

        maven("https://libraries.minecraft.net/") {
            name = "authlib maybe"
        }

        maven("https://maven.enginehub.org/repo/") {
            name = "Sk89q"
            content {
                includeGroup("com.sk89q.worldguard")
                includeGroup("com.sk89q.worldedit")
                includeGroup("com.sk89q.worldguard.worldguard-libs")
                includeGroup("com.sk89q.worldedit.worldedit-libs")
            }
        }

        maven("https://repo.jpenilla.xyz/snapshots/") {
            name = "Squaremap"
        }

        maven("https://repo.mikeprimm.com/") {
            name = "Dynmap"
        }

        maven("https://repo.rosewooddev.io/repository/public/") {
            name = "PlayerParticles"
            content { includeGroup("dev.esophose") }
        }
    }
}

subprojects {
    buildDir = rootProject.buildDir.resolve(this.name.lowercase())
}
