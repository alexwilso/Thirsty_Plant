package com.example.thirstyplant.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.thirstyplant.R;
import com.example.thirstyplant.model.Plant;
import com.example.thirstyplant.io.DataBaseHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

public class AddPlant extends AppCompatActivity {
    private EditText plantName, plantNickName, plantLocation, plantDate, plantInstructions;
    private Button addPlantButton, addPhotoButton;
    private ImageView plantPhoto;
    private final int requestImage = 101;
    private String plantPath = "app/src/main/res/drawable/plant.png";
    OutputStream outputStream;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plant);
        // References to controls on layout
        plantName = findViewById(R.id.plantKind);
        plantNickName = findViewById(R.id.plantNickName);
        plantLocation = findViewById(R.id.plantLocation);
        plantDate = findViewById(R.id.plantDate);
        plantInstructions = findViewById(R.id.plantInstructions);
        addPhotoButton= findViewById(R.id.addPhoto);
        plantPhoto = findViewById(R.id.plantPhoto);
        addPlantButton = findViewById(R.id.createPlant);

        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto(v);
            }
        });

        plantDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDate(plantDate);
            }
        });

        addPlantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPlant();
            }
        });
    }

    /**
     * Adds Plant to database
     */
    private void addPlant() {
        Plant plant;
        if (!missingData()) {
            try {
                plant = new Plant(-1, plantName.getText().toString(), plantNickName.getText().toString(),
                        plantLocation.getText().toString(), plantDate.getText().toString(),
                        plantInstructions.getText().toString(), plantPath,false,
                        false);
                Toast.makeText(AddPlant.this, plant.toString(), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                plant = new Plant(-1, "Error", "Error", "Error",
                        "Error", "Error", "Error", false, false);
                Toast.makeText(AddPlant.this, "Error creating plant", Toast.LENGTH_LONG).show();
            }
            DataBaseHelper dataBaseHelper = new DataBaseHelper(AddPlant.this);
            boolean success = dataBaseHelper.addPlant(plant);
            if (success) {
                Toast.makeText(AddPlant.this, "True", Toast.LENGTH_LONG).show();
                waterFertilize();
            }
        }
    }

    /**
     * Checks if user missing data, throws error in text box
     */
    private boolean missingData(){
        if (plantName.getText().toString().isEmpty()) {
            plantName.setError("Please enter a name for your plant");
            plantName.requestFocus();
            return true;
        }
        else if (plantLocation.getText().toString().isEmpty()) {
            plantLocation.setError("Please enter the location of your plant");
            plantLocation.requestFocus();
            return true;
        }
        else if (plantDate.getText().toString().isEmpty()){
            plantDate.setError("Please enter the date you acquired your plant");
            plantDate.requestFocus();
            return true;
        }
        return false;
    }

    /**
     * Starts new camera intent
     */
    private void takePhoto(View view){
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture, requestImage);
    }

    /**
     * Adds taken photo to image view
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == requestImage && resultCode == RESULT_OK) {
            Bitmap bitmap = (Bitmap)data.getExtras().get("data");
            plantPhoto.setImageBitmap(bitmap);
            try {
                assert bitmap != null;
                storePhoto(bitmap, plantName.getText().toString() + "_" + plantNickName.getText().toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * * Adds photo to internal storage @ /data/data/com.example.thirstyplant.controller/
     * app_plantPhotos/pathName.jpg
     */
    private void storePhoto(Bitmap bitmap, String pathName) throws IOException {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File directory = contextWrapper.getDir("plantPhotos", Context.MODE_PRIVATE);
        File myPath = new File(directory, pathName + ".jpg");
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(myPath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            plantPath = "/data/data/com.example.thirstyplant.controller/app_plantPhotos/" + pathName + ".jpg";

        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Uses datepickerdialog to set desired date. Sets date in editText
     */
    private void setDate(final EditText editText){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(AddPlant.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String date = month + "/" + dayOfMonth + "/" + year;
                editText.setText(date);
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    /**
     * Takes user to set water and fertilizing screen
     */
    private void waterFertilize(){
        Intent intent = new Intent(AddPlant.this, WaterSchedule.class);
        startActivity(intent);
    }
}