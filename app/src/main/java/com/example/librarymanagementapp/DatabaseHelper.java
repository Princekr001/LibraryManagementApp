package com.example.librarymanagementapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "library.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_BOOKS = "books";
    public static final String COL_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_AUTHOR = "author";
    public static final String COL_ISBN = "isbn";
    public static final String COL_TOTAL = "total_copies";
    public static final String COL_AVAILABLE = "available_copies";

    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createBooksTable = "CREATE TABLE " + TABLE_BOOKS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TITLE + " TEXT NOT NULL, " +
                COL_AUTHOR + " TEXT, " +
                COL_ISBN + " TEXT, " +
                COL_TOTAL + " INTEGER, " +
                COL_AVAILABLE + " INTEGER)";
        db.execSQL(createBooksTable);

        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT UNIQUE NOT NULL, " +
                COL_PASSWORD + " TEXT NOT NULL)";
        db.execSQL(createUsersTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // ---------- User auth methods ----------

    // Returns true if the username was new and registration succeeded,
    // false if that username is already taken.
    public boolean registerUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_PASSWORD, password);
        long result = db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
        return result != -1;
    }

    // Returns true if the username/password combination matches a stored user.
    public boolean checkLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                COL_USERNAME + "=? AND " + COL_PASSWORD + "=?",
                new String[]{username, password}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // Create
    public long addBook(Book book) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, book.getTitle());
        values.put(COL_AUTHOR, book.getAuthor());
        values.put(COL_ISBN, book.getIsbn());
        values.put(COL_TOTAL, book.getTotalCopies());
        values.put(COL_AVAILABLE, book.getAvailableCopies());
        long id = db.insert(TABLE_BOOKS, null, values);
        db.close();
        return id;
    }

    // Read single
    public Book getBook(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BOOKS, null, COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        Book book = null;
        if (cursor.moveToFirst()) {
            book = cursorToBook(cursor);
        }
        cursor.close();
        db.close();
        return book;
    }

    // Read all
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BOOKS, null, null, null, null, null, COL_TITLE + " ASC");
        if (cursor.moveToFirst()) {
            do {
                books.add(cursorToBook(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return books;
    }

    // Search by title or author
    public List<Book> searchBooks(String query) {
        List<Book> books = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COL_TITLE + " LIKE ? OR " + COL_AUTHOR + " LIKE ? OR " + COL_ISBN + " LIKE ?";
        String[] args = new String[]{"%" + query + "%", "%" + query + "%", "%" + query + "%"};
        Cursor cursor = db.query(TABLE_BOOKS, null, selection, args, null, null, COL_TITLE + " ASC");
        if (cursor.moveToFirst()) {
            do {
                books.add(cursorToBook(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return books;
    }

    // Update
    public int updateBook(Book book) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, book.getTitle());
        values.put(COL_AUTHOR, book.getAuthor());
        values.put(COL_ISBN, book.getIsbn());
        values.put(COL_TOTAL, book.getTotalCopies());
        values.put(COL_AVAILABLE, book.getAvailableCopies());
        int rows = db.update(TABLE_BOOKS, values, COL_ID + "=?",
                new String[]{String.valueOf(book.getId())});
        db.close();
        return rows;
    }

    // Delete
    public void deleteBook(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BOOKS, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Issue a copy: decrease available count if any left
    public boolean issueBook(int id) {
        Book book = getBook(id);
        if (book == null || book.getAvailableCopies() <= 0) return false;
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        updateBook(book);
        return true;
    }

    // Return a copy: increase available count if not exceeding total
    public boolean returnBook(int id) {
        Book book = getBook(id);
        if (book == null || book.getAvailableCopies() >= book.getTotalCopies()) return false;
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        updateBook(book);
        return true;
    }

    private Book cursorToBook(Cursor cursor) {
        return new Book(
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_AUTHOR)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_ISBN)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_TOTAL)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_AVAILABLE))
        );
    }
}
