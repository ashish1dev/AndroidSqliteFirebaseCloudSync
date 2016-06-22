package com.example.sqliteFirebasedatabase;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AddBookActivity extends Activity {
    TextView bookTitle;
    TextView authorName;
    Book newBook;
    JCGSQLiteHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        bookTitle = (TextView) findViewById(R.id.title);
        authorName = (TextView) findViewById(R.id.author);

        // open the database of the application context
        db = new JCGSQLiteHelper(getApplicationContext());
    }

    public void create(View v) {

        newBook = new Book();
        newBook.setTitle(((EditText) findViewById(R.id.titleEdit)).getText().toString());
        newBook.setAuthor(((EditText) findViewById(R.id.authorEdit)).getText().toString());

        // update book with changes
        db.createBook(newBook,true);
        Toast.makeText(getApplicationContext(), "Created New Book.", Toast.LENGTH_SHORT).show();
        finish();
    }
}
