import com.ghostchu.quickshop.buildlogic.SpigotRemapper
import com.ghostchu.quickshop.buildlogic.SpigotRemapTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar

plugins {
    id("quickshop.core-conventions")
}

data class SpigotPlatformVersion(
    val compileVersion: String,
    val mappingVersion: String = compileVersion,
    val javaVersion: Int,
)

val platformVersion = when (project.name) {
    "quickshop-platform-spigot-v1_20_R1" -> SpigotPlatformVersion("1.20.1-R0.1-SNAPSHOT", javaVersion = 17)
    "quickshop-platform-spigot-v1_20_R2" -> SpigotPlatformVersion("1.20.2-R0.1-SNAPSHOT", javaVersion = 17)
    "quickshop-platform-spigot-v1_20_R3" -> SpigotPlatformVersion("1.20.4-R0.1-SNAPSHOT", javaVersion = 17)
    "quickshop-platform-spigot-v1_20_R4" -> SpigotPlatformVersion("1.20.5-R0.1-SNAPSHOT", "1.20.6-R0.1-SNAPSHOT", 21)
    "quickshop-platform-spigot-v1_21_R1" -> SpigotPlatformVersion("1.21.1-R0.1-SNAPSHOT", javaVersion = 21)
    "quickshop-platform-spigot-v1_21_R2" -> SpigotPlatformVersion("1.21.1-R0.1-SNAPSHOT", javaVersion = 21)
    "quickshop-platform-spigot-v1_21_R3" -> SpigotPlatformVersion("1.21.3-R0.1-SNAPSHOT", javaVersion = 21)
    "quickshop-platform-spigot-v1_21_R4" -> SpigotPlatformVersion("1.21.4-R0.1-SNAPSHOT", javaVersion = 21)
    "quickshop-platform-spigot-v1_21_R5" -> SpigotPlatformVersion("1.21.5-R0.1-SNAPSHOT", javaVersion = 21)
    else -> error("Unknown Spigot platform module: ${project.name}")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(platformVersion.javaVersion))
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(platformVersion.javaVersion)
}

val mappingsMojang: Configuration = configurations.create("mappingsMojang") { isTransitive = false }
val mappingsSpigot: Configuration = configurations.create("mappingsSpigot") { isTransitive = false }
val spigotMojang: Configuration = configurations.create("spigotMojang") { isTransitive = false }
val spigotObfuscated: Configuration = configurations.create("spigotObfuscated") { isTransitive = false }

dependencies {
    api(project(":platform:quickshop-platform-spigot-abstract"))
    compileOnly("org.spigotmc:spigot:${platformVersion.compileVersion}:remapped-mojang")
    compileOnly("de.tr7zw:item-nbt-api-plugin:2.15.0") { isTransitive = false }

    mappingsMojang("org.spigotmc:minecraft-server:${platformVersion.mappingVersion}:maps-mojang@txt")
    mappingsSpigot("org.spigotmc:minecraft-server:${platformVersion.mappingVersion}:maps-spigot@csrg")
    spigotMojang("org.spigotmc:spigot:${platformVersion.mappingVersion}:remapped-mojang")
    spigotObfuscated("org.spigotmc:spigot:${platformVersion.mappingVersion}:remapped-obf")
}

val unremappedJar = tasks.named<Jar>("jar")
val obfuscatedJar = layout.buildDirectory.file("libs/${project.name}-${project.version}-remapped-obf.jar")
val remappedJar = layout.buildDirectory.file("libs/${project.name}-${project.version}-remapped.jar")

val remapObfuscated = tasks.register<SpigotRemapTask>("remapObfuscated") {
    group = "build"
    description = "Remaps Mojang-named NMS references to obfuscated names."
    dependsOn(unremappedJar)
    inputJar.set(unremappedJar.flatMap { it.archiveFile })
    mappings.from(mappingsMojang)
    inheritanceClasspath.from(spigotMojang, configurations.compileClasspath)
    reverse.set(true)
    outputJar.set(obfuscatedJar)
}

val remapSpigot = tasks.register<SpigotRemapTask>("remapSpigot") {
    group = "build"
    description = "Remaps obfuscated NMS references to Spigot runtime names."
    dependsOn(remapObfuscated)
    inputJar.set(obfuscatedJar)
    mappings.from(mappingsSpigot)
    inheritanceClasspath.from(spigotObfuscated, configurations.compileClasspath)
    reverse.set(false)
    outputJar.set(remappedJar)
}

configurations.named("runtimeElements") {
    outgoing.artifacts.clear()
    outgoing.artifact(remappedJar) {
        builtBy(remapSpigot)
    }
}

tasks.named("assemble") {
    dependsOn(remapSpigot)
}
