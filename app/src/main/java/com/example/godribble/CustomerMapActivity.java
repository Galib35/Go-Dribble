package com.example.godribble;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.widget.SearchView;

import android.widget.TextView;
import android.widget.Toast;

import com.example.godribble.classes.EachRequest;
import com.example.godribble.classes.Point;
import com.example.godribble.classes.SimpleLocation;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.godribble.databinding.ActivityCustomerMapBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import io.reactivex.annotations.NonNull;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityCustomerMapBinding binding;

    private Button btn_customer_logout,btn_customer_setting;



    private SimpleLocation simpleLocation = null;
    private boolean firstTime1 = true;

    private Marker myMarker = null;
    private Marker driverMarker;
    Location mLastLocation;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference  customerDatabaseRef;
    private DatabaseReference driverAvailableRef;
    private DatabaseReference DriverRef;
    private DatabaseReference DriverLocationRef;
    private DatabaseReference AssignedCustomerRef;

    private boolean currentCustomerStatus=false;

    private Button call_vehicle;

    private int customer_click_callVehicle=0;

    private int radius=1;

    private boolean driverFound=false;
    String driverFoundID;
    private String customerID;
    private String driverID, myName, myDest;
    private HashMap<String, Marker> driverMarkers = new HashMap<>();
    private LatLng myLatLng = null;

     public SearchView searchView;


    ImageView menu_img;
    DrawerLayout drawerLayout;
    NavigationView navigationView;

    private TextView textViewDriverName,textViewDriverEmail,textViewDriverPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCustomerMapBinding.inflate(getLayoutInflater());
       setContentView(binding.getRoot());

        customerID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DriverLocationRef=FirebaseDatabase.getInstance().getReference().child("Drivers Working");


        textViewDriverName=findViewById(R.id.tvDriverName);
        textViewDriverPhone=findViewById(R.id.tvDriverPhone);
        textViewDriverEmail=findViewById(R.id.tvDriverEmail);

        btn_customer_setting=findViewById(R.id.btn_customer_setting);
        btn_customer_logout=findViewById(R.id.btn_customer_logout);


        btn_customer_setting.setVisibility(View.INVISIBLE);
        btn_customer_logout.setVisibility(View.INVISIBLE);
        btn_customer_setting.setEnabled(false);
        btn_customer_logout.setEnabled(false);


        textViewDriverName.setVisibility(View.INVISIBLE);
        textViewDriverPhone.setVisibility(View.INVISIBLE);
        textViewDriverEmail.setVisibility(View.INVISIBLE);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if(mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        simpleLocation = new SimpleLocation(this,true,false,10*1000,false);



        //logOutCustomer();


        call_vehicle=findViewById(R.id.btn_customer_call_vehicle);

        customerDatabaseRef=FirebaseDatabase.getInstance().getReference().child("Customers Request");

        call_vehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Call_A_Driver();
            }
        });

        //  Customer Setting

        btn_customer_setting = findViewById(R.id.btn_customer_setting);
        btn_customer_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(CustomerMapActivity.this,CustomerProfile.class);
                startActivity(intent);
            }
        });


        // ---------  SearchView --------------------


        searchView=(SearchView) findViewById(R.id.idSearchView);

        searchView.setQueryHint("Search Destination");
     searchView.setIconified(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                // location name from search view.
                String location = searchView.getQuery().toString();

                // below line is to create a list of address
                // where we will store the list of all address.
                List<Address> addressList = null;

                // checking if the entered location is null or not.
                // on below line we are creating and initializing a geo coder.
                Geocoder geocoder = new Geocoder(CustomerMapActivity.this);
                try {
                    // on below line we are getting location from the
                    // location name and adding that location to address list.
                    addressList = geocoder.getFromLocationName(location, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // on below line we are getting the location
                // from our list a first position.
                Address address = addressList.get(0);

                // on below line we are creating a variable for our location
                // where we will add our locations latitude and longitude.
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                // on below line we are adding marker to that position.


                String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("Information").child("Customers").child(userID).child("name");


                int m=0;

                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {

                        if(snapshot.exists()){
                           String title=snapshot.getValue(String.class).toString();
                            mMap.addMarker(new MarkerOptions().position(latLng).title("Destination")).setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.destination_foreground));
                        }
                    }

                    @Override
                    public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

                    }
                });


      //          mMap.addMarker(new MarkerOptions().position(latLng).title(location)).setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.person_marker_foreground));

                // below line is to animate camera to that position.
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });



        //   ----  Navigation Drawer  ------------



        menu_img = findViewById(R.id.menu2);

        drawerLayout=findViewById(R.id.drawerCustomer);
        navigationView=findViewById(R.id.nav2);





        menu_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Menu menu=navigationView.getMenu();
                //MenuItem menuItem=menu.findItem(R.id.adm);


                if(!drawerLayout.isDrawerOpen(GravityCompat.START))
                {
                    drawerLayout.openDrawer(GravityCompat.START);
                    navigationView.bringToFront();
                }
                else
                {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@androidx.annotation.NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.acc:
                        Intent intent = new Intent(CustomerMapActivity.this, CustomerUpdateProfile.class);
                        startActivity(intent);
                        Toast.makeText(CustomerMapActivity.this, "Account", Toast.LENGTH_SHORT).show();
                        break;


                    case R.id.history:
                        intent = new Intent(CustomerMapActivity.this, CustomerMapActivity.class);
                        startActivity(intent);
                        break;

                    case R.id.payment:
                        intent = new Intent(CustomerMapActivity.this, CustomerPayment.class);
                        startActivity(intent);
                        break;

                    case R.id.reviews:
                        intent = new Intent(CustomerMapActivity.this, CustomerReviews.class);
                        startActivity(intent);
                        break;

                    case R.id.share_app:

                        Intent shareIntent=new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.putExtra(Intent.EXTRA_TEXT,"Welcome to Go Drible");
                        shareIntent.setType("text/plain");


                        if((shareIntent.resolveActivity(getPackageManager())!=null))
                        {
                            startActivity(shareIntent);

                        }

                        break;


                    case R.id.log_out:
         //               intent=new Intent(CustomerMapActivity.this,WelcomeActivity.class);
         //               startActivity(intent);
                        break;


                }
                return false;
            }
        });





    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        setListener();



    }

    private void setListener()
    {
        if(simpleLocation!=null){
            simpleLocation.beginUpdates();

            simpleLocation.setListener(new SimpleLocation.Listener() {
                @Override
                public void onPositionChanged() {

                    Point point=simpleLocation.getPosition();
                    myLatLng = new LatLng(point.getLatitude(),point.getLongitude());

                    moveCameraToPosition(point);

                    customerAvailabilityStoreFirebase(point);
                    showAllDrivers();
                }
            });
        }
    }

    private void moveCameraToPosition(Point point)
    {
        double lat=point.getLatitude();
        double lon=point.getLongitude();


        if(firstTime1)
        {
            LatLng latLng=new LatLng(lat,lon);

            MarkerOptions markerOptions=new MarkerOptions();

            markerOptions.position(latLng);


            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();


            String title=FirebaseDatabase.getInstance().getReference().child("Information").child("Customers").child(userID).child("name").getKey().toString();

     //       markerOptions.title(title);

            userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("Information").child("Customers").child(userID).child("name");


            int m=0;

            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {

                    if(snapshot.exists()){
                        String title=snapshot.getValue(String.class).toString();
                        mMap.addMarker(new MarkerOptions().position(latLng).title(title)).setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.person_marker_foreground));
                    }
                }

                @Override
                public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

                }
            });


            mMap.clear();

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,18));

            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.person_marker_foreground));

            mMap.addMarker(markerOptions);

            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(lat,lon)).zoom(18).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            firstTime1 = false;
        }

        if (myMarker != null) myMarker.remove();
    }

    private void customerAvailabilityStoreFirebase(Point point)
    {

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if(mAuth.getCurrentUser() != null && !currentCustomerStatus) {
            //        String userID= mAuth.getCurrentUser().getUID();
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();


            //     String userID= FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference CustomerAvailability = FirebaseDatabase.getInstance().getReference().child("Customers Available");

            GeoFire geoFire = new GeoFire(CustomerAvailability);

            geoFire.setLocation(userID, new GeoLocation(point.getLatitude(), point.getLongitude()));
        }

    }


    private void logOutCustomer() {

        btn_customer_logout = findViewById(R.id.btn_customer_logout);
        btn_customer_setting = findViewById(R.id.btn_customer_setting);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        btn_customer_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //  disconnect customer

                currentCustomerStatus=true;
                disconnectCustomerFromFirebase();
                SharedPreferences sp = getSharedPreferences("sp",MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("is_customer_signed_in",false);
                editor.apply();

                //  signout authentication
                mAuth.signOut();
                Intent welcomeIntent = new Intent(CustomerMapActivity.this, WelcomeActivity.class);

                // Kill current activity

                welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(welcomeIntent);
                finish();


            }
        });

        //       disconnectCustomerFromFirebase();



    }

    private void disconnectCustomerFromFirebase()
    {
        String userID= FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference DriverAvailibilityRef=FirebaseDatabase.getInstance().getReference().child("Customers Available");

        GeoFire geoFire=new GeoFire(DriverAvailibilityRef);
        geoFire.removeLocation(userID);

    }

    private void Call_A_Driver()
    {

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.user_input_layout);

        Window window = dialog.getWindow();

        if(window != null){

            window.setBackgroundDrawable(new ColorDrawable(0));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        }

        TextInputEditText editTextName = dialog.findViewById(R.id.editTextName);
        TextInputEditText editTextDest = dialog.findViewById(R.id.editTextDest);
        Button buttonCancel = dialog.findViewById(R.id.buttonCancel);
        Button buttonSubmit = dialog.findViewById(R.id.buttonSubmit);

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myName = String.valueOf(editTextName.getText());
                myDest = String.valueOf(editTextDest.getText());

                if(myName.isEmpty() || myDest.isEmpty()){
                    Toast.makeText(CustomerMapActivity.this, "Can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                dialog.dismiss();
                sendRequest();
            }
        });


        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setCancelable(false);
        dialog.show();
    }


    private void sendRequest(){
        if(myLatLng == null) return;

        radius = 1;
        getClosestDriver(myLatLng);
    }

    private void showAllDrivers()
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Drivers Available");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {

                ArrayList<EachRequest> requests = new ArrayList<>();

                for(DataSnapshot ds : snapshot.getChildren()){

                    try {
                        String uid = ds.getKey();
                        String slat = String.valueOf(ds.child("l").child("0").getValue());
                        String slon = String.valueOf(ds.child("l").child("1").getValue());


                        double lat = Double.parseDouble(slat);
                        double lon = Double.parseDouble(slon);

                        requests.add( new EachRequest(uid,lat,lon));

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }


                showAvailableDrivers(requests);

            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });

    }


    private void showAvailableDrivers(ArrayList<EachRequest> requests){

        for(EachRequest req : requests){
            Marker prevMarker = driverMarkers.get(req.getUid());

            if(prevMarker != null){
                prevMarker.remove();
                driverMarkers.remove(req.getUid());
            }


            Marker curMarker = mMap.addMarker(new MarkerOptions().position(req.getLatLong()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.food_delivery_real_foreground)));

            if(curMarker == null) continue;

           String name=FirebaseDatabase.getInstance().getReference().child("Information").child("Drivers").child(req.getUid()).child("name").getKey();


            curMarker.setTitle(name);
            driverMarkers.put(req.getUid(),curMarker);

        }
    }


    private void getClosestDriver(LatLng latLng)
    {
        driverAvailableRef=FirebaseDatabase.getInstance().getReference().child("Drivers Available");
        GeoFire geoFire=new GeoFire(driverAvailableRef);

        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude),radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!driverFound){
                    driverFound=true;
                    driverFoundID = key;

                    FirebaseUser user  = FirebaseAuth.getInstance().getCurrentUser();

                    if(user == null) return;

                    showDriverInfo(key);
                    String myId = user.getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("request").child(driverFoundID).child(myId);

                    HashMap<String,String> map = new HashMap<>();
                    map.put("name",myName);
                    map.put("dest",myDest);
                    ref.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@androidx.annotation.NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                showWaitingDialog(myId);
                            }
                            else{
                                Toast.makeText(CustomerMapActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    geoQuery.removeAllListeners();

                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                if(!driverFound){
                    radius=radius+1;
                    getClosestDriver(latLng);
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                System.out.println("Error -> "+error.getMessage());
            }
        });

    }


    private void showDriverInfo(String key){
        try{
            if(key == null) return;

            DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference().child("Information")
                    .child("Drivers").child(key);
            ref1.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                    if(!snapshot.exists()) return;

                    String name = String.valueOf(snapshot.child("name").getValue());
                    String email = String.valueOf(snapshot.child("email").getValue());
                    String phone = String.valueOf(snapshot.child("phone").getValue());
                }

                @Override
                public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

                }
            });
        }catch (Exception ignored){}

    }


    private void showWaitingDialog(String myId){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.waiting_layout);

        Window window = dialog.getWindow();

        if(window != null){

            window.setBackgroundDrawable(new ColorDrawable(0));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        }

        final boolean[] firstTime = {true};

        DatabaseReference refMain = FirebaseDatabase.getInstance().getReference().child("request").child(driverFoundID).child(myId);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("status").child(driverFoundID+"_"+myId);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                if(firstTime[0]){
                    firstTime[0] = false;
                }
                else {
                    if (snapshot.exists()) {
                        try {
                            boolean bool = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                            if (bool) {
                                Toast.makeText(CustomerMapActivity.this, "Accepted your call", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(CustomerMapActivity.this, "Rejected by driver", Toast.LENGTH_SHORT).show();
                            }
                            driverFound = false;

                            ref.removeEventListener(this);
                            ref.removeValue();
                            refMain.child("over").setValue(true).addOnCompleteListener(task -> dialog.dismiss());
                        } catch (Exception ignored) {
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        };

        startCheck(ref,listener);

        ImageView ivEnd = dialog.findViewById(R.id.ivCallEnd);

        ivEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                driverFound = false;
                refMain.removeValue().addOnCompleteListener(task -> dialog.dismiss());
            }
        });


        dialog.setCancelable(false);
        dialog.show();
    }

    private void startCheck(DatabaseReference ref, ValueEventListener listener){
        ref.addValueEventListener(listener);
    }


    private void GettingDriverLocation()
    {

        DriverLocationRef.child(driverFoundID).child("l").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {
                    List<Object> driverLocationMap = (List<Object>) snapshot.getValue();
                    double LocationLat = 0;
                    double LocationLng = 0;

                    call_vehicle.setText("Driver Found");

                    if (driverLocationMap.get(0) != null) {
                        LocationLat = Double.parseDouble(driverLocationMap.get(0).toString());

                    }


                    if (driverLocationMap.get(0) != null) {
                        LocationLng = Double.parseDouble(driverLocationMap.get(0).toString());

                    }

                    LatLng DriverLatLng = new LatLng(LocationLat, LocationLng);

                    if(driverMarker!=null)
                    {
                        driverMarker.remove();
                    }

                    Point point=simpleLocation.getPosition();

                    Location location1=new Location("");
                    location1.setLatitude(point.getLatitude());
                    location1.setLongitude(point.getLongitude());

                    Location location2=new Location("");
                    location2.setLatitude(DriverLatLng.latitude);
                    location2.setLongitude(DriverLatLng.longitude);

                    float Distance=location1.distanceTo(location2);
                    call_vehicle.setText("Driver Found" + String.valueOf(Distance));

                    driverMarker=mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("Driver is here"));

                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });

    }




    protected void onStop() {

        super.onStop();

        if(!currentCustomerStatus){
            currentCustomerStatus = true;
            disconnectCustomerFromFirebase();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        currentCustomerStatus = false;
    }
}