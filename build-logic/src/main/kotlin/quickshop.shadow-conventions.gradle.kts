import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.file.DuplicatesStrategy

plugins {
    id("quickshop.java-conventions")
}

apply<ShadowPlugin>()

tasks.withType<ShadowJar>().configureEach {
    archiveClassifier.set("")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    // Service descriptors must reach Shadow's transformer before duplicate
    // handling, otherwise only one of the H2/MySQL JDBC providers survives.
    filesMatching("META-INF/services/**") {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    manifest {
        attributes["Main-Class"] = "com.ghostchu.quickshop.bootstrap.Bootstrap"
    }

    // Every private runtime library is bundled and moved out of the server/plugin namespace.
    relocate("net.tnemc", "com.ghostchu.quickshop.shade.tne")
    relocate("com.alessiodp.libby", "com.ghostchu.quickshop.shade.com.alessiodp.libby")
    relocate("cc.carm.lib", "com.ghostchu.quickshop.shade.cc.carm.lib")
    relocate("com.zaxxer.hikari", "com.ghostchu.quickshop.shade.com.zaxxer.hikari")
    relocate("com.tcoded", "com.ghostchu.quickshop.shade.com.tcoded")
    relocate("io.papermc.lib", "com.ghostchu.quickshop.shade.io.papermc.lib")
    relocate("de.tr7zw.changeme.nbtapi", "com.ghostchu.quickshop.shade.de.tr7zw.changeme.nbtapi")
    relocate("org.bstats", "com.ghostchu.quickshop.shade.org.bstats")
    relocate("de.themoep.minedown", "com.ghostchu.quickshop.shade.de.themoep.minedown")
    relocate("net.kyori", "com.ghostchu.quickshop.shade.net.kyori")
    relocate("org.apache.commons", "com.ghostchu.quickshop.shade.org.apache.commons")
    relocate("org.apache.http", "com.ghostchu.quickshop.shade.org.apache.http")
    relocate("org.apache.hc", "com.ghostchu.quickshop.shade.org.apache.hc")
    relocate("org.slf4j", "com.ghostchu.quickshop.shade.org.slf4j")
    relocate("com.google.gson", "com.ghostchu.quickshop.shade.com.google.gson")
    relocate("com.google.common", "com.ghostchu.quickshop.shade.com.google.common")
    relocate("com.google.thirdparty", "com.ghostchu.quickshop.shade.com.google.thirdparty")
    relocate("com.google.protobuf", "com.ghostchu.quickshop.shade.com.google.protobuf")
    relocate("com.fasterxml.jackson", "com.ghostchu.quickshop.shade.com.fasterxml.jackson")
    relocate("com.rollbar", "com.ghostchu.quickshop.shade.com.rollbar")
    relocate("org.h2", "com.ghostchu.quickshop.shade.org.h2")
    relocate("kong.unirest", "com.ghostchu.quickshop.shade.kong.unirest")
    relocate("org.relique.jdbc", "com.ghostchu.quickshop.shade.org.relique.jdbc")
    relocate("org.dom4j", "com.ghostchu.quickshop.shade.org.dom4j")
    relocate("com.vdurmont.semver4j", "com.ghostchu.quickshop.shade.com.vdurmont.semver4j")
    relocate("com.ghostchu.crowdin", "com.ghostchu.quickshop.shade.com.ghostchu.crowdin")
    relocate("com.ghostchu.simplereloadlib", "com.ghostchu.quickshop.shade.com.ghostchu.simplereloadlib")
    relocate("com.mysql", "com.ghostchu.quickshop.shade.com.mysql")
    relocate("com.mohistmc", "com.ghostchu.quickshop.shade.com.mohistmc")
    relocate("com.sun.msv", "com.ghostchu.quickshop.shade.com.sun.msv")
    relocate("com.sun.xml", "com.ghostchu.quickshop.shade.com.sun.xml")
    relocate("io.vertx", "com.ghostchu.quickshop.shade.io.vertx")
    relocate("org.jaxen", "com.ghostchu.quickshop.shade.org.jaxen")
    relocate("org.xmlpull", "com.ghostchu.quickshop.shade.org.xmlpull")
    relocate("org.gjt", "com.ghostchu.quickshop.shade.org.gjt")
    relocate("org.relaxng", "com.ghostchu.quickshop.shade.org.relaxng")
    relocate("org.json.simple", "com.ghostchu.quickshop.shade.org.json.simple")
    relocate("org.relique", "com.ghostchu.quickshop.shade.org.relique")
    relocate("javax.xml.bind", "com.ghostchu.quickshop.shade.javax.xml.bind")
    relocate("unirest.shaded", "com.ghostchu.quickshop.shade.unirest.shaded")

    mergeServiceFiles()

    exclude(
        "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/*.kotlin_module",
        "META-INF/*.txt", "META-INF/INDEX.LIST", "META-INF/proguard/**",
        "module-info.class", "META-INF/versions/**/module-info.class", "META-INF/versions/9/**",
        "com/google/errorprone/**", "org/jetbrains/**", "org/intellij/**",
        "javax/xml/namespace/**", "javax/xml/stream/**", "org/w3c/dom/UserDataHandler.class",
        "*License*", "*LICENSE*"
    )
}

tasks.named("assemble") {
    dependsOn(tasks.named("shadowJar"))
}
