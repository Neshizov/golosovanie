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
        try {
            JSONObject data = new JSONObject();
            data.put("email", email);
            data.put("password", password);

            APIClient.post("/register", data, response -> {
                try {
                    String message = response.optString("message", "Неизвестная ошибка");
                    String token = response.optString("token");

                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

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
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Ошибка при обработке ответа", Toast.LENGTH_SHORT).show();
                }
            }, error -> {
                Toast.makeText(this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка при формировании данных", Toast.LENGTH_SHORT).show();
        }
    }
}