package one.lop.mrsu

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import one.lop.mrsu.network.RetrofitClient
import android.util.Log

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvToken: TextView // TextView для отображения токена
    private lateinit var securePrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        initPreferences()
        initUI()
    }

    private fun initPreferences() {
        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        securePrefs = EncryptedSharedPreferences.create(
            this,
            "secure_prefs", // Убедитесь, что имя файла совпадает
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun initUI() {
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvToken = findViewById(R.id.tvToken)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            } else {
                performLogin(username, password)
            }
        }
    }

    private fun performLogin(username: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.authInstance.login(
                    grantType = "password",
                    clientId = "8",
                    clientSecret = "qweasd",
                    username = username,
                    password = password
                )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val accessToken = response.body()!!.access_token
                        val refreshToken = response.body()!!.refresh_token
                        tvToken.text = "Access Token: $accessToken\nRefresh Token: $refreshToken"
                        saveTokens(accessToken, refreshToken)

                        Log.d("LoginActivity", "Tokens saved: accessToken=$accessToken, refreshToken=$refreshToken")

                        // Переход на MainActivity
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        tvToken.text = "Ошибка авторизации"
                        Toast.makeText(this@LoginActivity, "Неверный логин или пароль", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvToken.text = "Ошибка: ${e.message}"
                    Toast.makeText(this@LoginActivity, "Произошла ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun saveTokens(accessToken: String, refreshToken: String) {
        with(securePrefs.edit()) {
            putString("access_token", accessToken)
            putString("refresh_token", refreshToken)
            apply()
        }
    }
}
