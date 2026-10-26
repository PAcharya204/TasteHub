package com.example.tastehub;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import com.example.tastehub.database.DatabaseHelper;

public class ClearHistoryActivity extends Activity {

 DatabaseHelper dbHelper;
 int userId;

 @Override
 protected void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);

  setContentView(R.layout.activity_clear_history);

  dbHelper = new DatabaseHelper(this);

  userId = getSharedPreferences(
          "TasteHubSession", MODE_PRIVATE
  ).getInt("user_id", -1);

  // CANCEL BUTTON

  findViewById(R.id.cancelButton).setOnClickListener(v -> {
   finish();
  });

  // CLEAR BUTTON

  findViewById(R.id.clearButton).setOnClickListener(v -> {
   clearHistory();
  });

  // HOME NAVIGATION

  findViewById(R.id.navHome).setOnClickListener(v -> {
   Intent intent = new Intent(
           ClearHistoryActivity.this,
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
           ClearHistoryActivity.this,
           OrderHistoryActivity.class
   );

   intent.addFlags(
           Intent.FLAG_ACTIVITY_CLEAR_TOP |
                   Intent.FLAG_ACTIVITY_SINGLE_TOP
   );

   startActivity(intent);
   finish();
  });

  // PROFILE NAVIGATION

  findViewById(R.id.navProfile).setOnClickListener(v -> {
   Intent intent = new Intent(
           ClearHistoryActivity.this,
           ProfileActivity.class
   );

   startActivity(intent);
   finish();
  });
 }

 private void clearHistory() {

  if (userId == -1) {
   Toast.makeText(
           this,
           "Please login first",
           Toast.LENGTH_SHORT
   ).show();

   return;
  }

  SQLiteDatabase db = dbHelper.getWritableDatabase();

  db.beginTransaction();

  try {

   // DELETE ITEMS BELONGING TO THIS USER'S ORDERS

   db.delete(
           "order_items",
           "order_id IN (SELECT order_id FROM orders WHERE user_id=?)",
           new String[]{String.valueOf(userId)}
   );

   // DELETE THIS USER'S ORDERS

   db.delete(
           "orders",
           "user_id=?",
           new String[]{String.valueOf(userId)}
   );

   db.setTransactionSuccessful();

  } catch (Exception e) {

   Toast.makeText(
           this,
           "Unable to clear order history",
           Toast.LENGTH_SHORT
   ).show();

   return;

  } finally {

   db.endTransaction();
  }

  Toast.makeText(
          this,
          "Order history cleared",
          Toast.LENGTH_SHORT
  ).show();

  // RETURN TO ORDER HISTORY

  Intent intent = new Intent(
          ClearHistoryActivity.this,
          OrderHistoryActivity.class
  );

  intent.addFlags(
          Intent.FLAG_ACTIVITY_CLEAR_TOP |
                  Intent.FLAG_ACTIVITY_SINGLE_TOP
  );

  startActivity(intent);
  finish();
 }
}

