package com.sthagios.stopmotion.image.database

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   05.06.16
 */
open class Gif(
        @PrimaryKey open var id: Long = 0,
        open var name: String = "Stopmotion Gif",
        open var sharedAmount: Int = 0,
        open var fileName: String = "",
        open var fileUriString: String = "",
        open var shareUriString: String = ""
) : RealmObject() {
}