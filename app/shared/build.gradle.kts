import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidLibrary {
        namespace = "rs.moma.therminator.app"
        compileSdk = 36

        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }

        androidResources {
            enable = true
        }
    }

    js {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    dependencies {
        implementation(libs.androidx.lifecycle.viewmodelCompose)
        implementation(libs.androidx.lifecycle.runtimeCompose)
        implementation(libs.ktor.client.content.negotiation)
        implementation(libs.compose.components.resources)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.compose.uiToolingPreview)
        implementation(libs.ktor.serialization.json)
        implementation(libs.navigation.compose)
        implementation(libs.compose.foundation)
        implementation(libs.compose.material3)
        implementation(libs.ktor.client.core)
        implementation(libs.compose.runtime)
        implementation(libs.koin.compose)
        implementation(libs.compose.ui)
        implementation(libs.koin.core)
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

