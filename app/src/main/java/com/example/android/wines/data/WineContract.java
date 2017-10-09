package com.example.android.wines.data;


import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class WineContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.

    private WineContract() {
    }

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.wines";


    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.wines/wines/ is a valid path for
     * looking at pet data. content://com.example.android.wines/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_WINES = "wines";

    /**
     * Inner class that defines constant values for the wines database table.
     * Each entry in the table represents a single wine.
     **/

    public static final class WineEntry implements BaseColumns {

        /**
         * The content URI to access the wine data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_WINES);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of wines.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WINES;


        /**
         * The MIME type of the {@link #CONTENT_URI} for a single wine.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WINES;


        public static final String TABLE_NAME = "wines";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_WINE_NAME = "name";
        public static final String COLUMN_WINE_WINERY = "winery";
        public static final String COLUMN_WINE_YEAR = "year";
        public static final String COLUMN_WINE_QUANTITY = "quantity";
        public static final String COLUMN_WINE_PRICE = "price";
        public static final String COLUMN_WINE_EMAIL = "email";
        public static final String COLUMN_WINE_PHONE = "phone";
        public static final String COLUMN_WINE_IMAGE = "image";

    }//End of WineEntry class

}//End of WineContract
