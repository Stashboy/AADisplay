package io.github.stashboy.aa.pwned.util;

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.support.car.Car
import com.google.android.gms.car.CarFirstPartyManager
import com.github.kyuubiran.ezxhelper.utils.field
import io.github.stashboy.aa.pwned.BuildConfig
import io.github.stashboy.aa.pwned.service.AaActivityService

fun Car.getGmsCarFirstPartyManager() : CarFirstPartyManager? {
    return (this.getCarManager("car_1p") as? com.google.android.apps.auto.sdk.service.CarFirstPartyManager)?.let { car1p ->
        (car1p::class.java.field("a", false, CarFirstPartyManager::class.java).get(car1p) as CarFirstPartyManager)
    } ?: null
}

fun CarFirstPartyManager?.startCarAaDisplay() {
    if(this == null) return
    this.startCarActivity(
        Intent().apply {
            component = ComponentName(BuildConfig.APPLICATION_ID, AaActivityService::class.java.name)
            putExtra("android.intent.extra.PACKAGE_NAME", BuildConfig.APPLICATION_ID)
        }
    )
}

fun CarFirstPartyManager?.startCarTelecom() {
    if(this == null) return
    this.startCarActivity(
        Intent().apply {
            component = ComponentName("com.google.android.projection.gearhead", "com.google.android.projection.gearhead.telecom.TelecomService")
        }
    )
}
