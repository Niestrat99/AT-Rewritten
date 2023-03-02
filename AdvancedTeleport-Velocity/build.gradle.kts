
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("java-library")
    id("maven-publish")
    alias(libMinix.plugins.shadow)
    alias(libMinix.plugins.slimjar)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(libs.velocity)
    annotationsProcessor(libs.velocity)

    slim(libMinix.cloud.core)
    slim(libMinix.cloud.minecraft.velocity)
}
