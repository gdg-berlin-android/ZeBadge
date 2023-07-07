package de.berlindroid.zeapp.zebits

internal data class ZeVcardStringBuilder(
    var formattedName: String? = null,
    var title: String? = null,
    var phone: String? = null,
    var email: String? = null,
    var url: String? =  null,

) {
    fun buildString(): String = listOfNotNull(
        "BEGIN:VCARD",
        "VERSION:2.1",
        if (formattedName.isNullOrEmpty()) null else "FN:$formattedName\n",
        if (title.isNullOrEmpty()) null else "TITLE:$title\n",
        if (phone.isNullOrEmpty()) null else "TEL:$phone\n",
        if (email.isNullOrEmpty()) null else "EMAIL:$email\n",
        if (url.isNullOrEmpty()) null else "URL:$url\n",
        "END:VCARD"
    ).joinToString("\n")
}