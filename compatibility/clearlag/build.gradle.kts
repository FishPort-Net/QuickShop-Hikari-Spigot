plugins {
    id("quickshop.compat-conventions")
}

dependencies {
    compileOnly(libs.spigot.api.legacy)
    implementation(project(":compatibility:common"))
    compileOnly(project(":quickshop-bukkit"))
    implementation(files("lib/Clearlag-3.2.2.jar"))
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set("Compat-Clearlag")
}
