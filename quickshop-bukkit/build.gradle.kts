import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.ghostchu.quickshop.buildlogic.GitInfoValueSource
import org.gradle.api.attributes.java.TargetJvmVersion

plugins {
    id("quickshop.core-conventions")
    id("quickshop.shadow-conventions")
}

if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_21)) {
    // The plugin core remains Java 17, but the Java 21 distribution also
    // embeds the modern NMS adapters whose own bytecode must target Java 21.
    configurations.named("runtimeClasspath") {
        attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 21)
    }
}

dependencies {
    compileOnly(libs.spigot.api.legacy)
    api(project(":quickshop-api"))
    compileOnly(project(":platform:quickshop-platform-spigot-abstract"))

    compileOnly("net.dmulloy2:ProtocolLib:5.3.0")
    compileOnly("com.github.retrooper:packetevents-spigot:2.9.0-SNAPSHOT")
    compileOnly("net.milkbowl.vault:VaultUnlockedAPI:2.13") { isTransitive = false }
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1") { isTransitive = false }
    compileOnly("me.clip:placeholderapi:2.11.6") { isTransitive = false }

    implementation("net.tnemc:TNML-Bukkit:1.6.0.0-SNAPSHOT-15") {
        exclude("net.tnemc", "TNIL-Bukkit")
        exclude("net.tnemc", "TNIL-CORE")
        exclude("net.kyori")
    }
    implementation("net.tnemc:TNIL-Bukkit:0.1.7.6-LEGACY-13") {
        exclude("net.kyori")
    }
    implementation("net.tnemc:TNML-CORE:1.6.0.0-SNAPSHOT-15") {
        exclude("net.tnemc", "TNIL-Core")
        exclude("net.kyori")
    }
    implementation("net.tnemc:TNIL-Core:0.1.7.6-LEGACY-13") {
        exclude("net.kyori")
    }

    implementation(libs.paperlib)
    implementation("com.h2database:h2:2.1.214")
    implementation("com.mysql:mysql-connector-j:8.4.0")
    implementation("com.konghq:unirest-java:3.14.5")
    implementation("net.sourceforge.csvjdbc:csvjdbc:1.0.42")
    implementation("org.dom4j:dom4j:2.1.4")
    implementation("com.ghostchu.crowdin:crowdinota:1.0.3")
    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("com.rollbar:rollbar-java:2.0.0") {
        exclude("org.slf4j", "slf4j-api")
    }
    implementation("cc.carm.lib:easysql-hikaricp:0.4.7")
    implementation("org.apache.commons:commons-compress:1.26.2")
    implementation("com.tcoded:FoliaLib:0.5.1")

    compileOnly("net.essentialsx:EssentialsX:2.21.0") {
        exclude("io.papermc", "paperlib")
        exclude("org.yaml", "snakeyaml")
        exclude("com.google.errorprone", "error_prone_annotations")
        exclude("org.checkerframework", "checker-qual")
    }

    runtimeOnly(project(":platform:quickshop-platform-spigot-v1_20_R1"))
    runtimeOnly(project(":platform:quickshop-platform-spigot-v1_20_R2"))
    runtimeOnly(project(":platform:quickshop-platform-spigot-v1_20_R3"))

    if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_21)) {
        runtimeOnly(project(":platform:quickshop-platform-spigot-v1_20_R4"))
        runtimeOnly(project(":platform:quickshop-platform-spigot-v1_21_R1"))
        runtimeOnly(project(":platform:quickshop-platform-spigot-v1_21_R2"))
        runtimeOnly(project(":platform:quickshop-platform-spigot-v1_21_R3"))
        runtimeOnly(project(":platform:quickshop-platform-spigot-v1_21_R4"))
        runtimeOnly(project(":platform:quickshop-platform-spigot-v1_21_R5"))
        runtimeOnly(project(":platform:quickshop-platform-paper"))
    }
}

sourceSets {
    main {
        resources {
            srcDir("../crowdin")
        }
    }
}

tasks.named<ProcessResources>("processResources") {
    val pluginVersion = project.version.toString()
    filesMatching("plugin.yml") {
        expand(mapOf("project" to mapOf("version" to pluginVersion)))
    }

    val gitInfo = providers.of(GitInfoValueSource::class) {}
    filesMatching("BUILDINFO") {
        filter { line ->
            val tokens = gitInfo.get() + mapOf("git.build.version" to pluginVersion)
            tokens.entries.fold(line) { acc, (key, value) -> acc.replace("\${$key}", value) }
        }
    }
}

tasks.withType<ShadowJar>().configureEach {
    archiveBaseName.set("QuickShop-Hikari")
}
