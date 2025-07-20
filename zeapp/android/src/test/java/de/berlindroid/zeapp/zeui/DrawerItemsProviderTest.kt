package de.berlindroid.zeapp.zeui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.zeui.zehome.DrawerItemId
import de.berlindroid.zeapp.zeui.zehome.DrawerItemType
import de.berlindroid.zeapp.zeui.zehome.DrawerItemsProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class DrawerItemsProviderTest {
    @Test
    fun `getDrawerItems returns correct number of items`() {
        // When
        val items = DrawerItemsProvider.getDrawerItems()

        // Then
        assertEquals(14, items.size) // 9 navigation items + 5 dividers/spacers
    }

    @Test
    fun `getDrawerItems contains all expected navigation items`() {
        // When
        val items = DrawerItemsProvider.getDrawerItems()
        val navigationItems = items.filter { it.type == DrawerItemType.NAVIGATION }

        // Then
        assertEquals(9, navigationItems.size)

        val expectedIds =
            setOf(
                DrawerItemId.ZEPASS_CHAT,
                DrawerItemId.ALTER_EGOS,
                DrawerItemId.SAVE_ALL,
                DrawerItemId.UPDATE_CONFIG,
                DrawerItemId.SEND_RANDOM,
                DrawerItemId.SETTINGS,
                DrawerItemId.LANGUAGE_SETTINGS,
                DrawerItemId.CONTRIBUTORS,
                DrawerItemId.OPEN_SOURCE,
            )

        val actualIds = navigationItems.mapNotNull { it.id }.toSet()
        assertEquals(expectedIds, actualIds)
    }

    @Test
    fun `zepass chat item has correct properties`() {
        // When
        val items = DrawerItemsProvider.getDrawerItems()
        val zepassItem = items.find { it.id == DrawerItemId.ZEPASS_CHAT }

        // Then
        assertNotNull(zepassItem)
        assertEquals(DrawerItemType.NAVIGATION, zepassItem!!.type)
        assertEquals(R.string.open_zepass_chat, zepassItem.titleRes)
        assertEquals(Icons.Default.Person, zepassItem.vector)
        assertNull(zepassItem.painter)
    }

    @Test
    fun `contributors item has correct properties`() {
        // When
        val items = DrawerItemsProvider.getDrawerItems()
        val contributorsItem = items.find { it.id == DrawerItemId.CONTRIBUTORS }

        // Then
        assertNotNull(contributorsItem)
        assertEquals(DrawerItemType.NAVIGATION, contributorsItem!!.type)
        assertEquals(R.string.ze_navdrawer_contributors, contributorsItem.titleRes)
        assertEquals(Icons.Default.Info, contributorsItem.vector)
        assertNull(contributorsItem.painter)
    }

    @Test
    fun `settings item has correct properties`() {
        // When
        val items = DrawerItemsProvider.getDrawerItems()
        val settingsItem = items.find { it.id == DrawerItemId.SETTINGS }

        // Then
        assertNotNull(settingsItem)
        assertEquals(DrawerItemType.NAVIGATION, settingsItem!!.type)
        assertEquals(R.string.ze_navdrawer_settings, settingsItem.titleRes)
        assertNull(settingsItem.vector)
        assertEquals(R.drawable.ic_settings, settingsItem.painter)
    }

    @Test
    fun `divider and space items have no ids`() {
        // When
        val items = DrawerItemsProvider.getDrawerItems()
        val nonNavigationItems = items.filter { it.type != DrawerItemType.NAVIGATION }

        // Then
        assertEquals(5, nonNavigationItems.size) // 4 dividers + 1 space
        nonNavigationItems.forEach { item ->
            assertNull("Non-navigation items should have null id", item.id)
            assertEquals(0, item.titleRes)
            assertNull(item.vector)
            assertNull(item.painter)
        }
    }

    @Test
    fun `drawer items are in correct order`() {
        // When
        val items = DrawerItemsProvider.getDrawerItems()
        val navigationItems = items.filter { it.type == DrawerItemType.NAVIGATION }

        // Then - Check first few items are in expected order
        assertEquals(DrawerItemId.ZEPASS_CHAT, navigationItems[0].id)
        assertEquals(DrawerItemId.ALTER_EGOS, navigationItems[1].id)
        assertEquals(DrawerItemId.SAVE_ALL, navigationItems[2].id)
        assertEquals(DrawerItemId.UPDATE_CONFIG, navigationItems[3].id)
        assertEquals(DrawerItemId.SEND_RANDOM, navigationItems[4].id)
    }
}
