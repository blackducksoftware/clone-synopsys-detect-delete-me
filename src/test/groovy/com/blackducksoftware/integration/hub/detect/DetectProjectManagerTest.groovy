package com.blackducksoftware.integration.hub.detect

import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.junit.Assert
import org.junit.Test

import com.blackducksoftware.integration.hub.detect.model.BomToolType
import com.blackducksoftware.integration.util.IntegrationEscapeUtil

public class DetectProjectManagerTest {
    @Test
    void testGettingDefaultProjectVersions() {
        def detectConfiguration = new DetectConfiguration()
        detectConfiguration.projectVersionName = null
        detectConfiguration.defaultProjectVersionScheme = 'timestamp'
        detectConfiguration.defaultProjectVersionTimeformat = 'yyyy-MM-dd\'T\'HH:mm:ss.SSS'
        detectConfiguration.defaultProjectVersionText = 'default'

        def detectProjectManager = new DetectProjectManager()
        detectProjectManager.detectConfiguration = detectConfiguration

        String timeString = DateTimeFormat.forPattern('yyyy-MM-dd\'T\'HH:mm:ss.SSS').withZoneUTC().print(DateTime.now().withZone(DateTimeZone.UTC))
        String defaultTimestampVersion = detectProjectManager.getProjectVersionName(null)
        Assert.assertTrue(defaultTimestampVersion.startsWith(timeString[0..10]))

        detectConfiguration.defaultProjectVersionScheme = 'text'
        String defaultTextVersion = detectProjectManager.getProjectVersionName(detectConfiguration.defaultProjectVersionText)
        Assert.assertEquals('default', defaultTextVersion)

        detectConfiguration.projectVersionName = 'actual'
        String version = detectProjectManager.getProjectVersionName(detectConfiguration.defaultProjectVersionText)
        Assert.assertEquals('actual', version)
    }

    @Test
    void testProjectFileName() {
        def detectProjectManager = new DetectProjectManager()
        detectProjectManager.integrationEscapeUtil = new IntegrationEscapeUtil()
        String actual = detectProjectManager.createBdioFilename(BomToolType.NPM, "test", "short", "name")
        String expected = "NPM_short_name_test_bdio.jsonld"
        Assert.assertEquals(expected, actual)
    }

    @Test
    void testProjectLongFileName() {
        def detectProjectManager = new DetectProjectManager()
        detectProjectManager.integrationEscapeUtil = new IntegrationEscapeUtil()
        String longPath = StringUtils.repeat('a', 250)
        String longVersion = StringUtils.repeat('b', 250)
        String longName = StringUtils.repeat('c', 250)
        String actual = detectProjectManager.createBdioFilename(BomToolType.NPM, longPath, longName, longVersion)
        String expected = "NPM_ec4c72e67030b52_bc7ea19d315b641_b5d5e3e0fcccfb4_bdio.jsonld"
        Assert.assertEquals(expected, actual)
    }
}
