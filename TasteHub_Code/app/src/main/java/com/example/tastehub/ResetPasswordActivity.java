package com.example.tastehub;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tastehub.database.DatabaseHelper;

public class ResetPasswordActivity extends AppCompatActivity {
    private EditText etMobile, etNewPassword, etConfirmPassword;
    private DatabaseHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        helper = new DatabaseHelper(this);
        etMobile = findViewById(R.id.etMobile);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnResetPassword).setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String mobile = etMobile.getText().toString().trim();
        String password = etNewPassword.getText().toString();
        String confirm = etConfirmPassword.getText().toString();

        if (mobile.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            toast("Please fill in all fields");
            return;
        }
        if (!mobile.matches("\\d{10}")) {
            toast("Enter a valid 10-digit mobile number");
            return;
        }
        if (password.length() < 6) {
            toast("Password must be at least 6 characters");
            return;
        }
        if (!password.equals(confirm)) {
            toast("Passwords do not match");
            return;
        }

        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query("users", new String[]{"user_id"}, "mobile=?",
                new String[]{mobile}, null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();

        if (!exists) {
            toast("No account found with that mobile number");
            return;
        }

        ContentValues values = new ContentValues();
        values.put("password", password);
        int updated = db.update("users", values, "mobile=?", new String[]{mobile});

        if (updated > 0) {
            toast("Password reset successfully");
            finish();
        } else {
            toast("Could not reset password. Please try again.");
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
