package com.sthagios.stopmotion.image.database

import android.app.Activity
import io.realm.Realm
import io.realm.RealmConfiguration

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   05.06.16
 */
class DatabaseHelper {

    public fun storeGif(gifName: String, gifPath: String) {

    }

}

fun Activity.getRealmInstance(): Realm {

    // Create configuration and reset Realm.
    val realmConfig = RealmConfiguration.Builder(this).build()
//    Realm.deleteRealm(realmConfig)

    // Open the realm for the UI thread.
    val realm = Realm.getInstance(realmConfig)
    return realm
}