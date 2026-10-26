package com.example.tastehub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tastehub.database.DatabaseHelper;
import java.util.ArrayList;
import java.util.Locale;

public class OrderConfirmationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        int userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            userId = getSharedPreferences("TasteHubSession", MODE_PRIVATE).getInt("user_id", -1);
        }
        DatabaseHelper db = new DatabaseHelper(this);

        @SuppressWarnings("unchecked")
        ArrayList<DatabaseHelper.CartRow> items =
                (ArrayList<DatabaseHelper.CartRow>) getIntent().getSerializableExtra("ordered_items");

        if (items == null || items.isEmpty()) {
            items = db.getCartItems(userId);
        }

        // Ensure cart is cleared in database
        db.clearCart(userId);

        double subtotal = 0;
        if (items != null) {
            for (DatabaseHelper.CartRow item : items) {
                subtotal += item.price * item.quantity;
            }
        }
        double deliveryCharge = 30.0;
        double grandTotal = subtotal + deliveryCharge;

        initViews(items, deliveryCharge, grandTotal);
    }

    private void initViews(ArrayList<DatabaseHelper.CartRow> items, double deliveryCharge, double grandTotal) {
        LinearLayout layoutOrderItems = findViewById(R.id.layoutOrderItems);
        layoutOrderItems.removeAllViews();

        if (items != null) {
            for (DatabaseHelper.CartRow item : items) {
                View row = getLayoutInflater().inflate(R.layout.item_order_row, layoutOrderItems, false);
                TextView tvName = row.findViewById(R.id.tvRowName);
                TextView tvQty = row.findViewById(R.id.tvRowQty);
                TextView tvPrice = row.findViewById(R.id.tvRowPrice);

                tvName.setText(item.name);
                tvQty.setText(String.valueOf(item.quantity));
                tvPrice.setText(String.format(Locale.getDefault(), "₹%.2f", item.price * item.quantity));

                layoutOrderItems.addView(row);
            }
        }

        TextView tvDelivery = findViewById(R.id.tvConfirmationDelivery);
        tvDelivery.setText(String.format(Locale.getDefault(), "₹%.2f", deliveryCharge));

        TextView tvTotal = findViewById(R.id.tvConfirmationTotal);
        tvTotal.setText(String.format(Locale.getDefault(), "₹%.2f", grandTotal));

        Button btnBackToHome = findViewById(R.id.btnBackToHome);
        btnBackToHome.setOnClickListener(v -> goHome());

        findViewById(R.id.navHome).setOnClickListener(v -> goHome());
        findViewById(R.id.navOrders).setOnClickListener(v ->
                startActivity(new Intent(this, OrderHistoryActivity.class)));
        findViewById(R.id.navProfile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void goHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        goHome();
    }
}
