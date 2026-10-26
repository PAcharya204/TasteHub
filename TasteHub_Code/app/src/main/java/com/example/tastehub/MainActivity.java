package com.example.tastehub;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tastehub.database.DatabaseHelper;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper databaseHelper;

    EditText etMobile;
    EditText etPassword;

    Button btnLogin;

    TextView tvPasswordVisibility;
    TextView tvForgotPassword;

    boolean passwordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Open Database
        databaseHelper = new DatabaseHelper(this);
        databaseHelper.getWritableDatabase();

        // Connect XML Components
        etMobile = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvPasswordVisibility = findViewById(R.id.tvPasswordVisibility);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // ==========================================
        // SHOW / HIDE PASSWORD
        // ==========================================

        tvPasswordVisibility.setOnClickListener(v -> {

            if (passwordVisible) {

                // Hide Password
                etPassword.setTransformationMethod(
                        PasswordTransformationMethod.getInstance()
                );

                tvPasswordVisibility.setText("◉");
                passwordVisible = false;

            } else {

                // Show Password
                etPassword.setTransformationMethod(null);

                tvPasswordVisibility.setText("◎");
                passwordVisible = true;
            }

            // Keep Cursor at the End
            etPassword.setSelection(etPassword.getText().length());
        });

        // ==========================================
        // LOGIN BUTTON
        // ==========================================

        btnLogin.setOnClickListener(v -> {

            String mobile = etMobile.getText().toString().trim();
            String password = etPassword.getText().toString();

            // Check Empty Fields
            if (mobile.isEmpty() || password.isEmpty()) {

                Toast.makeText(
                        MainActivity.this,
                        "Please enter mobile number and password",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // Validate Mobile Number: Exactly 10 Digits
            if (!mobile.matches("[0-9]{10}")) {

                etMobile.setError(
                        "Enter a valid 10-digit mobile number"
                );

                etMobile.requestFocus();
                return;
            }

            // Validate Password: Minimum 8 Characters
            if (password.length() < 8) {

                etPassword.setError(
                        "Password must be at least 8 characters"
                );

                etPassword.requestFocus();
                return;
            }

            // Get Readable Database
            SQLiteDatabase db = databaseHelper.getReadableDatabase();

            // Check User Credentials
            Cursor cursor = db.rawQuery(
                    "SELECT * FROM users WHERE mobile = ? AND password = ?",
                    new String[]{mobile, password}
            );

            try {

                // ==========================================
                // LOGIN SUCCESS
                // ==========================================

                if (cursor.moveToFirst()) {

                    int userId = cursor.getInt(
                            cursor.getColumnIndexOrThrow("user_id")
                    );

                    Toast.makeText(
                            MainActivity.this,
                            "Login successful",
                            Toast.LENGTH_SHORT
                    ).show();

                    // Save Logged-in User ID
                    getSharedPreferences(
                            "TasteHubSession",
                            MODE_PRIVATE
                    ).edit()
                            .putInt("user_id", userId)
                            .apply();

                    // Navigate to Home
                    Intent intent = new Intent(
                            MainActivity.this,
                            HomeActivity.class
                    );

                    startActivity(intent);
                    finish();

                } else {

                    // ==========================================
                    // LOGIN FAILED
                    // ==========================================

                    Toast.makeText(
                            MainActivity.this,
                            "Invalid mobile number or password",
                            Toast.LENGTH_SHORT
                    ).show();
                }

            } finally {
                cursor.close();
            }
        });

        // ==========================================
        // FORGOT PASSWORD
        // ==========================================

        tvForgotPassword.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            MainActivity.this,
                            ResetPasswordActivity.class
                    )
            );
        });

        // ==========================================
        // REGISTER
        // ==========================================

        TextView tvRegister = findViewById(R.id.tvRegister);

        tvRegister.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            MainActivity.this,
                            CreateAccountActivity.class
                    )
            );
        });
    }
}
