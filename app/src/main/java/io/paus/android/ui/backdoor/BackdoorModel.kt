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
package io.paus.android.ui.backdoor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import io.paus.android.BuildConfig
import io.paus.android.async.Threads
import io.paus.android.coroutines.executeAndWait
import io.paus.android.coroutines.runAsync
import io.paus.android.crypto.HexString
import io.paus.android.livedata.castDown
import io.paus.android.livedata.map
import io.paus.android.livedata.waitForNonNullValue
import io.paus.android.logic.DefaultAppLogic
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

class BackdoorModel(application: Application): AndroidViewModel(application) {
    companion object {
        private val publicKey = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(HexString.fromHex(BuildConfig.backdoorPublicKey)))

        private fun verify(input: String, signature: ByteArray): Boolean {
            Signature.getInstance("SHA512withRSA").apply {
                initVerify(publicKey)
                update(input.toByteArray())

                return verify(signature)
            }
        }
    }

    private val logic = DefaultAppLogic.with(application)

    private val deviceId = logic.deviceId
    private val statusInternal = MutableLiveData<RecoveryStatus>().apply { value = RecoveryStatus.WaitingForCode }

    val status = statusInternal.castDown()
    val nonce = deviceId.map { deviceId ->
        "open-paus-${HexString.toHex((deviceId ?: "").toByteArray())}"
    }

    fun validateSignatureAndReset(signature: ByteArray) {
        runAsync {
            statusInternal.value = RecoveryStatus.Verifying

            val sequence = nonce.waitForNonNullValue()
            val isValid = Threads.crypto.executeAndWait {
                verify(sequence, signature)
            }

            if (isValid) {
                logic.appSetupLogic.resetAppCompletely()

                statusInternal.value = RecoveryStatus.Done
            } else {
                statusInternal.value = RecoveryStatus.Invalid
            }
        }
    }

    fun confirmInvalidCode() {
        statusInternal.value = RecoveryStatus.WaitingForCode
    }
}

enum class RecoveryStatus {
    WaitingForCode,
    Verifying,
    Done,
    Invalid
}