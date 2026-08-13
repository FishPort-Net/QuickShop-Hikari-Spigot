package com.ghostchu.quickshop.buildlogic

data class PackageRelocation(val source: String, val target: String)

object QuickShopRelocations {

    val all = listOf(
        PackageRelocation("net.tnemc", "com.ghostchu.quickshop.shade.tne"),
        PackageRelocation("com.alessiodp.libby", "com.ghostchu.quickshop.shade.com.alessiodp.libby"),
        PackageRelocation("cc.carm.lib", "com.ghostchu.quickshop.shade.cc.carm.lib"),
        PackageRelocation("com.zaxxer.hikari", "com.ghostchu.quickshop.shade.com.zaxxer.hikari"),
        PackageRelocation("com.tcoded", "com.ghostchu.quickshop.shade.com.tcoded"),
        PackageRelocation("io.papermc.lib", "com.ghostchu.quickshop.shade.io.papermc.lib"),
        PackageRelocation("de.tr7zw.changeme.nbtapi", "com.ghostchu.quickshop.shade.de.tr7zw.changeme.nbtapi"),
        PackageRelocation("org.bstats", "com.ghostchu.quickshop.shade.org.bstats"),
        PackageRelocation("de.themoep.minedown", "com.ghostchu.quickshop.shade.de.themoep.minedown"),
        PackageRelocation("net.kyori", "com.ghostchu.quickshop.shade.net.kyori"),
        PackageRelocation("org.apache.commons", "com.ghostchu.quickshop.shade.org.apache.commons"),
        PackageRelocation("org.apache.http", "com.ghostchu.quickshop.shade.org.apache.http"),
        PackageRelocation("org.apache.hc", "com.ghostchu.quickshop.shade.org.apache.hc"),
        PackageRelocation("org.slf4j", "com.ghostchu.quickshop.shade.org.slf4j"),
        PackageRelocation("com.google.gson", "com.ghostchu.quickshop.shade.com.google.gson"),
        PackageRelocation("com.google.common", "com.ghostchu.quickshop.shade.com.google.common"),
        PackageRelocation("com.google.thirdparty", "com.ghostchu.quickshop.shade.com.google.thirdparty"),
        PackageRelocation("com.google.protobuf", "com.ghostchu.quickshop.shade.com.google.protobuf"),
        PackageRelocation("com.fasterxml.jackson", "com.ghostchu.quickshop.shade.com.fasterxml.jackson"),
        PackageRelocation("com.rollbar", "com.ghostchu.quickshop.shade.com.rollbar"),
        PackageRelocation("org.h2", "com.ghostchu.quickshop.shade.org.h2"),
        PackageRelocation("kong.unirest", "com.ghostchu.quickshop.shade.kong.unirest"),
        PackageRelocation("org.relique.jdbc", "com.ghostchu.quickshop.shade.org.relique.jdbc"),
        PackageRelocation("org.dom4j", "com.ghostchu.quickshop.shade.org.dom4j"),
        PackageRelocation("com.vdurmont.semver4j", "com.ghostchu.quickshop.shade.com.vdurmont.semver4j"),
        PackageRelocation("com.ghostchu.crowdin", "com.ghostchu.quickshop.shade.com.ghostchu.crowdin"),
        PackageRelocation("com.ghostchu.simplereloadlib", "com.ghostchu.quickshop.shade.com.ghostchu.simplereloadlib"),
        PackageRelocation("com.mysql", "com.ghostchu.quickshop.shade.com.mysql"),
        PackageRelocation("com.mohistmc", "com.ghostchu.quickshop.shade.com.mohistmc"),
        PackageRelocation("com.sun.msv", "com.ghostchu.quickshop.shade.com.sun.msv"),
        PackageRelocation("com.sun.xml", "com.ghostchu.quickshop.shade.com.sun.xml"),
        PackageRelocation("io.vertx", "com.ghostchu.quickshop.shade.io.vertx"),
        PackageRelocation("org.jaxen", "com.ghostchu.quickshop.shade.org.jaxen"),
        PackageRelocation("org.xmlpull", "com.ghostchu.quickshop.shade.org.xmlpull"),
        PackageRelocation("org.gjt", "com.ghostchu.quickshop.shade.org.gjt"),
        PackageRelocation("org.relaxng", "com.ghostchu.quickshop.shade.org.relaxng"),
        PackageRelocation("org.json.simple", "com.ghostchu.quickshop.shade.org.json.simple"),
        PackageRelocation("org.relique", "com.ghostchu.quickshop.shade.org.relique"),
        PackageRelocation("javax.xml.bind", "com.ghostchu.quickshop.shade.javax.xml.bind"),
        PackageRelocation("unirest.shaded", "com.ghostchu.quickshop.shade.unirest.shaded")
    )
}
