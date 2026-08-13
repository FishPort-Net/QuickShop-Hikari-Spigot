import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.ghostchu.quickshop.buildlogic.QuickShopRelocations
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

    QuickShopRelocations.all.forEach { relocation ->
        relocate(relocation.source, relocation.target)
    }

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
