/*
 * Copyright (C) 2013 The Android Open Source Project
 */

package it.jaschke.alexandria.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
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

import com.google.zxing.integration.android.IntentIntegrator;

import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImage;


public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private View mRootView;
    private EditText mEanTv;
    private TextView mBookEmptyTv;
    private ImageView mBookCoverIv;
    private TextView mBookTitleTv;
    private TextView mBookSubTitleTv;
    private TextView mAuthorsTv;
    private TextView mCategoriesTv;
    private View mNextBtn;
    private View mDeleteBtn;


    private final int mLoaderId = 1;
    private final String mEanContent ="eanContent";

    private static final String NINE_SEVEN_EIGHT = "978";

    private final static String LOG_TAG = AddBook.class.getSimpleName();

    public AddBook() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mEanTv != null) {
            outState.putString(mEanContent, mEanTv.getText().toString());
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_add_book, container, false);

        mBookEmptyTv = (TextView) mRootView.findViewById(R.id.bookEmpty);

        mBookCoverIv = (ImageView) mRootView.findViewById(R.id.bookCover);
        mBookTitleTv = (TextView) mRootView.findViewById(R.id.bookTitle);
        mBookSubTitleTv = (TextView) mRootView.findViewById(R.id.bookSubTitle);
        mAuthorsTv = (TextView) mRootView.findViewById(R.id.authors);
        mCategoriesTv = (TextView) mRootView.findViewById(R.id.categories);

        mEanTv = (EditText) mRootView.findViewById(R.id.ean);

        mEanTv.addTextChangedListener(new TextWatcher() {
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
                //catch isbn10 numbers
                if (eanStr.length() == 10 && !eanStr.startsWith(NINE_SEVEN_EIGHT)) {
                    eanStr = NINE_SEVEN_EIGHT + eanStr;
                }
                if (eanStr.length() < 13) {
                    return;
                }
                mEanTv.setEnabled(false);
                hideKeyboard();
                //Once we have an ISBN, start a book intent
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, eanStr);
                bookIntent.setAction(BookService.FETCH_BOOK);
                getActivity().startService(bookIntent);
                AddBook.this.restartLoader();
                mBookEmptyTv.setText("Search started");
                // FIXME: 21/11/2015 - test below - is it still a problem?
//                in portraite enter ISBN and rotate, than click search - you will get NullPointerException on line 119
            }
        });

        mRootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This is the callback method that the system will invoke when your button is
                // clicked. You might do this by launching another app or by including the
                //functionality directly in this app.
                // Hint: Use a Try/Catch block to handle the Intent dispatch gracefully, if you
                // are using an external app.

                // FIXME: 21/11/2015 check if try/catch is required
                Context context = getActivity();

                IntentIntegrator integrator = new IntentIntegrator(getActivity());
                integrator.initiateScan();

            }
        });
        mNextBtn = (View) mRootView.findViewById(R.id.next_button);
        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDeleteBtn.setVisibility(View.INVISIBLE);
                clearFields();
                mEanTv.setText("");
                mEanTv.setEnabled(true);
            }
        });

        mDeleteBtn = (View) mRootView.findViewById(R.id.delete_button);
        mDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String eanStr = mEanTv.getText().toString();
                if (eanStr.length() == 10 && !eanStr.startsWith(NINE_SEVEN_EIGHT)) {
                    eanStr = NINE_SEVEN_EIGHT + eanStr;
                }
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, eanStr);
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                clearFields();
                mEanTv.setEnabled(true);
            }
        });

        if (savedInstanceState != null) {
            mEanTv.setText(savedInstanceState.getString(mEanContent));
            mEanTv.setHint("");
        }

        return mRootView;
    }

    private void restartLoader(){
        getLoaderManager().restartLoader(mLoaderId, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mEanTv.getText().length() == 0) {
            return null;
        }
        String eanStr= mEanTv.getText().toString();
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
     *
     * 978-11 180 878 86 OK
     * 1118087887 problem
     */
    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
//        Log.v(LOG_TAG, "onLoadFinished - data count: " + data.getCount());
        if (!data.moveToFirst()) {
            return;
        }

        mBookEmptyTv.setText("");

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
//        Log.v(LOG_TAG, "onLoadFinished - bookTitle: " + bookTitle);
        mBookTitleTv.setText(bookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
//        Log.v(LOG_TAG, "onLoadFinished - bookSubTitle: " + bookSubTitle);
        mBookSubTitleTv.setText(bookSubTitle);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
//        Log.v(LOG_TAG, "onLoadFinished - authors: " + authors);
        String[] authorsArr = authors.split(",");
        mAuthorsTv.setLines(authorsArr.length);
        mAuthorsTv.setText(authors.replace(",", "\n"));
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if (Patterns.WEB_URL.matcher(imgUrl).matches()) {
            new DownloadImage(mBookCoverIv).execute(imgUrl);
            mBookCoverIv.setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        mCategoriesTv.setText(categories);

        mNextBtn.setVisibility(View.VISIBLE);
        mDeleteBtn.setVisibility(View.VISIBLE);
//        Log.v(LOG_TAG, "onLoadFinished - end");
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    public void setScanedEAN(String ean) {
        mEanTv.setText(ean);
    }

    private void clearFields() {
        mEanTv.setText("");
        mBookTitleTv.setText("");
        mBookSubTitleTv.setText("");
        mAuthorsTv.setText("");
        mCategoriesTv.setText("");
        mRootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
        mNextBtn.setVisibility(View.INVISIBLE);
        mDeleteBtn.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.title_scan);
    }

    public void processIsbnNotFound(String msg) {
        mBookEmptyTv.setText(msg);
        mEanTv.setEnabled(true);
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mEanTv.getWindowToken(), 0);
    }

}
