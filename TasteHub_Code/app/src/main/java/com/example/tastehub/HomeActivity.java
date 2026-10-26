package com.example.tastehub;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tastehub.adapter.FoodAdapter;
import com.example.tastehub.database.DatabaseHelper;
import com.example.tastehub.model.Food;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    DatabaseHelper databaseHelper;

    RecyclerView recyclerPopular;
    EditText etSearch;

    FoodAdapter adapter;

    ArrayList<Food> allFoods;
    ArrayList<Food> filteredFoods;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);


        // =====================================================
        // DATABASE
        // =====================================================

        databaseHelper = new DatabaseHelper(this);


        // =====================================================
        // FIND VIEWS
        // =====================================================

        recyclerPopular = findViewById(R.id.recyclerPopular);
        etSearch = findViewById(R.id.etSearch);


        // =====================================================
        // GET FOOD FROM DATABASE
        // =====================================================

        allFoods = databaseHelper.getAllFoods();

        filteredFoods = new ArrayList<>(allFoods);


        // =====================================================
        // ADAPTER
        // =====================================================

        adapter = new FoodAdapter(this, filteredFoods);


        recyclerPopular.setLayoutManager(
                new GridLayoutManager(this, 2)
        );

        recyclerPopular.setAdapter(adapter);


        // The main ScrollView handles the scrolling
        recyclerPopular.setNestedScrollingEnabled(false);


        // Set height according to number of products
        setRecyclerViewHeight();


        // =====================================================
        // SEARCH
        // =====================================================

        etSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }


            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {

                String searchText = s.toString()
                        .trim()
                        .toLowerCase();

                filterFoods(searchText);
            }


            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        // =====================================================
        // CATEGORY BUTTONS
        // =====================================================

        findViewById(R.id.categoryPizza)
                .setOnClickListener(v -> {

                    etSearch.setText("Pizza");
                });


        findViewById(R.id.categoryBurger)
                .setOnClickListener(v -> {

                    etSearch.setText("Burger");
                });


        findViewById(R.id.categoryPasta)
                .setOnClickListener(v -> {

                    etSearch.setText("Pasta");
                });


        findViewById(R.id.categorySnacks)
                .setOnClickListener(v -> {
                    etSearch.setText("Snacks");
                });


        findViewById(R.id.categoryBeverages)
                .setOnClickListener(v -> {

                    etSearch.setText("Beverages");
                });


        findViewById(R.id.categoryDesserts)
                .setOnClickListener(v -> {

                    etSearch.setText("Desserts");
                });


        // =====================================================
        // CART BUTTON
        // =====================================================

        findViewById(R.id.ivCart).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, CartActivity.class));
        });


        // =====================================================
        // HOME BUTTON
        // =====================================================

        findViewById(R.id.navHome)
                .setOnClickListener(v -> {

                    // Already on Home page
                });


        // =====================================================
        // ORDERS BUTTON
        // =====================================================

        findViewById(R.id.navOrders)
                .setOnClickListener(v -> {


                      Intent intent = new Intent(
                              HomeActivity.this,
                              OrderHistoryActivity.class
                      );

                      startActivity(intent);


//                    Toast.makeText(
//                            HomeActivity.this,
//                            "Orders page will be added soon",
//                            Toast.LENGTH_SHORT
//                    ).show();
                });


        // =====================================================
        // PROFILE BUTTON
        // =====================================================

        findViewById(R.id.navProfile)
                .setOnClickListener(v -> {

                    /*
                     * PROFILE PAGE IS NOT CREATED YET.
                     *
                     * After creating ProfileActivity.java,
                     * remove the Toast and uncomment:
                     */
                     Intent intent = new Intent(
                              HomeActivity.this,
                              ProfileActivity.class
                      );

                      startActivity(intent);


//                    Toast.makeText(
//                            HomeActivity.this,
//                            "Profile page will be added soon",
//                            Toast.LENGTH_SHORT
//                    ).show();
                });
    }


    // =========================================================
    // SEARCH / FILTER
    // =========================================================

    private void filterFoods(String searchText) {

        filteredFoods.clear();


        // If search box is empty
        if (searchText.isEmpty()) {

            filteredFoods.addAll(allFoods);

        } else {

            ArrayList<Food> otherMatches =
                    new ArrayList<>();


            for (Food food : allFoods) {

                String name =
                        food.getName().toLowerCase();

                String category =
                        food.getCategory().toLowerCase();

                String description =
                        food.getDescription().toLowerCase();


                // Strong name match
                if (name.startsWith(searchText)
                        || name.equals(searchText)) {

                    filteredFoods.add(food);
                }


                // Other matches
                else if (name.contains(searchText)
                        || category.contains(searchText)
                        || description.contains(searchText)) {

                    otherMatches.add(food);
                }
            }


            // Add other matching foods after strong matches
            filteredFoods.addAll(otherMatches);
        }


        // Update screen
        adapter.notifyDataSetChanged();


        // Change height according to results
        setRecyclerViewHeight();
    }


    // =========================================================
    // SET RECYCLER VIEW HEIGHT
    // =========================================================

    private void setRecyclerViewHeight() {

        int numberOfItems = filteredFoods.size();


        // 2 products per row
        int numberOfRows =
                (numberOfItems + 1) / 2;


        // Approximate height of one row
        int rowHeightDp = 195;


        float density =
                getResources()
                        .getDisplayMetrics()
                        .density;


        int height =
                (int) (
                        numberOfRows
                                * rowHeightDp
                                * density
                );


        ViewGroup.LayoutParams params =
                recyclerPopular.getLayoutParams();


        params.height = height;


        recyclerPopular.setLayoutParams(params);
    }
}