package de.berlindroid.zeapp.zebits

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test

class ZeVcardStringBuilderTest {
    @Test
    fun `buildString should return a vcard string with all fields`() {
        val zeVcardStringBuilder =
            ZeVcardStringBuilder(
                formattedName = "John Doe",
                title = "Software Engineer",
                phone = "+49123456789",
                email = "zemail@berlindroid.de",
                url = "https://berlindroid.de",
            )
        val vcardString = zeVcardStringBuilder.buildString()
        val expectedVcardString =
            """
            BEGIN:VCARD
            VERSION:2.1
            FN:John Doe
            TITLE:Software Engineer
            TEL:+49123456789
            EMAIL:zemail@berlindroid.de
            URL:https://berlindroid.de
            END:VCARD
            """.trimIndent()

        assertThat(vcardString).isEqualTo(expectedVcardString)
    }

    @Test
    fun `buildString should return a vcard string with only required fields`() {
        val zeVcardStringBuilder =
            ZeVcardStringBuilder(
                formattedName = "John Doe",
            )
        val vcardString = zeVcardStringBuilder.buildString()
        val expectedVcardString =
            """
            BEGIN:VCARD
            VERSION:2.1
            FN:John Doe
            END:VCARD
            """.trimIndent()

        assertThat(vcardString).isEqualTo(expectedVcardString)
    }

    @Test
    fun `buildString should return a vcard string with no optional fields`() {
        val zeVcardStringBuilder = ZeVcardStringBuilder()
        val vcardString = zeVcardStringBuilder.buildString()
        val expectedVcardString =
            """
            BEGIN:VCARD
            VERSION:2.1
            END:VCARD
            """.trimIndent()

        assertThat(vcardString).isEqualTo(expectedVcardString)
    }

    @Test
    fun `buildString should handle null values gracefully`() {
        val zeVcardStringBuilder =
            ZeVcardStringBuilder(
                formattedName = null,
                title = null,
                phone = null,
                email = null,
                url = null,
            )
        val vcardString = zeVcardStringBuilder.buildString()
        val expectedVcardString =
            """
            BEGIN:VCARD
            VERSION:2.1
            END:VCARD
            """.trimIndent()

        assertThat(vcardString).isEqualTo(expectedVcardString)
    }

    @Test
    fun `buildString should handle empty strings gracefully`() {
        val zeVcardStringBuilder =
            ZeVcardStringBuilder(
                formattedName = "",
                title = "",
                phone = "",
                email = "",
                url = "",
            )
        val vcardString = zeVcardStringBuilder.buildString()
        val expectedVcardString =
            """
            BEGIN:VCARD
            VERSION:2.1
            END:VCARD
            """.trimIndent()

        assertThat(vcardString).isEqualTo(expectedVcardString)
    }

    @Test
    fun `buildString should handle a mix of null, empty, and non-empty fields`() {
        val zeVcardStringBuilder =
            ZeVcardStringBuilder(
                formattedName = "John Doe",
                title = "",
                phone = null,
                email = "john.doe@example.com",
                url = "http://example.com",
            )
        val vcardString = zeVcardStringBuilder.buildString()
        val expectedVcardString =
            """
            BEGIN:VCARD
            VERSION:2.1
            FN:John Doe
            EMAIL:john.doe@example.com
            URL:http://example.com
            END:VCARD
            """.trimIndent()

        assertThat(vcardString).isEqualTo(expectedVcardString)
    }
}
