package io.github.stashboy.aa.pwned.ui.aa;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.apps.auto.sdk.CarActivity;
import com.google.android.apps.auto.sdk.CarToast;
import io.github.stashboy.aa.pwned.R;
import io.github.stashboy.aa.pwned.databinding.ActivityAaDisplayBinding;

public class AaDisplayActivity extends CarActivity {
    private static final String TAG = "AADisplay_AaActivity";

    // Current live instance, so the AA-side session can be dismissed from elsewhere (e.g. when the
    // virtual display is torn down by the phone-side kill switch). CarActivity has no finish(),
    // so goHome() uses back navigation, which returns to the Android Auto launcher.
    public static AaDisplayActivity current = null;

    public void goHome() {
        // Return Android Auto to its native launcher by finishing this car activity. CarActivity
        // exposes no finish(); the actual finish lives on the host (com.google.android.gms.car.c's
        // private CarActivityHost field, which gearhead backs). Locate it by TYPE (not name) so R8
        // obfuscation of the field/class name cannot break it, then call CarActivityHost.finish().
        try {
            com.google.android.gms.car.CarActivityHost host = null;
            Class<?> cls = getClass();
            while (cls != null && host == null) {
                for (java.lang.reflect.Field f : cls.getDeclaredFields()) {
                    if (com.google.android.gms.car.CarActivityHost.class.isAssignableFrom(f.getType())) {
                        f.setAccessible(true);
                        Object value = f.get(this);
                        if (value instanceof com.google.android.gms.car.CarActivityHost) {
                            host = (com.google.android.gms.car.CarActivityHost) value;
                            break;
                        }
                    }
                }
                cls = cls.getSuperclass();
            }
            if (host != null) {
                Log.i(TAG, "goHome: finishing car activity via CarActivityHost -> AA launcher");
                host.finish();
            } else {
                Log.e(TAG, "goHome: CarActivityHost not found; cannot return to AA home");
            }
        } catch (Throwable t) {
            Log.e(TAG, "goHome failed", t);
        }
    }

    private ActivityAaDisplayBinding mBinding;

    protected Window getWindow() {
        return this.c();
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Log.i(TAG, "onCreate");
        current = this;
        setIgnoreConfigChanges(0xFFFF);
        this.setTheme(R.style.Theme_AADisplay);
        this.mBinding = ActivityAaDisplayBinding.inflate(getLayoutInflater());
        this.addGenericView(this.mBinding.getRoot());
        this.setContentView(this.mBinding.getRoot());
        this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        AaDisplayActivityKt.INSTANCE.showMain(getSupportFragmentManager());
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.i(TAG, "onStop");
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        switch (keyCode){
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                AaDisplayActivityKt.INSTANCE.pressKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                return true;
            case KeyEvent.KEYCODE_BACK:
                AaDisplayActivityKt.INSTANCE.pressKey(keyCode);
                return true;
            default:
                AaDisplayActivityKt.INSTANCE.toast("键值:[" + keyCode + "]未适配");
                return super.onKeyDown(keyCode, keyEvent);
        }
    }

    private boolean onGenericMotionEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_SCROLL: {
                float vScroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
                if (vScroll < 0) {
                    AaDisplayActivityKt.INSTANCE.pressKey(KeyEvent.KEYCODE_MEDIA_REWIND);
                } else if (vScroll > 0) {
                    AaDisplayActivityKt.INSTANCE.pressKey(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD);
                }
            }
        }
        return true;
    }
    private void addGenericView(ViewGroup viewGroup){
        View view = new View(this){
            @Override
            public boolean onGenericMotionEvent(MotionEvent event) {
                return AaDisplayActivity.this.onGenericMotionEvent(event);
            };
        };
        view.setLayoutParams(new ViewGroup.LayoutParams(1, 1));
        view.setAlpha(0);
        viewGroup.addView(view, 0);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        if (current == this) current = null;
        super.onDestroy();
    }


}
