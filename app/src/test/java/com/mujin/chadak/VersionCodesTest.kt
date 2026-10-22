package com.mujin.chadak

import java.io.File
import java.util.Properties
import org.junit.Assert.assertEquals
import org.junit.Test

class VersionCodesTest {
    @Test
    fun formulaMatchesTheSkeletonExamples() {
        assertEquals(100, VersionCodes.from(0, 1, 0))
        assertEquals(10203, VersionCodes.from(1, 2, 3))
        assertEquals(10000, VersionCodes.from(1, 0, 0))
    }

    @Test
    fun gradleVersionMatchesThePropertiesFile() {
        val file = listOf(File("../version.properties"), File("version.properties")).first { it.isFile }
        val props = Properties().apply { file.inputStream().use { load(it) } }
        val major = props.getProperty("VERSION_MAJOR").trim().toInt()
        val minor = props.getProperty("VERSION_MINOR").trim().toInt()
        val patch = props.getProperty("VERSION_PATCH").trim().toInt()
        assertEquals(VersionCodes.from(major, minor, patch), BuildConfig.VERSION_CODE)
        assertEquals("$major.$minor.$patch", BuildConfig.VERSION_NAME)
    }
}
