package com.example.tastehub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tastehub.database.DatabaseHelper;
import java.util.ArrayList;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {
    private LinearLayout itemsContainer;
    private TextView subtotalText, deliveryText, totalText, emptyText;
    private DatabaseHelper db;
    private int userId;
    private ArrayList<DatabaseHelper.CartRow> cartItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        SharedPreferences session = getSharedPreferences("TasteHubSession", MODE_PRIVATE);
        userId = session.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = new DatabaseHelper(this);
        initViews();
        loadCart();
    }

    private void initViews() {
        findViewById(R.id.btnCartBack).setOnClickListener(v -> finish());

        emptyText = findViewById(R.id.tvEmptyCart);
        itemsContainer = findViewById(R.id.itemsContainer);
        subtotalText = findViewById(R.id.tvSubtotal);
        deliveryText = findViewById(R.id.tvDelivery);
        totalText = findViewById(R.id.tvGrandTotal);

        Button btnAddMore = findViewById(R.id.btnAddMore);
        btnAddMore.setOnClickListener(v -> finish());

        Button btnCheckout = findViewById(R.id.btnCheckout);
        btnCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(CartActivity.this, AddressActivity.class);
            intent.putExtra("from_cart", true);
            startActivity(intent);
        });

        findViewById(R.id.navHome).setOnClickListener(v -> finish());
        findViewById(R.id.navOrders).setOnClickListener(v ->
                startActivity(new Intent(CartActivity.this, OrderHistoryActivity.class)));
        findViewById(R.id.navProfile).setOnClickListener(v ->
                startActivity(new Intent(CartActivity.this, ProfileActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (db != null && itemsContainer != null) {
            loadCart();
        }
    }

    private void loadCart() {
        cartItems = db.getCartItems(userId);
        itemsContainer.removeAllViews();
        emptyText.setVisibility(cartItems.isEmpty() ? View.VISIBLE : View.GONE);

        double subtotal = 0;
        for (DatabaseHelper.CartRow item : cartItems) {
            subtotal += item.price * item.quantity;
            itemsContainer.addView(createItemRow(item));
        }

        double delivery = subtotal == 0 ? 0 : 30.0;
        subtotalText.setText(String.format(Locale.getDefault(), "Cart Total                         ₹%.2f", subtotal));
        deliveryText.setText(String.format(Locale.getDefault(), "Delivery Charge                   ₹%.2f", delivery));
        totalText.setText(String.format(Locale.getDefault(), "Grand Total                       ₹%.2f", subtotal + delivery));
    }

    private View createItemRow(DatabaseHelper.CartRow item) {
        View card = getLayoutInflater().inflate(R.layout.item_cart, itemsContainer, false);

        ImageView image = card.findViewById(R.id.ivFoodThumb);
        TextView name = card.findViewById(R.id.tvFoodName);
        TextView price = card.findViewById(R.id.tvFoodPrice);
        TextView minus = card.findViewById(R.id.btnMinus);
        TextView qty = card.findViewById(R.id.tvQuantity);
        TextView plus = card.findViewById(R.id.btnPlus);
        TextView lineTotal = card.findViewById(R.id.tvItemTotal);

        name.setText(item.name);
        price.setText(String.format(Locale.getDefault(), "₹%.2f each", item.price));
        qty.setText(String.valueOf(item.quantity));
        lineTotal.setText(String.format(Locale.getDefault(), "₹%.2f", item.price * item.quantity));

        int res = getResources().getIdentifier(item.image, "drawable", getPackageName());
        if (res != 0) {
            image.setImageResource(res);
        } else {
            image.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        minus.setOnClickListener(v -> {
            if (item.quantity <= 1) {
                db.removeCartItem(userId, item.foodId);
            } else {
                db.setCartItemQuantity(userId, item.foodId, item.quantity - 1);
            }
            loadCart();
        });

        plus.setOnClickListener(v -> {
            if (item.quantity < 99) {
                db.setCartItemQuantity(userId, item.foodId, item.quantity + 1);
            } else {
                Toast.makeText(this, "Maximum quantity is 99", Toast.LENGTH_SHORT).show();
            }
            loadCart();
        });

        card.setOnLongClickListener(v -> {
            db.removeCartItem(userId, item.foodId);
            Toast.makeText(this, item.name + " removed", Toast.LENGTH_SHORT).show();
            loadCart();
            return true;
        });

        return card;
    }
}
