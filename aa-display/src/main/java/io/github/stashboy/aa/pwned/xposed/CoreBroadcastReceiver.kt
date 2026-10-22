package io.github.stashboy.aa.pwned.xposed

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.stashboy.aa.pwned.BuildConfig

object CoreBroadcastReceiver : BroadcastReceiver() {
    const val TAG = "AADisplay_CoreBroadcastReceiver"
    override fun onReceive(context: Context, intent: Intent) {
        TipUtil.showToast("事件: ${intent.action}")
    }
}