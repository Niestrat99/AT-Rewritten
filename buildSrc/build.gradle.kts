plugins {
    id("java")
    `kotlin-dsl`
}

group = "io.github.niestrat99"
version = "6.2.0"

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("de.eldoria:plugin-yml:0.9.0")
    implementation("de.crazydev22.slimjar:gradle-plugin:2.1.9")
    implementation("com.gradleup.shadow:shadow-gradle-plugin:9.5.1")
}