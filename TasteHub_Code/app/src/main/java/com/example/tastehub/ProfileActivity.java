package com.example.tastehub;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tastehub.database.DatabaseHelper;

public class ProfileActivity extends Activity {

 TextView profileName, profileEmail, profilePhone;
 DatabaseHelper dbHelper;
 int userId;

 @Override
 protected void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  setContentView(R.layout.activity_profile);

  // Initialize Profile Views
  profileName = findViewById(R.id.profileName);
  profileEmail = findViewById(R.id.profileEmail);
  profilePhone = findViewById(R.id.profilePhone);

  // Initialize Database Helper
  dbHelper = new DatabaseHelper(this);

  // Get Logged-in User ID
  userId = getSharedPreferences(
          "TasteHubSession",
          MODE_PRIVATE
  ).getInt("user_id", -1);

  // EDIT PROFILE
  findViewById(R.id.editProfileRow).setOnClickListener(v -> {
   startActivity(
           new Intent(ProfileActivity.this,
                   EditProfileActivity.class)
   );
  });

  // ORDER DETAILS
  findViewById(R.id.orderDetailsRow).setOnClickListener(v -> {
   startActivity(
           new Intent(ProfileActivity.this,
                   OrderHistoryActivity.class)
   );
  });

  // ADDRESS
  findViewById(R.id.addressRow).setOnClickListener(v -> {
   Intent intent = new Intent(
           ProfileActivity.this,
           AddressActivity.class
   );

   intent.putExtra("from_cart", false);
   startActivity(intent);
  });

  // LOGOUT WITH CONFIRMATION
  findViewById(R.id.logoutRow).setOnClickListener(v -> {

   new AlertDialog.Builder(ProfileActivity.this)

           .setTitle("Logout")

           .setMessage(
                   "Are you sure you want to log out?"
           )

           .setPositiveButton("Logout", (dialog, which) -> {

            // Clear Login Session
            getSharedPreferences(
                    "TasteHubSession",
                    MODE_PRIVATE
            ).edit().clear().apply();

            // Navigate to Login Page
            Intent intent = new Intent(
                    ProfileActivity.this,
                    MainActivity.class
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();

           })

           .setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
           })

           .setCancelable(true)

           .show();
  });

  // HOME NAVIGATION
  findViewById(R.id.navHome).setOnClickListener(v -> {

   Intent intent = new Intent(
           ProfileActivity.this,
           HomeActivity.class
   );

   intent.addFlags(
           Intent.FLAG_ACTIVITY_CLEAR_TOP |
                   Intent.FLAG_ACTIVITY_SINGLE_TOP
   );

   startActivity(intent);
   finish();
  });

  // ORDERS NAVIGATION
  findViewById(R.id.navOrders).setOnClickListener(v -> {

   Intent intent = new Intent(
           ProfileActivity.this,
           OrderHistoryActivity.class
   );

   startActivity(intent);
  });

  // PROFILE NAVIGATION
  findViewById(R.id.navProfile).setOnClickListener(v -> {

   // Already on the Profile page.
  });

  // Load Profile Information
  loadProfile();
 }

 @Override
 protected void onResume() {
  super.onResume();
  loadProfile();
 }

 // LOAD PROFILE DETAILS FROM DATABASE
 private void loadProfile() {

  if (userId == -1) {

   Toast.makeText(
           this,
           "Please login first",
           Toast.LENGTH_SHORT
   ).show();

   return;
  }

  Cursor cursor = dbHelper.getReadableDatabase().query(
          "users",
          new String[]{"name", "email", "mobile"},
          "user_id=?",
          new String[]{String.valueOf(userId)},
          null,
          null,
          null
  );

  try {

   if (cursor.moveToFirst()) {

    profileName.setText(cursor.getString(0));
    profileEmail.setText(cursor.getString(1));
    profilePhone.setText(cursor.getString(2));

   }

  } finally {

   cursor.close();
  }
 }
}
