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

import java.util.PrimitiveIterator;

public class CustomerLoginRegisterActivity extends AppCompatActivity {

    private Button CustomerLoginButton;
    private Button CustomerRegisterButton;
    private TextView CustomerRegisterLink;
    private  TextView CustomerStatus;

    private EditText EmailCustomer;
    private  EditText PasswordCustomer;

    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference CustomerDatabaseRef;

    private String onlineCustomerID;

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login_register);

        mAuth=FirebaseAuth.getInstance();



        CustomerLoginButton=(Button) findViewById(R.id.customer_login_btn);
        CustomerRegisterButton=(Button)findViewById(R.id.customer_register_btn);
        CustomerRegisterLink=(TextView) findViewById(R.id.customer_register_link);
        CustomerStatus=(TextView) findViewById(R.id.customer_status);

        EmailCustomer = (EditText) findViewById(R.id.email_customer);
        PasswordCustomer = (EditText) findViewById(R.id.password_customer);

        loadingBar=new ProgressDialog(this);

        CustomerRegisterButton.setVisibility(View.INVISIBLE);
        CustomerRegisterButton.setEnabled(false);


        CustomerRegisterButton.setVisibility(View.INVISIBLE);
        CustomerRegisterButton.setEnabled(false);


        //    ----------   When Click on Customer Register Link


        CustomerRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                CustomerLoginButton.setVisibility(View.INVISIBLE);
                CustomerRegisterLink.setVisibility(View.INVISIBLE);
                CustomerStatus.setText("Customer Register");

                CustomerRegisterButton.setVisibility(View.VISIBLE);
                CustomerRegisterButton.setEnabled(true);
            }
        });


        // ------    When Click on Customer Register Button

        CustomerRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = EmailCustomer.getText().toString();
                String password = PasswordCustomer.getText().toString();

                RegisterCustomer(email, password);
            }
        });


        //  -----   When Click on Customer Login Button

        CustomerLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email=EmailCustomer.getText().toString();
                String password= PasswordCustomer.getText().toString();

                SignInCustomer(email,password);
            }
        });

    }





    // ---------Finish -------------





    //  RegisterCustomer Method

    private void RegisterCustomer(String email,String password)
    {
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(CustomerLoginRegisterActivity.this, "Please write Email...", Toast.LENGTH_SHORT).show();

        }

        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(CustomerLoginRegisterActivity.this, "Please write Password...", Toast.LENGTH_SHORT).show();

        }

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){

            loadingBar.setTitle("Customer Registration");
            loadingBar.setMessage("Please wait, while we are register your data...");
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){
                                Toast.makeText(CustomerLoginRegisterActivity.this,"Customer Registration Successfully ...",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                checkAndGo();
                            }

                            else
                            {
                                Toast.makeText(CustomerLoginRegisterActivity.this,"Registration Unsuccessful, Please Try Again...",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    // -------------




    // SignInCustomer Method


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
                                        CustomerLoginRegisterActivity.this,
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
                        CustomerLoginRegisterActivity.this,
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
        editor.putBoolean("is_customer_signed_in",true);
        editor.apply();
        Intent DriverIntent= new Intent(CustomerLoginRegisterActivity.this, CustomerMapActivity.class);
        startActivity(DriverIntent);

    }

    private void checkAndGo(){
        checkLocationPermission();
    }

    private void SignInCustomer(String email, String password)
    {
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(CustomerLoginRegisterActivity.this, "Please write Email...", Toast.LENGTH_SHORT).show();

        }

        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(CustomerLoginRegisterActivity.this, "Please write Password...", Toast.LENGTH_SHORT).show();

        }

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){

            loadingBar.setTitle("Customer Login");
            loadingBar.setMessage("Please wait, while we are checking your data...");
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){

                                onlineCustomerID=mAuth.getCurrentUser().getUid();
                                CustomerDatabaseRef= FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(onlineCustomerID);

                                CustomerDatabaseRef.setValue(true);



                                Toast.makeText(CustomerLoginRegisterActivity.this,"Customer Login Successfully ...",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                startNextPage();

                            }

                            else
                            {
                                Toast.makeText(CustomerLoginRegisterActivity.this,"Login Unsuccessful, Please Try Again...",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }


    // ---------
}