package com.example.godribble;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Thread thread=new Thread(){

            public void run(){
                try {
                    sleep(2000);
                }

                catch (Exception e)
                {
                    e.printStackTrace();
                }

                finally {

                    SharedPreferences sp = getSharedPreferences("sp",MODE_PRIVATE);
                    boolean isSignedIn = sp.getBoolean("is_driver_signed_in",false);
                    if(isSignedIn){
                        startActivity(new Intent(MainActivity.this,DriverMapActivity.class));
                    }
                    else {
                        boolean isCustomerSignedIn = sp.getBoolean("is_customer_signed_in",false);
                        if(isCustomerSignedIn){
                            startActivity(new Intent(MainActivity.this,CustomerMapActivity.class));
                        }
                        else {
                            Intent welcomeIntent = new Intent(MainActivity.this, WelcomeActivity.class);
                            startActivity(welcomeIntent);
                        }
                    }
                }
            }
        };

        thread.start();

    }

//   -------

    public void onPause(){
        super.onPause();

        finish();
    }
}