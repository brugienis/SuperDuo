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
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import it.jaschke.alexandria.R;
import it.jaschke.alexandria.api.Callback;
import it.jaschke.alexandria.fragments.About;
import it.jaschke.alexandria.fragments.AddBook;
import it.jaschke.alexandria.fragments.BookDetail;
import it.jaschke.alexandria.fragments.ListOfBooks;
import it.jaschke.alexandria.fragments.NavigationDrawerFragment;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        Callback,
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

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence title;
    public static boolean IS_TABLET = false;

    private final static String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IS_TABLET = isTablet();
        if (IS_TABLET){
            setContentView(R.layout.activity_main_tablet);
        }else {
            setContentView(R.layout.activity_main);
        }

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        title = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }


    private static final String TAG_LIST_OF_BOOKS = "List_of_books";
    private static final String TAG_ADD_BOOK = "add_book";
    private static final String TAG_ABOUT = "about";

    private static final String TITLE_LIST_OF_BOOKS = "list of Books";
    private static final String TITLE_ADD_BOOK = "Scan/Add a Book";
    private static final String TITLE_ABOUT = "About this App";

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
//            Log.v(LOG_TAG, "onNavigationDrawerItemSelected - this fragment is already the current tag/backStackName: " + getSupportFragmentManager().getBackStackEntryCount() + "/" + tag + "/" + backStackName);
            return;
        }

//        Log.v(LOG_TAG, "onNavigationDrawerItemSelected - backStackEntryCount/tag/backStackName: " + getSupportFragmentManager().getBackStackEntryCount() + "/" + tag + "/" + backStackName);
        int cnt = getSupportFragmentManager().getBackStackEntryCount();
        if (cnt > 0) {
            getSupportFragmentManager().popBackStack();
        }
        fragmentManager.beginTransaction()
                .replace(R.id.container, nextFragment, tag)
                .addToBackStack(backStackName)
                .commit();

//        for (int i = 0, intCnt = getSupportFragmentManager().getBackStackEntryCount(); i < intCnt; i++) {
//            Log.v(LOG_TAG, "onNavigationDrawerItemSelected - name: " + i + ": " + getSupportFragmentManager().getBackStackEntryAt(i).getName());
//
//        }
    }

    private String getCurrTopBackStackEntryName() {
        int cnt = getSupportFragmentManager().getBackStackEntryCount();
        return cnt == 0 ? null : getSupportFragmentManager().getBackStackEntryAt(cnt - 1).getName();
    }

    public void setTitle(int titleId) {
        title = getString(titleId);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(title);
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
        } else if (id == R.id.action_show_backstack) {
            // FIXME: 1/10/2015 remove after testing
            for (int i = 0, cnt = getSupportFragmentManager().getBackStackEntryCount(); i < cnt; i++) {
                Log.v(LOG_TAG, "backStack - name: " + i + ": " + getSupportFragmentManager().getBackStackEntryAt(i).getName());
            }
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
//        Log.v(LOG_TAG, "onItemSelected - topBackStackEntryName: " + topBackStackEntryName);
        if (topBackStackEntryName != null && topBackStackEntryName.equals(getResources().getString(R.string.title_book_details))) {
//            Log.v(LOG_TAG, "onItemSelected - this fragment is already the current tag/backStackName: " + getSupportFragmentManager().getBackStackEntryCount() + "/" + topBackStackEntryName);
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
//        Log.v(LOG_TAG, "onBackPressed - start - cnt: " + getSupportFragmentManager().getBackStackEntryCount());
        int cnt = getSupportFragmentManager().getBackStackEntryCount();
        String topBackStackEntryName = getCurrTopBackStackEntryName();
        Log.v(LOG_TAG, "onBackPressed - start - name: " + topBackStackEntryName);
        if (!IS_TABLET && topBackStackEntryName.equals(getResources().getString(R.string.title_book_details))) {
            setTitle(R.string.title_list_of_books);
//            Log.v(LOG_TAG, "onBackPressed - title changed");
        }
        if (getSupportFragmentManager().getBackStackEntryCount() < 2){
            finish();
        }

        getSupportFragmentManager().popBackStack();
        removeBookDetailFragment();
    }

    private void removeBookDetailFragment() {
        if (mBookDetailFragment == null || !IS_TABLET) {
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
//            Log.d(LOG_TAG, "scan result: " + scanResult.toString());
            try {
                AddBook addBook = (AddBook) getSupportFragmentManager().findFragmentByTag(TAG_ADD_BOOK);
                addBook.setScanedEAN(scanResult.getContents());
            } catch (Exception e) {
                // FIXME: 20/11/2015 - show error description
                e.printStackTrace();
                // maybe no add book fragment around?
            }
        }

    }


    @Override
    public void processBookDeleted() {
//    todo in onItemSelected() use tag to add mNavigationDrawerFragment.isDrawerOpen()HERE find it and call method in ListOfBooks to get a new cursor
        FragmentManager fragmentManager = getSupportFragmentManager();
        ListOfBooks listOfBooks = (ListOfBooks) fragmentManager.findFragmentByTag(TAG_LIST_OF_BOOKS);
        Log.v(LOG_TAG, "processBookDeleted - listOfBooks: " + listOfBooks);
        if (listOfBooks != null) {
            listOfBooks.getBooksFromDB();
            getSupportActionBar().setTitle(getResources().getString(R.string.title_list_of_books));
        }
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msgKey = intent.getStringExtra(MESSAGE_KEY);
            Log.v(LOG_TAG, "messageReceiver - intent/msgKey: " + intent + "/" + msgKey);
            if (msgKey != null) {
                if (msgKey.equals(DELETE_EVENT)) {
                    processBookDeleted();
                } else if (msgKey != null) {
                    // FIXME: 27/09/2015 call method in AddBook that will execute two lines below
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    AddBook addBook = (AddBook) fragmentManager.findFragmentByTag(TAG_ADD_BOOK);
                    addBook.processIsbnNotFound(msgKey);
                }
            }
        }
    }
}