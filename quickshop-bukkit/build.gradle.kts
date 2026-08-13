import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.ghostchu.quickshop.buildlogic.GenerateRuntimeLibraryManifest
import com.ghostchu.quickshop.buildlogic.GitInfoValueSource
import com.ghostchu.quickshop.buildlogic.QuickShopRelocations
import com.ghostchu.quickshop.buildlogic.StageSanitizedDependencyJar
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.attributes.java.TargetJvmVersion
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

val crowdinOtaForShadow = configurations.create("crowdinOtaForShadow") {
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false
}

fun nonTransitiveShadowDependency(name: String) = configurations.create(name) {
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false
}

val tnmlBukkitForShadow = nonTransitiveShadowDependency("tnmlBukkitForShadow")
val tnilBukkitForShadow = nonTransitiveShadowDependency("tnilBukkitForShadow")
val tnmlCoreForShadow = nonTransitiveShadowDependency("tnmlCoreForShadow")
val tnilCoreForShadow = nonTransitiveShadowDependency("tnilCoreForShadow")
val xpp3ForShadow = nonTransitiveShadowDependency("xpp3ForShadow")
val jaxenForShadow = nonTransitiveShadowDependency("jaxenForShadow")

// Only the bootstrap and platform modules are packaged directly. Private
// libraries are downloaded, verified, relocated and cached individually.
val thinRuntime = configurations.create("thinRuntime") {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val privateRuntime = configurations.create("privateRuntime") {
    isCanBeConsumed = false
    isCanBeResolved = true
    extendsFrom(configurations.api.get(), configurations.implementation.get())
    attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 17)
}

if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_21)) {
    // Common/plugin code remains Java 17. A Java 21 release build additionally
    // resolves the modern Spigot adapters that are loaded only by class name.
    configurations.named("runtimeClasspath") {
        attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 21)
    }
}

// Core classes must retain the Spigot 1.20.1 ABI even in Java 21 release
// builds. Compile-only integrations such as EssentialsX otherwise upgrade the
// API transitively to 1.21, where InventoryView changed from a class to an
// interface and produces bytecode that cannot run on 1.20.1.
configurations.named("compileClasspath") {
    resolutionStrategy.force(libs.spigot.api.legacy.get())
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
    compileOnly("com.ghostchu.lib.unofficial.com.alessiodp.libby:libby-bukkit:2.0.2-SNAPSHOT")
    thinRuntime("com.ghostchu.lib.unofficial.com.alessiodp.libby:libby-bukkit:2.0.2-SNAPSHOT")

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
    tnmlBukkitForShadow("net.tnemc:TNML-Bukkit:1.6.0.0-SNAPSHOT-15")
    tnilBukkitForShadow("net.tnemc:TNIL-Bukkit:0.1.7.6-LEGACY-13")
    tnmlCoreForShadow("net.tnemc:TNML-CORE:1.6.0.0-SNAPSHOT-15")
    tnilCoreForShadow("net.tnemc:TNIL-Core:0.1.7.6-LEGACY-13")

    implementation(libs.paperlib)
    implementation("com.google.guava:guava:31.1-jre") {
        // These are compile-time annotations only. Keep the runtime set to
        // Guava plus failureaccess, both relocated into QuickShop's namespace.
        exclude("com.google.code.findbugs", "jsr305")
        exclude("org.checkerframework", "checker-qual")
        exclude("com.google.errorprone", "error_prone_annotations")
        exclude("com.google.j2objc", "j2objc-annotations")
    }
    implementation("com.h2database:h2:2.1.214")
    implementation("com.mysql:mysql-connector-j:8.4.0") {
        // QuickShop uses classic JDBC only. Protobuf is required by Connector/J's
        // optional X Protocol implementation and adds roughly 1.8 MB to the JAR.
        exclude("com.google.protobuf", "protobuf-java")
    }
    // Keep one complete HTTP implementation. Shadow relocates it into QuickShop's
    // namespace; the duplicate copy embedded in CrowdinOTA is removed below.
    implementation("com.konghq:unirest-java:3.14.5")
    implementation("net.sourceforge.csvjdbc:csvjdbc:1.0.42")
    implementation("org.dom4j:dom4j:2.1.4")
    xpp3ForShadow("xpp3:xpp3:1.1.4c")
    jaxenForShadow("jaxen:jaxen:1.1.6")
    // CrowdinOTA is published as a fat JAR containing its own complete Unirest
    // stack. Compile against it, then stage only CrowdinOTA's own classes below.
    compileOnly("com.ghostchu.crowdin:crowdinota:1.0.3") { isTransitive = false }
    crowdinOtaForShadow("com.ghostchu.crowdin:crowdinota:1.0.3")
    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("com.rollbar:rollbar-java:2.0.0") {
        exclude("org.slf4j", "slf4j-api")
    }
    implementation("cc.carm.lib:easysql-hikaricp:0.4.7")
    easySqlHikariForShadow("cc.carm.lib:easysql-hikaricp:0.4.7")
    implementation("org.apache.commons:commons-lang3:3.14.0")
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
    thinRuntime(project(":platform:quickshop-platform-spigot-v1_20_R1")) { isTransitive = false }
    thinRuntime(project(":platform:quickshop-platform-spigot-v1_20_R2")) { isTransitive = false }
    thinRuntime(project(":platform:quickshop-platform-spigot-v1_20_R3")) { isTransitive = false }

    if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_21)) {
        runtimeOnly(project(":platform:quickshop-platform-spigot-v1_20_R4"))
        runtimeOnly(project(":platform:quickshop-platform-spigot-v1_21_R1"))
        runtimeOnly(project(":platform:quickshop-platform-spigot-v1_21_R2"))
        runtimeOnly(project(":platform:quickshop-platform-spigot-v1_21_R3"))
        runtimeOnly(project(":platform:quickshop-platform-spigot-v1_21_R4"))
        runtimeOnly(project(":platform:quickshop-platform-spigot-v1_21_R5"))
        thinRuntime(project(":platform:quickshop-platform-spigot-v1_20_R4")) { isTransitive = false }
        thinRuntime(project(":platform:quickshop-platform-spigot-v1_21_R1")) { isTransitive = false }
        thinRuntime(project(":platform:quickshop-platform-spigot-v1_21_R2")) { isTransitive = false }
        thinRuntime(project(":platform:quickshop-platform-spigot-v1_21_R3")) { isTransitive = false }
        thinRuntime(project(":platform:quickshop-platform-spigot-v1_21_R4")) { isTransitive = false }
        thinRuntime(project(":platform:quickshop-platform-spigot-v1_21_R5")) { isTransitive = false }
    }
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

// CrowdinOTA 1.0.3 embeds an entire unrelocated HTTP client despite declaring
// the same standalone client as a Maven dependency. Keep its API only and use
// QuickShop's single relocated Unirest copy.
val stageCrowdinOtaForShadow = tasks.register<StageSanitizedDependencyJar>("stageCrowdinOtaForShadow") {
    inputJars.from(crowdinOtaForShadow)
    excludedPrefixes.set(listOf(
        "com/google/",
        "kong/",
        "mozilla/",
        "org/",
        "unirest/",
        "META-INF/DEPENDENCIES",
        "META-INF/LICENSE",
        "META-INF/NOTICE",
        "META-INF/maven/com.google.code.gson/",
        "META-INF/maven/com.konghq/",
        "META-INF/maven/commons-codec/",
        "META-INF/maven/commons-logging/",
        "META-INF/maven/org.apache.httpcomponents/",
        "META-INF/versions/"
    ))
    outputDirectory.set(layout.buildDirectory.dir("shadow/crowdinota"))
}

fun stageTneArtifact(name: String, configuration: Configuration) =
    tasks.register<StageSanitizedDependencyJar>(name) {
        inputJars.from(configuration)
        includedPrefixes.set(listOf("net/tnemc/"))
        excludedPrefixes.set(emptyList())
        outputDirectory.set(layout.buildDirectory.dir("shadow/$name"))
    }

val stageTnmlBukkitForShadow = stageTneArtifact("stageTnmlBukkitForShadow", tnmlBukkitForShadow)
val stageTnilBukkitForShadow = stageTneArtifact("stageTnilBukkitForShadow", tnilBukkitForShadow)
val stageTnmlCoreForShadow = stageTneArtifact("stageTnmlCoreForShadow", tnmlCoreForShadow)
val stageTnilCoreForShadow = stageTneArtifact("stageTnilCoreForShadow", tnilCoreForShadow)

val stageXpp3ForShadow = tasks.register<StageSanitizedDependencyJar>("stageXpp3ForShadow") {
    inputJars.from(xpp3ForShadow)
    includedPrefixes.set(listOf("org/xmlpull/"))
    excludedPrefixes.set(emptyList())
    outputDirectory.set(layout.buildDirectory.dir("shadow/xpp3"))
}

val stageJaxenForShadow = tasks.register<StageSanitizedDependencyJar>("stageJaxenForShadow") {
    inputJars.from(jaxenForShadow)
    includedPrefixes.set(listOf("org/jaxen/"))
    excludedPrefixes.set(emptyList())
    outputDirectory.set(layout.buildDirectory.dir("shadow/jaxen"))
}

tasks.named<ProcessResources>("processResources") {
    val pluginVersion = project.version.toString()
    val gitInfo = providers.of(GitInfoValueSource::class) {}
    inputs.property("pluginVersion", pluginVersion)
    inputs.property("gitInfo", gitInfo)

    filesMatching("plugin.yml") {
        expand(mapOf("project" to mapOf("version" to pluginVersion)))
    }

    filesMatching("BUILDINFO") {
        filter { line ->
            val tokens = gitInfo.get() + mapOf("git.build.version" to pluginVersion)
            tokens.entries.fold(line) { acc, (key, value) -> acc.replace("\${$key}", value) }
        }
    }
}

val privateRuntimeArtifacts = privateRuntime.incoming.artifactView {
    componentFilter { it is ModuleComponentIdentifier }
}.artifacts

val runtimeCoordinates = providers.provider {
    buildMap {
        privateRuntimeArtifacts.artifacts.forEach { artifact ->
            val identifier = artifact.id.componentIdentifier as ModuleComponentIdentifier
            val coordinate = "${identifier.group}:${identifier.module}:${identifier.version}"
            require(put(artifact.file.name, coordinate) == null) {
                "Duplicate runtime dependency file name: ${artifact.file.name}"
            }
        }
    }
}

val runtimeManifestDirectory = layout.buildDirectory.dir("generated/runtime-library-manifest")
val generateRuntimeLibraryManifest = tasks.register<GenerateRuntimeLibraryManifest>("generateRuntimeLibraryManifest") {
    group = "build"
    description = "Generates the verified per-dependency runtime library manifest."
    inputJars.from(privateRuntimeArtifacts.artifactFiles)
    coordinatesByFileName.set(runtimeCoordinates)
    excludedModules.set(setOf(
        // The sanitized EasySQL fat JAR provides the implementation and pool,
        // while its separate API JAR remains a private runtime dependency.
        "cc.carm.lib:easysql-impl",
        "cc.carm.lib:easysql-hikaricp",
        // TNE publishes fat JARs with mutually duplicated dependencies. Only
        // its own packages are staged into the plugin JAR below.
        "net.tnemc:TNIL-Bukkit",
        "net.tnemc:TNIL-Core",
        "net.tnemc:TNML-Bukkit",
        "net.tnemc:TNML-CORE",
        // xpp3 embeds a conflicting copy of javax.xml.namespace.QName.
        "xpp3:xpp3",
        // Jaxen embeds a JDK DOM compatibility class.
        "jaxen:jaxen",
        // Compile-time placeholders or APIs supplied by Java 17 itself.
        "com.google.errorprone:error_prone_annotations",
        "com.google.guava:listenablefuture",
        "javax.xml.stream:stax-api"
    ))
    relocations.set(QuickShopRelocations.all.map { "${it.source}=${it.target}" })
    repositoryRoutes.set(mapOf(
        "com.tcoded" to "https://repo.tcoded.com/releases",
        "io.papermc" to "https://repo.papermc.io/repository/maven-public"
    ))
    defaultRepository.set("https://repo.maven.apache.org/maven2")
    outputFile.set(runtimeManifestDirectory.map { it.file("quickshop-libraries.properties") })
}

sourceSets.main {
    resources.srcDir(runtimeManifestDirectory)
}

tasks.named<ProcessResources>("processResources") {
    dependsOn(generateRuntimeLibraryManifest)
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("QuickShop-Hikari")
    configurations = listOf(thinRuntime)
    dependencies {
        // Project outputs are staged below so Shadow sees stable files.
    }
    from(stageProjectOutputsForShadow)
    // Keep sanitized package-only copies of upstream fat or malformed JARs so
    // their embedded dependencies and JDK compatibility classes are not loaded.
    from(stageEasySqlHikariForShadow)
    from(stageCrowdinOtaForShadow)
    from(stageTnmlBukkitForShadow)
    from(stageTnilBukkitForShadow)
    from(stageTnmlCoreForShadow)
    from(stageTnilCoreForShadow)
    from(stageXpp3ForShadow)
    from(stageJaxenForShadow)
    dependsOn(generateRuntimeLibraryManifest)
}
