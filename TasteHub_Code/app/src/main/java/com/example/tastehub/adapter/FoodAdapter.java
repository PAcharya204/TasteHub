package com.example.tastehub.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tastehub.FoodDetailsActivity;
import com.example.tastehub.R;
import com.example.tastehub.model.Food;

import java.util.ArrayList;

public class FoodAdapter
        extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private Context context;
    private ArrayList<Food> foodList;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public FoodAdapter(
            Context context,
            ArrayList<Food> foodList) {

        this.context = context;
        this.foodList = foodList;
    }


    // =====================================================
    // CREATE VIEW HOLDER
    // =====================================================

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(
                        R.layout.item_food,
                        parent,
                        false
                );

        return new FoodViewHolder(view);
    }


    // =====================================================
    // DISPLAY FOOD DATA
    // =====================================================

    @Override
    public void onBindViewHolder(
            @NonNull FoodViewHolder holder,
            int position) {

        Food food = foodList.get(position);


        // Food name
        holder.foodName.setText(
                food.getName()
        );


        // Food price
        holder.foodPrice.setText(
                "₹" + food.getPrice()
        );


        // Food rating
        holder.foodRating.setText(
                "★ " + food.getRating()
        );


        // Veg / Non-Veg color
        if (food.getIsVeg() == 1) {

            holder.foodVeg.setTextColor(
                    Color.rgb(21, 148, 71)
            );

        } else {

            holder.foodVeg.setTextColor(
                    Color.rgb(229, 57, 53)
            );
        }


        // =================================================
        // FOOD IMAGE
        // =================================================

        String imageName = food.getImage() == null
                ? "" : food.getImage().trim().toLowerCase();
        if (imageName.endsWith(".png") || imageName.endsWith(".jpg")) {
            imageName = imageName.substring(0, imageName.lastIndexOf('.'));
        }
        int imageId = context.getResources().getIdentifier(
                imageName, "drawable", context.getPackageName());
        if (imageId == 0) {
            imageId = R.drawable.img;
        }
        holder.foodImage.setImageResource(imageId);


        // =================================================
        // CLICK FOOD CARD
        // =================================================

        holder.itemView.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            context,
                            FoodDetailsActivity.class
                    );


            // Send the ID of the clicked food
            intent.putExtra(
                    "food_id",
                    food.getFoodId()
            );


            // Open Food Details page
            context.startActivity(intent);
        });
    }


    // =====================================================
    // NUMBER OF FOODS
    // =====================================================

    @Override
    public int getItemCount() {

        return foodList.size();
    }


    // =====================================================
    // VIEW HOLDER
    // =====================================================

    public static class FoodViewHolder
            extends RecyclerView.ViewHolder {

        ImageView foodImage;

        TextView foodName;
        TextView foodPrice;
        TextView foodRating;
        TextView foodVeg;


        public FoodViewHolder(
                @NonNull View itemView) {

            super(itemView);


            foodImage =
                    itemView.findViewById(
                            R.id.foodImage
                    );

            foodName =
                    itemView.findViewById(
                            R.id.foodName
                    );

            foodPrice =
                    itemView.findViewById(
                            R.id.foodPrice
                    );

            foodRating =
                    itemView.findViewById(
                            R.id.foodRating
                    );

            foodVeg =
                    itemView.findViewById(
                            R.id.foodVeg
                    );
        }
    }
}