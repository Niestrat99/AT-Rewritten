import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import java.io.BufferedReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.regex.Pattern

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    id("java-library")
    id("maven-publish")
    id("com.modrinth.minotaur")
    alias(libMinix.plugins.kotlin.jvm)
    alias(libMinix.plugins.shadow)
    alias(libMinix.plugins.minecraft.pluginYML)
    alias(libMinix.plugins.minecraft.runPaper)
    alias(libMinix.plugins.slimjar)
}

slimJar {
    val baseRelocation = "io.github.niestrat99.advancedteleport.libs"

    relocate("org.bstats", "$baseRelocation.bstats")
    relocate("io.papermc.lib", "$baseRelocation.paperlib")
    relocate("io.github.thatsmusic99.configurationmaster", "$baseRelocation.configurationmaster")
}

repositories {

    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        name = "Spigot"
    }

    maven("https://papermc.io/repo/repository/maven-public/") {
        name = "Paper and PaperLib"
    }

    maven("https://repo.codemc.io/repository/maven-public/") {
        name = "Vault"
    }

    maven("https://jitpack.io") {
        name = "WorldBorder and Chunky"
    }

    maven("https://ci.pluginwiki.us/plugin/repository/everything/") {
        name = "ConfigurationMaster"
    }

    maven("https://repo.essentialsx.net/releases/") {
        name = "Essentials"
    }

    maven("https://repo.opencollab.dev/maven-snapshots/") {
        name = "Geyser"
    }

    maven("https://libraries.minecraft.net/") {
        name = "authlib maybe"
    }

    maven("https://repo.maven.apache.org/maven2/") {
        name = "Adventure"
    }

    maven("https://maven.enginehub.org/repo/") {
        name = "Sk89q"
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

    maven("https://repo.racci.dev/releases") {
        name = "RacciRepo"
        mavenContent { releasesOnly() }
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT")

    implementation(libMinix.slimjar)

    slim(libMinix.bundles.kotlin)
    slim(libMinix.adventure.api)
    slim(libMinix.adventure.minimessage)
    slim(libMinix.adventure.platform.bukkit)
    slim(libMinix.minecraft.bstats.bukkit)
    slim(libs.paperlib)
    slim(libs.kyori.nbt)
    slim(libs.kyori.examination)
    slim(libs.configuration)

    slim(libs.paperlib)
    slim(libs.kyori.nbt)
    slim(libs.kyori.examination)
    slim(libs.configuration)

    compileOnly(libMinix.minecraft.authLib)
    compileOnly(libs.annotations)
    compileOnly(libs.vault)
    compileOnly(libs.essentials)
    compileOnly(libs.essentials.spawn)
    compileOnly(libs.worldborder)
    compileOnly(libs.chunkyborder)
    compileOnly(libs.floodgate)
    compileOnly(libMinix.minecraft.api.landsAPI)
    compileOnly(libs.griefprevention)
    compileOnly(libs.playerparticles)
    compileOnly(libs.worldguard)
    compileOnly(libs.squaremap)
    compileOnly(libs.dynmap) {
        artifact { // Uses wrong jar if not specified
            name = "dynmap-api"
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

    runServer {

        // Wait for slimJar to go through first
        dependsOn(slimJar)

        // Set the version to 1.19.4
        minecraftVersion("1.19.4")

        // Get the dev server folder
        val devServer = file(findProperty("devServer") ?: "${System.getProperty("user.home")}/Documents/Minecraft/Dev")
        val pluginsFolder = devServer.resolve("plugins")
        runDirectory.set(rootDir.resolve(".run"))
        pluginJars(pluginsFolder.resolve("Vault.jar"))
        pluginJars(pluginsFolder.resolve("Spark.jar"))
        pluginJars(pluginsFolder.resolve("Spoofer.jar"))
        pluginJars(pluginsFolder.resolve("squaremap.jar"))
        pluginJars(pluginsFolder.resolve("PlayerParticles.jar"))
        pluginJars(pluginsFolder.resolve("dynmap.jar"))
        pluginJars(getJarFile())

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
        dependsOn(shadowJar)
        dependsOn(jar)
        dependsOn(inspectClassesForKotlinIC)
    }
}

// Lead development use only.
modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("BQFzmxKU")
    versionNumber.set(project.version.toString())
    versionType.set(getReleaseType())
    uploadFile.set(getJarFile())
    gameVersions.addAll(arrayListOf("1.18", "1.18.1", "1.18.2", "1.19", "1.19.1", "1.19.2", "1.19.3", "1.19.4"))
    loaders.addAll("paper", "spigot", "purpur")
    changelog.set(getCogChangelog())
}

bukkit {
    name = "AdvancedTeleport"
    version = project.version.toString().substring(if (project.version.toString().startsWith("v")) 1 else 0) // Remove the v in front
    description = "A plugin that allows you to teleport to players, locations, and more!"
    authors = listOf("Niestrat99", "Thatsmusic99", "SuspiciousLookingOwl (Github)", "animeavi (Github)", "MrEngMan", "LucasMucGH (Github)", "jonas-t-s (Github)", "DaRacci")

    apiVersion = "1.16"
    main = "io.github.niestrat99.advancedteleport.CoreClass"
    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD

    softDepend = listOf("Vault", "Ultimate_Economy", "ConfigurationMaster", "WorldBorder", "ChunkyBorder", "floodgate", "Lands", "WorldGuard", "GriefProtection", "dynmap", "squaremap", "PlayerParticles")
    loadBefore = listOf("Essentials", "EssentialsSpawn")

    commands {
        register("at") {
            description = "The core AT command."
            permission = "at.member.core"
        }

        register("warps") {
            description = "Gives you a list of warps."
            permission = "at.member.warps"
        }

        register("warp") {
            description = "Teleports you to a specified warp."
            permission = "at.member.warp"
            usage = "/warp <Warp name>"
        }

        register("setwarp") {
            description = "Sets a warp at your location."
            permission = "at.admin.setwarp"
            usage = "/setwarp <Warp name>"
        }

        register("delwarp") {
            description = "Deletes a specified warp."
            permission = "at.admin.delwarp"
            usage = "/delwarp <Warp name>"
        }

        register("movewarp") {
            description = "Moves a specified warp."
            permission = "at.admin.movewarp"
            usage = "/movewarp <Warp name>"
        }

        register("tpr") {
            description = "Teleports you to a random place."
            permission = "at.member.tpr"
            aliases = listOf("rtp")
            usage = "/tpr [World]"
        }

        register("tpohere") {
            description = "(ADMIN ONLY COMMAND) Instantly teleports a player to you."
            permission = "at.admin.tpohere"
            usage = "/tpohere <Player>"
        }

        register("tpall") {
            description = "(ADMIN ONLY COMMAND) Sends a \"TPAHere\"-request to every online player."
            permission = "at.admin.all"
            aliases = listOf("tpaall")
        }

        register("tpoff") {
            description = "Stops you from receiving teleport requests."
            permission = "at.member.off"
        }

        register("tpon") {
            description = "Allows you to receive teleport requests"
            permission = "at.member.on"
        }

        register("toggletp") {
            description = "Lets you switch between receiving tp requests and not receiving teleport requests. then"
            permission = "at.member.toggletp"
        }

        register("tpcancel") {
            description = "Cancels a teleport request you've sent."
            permission = "at.member.cancel"
        }

        register("tpunblock") {
            description = "Unblocks a player."
            permission = "at.member.unblock"
            usage = "/tpunblock <Player>"
        }

        register("tpblock") {
            description = "Blocks and prevents a player to send a teleport request to you."
            permission = "at.member.block"
            usage = "/tpblock <Player>"
        }

        register("tpdeny") {
            description = "Declines a teleport request someone sent you."
            permission = "at.member.no"
            aliases = listOf("tpano", "tpno")
        }

        register("tpaccept") {
            description = "Accepts a teleport request someone sent you."
            permission = "at.member.yes"
            aliases = listOf("tpayes", "tpyes")
            usage = "/tpaccept [Player]"
        }

        register("tpo") {
            description = "(ADMIN ONLY COMMAND) Instantly teleports you to another player."
            permission = "at.admin.tpo"
            usage = "/tpo <Player>"
        }

        register("tpahere") {
            description = "Sends the targeted player a teleport requests to where you are."
            permission = "at.member.here"
            usage = "/tpahere <Player>"
        }

        register("tpa") {
            description = "Sends the targeted player a teleport request to where they are."
            permission = "at.member.tpa"
            usage = "/tpa <Player>"
        }

        register("tpalist") {
            description = "Lists your teleport requests."
            permission = "at.member.list"
            usage = "/tpalist"
        }

        register("spawn") {
            description = "Teleports you to the spawn location."
            permission = "at.member.spawn"
            usage = "/spawn"
        }

        register("setspawn") {
            description = "Sets a spawn at your location."
            permission = "at.admin.setspawn"
            usage = "/setspawn"
        }

        register("mirrorspawn") {
            description = "Redirects people using /spawn in one world to another spawn point."
            permission = "at.admin.mirrorspawn"
            usage = "/mirrorspawn <To World>|[From World] [To World]"
        }

        register("removespawn") {
            description = "Removes a given spawnpoint."
            permission = "at.admin.removespawn"
            usage = "/removespawn [ID]"
        }

        register("setmainspawn") {
            description = "Sets the main spawnpoint."
            permission = "at.admin.setmainspawn"
            usage = "/setmainspawn [ID]"
        }

        register("sethome") {
            description = "Sets a home point at your location."
            permission = "at.member.sethome"
            usage = "/sethome <Home>"
        }

        register("delhome") {
            description = "Deletes a home point you've set."
            permission = "at.member.delhome"
            usage = "/delhome <Home>"
        }

        register("home") {
            description = "Sends you to your home point."
            permission = "at.member.home"
            usage = "/home <Home>"
        }

        register("homes") {
            description = "Gives a list of homes you've set."
            permission = "at.member.homes"
            usage = "/homes [Player]"
        }

        register("movehome") {
            description = "Moves a home to a new location."
            permission = "at.member.movehome"
            usage = "/movehome <Home>"
        }

        register("setmainhome") {
            description = "Sets a user's main home."
            permission = "at.member.setmainhome"
            usage = "/setmainhome <Home>"
            aliases = listOf("setmhome")
        }

        register("back") {
            description = "Teleports you to your last location."
            permission = "at.member.back"
            usage = "/back"
        }

        register("tploc") {
            description = "Teleports you to a specified location."
            permission = "at.admin.tploc"
            usage = "/tploc <x> <y> <z> [Yaw] [Pitch] [World] [Player]"
        }

        register("tpoffline") {
            description = "Teleports to an offline player."
            permission = "at.admin.tpoffline"
            usage = "/tpoffline <Player>"
            aliases = listOf("tpoffl")
        }

        register("tpofflinehere") {
            description = "Teleports an offline player to your location."
            permission = "at.admin.tpofflinehere"
            usage = "/tpofflinehere <Player>"
            aliases = listOf("tpofflh", "tpofflhere")
        }
    }

    permissions {

        register("at.member.warp.*") {
            default = BukkitPluginDescription.Permission.Default.OP
        }

        register("at.member.spawn") {
            default = BukkitPluginDescription.Permission.Default.TRUE
            childrenMap = mapOf(
                "at.member.spawn.*" to false
            )
        }

        register("at.member.*") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Allows access to member-based commands of the plugin."
            childrenMap = mapOf(
                "at.member.tpr" to true,
                "at.member.off" to true,
                "at.member.on" to true,
                "at.member.toggletp" to true,
                "at.member.cancel" to true,
                "at.member.unblock" to true,
                "at.member.block" to true,
                "at.member.no" to true,
                "at.member.yes" to true,
                "at.member.here" to true,
                "at.member.help" to true,
                "at.member.list" to true,
                "at.member.warp" to true,
                "at.member.warps" to true,
                "at.member.spawn" to true,
                "at.member.sethome" to true,
                "at.member.delhome" to true,
                "at.member.home" to true,
                "at.member.homes" to true,
                "at.member.setmainhome" to true,
                "at.member.movehome" to true,
                "at.member.tpa" to true,
                "at.member.back" to true,
                "at.member.spawn.use-sign" to true,
                "at.member.randomtp.use-sign" to true,
                "at.member.warp.use-sign" to true,
                "at.member.warps.use-sign" to true,
                "at.member.bed.use-sign" to true,
                "at.member.back.death" to true,
                "at.member.warps.location" to true,
                "at.member.homes.location" to true,
                "at.member.spawn.*" to false,
                "at.member.core.*" to false
            )
        }

        register("at.admin.*") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Allows access to admin-based commands of the plugin."
            childrenMap = mapOf(
                "at.admin.tpohere" to true,
                "at.admin.all" to true,
                "at.admin.tpo" to true,
                "at.admin.setwarp" to true,
                "at.admin.delwarp" to true,
                "at.admin.setspawn" to true,
                "at.admin.setspawn.other" to true,
                "at.admin.removespawn" to true,
                "at.admin.mirrorspawn" to true,
                "at.admin.setmainspawn" to true,
                "at.admin.sethome" to true,
                "at.admin.delhome" to true,
                "at.admin.homes" to true,
                "at.admin.home" to true,
                "at.admin.bypass.distance-limit" to true,
                "at.admin.sethome.bypass" to true,
                "at.admin.bypass.teleport-on-join" to true,
                "at.admin.toggletp" to true,
                "at.admin.bypass" to true,
                "at.admin.tploc" to true,
                "at.admin.tploc.others" to true,
                "at.admin.bypass.timer" to true,
                "at.admin.bypass.cooldown" to true,
                "at.admin.request-in-vanish" to true,
                "at.admin.sign.spawn.create" to true,
                "at.admin.sign.randomtp.create" to true,
                "at.admin.sign.warp.create" to true,
                "at.admin.sign.warps.create" to true,
                "at.admin.sign.bed.create" to true,
                "at.admin.tpoffline" to true,
                "at.admin.tpofflinehere" to true,
                "at.member.homes.unlimited" to true
            )
        }
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
    val fileName = project.name + "-" + project.version.toString() + ".jar"
    return tasks.slimJar.get().buildDirectory.resolve("libs").resolve(fileName)
}
