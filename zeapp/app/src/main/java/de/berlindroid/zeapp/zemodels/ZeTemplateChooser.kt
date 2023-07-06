package de.berlindroid.zeapp.zemodels

/**
 * State of which configurations can be applied to the selected slot.
 *
 * Used for creating a chooser in the ui, to select which editor should be used next.
 *
 * @param slot stores which slot the to be selected configuration should be applied to.
 * @param configurations a list of valid configuration of this slot.
 */
data class ZeTemplateChooser(
    val slot: ZeSlot,
    val configurations: List<ZeConfiguration>,
)
