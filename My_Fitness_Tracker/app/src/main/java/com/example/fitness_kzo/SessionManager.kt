import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    private val editor = prefs.edit()

    companion object {
        private const val KEY_AUTH_TOKEN = "AUTH_TOKEN"
        private const val KEY_USER_ID = "USER_ID"
        private const val KEY_USER_EMAIL = "USER_EMAIL"
        private const val KEY_USER_NAME = "USER_NAME"
        private const val KEY_USER_HEIGHT = "USER_HEIGHT"
        private const val KEY_USER_WEIGHT = "USER_WEIGHT"
    }

    // Save authentication token
    fun saveAuthToken(token: String) {
        editor.putString(KEY_AUTH_TOKEN, token)
        editor.apply()
    }

    // Retrieve authentication token
    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    // Save user ID
    fun saveUserId(userId: Int) {
        editor.putInt(KEY_USER_ID, userId)
        editor.apply()
    }

    // Retrieve user ID
    fun getUserId(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }

    // Save user email
    fun saveUserEmail(email: String) {
        editor.putString(KEY_USER_EMAIL, email)
        editor.apply()
    }

    // Save user name
    fun saveUserName(name: String) {
        editor.putString(KEY_USER_NAME, name)
        editor.apply()
    }

    // Save user height
    fun saveUserHeight(height: Float) {
        editor.putFloat(KEY_USER_HEIGHT, height)
        editor.apply()
    }


    // Save user weight
    fun saveUserWeight(weight: Float) {
        editor.putFloat(KEY_USER_WEIGHT, weight)
        editor.apply()
    }
    fun getUserWeight(): Float {
        return prefs.getFloat(KEY_USER_WEIGHT, 70f) // Default 70kg
    }
    fun isUserLoggedIn(): Boolean {
        return getUserId() != -1
    }

    // Clear all session data
    fun clearSession() {
        editor.clear()
        editor.apply()
    }
}