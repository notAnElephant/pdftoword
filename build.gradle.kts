import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose") 
}


group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "pdftoword2"
            packageVersion = "1.0.0"
        }
    }
    dependencies{
        implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0") 
        implementation("co.touchlab:kermit:2.0.4")
        implementation("org.apache.pdfbox:pdfbox:2.0.27")
        implementation("com.darkrockstudios:mpfilepicker:3.1.0")
        implementation("org.docx4j:docx4j-JAXB-MOXy:11.5.0")
        implementation(compose.materialIconsExtended)
        implementation("org.jetbrains.compose.material:material-icons-extended:1.6.8")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.6.4")
    }
}


