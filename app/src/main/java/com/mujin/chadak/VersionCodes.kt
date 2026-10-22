package com.mujin.chadak

/**
 * versionCode = major * 10000 + minor * 100 + patch.
 * The same formula is applied in app/build.gradle.kts. Minor and patch stay in 0..99.
 */
object VersionCodes {
    fun from(major: Int, minor: Int, patch: Int): Int {
        require(major >= 0) { "major must be >= 0" }
        require(minor in 0..99) { "minor must be 0..99" }
        require(patch in 0..99) { "patch must be 0..99" }
        return major * 10000 + minor * 100 + patch
    }
}
