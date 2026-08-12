plugins {
    id("quickshop.core-conventions")
}

dependencies {
    api(project(":quickshop-common"))
    compileOnly(libs.spigot.api.legacy)
}

tasks.named<Javadoc>("javadoc") {
    (options as StandardJavadocDocletOptions).apply {
        windowTitle = "Quickshop - Hikari"
        addStringOption("Xdoclint:none", "-quiet")
        exclude(
            "**/com/ghostchu/quickshop/nonquickshopstuff/**",
            "**/com/ghostchu/quickshop/compatibility/**",
            "**/com/ghostchu/quickshop/addon/**",
        )
        author(true)
        version(true)
        linkSource(true)
        noTimestamp(true)
        bottom = "<b>QuickShopCommunity, 2026</b>"
    }
    isFailOnError = false
}
