package com.example.sqliteFirebasedatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.LinkedList;
import java.util.List;


public class JCGSQLiteHelper extends SQLiteOpenHelper {

    // database version
    private static final int database_VERSION = 1;
    // database name
    private static final String database_NAME = "BookDB";
    private static final String table_BOOKS = "books";
    private static final String book_ID = "id";
    private static final String book_TITLE = "title";
    private static final String book_AUTHOR = "author";

    private static final String[] COLUMNS = {book_ID, book_TITLE, book_AUTHOR};


    //Firebase DB
    public static FirebaseDatabase mFirebase ;
    // [START declare_database_ref]
    public static DatabaseReference mDatabase;
    // [END declare_database_ref]

    public static DatabaseReference mBooksReference;

    public JCGSQLiteHelper(Context context) {
        super(context, database_NAME, null, database_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create book table
        String CREATE_BOOK_TABLE = "CREATE TABLE books ( " + "id TEXT PRIMARY KEY , " + "title TEXT, " + "author TEXT )";
        db.execSQL(CREATE_BOOK_TABLE);

        // [START initialize_database_ref]
        mFirebase = FirebaseDatabase.getInstance();
        mFirebase.setPersistenceEnabled(true);

        mDatabase = mFirebase.getReference();
        mDatabase.keepSynced(true);
       // FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        // [END initialize_database_ref]


        // Initialize Database
        mBooksReference = mDatabase.child("Books");
        mBooksReference.keepSynced(true);
        //mBooksReference.removeValue();

        // Retrieve new Books as they are added to our database
        mBooksReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Book newBook = dataSnapshot.getValue(Book.class);
                createBook(newBook,false);
                SqliteFirebaseDemo.refreshList();
                Log.d("Data onChildAdded", dataSnapshot.getValue().toString());
                //Toast.makeText(getBaseContext(), "data=" + dataSnapshot.getValue(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                Book updatedBook = dataSnapshot.getValue(Book.class);
                updateBook(updatedBook,false, key);
                SqliteFirebaseDemo.refreshList();
                Log.d("Data onChildChanged", dataSnapshot.getValue().toString());
                //Toast.makeText(getBaseContext(), "data=" + dataSnapshot.getValue(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                Book deletedBook = dataSnapshot.getValue(Book.class);
                deleteBook(deletedBook,false,key);
                SqliteFirebaseDemo.refreshList();
                Log.d("Data onChildRemoved", dataSnapshot.getValue().toString());
                //Toast.makeText(getBaseContext(), "data=" + dataSnapshot.getValue(), Toast.LENGTH_LONG).show();

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d("Data onChildMoved", dataSnapshot.getValue().toString());
                //Toast.makeText(getBaseContext(), "data=" + dataSnapshot.getValue(), Toast.LENGTH_LONG).show();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Data onCancelled", databaseError.toString());
            }
        });
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // drop books table if already exists
        db.execSQL("DROP TABLE IF EXISTS books");
        this.onCreate(db);
    }

    public void createBook(Book book, boolean toSync) {



        if(toSync){
//            book.setId(rowId);
            String key = mBooksReference.push().getKey();
            book.setId(key);
            // Push the object, it will appear in the list

            mBooksReference.child(key + "").setValue(book);

        }
        else{
            // get reference of the BookDB database
            SQLiteDatabase db = this.getWritableDatabase();

            // make values to be inserted
            ContentValues values = new ContentValues();


            values.put(book_ID, book.getId());
            values.put(book_TITLE, book.getTitle());
            values.put(book_AUTHOR, book.getAuthor());

            // insert book
            db.insert(table_BOOKS, null, values);
            // close database transaction
            db.close();

        }

    }

    public Book readBook(String id) {
        // get reference of the BookDB database
        SQLiteDatabase db = this.getReadableDatabase();

        // get book query
        Cursor cursor = db.query(table_BOOKS, // a. table
                COLUMNS, " id = ?", new String[]{id}, null, null, null, null);

        // if results !=null, parse the first one
        if (cursor != null)
            cursor.moveToFirst();

        Book book = new Book();
        book.setId((cursor.getString(0)));
        book.setTitle(cursor.getString(1));
        book.setAuthor(cursor.getString(2));

        return book;
    }

    public List<Book> getAllBooks() {
        List<Book> books = new LinkedList<Book>();

        // select book query
        String query = "SELECT  * FROM " + table_BOOKS;

        // get reference of the BookDB database
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // parse all results
        Book book = null;
        if (cursor.moveToFirst()) {
            do {
                book = new Book();
                book.setId((cursor.getString(0)));
                book.setTitle(cursor.getString(1));
                book.setAuthor(cursor.getString(2));

                // Add book to books
                books.add(book);
            } while (cursor.moveToNext());
        }
        return books;
    }

    public int updateBook(Book book, boolean toSync, String key) {

        // get reference of the BookDB database
        SQLiteDatabase db = this.getWritableDatabase();

        // make values to be inserted
        ContentValues values = new ContentValues();
        values.put("title", book.getTitle()); // get title
        values.put("author", book.getAuthor()); // get author

        // update
        int i = db.update(table_BOOKS, values, book_ID + " = ?", new String[]{String.valueOf(book.getId())});

        db.close();
        if(toSync) {
            mBooksReference.child(book.getId() + "").setValue(book);
        }
        return i;
    }

    // Deleting single book
    public void deleteBook(Book book, boolean toSync,String key) {

        // get reference of the BookDB database
        SQLiteDatabase db = this.getWritableDatabase();

        // delete book
        db.delete(table_BOOKS, book_ID + " = ?", new String[]{String.valueOf(book.getId())});
        db.close();
        if (toSync){
            mBooksReference.child(book.getId() + "").removeValue();
        }
    }
}