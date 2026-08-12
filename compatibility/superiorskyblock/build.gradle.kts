plugins {
    id("quickshop.compat-conventions")
}

dependencies {
    compileOnly(libs.spigot.api.legacy)
    implementation(project(":compatibility:common"))
    compileOnly(project(":quickshop-bukkit"))
    compileOnly("com.bgsoftware:SuperiorSkyblockAPI:1.11.1")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set("Compat-SuperiorSkyblock")
}
