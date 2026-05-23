package io.github.nitsuya.aa.display.util

import com.topjohnwu.superuser.Shell

object WazeOnAaManager {
    private const val WAZE_PACKAGE = "com.waze"
    private const val AA_PACKAGE = "com.google.android.projection.gearhead"
    const val GLOBAL_DISABLE_WAZE_ON_AA_KEY = "aad_disable_waze_on_aa"

    private val targetComponents = arrayOf(
        "com.waze/com.waze.car_lib.WazeCarAppService",
        "com.waze/com.waze.android_auto.AndroidAutoPhoneActivity"
    )

    fun apply(disableWazeOnAa: Boolean): Boolean {
        val componentCommand = if (disableWazeOnAa) "disable" else "enable"
        val componentOpsSucceeded = targetComponents.all { component ->
            Shell.getShell().newJob().add("pm $componentCommand $component").exec().isSuccess
        }

        val setGlobalToggle = Shell.getShell()
            .newJob()
            .add("settings put global $GLOBAL_DISABLE_WAZE_ON_AA_KEY ${if (disableWazeOnAa) 1 else 0}")
            .exec()
            .isSuccess
        val stopWaze = Shell.getShell().newJob().add("am force-stop $WAZE_PACKAGE").exec().isSuccess
        val stopAa = Shell.getShell().newJob().add("am force-stop $AA_PACKAGE").exec().isSuccess
        return componentOpsSucceeded && setGlobalToggle && stopWaze && stopAa
    }
}
