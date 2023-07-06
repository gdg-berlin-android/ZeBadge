package de.berlindroid.zeapp.zemodels

/**
 * Slot a page can reside in.
 *
 */
sealed class ZeSlot(val name: String) {
    object Name : ZeSlot("A")
    object FirstSponsor : ZeSlot("B")
    object SecondSponsor : ZeSlot("C")
    object FirstCustom : ZeSlot("Up")
    object SecondCustom : ZeSlot("Down")
    object ThirdCustom : ZeSlot("R")
    object QRCode : ZeSlot("Q")
}
