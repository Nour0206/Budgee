package com.example.budgetmanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryAdapter
        extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryActionListener {
        void onEdit(int position);
        void onDelete(int position);
    }

    private final List<Category> categoryList;
    private final OnCategoryActionListener listener;

    public CategoryAdapter(List<Category> categoryList,
                           OnCategoryActionListener listener) {
        this.categoryList = categoryList;
        this.listener     = listener;
    }

    @NonNull @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int pos) {
        Category c = categoryList.get(pos);
        holder.name.setText(c.getName());
        holder.type.setText(c.getType());
        holder.ivEdit.setOnClickListener(v -> listener.onEdit(holder.getAdapterPosition()));
        holder.ivDelete.setOnClickListener(v -> listener.onDelete(holder.getAdapterPosition()));
    }

    @Override public int getItemCount() {
        return categoryList.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView name, type;
        ImageView ivEdit, ivDelete;
        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEdit   = itemView.findViewById(R.id.ivEditCategory);
            ivDelete = itemView.findViewById(R.id.ivDeleteCategory);
            name     = itemView.findViewById(R.id.categoryName);
            type     = itemView.findViewById(R.id.categoryType);
        }
    }
}
