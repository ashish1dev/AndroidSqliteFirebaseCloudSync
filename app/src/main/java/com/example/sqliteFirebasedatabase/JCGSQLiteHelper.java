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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Map;

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
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    //Firebase DB
    public  FirebaseDatabase mFirebase ;
    // [START declare_database_ref]
    public  DatabaseReference mDatabase;
    // [END declare_database_ref]

    public static DatabaseReference mBooksReference;
    static boolean calledAlready = false;

    public static DatabaseReference libraryRef;

    public JCGSQLiteHelper(Context context) {
        super(context, database_NAME, null, database_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create book table
        String CREATE_BOOK_TABLE = "CREATE TABLE books ( " + "id TEXT PRIMARY KEY , " + "title TEXT, " + "author TEXT )";
        db.execSQL(CREATE_BOOK_TABLE);

        // [START initialize_database_ref]

        if (!calledAlready)
        {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);

            calledAlready = true;
        }

        mFirebase = FirebaseDatabase.getInstance();// mFirebase.setPersistenceEnabled(true);

        mDatabase = mFirebase.getReference();
        mDatabase.keepSynced(true);
       // FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        // [END initialize_database_ref]


        // Initialize Database
        mBooksReference = mDatabase.child("Books");
        mBooksReference.keepSynced(true);
        //mBooksReference.removeValue();

        libraryRef = mDatabase.child("Library");
        libraryRef.keepSynced(true);
        //transaction example function
        runTransactionExample(libraryRef);

        // Retrieve new Books as they are added to our database
        mBooksReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
               // Book newBook = dataSnapshot.getValue(Book.class);

                Log.d("data",dataSnapshot.getValue().toString());
                String key = dataSnapshot.getKey();
                Map<String, Object> newPost = (Map<String, Object>) dataSnapshot.getValue();
                String title = newPost.get("title").toString();
                String author = newPost.get("author").toString();
                String id = key;

                Book newBook = new Book();
                newBook.setId(id);
                newBook.setTitle(title);
                newBook.setAuthor(author);

                createBook(newBook,false);
                SqliteFirebaseDemo.refreshList();
                Log.d("Data onChildAdded", dataSnapshot.getValue().toString());

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                Log.d("data key",key);
                //Book updatedBook = dataSnapshot.getValue(Book.class);

                Map<String, Object> newPost = (Map<String, Object>) dataSnapshot.getValue();
                String title = newPost.get("title").toString();
                String author = newPost.get("author").toString();
                String id = key;

                Book updatedBook = new Book();
                updatedBook.setId(id);
                updatedBook.setTitle(title);
                updatedBook.setAuthor(author);

                updateBook(updatedBook,false, key);
                SqliteFirebaseDemo.refreshList();
                Log.d("Data onChildChanged", dataSnapshot.getValue().toString());

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                //Book deletedBook = dataSnapshot.getValue(Book.class);

                Map<String, Object> newPost = (Map<String, Object>) dataSnapshot.getValue();
                String title = newPost.get("title").toString();
                String author = newPost.get("author").toString();
                String id = key;

                Book deletedBook = new Book();
                deletedBook.setId(id);
                deletedBook.setTitle(title);
                deletedBook.setAuthor(author);

                deleteBook(deletedBook,false,key);
                SqliteFirebaseDemo.refreshList();
                Log.d("Data onChildRemoved", dataSnapshot.getValue().toString());


            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d("Data onChildMoved", dataSnapshot.getValue().toString());


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
            String key = mBooksReference.push().getKey();
            book.setId(key);
            // Push the object, it will appear in the list
            DatabaseReference keyRef = mBooksReference.child(key);

            Map<String, String> bookData = new HashMap<String, String>();
            bookData.put("title",book.getTitle());
            bookData.put("author",book.getAuthor());
            bookData.put("createdAt", dateFormat.format(new Date()));

            keyRef.setValue(bookData);
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

            DatabaseReference keyRef = mBooksReference.child(book.getId() + "");

            Map<String, String> bookData = new HashMap<String, String>();
            bookData.put("title",book.getTitle());
            bookData.put("author",book.getAuthor());
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            bookData.put("updatedAt", dateFormat.format(new Date()));

            keyRef.setValue(bookData);
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

    public void runTransactionExample(DatabaseReference libRef){

        DatabaseReference ref = libRef.child("value");

        System.out.println("ref ="+ref);

        if(ref==null){
            ref.setValue(null);
        }


        ref.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(final MutableData currentData) {
                System.out.println("K:V = "+currentData.getKey()+":"+currentData.getValue());
                if (currentData.getValue() == null) {
                    currentData.setValue(1);
                } else {
                    currentData.setValue((Long) currentData.getValue() + 1);
                }

                return Transaction.success(currentData);
            }


            @Override
            public void onComplete(DatabaseError firebaseError, boolean committed, DataSnapshot currentData) {
                if (firebaseError != null) {
                    System.out.println(firebaseError);
                    System.out.println("value counter increment failed.");
                } else {
                    System.out.println("value counter increment succeeded.");
                }
            }
        });


    }
}