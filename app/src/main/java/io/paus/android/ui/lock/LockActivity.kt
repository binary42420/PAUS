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
package io.paus.android.ui.lock

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.viewpager.widget.ViewPager
import io.paus.android.databinding.LockActivityBinding
import io.paus.android.extensions.showSafe
import io.paus.android.logic.BlockingReason
import io.paus.android.logic.DefaultAppLogic
import io.paus.android.u2f.U2fManager
import io.paus.android.u2f.protocol.U2FDevice
import io.paus.android.ui.login.AuthTokenLoginProcessor
import io.paus.android.ui.login.NewLoginFragment
import io.paus.android.ui.main.ActivityViewModel
import io.paus.android.ui.main.ActivityViewModelHolder
import io.paus.android.ui.main.AuthenticationFab

class LockActivity : AppCompatActivity(), ActivityViewModelHolder, U2fManager.DeviceFoundListener {
    companion object {
        private const val EXTRA_PACKAGE_NAME = "pkg"
        private const val EXTRA_ACTIVITY_NAME = "an"
        private const val LOGIN_DIALOG_TAG = "ldt"

        val currentInstances = mutableSetOf<LockActivity>()

        fun start(context: Context, packageName: String, activityName: String?) {
            context.startActivity(
                    Intent(context, LockActivity::class.java)
                            .putExtra(EXTRA_PACKAGE_NAME, packageName)
                            .apply {
                                if (activityName != null) {
                                    putExtra(EXTRA_ACTIVITY_NAME, activityName)
                                }
                            }
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            )
        }
    }

    private val model: LockModel by viewModels()
    private val activityModel: ActivityViewModel by viewModels()

    override var ignoreStop: Boolean = false

    private val blockedPackageName: String by lazy {
        intent.getStringExtra(EXTRA_PACKAGE_NAME)!!
    }

    private val blockedActivityName: String? by lazy {
        if (intent.hasExtra(EXTRA_ACTIVITY_NAME))
            intent.getStringExtra(EXTRA_ACTIVITY_NAME)
        else
            null
    }

    private val showAuth = MutableLiveData<Boolean>().apply { value = false }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        U2fManager.setupActivity(this)

        val adapter = LockActivityAdapter(supportFragmentManager, this)

        val binding = LockActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentInstances.add(this)

        model.init(blockedPackageName, blockedActivityName)

        binding.pager.adapter = adapter

        AuthenticationFab.manageAuthenticationFab(
                fab = binding.fab,
                shouldHighlight = activityModel.shouldHighlightAuthenticationButton,
                authenticatedUser = activityModel.authenticatedUser,
                activity = this,
                doesSupportAuth = showAuth
        )

        binding.fab.setOnClickListener { showAuthenticationScreen() }

        binding.pager.addOnPageChangeListener(object: ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                showAuth.value = position == 1
            }
        })

        binding.tabs.setupWithViewPager(binding.pager)

        model.content.observe(this) {
            val isTimeOver = it is LockscreenContent.Blocked.BlockedCategory && it.blockingHandling.activityBlockingReason == BlockingReason.TimeOver

            adapter.showTasksFragment = isTimeOver
        }

        onBackPressedDispatcher.addCallback(object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {/* nothing to do */}
        })
    }

    override fun onDestroy() {
        super.onDestroy()

        currentInstances.remove(this)
    }

    override fun getActivityViewModel(): ActivityViewModel = activityModel

    override fun showAuthenticationScreen() {
        NewLoginFragment().showSafe(supportFragmentManager, LOGIN_DIALOG_TAG)
    }

    override fun onResume() {
        super.onResume()

        lockTaskModeWorkaround()
        U2fManager.with(this).registerListener(this)
    }

    override fun onPause() {
        super.onPause()

        lockTaskModeWorkaround()
        U2fManager.with(this).unregisterListener(this)
    }

    override fun onStop() {
        super.onStop()

        if ((!isChangingConfigurations) && (!ignoreStop)) {
            getActivityViewModel().logOut()
        }
    }

    private fun lockTaskModeWorkaround() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val platformIntegration = DefaultAppLogic.with(this).platformIntegration
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

            val isLocked = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                activityManager.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_PINNED
            else
                activityManager.isInLockTaskMode

            if (isLocked) {
                platformIntegration.setSuspendedApps(listOf(blockedPackageName), true)
                platformIntegration.setSuspendedApps(listOf(blockedPackageName), false)
            }
        }
    }

    override fun onDeviceFound(device: U2FDevice) = AuthTokenLoginProcessor.process(device, getActivityViewModel())
}
