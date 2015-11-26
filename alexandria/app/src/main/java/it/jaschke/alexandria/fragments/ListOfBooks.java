/*
 * Copyright (C) 2013 The Android Open Source Project
 */

package it.jaschke.alexandria.fragments;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import it.jaschke.alexandria.R;
import it.jaschke.alexandria.api.BookListAdapter;
import it.jaschke.alexandria.data.AlexandriaContract;


public class ListOfBooks extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private BookListAdapter mBookListAdapter;
    private ListView mBookList;
    private TextInputLayout mSearchTextInputLayout;
    private View mSearchButtonVw;
    private int mPosition = ListView.INVALID_POSITION;
    private EditText mSearchText;
    private Callbacks mCallbacks;

    private final int mLoaderId = 10;

    private final static String LOG_TAG = ListOfBooks.class.getSimpleName();

    public interface Callbacks {
        void onItemSelected(String ean);
    }

    public ListOfBooks() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Cursor cursor = getActivity().getContentResolver().query(
                AlexandriaContract.BookEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        mBookListAdapter = new BookListAdapter(getActivity(), cursor, 0);

        View rootView = inflater.inflate(R.layout.fragment_list_of_books, container, false);

        mSearchText = (EditText) rootView.findViewById(R.id.searchText);
        mSearchButtonVw = rootView.findViewById(R.id.searchButton);

        mSearchTextInputLayout = (TextInputLayout) rootView.findViewById(R.id.name_et_textinputlayout);
        mSearchText.requestFocus();

        if (cursor.getCount() == 0) {
            mSearchTextInputLayout.setHint(getResources().getString(R.string.no_books_available));
            mSearchText.setEnabled(false);
            mSearchButtonVw.setEnabled(false);
        } else {
            mSearchTextInputLayout.setHint(getActivity().getResources().getString(R.string.title_input_hint));
        }

        mSearchButtonVw.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ListOfBooks.this.restartLoader();
                    }
                }
        );

        mBookList = (ListView) rootView.findViewById(R.id.listOfBooks);
        mBookList.setAdapter(mBookListAdapter);

        mBookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = mBookListAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    bookDetailsSelected(cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry._ID)));
                }
            }
        });

        return rootView;
    }

//    private ActionBar getActionBar() {
//        return ((ActionBarActivity) getActivity()).getSupportActionBar();
//    }

    private void bookDetailsSelected(String ean) {
        mCallbacks.onItemSelected(ean);
        hideKeyboard();
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
    }

    public void getBooksFromDB() {
        Cursor cursor = getActivity().getContentResolver().query(
                AlexandriaContract.BookEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        if (mSearchText != null) {
            mSearchText.requestFocus();
        }
        if (cursor.getCount() == 0) {
            mSearchTextInputLayout.setHint("");
            mSearchTextInputLayout.setError(getResources().getString(R.string.no_books_available));
            mSearchText.setEnabled(false);
            mSearchButtonVw.setEnabled(false);
        } else {
            mSearchTextInputLayout.setHint(getActivity().getResources().getString(R.string.title_input_hint));
        }

        mBookListAdapter = new BookListAdapter(getActivity(), cursor, 0);
        mBookList.setAdapter(mBookListAdapter);
    }


    private void restartLoader(){
        getLoaderManager().restartLoader(mLoaderId, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        final String selection = getResources().getString(R.string.book_search_columns, AlexandriaContract.BookEntry.TITLE, AlexandriaContract.BookEntry.SUBTITLE);
        String searchString = mSearchText.getText().toString();

        if (searchString.length()>0) {
            return new CursorLoader(
                    getActivity(),
                    AlexandriaContract.BookEntry.CONTENT_URI,
                    null,
                    selection,
                    new String[]{getResources().getString(R.string.book_search_string, searchString)},
                    null
            );
        }

        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mBookListAdapter.swapCursor(data);
        if (mBookListAdapter.isEmpty()) {
            mSearchTextInputLayout.setHint("No books available");
        } else {
            mSearchTextInputLayout.setHint(getResources().getString(R.string.title_input_hint));
        }
        if (mPosition != ListView.INVALID_POSITION) {
            mBookList.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mBookListAdapter.swapCursor(null);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (Callbacks) activity;
        activity.setTitle(R.string.title_list_of_books);
    }


}
