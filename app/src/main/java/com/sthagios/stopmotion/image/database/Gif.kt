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
        //Name of the gif
        open var name: String = "Stopmotion Gif",
        //Times shared
        open var sharedAmount: Int = 0,
        //Gif file name
        open var fileName: String = "",
        //Gif file uri as string
        open var fileUriString: String = "",
        //Gif share uri as string
        open var shareUriString: String = "",
        //Thumbnail file uri as string
        open var thumbnailUriString: String = ""
) : RealmObject()