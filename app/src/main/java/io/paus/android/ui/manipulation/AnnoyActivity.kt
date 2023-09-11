/*
 * Paus Copyright (C) 2023 Ryley Holmes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package io.paus.android.ui.manipulation

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import io.paus.android.BuildConfig
import io.paus.android.R
import io.paus.android.data.model.UserType
import io.paus.android.databinding.AnnoyActivityBinding
import io.paus.android.extensions.showSafe
import io.paus.android.integration.platform.android.AndroidIntegrationApps
import io.paus.android.livedata.map
import io.paus.android.logic.DefaultAppLogic
import io.paus.android.u2f.U2fManager
import io.paus.android.u2f.protocol.U2FDevice
import io.paus.android.ui.backdoor.BackdoorDialogFragment
import io.paus.android.ui.login.AuthTokenLoginProcessor
import io.paus.android.ui.login.NewLoginFragment
import io.paus.android.ui.main.ActivityViewModel
import io.paus.android.ui.main.ActivityViewModelHolder
import io.paus.android.ui.manage.device.manage.ManipulationWarnings
import io.paus.android.util.TimeTextUtil

class AnnoyActivity : AppCompatActivity(), ActivityViewModelHolder, U2fManager.DeviceFoundListener {
    companion object {
        private const val LOG_TAG = "AnnoyActivity"

        fun start(context: Context) {
            context.startActivity(
                    Intent(context, AnnoyActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            )
        }
    }

    private val model: ActivityViewModel by viewModels()
    override var ignoreStop: Boolean = false
    override fun getActivityViewModel() = model
    override fun showAuthenticationScreen() { NewLoginFragment.newInstance(showOnLockscreen = true).showSafe(supportFragmentManager, "nlf") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        U2fManager.setupActivity(this)

        val logic = DefaultAppLogic.with(this)

        val binding = AnnoyActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val systemImageApps = packageManager.getInstalledApplications(0)
                .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == ApplicationInfo.FLAG_SYSTEM }
                .map { it.packageName }.toSet()

            val lockTaskPackages = AndroidIntegrationApps.appsToIncludeInLockTasks + setOf(packageName) + systemImageApps

            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "setLockTaskPackages: $lockTaskPackages")
            }

            if (logic.platformIntegration.setLockTaskPackages(lockTaskPackages.toList())) {
                startLockTask()
            }
        }

        logic.annoyLogic.shouldAnnoyRightNow.observe(this) { shouldRun ->
            if (!shouldRun) shutdown()
        }

        logic.annoyLogic.nextManualUnblockCountdown.observe(this) { countdown ->
            binding.canRequestUnlock = countdown == 0L
            binding.countdownText = getString(R.string.annoy_timer, TimeTextUtil.seconds((countdown / 1000).toInt(), this@AnnoyActivity))
        }

        logic.deviceEntry.map {
            val reasonItems = (it?.let { ManipulationWarnings.getFromDevice(it) } ?: ManipulationWarnings.empty)
                    .current
                    .map { getString(it.labelResourceId) }

            if (reasonItems.isEmpty()) {
                null
            } else {
                getString(R.string.annoy_reason, reasonItems.joinToString(separator = ", "))
            }
        }.observe(this) { binding.reasonText = it }

        binding.unlockTemporarilyButton.setOnClickListener {
            AnnoyUnlockDialogFragment.newInstance(AnnoyUnlockDialogFragment.UnlockDuration.Short)
                .show(supportFragmentManager)
        }

        binding.parentUnlockButton.setOnClickListener {
            AnnoyUnlockDialogFragment.newInstance(AnnoyUnlockDialogFragment.UnlockDuration.Long)
                .show(supportFragmentManager)
        }

        binding.useBackdoorButton.setOnClickListener { BackdoorDialogFragment().show(supportFragmentManager) }

        model.authenticatedUser.observe(this) { user ->
            if (user?.type == UserType.Parent) {
                logic.annoyLogic.doParentTempUnlock()
            }
        }

        onBackPressedDispatcher.addCallback(object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {/* nothing to do */}
        })
    }

    private fun shutdown() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            stopLockTask()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        U2fManager.with(this).registerListener(this)
    }

    override fun onPause() {
        super.onPause()

        U2fManager.with(this).unregisterListener(this)
    }

    override fun onDeviceFound(device: U2FDevice) = AuthTokenLoginProcessor.process(device, getActivityViewModel())
}
