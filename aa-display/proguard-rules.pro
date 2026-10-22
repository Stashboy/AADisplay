-keep class io.github.stashboy.aa.pwned.** { *; }

# Keep the AA SDK host interface so AaDisplayActivity.goHome() can call CarActivityHost.finish()
# (located reflectively by type) to return Android Auto to its native launcher.
-keep interface com.google.android.gms.car.CarActivityHost { *; }
-keep interface com.google.android.gms.car.CarActivityHost$HostedCarActivity { *; }