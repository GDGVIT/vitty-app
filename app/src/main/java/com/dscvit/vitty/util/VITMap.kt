package com.dscvit.vitty.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object VITMap {
    private const val VIT = "Vellore+Institute+of+Technology,+Vellore+India" // done
    val blocks: HashMap<String, String> = HashMap()

    init {
        blocks["CDMM"] = "Centre+For+Disaster+Mitigation+And+Management,+$VIT"
        blocks["SJT"] = "SJT+Building+%2F+Silver+Jubilee+Towers,+$VIT"
        blocks["MB"] = "MB+-+Main+Building,+$VIT"
        blocks["MGB"] = "Mahatma+Gandhi+Block,+VIT+University,+$VIT"
        blocks["TT"] = "Technology+Tower+-+TT,+$VIT"
        blocks["SMV"] = "SMV,+$VIT"
        blocks["PRP"] = "12.971272,79.166357"
//        blocks["PLB"] = ""
        blocks["CBMR"] = "CBMR+block,+$VIT"
        blocks["GDN"] = "GDN,+$VIT"
    }

    fun openClassMap(context: Context, classId: String) {
        try {
            var classLocation = ""
            var classBlock = ""
            blocks.forEach { (key, value) ->
                if (classId.startsWith(key)) {
                    classLocation = value
                    classBlock = key
                }
            }
            if (classLocation != "") {
                val gmmIntentUri = Uri.parse("google.navigation:q=$classLocation&mode=w")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                try {
                    Analytics.navigation(classBlock)
                } catch (e: Exception) {
                }
                mapIntent.resolveActivity(context.packageManager)?.let {
                    context.startActivity(mapIntent)
                }
                Toast.makeText(context, "Taking you to $classBlock Block", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(context, "Block not found!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Map application not found!", Toast.LENGTH_SHORT).show()
        }
    }
}
