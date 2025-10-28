import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("com.google.gms.google-services")
}

val secrets = Properties()
val secretsFile = rootProject.file("secrets.properties")
if (secretsFile.exists()) {
    secrets.load(FileInputStream(secretsFile))
}

android {



    namespace = "com.cibertec.proyectodami"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.cibertec.proyectodami"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "DEV_BASE_URL", "\"${secrets.getProperty("TOOLIFY_DEV_BASE_URL", "http://10.0.2.2:8080/")}\"")
        buildConfigField("String", "PROD_BASE_URL", "\"${secrets.getProperty("TOOLIFY_PROD_BASE_URL", "https://api.toolify.com/")}\"")
        buildConfigField("String", "WEBSOCKET_URL", "\"${secrets.getProperty("TOOLIFY_WEBSOCKET_URL", "ws://api.toolify.com/ws/")}\"")

        resValue("string", "google_maps_key", secrets.getProperty("GOOGLE_MAPS_API_KEY", "YOUR_KEY_HERE"))
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {

        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes.add("META-INF/DEPENDENCIES")
            excludes.add("META-INF/INDEX.LIST")
            excludes.add("META-INF/NOTICE")
            excludes.add("META-INF/LICENSE")
            excludes.add("META-INF/LICENSE.txt")
            excludes.add("META-INF/NOTICE.txt")
            excludes.add("META-INF/ASL2.0")
            excludes.add("META-INF/io.netty.versions.properties")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.mlkit.barcode)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.glide)
    implementation(libs.firebase.appdistribution.gradle)
    kapt(libs.glide.compiler)
    implementation(libs.zxing.core)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.auth.ktx)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


}