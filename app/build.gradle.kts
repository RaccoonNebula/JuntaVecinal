import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    jacoco
}

val firma = Properties().apply {
    val archivo = rootProject.file("keystore.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}

android {
    namespace = "com.gestion.juntavecinos"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.gestion.juntavecinos"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Direccion del api-gateway. 10.0.2.2 es el computador visto desde el emulador.
        // Se puede cambiar con -PapiBaseUrl=http://localhost:9090/ (telefono con adb reverse)
        val apiBaseUrl = providers.gradleProperty("apiBaseUrl").getOrElse("http://10.0.2.2:9090/")
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
    }

    signingConfigs {
        create("release") {
            if (firma.isNotEmpty()) {
                storeFile = rootProject.file(firma.getProperty("storeFile"))
                storePassword = firma.getProperty("storePassword")
                keyAlias = firma.getProperty("keyAlias")
                keyPassword = firma.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            // Registra que lineas ejecutan las pruebas unitarias
            enableUnitTestCoverage = true
        }
        release {
            if (firma.isNotEmpty()) signingConfig = signingConfigs.getByName("release")
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    testOptions {
        // Las clases de Android entregan valores por defecto en vez de fallar en el PC
        unitTests { isReturnDefaultValues = true }
    }
    testCoverage {
        jacocoVersion = "0.8.14"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    //Dependencia de Room (Almacenamiento DB)
    implementation("androidx.room:room-runtime:2.8.5")
    implementation("androidx.room:room-ktx:2.8.5")
    ksp("androidx.room:room-compiler:2.8.5")

    //Dependencias de red: comunicacion con el backend
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    //Dependencia para guardar la sesion
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    //Dependencia para mostrar foto de comprobante en FinanzaScreen
    implementation("io.coil-kt:coil-compose:2.7.0")

    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    //Pruebas unitarias: JUnit 5, Kotest, MockK y corrutinas
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.mockk:mockk:1.14.6")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.13.4")

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

// Para usar JUnit 5 y Kotest
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

jacoco {
    toolVersion = "0.8.14"
}

// Informe de cobertura solo de la logica (sin pantallas ni clases que dependen de Android)
tasks.register<JacocoReport>("cobertura") {
    group = "verification"
    description = "Ejecuta las pruebas unitarias y genera el informe de cobertura de la logica"
    dependsOn("testDebugUnitTest")

    val logica = listOf(
        "logic/**", "model/Rol*", "model/EstadoReserva*", "utils/FechaUtils*", "ui/state/**",
        "data/remote/Dtos*", "data/remote/*Dto*", "data/remote/Resultado*", "data/remote/AuthInterceptor*",
        "data/repository/**", "viewmodel/**"
    ).map { "com/gestion/juntavecinos/$it" }

    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")) {
            include(logica)
        }
    )
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(fileTree(layout.buildDirectory) { include("outputs/unit_test_code_coverage/**/*.exec") })
    reports {
        html.required = true
        csv.required = true
        xml.required = false
    }
}
