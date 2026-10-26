package com.example.tastehub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.tastehub.database.DatabaseHelper;
import java.util.ArrayList;

public class AddressActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private int userId;
    private int selectedAddressId = -1;
    private RadioGroup rgAddresses;
    private TextView emptyText;
    private ArrayList<DatabaseHelper.AddressRow> addresses = new ArrayList<>();
    private boolean isFromCart = false;
    private SharedPreferences session;

    private int dp(float v) {
        return (int) (v * getResources().getDisplayMetrics().density + .5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        session = getSharedPreferences("TasteHubSession", MODE_PRIVATE);
        userId = session.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        isFromCart = getIntent().getBooleanExtra("from_cart", false);

        db = new DatabaseHelper(this);
        initViews();
        loadAddresses();
    }

    private void initViews() {
        TextView btnAddressBack = findViewById(R.id.btnAddressBack);
        btnAddressBack.setOnClickListener(v -> finish());

        emptyText = findViewById(R.id.tvEmptyAddress);
        rgAddresses = findViewById(R.id.rgAddresses);

        TextView tvTitle = findViewById(R.id.tvAddressTitle);
        TextView tvSubtitle = findViewById(R.id.tvAddressSubtitle);
        Button btnUseAddress = findViewById(R.id.btnUseAddress);

        if (isFromCart) {
            View headerBrand = findViewById(R.id.headerBrand);
            if (headerBrand != null) headerBrand.setVisibility(View.GONE);
            View addressHeader = findViewById(R.id.addressHeader);
            if (addressHeader != null) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) addressHeader.getLayoutParams();
                lp.height = dp(64);
                addressHeader.setLayoutParams(lp);
            }
            btnAddressBack.setText("‹   Select Address");
            if (tvTitle != null) tvTitle.setText("Delivery Address");
            if (tvSubtitle != null) tvSubtitle.setText("Choose where you would like your order delivered.");
            btnUseAddress.setText("USE THIS ADDRESS");
        } else {
            btnAddressBack.setText("‹   My Addresses");
            if (tvTitle != null) tvTitle.setText("Saved Addresses");
            if (tvSubtitle != null) tvSubtitle.setText("Manage your addresses or set a default address.");
            btnUseAddress.setText("SET AS DEFAULT ADDRESS");
        }

        Button btnAddAddress = findViewById(R.id.btnAddAddress);
        btnAddAddress.setOnClickListener(v -> showAddAddressDialog());

        btnUseAddress.setOnClickListener(v -> {
            if (selectedAddressId == -1) {
                Toast.makeText(this, "Please select or add an address", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isFromCart) {
                Intent intent = new Intent(this, CheckoutActivity.class);
                intent.putExtra("address_id", selectedAddressId);
                startActivity(intent);
            } else {
                session.edit().putInt("default_address_id", selectedAddressId).apply();
                Toast.makeText(this, "Default address set successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // Bottom Navigation
        View navHome = findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        View navOrders = findViewById(R.id.navOrders);
        if (navOrders != null) {
            navOrders.setOnClickListener(v -> {
                startActivity(new Intent(this, OrderHistoryActivity.class));
                finish();
            });
        }

        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> finish());
        }
    }

    private void loadAddresses() {
        addresses = db.getAddresses(userId);
        rgAddresses.removeAllViews();
        emptyText.setVisibility(addresses.isEmpty() ? View.VISIBLE : View.GONE);

        int defaultAddressId = session.getInt("default_address_id", -1);
        int teal = ContextCompat.getColor(this, R.color.primary_teal);

        for (DatabaseHelper.AddressRow a : addresses) {
            RadioButton rb = new RadioButton(this);
            String addressText = a.getFullAddress();
            if (a.addressId == defaultAddressId) {
                addressText += "  [Default]";
            }
            rb.setText(addressText);
            rb.setTextSize(15);
            rb.setPadding(dp(8), dp(8), dp(8), dp(8));
            rb.setId(a.addressId);
            rb.setButtonTintList(ColorStateList.valueOf(teal));
            rgAddresses.addView(rb, new RadioGroup.LayoutParams(-1, -2));
            if (selectedAddressId == a.addressId) {
                rb.setChecked(true);
            }
        }

        rgAddresses.setOnCheckedChangeListener((g, id) -> selectedAddressId = id);

        if (selectedAddressId == -1 && !addresses.isEmpty()) {
            if (defaultAddressId != -1) {
                for (DatabaseHelper.AddressRow a : addresses) {
                    if (a.addressId == defaultAddressId) {
                        selectedAddressId = defaultAddressId;
                        break;
                    }
                }
            }
            if (selectedAddressId == -1) {
                selectedAddressId = addresses.get(0).addressId;
            }
            rgAddresses.check(selectedAddressId);
        }
    }

    private void showAddAddressDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_address, null);
        EditText address = dialogView.findViewById(R.id.etAddress);
        EditText city = dialogView.findViewById(R.id.etCity);
        EditText state = dialogView.findViewById(R.id.etState);
        EditText pin = dialogView.findViewById(R.id.etPincode);

        new AlertDialog.Builder(this)
                .setTitle("Add New Address")
                .setView(dialogView)
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Save Address", (d, w) -> {
                    String a = address.getText().toString().trim();
                    String c = city.getText().toString().trim();
                    String st = state.getText().toString().trim();
                    String p = pin.getText().toString().trim();

                    if (a.isEmpty() || c.isEmpty() || st.isEmpty() || p.length() != 6) {
                        Toast.makeText(this, "Enter all details and a valid 6-digit PIN code", Toast.LENGTH_LONG).show();
                        return;
                    }
                    long id = db.addAddress(userId, a, c, st, p);
                    if (id == -1) {
                        Toast.makeText(this, "Could not save address", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    selectedAddressId = (int) id;
                    loadAddresses();
                    Toast.makeText(this, "Address saved", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}
