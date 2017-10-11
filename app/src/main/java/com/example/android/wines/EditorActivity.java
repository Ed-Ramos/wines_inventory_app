package com.example.android.wines;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.wines.data.WineContract.WineEntry;
import com.example.android.wines.data.WineDbHelper;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = WineDbHelper.class.getSimpleName();

    /**
     * Identifier for the wine data loader
     */
    private static final int EXISTING_WINE_LOADER = 0;

    /**
     * Content URI for the existing wine (null if it's a new wine)
     */
    private Uri mCurrentWineUri;

    /**
     * Content URI for the existing wine image (null if it's a new wine)
     */
    private Uri mCurrentWineImageUri;

    private static final int WINE_IMAGE_REQUEST_CODE = 100;
    /**
     * EditText field to enter the wine's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the wine's winery
     */
    private EditText mWineryEditText;

    /**
     * EditText field to enter the wine's year
     */
    private EditText mYearEditText;

    /**
     * EditText field to enter the wine's quantity
     */
    private EditText mQuantityEditText;

    /**
     * EditText field to enter the wine's price
     */
    private EditText mPriceEditText;

    /**
     * EditText field to enter the wine's winery email
     */
    private EditText mEmailEditText;

    /**
     * EditText field to enter the wine's winery phone number
     */
    private EditText mPhoneEditText;

    /**
     * ImageView field to enter the wine's image
     */
    private ImageView mWineImageView;

    /**
     * Boolean flag that keeps track of whether the wine has been edited (true) or not (false)
     */
    private boolean mWineHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mWineHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mWineHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //Examine the intent that was used to launch this activity,
        //in order to figure out if we're creating a new wine or editing and existing wine
        Intent intent = getIntent();
        mCurrentWineUri = intent.getData();

        // If the intent DOES NOT contain a pet URI, then we know that we are
        //creating a new pet
        if (mCurrentWineUri == null) {
            // This is a new Wine, so change the app bar to say "Add a Wine"
            setTitle(getString(R.string.editor_activity_title_new_wine));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a wine that hasn't been created yet.)
            invalidateOptionsMenu();

        } else {
            // Otherwise this is an existing wine, so change the app bar to say "Edit Wine"
            setTitle(getString(R.string.editor_activity_title_edit_wine));

            // Initialize a loader to read the pet data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_WINE_LOADER, null, this);

        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_wine_name);
        mWineryEditText = (EditText) findViewById(R.id.edit_wine_winery);
        mYearEditText = (EditText) findViewById(R.id.edit_wine_year);
        mQuantityEditText = (EditText) findViewById(R.id.edit_wine_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_wine_price);
        mEmailEditText = (EditText) findViewById(R.id.edit_wine_winery_email);
        mPhoneEditText = (EditText) findViewById(R.id.edit_wine_winery_phone);
        mWineImageView = (ImageView) findViewById(R.id.wine_image);

        // intent to pick wine image
        mWineImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                } else {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                }
                intent.setType("image/*");
                startActivityForResult(intent, WINE_IMAGE_REQUEST_CODE);
            }
        });

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mWineryEditText.setOnTouchListener(mTouchListener);
        mYearEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mEmailEditText.setOnTouchListener(mTouchListener);
        mPhoneEditText.setOnTouchListener(mTouchListener);
        mWineImageView.setOnTouchListener(mTouchListener);

    }//End of onCreate method

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == WINE_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            //If wrong code is returned or result is not ok, then return early and show toast message
            if (data == null) {
                Toast.makeText(this, getString(R.string.editor_wine_image_failed), Toast.LENGTH_SHORT).show();
                return;
            }
            //Store the URI of chosen image
            mCurrentWineImageUri = data.getData();
            //Set it on the ImageView
            mWineImageView.setImageURI(mCurrentWineUri);
            //Change scaleType from centerInside to centerCrop so its takes entire view
            mWineImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }

    private void saveWine() {

        // Read from input fields
        // Use trim to eliminate leading or trailing white space

        String nameString = mNameEditText.getText().toString().trim();
        String wineryString = mWineryEditText.getText().toString().trim();
        String yearString = mYearEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String emailString = mEmailEditText.getText().toString().trim();
        String phoneString = mPhoneEditText.getText().toString().trim();
        String imageString = null;

        if (mCurrentWineUri == null) {

            if (mCurrentWineImageUri != null) {
                imageString = mCurrentWineImageUri.toString();
            }

        } else {
            imageString = mCurrentWineImageUri.toString();
            Log.e(LOG_TAG, "imageUri " + imageString);
        }

        // Check if this is supposed to be a new wine
        // and check if all the fields in the editor are blank
        if (mCurrentWineUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(wineryString) &&
                TextUtils.isEmpty(yearString) && TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(priceString) && TextUtils.isEmpty(emailString) &&
                TextUtils.isEmpty(phoneString) && TextUtils.isEmpty(imageString)) {
            // Since no fields were modified, we can return early without creating a new wine.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, getString(R.string.editor_require_wine_name),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(wineryString)) {
            Toast.makeText(this, getString(R.string.editor_require_wine_winery),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (Integer.parseInt(yearString) < 1800) {
            Toast.makeText(this, getString(R.string.editor_invalid_wine_year),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(yearString)) {
            Toast.makeText(this, getString(R.string.editor_require_wine_year),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, getString(R.string.editor_require_wine_quantity),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, getString(R.string.editor_require_wine_price),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(emailString)) {
            Toast.makeText(this, getString(R.string.editor_require_wine_email),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(phoneString)) {
            Toast.makeText(this, getString(R.string.editor_require_wine_phone),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(imageString)) {
            Toast.makeText(this, getString(R.string.editor_require_wine_phone),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a ContentValues object
        ContentValues values = new ContentValues();
        values.put(WineEntry.COLUMN_WINE_NAME, nameString);
        values.put(WineEntry.COLUMN_WINE_WINERY, wineryString);
        values.put(WineEntry.COLUMN_WINE_YEAR, yearString);
        values.put(WineEntry.COLUMN_WINE_QUANTITY, quantityString);
        values.put(WineEntry.COLUMN_WINE_PRICE, priceString);
        values.put(WineEntry.COLUMN_WINE_EMAIL, emailString);
        values.put(WineEntry.COLUMN_WINE_PHONE, phoneString);
        values.put(WineEntry.COLUMN_WINE_IMAGE, imageString);

        // Determine if this is a new or existing wine by checking if mCurrentWineUri is null or not
        if (mCurrentWineUri == null) {
            // This is a NEW wine, so insert a new wine into the provider,
            // returning the content URI for the new wine.
            Uri newUri = getContentResolver().insert(WineEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_wine_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_wine_successful),
                        Toast.LENGTH_SHORT).show();

                finish();
            }

        } else {
            // Otherwise this is an EXISTING wine, so update the wine with content URI: mCurrentWineUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentWineUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentWineUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_wine_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_wine_successful),
                        Toast.LENGTH_SHORT).show();
            }

            finish();
        }

    }//End of saveWine method

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentWineUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveWine();
                //finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:

                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the wine hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.

                if (!mWineHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * +     * This method is called when the back button is pressed.
     * +
     */

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mWineHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
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
                WineEntry.COLUMN_WINE_PRICE,
                WineEntry.COLUMN_WINE_EMAIL,
                WineEntry.COLUMN_WINE_PHONE,
                WineEntry.COLUMN_WINE_IMAGE};

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
            int emailColumnIndex = cursor.getColumnIndex(WineEntry.COLUMN_WINE_EMAIL);
            int phoneColumnIndex = cursor.getColumnIndex(WineEntry.COLUMN_WINE_PHONE);
            int imageColumnIndex = cursor.getColumnIndex(WineEntry.COLUMN_WINE_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String winery = cursor.getString(wineryColumnIndex);
            int year = cursor.getInt(yearColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            Float price = cursor.getFloat(priceColumnIndex);
            String email = cursor.getString(emailColumnIndex);
            String phone = cursor.getString(phoneColumnIndex);
            String image = cursor.getString(imageColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mWineryEditText.setText(winery);
            mYearEditText.setText(Integer.toString(year));
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(Float.toString(price));
            mEmailEditText.setText(email);
            mPhoneEditText.setText(phone);
            Uri uri = Uri.parse(image);
            mWineImageView.setImageURI(uri);
            mCurrentWineImageUri = uri;
            mWineImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

    }//End of OnLoaderFinished

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mWineryEditText.setText("");
        mYearEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
        mEmailEditText.setText("");
        mPhoneEditText.setText("");
        mWineImageView.setImageURI(null);
    }

    /**
     * +     * Show a dialog that warns the user there are unsaved changes that will be lost
     * +     * if they continue leaving the editor.
     * +     *
     * +     * @param discardButtonClickListener is the click listener for what to do when
     * +     *                                   the user confirms they want to discard their changes
     * +
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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

    /**
     * Perform the deletion of the wine in the database.
     */
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

}//End of EditorActivity class
