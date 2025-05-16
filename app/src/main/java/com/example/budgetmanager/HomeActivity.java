package com.example.budgetmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import com.google.android.material.navigation.NavigationBarView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fab = findViewById(R.id.fab);
        bottomNavigationView.setBackground(null);

        // Shows home fragment by default (bch doub ma nodkhl nra l home)
        loadFragment(new HomeFragment());

        // bch ky ncklicki fl nav thzny ll fragments
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                if (item.getItemId() == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (item.getItemId() == R.id.nav_transactions) {
                    selectedFragment = new TransactionFragment();
                } else if (item.getItemId() == R.id.nav_categories) {
                    selectedFragment = new CategoriesFragment();
                } else if (item.getItemId() == R.id.nav_reports) {
                    selectedFragment = new SettingsFragment();
                }

                return loadFragment(selectedFragment);
            }
        });

        // l + button bch nzyd transactions
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddTransactionActivity.class);
            startActivity(intent);
        });
    }

    //tbadl ma bin lfragments
    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}