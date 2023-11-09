package com.example.assignment2;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView displayCoordinates, operationStatus;
    EditText addressField, nameField;
    Button searchAddress,removeLocation,updateLocation,addCoordinates;
    DatabaseHelper myDB;
    String address;
    Geocoder geocoder;
    List<Address> addresses;

    double latitude, longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));
        displayCoordinates = findViewById(R.id.displayCoordinates);
        addressField = findViewById(R.id.addressFieldET);
        searchAddress = findViewById(R.id.searchAddress);
        removeLocation = findViewById(R.id.removeLocation);
        updateLocation = findViewById(R.id.updateLocation);
        addCoordinates = findViewById(R.id.addLocation);
        nameField = findViewById(R.id.nameFieldET);
        operationStatus = findViewById(R.id.operationStatus);

        //calls the function to populate DB with locations.txt values
        myDB = new DatabaseHelper(this);
        myDB.populateDBFromTxt(this);
        myDB.populateAddressList(); //function to populate the arraylist with addresses of the coordinates in DB
        myDB.printAddressListToLog();       //prints address list to log.. used for debugging


        //initializing geocoder which will be used to find coordinates of entered address
        geocoder = new Geocoder(this);

        //takes the address entered by user and sees if it matches any of the addresses in the arraylist containing addresses of coordinates in DB.
        //if it matches, then it will display the coordinates of the address, if not then it will not display a msg saying address not in DB

        searchAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                address = addressField.getText().toString();    //gets the address entered by user and sets it to string

                boolean confirmAddress = myDB.validateAddress(address);

                if(confirmAddress) {
                     userAddressCoordinates();      //calling function to get coordinates from address user has entered
                } else {
                    displayCoordinates.setText("Address is not in DB.");
                }

            }
        });

        //onclick to call the remove function once the user clicks it
        removeLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String locationName = nameField.getText().toString();
                if(!locationName.isEmpty()){
                    myDB.removeLocation(locationName);
                    operationStatus.setText(locationName + "has been removed from DB");
                } else{
                    Toast.makeText(v.getContext(),"Please enter a valid name.",Toast.LENGTH_SHORT).show();
                }

            }
        });

        //onclick to call update function once user clicks it
        updateLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String locationName = nameField.getText().toString();
                address = addressField.getText().toString();
                if(!locationName.isEmpty() && !address.isEmpty()){
                    userAddressCoordinates(); //gets coordinates of the new address
                    myDB.updateLocation(locationName,latitude,longitude);
                    operationStatus.setText(locationName + " has been updated!");

                } else {
                    Toast.makeText(v.getContext(),"Verify name and address.",Toast.LENGTH_SHORT).show();
                }

            }
        });

        //function to add a location to the database. Take the name and address and convert address to latitude and longitude
        //then add the name, latitude, and longitude to the database
        addCoordinates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String locationName = nameField.getText().toString();       //gets the name of the location user has entered to add
                address = addressField.getText().toString();        //gets address that the user has entered
                if(!locationName.isEmpty() && !address.isEmpty()){
                    userAddressCoordinates();       //gets coordinates of the address user has entered
                    //using addLocation function on DatabaseHelper to add location name and coordinates
                    myDB.addLocation(locationName,String.valueOf(latitude), String.valueOf(longitude));
                    operationStatus.setText(locationName + " has been added to DB");
                } else{
                    Toast.makeText(v.getContext(),"Verify the address.",Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    //function that gets the coordinates from the address that the user has entered.
    public void userAddressCoordinates() {
        try {
            addresses = geocoder.getFromLocationName(address,1);
            if (!addresses.isEmpty()) {
                latitude = addresses.get(0).getLatitude();
                longitude = addresses.get(0).getLongitude();
                displayCoordinates.setText("Latitude: " + latitude + ", and Longitude: " + longitude);
            } else {
                displayCoordinates.setText("Coordinates not found.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}