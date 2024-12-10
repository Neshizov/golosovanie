package com.example.golosovanie;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;

public class LoginActivity extends AppCompatActivity {
    private EditText emailInput, passwordInput;
    private Button loginButton;
    private TextView registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        APIClient.init(getApplicationContext());

        emailInput = findViewById(R.id.emailEt);
        passwordInput = findViewById(R.id.passwordEt);
        loginButton = findViewById(R.id.bottom_login);
        registerButton = findViewById(R.id.text_register);

        loginButton.setOnClickListener(view -> login());
        registerButton.setOnClickListener(view -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void login() {
        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            APIClient.post("/login", new JSONObject()
                    .put("email", email)
                    .put("password", password), response -> {
                try {
                    Log.d("LoginResponse", "Ответ от сервера: " + response.toString());

                    if (response.has("token")) {
                        String token = response.getString("token");
                        SessionManager.getInstance(this).login(token);

                        startActivity(new Intent(this, GolosActivity.class));
                        finish();
                    } else {
                        String message = response.optString("message", "Ошибка входа");
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Ошибка обработки данных", Toast.LENGTH_SHORT).show();
                }
            }, error -> {
                Log.e("LoginError", "Ошибка сети: " + error.getMessage());
                Toast.makeText(this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            });
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка в отправленных данных", Toast.LENGTH_SHORT).show();
        }
    }
}