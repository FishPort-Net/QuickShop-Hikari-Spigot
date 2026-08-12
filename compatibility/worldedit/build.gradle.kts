plugins {
    id("quickshop.compat-conventions")
}

dependencies {
    compileOnly(libs.spigot.api.legacy)
    implementation(project(":compatibility:common"))
    compileOnly(project(":quickshop-bukkit"))
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.18") {
        exclude("org.bstats")
        exclude("it.unimi.dsi", "fastutil")
        exclude("io.papermc", "paperlib")
        exclude("org.antlr")
        exclude("com.google.code.gson", "gson")
        exclude("com.google.guava", "guava")
        exclude("com.sk89q", "jchronic")
        exclude("com.sk89q.lib", "jlibnoise")
        exclude("org.enginehub.lin-bus")
        exclude("org.enginehub.lin-bus.format")
        exclude("org.apache.logging.log4j", "log4j-api")
        exclude("com.thoughtworks.paranamer", "paranamer")
        exclude("org.mozilla", "rhino-runtime")
        exclude("org.yaml", "snakeyaml")
        exclude("com.google.code.findbugs", "jsr305")
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set("Compat-WorldEdit")
}
