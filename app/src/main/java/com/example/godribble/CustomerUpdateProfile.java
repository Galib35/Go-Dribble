package com.example.godribble;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CustomerUpdateProfile extends AppCompatActivity {


    private EditText name,email,phone;
    private Button saveProfile;
    DatabaseReference ref;


    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference AssignedCustomerRef;


    private String driverID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_update_profile);

        name=findViewById(R.id.editPersonCustomer);
        email=findViewById(R.id.editEmailCustomer);
        phone=findViewById(R.id.editPhoneCustomer);

        saveProfile=findViewById(R.id.saveProfileCustomer);

        mAuth = FirebaseAuth.getInstance();

        driverID=mAuth.getCurrentUser().getUid();


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String myId = user.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Information").child("Customers").child(myId);





        //   ref=FirebaseDatabase.getInstance().getReference().child("Information").child("Drivers");



        //DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Information").child("Drivers");

        saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String driverName = name.getText().toString().trim();
                String driverEmail = email.getText().toString().trim();
                String driverPhone = phone.getText().toString().trim();

                if(!TextUtils.isEmpty(driverName) && !TextUtils.isEmpty(driverEmail) && !TextUtils.isEmpty(driverPhone)) {



                    CustomerInfo info = new CustomerInfo(driverName, driverEmail, driverPhone);

                    //       FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    //        if(user == null) return;


                    //             String myId = user.getUid();

                    //                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Information").child("Drivers").child(myId);

                    ref.setValue(info);
                }

            }
        });

    }
    }
