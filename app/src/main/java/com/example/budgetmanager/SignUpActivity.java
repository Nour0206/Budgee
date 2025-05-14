package com.example.budgetmanager;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    private EditText emailInput, passwordInput, confirmPasswordInput;
    private Button signUpButton;
    private TextView LoginAccountbtn;
    private FirebaseAuth auth;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        signUpButton = findViewById(R.id.signUpButton);
        LoginAccountbtn = findViewById(R.id.createAccount);

        // Firebase of the user that signed up
        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("users");

        signUpButton.setOnClickListener(view -> registerUser());

        LoginAccountbtn.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void registerUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }


        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = auth.getCurrentUser().getUid();
                        DatabaseReference userRef = dbRef.child(uid);
                        userRef.child("email").setValue(email);

                        addDefaultCategories(userRef.child("categories"));

                        Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, HomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void addDefaultCategories(DatabaseReference categoriesRef) {
        // Expense categories
        addCategory(categoriesRef, "Food", "Expense");
        addCategory(categoriesRef, "Transport", "Expense");
        addCategory(categoriesRef, "Utilities", "Expense");
        addCategory(categoriesRef, "Entertainment", "Expense");

        // Income categories
        addCategory(categoriesRef, "Salary", "Income");
        addCategory(categoriesRef, "Bonus", "Income");
        addCategory(categoriesRef, "Freelance", "Income");
    }

    private void addCategory(DatabaseReference categoriesRef, String name, String type) {
        String id = categoriesRef.push().getKey();
        Map<String, Object> category = new HashMap<>();
        category.put("name", name);
        category.put("type", type);
        categoriesRef.child(id).setValue(category);
    }
}