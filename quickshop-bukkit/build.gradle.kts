import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.ghostchu.quickshop.buildlogic.GitInfoValueSource
import com.ghostchu.quickshop.buildlogic.StageSanitizedDependencyJar
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.bundling.Jar

plugins {
    id("quickshop.core-conventions")
    id("quickshop.shadow-conventions")
}

val easySqlHikariForShadow = configurations.create("easySqlHikariForShadow") {
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false
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
    implementation("com.google.guava:guava:31.1-jre") {
        // These are compile-time annotations only. Keep the runtime bundle to
        // Guava plus failureaccess, both relocated into QuickShop's namespace.
        exclude("com.google.code.findbugs", "jsr305")
        exclude("org.checkerframework", "checker-qual")
        exclude("com.google.errorprone", "error_prone_annotations")
        exclude("com.google.j2objc", "j2objc-annotations")
    }
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
    easySqlHikariForShadow("cc.carm.lib:easysql-hikaricp:0.4.7")
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
}

sourceSets {
    main {
        resources {
            srcDir("../crowdin")
        }
    }
}

// Shadow currently receives project dependencies as JAR-backed zip trees. On
// Java 21, Gradle can expose an empty temporary file for an entry in those
// trees, causing Shadow's relocator to feed zero bytes to ASM. Expand only the
// non-remapped project JARs first so relocation reads stable directory files.
// The version-specific Spigot JARs intentionally remain on runtimeClasspath:
// their published artifacts are the SpecialSource-remapped outputs.
val projectsStagedForShadow = buildList {
    add(project(":quickshop-common"))
    add(project(":quickshop-api"))
    add(project(":platform:quickshop-platform-interface"))
    add(project(":platform:quickshop-platform-spigot-abstract"))
}

val stageProjectOutputsForShadow = tasks.register<Sync>("stageProjectOutputsForShadow") {
    into(layout.buildDirectory.dir("shadow/project-outputs"))
    includeEmptyDirs = false
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("META-INF/MANIFEST.MF")

    projectsStagedForShadow.forEach { dependencyProject ->
        val jarTask = dependencyProject.tasks.named<Jar>("jar")
        from(jarTask.map { zipTree(it.archiveFile.get().asFile) })
    }
}

// EasySQL Hikari 0.4.7 is a fat JAR that embeds SLF4J 1.7. Shadow must not
// combine those classes with QuickShop's SLF4J 2.0 API and JUL provider.
val stageEasySqlHikariForShadow = tasks.register<StageSanitizedDependencyJar>("stageEasySqlHikariForShadow") {
    inputJars.from(easySqlHikariForShadow)
    excludedPrefixes.set(listOf("org/slf4j/", "META-INF/maven/org.slf4j/"))
    outputDirectory.set(layout.buildDirectory.dir("shadow/easysql-hikaricp"))
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
    dependencies {
        projectsStagedForShadow.forEach { dependencyProject ->
            exclude(project(dependencyProject.path))
        }
        exclude(dependency("cc.carm.lib:easysql-hikaricp:.*"))
    }
    from(stageProjectOutputsForShadow)
    from(stageEasySqlHikariForShadow)
}
