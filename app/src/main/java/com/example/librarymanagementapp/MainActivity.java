package com.example.librarymanagementapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity implements BookAdapter.OnBookActionListener {

    public static final String EXTRA_BOOK_ID = "extra_book_id";

    private DatabaseHelper dbHelper;
    private BookAdapter adapter;
    private RecyclerView recyclerView;
    private EditText searchBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerView);
        searchBox = findViewById(R.id.searchBox);
        FloatingActionButton fab = findViewById(R.id.fabAddBook);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookAdapter(dbHelper.getAllBooks(), this);
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddEditBookActivity.class)));

        searchBox.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBooks(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Logout");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(LoginActivity.KEY_LOGGED_IN, false).apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void refreshList() {
        String query = searchBox.getText() != null ? searchBox.getText().toString() : "";
        filterBooks(query);
    }

    private void filterBooks(String query) {
        List<Book> books = query.isEmpty() ? dbHelper.getAllBooks() : dbHelper.searchBooks(query);
        adapter.updateData(books);
    }

    @Override
    public void onEdit(Book book) {
        Intent intent = new Intent(this, AddEditBookActivity.class);
        intent.putExtra(EXTRA_BOOK_ID, book.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(Book book) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Book")
                .setMessage("Delete \"" + book.getTitle() + "\"? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteBook(book.getId());
                    Toast.makeText(this, "Book deleted", Toast.LENGTH_SHORT).show();
                    refreshList();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onIssue(Book book) {
        boolean success = dbHelper.issueBook(book.getId());
        Toast.makeText(this, success ? "Book issued" : "No copies available", Toast.LENGTH_SHORT).show();
        refreshList();
    }

    @Override
    public void onReturn(Book book) {
        boolean success = dbHelper.returnBook(book.getId());
        Toast.makeText(this, success ? "Book returned" : "All copies already returned", Toast.LENGTH_SHORT).show();
        refreshList();
    }
}
