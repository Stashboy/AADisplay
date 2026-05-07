package io.github.nitsuya.aa.display.util

interface AABroadcastConst {
    companion object {
        val ACTION_SCREEN_CONTROL = "aa.display.action.SCREEN_CONTROL"
        val ACTION_STEERING_WHEEL_CONTROL = "aa.display.action.STEERING_WHEEL_CONTROL"
        val ACTION_SESSION_CONTROL = "aa.display.action.SESSION_CONTROL"
        val EXTRA_ACTION = "aa.display.extra.ACTION"
        val EXTRA_TYPE = "aa.display.extra.TYPE"
        val EXTRA_SESSION_ACTION = "aa.display.extra.SESSION_ACTION"
        val SESSION_ACTION_EXIT = 1
    }
}
