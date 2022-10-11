import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("java-library")
    id("maven-publish")
    alias(libs.plugins.shadow)
    alias(libs.plugins.userdev)
    alias(libs.plugins.bukkitYML)
}

repositories {

    maven {
        name = "Spigot"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }

    maven {
        name = "Paper and PaperLib"
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }

    maven {
        name = "Vault"
        url = uri("https://repo.codemc.io/repository/maven-public/")
    }

    maven {
        name = "WorldBorder and Chunky"
        url = uri("https://jitpack.io")
    }

    maven {
        name = "ConfigurationMaster"
        url = uri("https://ci.pluginwiki.us/plugin/repository/everything/")
    }

    maven {
        name = "Essentials"
        url = uri("https://repo.essentialsx.net/releases/")
    }

    maven {
        name = "Geyser"
        url = uri("https://repo.opencollab.dev/maven-snapshots/")
    }

    maven {
        name = "authlib maybe"
        url = uri("https://libraries.minecraft.net/")
    }

    maven {
        name = "Adventure"
        url = uri("https://repo.maven.apache.org/maven2/")
    }

    maven {
        name = "Sk89q"
        url = uri("https://maven.enginehub.org/repo/")
    }

    maven {
        name = "Squaremap"
        url = uri("https://repo.jpenilla.xyz/snapshots/")
    }

    maven {
        name = "Dynmap"
        url = uri("https://repo.mikeprimm.com/")
    }
}

dependencies {
    paperDevBundle("1.19.2-R0.1-SNAPSHOT")

    implementation(libs.paperlib)
    implementation(libs.kyori.nbt)
    implementation(libs.kyori.examination)
    implementation(libs.configuration)
    implementation(libs.annotations)

    compileOnly(libs.vault)
    compileOnly(libs.essentials)
    compileOnly(libs.essentials.spawn)
    compileOnly(libs.worldborder)
    compileOnly(libs.chunkyborder)
    compileOnly(libs.floodgate)
    compileOnly(libs.lands)
    compileOnly(libs.griefprevention)
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

    withType<ProcessResources> {
        val currentDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:'Z'").format(Date())
        inputs.property("version", project.version)
        inputs.property("timestamp", currentDate)

        filesMatching("update.properties") {
            expand(mutableMapOf("timestamp" to currentDate))
        }
    }

    shadowJar {
        dependencyFilter.apply {
            dependency(libs.paperlib.get())
            dependency(libs.configuration.get())
            dependency(libs.kyori.nbt.get())
            dependency(libs.kyori.examination.get())
        }

        val baseRelocation = "io.github.niestrat99.advancedteleport"
        relocate("io.paper", "$baseRelocation.paperlib")
        relocate("io.github.thatsmusic99.configurationsmaster", "$baseRelocation.configurationmaster")
    }
}

bukkit {
    main = "io.github.niestrat99.advancedteleport.CoreClass"
    version = project.version.toString()
    description = "A rapidly growing teleportation plugin looking to break the boundaries of traditional teleport plugins."
    authors = listOf("Niestrat99, Thatsmusic99, SuspiciousLookingOwl (Github), animeavi (Github), MrEngMan, LucasMucGH (Github), jonas-t-s (Github), DaRacci")
    softDepend = listOf("Vault, Ultimate_Economy, ConfigurationMaster, WorldBorder, ChunkyBorder, floodgate, Lands, WorldGuard, GriefProtection, dynmap, squaremap, PlayerParticles")
    loadBefore = listOf("Essentials, EssentialsSpawn")
    apiVersion = "1.16"

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
