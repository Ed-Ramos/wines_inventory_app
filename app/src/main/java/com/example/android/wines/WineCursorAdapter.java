package com.example.android.wines;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.wines.data.WineContract.WineEntry;

/**
 * {@link WineCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of pet data as its data source. This adapter knows
 * how to create list items for each row of wine data in the {@link Cursor}.
 */

public class WineCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link WineCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */

    public WineCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);

        // Find the columns of wine attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(WineEntry.COLUMN_WINE_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(WineEntry.COLUMN_WINE_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(WineEntry.COLUMN_WINE_PRICE);
        int IdColumnIndex = cursor.getColumnIndex(WineEntry._ID);

        // Read the wine attributes from the Cursor for the current wine
        String wineName = cursor.getString(nameColumnIndex);
        final int wineQuantity = cursor.getInt(quantityColumnIndex);
        Float winePrice = cursor.getFloat(priceColumnIndex);
        final int wineId = cursor.getInt(IdColumnIndex);

        // Update the TextViews with the attributes for the current wine
        nameTextView.setText(wineName);
        quantityTextView.setText("Qty: " + String.valueOf(wineQuantity));
        priceTextView.setText("Price: " + "$" + String.valueOf(winePrice));

        Button saleButton = (Button) view.findViewById(R.id.sale_button);
        saleButton.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View view) {
                if (wineQuantity > 0) {
                    int newWineQuantity = wineQuantity - 1;
                    Uri productUri = ContentUris.withAppendedId(WineEntry.CONTENT_URI, wineId);

                    ContentValues values = new ContentValues();
                    values.put(WineEntry.COLUMN_WINE_QUANTITY, newWineQuantity);
                    context.getContentResolver().update(productUri, values, null, null);
                    Toast.makeText(context, context.getString(R.string.category_wine_sold), Toast.LENGTH_SHORT).show();

                } else {

                    Toast.makeText(context, context.getString(R.string.category_wine_out_of_stock), Toast.LENGTH_SHORT).show();
                }
            }

        });

    }//End of BindView
}//End of WineCursorAdapter
