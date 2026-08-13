rootProject.name = "quickshop-hikari-spigot"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    includeBuild("build-logic")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()

        maven("https://repo.papermc.io/repository/maven-public/") {
            content { includeGroupByRegex("io\\.papermc(\\..*)?") }
        }
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
            content {
                includeGroup("org.spigotmc")
                includeGroup("net.md-5")
            }
        }
        maven("https://repo.codemc.io/repository/nms/") {
            content { includeGroup("org.spigotmc") }
        }
        maven("https://repo.codemc.io/repository/maven-public/") {
            content {
                includeGroupByRegex("com\\.ghostchu(\\..*)?")
                includeGroupByRegex("com\\.github\\.retrooper(\\..*)?")
                includeGroupByRegex("net\\.tnemc(\\..*)?")
                includeGroup("net.dmulloy2")
                includeGroupByRegex("cc\\.carm(\\..*)?")
                includeGroup("de.tr7zw")
            }
        }
        maven("https://repo.codemc.io/repository/maven-releases/")
        maven("https://repo.codemc.io/repository/maven-snapshots/")
        maven("https://repo.essentialsx.net/releases/") {
            content { includeGroup("net.essentialsx") }
        }
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
            content { includeGroup("me.clip") }
        }
        maven("https://repo.tcoded.com/releases") {
            content { includeGroupByRegex("com\\.tcoded(\\..*)?") }
        }
        maven("https://repo.viaversion.com/everything/") {
            content { includeGroupByRegex("com\\.viaversion(\\..*)?") }
        }
        maven("https://jitpack.io") {
            content { includeGroupByRegex("com\\.github(\\..*)?") }
            metadataSources {
                mavenPom()
                artifact()
            }
        }

        // Compatibility/addon repositories retained from the upstream Gradle migration.
        maven("https://api.modrinth.com/maven")
        maven("https://m2.dv8tion.net/releases")
        maven("https://maven.devs.beer/")
        maven("https://maven.enginehub.org/repo/")
        maven("https://maven.mohistmc.com/")
        maven("https://nexus.iridiumdevelopment.net/repository/maven-releases/")
        maven("https://nexus.liggesmeyer.net/repository/maven-releases/")
        maven("https://nexus.liggesmeyer.net/repository/maven-snapshots/")
        maven("https://nexus.scarsz.me/content/groups/public/")
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://repo.auxilor.io/repository/maven-public/")
        maven("https://repo.bg-software.com/repository/api/")
        maven("https://repo.bluecolored.de/releases")
        maven("https://repo.crazycrew.us/releases")
        maven("https://repo.dustplanet.de/artifactory/libs-release-local/")
        maven("https://repo.georgev22.com/releases")
        maven("https://repo.glaremasters.me/repository/towny/")
        maven("https://repo.jsinco.dev/releases")
        maven("https://repo.magmaguy.com/releases")
        maven("https://repo.mikeprimm.com/")
        maven("https://repo.minebench.de/")
        maven("https://repo.nexomc.com/releases")
        maven("https://repo.nightexpressdev.com/releases")
        maven("https://repo.opencollab.dev/main")
        maven("https://repo.opencollab.dev/maven-snapshots")
        maven("https://repo.oraxen.com/releases")
        maven("https://repo.songoda.com/repository/minecraft-plugins/")
        maven("https://repo.thenextlvl.net/releases")
        maven("https://repo.william278.net/releases")
        maven("https://repo.xenondevs.xyz/releases")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}

include(
    "quickshop-common",
    "quickshop-api",
    "platform:quickshop-platform-interface",
    "platform:quickshop-platform-spigot-abstract",
    "platform:quickshop-platform-spigot-v1_20_R1",
    "platform:quickshop-platform-spigot-v1_20_R2",
    "platform:quickshop-platform-spigot-v1_20_R3",
    "quickshop-bukkit",
    "compatibility:common",
)

listOf(
    "advancedregionmarket", "bentobox", "bungeecord", "bungeecord-geyser",
    "chestprotect", "clearlag", "dominion", "ecoenchants", "elitemobs", "fabledskyblock",
    "griefprevention", "husktowns", "itemsadder", "lands", "matcherplus",
    "openinv", "plotsquared", "reforges", "residence", "slimefun", "superiorskyblock",
    "towny", "velocity", "voidchest", "worldedit", "worldguard",
).forEach { include("compatibility:$it") }

listOf(
    "bluemap", "discordsrv", "discount", "displaycontrol", "dynmap", "limited",
    "list", "plan", "reremake-migrator", "shopitemonly",
).forEach { include("addon:$it") }
