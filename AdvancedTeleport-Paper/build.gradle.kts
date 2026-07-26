import java.io.BufferedReader
import java.util.regex.Pattern

plugins {
    id("java")
    id("java-library")
    id("com.modrinth.minotaur") version "2.+"
    id("bukkit-common-conventions")
    alias(libs.plugins.hangar)
    alias(libs.plugins.minecraft.runPaper)
}

group = "io.github.niestrat99"
version = project.version

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

    implementation(project(":AdvancedTeleport-Common")) {
        exclude("org.spigotmc:.*")
    }

    compileOnly(libs.paper)
}

bukkit.main = "io.github.niestrat99.advancedteleport.PaperAdvancedTeleport"

hangarPublish {
    publications.register("plugin") {
        version.set(project.version as String)
        id.set("AdvancedTeleport")
        channel.set("Release")
        changelog.set(getCogChangelog())
        apiKey.set(System.getenv("HANGAR_TOKEN"))

        platforms {
            register(io.papermc.hangarpublishplugin.model.Platforms.PAPER) {
                jar.set(getJarFile())
                platformVersions.set(listOf(project.property("minecraft_version") as String))
                dependencies {
                    url("Vault", "https://dev.bukkit.org/projects/vault") {
                        required.set(false)
                    }
                    url("floodgate", "https://hangar.papermc.io/GeyserMC/Floodgate") {
                        required.set(false)
                    }
                    url("WorldBorder", "https://www.spigotmc.org/resources/worldborder-1-15.80466/") {
                        required.set(false)
                    }
                    url("ChunkyBorder", "https://www.spigotmc.org/resources/chunkyborder.84278/") {
                        required.set(false)
                    }
                }
            }
        }
    }
}

// Lead development use only.
modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("BQFzmxKU")
    versionNumber.set(project.version.toString())
    versionType.set(getReleaseType())
    uploadFile.set(tasks.shadowJar.get())
    gameVersions.addAll(arrayListOf("1.18", "1.18.1", "1.18.2", "1.19", "1.19.1", "1.19.2", "1.19.3", "1.19.4", "1.20",
        "1.20.1", "1.20.2", "1.20.3", "1.20.4", "1.20.5", "1.20.6", "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.11"))
    loaders.addAll("paper", "purpur")
    changelog.set(getCogChangelog())
}


configurations.configureEach {
    resolutionStrategy {
        capabilitiesResolution {
            force("com.google.code.gson:gson:2.13.2")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    this.modrinth {
        dependsOn(shadowJar)
    }

    runServer {
        dependsOn(shadowJar)
        minecraftVersion("26.2")
        runDirectory.set(rootDir.resolve(".run"))
        pluginJars(getJarFile())
    }
}

// Lead development use only.
fun getCogChangelog(): String {

    println("Fetching changelog at v" + project.version.toString())
    return runCatching { Runtime.getRuntime().exec("cog changelog --at v" + project.version.toString()) }
        .onSuccess(Process::waitFor)
        .map { process ->
            if (process.exitValue() != 0) ""
            else process.inputStream.bufferedReader().use(BufferedReader::readText)
        }
        .onFailure { "" }
        .getOrDefault("")
}

// Lead development use only.
fun getReleaseType(): String {

    val pattern = Pattern.compile("""v?[\d\\.]-(\w+)\\.?\d?""")
    val matcher = pattern.matcher(project.version.toString())
    if (!matcher.matches()) return "release"
    return matcher.group(1)
}

fun getJarFile(): File {

    // Get the jar file
    val fileName = project.name + "-" + project.version.toString() + "-all.jar"
    return buildDir.resolve("libs").resolve(fileName)
}