package com.example.sqliteFirebasedatabase;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class BookActivity extends Activity {
	TextView bookTitle;
	TextView authorName;
	Book selectedBook;
	JCGSQLiteHelper db;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_book);

		bookTitle = (TextView) findViewById(R.id.title);
		authorName = (TextView) findViewById(R.id.author);

		// get the intent that we have passed from SqliteFirebaseDemo
		Intent intent = getIntent();
		String id = intent.getStringExtra("book");

		// open the database of the application context
		db = new JCGSQLiteHelper(getApplicationContext());

		// read the book with "id" from the database
		selectedBook = db.readBook(id);

		initializeViews();
	}

	public void initializeViews() {
		bookTitle.setText(selectedBook.getTitle());
		authorName.setText(selectedBook.getAuthor());
	}

	public void update(View v) {
		Toast.makeText(getApplicationContext(), "This book is updated.", Toast.LENGTH_SHORT).show();
		selectedBook.setTitle(((EditText) findViewById(R.id.titleEdit)).getText().toString());
		selectedBook.setAuthor(((EditText) findViewById(R.id.authorEdit)).getText().toString());

		// update book with changes
		db.updateBook(selectedBook,true,null);
		finish();
	}

	public void delete(View v) {
		Toast.makeText(getApplicationContext(), "This book is deleted.", Toast.LENGTH_SHORT).show();

		// delete selected book
		db.deleteBook(selectedBook,true,null);
		finish();
	}
}
