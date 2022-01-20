package com.dscvit.vitty.util

import android.content.Context
import com.dscvit.vitty.R
import kotlin.random.Random

object Quote {
    fun getLine(context: Context): String {
        val quoteData =
            context.resources.getStringArray(
                if (!RemoteConfigUtils.getOnlineMode()) R.array.no_classes_today_subtext_list
                else R.array.no_classes_today_subtext_list_online
            )
        val randomIndex = Random.nextInt(quoteData.size)
        return quoteData[randomIndex]
    }
}
