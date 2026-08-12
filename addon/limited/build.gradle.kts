plugins {
    id("quickshop.addon-conventions")
}

dependencies {
    compileOnly(libs.spigot.api.legacy)
    compileOnly(project(":quickshop-bukkit"))
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set("Addon-Limited")
}
