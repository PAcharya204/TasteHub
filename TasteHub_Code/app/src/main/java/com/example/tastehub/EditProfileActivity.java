package com.example.tastehub;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tastehub.database.DatabaseHelper;

public class EditProfileActivity extends Activity {

 EditText name, email, phone;
 DatabaseHelper dbHelper;
 int userId;

 @Override
 protected void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);

  setContentView(R.layout.activity_edit_profile);

  dbHelper = new DatabaseHelper(this);

  userId = getSharedPreferences(
          "TasteHubSession", MODE_PRIVATE
  ).getInt("user_id", -1);

  name = findViewById(R.id.editName);
  email = findViewById(R.id.editEmail);
  phone = findViewById(R.id.editPhone);

  // BACK BUTTON

  findViewById(R.id.backButton).setOnClickListener(v -> {
   finish();
  });

  // SAVE CHANGES

  findViewById(R.id.saveChangesButton)
          .setOnClickListener(v -> saveProfile());

  // BOTTOM NAVIGATION

  findViewById(R.id.navHome).setOnClickListener(v -> {
   startActivity(new android.content.Intent(
           EditProfileActivity.this,
           HomeActivity.class
   ));
   finish();
  });

  findViewById(R.id.navOrders).setOnClickListener(v -> {
   startActivity(new android.content.Intent(
           EditProfileActivity.this,
           OrderHistoryActivity.class
   ));
   finish();
  });

  findViewById(R.id.navProfile).setOnClickListener(v -> {
   finish();
  });

  loadProfile();
 }

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

    name.setText(cursor.getString(0));
    email.setText(cursor.getString(1));
    phone.setText(cursor.getString(2));

   }

  } finally {
   cursor.close();
  }
 }

 private void saveProfile() {

  if (userId == -1) {
   Toast.makeText(
           this,
           "Please login first",
           Toast.LENGTH_SHORT
   ).show();

   return;
  }

  String fullName = name.getText().toString().trim();
  String userEmail = email.getText().toString().trim();
  String mobile = phone.getText().toString().trim();

  if (fullName.isEmpty()
          || userEmail.isEmpty()
          || mobile.isEmpty()) {

   Toast.makeText(
           this,
           "Please fill all required fields",
           Toast.LENGTH_SHORT
   ).show();

   return;
  }

  ContentValues values = new ContentValues();

  values.put("name", fullName);
  values.put("email", userEmail);
  values.put("mobile", mobile);

  int rows = dbHelper.getWritableDatabase().update(
          "users",
          values,
          "user_id=?",
          new String[]{String.valueOf(userId)}
  );

  if (rows > 0) {

   Toast.makeText(
           this,
           "Profile updated successfully",
           Toast.LENGTH_SHORT
   ).show();

   finish();

  } else {

   Toast.makeText(
           this,
           "Profile was not updated",
           Toast.LENGTH_SHORT
   ).show();
  }
 }
}