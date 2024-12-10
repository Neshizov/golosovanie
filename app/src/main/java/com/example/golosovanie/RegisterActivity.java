package com.example.golosovanie;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import android.util.Log;
import android.widget.TextView;
import com.android.volley.VolleyError;

public class RegisterActivity extends AppCompatActivity {
    private EditText emailInput, passwordInput;
    private Button registerButton;
    private TextView loginText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        APIClient.init(getApplicationContext());

        emailInput = findViewById(R.id.emailEt);
        passwordInput = findViewById(R.id.passwordEt);
        registerButton = findViewById(R.id.registerButton);
        loginText = findViewById(R.id.loginText);

        registerButton.setOnClickListener(view -> register());

        loginText.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void register() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.contains("@") || email.indexOf('@') <= 5) {
            Toast.makeText(this, "Часть до @ в email должна быть больше 5 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 5) {
            Toast.makeText(this, "Пароль должен быть не меньше 5 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.matches("\\d+")) {
            Toast.makeText(this, "Пароль не должен состоять только из цифр", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            JSONObject data = new JSONObject();
            data.put("email", email);
            data.put("password", password);

            APIClient.post("/register", data, response -> {
                try {
                    String message = response.optString("message", "Неизвестная ошибка");
                    String token = response.optString("token");

                    if (message.equals("Пользователь успешно зарегистрирован") && !token.isEmpty()) {
                        DatabaseHelper dbHelper = new DatabaseHelper(this);
                        boolean isSaved = dbHelper.addUser(email, token);

                        if (isSaved) {
                            Toast.makeText(this, "Токен сохранён локально", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Ошибка сохранения токена", Toast.LENGTH_SHORT).show();
                        }
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Ошибка при обработке ответа", Toast.LENGTH_SHORT).show();
                }
            }, error -> {
                try {
                    String errorMessage = error.networkResponse != null
                            ? new JSONObject(new String(error.networkResponse.data)).optString("message", "Ошибка сети")
                            : "Ошибка сети";

                    if (errorMessage.equals("Пользователь с таким email уже существует")) {
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Ошибка регистрации: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка при формировании данных", Toast.LENGTH_SHORT).show();
        }
    }
}