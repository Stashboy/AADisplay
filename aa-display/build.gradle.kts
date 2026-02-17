plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    //kotlin("android")
    id("kotlin-android")
    id("dev.rikka.tools.refine") version "4.4.0"
}

android {
    val buildTime = System.currentTimeMillis()
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.nitsuya.aa.display"
        minSdk = 31
        targetSdk = 36
        versionCode = 3000  // 设置一个很大的版本号，避免 LSPosed 提示更新
        versionName = "0.22#16.1+portrait"
        buildConfigField("long", "BUILD_TIME", buildTime.toString())
    }

    packaging {
        resources.excludes.addAll(
            arrayOf(
                "META-INF/**",
                "kotlin/**"
            )
        )
    }
    signingConfigs {
        create("release") {
            storeFile = file("../key.jks")
            storePassword = System.getenv("KEY_ANDROID")
            keyAlias = "key0"
            keyPassword = System.getenv("KEY_ANDROID")
            enableV1Signing = false
            enableV2Signing = false
            enableV3Signing = true
            enableV4Signing = true
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            sourceSets.getByName("main").java.srcDir(File("build/generated/ksp/release/kotlin"))
        }
        getByName("debug") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // 如果环境变量存在且签名配置有效，使用 release 签名，否则使用默认调试签名
            if (System.getenv("KEY_ANDROID") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
        }

    }
    
    applicationVariants.all {
        val variant = this
        variant.outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val versionName = variant.versionName.replace("#", "-")
            output.outputFileName = "aa-display-${versionName}.apk"
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        languageVersion = "2.0"
    }
    buildFeatures {
        viewBinding = true
        aidl = true
    }
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    androidResources.additionalParameters += mutableListOf("--allow-reserved-package-id", "--package-id", "0x64")

    namespace = "io.github.nitsuya.aa.display"
    buildToolsVersion = "35.0.0"
}

configurations.all {
    exclude("androidx.appcompat", "appcompat")
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-ktx:1.11.0")
    implementation("androidx.fragment:fragment-ktx:1.8.9")
//    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.media:media:1.7.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("dev.rikka.rikkax.appcompat:appcompat:1.6.1")
    implementation("dev.rikka.rikkax.material:material-preference:2.0.0")

    //kotlinx-coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    //ViewBindingUtil
    implementation("com.github.matsudamper:ViewBindingUtil:0.1")

    compileOnly(project(":lib-stub"))
    implementation("dev.rikka.tools.refine:runtime:4.4.0")
    implementation("dev.rikka.hidden:compat:4.4.0")
    compileOnly("dev.rikka.hidden:stub:4.4.0")
    compileOnly(files("./libs/de.robv.android.xposed_api_82.jar"))
    implementation("com.github.kyuubiran:EzXHelper:1.0.3")
    implementation("com.github.topjohnwu.libsu:core:5.2.0")
    implementation("org.luckypray:dexkit:2.0.0-rc3")
//    implementation("com.github.martoreto:aauto-sdk:v4.7")
    implementation(files("./libs/aauto.aar"))

    //lifecycle
    val lifecycleVersion = "2.9.3"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

}