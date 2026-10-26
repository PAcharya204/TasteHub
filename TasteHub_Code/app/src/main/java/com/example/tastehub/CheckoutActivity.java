package com.example.tastehub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tastehub.database.DatabaseHelper;
import java.util.ArrayList;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {

    private static final int ID_COD  = 1001;
    private static final int ID_UPI  = 1002;
    private static final int ID_CARD = 1003;

    private int selectedPayment = ID_COD;
    private int userId = -1;
    private int addressId = -1;
    private DatabaseHelper db;
    private ArrayList<DatabaseHelper.CartRow> cartItems = new ArrayList<>();

    private LinearLayout panelUpi, panelCard;
    private EditText etUpiId, etCardNumber, etCvv, etExpiry;
    private double grandTotal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        SharedPreferences session = getSharedPreferences("TasteHubSession", MODE_PRIVATE);
        userId    = session.getInt("user_id", -1);
        addressId = getIntent().getIntExtra("address_id", -1);
        db = new DatabaseHelper(this);

        // Resolve address
        String userName = session.getString("user_name", "User");
        String addressLine = "";
        for (DatabaseHelper.AddressRow a : db.getAddresses(userId)) {
            if (a.addressId == addressId) {
                addressLine = a.getFullAddress();
                break;
            }
        }
        if (addressLine.isEmpty()) {
            int defaultAddressId = session.getInt("default_address_id", -1);
            ArrayList<DatabaseHelper.AddressRow> allAddresses = db.getAddresses(userId);
            for (DatabaseHelper.AddressRow a : allAddresses) {
                if (a.addressId == defaultAddressId) {
                    addressId = a.addressId;
                    addressLine = a.getFullAddress();
                    break;
                }
            }
            if (addressLine.isEmpty() && !allAddresses.isEmpty()) {
                addressId = allAddresses.get(0).addressId;
                addressLine = allAddresses.get(0).getFullAddress();
            }
        }

        // Resolve totals
        cartItems = db.getCartItems(userId);
        double subtotal = 0;
        for (DatabaseHelper.CartRow item : cartItems) {
            subtotal += item.price * item.quantity;
        }
        double deliveryCharge = 30.0;
        grandTotal = subtotal + deliveryCharge;

        initViews(userName, addressLine, subtotal, deliveryCharge);
    }

    private void initViews(String userName, String addressLine, double subtotal, double deliveryCharge) {
        // Address card
        TextView tvUserName = findViewById(R.id.tvUserName);
        TextView tvAddressLine = findViewById(R.id.tvAddressLine);
        tvUserName.setText(userName != null && !userName.isEmpty() ? userName : "User");
        tvAddressLine.setText(!addressLine.isEmpty() ? addressLine : "No address specified");

        // Order delivery card
        TextView tvFoodSubtotal = findViewById(R.id.tvFoodSubtotal);
        TextView tvDeliveryCharge = findViewById(R.id.tvDeliveryCharge);
        TextView tvOrderGrandTotal = findViewById(R.id.tvOrderGrandTotal);
        tvFoodSubtotal.setText(String.format(Locale.getDefault(), "₹%.2f", subtotal));
        tvDeliveryCharge.setText(String.format(Locale.getDefault(), "₹%.2f", deliveryCharge));
        tvOrderGrandTotal.setText(String.format(Locale.getDefault(), "₹%.2f", grandTotal));

        // Payment panels & fields
        panelUpi = findViewById(R.id.panelUpi);
        panelCard = findViewById(R.id.panelCard);
        etUpiId = findViewById(R.id.etUpiId);
        etCardNumber = findViewById(R.id.etCardNumber);
        etExpiry = findViewById(R.id.etExpiry);
        etCvv = findViewById(R.id.etCvv);

        etCardNumber.addTextChangedListener(new CardNumberWatcher(etCardNumber));
        etExpiry.addTextChangedListener(new ExpiryWatcher(etExpiry));

        RadioGroup rgPayment = findViewById(R.id.rgPayment);
        RadioButton rbCod = findViewById(R.id.rbCod);
        RadioButton rbUpi = findViewById(R.id.rbUpi);
        RadioButton rbCard = findViewById(R.id.rbCard);

        rgPayment.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbCod) {
                selectedPayment = ID_COD;
                panelUpi.setVisibility(View.GONE);
                panelCard.setVisibility(View.GONE);
            } else if (checkedId == R.id.rbUpi) {
                selectedPayment = ID_UPI;
                panelUpi.setVisibility(View.VISIBLE);
                panelCard.setVisibility(View.GONE);
            } else if (checkedId == R.id.rbCard) {
                selectedPayment = ID_CARD;
                panelUpi.setVisibility(View.GONE);
                panelCard.setVisibility(View.VISIBLE);
            }
        });

        // Action buttons
        Button btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        btnPlaceOrder.setText(String.format(Locale.getDefault(), "PLACE ORDER  ·  ₹%.2f", grandTotal));
        btnPlaceOrder.setOnClickListener(v -> handlePlaceOrder());

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Bottom nav
        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        findViewById(R.id.navOrders).setOnClickListener(v ->
                startActivity(new Intent(this, OrderHistoryActivity.class)));
        findViewById(R.id.navProfile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void handlePlaceOrder() {
        if (selectedPayment == ID_UPI) {
            String id = etUpiId.getText().toString().trim();
            if (id.length() < 5 || !id.endsWith("@upi")) {
                Toast.makeText(this, "Enter a valid UPI ID ending with @upi", Toast.LENGTH_LONG).show();
                return;
            }
        } else if (selectedPayment == ID_CARD) {
            String num    = etCardNumber.getText().toString().replaceAll("\\s", "");
            String expiry = etExpiry.getText().toString().trim();
            String cvv    = etCvv.getText().toString().trim();
            if (num.length() != 16) {
                Toast.makeText(this, "Enter a valid 16-digit card number", Toast.LENGTH_LONG).show();
                return;
            }
            if (!isValidExpiry(expiry)) {
                Toast.makeText(this, "Enter a valid expiry date (MM/YY)", Toast.LENGTH_LONG).show();
                return;
            }
            if (cvv.length() != 3) {
                Toast.makeText(this, "Enter a valid 3-digit CVV", Toast.LENGTH_LONG).show();
                return;
            }
        }
        String paymentMethod = "Cash on Delivery";
        if (selectedPayment == ID_UPI) {
            paymentMethod = "UPI";
        } else if (selectedPayment == ID_CARD) {
            paymentMethod = "Credit / Debit Card";
        }

        // Save order and clear cart in database
        db.placeOrder(userId, addressId, grandTotal, paymentMethod, cartItems);
        db.clearCart(userId);

        // All validations passed — navigate to confirmation screen
        Intent intent = new Intent(this, OrderConfirmationActivity.class);
        intent.putExtra("user_id", userId);
        intent.putExtra("ordered_items", cartItems);
        startActivity(intent);
        finish();
    }

    private boolean isValidExpiry(String exp) {
        if (exp == null || exp.length() != 5 || exp.charAt(2) != '/') return false;
        try {
            int m = Integer.parseInt(exp.substring(0, 2));
            int y = Integer.parseInt(exp.substring(3));
            return m >= 1 && m <= 12 && y >= 24;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // ── TextWatcher: auto-space card number every 4 digits ───────────────────
    private static class CardNumberWatcher implements TextWatcher {
        private final EditText et;
        private boolean editing = false;
        CardNumberWatcher(EditText et) { this.et = et; }
        @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
        @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
        @Override public void afterTextChanged(Editable s) {
            if (editing) return; editing = true;
            String d = s.toString().replaceAll("\\s", "");
            if (d.length() > 16) d = d.substring(0, 16);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < d.length(); i++) {
                if (i > 0 && i % 4 == 0) sb.append(' ');
                sb.append(d.charAt(i));
            }
            et.setText(sb.toString());
            et.setSelection(sb.length());
            editing = false;
        }
    }

    // ── TextWatcher: auto-slash expiry after 2 digits ─────────────────────────
    private static class ExpiryWatcher implements TextWatcher {
        private final EditText et;
        private boolean editing = false;
        ExpiryWatcher(EditText et) { this.et = et; }
        @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
        @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
        @Override public void afterTextChanged(Editable s) {
            if (editing) return; editing = true;
            String raw = s.toString().replace("/", "");
            if (raw.length() > 4) raw = raw.substring(0, 4);
            String f = raw.length() >= 3 ? raw.substring(0, 2) + "/" + raw.substring(2) : raw;
            et.setText(f);
            et.setSelection(f.length());
            editing = false;
        }
    }
}
