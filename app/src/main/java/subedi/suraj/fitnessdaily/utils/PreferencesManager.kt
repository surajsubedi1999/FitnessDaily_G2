package subedi.suraj.fitnessdaily.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("fitness_daily_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_TUTORIAL_COMPLETED = "tutorial_completed"
    }

    fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    fun setFirstLaunchCompleted() {
        sharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }

    fun isTutorialCompleted(): Boolean {
        return sharedPreferences.getBoolean(KEY_TUTORIAL_COMPLETED, false)
    }

    fun setTutorialCompleted() {
        sharedPreferences.edit().putBoolean(KEY_TUTORIAL_COMPLETED, true).apply()
    }
}