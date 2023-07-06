package de.berlindroid.zeapp.zemodels

/**
 * State of the current editor
 *
 * Use to display an editor to update the current slot's configuration.
 *
 * @param slot the slot of the badge the current editor works on
 * @param config the initial configuration of slot to be worked on by the editor.
 */
data class ZeEditor(
    val slot: ZeSlot,
    val config: ZeConfiguration
)
