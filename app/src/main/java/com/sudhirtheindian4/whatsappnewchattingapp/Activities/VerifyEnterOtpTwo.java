package com.sudhirtheindian4.whatsappnewchattingapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.sudhirtheindian4.whatsappnewchattingapp.R;

import java.util.concurrent.TimeUnit;

public class VerifyEnterOtpTwo extends AppCompatActivity {

    EditText inputNumber1,inputNumber2,inputNumber3,
            inputNumber4,inputNumber5,inputNumber6;

     String getbackendOtp;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_enter_otp_two);
        inputNumber1= findViewById(R.id.inputOtp1);
        inputNumber2= findViewById(R.id.inputOtp2);
        inputNumber3= findViewById(R.id.inputOtp3);
        inputNumber4= findViewById(R.id.inputOtp4);
        inputNumber5= findViewById(R.id.inputOtp5);
        inputNumber6= findViewById(R.id.inputOtp6);

        // final krne se dobara yah changen nahi hoga
    final    Button verifyButtonClick = findViewById(R.id.buttonGetOtp);
    final     ProgressBar progressBarverify = findViewById(R.id.progressbar_verifying_otp);

    TextView resendotp= findViewById(R.id.textresendOtp);


    resendotp.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    "+91" + getIntent().getStringExtra("mobile")
                    , 60,
                    TimeUnit.SECONDS,
                    VerifyEnterOtpTwo.this,
                    new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        @Override
                        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {


                        }

                        @Override
                        public void onVerificationFailed(@NonNull FirebaseException e) {

                            Toast.makeText(VerifyEnterOtpTwo.this, e.getMessage(), Toast.LENGTH_SHORT).show();


                        }

                        @Override
                        public void onCodeSent(@NonNull String newbackendOtp, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                         // isme nya otp store hoga
                      getbackendOtp = newbackendOtp;
                            Toast.makeText(VerifyEnterOtpTwo.this, "OTP send Successsfully", Toast.LENGTH_SHORT).show();


                        }
                    }
            );

        }
    });


        getbackendOtp = getIntent().getStringExtra("backendOtp");


        TextView textView = findViewById(R.id.textmobilenubmershow);
        textView.setText(String.format(
               "+91-%s",getIntent().getStringExtra("mobile")
        ));

        verifyButtonClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!inputNumber1.getText().toString().trim().isEmpty()&& !inputNumber2.getText().toString().trim().isEmpty() && !inputNumber3.getText().toString().trim().isEmpty()
                        && !inputNumber4.getText().toString().trim().isEmpty() && !inputNumber5.getText().toString().trim().isEmpty() && !inputNumber6.getText().toString().trim().isEmpty()){
//                    Toast.makeText(VerifyEnterOtpTwo.this, "OTP verify", Toast.LENGTH_SHORT).show();

                    String enterCodeOtp = inputNumber1.getText().toString()+
                            inputNumber2.getText().toString()+
                            inputNumber3.getText().toString()+
                            inputNumber4.getText().toString()+
                            inputNumber5.getText().toString()+
                            inputNumber6.getText().toString();
                    if(getbackendOtp!=null){

                        progressBarverify.setVisibility(View.VISIBLE);
                        verifyButtonClick.setVisibility(View.INVISIBLE);

                        PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(
                                getbackendOtp,enterCodeOtp
                        );

                        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        progressBarverify.setVisibility(View.INVISIBLE);
                                        verifyButtonClick.setVisibility(View.VISIBLE);

                                        if(task.isSuccessful()){

                                            Intent intent = new Intent(getApplicationContext(), SetupProfileActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finishAffinity();// yah pahle se open sare activity ko clear kr dega
                                        }
                                        else {

                                            Toast.makeText(VerifyEnterOtpTwo.this, "Enter the correct OTP", Toast.LENGTH_SHORT).show();

                                        }


                                    }
                                });

                    }
                    else {
                        Toast.makeText(VerifyEnterOtpTwo.this, "Please check Internet Connection", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(VerifyEnterOtpTwo.this, "please enter all number", Toast.LENGTH_SHORT).show();
                }
            }
        });

        numberOtpMove();
    }

    private void numberOtpMove() {
        inputNumber1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty()){
                    inputNumber2.requestFocus();
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputNumber2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty()){
                    inputNumber3.requestFocus();
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputNumber3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty()){
                    inputNumber4.requestFocus();
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputNumber4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty()){
                    inputNumber5.requestFocus();
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputNumber5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty()){
                    inputNumber6.requestFocus();
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
}