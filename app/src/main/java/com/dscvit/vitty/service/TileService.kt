package com.dscvit.vitty.service

import android.content.SharedPreferences
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.dscvit.vitty.notif.NotificationHelper
import com.dscvit.vitty.util.Constants

class TileService : TileService() {
    private lateinit var prefs: SharedPreferences

    override fun onClick() {
        super.onClick()
        changeExamMode()
        checkExamMode()
        // Called when the user click the tile
    }

    override fun onTileAdded() {
        super.onTileAdded()
        checkExamMode()
    }

    private fun checkExamMode() {
        prefs = getSharedPreferences(Constants.USER_INFO, 0)
        if (prefs.getBoolean(Constants.EXAM_MODE, false)) {
            qsTile.state = Tile.STATE_ACTIVE
        } else {
            qsTile.state = Tile.STATE_INACTIVE
        }
        qsTile.updateTile()
    }

    private fun changeExamMode() {
        prefs = getSharedPreferences(Constants.USER_INFO, 0)
        val current = !prefs.getBoolean(Constants.EXAM_MODE, false)
        prefs.edit().putBoolean(Constants.EXAM_MODE, current)
            .apply()
        if (current) {
            NotificationHelper.cancelAlarm(this)
        } else {
            NotificationHelper.setAlarm(this)
        }
    }
}