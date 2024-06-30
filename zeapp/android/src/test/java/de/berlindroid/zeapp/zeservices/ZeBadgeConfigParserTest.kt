package de.berlindroid.zeapp.zeservices

import android.util.Base64
import assertk.assertFailure
import assertk.assertions.messageContains
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

class ZeBadgeConfigParserTest {
    private val parser = ZeBadgeConfigParser()

    @Before
    fun setup() {
        mockkStatic(Base64::class)
        every { Base64.decode(any<String>(), any<Int>()) } returns byteArrayOf(1, 2, 3, 4)
    }

    @Test
    fun `parse valid config string`() {
        val configString =
            "wifi_attached=False user.uuid=4d3f6ca7-d256-4f84-a6c6-099a26055d4c " +
            "user.description=Edward\$SPACE#Bernard,\$SPACE#a\$SPACE#veteran " +
            "user.name=Edward\$SPACE#Bernard developer_mode=True " +
            "user.profileB64=eNpjYGBgUJnkqaIg6MDAAmTX/+U+WGd//399OwNjYfv/gk1AQ== " +
            "user.chatPhrase=CodeMonster2024"

        val result = parser.parse(configString)

        assertFalse(result.isWiFiAttached)
        assertTrue(result.isDeveloperMode)
        assertNotNull(result.userInfo)
        assertEquals(UUID.fromString("4d3f6ca7-d256-4f84-a6c6-099a26055d4c"), result.userInfo?.id)
        assertEquals("Edward Bernard", result.userInfo?.name)
        assertEquals("Edward Bernard, a veteran", result.userInfo?.description)
        assertEquals("CodeMonster2024", result.userInfo?.chatPhrase)
        assertNotNull(result.userInfo?.profilePhoto)
        assertArrayEquals(byteArrayOf(1, 2, 3, 4), result.userInfo?.profilePhoto)
    }

    @Test
    fun `parse config string with missing user info`() {
        val configString = "wifi_attached=True developer_mode=False"

        val result = parser.parse(configString)

        assertNull(result.userInfo)
        assertTrue(result.isWiFiAttached)
        assertFalse(result.isDeveloperMode)
    }

    @Test
    fun `parse config string with partial user info`() {
        val configString =
            "user.uuid=4d3f6ca7-d256-4f84-a6c6-099a26055d4c " +
                "user.name=John\$SPACE#Doe wifi_attached=True"

        val result = parser.parse(configString)

        assertNull(result.userInfo)
        assertTrue(result.isWiFiAttached)
        assertFalse(result.isDeveloperMode)
    }

    @Test
    fun `parse config string with invalid UUID`() {
        val configString =
            "user.uuid=invalid-uuid user.name=John\$SPACE#Doe " +
                "user.description=Test user.profileB64=eNpjYGBg"

        assertFailure { parser.parse(configString) }
            .messageContains("invalid-uuid")
    }

    @Test
    fun `parse config string with multiple spaces between key-value pairs`() {
        val configString =
            "wifi_attached=True     developer_mode=True    " +
                "user.uuid=4d3f6ca7-d256-4f84-a6c6-099a26055d4c"

        val result = parser.parse(configString)

        assertNull(result.userInfo)
        assertTrue(result.isWiFiAttached)
        assertTrue(result.isDeveloperMode)
    }
}
