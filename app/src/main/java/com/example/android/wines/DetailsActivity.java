package com.example.android.wines;


import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.wines.data.WineContract.WineEntry;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{


    /**
     * Identifier for the wine data loader
     */
    private static final int EXISTING_WINE_LOADER = 1;

    /**
     * Content URI for the existing wine being displayed
     */
    private Uri mCurrentWineUri;

    /**
     * TextView field to display the wine's name
     */
    private TextView mNameTextView;


    /**
     * TextView field to display the wine's winery
     */
    private TextView mWineryTextView;

    /**
     * TextView field to display the wine's year
     */
    private TextView mYearTextView;


    /**
     * TextView field to display the wine's quantity
     */
    private TextView mQuantityTextView;

    /**
     * TextView field to display the wine's price
     */
    private TextView mPriceTextView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        //Get the intent used to launch this activity. Assign data to mCurrentWineUri variable
        Intent intent = getIntent();
        mCurrentWineUri = intent.getData();



        // Find all relevant views that we will need to read user input from
        mNameTextView = (TextView) findViewById(R.id.details_wine_name);
        mWineryTextView = (TextView) findViewById(R.id.details_wine_winery);
        mYearTextView = (TextView) findViewById(R.id.details_wine_year);
        mQuantityTextView = (TextView) findViewById(R.id.details_wine_quantity);
        mPriceTextView = (TextView) findViewById(R.id.details_wine_price);


        getLoaderManager().initLoader(EXISTING_WINE_LOADER, null, this);

    }//End OnCreate method


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_wine:
                Intent intent = new Intent(DetailsActivity.this, EditorActivity.class);
                intent.setData(mCurrentWineUri);
                startActivity(intent);
                return true;
            case R.id.action_delete:

                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all wine attributes, define a projection that contains
        // all columns from the wine table
        String[] projection = {
                WineEntry._ID,
                WineEntry.COLUMN_WINE_NAME,
                WineEntry.COLUMN_WINE_WINERY,
                WineEntry.COLUMN_WINE_YEAR,
                WineEntry.COLUMN_WINE_QUANTITY,
                WineEntry.COLUMN_WINE_PRICE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentWineUri,         // Query the content URI for the current wine
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(WineEntry.COLUMN_WINE_NAME);
            int wineryColumnIndex = cursor.getColumnIndex(WineEntry.COLUMN_WINE_WINERY);
            int yearColumnIndex = cursor.getColumnIndex(WineEntry.COLUMN_WINE_YEAR);
            int quantityColumnIndex = cursor.getColumnIndex(WineEntry.COLUMN_WINE_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(WineEntry.COLUMN_WINE_PRICE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String winery = cursor.getString(wineryColumnIndex);
            int year = cursor.getInt(yearColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int price = cursor.getInt(priceColumnIndex);

            // Update the views on the screen with the values from the database
            mNameTextView.setText(name);
            mWineryTextView.setText(winery);
            mYearTextView.setText(Integer.toString(year));
            mQuantityTextView.setText(Integer.toString(quantity));
            mPriceTextView.setText(Integer.toString(price));
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {


        // If the loader is invalidated, clear out all the data from the input fields.
        mNameTextView.setText("");
        mWineryTextView.setText("");
        mYearTextView.setText("");
        mQuantityTextView.setText("");
        mPriceTextView.setText("");

    }


    /**
     * +     * Prompt the user to confirm that they want to delete this wine.
     * +
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteWine();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the wine.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void deleteWine() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentWineUri != null) {
            // Call the ContentResolver to delete the wine at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentWineUri
            // content URI already identifies the wine that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentWineUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_wine_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_wine_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

}//End of DetailsActivity


