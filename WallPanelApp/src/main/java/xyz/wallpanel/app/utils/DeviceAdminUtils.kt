package xyz.wallpanel.app.utils

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import timber.log.Timber
import xyz.wallpanel.app.WallPanelDeviceAdminReceiver
import javax.inject.Inject

import dagger.hilt.android.qualifiers.ApplicationContext

class DeviceAdminUtils @Inject constructor(@ApplicationContext private val context: Context) {

    private val devicePolicyManager: DevicePolicyManager by lazy {
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    }

    private val componentName: ComponentName by lazy {
        ComponentName(context, WallPanelDeviceAdminReceiver::class.java)
    }

    fun isDeviceOwner(): Boolean {
        return devicePolicyManager.isDeviceOwnerApp(context.packageName)
    }

    fun isAdmin(): Boolean {
        return devicePolicyManager.isAdminActive(componentName)
    }

    fun setLockTaskPackages(packages: Array<String>) {
        if (isDeviceOwner()) {
            try {
                devicePolicyManager.setLockTaskPackages(componentName, packages)
                Timber.d("LockTask packages set: ${packages.joinToString()}")
            } catch (e: SecurityException) {
                Timber.e(e, "Error setting LockTask packages")
            }
        }
    }

    fun startLockTask(activity: android.app.Activity) {
        if (isDeviceOwner()) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                     // Ensure the package is allowed to lock task
                     val currentPackages = devicePolicyManager.getLockTaskPackages(componentName)
                     if (!currentPackages.contains(context.packageName)) {
                         setLockTaskPackages(arrayOf(context.packageName))
                     }
                } else {
                     setLockTaskPackages(arrayOf(context.packageName))
                }

                activity.startLockTask()
                Timber.d("LockTask started")
            } catch (e: Exception) {
                Timber.e(e, "Error starting LockTask")
            }
        }
    }

    fun stopLockTask(activity: android.app.Activity) {
        if (isDeviceOwner()) {
            try {
                activity.stopLockTask()
                Timber.d("LockTask stopped")
            } catch (e: Exception) {
                Timber.e(e, "Error stopping LockTask")
            }
        }
    }
}
