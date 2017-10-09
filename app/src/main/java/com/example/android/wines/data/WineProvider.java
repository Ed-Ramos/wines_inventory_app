package com.example.android.wines.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.wines.data.WineContract.WineEntry;

/**
 * {@link ContentProvider} for Wines app.
 */

public class WineProvider extends ContentProvider {

    /**
     * URI matcher code for the content URI for the wines table
     */
    public static final int WINES = 100;

    /**
     * URI matcher code for the content URI for a single wine in the wines table
     */
    public static final int WINE_ID = 101;

    /**
     * URI matcher object to match a context URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.wines/wines" will map to the
        // integer code {@link #WINES}. This URI is used to provide access to MULTIPLE rows
        // of the wines table.
        sUriMatcher.addURI(WineContract.CONTENT_AUTHORITY, WineContract.PATH_WINES, WINES);

        // The content URI of the form "content://com.example.android.wines/wines/#" will map to the
        // integer code {@link #WINES_ID}. This URI is used to provide access to ONE single row
        // of the wines table.

        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.wines/wines/3" matches, but
        // "content://com.example.android.wines/wines" (without a number at the end) doesn't match.
        sUriMatcher.addURI(WineContract.CONTENT_AUTHORITY, WineContract.PATH_WINES + "/#", WINE_ID);
    }//End static

    private WineDbHelper mDbHelper;

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = WineProvider.class.getSimpleName();

    /**
     * Initialize the provider and the database helper object.
     */

    @Override
    public boolean onCreate() {

        mDbHelper = new WineDbHelper(getContext());

        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case WINES:
                // For the WINES code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the wines table.
                cursor = database.query(WineContract.WineEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case WINE_ID:
                // For the WINE_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.wines/wines/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = WineEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the wines table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(WineEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        //Set notification URI on the Cursor,
        //so we know what content URI the Cursor was created for.
        //If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        //return the cursor
        return cursor;
    }//End query method

    /**
     * Insert new data into the provider with the given ContentValues.
     */

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case WINES:
                return insertWine(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertWine(Uri uri, ContentValues values) {

        // Check that the name is not null
        String name = values.getAsString(WineEntry.COLUMN_WINE_NAME);
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Pet requires a name");
        }

        // Check that the winery is not null
        String winery = values.getAsString(WineEntry.COLUMN_WINE_WINERY);
        if (winery == null || winery.length() == 0) {
            throw new IllegalArgumentException("Wine requires a winery");
        }

        // If the year is provided, check that it's greater than or equal to 1800.
        // Store will never have bottles older than 1800
        Integer year = values.getAsInteger(WineEntry.COLUMN_WINE_YEAR);

        if (year != null && year < 1800) {
            throw new IllegalArgumentException("Wine requires valid year");
        }

        // If the quantity is provided, check that it's greater than or equal to 0 bottles
        Integer quantity = values.getAsInteger(WineEntry.COLUMN_WINE_QUANTITY);

        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Wine requires valid quantity");
        }

        // If the price is provided, check that it's greater than or equal to 0
        Float price = values.getAsFloat(WineEntry.COLUMN_WINE_PRICE);

        if (price != null && price < 0) {
            throw new IllegalArgumentException("Wine requires valid price");
        }

        // Check that the winery email is not null
        String email = values.getAsString(WineEntry.COLUMN_WINE_EMAIL);
        if (email == null || email.length() == 0) {
            throw new IllegalArgumentException("Wine requires a winery email");
        }

        // Check that the winery phone is not null
        String phone = values.getAsString(WineEntry.COLUMN_WINE_PHONE);
        if (phone == null || phone.length() == 0) {
            throw new IllegalArgumentException("Wine requires a winery phone");
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new wine with the given values
        long id = database.insert(WineEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        //Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);

    }//End InsertWine

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case WINES:
                return updateWine(uri, contentValues, selection, selectionArgs);
            case WINE_ID:
                // For the WINE_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = WineEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateWine(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }

    }//End Update

    /**
     * Update wines in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more wines).
     * Return the number of rows that were successfully updated.
     */
    private int updateWine(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // If the {@link WineEntry#COLUMN_WINE_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(WineEntry.COLUMN_WINE_NAME)) {
            String name = values.getAsString(WineEntry.COLUMN_WINE_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Wine requires a name");
            }
        }

        // If the {@link WineEntry#COLUMN_WINE_WINERY} key is present,
        // check that the name value is not null.
        if (values.containsKey(WineEntry.COLUMN_WINE_WINERY)) {
            String winery = values.getAsString(WineEntry.COLUMN_WINE_WINERY);
            if (winery == null) {
                throw new IllegalArgumentException("Wine requires a winery");
            }
        }

        // If the {@link PetEntry#COLUMN_WINE_YEAR} key is present,
        // check that the year value is valid.
        if (values.containsKey(WineEntry.COLUMN_WINE_YEAR)) {
            // Check that the year is greater than or equal to 1800
            Integer year = values.getAsInteger(WineEntry.COLUMN_WINE_YEAR);
            if (year != null && year < 1800) {
                throw new IllegalArgumentException("Pet requires valid year");
            }
        }

        // If the {@link PetEntry#COLUMN_WINE_QUANTITY} key is present,
        // check that the quantity value is valid.
        if (values.containsKey(WineEntry.COLUMN_WINE_QUANTITY)) {
            // Check that the quantity is greater than or equal to 0 bottles
            Integer quantity = values.getAsInteger(WineEntry.COLUMN_WINE_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Pet requires valid quantity");
            }
        }

        // If the {@link PetEntry#COLUMN_WINE_PRICE} key is present,
        // check that the price value is valid.
        if (values.containsKey(WineEntry.COLUMN_WINE_PRICE)) {
            // Check that the price is greater than or equal to 0
            Float price = values.getAsFloat(WineEntry.COLUMN_WINE_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Pet requires valid price");
            }
        }

        // If the {@link WineEntry#COLUMN_WINE_EMAIL} key is present,
        // check that the email value is not null.
        if (values.containsKey(WineEntry.COLUMN_WINE_EMAIL)) {
            String email = values.getAsString(WineEntry.COLUMN_WINE_EMAIL);
            if (email == null) {
                throw new IllegalArgumentException("Winery requires a email");
            }
        }

        // If the {@link WineEntry#COLUMN_WINE_PHONE} key is present,
        // check that the phone value is not null.
        if (values.containsKey(WineEntry.COLUMN_WINE_PHONE)) {
            String phone = values.getAsString(WineEntry.COLUMN_WINE_PHONE);
            if (phone == null) {
                throw new IllegalArgumentException("Winery requires a phone");
            }
        }

        // If the {@link WineEntry#COLUMN_WINE_IMAGE} key is present,
        // check that the image value is not null.
        if (values.containsKey(WineEntry.COLUMN_WINE_IMAGE)) {
            String image = values.getAsString(WineEntry.COLUMN_WINE_IMAGE);
            if (image == null) {
                throw new IllegalArgumentException("Wine requires an image");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(WineEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;

    }//End Of UpdateWine method

    /**
     * Delete the data at the given selection and selection arguments.
     */

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        switch (match) {
            case WINES:
                // Delete all rows that match the selection and selection args
                //for case WINES
                rowsDeleted = database.delete(WineEntry.TABLE_NAME, selection, selectionArgs);

                // If 1 or more rows were deleted, then notify all listeners that the data at the
                // given URI has changed
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                // Return the number of rows deleted
                return rowsDeleted;

            case WINE_ID:
                // Delete a single row given by the ID in the URI
                selection = WineEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(WineEntry.TABLE_NAME, selection, selectionArgs);
                // If 1 or more rows were deleted, then notify all listeners that the data at the
                // given URI has changed
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                // Return the number of rows deleted
                return rowsDeleted;

            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

    }//End delete method

    /**
     * Returns the MIME type of data for the content URI.
     */

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case WINES:
                return WineEntry.CONTENT_LIST_TYPE;
            case WINE_ID:
                return WineEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }

    }

}//End WineProvider class
