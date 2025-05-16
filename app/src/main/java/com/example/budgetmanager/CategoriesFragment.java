package com.example.budgetmanager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class CategoriesFragment extends Fragment {

    private RecyclerView recyclerView;
    private RadioGroup radioGroup;
    private CategoryAdapter adapter;
    private List<Category> categoryList;
    private List<String> categoryKeys;
    private DatabaseReference categoryRef;
    private String filterType = "Expense";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);

        // Bind views
        recyclerView = view.findViewById(R.id.rvCategories);
        radioGroup   = view.findViewById(R.id.radioGroupType);
        ImageView ivAdd = view.findViewById(R.id.ivAddCategory);

        // Set default selected RadioButton to Expense
        radioGroup.check(R.id.rbExpense);  // 👈 this line sets the default selection

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        categoryList = new ArrayList<>();
        categoryKeys = new ArrayList<>();
        adapter = new CategoryAdapter(categoryList, new CategoryAdapter.OnCategoryActionListener() {
            @Override
            public void onEdit(int pos) {
                String key = categoryKeys.get(pos);
                Category cat = categoryList.get(pos);
                showEditDialog(key, cat);
            }
            @Override
            public void onDelete(int pos) {
                confirmDelete(pos);
            }
        });
        recyclerView.setAdapter(adapter);

        // Firebase reference
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        categoryRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("categories");

        // Add‐button listener
        ivAdd.setOnClickListener(v -> showAddDialog());

        // Default load & filter listener
        loadCategories(filterType); // filterType is already initialized as "Expense"
        radioGroup.setOnCheckedChangeListener((g, id) -> {
            filterType = (id == R.id.rbExpense) ? "Expense" : "Income";
            loadCategories(filterType);
        });

        return view;
    }


    private void loadCategories(String typeFilter) {
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                categoryList.clear();
                categoryKeys.clear();
                for (DataSnapshot ds : snap.getChildren()) {
                    Category c = ds.getValue(Category.class);
                    if (c != null && c.getType().equalsIgnoreCase(typeFilter)) {
                        categoryList.add(c);
                        categoryKeys.add(ds.getKey());
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError err) {
                Toast.makeText(getContext(),
                        "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddDialog() {
        View dlgView = LayoutInflater.from(getContext())
                .inflate(R.layout.edit_category, null, false);

        EditText etName = dlgView.findViewById(R.id.etCategoryName);
        RadioGroup rg   = dlgView.findViewById(R.id.rgType);
        rg.check(R.id.rbExpenseDialog);  // default selection

        new AlertDialog.Builder(requireContext())
                .setTitle("Add Category")
                .setView(dlgView)
                .setPositiveButton("Add", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(getContext(),
                                "Name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String type = (rg.getCheckedRadioButtonId() == R.id.rbIncomeDialog)
                            ? "Income" : "Expense";

                    String key = categoryRef.push().getKey();
                    if (key != null) {
                        categoryRef.child(key)
                                .setValue(new Category(name, type))
                                .addOnSuccessListener(v -> {
                                    Toast.makeText(getContext(),
                                            "Category added", Toast.LENGTH_SHORT).show();
                                    loadCategories(filterType);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(),
                                            "Failed to add", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditDialog(String key, Category cat) {
        View dlgView = LayoutInflater.from(getContext())
                .inflate(R.layout.edit_category, null, false);

        EditText etName = dlgView.findViewById(R.id.etCategoryName);
        RadioGroup rg   = dlgView.findViewById(R.id.rgType);
        RadioButton rbExp = dlgView.findViewById(R.id.rbExpenseDialog);
        RadioButton rbInc = dlgView.findViewById(R.id.rbIncomeDialog);

        // Prefill
        etName.setText(cat.getName());
        if ("Income".equalsIgnoreCase(cat.getType())) rbInc.setChecked(true);
        else                                         rbExp.setChecked(true);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Category")
                .setView(dlgView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    if (TextUtils.isEmpty(newName)) {
                        Toast.makeText(getContext(),
                                "Name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String newType = (rg.getCheckedRadioButtonId() == R.id.rbIncomeDialog)
                            ? "Income" : "Expense";

                    categoryRef.child(key)
                            .setValue(new Category(newName, newType))
                            .addOnSuccessListener(v -> {
                                Toast.makeText(getContext(),
                                        "Category updated", Toast.LENGTH_SHORT).show();
                                loadCategories(filterType);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(),
                                        "Update failed", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(int pos) {
        String key  = categoryKeys.get(pos);
        String name = categoryList.get(pos).getName();

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete \"" + name + "\"?")
                .setMessage("This action cannot be undone.")
                .setPositiveButton("Delete", (d, w) ->
                        categoryRef.child(key).removeValue()
                                .addOnSuccessListener(v -> {
                                    Toast.makeText(getContext(),
                                            "Category deleted", Toast.LENGTH_SHORT).show();
                                    loadCategories(filterType);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(),
                                            "Delete failed", Toast.LENGTH_SHORT).show();
                                })
                )
                .setNegativeButton("Cancel", null)
                .show();
    }
}