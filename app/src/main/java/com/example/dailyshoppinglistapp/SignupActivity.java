package com.example.dailyshoppinglistapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {

    private EditText emailText;
    private EditText passwordText;
    private TextView loginText;
    private Button registerButton;
    private FirebaseAuth userAuthentication;
    private ProgressDialog  progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        userAuthentication = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        emailText = findViewById(R.id.emailFieldRegisteration);
        passwordText = findViewById(R.id.passwordFieldRegisteration);
        loginText = findViewById(R.id.loginLink);
        registerButton = findViewById(R.id.signupButton);

        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailText.getText().toString().trim();
                String password = passwordText.getText().toString().trim();
                Log.i("Info","email: "+email+"p passord: "+password);

                if(TextUtils.isEmpty(email)){
                    emailText.setError("Required field!");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    passwordText.setError("Required field!");
                    return;
                }

                progressDialog.setMessage("processing");
                progressDialog.show();

                userAuthentication.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                            Toast.makeText(getApplicationContext(), "Signup successful", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }else {
                            Toast.makeText(getApplicationContext(), task.getException().getMessage().toString(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
            }
        });
    }
}
