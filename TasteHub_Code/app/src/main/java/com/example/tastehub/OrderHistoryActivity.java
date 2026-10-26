package com.example.tastehub;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tastehub.database.DatabaseHelper;

import java.text.NumberFormat;
import java.util.Locale;

public class OrderHistoryActivity extends Activity {

 LinearLayout currentOrders, previousOrders;
 DatabaseHelper dbHelper;
 int userId;

 @Override
 protected void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);

  setContentView(R.layout.activity_order_history);

  dbHelper = new DatabaseHelper(this);

  userId = getSharedPreferences(
          "TasteHubSession", MODE_PRIVATE
  ).getInt("user_id", -1);

  currentOrders = findViewById(R.id.currentOrdersContainer);
  previousOrders = findViewById(R.id.previousOrdersContainer);

  // CART BUTTON

  findViewById(R.id.ivCart).setOnClickListener(v -> {
   startActivity(
           new Intent(
                   OrderHistoryActivity.this,
                   CartActivity.class
           )
   );
  });

  // CLEAR HISTORY BUTTON

  findViewById(R.id.clearHistoryButton).setOnClickListener(v -> {
   startActivity(
           new Intent(
                   OrderHistoryActivity.this,
                   ClearHistoryActivity.class
           )
   );
  });

  // HOME NAVIGATION

  findViewById(R.id.navHome).setOnClickListener(v -> {
   Intent intent = new Intent(
           OrderHistoryActivity.this,
           HomeActivity.class
   );
   startActivity(intent);
   finish();
  });

  // ORDERS NAVIGATION

  findViewById(R.id.navOrders).setOnClickListener(v -> {
   // Already on Order History page
  });

  // PROFILE NAVIGATION

  findViewById(R.id.navProfile).setOnClickListener(v -> {
   Intent intent = new Intent(
           OrderHistoryActivity.this,
           ProfileActivity.class
   );
   startActivity(intent);
  });
 }

 @Override
 protected void onResume() {
  super.onResume();
  loadOrders();
 }

 private void loadOrders() {

  currentOrders.removeAllViews();
  previousOrders.removeAllViews();

  if (userId == -1) {
   Toast.makeText(
           this,
           "Please login first",
           Toast.LENGTH_SHORT
   ).show();
   return;
  }

  Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
          "SELECT o.order_id, o.order_date, o.total_amount, " +
                  "o.status, COUNT(oi.order_item_id) " +
                  "FROM orders o LEFT JOIN order_items oi " +
                  "ON o.order_id = oi.order_id " +
                  "WHERE o.user_id = ? " +
                  "GROUP BY o.order_id ORDER BY o.order_id DESC",
          new String[]{String.valueOf(userId)}
  );

  try {

   while (cursor.moveToNext()) {

    String orderId = "TH" + cursor.getString(0);
    String date = cursor.getString(1);
    double amount = cursor.getDouble(2);
    String status = cursor.getString(3);
    int itemCount = cursor.getInt(4);

    LinearLayout card = new LinearLayout(this);
    card.setOrientation(LinearLayout.VERTICAL);
    card.setPadding(20, 18, 20, 18);
    card.setBackgroundColor(0xFFFFFFFF);

    LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(
                    -1, -2
            );

    params.setMargins(0, 0, 0, 14);
    card.setLayoutParams(params);

    TextView idView = new TextView(this);
    idView.setText(orderId);
    idView.setTextSize(18);
    idView.setTextColor(0xFF111111);
    idView.setTypeface(null, 1);

    TextView dateView = new TextView(this);
    dateView.setText(date == null ? "" : date);
    dateView.setTextSize(14);
    dateView.setTextColor(0xFF555555);

    TextView itemView = new TextView(this);
    itemView.setText(itemCount + " Items");
    itemView.setTextColor(0xFF555555);

    TextView statusView = new TextView(this);
    statusView.setText(
            status == null ? "Processing" : status
    );
    statusView.setTextColor(0xFF087F80);
    statusView.setTextSize(14);

    TextView amountView = new TextView(this);
    amountView.setText(
            NumberFormat.getCurrencyInstance(
                    new Locale("en", "IN")
            ).format(amount)
    );
    amountView.setTextColor(0xFF111111);
    amountView.setTextSize(16);
    amountView.setGravity(Gravity.END);

    card.addView(idView);
    card.addView(dateView);
    card.addView(itemView);
    card.addView(statusView);
    card.addView(amountView);

    if (currentOrders.getChildCount() == 0) {
     currentOrders.addView(card);
    } else {
     previousOrders.addView(card);
    }
   }

  } finally {
   cursor.close();
  }

  if (currentOrders.getChildCount() == 0) {
   addMessage(currentOrders, "No current orders.");
  }

  if (previousOrders.getChildCount() == 0) {
   addMessage(previousOrders, "No previous orders.");
  }
 }

 private void addMessage(
         LinearLayout layout,
         String message
 ) {

  TextView text = new TextView(this);
  text.setText(message);
  text.setTextSize(15);
  text.setPadding(8, 12, 8, 12);
  text.setTextColor(0xFF777777);

  layout.addView(text);
 }
}