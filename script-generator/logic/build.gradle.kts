import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jmailen.gradle.kotlinter.tasks.ConfigurableKtLintTask

plugins {
    buildsrc.convention.`kotlin-jvm`

    kotlin("plugin.serialization")
}

dependencies {
    implementation(projects.automation.typings)
    implementation(projects.automation.wrapperGenerator)
    implementation(projects.library)
    implementation("com.charleskorn.kaml:kaml:0.53.0")
    implementation("com.squareup:kotlinpoet:1.13.0")
    implementation(kotlin("reflect"))
}

fun ConfigurableKtLintTask.kotlinterConfig() {
    exclude("**/generated/**", "**/actual/**")
}

tasks.lintKotlinTest {
    kotlinterConfig()
}
tasks.formatKotlinTest {
    kotlinterConfig()
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=io.github.typesafegithub.workflows.internal.InternalGithubActionsApi",
        )
    }
}
