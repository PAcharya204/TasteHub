package com.example.tastehub.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.tastehub.model.Food;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    // =====================================================
    // DATABASE DETAILS
    // =====================================================

    private static final String DATABASE_NAME = "tastehub.db";

    /*
     * Increase this number whenever the database structure
     * is changed.
     */
    private static final int DATABASE_VERSION = 4;

    private Context context;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public DatabaseHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        this.context = context;
    }


    // =====================================================
    // CREATE DATABASE
    // =====================================================

    @Override
    public void onCreate(SQLiteDatabase db) {


        // =================================================
        // FOOD TABLE
        // =================================================

        String foodTable = "CREATE TABLE food (" +
                "food_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "category TEXT NOT NULL, " +
                "description TEXT, " +
                "price REAL, " +
                "rating REAL, " +
                "is_veg INTEGER, " +
                "image TEXT" +
                ")";

        db.execSQL(foodTable);


        // =================================================
        // USERS TABLE
        // =================================================

        String userTable = "CREATE TABLE users (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "mobile TEXT UNIQUE, " +
                "email TEXT, " +
                "password TEXT" +
                ")";

        db.execSQL(userTable);


        // =================================================
        // ADDRESS TABLE
        // =================================================

        String addressTable = "CREATE TABLE addresses (" +
                "address_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "address TEXT, " +
                "city TEXT, " +
                "state TEXT, " +
                "pincode TEXT" +
                ")";

        db.execSQL(addressTable);


        // =================================================
        // CART TABLE
        // =================================================

        String cartTable = "CREATE TABLE cart (" +
                "cart_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "food_id INTEGER, " +
                "quantity INTEGER" +
                ")";

        db.execSQL(cartTable);


        // =================================================
        // ORDERS TABLE
        // =================================================

        String orderTable = "CREATE TABLE orders (" +
                "order_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "address_id INTEGER, " +
                "total_amount REAL, " +
                "payment_method TEXT, " +
                "order_date TEXT, " +
                "status TEXT" +
                ")";

        db.execSQL(orderTable);


        // =================================================
        // ORDER ITEMS TABLE
        // =================================================

        String orderItemsTable = "CREATE TABLE order_items (" +
                "order_item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "order_id INTEGER, " +
                "food_id INTEGER, " +
                "quantity INTEGER, " +
                "price REAL" +
                ")";

        db.execSQL(orderItemsTable);


        // =================================================
        // LOAD FOOD DATA FROM CSV
        // =================================================

        readFoodFromCSV(db);


        // =================================================
        // TEST USER
        // =================================================

        ContentValues user = new ContentValues();

        user.put("name", "Test User");
        user.put("mobile", "9876543210");
        user.put("email", "a@gmail.com");
        user.put("password", "123456");

        db.insert("users", null, user);
    }


    // =====================================================
    // READ FOOD DATA FROM CSV
    // =====================================================

    private void readFoodFromCSV(SQLiteDatabase db) {

        try {

            // Open foods.csv from assets
            InputStream inputStream =
                    context.getAssets().open("foods.csv");

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(inputStream)
                    );


            // Skip first line because it is the CSV header
            String line = reader.readLine();


            // Read remaining food records
            while ((line = reader.readLine()) != null) {

                /*
                 * Current CSV contains 7 columns:
                 *
                 * name
                 * category
                 * description
                 * price
                 * rating
                 * is_veg
                 * image
                 */

                String[] data = line.split(",");


                if (data.length >= 7) {

                    ContentValues values = new ContentValues();

                    values.put("name", data[0]);
                    values.put("category", data[1]);
                    values.put("description", data[2]);

                    values.put(
                            "price",
                            Double.parseDouble(data[3])
                    );

                    values.put(
                            "rating",
                            Double.parseDouble(data[4])
                    );

                    values.put(
                            "is_veg",
                            Integer.parseInt(data[5])
                    );

                    values.put("image", data[6]);


                    // Insert food into database
                    db.insert(
                            "food",
                            null,
                            values
                    );
                }
            }


            // Close reader
            reader.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // =====================================================
    // GET ALL FOODS
    // =====================================================

    public ArrayList<Food> getAllFoods() {

        ArrayList<Food> foodList = new ArrayList<>();

        SQLiteDatabase db =
                this.getReadableDatabase();


        // Get all food records
        Cursor cursor =
                db.rawQuery(
                        "SELECT * FROM food",
                        null
                );


        if (cursor.moveToFirst()) {

            do {

                int foodId =
                        cursor.getInt(
                                cursor.getColumnIndexOrThrow(
                                        "food_id"
                                )
                        );


                String name =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        "name"
                                )
                        );


                String category =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        "category"
                                )
                        );


                String description =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        "description"
                                )
                        );


                double price =
                        cursor.getDouble(
                                cursor.getColumnIndexOrThrow(
                                        "price"
                                )
                        );


                double rating =
                        cursor.getDouble(
                                cursor.getColumnIndexOrThrow(
                                        "rating"
                                )
                        );


                int isVeg =
                        cursor.getInt(
                                cursor.getColumnIndexOrThrow(
                                        "is_veg"
                                )
                        );


                String image =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        "image"
                                )
                        );


                // Create Food object
                Food food =
                        new Food(
                                foodId,
                                name,
                                category,
                                description,
                                price,
                                rating,
                                isVeg,
                                image
                        );


                // Add to list
                foodList.add(food);


            } while (cursor.moveToNext());
        }


        // Close cursor
        cursor.close();


        return foodList;
    }


    // =====================================================
    // GET ONE FOOD BY FOOD ID
    // =====================================================
    /*
     * This method is used by FoodDetailsActivity.
     *
     * Example:
     *
     * User clicks Farmhouse Pizza
     *        ↓
     * food_id = 2 is sent through Intent
     *        ↓
     * getFoodById(2)
     *        ↓
     * Farmhouse Pizza details are returned
     */

    public Food getFoodById(int foodId) {

        SQLiteDatabase db =
                this.getReadableDatabase();


        Cursor cursor =
                db.rawQuery(
                        "SELECT * FROM food WHERE food_id = ?",
                        new String[]{
                                String.valueOf(foodId)
                        }
                );


        Food food = null;


        if (cursor.moveToFirst()) {

            int id =
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                    "food_id"
                            )
                    );


            String name =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "name"
                            )
                    );


            String category =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "category"
                            )
                    );


            String description =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "description"
                            )
                    );


            double price =
                    cursor.getDouble(
                            cursor.getColumnIndexOrThrow(
                                    "price"
                            )
                    );


            double rating =
                    cursor.getDouble(
                            cursor.getColumnIndexOrThrow(
                                    "rating"
                            )
                    );


            int isVeg =
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                    "is_veg"
                            )
                    );


            String image =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "image"
                            )
                    );


            // Create Food object
            food =
                    new Food(
                            id,
                            name,
                            category,
                            description,
                            price,
                            rating,
                            isVeg,
                            image
                    );
        }


        // Close cursor
        cursor.close();


        return food;
    }


    // =====================================================
    // ADD FOOD TO CART
    // =====================================================
    /*
     * This method is USER-SPECIFIC.
     *
     * userId tells us whose cart it is.
     * foodId tells us which food is being added.
     *
     * If the food already exists in that user's cart,
     * quantity is increased.
     *
     * Otherwise a new cart record is created.
     */

    public void addToCart(
            int userId,
            int foodId,
            int quantity) {


        SQLiteDatabase db =
                this.getWritableDatabase();


        // Check whether this food already exists
        // in this particular user's cart
        Cursor cursor =
                db.rawQuery(
                        "SELECT quantity FROM cart " +
                                "WHERE user_id = ? AND food_id = ?",
                        new String[]{
                                String.valueOf(userId),
                                String.valueOf(foodId)
                        }
                );


        if (cursor.moveToFirst()) {

            // Food already exists
            int oldQuantity =
                    cursor.getInt(0);


            int newQuantity =
                    oldQuantity + quantity;


            ContentValues values =
                    new ContentValues();

            values.put(
                    "quantity",
                    newQuantity
            );


            db.update(
                    "cart",
                    values,
                    "user_id = ? AND food_id = ?",
                    new String[]{
                            String.valueOf(userId),
                            String.valueOf(foodId)
                    }
            );

        } else {

            // Food does not exist yet
            // Create a new cart record

            ContentValues values =
                    new ContentValues();

            values.put(
                    "user_id",
                    userId
            );

            values.put(
                    "food_id",
                    foodId
            );

            values.put(
                    "quantity",
                    quantity
            );


            db.insert(
                    "cart",
                    null,
                    values
            );
        }


        cursor.close();
    }



    // =====================================================
    // CART OPERATIONS
    // =====================================================

    public static class CartRow implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        public final int foodId;
        public final String name;
        public final String image;
        public final double price;
        public final int quantity;

        public CartRow(int foodId, String name, String image, double price, int quantity) {
            this.foodId = foodId;
            this.name = name;
            this.image = image;
            this.price = price;
            this.quantity = quantity;
        }
    }

    public ArrayList<CartRow> getCartItems(int userId) {
        ArrayList<CartRow> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT f.food_id, f.name, f.image, f.price, c.quantity " +
                "FROM cart c INNER JOIN food f ON c.food_id = f.food_id " +
                "WHERE c.user_id = ? ORDER BY c.cart_id DESC",
                new String[]{String.valueOf(userId)});
        try {
            while (cursor.moveToNext()) {
                items.add(new CartRow(cursor.getInt(0), cursor.getString(1),
                        cursor.getString(2), cursor.getDouble(3), cursor.getInt(4)));
            }
        } finally {
            cursor.close();
        }
        return items;
    }

    public void setCartItemQuantity(int userId, int foodId, int quantity) {
        if (quantity <= 0) {
            removeCartItem(userId, foodId);
            return;
        }
        ContentValues values = new ContentValues();
        values.put("quantity", quantity);
        getWritableDatabase().update("cart", values, "user_id = ? AND food_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(foodId)});
    }

    public void removeCartItem(int userId, int foodId) {
        getWritableDatabase().delete("cart", "user_id = ? AND food_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(foodId)});
    }

    public void clearCart(int userId) {
        getWritableDatabase().delete("cart", "user_id = ?",
                new String[]{String.valueOf(userId)});
    }

    public long placeOrder(int userId, int addressId, double totalAmount, String paymentMethod, ArrayList<CartRow> items) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues orderValues = new ContentValues();
        orderValues.put("user_id", userId);
        orderValues.put("address_id", addressId);
        orderValues.put("total_amount", totalAmount);
        orderValues.put("payment_method", paymentMethod);
        orderValues.put("order_date", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date()));
        orderValues.put("status", "Confirmed");
        long orderId = db.insert("orders", null, orderValues);

        if (orderId != -1 && items != null) {
            for (CartRow item : items) {
                ContentValues itemValues = new ContentValues();
                itemValues.put("order_id", orderId);
                itemValues.put("food_id", item.foodId);
                itemValues.put("quantity", item.quantity);
                itemValues.put("price", item.price);
                db.insert("order_items", null, itemValues);
            }
        }
        clearCart(userId);
        return orderId;
    }


    // =====================================================
    // ADDRESS OPERATIONS
    // =====================================================
    public static class AddressRow {
        public int addressId;
        public String address, city, state, pincode;
        public AddressRow(int id, String address, String city, String state, String pincode) {
            this.addressId = id; this.address = address; this.city = city;
            this.state = state; this.pincode = pincode;
        }
        public String getFullAddress() {
            return address + ", " + city + ", " + state + " - " + pincode;
        }
    }

    public ArrayList<AddressRow> getAddresses(int userId) {
        ArrayList<AddressRow> rows = new ArrayList<>();
        Cursor c = getReadableDatabase().query("addresses",
                new String[]{"address_id", "address", "city", "state", "pincode"},
                "user_id = ?", new String[]{String.valueOf(userId)}, null, null, "address_id DESC");
        try {
            while (c.moveToNext()) rows.add(new AddressRow(c.getInt(0), c.getString(1),
                    c.getString(2), c.getString(3), c.getString(4)));
        } finally { c.close(); }
        return rows;
    }

    public long addAddress(int userId, String address, String city, String state, String pincode) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId); values.put("address", address);
        values.put("city", city); values.put("state", state); values.put("pincode", pincode);
        return getWritableDatabase().insert("addresses", null, values);
    }

    // =====================================================
    // DATABASE UPGRADE
    // =====================================================

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion) {


        /*
         * For this college project we are recreating
         * the database whenever the database version changes.
         */

        db.execSQL(
                "DROP TABLE IF EXISTS order_items"
        );

        db.execSQL(
                "DROP TABLE IF EXISTS orders"
        );

        db.execSQL(
                "DROP TABLE IF EXISTS cart"
        );

        db.execSQL(
                "DROP TABLE IF EXISTS addresses"
        );

        db.execSQL(
                "DROP TABLE IF EXISTS users"
        );

        db.execSQL(
                "DROP TABLE IF EXISTS food"
        );


        // Create database again
        onCreate(db);
    }
}