plugins {
    id("quickshop.compat-conventions")
}

dependencies {
    compileOnly(libs.spigot.api.legacy)
    implementation(project(":compatibility:common"))
    compileOnly(project(":quickshop-bukkit"))
    compileOnly("com.craftaro:FabledSkyBlock:3.5.2-SNAPSHOT")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set("Compat-FabledSkyBlock")
}
