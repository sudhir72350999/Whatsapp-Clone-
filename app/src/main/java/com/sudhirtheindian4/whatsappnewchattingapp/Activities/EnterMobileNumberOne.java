package com.sudhirtheindian4.whatsappnewchattingapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.sudhirtheindian4.whatsappnewchattingapp.R;

import java.util.concurrent.TimeUnit;

public class EnterMobileNumberOne extends AppCompatActivity {

    EditText enterNubmer;
    Button getOtpButton;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_mobile_number_one);
        enterNubmer= findViewById(R.id.input_mobile_number);
        getOtpButton= findViewById(R.id.buttonGetOtp);

        ProgressBar progressBar = findViewById(R.id.progressbar_sending_otp);

        auth= FirebaseAuth.getInstance();

        // if user is already registerd then open always to mainactivity and this code will be executed
        // ise comment nahi karne par dusre ke mobile me crash krega

//        if(auth!=null){
//            Intent intent = new Intent(EnterMobileNumberOne.this,MainActivity.class);
//            startActivity(intent);
//            finish();
//        }

//                        or

        if(auth.getCurrentUser() != null) {
            Intent intent = new Intent(EnterMobileNumberOne.this, MainActivity.class);
            startActivity(intent);
            finish();
        }





        getOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!enterNubmer.getText().toString().trim().isEmpty()){

                    if(enterNubmer.getText().toString().trim().length()==10){


                        progressBar.setVisibility(View.VISIBLE);
                        getOtpButton.setVisibility(View.INVISIBLE);

// after 60 second bad otp khtm ho jayega
                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                "+91" + enterNubmer.getText().toString()
                                , 60,
                                TimeUnit.SECONDS,
                                EnterMobileNumberOne.this,
                                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                    @Override
                                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        getOtpButton.setVisibility(View.VISIBLE);

                                    }

                                    @Override
                                    public void onVerificationFailed(@NonNull FirebaseException e) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        getOtpButton.setVisibility(View.VISIBLE);
                                        Toast.makeText(EnterMobileNumberOne.this, e.getMessage(), Toast.LENGTH_SHORT).show();


                                    }

                                    @Override
                                    public void onCodeSent(@NonNull String backendOtp, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        getOtpButton.setVisibility(View.VISIBLE);
                                        Intent intent = new Intent(getApplicationContext(), VerifyEnterOtpTwo.class);
                                      intent.putExtra("mobile",enterNubmer.getText().toString());
                                      intent.putExtra("backendOtp",backendOtp);
                                      startActivity(intent);


                                    }
                                }
                        );


//                        Intent intent = new Intent(getApplicationContext(),VerifyEnterOtpTwo.class);
//                        intent.putExtra("mobile",enterNubmer.getText().toString());
//                        startActivity(intent);


                    }
                    else {
                        Toast.makeText(EnterMobileNumberOne.this, "Please Enter the correct number", Toast.LENGTH_SHORT).show();

                    }
                }
                else{
                    Toast.makeText(EnterMobileNumberOne.this, "Enter mobile number ", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
}