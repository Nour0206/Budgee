package com.example.budgetmanager;

public class Category {
    private String name;
    private String type; // "Expense" or "Income"

    // Required empty constructor for Firebase
    public Category() { }

    public Category(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name; }
    public String getType() {
        return type; }
    public void setName(String name) {
        this.name = name; }
    public void setType(String type) {
        this.type = type; }
}
