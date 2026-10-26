package com.example.tastehub;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tastehub.database.DatabaseHelper;
import com.example.tastehub.model.Food;

import java.util.Locale;

public class FoodDetailsActivity extends AppCompatActivity {
    private ImageView foodImage;
    private TextView foodName, foodRating, foodPrice, foodDescription, foodVeg;
    private TextView quantityText, decreaseQuantity, increaseQuantity, backButton, wishlistButton;
    private Button addToCartButton;
    private DatabaseHelper databaseHelper;
    private Food food;
    private int quantity = 1;
    private boolean isWishlisted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_details);

        foodImage = findViewById(R.id.foodImage);
        foodName = findViewById(R.id.foodName);
        foodRating = findViewById(R.id.foodRating);
        foodPrice = findViewById(R.id.foodPrice);
        foodDescription = findViewById(R.id.foodDescription);
        foodVeg = findViewById(R.id.foodVeg);
        quantityText = findViewById(R.id.quantityText);
        decreaseQuantity = findViewById(R.id.decreaseQuantity);
        increaseQuantity = findViewById(R.id.increaseQuantity);
        addToCartButton = findViewById(R.id.addToCartButton);
        backButton = findViewById(R.id.backButton);
        wishlistButton = findViewById(R.id.wishlistButton);
        databaseHelper = new DatabaseHelper(this);

        backButton.setOnClickListener(v -> finish());

        int foodId = getIntent().getIntExtra("food_id", -1);
        if (foodId == -1) {
            Toast.makeText(this, "Food not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        food = databaseHelper.getFoodById(foodId);
        if (food == null) {
            Toast.makeText(this, "This food is no longer available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        displayFoodDetails(food);
        updateQuantityAndPrice();

        decreaseQuantity.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateQuantityAndPrice();
            } else {
                Toast.makeText(this, "Minimum quantity is 1", Toast.LENGTH_SHORT).show();
            }
        });
        increaseQuantity.setOnClickListener(v -> {
            if (quantity < 99) {
                quantity++;
                updateQuantityAndPrice();
            } else {
                Toast.makeText(this, "Maximum quantity is 99", Toast.LENGTH_SHORT).show();
            }
        });

        // Wishlist state is stored locally for this device and food item.
        SharedPreferences wishlistPrefs = getSharedPreferences("TasteHubWishlist", MODE_PRIVATE);
        String wishlistKey = "food_" + foodId;
        isWishlisted = wishlistPrefs.getBoolean(wishlistKey, false);
        updateWishlistIcon();
        wishlistButton.setOnClickListener(v -> {
            isWishlisted = !isWishlisted;
            wishlistPrefs.edit().putBoolean(wishlistKey, isWishlisted).apply();
            updateWishlistIcon();
            Toast.makeText(this, isWishlisted ? "Added to wishlist" : "Removed from wishlist", Toast.LENGTH_SHORT).show();
        });

        addToCartButton.setOnClickListener(v -> {
            SharedPreferences preferences = getSharedPreferences("TasteHubSession", MODE_PRIVATE);
            int userId = preferences.getInt("user_id", -1);
            if (userId == -1) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                return;
            }
            databaseHelper.addToCart(userId, food.getFoodId(), quantity);
            Toast.makeText(this, quantity + " × " + food.getName() + " added to cart", Toast.LENGTH_SHORT).show();
        });
    }

    private void displayFoodDetails(Food item) {
        foodName.setText(item.getName());
        foodPrice.setText(String.format(Locale.getDefault(), "₹%.2f", item.getPrice()));
        foodRating.setText(String.format(Locale.getDefault(), "★ %.1f", item.getRating()));
        foodDescription.setText(item.getDescription());
        if (item.getIsVeg() == 1) {
            foodVeg.setText("●  Vegetarian");
            foodVeg.setTextColor(Color.rgb(21, 148, 71));
        } else {
            foodVeg.setText("●  Non-vegetarian");
            foodVeg.setTextColor(Color.rgb(229, 57, 53));
        }
        int imageId = getResources().getIdentifier(item.getImage(), "drawable", getPackageName());
        if (imageId != 0) foodImage.setImageResource(imageId);
    }

    private void updateQuantityAndPrice() {
        quantityText.setText(String.valueOf(quantity));
        addToCartButton.setText(String.format(Locale.getDefault(), "ADD TO CART  ·  ₹%.2f", food.getPrice() * quantity));
    }

    private void updateWishlistIcon() {
        wishlistButton.setText(isWishlisted ? "♥" : "♡");
        wishlistButton.setTextColor(isWishlisted ? Color.rgb(229, 57, 53) : Color.WHITE);
    }
    // XML fallback for the visible back arrow.
    public void onBackArrowClicked(android.view.View view) {
        finish();
    }
}
