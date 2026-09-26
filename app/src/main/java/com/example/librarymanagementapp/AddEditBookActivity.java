package com.example.librarymanagementapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddEditBookActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private EditText etTitle, etAuthor, etIsbn, etQuantity;
    private Button btnSave;
    private TextView tvHeader;

    private int bookId = -1; // -1 means "add new"
    private Book existingBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_book);

        dbHelper = new DatabaseHelper(this);

        tvHeader = findViewById(R.id.tvHeader);
        etTitle = findViewById(R.id.etTitle);
        etAuthor = findViewById(R.id.etAuthor);
        etIsbn = findViewById(R.id.etIsbn);
        etQuantity = findViewById(R.id.etQuantity);
        btnSave = findViewById(R.id.btnSave);

        bookId = getIntent().getIntExtra(MainActivity.EXTRA_BOOK_ID, -1);

        if (bookId != -1) {
            tvHeader.setText("Edit Book");
            existingBook = dbHelper.getBook(bookId);
            if (existingBook != null) {
                etTitle.setText(existingBook.getTitle());
                etAuthor.setText(existingBook.getAuthor());
                etIsbn.setText(existingBook.getIsbn());
                etQuantity.setText(String.valueOf(existingBook.getTotalCopies()));
            }
        } else {
            tvHeader.setText("Add New Book");
        }

        btnSave.setOnClickListener(v -> saveBook());
    }

    private void saveBook() {
        String title = etTitle.getText().toString().trim();
        String author = etAuthor.getText().toString().trim();
        String isbn = etIsbn.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            return;
        }
        if (quantityStr.isEmpty()) {
            etQuantity.setError("Quantity is required");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity < 1) {
                etQuantity.setError("Quantity must be at least 1");
                return;
            }
        } catch (NumberFormatException e) {
            etQuantity.setError("Enter a valid number");
            return;
        }

        if (bookId != -1 && existingBook != null) {
            // Editing: preserve issued-copy count by adjusting available copies
            // proportionally if total copies changed.
            int issuedCopies = existingBook.getTotalCopies() - existingBook.getAvailableCopies();
            int newAvailable = Math.max(quantity - issuedCopies, 0);

            existingBook.setTitle(title);
            existingBook.setAuthor(author);
            existingBook.setIsbn(isbn);
            existingBook.setTotalCopies(quantity);
            existingBook.setAvailableCopies(newAvailable);

            dbHelper.updateBook(existingBook);
            Toast.makeText(this, "Book updated", Toast.LENGTH_SHORT).show();
        } else {
            Book newBook = new Book(title, author, isbn, quantity);
            dbHelper.addBook(newBook);
            Toast.makeText(this, "Book added", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}
