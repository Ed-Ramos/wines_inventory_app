package com.example.android.wines;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.wines.data.WineContract.WineEntry;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    //Identifier for the wine data loader
    private static final int WINE_LOADER = 0;

    //Adapter for the ListView
    WineCursorAdapter mCursorAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        ListView wineListView = (ListView) findViewById(R.id.list);

        View emptyView = findViewById(R.id.empty_view);
        wineListView.setEmptyView(emptyView);

        mCursorAdapter = new WineCursorAdapter(this, null);
        wineListView.setAdapter(mCursorAdapter);

      //Setup the item click listener
        wineListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, DetailsActivity.class);

                //Form the content uri that represents the specific wine that was clicked on,
                //by appending the "id" passed as input to this method onto the
                //{@link WineEntry#Content_URI}.
                //For example, the URI would be "content://com.example.android.wines/wines/2"
                //if the wine with ID 2 was clicked on
                Uri currentWineUri = ContentUris.withAppendedId(WineEntry.CONTENT_URI, id);

                intent.setData(currentWineUri);

                startActivity(intent);

            }

        });

        // kick off the loader
        getSupportLoaderManager().initLoader(WINE_LOADER, null, this);

    }//End OnCreate


    private void insertWine() {

        // Create a ContentValues object where column names are the keys,
        // and Merlot's wine attributes are the values.
        ContentValues values = new ContentValues();
        values.put(WineEntry.COLUMN_WINE_NAME, "Merlot");
        values.put(WineEntry.COLUMN_WINE_WINERY, "Mondalvi");
        values.put(WineEntry.COLUMN_WINE_YEAR, 2015);
        values.put(WineEntry.COLUMN_WINE_QUANTITY, 10);
        values.put(WineEntry.COLUMN_WINE_PRICE, 7);

        // Insert a new row for Merlot into the provider using the ContentResolver.
        // Use the {@link wineEntry#CONTENT_URI} to indicate that we want to insert
        // into the wines database table.
        // Receive the new content URI that will allow us to access Merlot's data in the future.
        Uri newUri = getContentResolver().insert(WineEntry.CONTENT_URI, values);

    }

    /**
     * Helper method to delete all wines in the database.
     */
    private void deleteAllWines() {
        int rowsDeleted = getContentResolver().delete(WineEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from wine database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertWine();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllWines();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
     // define a projection that specifies the columns from the table we care about

        String[] projection = {
                WineEntry._ID,
                WineEntry.COLUMN_WINE_NAME,
                WineEntry.COLUMN_WINE_QUANTITY,
                WineEntry.COLUMN_WINE_PRICE};
        // This loader will execute the ContentProvider's query method on a background thread

        return new CursorLoader(this,
                WineEntry.CONTENT_URI, //Provider content URI to query
                projection,           // Columns to include in the resulting cursor
                null,                 // no selection clause
                null,                 //no selection clause
                null);                // Default sort order

    }

        @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            // Update {link WineCursorAdapter} with this new cursor containing updated wine data
            mCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);

    }
}//End of CatalogActivity class
