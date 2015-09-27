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
import android.view.View;

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
    private NavigationDrawerFragment navigationDrawerFragment;
    private BroadcastReceiver messageReciever;
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
        if(IS_TABLET){
            setContentView(R.layout.activity_main_tablet);
        }else {
            setContentView(R.layout.activity_main);
        }

        navigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        title = getTitle();

        // Set up the drawer.
        navigationDrawerFragment.setUp(R.id.navigation_drawer,
                    (DrawerLayout) findViewById(R.id.drawer_layout));
    }


    private static final String LIST_OF_BOOKS = "list_of_books";
    private static final String ADD_BOOK = "add_book";
    private static final String ABOUT = "about";
    @Override
    public void onNavigationDrawerItemSelected(int position) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment nextFragment;
        String tag;

        switch (position){
            default:
            case 0:
                nextFragment = new ListOfBooks();
                tag = LIST_OF_BOOKS;
                break;
            case 1:
                nextFragment = new AddBook();
                tag = ADD_BOOK;
                break;
            case 2:
                nextFragment = new About();
                tag = ABOUT;
                break;

        }

        Log.v(LOG_TAG, "onNavigationDrawerItemSelected - tag/title: " + tag + "/" + title);
        fragmentManager.beginTransaction()
                .replace(R.id.container, nextFragment, tag)
                .addToBackStack((String) title)
//                .addToBackStack(tag)
                .commit();
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
        if (!navigationDrawerFragment.isDrawerOpen()) {
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

        BookDetail fragment = new BookDetail();
        fragment.setArguments(args);

        int id = R.id.container;
        if(findViewById(R.id.right_container) != null){
            id = R.id.right_container;
        }
        getSupportFragmentManager().beginTransaction()
                .replace(id, fragment)
                .addToBackStack("Book Detail")
                .commit();

    }

    public void goBack(View view){
        getSupportFragmentManager().popBackStack();
    }

    private boolean isTablet() {
        return (getApplicationContext().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount()<2){
            finish();
        }
        super.onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
    // FIXME: 27/09/2015 - create new if does not exist
        messageReciever = new MessageReceiver();
        IntentFilter filter = new IntentFilter(MESSAGE_EVENT);
        LocalBroadcastManager.getInstance(getApplication()).registerReceiver(messageReciever, filter);
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplication()).unregisterReceiver(messageReciever);
    }


    @Override
    public void processBookDeleted() {
//    todo in onItemSelected() use tag to add navigationDrawerFragment.isDrawerOpen()HERE find it and call method in ListOfBooks to get a new cursor
        FragmentManager fragmentManager = getSupportFragmentManager();
        ListOfBooks listOfBooks = (ListOfBooks) fragmentManager.findFragmentByTag(LIST_OF_BOOKS);
        Log.v(LOG_TAG, "processBookDeleted - listOfBooks: " + listOfBooks);
        listOfBooks.getBooksFromDB();
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msgKey = intent.getStringExtra(MESSAGE_KEY);
            Log.v(LOG_TAG, "messageReceiver - intent: " + intent);
            if (msgKey != null) {
                if (msgKey.equals(DELETE_EVENT)) {
                    processBookDeleted();
                } else if (msgKey != null) {
                    // FIXME: 27/09/2015 call method in AddBook that will execute two lines below
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    AddBook addBook = (AddBook) fragmentManager.findFragmentByTag(ADD_BOOK);
                    addBook.iprocessIsbnNotFound(msgKey);
//                bookEmptyTv.setText(intent.getStringExtra(MESSAGE_KEY));
//                eanTv.setEnabled(true);
                }
            }
        }
    }
}