package com.example.applicationquestionnairesgmu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.Objects;

public class ChangePasswordActivity extends AppCompatActivity {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    private FirebaseUser user = mAuth.getCurrentUser();
    private Button button_change_password;
    private EditText old_password, new_password_1, new_password_2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        Button button_back = (Button)findViewById(R.id.btn_back);
        button_back.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChangePasswordActivity.this, MenuActivity.class);
                startActivity(intent);
            }
        });

        button_change_password = (Button)findViewById(R.id.button_change_password);
        button_change_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                old_password = (EditText) findViewById(R.id.et_old_password);
                new_password_1 = (EditText) findViewById(R.id.et_new_password_1);
                new_password_2 = (EditText) findViewById(R.id.et_new_password_2);
                String o_pass = old_password.getText().toString();
                String new_pass_1 = new_password_1.getText().toString();
                String new_pass_2 = new_password_2.getText().toString();

                if (check_new_password(new_pass_1, new_pass_2)){
                    change_password(o_pass, new_pass_1);
                } else{
                    Toast.makeText(ChangePasswordActivity.this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean check_new_password(String p1, String p2){
        return Objects.equals(p1, p2);
    }

    private void change_password(String old_p, String new_p){
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), old_p);

        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            user.updatePassword(new_p).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ChangePasswordActivity.this, "Пароль изменен", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ChangePasswordActivity.this, "Введен неверный пароль", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(ChangePasswordActivity.this, "Авторизация не прошла", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}