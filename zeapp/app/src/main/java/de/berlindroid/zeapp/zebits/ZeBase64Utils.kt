package de.berlindroid.zeapp.zebits

import java.util.Base64

/**
 * Helper to convert a bytearray to base64
 */
fun ByteArray.base64(): String = Base64.getEncoder().encodeToString(this)

/**
 * Take a base64 encoded string and convert it back to å bytearray.
 */
fun String.debase64(): ByteArray = Base64.getDecoder().decode(this)
