package com.example.tastehub;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tastehub.database.DatabaseHelper;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText etName, etMobile, etEmail;
    private EditText etPassword, etConfirmPassword;

    private DatabaseHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // Initialize Database Helper
        helper = new DatabaseHelper(this);

        // Initialize Input Fields
        etName = findViewById(R.id.etName);
        etMobile = findViewById(R.id.etMobile);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        // Login Navigation
        TextView tvGoLogin = findViewById(R.id.tvGoLogin);

        tvGoLogin.setOnClickListener(v -> finish());

        // Create Account Button
        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);

        btnCreateAccount.setOnClickListener(v -> createAccount());
    }

    private void createAccount() {

        // Get Input Values
        String name = etName.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirm = etConfirmPassword.getText().toString();

        // Validate Empty Fields
        if (name.isEmpty() || mobile.isEmpty() || email.isEmpty()
                || password.isEmpty() || confirm.isEmpty()) {

            toast("Please fill in all fields");
            return;
        }

        // Validate Mobile Number
        if (!mobile.matches("[0-9]{10}")) {

            etMobile.setError("Enter a valid 10-digit mobile number");
            etMobile.requestFocus();
            return;
        }

        // Validate Email Address
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        // Validate Password Length
        if (password.length() < 8) {

            etPassword.setError("Password must be at least 8 characters");
            etPassword.requestFocus();
            return;
        }

        // Confirm Password
        if (!password.equals(confirm)) {

            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Store User Details
        ContentValues values = new ContentValues();

        values.put("name", name);
        values.put("mobile", mobile);
        values.put("email", email);
        values.put("password", password);

        // Insert User into Database
        SQLiteDatabase db = helper.getWritableDatabase();

        long id = db.insert("users", null, values);

        // Handle Duplicate Mobile Number
        if (id == -1) {

            toast("This mobile number is already registered");
            return;
        }

        // Save User Session
        getSharedPreferences("TasteHubSession", MODE_PRIVATE)
                .edit()
                .putInt("user_id", (int) id)
                .apply();

        // Registration Success
        toast("Account created successfully");

        // Navigate to Home Page
        Intent intent = new Intent(
                CreateAccountActivity.this,
                HomeActivity.class
        );

        startActivity(intent);
        finish();
    }

    // Display Toast Message
    private void toast(String message) {

        Toast.makeText(
                CreateAccountActivity.this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }
}
