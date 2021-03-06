/*
 * Copyright (C) 2013 The Android Open Source Project
 */

package it.jaschke.alexandria.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import it.jaschke.alexandria.R;
import it.jaschke.alexandria.fragments.About;
import it.jaschke.alexandria.fragments.AddBook;
import it.jaschke.alexandria.fragments.BookDetail;
import it.jaschke.alexandria.fragments.ListOfBooks;
import it.jaschke.alexandria.fragments.NavigationDrawerFragment;


public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        ListOfBooks.Callbacks,
        BookDetail.Callbacks{

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private BookDetail mBookDetailFragment;
    private BroadcastReceiver mMessageReceiver;
    public static final String DELETE_EVENT = "DELETE_EVENT";
    public static final String MESSAGE_EVENT = "MESSAGE_EVENT";
    public static final String MESSAGE_KEY = "MESSAGE_EXTRA";

    private static final String TAG_LIST_OF_BOOKS = "List_of_books";
    private static final String TAG_ADD_BOOK = "add_book";
    private static final String TAG_ABOUT = "about";

    private static final String TITLE_LIST_OF_BOOKS = "list of Books";
    private static final String TITLE_ADD_BOOK = "Scan/Add a Book";
    private static final String TITLE_ABOUT = "About this App";

    /**
     * Used to store the last screen mTitle. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    public static boolean sIsTablet = false;

    private final static String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sIsTablet = isTablet();
        if (sIsTablet){
            setContentView(R.layout.activity_main_tablet);
        } else {
            setContentView(R.layout.activity_main);
        }

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment nextFragment;
        String tag;
        String backStackName;

        switch (position){
            default:
            case 0:
                nextFragment = new ListOfBooks();
                tag = TAG_LIST_OF_BOOKS;
                backStackName = TITLE_LIST_OF_BOOKS;
                break;
            case 1:
                nextFragment = new AddBook();
                tag = TAG_ADD_BOOK;
                backStackName = TITLE_ADD_BOOK;
                removeBookDetailFragment();
                break;
            case 2:
                nextFragment = new About();
                tag = TAG_ABOUT;
                backStackName = TITLE_ABOUT;
                break;

        }

        String topBackStackEntryName = getCurrTopBackStackEntryName();
        if (topBackStackEntryName != null && topBackStackEntryName.equals(backStackName)) {
            return;
        }

        int cnt = getSupportFragmentManager().getBackStackEntryCount();
        if (cnt > 0) {
            getSupportFragmentManager().popBackStack();
        }
        fragmentManager.beginTransaction()
                .replace(R.id.container, nextFragment, tag)
                .addToBackStack(backStackName)
                .commit();
    }

    private String getCurrTopBackStackEntryName() {
        int cnt = getSupportFragmentManager().getBackStackEntryCount();
        return cnt == 0 ? null : getSupportFragmentManager().getBackStackEntryAt(cnt - 1).getName();
    }

    public void setTitle(int titleId) {
        mTitle = getString(titleId);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(String ean) {
        Bundle args = new Bundle();
        args.putString(BookDetail.EAN_KEY, ean);

        mBookDetailFragment = new BookDetail();
        mBookDetailFragment.setArguments(args);

        int id = R.id.container;
        if (findViewById(R.id.right_container) == null) {
            setTitle(R.string.title_book_details);
        } else {
            id = R.id.right_container;
        }

        String topBackStackEntryName = getCurrTopBackStackEntryName();
        if (topBackStackEntryName != null && topBackStackEntryName.equals(getResources().getString(R.string.title_book_details))) {
            getSupportFragmentManager().beginTransaction()
                    .replace(id, mBookDetailFragment)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(id, mBookDetailFragment)
                    .addToBackStack(getResources().getString(R.string.title_book_details))
                    .commit();
        }
    }

    private boolean isTablet() {
        return (getApplicationContext().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
    public void onBackPressed() {
        int cnt = getSupportFragmentManager().getBackStackEntryCount();
        String topBackStackEntryName = getCurrTopBackStackEntryName();
        if (!sIsTablet && topBackStackEntryName.equals(getResources().getString(R.string.title_book_details))) {
            setTitle(R.string.title_list_of_books);
        }
        if (getSupportFragmentManager().getBackStackEntryCount() < 2){
            finish();
        }

        getSupportFragmentManager().popBackStack();
        removeBookDetailFragment();
    }

    private void removeBookDetailFragment() {
        if (mBookDetailFragment == null || !sIsTablet) {
            return;
        }
        int cnt = getSupportFragmentManager().getBackStackEntryCount();
        if (getSupportFragmentManager().getBackStackEntryAt(cnt - 1).getName().equals(getResources().getString(R.string.title_book_details))) {
            getSupportFragmentManager().beginTransaction()
                    .detach(mBookDetailFragment)
                    .commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter(MESSAGE_EVENT);
        LocalBroadcastManager.getInstance(getApplication()).registerReceiver(mMessageReceiver, filter);
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplication()).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            try {
                AddBook addBook = (AddBook) getSupportFragmentManager().findFragmentByTag(TAG_ADD_BOOK);
                addBook.setScanedEAN(scanResult.getContents());
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.scanner_results_processing_problem),
                        Toast.LENGTH_LONG).show();
            }
        }

    }


    @Override
    public void processBookDeleted() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        ListOfBooks listOfBooks = (ListOfBooks) fragmentManager.findFragmentByTag(TAG_LIST_OF_BOOKS);
        if (listOfBooks != null) {
            listOfBooks.getBooksFromDB();
            getSupportActionBar().setTitle(getResources().getString(R.string.title_list_of_books));
        }
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msgKey = intent.getStringExtra(MESSAGE_KEY);
            if (msgKey != null) {
                if (msgKey.equals(DELETE_EVENT)) {
                    processBookDeleted();
                } else if (msgKey != null) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    AddBook addBook = (AddBook) fragmentManager.findFragmentByTag(TAG_ADD_BOOK);
                    addBook.processIsbnNotFound(msgKey);
                }
            }
        }
    }
}