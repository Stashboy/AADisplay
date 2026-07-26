package io.github.nitsuya.aa.display.xposed

import android.util.Log
import de.robv.android.xposed.XposedBridge

fun log(tag: String, message: String) {
    Log.i(tag, message)
    XposedBridge.log("[$tag] $message")
}

fun log(tag: String, message: String, t: Throwable?) {
    Log.e(tag, message, t)
    XposedBridge.log("[$tag] $message")
    if(t != null){
        XposedBridge.log(t)
    }
}
