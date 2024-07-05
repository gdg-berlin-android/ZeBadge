package de.berlindroid.zeapp.zemodels

/**
 * Slot a page can reside in.
 *
 */
sealed class ZeSlot(val name: String) {
    data object Name : ZeSlot("A")

    data object FirstSponsor : ZeSlot("B")

    data object FirstCustom : ZeSlot("Up")

    data object SecondCustom : ZeSlot("Down")

    data object QRCode : ZeSlot("Q")

    data object Weather : ZeSlot("Wa")

    data object Quote : ZeSlot("Quite")

    data object BarCode : ZeSlot("Ba")

    data object Add : ZeSlot("Add")

    data object Camera : ZeSlot("Camera")
}
