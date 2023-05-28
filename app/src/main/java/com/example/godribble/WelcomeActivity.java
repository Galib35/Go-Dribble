package com.example.godribble;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class WelcomeActivity extends AppCompatActivity {

    private Button welcomeDriverButton;
    private Button welcomeCustomerButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        welcomeCustomerButton=(Button) findViewById(R.id.welcome_customer_btn);
        welcomeDriverButton=(Button) findViewById(R.id.welcome_driver_btn);


        welcomeCustomerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent customer_login=new Intent(WelcomeActivity.this,CustomerLoginRegisterActivity.class);

                startActivity(customer_login);
            }
        });


        welcomeDriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent driver_login=new Intent(WelcomeActivity.this,DriverLoginRegisterActivity.class);

                startActivity(driver_login);
            }
        });

    }
}