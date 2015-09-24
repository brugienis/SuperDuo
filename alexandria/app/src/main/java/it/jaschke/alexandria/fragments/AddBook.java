package it.jaschke.alexandria.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImage;


public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private View rootView;
    private EditText eanTv;
    private TextView bookEmptyTv;
    private ImageView bookCoverIv;
    private TextView bookTitleTv;
    private TextView bookSubTitleTv;
    private TextView authorsTv;
    private TextView categoriesTv;
    private View nextBtn;
    private View deleteBtn;


    private final int LOADER_ID = 1;
    private final String EAN_CONTENT="eanContent";
    private BroadcastReceiver messageReciever;

    private String mScanFormat = "Format:";
    private String mScanContents = "Contents:";

    private static final String NINE_SEVEN_EIGHT = "978";
    public static final String MESSAGE_EVENT = "MESSAGE_EVENT";
    public static final String MESSAGE_KEY = "MESSAGE_EXTRA";

//    private static final String SCAN_FORMAT = "scanFormat";
//    private static final String SCAN_CONTENTS = "scanContents";
//    private static final String TAG = "INTENT_TO_SCAN_ACTIVITY";

    private final static String LOG_TAG = AddBook.class.getSimpleName();

    public AddBook() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (eanTv != null) {
            outState.putString(EAN_CONTENT, eanTv.getText().toString());
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_add_book, container, false);

        bookEmptyTv = (TextView) rootView.findViewById(R.id.bookEmpty);

        bookCoverIv = (ImageView) rootView.findViewById(R.id.bookCover);
        bookTitleTv = (TextView) rootView.findViewById(R.id.bookTitle);
        bookSubTitleTv = (TextView) rootView.findViewById(R.id.bookSubTitle);
        authorsTv = (TextView) rootView.findViewById(R.id.authors);
        categoriesTv = (TextView) rootView.findViewById(R.id.categories);

        eanTv = (EditText) rootView.findViewById(R.id.ean);

        eanTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                String eanStr = s.toString();
//                Log.v(LOG_TAG, "afterTextChanged - eanTv: " + eanStr);
                //catch isbn10 numbers
                if (eanStr.length() == 10 && !eanStr.startsWith(NINE_SEVEN_EIGHT)) {
                    eanStr = NINE_SEVEN_EIGHT + eanStr;
                }
                if (eanStr.length() < 13) {
//                    clearFields();
                    return;
                }
                eanTv.setEnabled(false);
                hideKeyboard();
                //Once we have an ISBN, start a book intent
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, eanStr);
                bookIntent.setAction(BookService.FETCH_BOOK);
                getActivity().startService(bookIntent);
                AddBook.this.restartLoader();
                bookEmptyTv.setText("Search started");
//                in portraite enter ISBN and rotate, than click search - you will get NullPointerException on line 119
            }
        });

        rootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This is the callback method that the system will invoke when your button is
                // clicked. You might do this by launching another app or by including the
                //functionality directly in this app.
                // Hint: Use a Try/Catch block to handle the Intent dispatch gracefully, if you
                // are using an external app.
                //when you're done, remove the toast below.
                Context context = getActivity();
                CharSequence text = "This button should let you scan a book for its barcode!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

            }
        });
        nextBtn = (View) rootView.findViewById(R.id.next_button);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteBtn.setVisibility(View.INVISIBLE);
                clearFields();
                eanTv.setText("");
                eanTv.setEnabled(true);
            }
        });

        deleteBtn = (View) rootView.findViewById(R.id.delete_button);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String eanStr = eanTv.getText().toString();
                if (eanStr.length() == 10 && !eanStr.startsWith(NINE_SEVEN_EIGHT)) {
                    eanStr = NINE_SEVEN_EIGHT + eanStr;
                }
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, eanStr);
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                clearFields();
                eanTv.setEnabled(true);
            }
        });

        if (savedInstanceState != null) {
            eanTv.setText(savedInstanceState.getString(EAN_CONTENT));
            eanTv.setHint("");
        }

        return rootView;
    }

    private void restartLoader(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (eanTv.getText().length() == 0) {
            return null;
        }
        String eanStr= eanTv.getText().toString();
        if(eanStr.length() == 10 && !eanStr.startsWith(NINE_SEVEN_EIGHT)){
            eanStr = NINE_SEVEN_EIGHT + eanStr;
        }
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
                null,
                null,
                null,
                null
        );
    }

    /**
     *
     * test with
     * Popular: How a Geek in Pearls Discovered the Secret to Confidence
     * by Maya Van Wagenen
     * ISBN-13: 978-01 475 125 43
     */
    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        bookEmptyTv.setText("");

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        bookTitleTv.setText(bookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        bookSubTitleTv.setText(bookSubTitle);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        String[] authorsArr = authors.split(",");
        authorsTv.setLines(authorsArr.length);
        authorsTv.setText(authors.replace(",", "\n"));
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if (Patterns.WEB_URL.matcher(imgUrl).matches()) {
            new DownloadImage(bookCoverIv).execute(imgUrl);
            bookCoverIv.setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        categoriesTv.setText(categories);

        nextBtn.setVisibility(View.VISIBLE);
        deleteBtn.setVisibility(View.VISIBLE);
//        Log.v(LOG_TAG, "onLoadFinished - end");
    }

    // FIXME: 21/09/2015 - why it is here - it does nothing
    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    private void clearFields() {
        eanTv.setText("");
        bookTitleTv.setText("");
        bookSubTitleTv.setText("");
        authorsTv.setText("");
        categoriesTv.setText("");
        rootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
        nextBtn.setVisibility(View.INVISIBLE);
        deleteBtn.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.scan);
    }

    private class messageReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra(MESSAGE_KEY) != null) {
                bookEmptyTv.setText(intent.getStringExtra(MESSAGE_KEY));
                eanTv.setEnabled(true);
            }
        }
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(eanTv.getWindowToken(), 0);
    }

    @Override
    public void onResume() {
        super.onResume();

        messageReciever = new messageReciever();
        IntentFilter filter = new IntentFilter(MESSAGE_EVENT);
        LocalBroadcastManager.getInstance(getActivity().getApplication()).registerReceiver(messageReciever, filter);
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getActivity().getApplication()).unregisterReceiver(messageReciever);
    }
}
