package com.example.budgetmanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private TextView tvCurrentDate, balance, income, expense;
    private RecyclerView recentTransactionsRecycler;
    private FirebaseAuth auth;
    private DatabaseReference dbRef;
    private ArrayList<Transaction> transactionList;
    private TransactionAdapter transactionAdapter;
    private Map<String, String> categoryMap = new HashMap<>(); // Store category ID and name (l hashmap kyma dictionnaire fyh id w valeur)
    private ImageView logoutImage;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        balance = view.findViewById(R.id.balance);
        income = view.findViewById(R.id.income);
        expense = view.findViewById(R.id.expense);
        recentTransactionsRecycler = view.findViewById(R.id.recentTransactionsRecycler);
        logoutImage = view.findViewById(R.id.logoutImage);

        // Firebase of the user that signed in (bl id mteeou)
        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("users")
                .child(auth.getCurrentUser().getUid());


        // Setup RecyclerView
        transactionList = new ArrayList<>(); //ly feha les 3 derniers transactions
        transactionAdapter = new TransactionAdapter(transactionList);
        recentTransactionsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recentTransactionsRecycler.setAdapter(transactionAdapter); //conectina liste bl recucler view a l'aide d'adaptateur

        // Load categories first (naqraw l categories ml firebase id,valeur bch baad nwary l transaction ,kahter f transaction andy cle etranger ll categoiries)
        loadCategories();
        logoutImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                FirebaseAuth.getInstance().signOut();

                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);

                            }
                        })
                        .setNegativeButton("Cancel", null) // Just dismiss the dialog
                        .show();
            }
        });

        return view;


    }

    private void loadCategories() {
        dbRef.child("categories").addListenerForSingleValueEvent(new ValueEventListener() {//dkhalna l categories
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryMap.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    String id = data.getKey();
                    String name = data.child("name").getValue(String.class);
                    if (id != null && name != null) {
                        categoryMap.put(id, name);
                    }
                }
                loadTransactionData(); // Load transactions ky arfna l categories
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // ken fama erreur
            }
        });
    }

    private void loadTransactionData() {
        dbRef.child("transactions").addValueEventListener(new ValueEventListener() {//dkhalna l transactions
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double totalIncome = 0;
                double totalExpense = 0;
                transactionList.clear();

                ArrayList<Transaction> allTransactions = new ArrayList<>();

                for (DataSnapshot data : snapshot.getChildren()) {
                    Transaction transaction = data.getValue(Transaction.class);
                    if (transaction != null) {

                        // nbdlou id bl nom mtaa l category
                        if (transaction.getCategory() != null && categoryMap.containsKey(transaction.getCategory())) {
                            transaction.setDisplayCategoryName(categoryMap.get(transaction.getCategory()));
                        }

                        allTransactions.add(transaction);//nhotou transactions kol fl temporary list

                        if ("Income".equals(transaction.getType())) {
                            totalIncome += transaction.getAmount();
                        } else if ("Expense".equals(transaction.getType())) {
                            totalExpense += transaction.getAmount();
                        }
                    }
                }

                // on tire par date (nhbou ken last 3 recent ones)
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Collections.sort(allTransactions, (t1, t2) -> {
                    try {
                        Date d1 = sdf.parse(t1.getDate());
                        Date d2 = sdf.parse(t2.getDate());
                        return d2.compareTo(d1); // latest first
                    } catch (Exception e) {
                        return 0;
                    }
                });

                // Add top 3 to display
                for (int i = 0; i < Math.min(3, allTransactions.size()); i++) {
                    transactionList.add(allTransactions.get(i));
                }

                double totalBalance = totalIncome - totalExpense;
                income.setText(totalIncome+" tnd");
                expense.setText(totalExpense+" tnd");
                balance.setText(totalBalance+" tnd");

                transactionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // ken fama erreur
            }
        });
    }
}
