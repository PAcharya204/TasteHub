package com.example.tastehub.model;

public class Food {

    private int foodId;
    private String name;
    private String category;
    private String description;
    private double price;
    private double rating;
    private int isVeg;
    private String image;

    public Food(int foodId, String name, String category,
                String description, double price,
                double rating, int isVeg, String image) {

        this.foodId = foodId;
        this.name = name;
        this.category = category;
        this.description = description;
        this.price = price;
        this.rating = rating;
        this.isVeg = isVeg;
        this.image = image;
    }

    public int getFoodId() {
        return foodId;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public double getRating() {
        return rating;
    }

    public int getIsVeg() {
        return isVeg;
    }

    public String getImage() {
        return image;
    }
}