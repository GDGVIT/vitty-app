package com.dscvit.vitty.util

import android.content.Context
import com.dscvit.vitty.R
import com.dscvit.vitty.util.Constants.DEFAULT_QUOTE
import kotlin.random.Random

object Quote {
    fun getLine(context: Context?): String {
        if (context != null) {
            val quoteData =
                context.resources.getStringArray(
                    if (!RemoteConfigUtils.getOnlineMode()) R.array.no_classes_today_subtext_list
                    else R.array.no_classes_today_subtext_list_online
                )
            val randomIndex = Random.nextInt(quoteData.size)
            return quoteData[randomIndex]
        }
        return DEFAULT_QUOTE
    }
}
