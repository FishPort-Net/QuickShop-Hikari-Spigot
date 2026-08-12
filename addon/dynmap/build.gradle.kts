plugins {
    id("quickshop.addon-conventions")
}

dependencies {
    compileOnly(libs.spigot.api.legacy)
    compileOnly(project(":quickshop-bukkit"))
    compileOnly("org.dynmap:DynmapCoreAPI:2.0")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set("Addon-Dynmap")
}
