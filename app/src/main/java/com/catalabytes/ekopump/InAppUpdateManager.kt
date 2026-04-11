package com.catalabytes.ekopump

import android.app.Activity
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

object InAppUpdateManager {

    private const val TAG = "InAppUpdate"

    fun checkAndPrompt(activity: Activity) {
        val manager = AppUpdateManagerFactory.create(activity)
        manager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                manager.startUpdateFlow(
                    info,
                    activity,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                )
            }
        }.addOnFailureListener { e ->
            Log.w(TAG, "No se pudo comprobar actualización: ${e.message}")
        }
    }
}
