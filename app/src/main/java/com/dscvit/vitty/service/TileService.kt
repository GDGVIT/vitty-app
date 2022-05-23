package com.dscvit.vitty.service

import android.content.SharedPreferences
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.dscvit.vitty.util.Constants
import com.dscvit.vitty.util.NotificationHelper
import com.dscvit.vitty.util.UtilFunctions.reloadWidgets

class TileService : TileService() {
    private lateinit var prefs: SharedPreferences

    override fun onClick() {
        super.onClick()
        changeExamMode()
        checkExamMode()
    }

    override fun onTileAdded() {
        super.onTileAdded()
        checkExamMode()
    }

    override fun onStartListening() {
        super.onStartListening()
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
        reloadWidgets(this)
    }
}
