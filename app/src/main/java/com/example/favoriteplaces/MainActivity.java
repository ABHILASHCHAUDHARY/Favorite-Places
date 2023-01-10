package com.example.favoriteplaces;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;

import static java.util.Arrays.asList;

public class MainActivity extends AppCompatActivity {

    ListView listView;
   static ArrayList<String> arrayList = new ArrayList<String>();
   static ArrayList<LatLng> locations = new ArrayList<LatLng>();
   static ArrayAdapter arrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.favoriteplaces", Context.MODE_PRIVATE);

        ArrayList<String> latitudes = new ArrayList<>();
        ArrayList<String> longitudes = new ArrayList<>();

        arrayList.clear();
        latitudes.clear();
        longitudes.clear();
        locations.clear();;

        try {
            arrayList =(ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("placesArrayList",ObjectSerializer.serialize(new ArrayList<String>())));

            latitudes =(ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("Lats",ObjectSerializer.serialize(new ArrayList<String>())));
            longitudes =(ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("Longs",ObjectSerializer.serialize(new ArrayList<String>())));

        } catch (Exception e) {
            e.printStackTrace();
        }

        if(arrayList.size() > 0 && latitudes.size() >0 && longitudes.size() >0){
            if(arrayList.size() == latitudes.size() && arrayList.size() == longitudes.size()){
                for(int i =0;i<latitudes.size();i++){
                    locations.add(new LatLng(Double.parseDouble(latitudes.get(i)),Double.parseDouble(longitudes.get(i))));
                }
            }

        }else {
            arrayList.add("Add your new Favorite Places");
            locations.add(new LatLng(0,0));
        }




        listView = (ListView) findViewById(R.id.listView);


        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,arrayList);

        listView.setAdapter(arrayAdapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
                intent.putExtra("placeNumber",i);
              intent.putExtra("Info","Long press on the Location to add in favorite");
                startActivity(intent);
            }
        });
    }
}