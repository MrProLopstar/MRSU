package one.lop.mrsu

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.annotation.RequiresExtension
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import one.lop.mrsu.network.RetrofitClient
import one.lop.mrsu.util.TokenManager
import java.io.IOException
import retrofit2.HttpException

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvToken: TextView

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        if (MyApplication.instance.isLoggedIn()) {
            navigateToBaseActivity()
            return
        }

        val loginContainer = findViewById<LinearLayout>(R.id.loginContainer)

        val screenHeight = resources.displayMetrics.heightPixels
        val topMargin = (screenHeight * 0.2).toInt()

        loginContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
            this.topMargin = topMargin
        }

        initUI()
    }

    private fun navigateToBaseActivity() {
        val intent = Intent(this, BaseActivity::class.java)
        startActivity(intent)
        finish()
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun initUI() {
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        //tvToken = findViewById(R.id.tvToken)

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

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
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
                        //tvToken.text = "Access Token: $accessToken\nRefresh Token: $refreshToken"

                        TokenManager.saveTokens(accessToken, refreshToken)

                        Log.d("LoginActivity", "Tokens saved: accessToken=$accessToken, refreshToken=$refreshToken")

                        val intent = Intent(this@LoginActivity, BaseActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        //tvToken.text = "Ошибка авторизации"
                        Toast.makeText(this@LoginActivity, "Неверный логин или пароль", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    Log.e("LoginActivity", "HTTP Error: ${e.message}")
                    //tvToken.text = "Ошибка: ${e.message}"
                    Toast.makeText(this@LoginActivity, "HTTP ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Log.e("LoginActivity", "Network Error: ${e.message}")
                    //tvToken.text = "Ошибка сети: ${e.message}"
                    Toast.makeText(this@LoginActivity, "Проблемы с сетью: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("LoginActivity", "Unexpected Error: ${e.message}")
                    //tvToken.text = "Произошла непредвиденная ошибка: ${e.message}"
                    Toast.makeText(this@LoginActivity, "Неизвестная ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
