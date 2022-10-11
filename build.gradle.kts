plugins {
    java
}

allprojects {
    apply(plugin = "java")

    java.toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
