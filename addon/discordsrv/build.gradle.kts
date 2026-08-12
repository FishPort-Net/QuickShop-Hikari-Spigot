plugins {
    id("quickshop.addon-conventions")
}

dependencies {
    compileOnly(libs.spigot.api.legacy)
    compileOnly(project(":quickshop-bukkit"))
    compileOnly("com.discordsrv:discordsrv:1.29.0")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set("Addon-DiscordSRV")
}
