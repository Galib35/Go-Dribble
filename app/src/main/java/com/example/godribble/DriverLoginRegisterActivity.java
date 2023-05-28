package com.example.godribble;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverLoginRegisterActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 2;
    private Button DriverLoginButton;
    private Button DriverRegisterButton;
    private TextView DriverRegisterLink;
    private  TextView DriverStatus;
    private EditText EmailDriver;
    private  EditText PasswordDriver;

    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    DatabaseReference DriverDatabaseRef;

    private String onlineDriverID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login_register);

        mAuth=FirebaseAuth.getInstance();



        DriverLoginButton = (Button) findViewById(R.id.driver_login_btn);
        DriverRegisterButton = (Button) findViewById(R.id.driver_register_btn);
        DriverRegisterLink = (TextView) findViewById(R.id.driver_register_link);
        DriverStatus = (TextView) findViewById(R.id.driver_status);
        EmailDriver = (EditText) findViewById(R.id.email_driver);
        PasswordDriver = (EditText) findViewById(R.id.password_driver);

        loadingBar=new ProgressDialog(this);

        DriverRegisterButton.setVisibility(View.INVISIBLE);
        DriverRegisterButton.setEnabled(false);


        // When Click on Driver Register Link


        DriverRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DriverLoginButton.setVisibility(View.INVISIBLE);
                DriverRegisterLink.setVisibility(View.INVISIBLE);
                DriverStatus.setText("Driver Register");

                DriverRegisterButton.setVisibility(View.VISIBLE);
                DriverRegisterButton.setEnabled(true);
            }
        });

        // When click on Driver Register Button

        DriverRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = EmailDriver.getText().toString();
                String password = PasswordDriver.getText().toString();

                RegisterDriver(email, password);
            }
        });


        // When Click on Driver Login Button

        DriverLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email=EmailDriver.getText().toString();
                String password= PasswordDriver.getText().toString();

                SignInDriver(email,password);
            }
        });

    }


    //  ------  RegisterDriver Method



        private void RegisterDriver(String email,String password)
        {
            if(TextUtils.isEmpty(email))
            {
                Toast.makeText(DriverLoginRegisterActivity.this, "Please write Email...", Toast.LENGTH_SHORT).show();

            }

            if(TextUtils.isEmpty(password))
            {
                Toast.makeText(DriverLoginRegisterActivity.this, "Please write Password...", Toast.LENGTH_SHORT).show();

            }

            if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){

                loadingBar.setTitle("Driver Registration");
                loadingBar.setMessage("Please wait, while we are register your data...");
                loadingBar.show();

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if(task.isSuccessful()){

                                    onlineDriverID=mAuth.getCurrentUser().getUid();
                                    DriverDatabaseRef= FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(onlineDriverID);

                                    DriverDatabaseRef.setValue(true);

                                    Intent driverIntent=new Intent(DriverLoginRegisterActivity.this,DriverMapActivity.class);
                                    startActivity(driverIntent);


                                    Toast.makeText(DriverLoginRegisterActivity.this,"Driver Registration Successfully ...",Toast.LENGTH_SHORT).show();
                                   loadingBar.dismiss();
                                   checkAndGo();
                                }

                                else
                                {
                                    Toast.makeText(DriverLoginRegisterActivity.this,"Registration Unsuccessful, Please Try Again...",Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            }
                        });
            }
    }

    //  ---------



    // SignInDriver Method
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)
        {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,android.Manifest.permission.ACCESS_FINE_LOCATION))
            {
                new AlertDialog.Builder(this)
                        .setTitle("Allow location permission")
                        .setMessage("Allow permission to use map feature")
                        .setPositiveButton("ok", (dialogInterface, i) ->
                                ActivityCompat.requestPermissions(
                                        DriverLoginRegisterActivity.this,
                                        new String[]{
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                        },
                                        MY_PERMISSIONS_REQUEST_LOCATION))
                        .setCancelable(false)
                        .setNegativeButton("cancel", (dialogInterface, i) -> {
                            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                        })
                        .show();
            }
            else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                        DriverLoginRegisterActivity.this,
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        },
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
        else{
            startNextPage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {//granted
                if (
                        ContextCompat.checkSelfPermission(
                                this,
                                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                                ContextCompat.checkSelfPermission(
                                        this,
                                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    startNextPage();
                }
            }
            else {//permission rejected
                Toast.makeText(this, "Allow permission first", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startNextPage(){
        SharedPreferences sp = getSharedPreferences("sp",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("is_driver_signed_in",true);
        editor.apply();
        Intent DriverIntent= new Intent(DriverLoginRegisterActivity.this, DriverMapActivity.class);
        startActivity(DriverIntent);

    }

    private void checkAndGo(){
        checkLocationPermission();
    }

    private void SignInDriver(String email, String password)
    {
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(DriverLoginRegisterActivity.this, "Please write Email...", Toast.LENGTH_SHORT).show();

        }

        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(DriverLoginRegisterActivity.this, "Please write Password...", Toast.LENGTH_SHORT).show();

        }

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){

            loadingBar.setTitle("Driver Login");
            loadingBar.setMessage("Please wait, while we are checking your data...");
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){
                                Toast.makeText(DriverLoginRegisterActivity.this,"Driver Login Successfully ...",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                startNextPage();
                            }

                            else
                            {
                                Toast.makeText(DriverLoginRegisterActivity.this,"Login Unsuccessful, Please Try Again...",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }



    // ---------


}