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
    private static final String EMPTY_STRING = "";

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

                IntentIntegrator integrator = new IntentIntegrator(getActivity());
                integrator.initiateScan();

            }
        });
        mNextBtn = mRootView.findViewById(R.id.next_button);
        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDeleteBtn.setVisibility(View.INVISIBLE);
                clearFields();
                mEanTv.setText(EMPTY_STRING);
                mEanTv.setEnabled(true);
            }
        });

        mDeleteBtn = mRootView.findViewById(R.id.delete_button);
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
     * Show details of a book.
     */
    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        mBookEmptyTv.setText(EMPTY_STRING);

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        mBookTitleTv.setText(bookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        mBookSubTitleTv.setText(bookSubTitle);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        if (authors == null) {
            mAuthorsTv.setText(EMPTY_STRING);
        } else {
            String[] authorsArr = authors.split(getResources().getString(R.string.authors_column_separator));
            mAuthorsTv.setLines(authorsArr.length);
            mAuthorsTv.setText(authors.replace(
                    getResources().getString(R.string.authors_column_separator),
                    getResources().getString(R.string.new_line)));
        }

        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if (Patterns.WEB_URL.matcher(imgUrl).matches()) {
            new DownloadImage(mBookCoverIv, getActivity()).execute(imgUrl);
            mBookCoverIv.setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        mCategoriesTv.setText(categories);

        mNextBtn.setVisibility(View.VISIBLE);
        mDeleteBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    public void setScanedEAN(String ean) {
        mEanTv.setText(ean);
    }

    private void clearFields() {
        mEanTv.setText(EMPTY_STRING);
        mBookTitleTv.setText(EMPTY_STRING);
        mBookSubTitleTv.setText(EMPTY_STRING);
        mAuthorsTv.setText(EMPTY_STRING);
        mCategoriesTv.setText(EMPTY_STRING);
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
