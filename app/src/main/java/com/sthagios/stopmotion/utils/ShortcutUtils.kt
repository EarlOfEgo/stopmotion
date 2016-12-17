package com.sthagios.stopmotion.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.sthagios.stopmotion.image.database.Gif
import com.sthagios.stopmotion.show.ShowGifActivity
import java.util.*


/**
 * Created by stephan on 17/12/2016.
 */

inline fun Context.addShortcut(gif: Gif) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {

        val thumbUri = Uri.parse(gif.thumbnailUriString)
        val iconBitmap = MediaStore.Images.Media.getBitmap(contentResolver, thumbUri)

        val intent = Intent(this, ShowGifActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra("long_param", gif.id)

        val shortcutManager = getSystemService(ShortcutManager::class.java)
        val shortcut = ShortcutInfo.Builder(this, "id1")
                .setShortLabel("Share Gif")
                .setLongLabel("Share ${gif.name}")
                .setIcon(Icon.createWithBitmap(iconBitmap))
                .setIntent(intent)
                .build()
        shortcutManager.removeAllDynamicShortcuts()
        shortcutManager.dynamicShortcuts = Arrays.asList(shortcut)
    }
}