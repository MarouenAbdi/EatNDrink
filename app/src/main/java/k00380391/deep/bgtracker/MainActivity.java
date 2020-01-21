package k00380391.deep.bgtracker;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private GoogleMap map;
    private LatLng currentLocation;
    Location location;
    private double longitude,latitude;
    LocationService ls;
    ImageButton bar,rest,cafe, camera;

    private static final String[] LOCATION_PERMS={
        Manifest.permission.ACCESS_FINE_LOCATION,
    };

    private static final int INITIAL_REQUEST = 1337;

    List<Results> resultValues = new ArrayList<Results>();

    ListView listView;
    ListAdapter adapter;
    Context context;
    GoogleApiResponse apiResponse;
    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //remove title
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        bar = (ImageButton)findViewById(R.id.ImageBar);
        cafe = (ImageButton)findViewById(R.id.ImageCafe);
        rest = (ImageButton)findViewById(R.id.ImageRes);
        camera = (ImageButton)findViewById(R.id.ImageCamera);

        bar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchData(2);
            }
        });

        rest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchData(0);
            }
        });

        cafe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchData(1);
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,CloudActivity.class));
            }
        });




        context = this;

        listView = (ListView) findViewById(R.id.listview);

        ls = new LocationService(this);

        if (!canAccessLocation() && Build.VERSION.SDK_INT >= 23) {
            requestPermissions(LOCATION_PERMS, INITIAL_REQUEST);
        }else{
            loadGoogleMap();
            fetchData(0);
        }
    }


    public void loadGoogleMap() {

        longitude = ls.getLongitude();
        latitude = ls.getLatitude();


    }

    public void fetchData(int type) {

        MyApi googleApi = MyApi.retrofit.create(MyApi.class);

        String location = latitude + "," + longitude;

        Call<GoogleApiResponse> call;

        String place = "restaurant";

        switch (type) {
            //Restaurant
            case 0 : {
                place = "restaurant";
                break;
            }

            //Bars
            case 1 : {
                place = "bar";
                break;
            }

            //Cafe
            case 2 : {
                place = "cafe";
                break;
            }

        }

        call = googleApi.getNearByPlaces(location,"3000",place,Constants.API_KEY);

        final ProgressDialog progressDialog = ProgressDialog.show(this, "Please wait", "Loading...", false, true);
        call.enqueue(new Callback<GoogleApiResponse>() {

            @Override
            public void onResponse(Call<GoogleApiResponse> call, Response<GoogleApiResponse> response) {

                //apiResponse = response.body();

                if(counter == 0) {
                    loadListView(response.body());
                }else{
                    refreshLisview(response.body());
                }

                counter++;
                progressDialog.dismiss();
                //Log.e("Response",response.body().getResults().toString());
            }

            @Override
            public void onFailure(Call<GoogleApiResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private boolean canAccessLocation() {
        return(hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    private boolean hasPermission(String perm) {
        if ( Build.VERSION.SDK_INT >= 23) {
            return (PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm));
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case INITIAL_REQUEST: {
                // If request is cancelled, the result arrays are empty.

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    loadGoogleMap();
                    fetchData(0);
                    // permission was granted, yay! Do the task you need to do.

                }else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                }
                return;
            }
        }
    }

    private void loadListView(GoogleApiResponse listItems) {



        adapter = new ListAdapter(context,listItems,latitude,longitude);


        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String place_id = adapter.getPlaceId(position);

                String name = adapter.getName(position);


                Intent i = new Intent(context,VendorActivity.class);
                i.putExtra("place_id", place_id);
                i.putExtra("name",name);
                i.putExtra("source_latitude",latitude);
                i.putExtra("source_longitude",longitude);
                i.putExtra("vendor_latitude",adapter.getVendorLatitude(position));
                i.putExtra("vendor_longitude", adapter.getVendorLongitude(position));
                startActivity(i);
            }
        });

    }

    private void refreshLisview(GoogleApiResponse listItems){

        adapter.setListItems(listItems);
        adapter.notifyDataSetChanged();
    }
}
