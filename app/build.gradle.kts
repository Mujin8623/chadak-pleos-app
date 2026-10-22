import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val versionProps = Properties().apply {
    rootProject.file("version.properties").inputStream().use { load(it) }
}

fun versionPart(name: String): Int = versionProps.getProperty(name).trim().toInt()

val versionMajor = versionPart("VERSION_MAJOR")
val versionMinor = versionPart("VERSION_MINOR")
val versionPatch = versionPart("VERSION_PATCH")

// Keep this formula identical to com.mujin.chadak.VersionCodes.
val chadakVersionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
val chadakVersionName = "$versionMajor.$versionMinor.$versionPatch"

fun gradleToken(name: String, default: String): String {
    val raw = (findProperty(name) as String?)?.trim().orEmpty()
    return raw.ifEmpty { default }
}

// Whitespace is dropped ("차 분석가" -> "차분석가"); characters invalid in file names become "_".
fun apkToken(raw: String): String =
    raw.replace(Regex("""\s+"""), "").replace(Regex("""[\\/:*?"<>|]"""), "_")

// Contest rule: apk/팀명_앱이름_release.apk -> 차분석가_차닥_release.apk
// (team "차 분석가" is written without the space). Kotlin sources are UTF-8.
val teamName = apkToken(gradleToken("teamName", "차분석가"))
val apkAppName = apkToken(gradleToken("apkAppName", "차닥"))

val keystorePath = System.getenv("KEYSTORE_PATH")?.trim().orEmpty()
val keystorePassword = System.getenv("KEYSTORE_PASSWORD")?.trim().orEmpty()
val releaseKeyAlias = System.getenv("KEY_ALIAS")?.trim().orEmpty()
val releaseKeyPassword = System.getenv("KEY_PASSWORD")?.trim().orEmpty()
val releaseKeystore = keystorePath.takeIf { it.isNotEmpty() }?.let { file(it) }
val hasReleaseSigning = releaseKeystore?.isFile == true &&
    keystorePassword.isNotEmpty() &&
    releaseKeyAlias.isNotEmpty() &&
    releaseKeyPassword.isNotEmpty()

android {
    namespace = "com.mujin.chadak"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.mujin.chadak"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = chadakVersionCode
        versionName = chadakVersionName
    }

    // android.car is provided by the Pleos / AAOS image. Gear and speed are read
    // from CarPropertyManager, not from the hidden Vehicle SDK Gear API.
    useLibrary("android.car")

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = releaseKeystore
                storePassword = keystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Secrets live in GitHub Actions, never in the repo. Without them the
            // release APK is still runnable because it is signed with the debug key.
            signingConfig = if (hasReleaseSigning) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    lint {
        abortOnError = true
        warningsAsErrors = false
    }

    testOptions {
        unitTests.isIncludeAndroidResources = false
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.material)

    // Verified against https://nexus-playground.pleos.ai/repository/maven-releases/
    // and the import pages / sources noted in docs/feasibility.md.
    implementation(libs.pleos.vehicle)
    implementation(libs.pleos.navi.helper)
    implementation(libs.pleos.text.to.speech)
    implementation(libs.pleos.speech.to.text)
    implementation(libs.pleos.fused.location)

    testImplementation(libs.junit)
}

tasks.register<Copy>("packageSubmissionApk") {
    group = "distribution"
    description = "Copy the runnable release APK to <teamName>_<apkAppName>_release.apk"
    dependsOn("assembleRelease")
    from(layout.buildDirectory.dir("outputs/apk/release"))
    include("*.apk")
    into(layout.buildDirectory.dir("submission"))
    rename { "${teamName}_${apkAppName}_release.apk" }
}
