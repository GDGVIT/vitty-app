package com.dscvit.vitty.util

import android.content.Context

object ArraySaverLoader {

    fun loadArray(arrayName: String, context: Context): Array<String?> {
        val prefs = context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE)
        val size = prefs.getInt(arrayName + "_size", 0)
        val array = arrayOfNulls<String>(size)
        for (i in 0 until size) array[i] = prefs.getString(arrayName + "_" + i, null)
        return array
    }

    fun saveArray(array: ArrayList<String>, arrayName: String, context: Context): Boolean {
        val prefs = context.getSharedPreferences(Constants.USER_INFO, 0)
        val editor = prefs.edit()
        editor.putInt(arrayName + "_size", array.size)
        for (i in array.indices) editor.putString(arrayName + "_" + i, array[i])
        return editor.commit()
    }
}
