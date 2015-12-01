package barqsoft.footballscores.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.text.SimpleDateFormat;

import barqsoft.footballscores.fragments.PagerFragment;
import barqsoft.footballscores.R;

public class MainActivity extends ActionBarActivity {

    private static int sSelectedMatchId;
    private PagerFragment mPagerFragment;
    private int mWidgetSelectedMatchId;
    private int mWidgetSelectedRowIdx = -1;
    private static final long TWENTY_FOUR_HOURS_IN_MILLIS = 86400000L;
    private static SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final String PAGER_CURRENT = "Pager_Current";
    private static final String SELECTED_MATCH = "Selected_match";
    private static final String PAGER_FRAGMENT = "Selected_match";
    public static final String WIDGET_SELECTED_MATCH_ID = "widget_selected_match_id";
    public static final String WIDGET_SELECTED_ROW_IDX = "widget_selected_row_idx";
    // FIXME: 6/11/2015 - below change back to 5
    private static final int NUM_PAGES = 13;
    private static final int TODAYS_PAGE = NUM_PAGES / 2;   /* number of pages or tabs */
    private static final int DEFAULT_DAY_ADJUSTMENT = -1;    /* -1 yesterday, 0 today, 1 tomorrow, etc. */

    private static int sCurrentFragment = TODAYS_PAGE + DEFAULT_DAY_ADJUSTMENT;

    private final static String LOG_TAG = MainActivity.class.getSimpleName();

    /**
        If app opened by a click on the ScoresCollection widget, extract clicked match Id and index
        of the touched row. When current page becomes visible, the row will be visible.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        if (intent.hasExtra(WIDGET_SELECTED_MATCH_ID)) {
            mWidgetSelectedMatchId = (int) intent.getDoubleExtra(WIDGET_SELECTED_MATCH_ID, -1);
            mWidgetSelectedRowIdx = intent.getIntExtra(WIDGET_SELECTED_ROW_IDX, -1);
        }
        if (savedInstanceState == null) {
            mPagerFragment = new PagerFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mPagerFragment)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            Intent start_about = new Intent(this, AboutActivity.class);
            startActivity(start_about);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(PAGER_CURRENT, mPagerFragment.getPagerHandler().getCurrentItem());
        outState.putInt(SELECTED_MATCH, sSelectedMatchId);
        getSupportFragmentManager().putFragment(outState, PAGER_FRAGMENT, mPagerFragment);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        sCurrentFragment = savedInstanceState.getInt(PAGER_CURRENT);
        sSelectedMatchId = savedInstanceState.getInt(SELECTED_MATCH);
        mPagerFragment = (PagerFragment) getSupportFragmentManager().getFragment(savedInstanceState,
                PAGER_FRAGMENT);
        super.onRestoreInstanceState(savedInstanceState);
    }


    /**
     * Returns sSelectedMatchId
     */
    public static int getSelectedMatchId() {
        return sSelectedMatchId;
    }


    /**
     * Sets sSelectedMatchId
     */
    public static void setSelectedMatchId(int selectedMatchId) {
        sSelectedMatchId = selectedMatchId;
    }

    /**
     * Returns number of pages (tabs)
     */
    public static int getNumPages() {
        return NUM_PAGES;
    }

    /**
     * Returns sCurrentFragment
     */
    public static int getCurrentFragment() {
        return sCurrentFragment;
    }

    /**
        Returns the selected widget's match Id
     */
    public int  getWidgetSelectedMatchId() {
        return mWidgetSelectedMatchId;
    }

    /**
       Returns the selected widget's row
     */
    public int getWidgetSelectedRowIdx() {
        return mWidgetSelectedRowIdx;
    }

    /**
        After the app scroll to the selected widget's row, this method is called to clear its value
     */
    public void clearWidgetSelectedRowIdx() {
        mWidgetSelectedRowIdx = -1;
    }

    /**
        returns the 'today' page - when the app starts, it shows data for that date
     */
    public static int getTodaysPage() {
        return TODAYS_PAGE;
    }

    /**
        Return default page's date in milliseconds.

        DEFAULT_DAY_ADJUSTMENT = 0   today's date
        DEFAULT_DAY_ADJUSTMENT = -1  yesterday's date
        DEFAULT_DAY_ADJUSTMENT = 1   tomorrow's date
     */
    public static String getDefaultPageDayInMillis() {
        return dayFormat.format(System.currentTimeMillis() + DEFAULT_DAY_ADJUSTMENT * TWENTY_FOUR_HOURS_IN_MILLIS);
    }
}
