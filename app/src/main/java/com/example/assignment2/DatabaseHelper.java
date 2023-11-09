package com.example.assignment2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private List<String> addressList;
    private Geocoder geocoder;
    private static final String DATABASE_NAME = "Location.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "Location";
    private static final String LOCATION_ID = "_id";
    public static final String LOCATION_NAME = "location_name";
    public static final String LOCATION_LATITUDE = "latitude";
    public static final String LOCATION_LONGITUDE = "longitude";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        addressList = new ArrayList<>();
        geocoder = new Geocoder(context, Locale.getDefault());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + "(" + LOCATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                LOCATION_NAME + " TEXT, " + LOCATION_LATITUDE + " TEXT, " + LOCATION_LONGITUDE + " TEXT);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    //function to read the locations.txt while and get the 50 different names,latitudes,longitudes and populate the DB
    public void populateDBFromTxt(Context context){
        SQLiteDatabase db = this.getWritableDatabase();

        //checking the number of rows in the table. if row count is 0 then it will run the if statement. if it is not 0
        //then it will skip this step since the DB already contains info from txt file.
        String query = "SELECT count(*) FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        int row = cursor.getInt(0);
        cursor.close();


        //if the row count is 0, then that means that the information from the txt file has not populated the db yet.
        //it will populate the DB now
        if(row == 0) {
            try {
                InputStream inputStream = context.getAssets().open("locations.txt");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                //while loop to parse through locations.txt and splitting each line into 3 parts which are separated by commas
                while ((line = bufferedReader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 3) {
                        String name = parts[0];         //string to hold name of a location
                        String latitude = parts[1];     //string to hold latitude of a location
                        String longitude = parts[2];    //string to hold longitude of a location

                        //put the values of a location in the database
                        ContentValues values = new ContentValues();
                        values.put(LOCATION_NAME, name);
                        values.put(LOCATION_LATITUDE, latitude);
                        values.put(LOCATION_LONGITUDE, longitude);
                        db.insert(TABLE_NAME, null, values);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            db.close();
        }
    }

    //function to populate the arraylist with addresses of the coordinates in the database.
    //Arraylist is required since our database can only contain id,name,longitude,latitude.
    public void populateAddressList() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {LOCATION_LATITUDE, LOCATION_LONGITUDE};
        Cursor cursor = db.query(TABLE_NAME, columns, null,null,null,null,null);

        if(cursor != null){
            while(cursor.moveToNext()) {
                double latitude = cursor.getDouble(cursor.getColumnIndex(LOCATION_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndex(LOCATION_LONGITUDE));

                String address = CoordinatesToAddress(latitude,longitude);      //calling function that converts coordinates to address
                if(address != null){
                    addressList.add(address);
                }
            }
            cursor.close();
        }
    }

    //function to convert the coordinates into addresses
    private String CoordinatesToAddress(double latitude, double longitude){
        try{
            List<Address> addresses = geocoder.getFromLocation(latitude,longitude,1);
            if(addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getAddressLine(0);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //boolean function to confirm if address entered by user is in the database
    public boolean validateAddress(String address) {
        //this is how the address is stored in the address list : "473 Bathurst St, Toronto, ON M5T 2S9, Canada"
        //if the user enters just the postal code, this loop checks if the postal code is in the full address. If it is
        //then it will show as an address match, if not then it will show that the address is not in DB.
        for (String fullAddress : addressList) {
            if(fullAddress.contains(address)){
                return true;
            }
        }
        return false;
    }


    //function to add a location to the database
    public void addLocation(String name, String latitude, String longitude){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LOCATION_NAME, name);
        values.put(LOCATION_LATITUDE,latitude);
        values.put(LOCATION_LONGITUDE,longitude);
        db.insert(TABLE_NAME,null,values);
        db.close();
    }

    //function to update a preexisting location in the database
    public void updateLocation(String name, double newLatitude, double newLongitude){
        SQLiteDatabase db = this.getWritableDatabase();
        //updating the location based off the name
        ContentValues values = new ContentValues();
        values.put(LOCATION_NAME, name);
        values.put(LOCATION_LATITUDE, String.valueOf(newLatitude));
        values.put(LOCATION_LONGITUDE, String.valueOf(newLongitude));
        db.update(TABLE_NAME, values, LOCATION_NAME + "=?", new String[]{name});
        db.close();
    }

    //function to remove a location from the db
    public void removeLocation(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME,LOCATION_NAME + "=?", new String[]{name});
    }
    //function to see contents of list addresses in the logcat
    public void printAddressListToLog() {
        for (String address : addressList) {
            Log.d("AddressList", address);
        }
    }



}
