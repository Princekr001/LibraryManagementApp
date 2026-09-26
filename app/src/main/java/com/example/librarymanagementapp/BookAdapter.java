package com.example.librarymanagementapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    public interface OnBookActionListener {
        void onEdit(Book book);
        void onDelete(Book book);
        void onIssue(Book book);
        void onReturn(Book book);
    }

    private List<Book> books;
    private final OnBookActionListener listener;

    public BookAdapter(List<Book> books, OnBookActionListener listener) {
        this.books = books;
        this.listener = listener;
    }

    public void updateData(List<Book> newBooks) {
        this.books = newBooks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        holder.title.setText(book.getTitle());
        holder.author.setText(book.getAuthor());
        holder.isbn.setText("ISBN: " + book.getIsbn());
        holder.availability.setText(book.getAvailableCopies() + " / " + book.getTotalCopies() + " available");

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(book));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(book));
        holder.btnIssue.setOnClickListener(v -> listener.onIssue(book));
        holder.btnReturn.setOnClickListener(v -> listener.onReturn(book));

        holder.btnIssue.setEnabled(book.getAvailableCopies() > 0);
        holder.btnReturn.setEnabled(book.getAvailableCopies() < book.getTotalCopies());
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView title, author, isbn, availability;
        Button btnEdit, btnDelete, btnIssue, btnReturn;

        BookViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            author = itemView.findViewById(R.id.tvAuthor);
            isbn = itemView.findViewById(R.id.tvIsbn);
            availability = itemView.findViewById(R.id.tvAvailability);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnIssue = itemView.findViewById(R.id.btnIssue);
            btnReturn = itemView.findViewById(R.id.btnReturn);
        }
    }
}
