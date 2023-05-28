package com.example.godribble;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.godribble.classes.EachRequest;
import com.example.godribble.classes.Point;
import com.example.godribble.classes.SimpleLocation;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener

{


    private Button btn_setting,btn_logout;
    private GoogleMap mMap;


    private SimpleLocation simpleLocation = null;
    private boolean firstTime = true;

    private Marker myMarker = null;

    Location mLastLocation;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference AssignedCustomerRef;

    private boolean currentDriverStatus=false;
    private HashMap<String, Marker> customerMarkers = new HashMap<>();

    private String driverID;
    private boolean isShowing = false;

//    private  static final int[] COLORS = new int[]{ R.color.BlueViolet,  R.color.Blue,R.color.Brown, R.color.purple_200};

    ImageView menu_img;
    DrawerLayout drawerLayout;
    NavigationView navigationView;


    private TextView textViewCustomerName,textViewCustomerEmail,textViewCustomerPhone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_driver_map);

        mAuth = FirebaseAuth.getInstance();

        driverID=mAuth.getCurrentUser().getUid();


        textViewCustomerName=findViewById(R.id.tvCustomerName);
        textViewCustomerEmail=findViewById(R.id.tvCustomerEmail);
        textViewCustomerPhone=findViewById(R.id.tvCustomerPhone);

        textViewCustomerName.setVisibility(View.INVISIBLE);
        textViewCustomerEmail.setVisibility(View.INVISIBLE);
        textViewCustomerPhone.setVisibility(View.INVISIBLE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if(mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        simpleLocation = new SimpleLocation(this,true,false,10*1000,false);

        logOutDriver();

        btn_setting = findViewById(R.id.btn_setting);
        btn_logout=findViewById(R.id.btn_logout);

        btn_setting.setVisibility(View.INVISIBLE);
        btn_logout.setVisibility(View.INVISIBLE);
        btn_setting.setEnabled(false);
        btn_logout.setEnabled(false);


        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(DriverMapActivity.this,DriverProfile.class);
                startActivity(intent);
            }
        });


        //   ----  Navigation Drawer  ------------

        menu_img = findViewById(R.id.menu);

        drawerLayout=findViewById(R.id.drawer);
        navigationView=findViewById(R.id.nav);





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
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.acc:
                        Intent intent = new Intent(DriverMapActivity.this, UpdateProfile.class);
                        startActivity(intent);
                        Toast.makeText(DriverMapActivity.this, "Account", Toast.LENGTH_SHORT).show();
                        break;


                    case R.id.history:
                         intent = new Intent(DriverMapActivity.this, DriverRideHistory.class);
                        startActivity(intent);
                        break;

                    case R.id.payment:
                        intent = new Intent(DriverMapActivity.this, DriverPayment.class);
                        startActivity(intent);
                        break;

                    case R.id.reviews:
                        intent = new Intent(DriverMapActivity.this, DriverReviews.class);
                        startActivity(intent);
                        break;


                    case R.id.log_out:
        //                intent=new Intent(DriverMapActivity.this,WelcomeActivity.class);
          //              startActivity(intent);
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

    private void setListener(){
        if(simpleLocation != null){

            startReceivingRequest();
            simpleLocation.beginUpdates();

            simpleLocation.setListener(new SimpleLocation.Listener() {
                @Override
                public void onPositionChanged() {
                    Point point = simpleLocation.getPosition();

                    moveCameraToPosition(point);

                    driverAvailabilityStoreFirebase(point);
                    showAllCustomerRequests();
                }
            });
        }
    }

    private void startReceivingRequest(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null) return;


        String myId = user.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("request").child(myId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(isShowing) return;
                    for(DataSnapshot ds : snapshot.getChildren()){

                        if(ds.child("over").exists()) continue;

                        String cusId = ds.getKey();
                        String name = String.valueOf(ds.child("name").getValue());
                        String dest = String.valueOf(ds.child("dest").getValue());

                        showPopUp(cusId,name,dest);

                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // --------  Show Route ----------

        getRouteMarker();



    }

    private void showPopUp(String cusId, String name, String dest){

        isShowing = true;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("status").child(uid+"_"+cusId);

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.driver_receive_layout);

        Window window = dialog.getWindow();

        if(window != null){

            window.setBackgroundDrawable(new ColorDrawable(0));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        }


        ImageView ivEnd = dialog.findViewById(R.id.ivCallEnd);
        ImageView ivAccept = dialog.findViewById(R.id.ivCallAccept);
        TextView tvName = dialog.findViewById(R.id.tvName);
        TextView tvDest = dialog.findViewById(R.id.tvDest);

        tvName.setText(name);
        tvDest.setText(dest);

        ivAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ref.setValue(true);
                dialog.dismiss();
                isShowing = false;
                Toast.makeText(DriverMapActivity.this, "Accepted", Toast.LENGTH_SHORT).show();
            }
        });

        ivEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ref.setValue(false);
                isShowing = false;
                Toast.makeText(DriverMapActivity.this, "Rejected", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.setCancelable(false);
        dialog.show();

    }

    private void moveCameraToPosition(Point point) {
        if(point == null) return;
        double lat = point.getLatitude(), lon = point.getLongitude();
        /*
        if(firstTime) {

            LatLng latLng = new LatLng(lat,lon);

            MarkerOptions options = new MarkerOptions();
            options.position(latLng);
            options.title("KUET");

            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(lat,lon)).zoom(10).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            firstTime = false;
            }

        if (myMarker != null) myMarker.remove();

        */


       if(firstTime) {

            LatLng latLng = new LatLng(lat,lon);

            MarkerOptions markerOptions=new MarkerOptions();

            markerOptions.position(latLng);
            markerOptions.title(latLng.latitude + " : "+ latLng.longitude).icon(BitmapDescriptorFactory.fromResource(R.mipmap.food_delivery_marker_foreground));

            mMap.clear();


            mMap.addMarker(markerOptions);

            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(lat,lon)).zoom(14).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            firstTime = false;
        }

        if (myMarker != null) myMarker.remove();


    }


    private void locationStoreFirebase(Point point)
    {

        if(point==null)return;
        double lat = point.getLatitude(), lon = point.getLongitude();

        FirebaseDatabase.getInstance().getReference("Driver location").setValue(point).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(DriverMapActivity.this, "Location Saved", Toast.LENGTH_SHORT).show();
                }

                if(task.isSuccessful()){
                    Toast.makeText(DriverMapActivity.this, "Location Saved", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void driverAvailabilityStoreFirebase(Point point)
    {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if(mAuth.getCurrentUser() != null) {
            String userID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            DatabaseReference DriverAvailability = FirebaseDatabase.getInstance().getReference().child("Drivers Available");

            GeoFire geoFire = new GeoFire(DriverAvailability);

            geoFire.setLocation(userID, new GeoLocation(point.getLatitude(), point.getLongitude()));

        }
    }


    private void logOutDriver()
    {

        btn_logout=findViewById(R.id.btn_logout);
        btn_setting=findViewById(R.id.btn_setting);

        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                    //  Disconnect Driver
                    currentDriverStatus = true;
                    disconnectDriverFromFirebase();

                    //  SignOut Driver
                    SharedPreferences sp = getSharedPreferences("sp", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean("is_driver_signed_in", false);
                    editor.apply();

                    mAuth.signOut();
                    Intent welcomeIntent = new Intent(DriverMapActivity.this, WelcomeActivity.class);

                    // Kill current activity

                    welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(welcomeIntent);
                    finish();

                }

        });

     //   disconnectDriverFromFirebase();

        GetAssignedCustomerRequest();

    }

    private void disconnectDriverFromFirebase()
    {
        String userID= Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference CustomerAvailibilityRef=FirebaseDatabase.getInstance().getReference().child("Drivers Available");

        GeoFire geoFire=new GeoFire(CustomerAvailibilityRef);
        geoFire.removeLocation(userID);
    }


    private void GetAssignedCustomerRequest()
    {
        AssignedCustomerRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverID).child("CustomerRideID");

        AssignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //customerID=snapshot.getValue().toString();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void showAllCustomerRequests()
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Customers Available");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

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


                showRequestMarkers(requests);

                //List<Object>customerLocationMap=(List<Object>) snapshot.getValue();

                //double LocationLat = 0;
                //double LocationLng = 0;



//                if (customerLocationMap.get(0) != null) {
//                    LocationLat = Double.parseDouble(customerLocationMap.get(0).toString());
//
//                }
//
//
//                if (customerLocationMap.get(0) != null) {
//                    LocationLng = Double.parseDouble(customerLocationMap.get(0).toString());
//
//                }
//
//                LatLng DriverLatLng = new LatLng(LocationLat, LocationLng);
//                mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("PickUp Location"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void showRequestMarkers(ArrayList<EachRequest> requests){

        try {
            for (String key : customerMarkers.keySet()) {
                customerMarkers.get(key).remove();
            }
        }catch (Exception e){}

        for(EachRequest req : requests){
            Marker prevMarker = customerMarkers.get(req.getUid());

            if(prevMarker != null){
                prevMarker.remove();
                customerMarkers.remove(req.getUid());
            }


            Marker curMarker = mMap.addMarker(new MarkerOptions().position(req.getLatLong()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

            if(curMarker == null) continue;

            curMarker.setTitle(req.getUid());
            customerMarkers.put(req.getUid(),curMarker);

        }
    }



    protected void onStop() {

        super.onStop();


        if(!currentDriverStatus){
            disconnectDriverFromFirebase();
        }

    }


    @Override
    public void onRoutingFailure(RouteException e) {

//        if(e != null) {
//            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
//        }else {
//            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
//        }
    }

    @Override
    public void onRoutingStart() {

    }


    private List<Polyline> polylines;

 //   private  final int[] COLORS = new int[]{com.nicolasmilliard.rxtask.R.color.wallet_holo_blue_light, R.color.BlueViolet,  R.color.Blue,R.color.Brown};
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
     //       int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
    //        polyOptions.color(getResources().getColor(COLORS[colorIndex]));

            polyOptions.color(getResources().getColor(android.R.color.holo_blue_bright));

            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }

        LatLng start=new LatLng(simpleLocation.getLatitude(),simpleLocation.getLongitude());

     //   LatLng start = new LatLng(18.015365, -77.499382);
        LatLng  waypoint= new LatLng(18.01455, -77.499333);
        LatLng   end = new LatLng(18.012590, -77.500659);

        // Start marker
        MarkerOptions options = new MarkerOptions();
        options.position(start);
       options.icon(BitmapDescriptorFactory.fromResource(com.nicolasmilliard.rxtask.R.drawable.googleg_disabled_color_18));
        mMap.addMarker(options);

        // End marker
        options = new MarkerOptions();
        options.position(end);
        options.icon(BitmapDescriptorFactory.fromResource(com.firebase.geofire.R.drawable.googleg_disabled_color_18));
        mMap.addMarker(options);
    }

    @Override
    public void onRoutingCancelled() {

    }

    private void getRouteMarker()
    {
    //    Point point1 = simpleLocation.getPosition();

     //   Point point2=new Point(18.015365, -77.499382);

     //   Point point3=new Point(18.01455, -77.499333);

        LatLng start=new LatLng(simpleLocation.getLatitude(),simpleLocation.getLongitude());

     //   LatLng start = new LatLng(18.015365, -77.499382);
      LatLng  waypoint= new LatLng(22.9007698153893, 89.5023135821923);
     LatLng   end = new LatLng(22.9007698153893, 89.5023135821923);

        Routing routing = new Routing.Builder()
                .travelMode(Routing.TravelMode.DRIVING)
                .withListener(this)
                .waypoints(start,waypoint,end)
                .build();
        routing.execute();
    }
}



