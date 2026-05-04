package io.github.nitsuya.aa.display.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.net.toUri
import androidx.core.view.MenuProvider
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topjohnwu.superuser.Shell
import io.github.duzhaokun123.template.bases.BaseActivity
import io.github.nitsuya.aa.display.BuildConfig
import io.github.nitsuya.aa.display.CoreApi
import io.github.nitsuya.aa.display.R
import io.github.nitsuya.aa.display.databinding.ActivityMainBinding
import io.github.nitsuya.aa.display.util.AADisplayConfig
import io.github.nitsuya.template.bases.getAttr

class MainActivity :
    BaseActivity<ActivityMainBinding>(
        ActivityMainBinding::class.java,
        Config.NO_BACK,
        Config.LAYOUT_MATCH_HORI
    ),
    MenuProvider {
    companion object {
        const val TAG = "AADisplay_MainActivity"
    }

    private val appConfig by lazy {
        getSharedPreferences(AADisplayConfig.ConfigName, MODE_WORLD_READABLE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ActivityMainBinding.inflate(LayoutInflater.from(this))
        super.onCreate(savedInstanceState)
        addMenuProvider(this, this)
    }

    override fun initViews() {
        super.initViews()
        setupSettingControls()
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        val buildTime = CoreApi.buildTime
        Log.d(TAG, "buildtime: $buildTime ${BuildConfig.BUILD_TIME}")
        when (buildTime) {
            0L -> {
                baseBinding.ivIcon.setImageResource(R.drawable.ic_error_outline_24)
                baseBinding.tvActive.setText(R.string.not_activated)
                baseBinding.tvVersion.text = ""
                val colorError = android.R.attr.colorError
                val colorOnError = theme.getAttr(com.google.android.material.R.attr.colorOnError).data
                baseBinding.mcvStatus.setCardBackgroundColor(colorError)
                baseBinding.mcvStatus.outlineAmbientShadowColor = colorError
                baseBinding.mcvStatus.outlineSpotShadowColor = colorError
                baseBinding.tvActive.setTextColor(colorOnError)
                baseBinding.tvVersion.setTextColor(colorOnError)
            }

            BuildConfig.BUILD_TIME -> {
                baseBinding.ivIcon.setImageResource(R.drawable.ic_round_check_circle_24)
                baseBinding.tvActive.setText(R.string.activated)
                baseBinding.tvVersion.text = CoreApi.versionName
            }

            else -> {
                baseBinding.ivIcon.setImageResource(R.drawable.ic_warning_amber_24)
                baseBinding.tvActive.setText(R.string.need_reboot)
                baseBinding.tvVersion.text =
                    "system: ${CoreApi.versionName}\nmodule: ${BuildConfig.VERSION_NAME}"
                baseBinding.mcvStatus.setCardBackgroundColor(
                    MaterialColors.harmonizeWithPrimary(this, getColor(R.color.color_warning))
                )
                baseBinding.mcvStatus.setOnClickListener {
                    MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.need_reboot)
                        .setPositiveButton(R.string.reboot) { _, _ ->
                            Shell.getShell().newJob().add("reboot").exec()
                        }
                        .show()
                }
            }
        }

        if (Build.VERSION.PREVIEW_SDK_INT != 0) {
            baseBinding.systemVersion.text =
                "${Build.VERSION.CODENAME} Preview (API ${Build.VERSION.SDK_INT})"
        } else {
            baseBinding.systemVersion.text = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        }
        baseBinding.rootPrivilege.text = if (Shell.getShell().isRoot) "YES" else "NO"
        refreshSettingControls()
    }

    override fun onResume() {
        super.onResume()
        refreshSettingControls()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_main, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.github -> {
                startActivity(Intent(Intent.ACTION_VIEW).apply {
                    data = "https://github.com/Nitsuya/AADisplay".toUri()
                })
                true
            }

            else -> false
        }
    }

    private fun setupSettingControls() {
        baseBinding.switchAutoOpen.setOnCheckedChangeListener { _, isChecked ->
            appConfig.edit().putBoolean(AADisplayConfig.AutoOpen.key, isChecked).apply()
        }
        baseBinding.etLauncherPackage.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) persistLauncherPackage()
        }
        baseBinding.etDelayDestroyTime.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) persistDelayDestroyTime()
        }
        baseBinding.etLauncherPackage.setOnEditorActionListener { _, _, _ ->
            persistLauncherPackage()
            false
        }
        baseBinding.etDelayDestroyTime.setOnEditorActionListener { _, _, _ ->
            persistDelayDestroyTime()
            false
        }
    }

    private fun refreshSettingControls() {
        baseBinding.switchAutoOpen.isChecked = AADisplayConfig.AutoOpen.get(appConfig)
        baseBinding.etLauncherPackage.setText(AADisplayConfig.LauncherPackage.get(appConfig).orEmpty())
        baseBinding.etDelayDestroyTime.setText(AADisplayConfig.DelayDestroyTime.get(appConfig).toString())
    }

    private fun persistLauncherPackage() {
        val value = baseBinding.etLauncherPackage.text?.toString()?.trim().orEmpty()
        if (value.isBlank()) {
            baseBinding.tilLauncherPackage.error = getString(R.string.default_launch_package_empty)
            return
        }
        baseBinding.tilLauncherPackage.error = null
        appConfig.edit()
            .putString(AADisplayConfig.LauncherPackage.key, value)
            .putString(AADisplayConfig.HomePackage.key, value)
            .apply()
    }

    private fun persistDelayDestroyTime() {
        val raw = baseBinding.etDelayDestroyTime.text?.toString()?.trim().orEmpty()
        val value = raw.toIntOrNull()
        if (value == null || value < 0) {
            baseBinding.tilDelayDestroyTime.error = getString(R.string.delay_destroy_time_invalid)
            return
        }
        baseBinding.tilDelayDestroyTime.error = null
        appConfig.edit()
            .putString(AADisplayConfig.DelayDestroyTime.key, value.toString())
            .apply()
    }
}
