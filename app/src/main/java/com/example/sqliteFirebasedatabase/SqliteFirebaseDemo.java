package com.example.sqliteFirebasedatabase;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.util.Log;


public class SqliteFirebaseDemo extends ListActivity implements OnItemClickListener {
	public static JCGSQLiteHelper db  ;
	public static List<Book> list;
	public static ArrayAdapter<String> myAdapter;
	public static ListView lview;
	public static SqliteFirebaseDemo thisInstance;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		db = new JCGSQLiteHelper(this);
		// drop this database if already exists
		db.onUpgrade(db.getWritableDatabase(), 1, 2);

		// get all books
		list = db.getAllBooks();
		Log.d("Data : list",list.isEmpty()+"");
		Log.d("Data : list size",list.size()+"");


		List<String> listTitle = new ArrayList<String>();

		for (int i = 0; i < list.size(); i++) {
			listTitle.add(i, list.get(i).getTitle());
			Log.d("Data :",list.get(i).getTitle());
		}

		myAdapter = new ArrayAdapter<String>(this, R.layout.row_layout, R.id.listText, listTitle);
		getListView().setOnItemClickListener(this);
		thisInstance = this;
		lview = getListView();
		
		setListAdapter(myAdapter);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// start BookActivity with extras the book id
		Intent intent = new Intent(this, BookActivity.class);
		intent.putExtra("book", list.get(arg2).getId());
		startActivityForResult(intent, 1);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// get all books again, because something changed
		list = db.getAllBooks();

		List<String> listTitle = new ArrayList<String>();

		for (int i = 0; i < list.size(); i++) {
			listTitle.add(i, list.get(i).getTitle());
		}

		myAdapter = new ArrayAdapter<String>(this, R.layout.row_layout, R.id.listText, listTitle);
		getListView().setOnItemClickListener(this);
		setListAdapter(myAdapter);
	}

	public void AddNewBook(View v){
		// start AddBookActivity with extras the book id
		Intent intent = new Intent(this, AddBookActivity.class);

		startActivityForResult(intent, 1);
	}

	public static void refreshList(){
		// get all books again, because something changed
		list = db.getAllBooks();

		List<String> listTitle = new ArrayList<String>();

		for (int i = 0; i < list.size(); i++) {
			listTitle.add(i, list.get(i).getTitle());
		}

		myAdapter = new ArrayAdapter<String>(thisInstance, R.layout.row_layout, R.id.listText, listTitle);

		lview.setOnItemClickListener(thisInstance);
		lview.setAdapter(myAdapter);
	}
}
