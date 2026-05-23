package io.github.nitsuya.aa.display.util

import com.topjohnwu.superuser.Shell

object GoogleMapsOnAaManager {
    private const val MAPS_PACKAGE = "com.google.android.apps.maps"
    private const val AA_PACKAGE = "com.google.android.projection.gearhead"
    const val GLOBAL_DISABLE_GOOGLE_MAPS_ON_AA_KEY = "aad_disable_google_maps_on_aa"

    // Extracted from installed Maps manifest (26.20.01.913318892):
    // services declaring CATEGORY_PROJECTION(_NAVIGATION) + AA ghost activity.
    private val targetComponents = arrayOf(
        "com.google.android.apps.maps/com.google.android.apps.gmm.car.GmmCarProjectionService",
        "com.google.android.apps.maps/com.google.android.apps.gmm.car.LimitedGmmCarProjectionService",
        "com.google.android.apps.maps/com.google.android.apps.gmm.car.projected.auxiliarymap.GmmCarAuxiliaryProjectionService",
        "com.google.android.apps.maps/com.google.android.apps.gmm.car.WidescreenWidgetLimitedGmmCarProjectionService",
        "com.google.android.apps.maps/com.google.android.apps.auto.client.activity.ghost.GhostActivity"
    )

    fun apply(disableGoogleMapsOnAa: Boolean): Boolean {
        val componentCommand = if (disableGoogleMapsOnAa) "disable" else "enable"
        val componentOpsSucceeded = targetComponents.all { component ->
            Shell.getShell().newJob().add("pm $componentCommand $component").exec().isSuccess
        }

        val setGlobalToggle = Shell.getShell()
            .newJob()
            .add("settings put global $GLOBAL_DISABLE_GOOGLE_MAPS_ON_AA_KEY ${if (disableGoogleMapsOnAa) 1 else 0}")
            .exec()
            .isSuccess
        val stopMaps = Shell.getShell().newJob().add("am force-stop $MAPS_PACKAGE").exec().isSuccess
        val stopAa = Shell.getShell().newJob().add("am force-stop $AA_PACKAGE").exec().isSuccess
        return componentOpsSucceeded && setGlobalToggle && stopMaps && stopAa
    }
}
