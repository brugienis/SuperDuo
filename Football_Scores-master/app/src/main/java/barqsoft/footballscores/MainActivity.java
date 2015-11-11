package barqsoft.footballscores;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.text.SimpleDateFormat;

public class MainActivity extends ActionBarActivity {

    public static int selectedMatchId;
//    public static int currentFragment = 2;
    private final String saveTag = "Save Test";
    private PagerFragment mMyMain;
    public static final String WIDGET_SELECTED_MATCH_ID = "widget_selected_match_id";
    public static final String WIDGET_SELECTED_ROW_IDX = "widget_selected_row_idx";
    private int widgetSelectedMatchId;
    private int widgetSelectedRowIdx = -1;
    private static final long TWENTY_FOUR_HOURS_IN_MILLIS = 86400000L;
    private static SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
    // FIXME: 6/11/2015 - below change back to 5
    private static final int NUM_PAGES = 7;
    private static final int TODAYS_PAGE = NUM_PAGES / 2;    /* number of pages or tabs */

    private static final int DEFAULT_DAY_ADJUSTMENT = -2;
//    public static int currentFragment = TODAYS_PAGE;
    public static int currentFragment = TODAYS_PAGE + DEFAULT_DAY_ADJUSTMENT;

    private final static String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        if (intent.hasExtra(WIDGET_SELECTED_MATCH_ID)) {
            widgetSelectedMatchId = (int) intent.getDoubleExtra(WIDGET_SELECTED_MATCH_ID, -1);
            widgetSelectedRowIdx = intent.getIntExtra(WIDGET_SELECTED_ROW_IDX, -1);
        }
        Log.d(LOG_TAG, "onCreate - widgetSelectedRowIdx/widgetSelectedMatchId: " + widgetSelectedRowIdx + "/" + widgetSelectedMatchId);
        if (savedInstanceState == null) {
            mMyMain = new PagerFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mMyMain)
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
        Log.v(saveTag, "will save");
        Log.v(saveTag, "fragment: " + String.valueOf(mMyMain.mPagerHandler.getCurrentItem()));
        Log.v(saveTag, "selected id: " + selectedMatchId);
        outState.putInt("Pager_Current", mMyMain.mPagerHandler.getCurrentItem());
        outState.putInt("Selected_match", selectedMatchId);
        getSupportFragmentManager().putFragment(outState, "mMyMain", mMyMain);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.v(saveTag, "will retrive");
        Log.v(saveTag, "fragment: " + String.valueOf(savedInstanceState.getInt("Pager_Current")));
        Log.v(saveTag, "selected id: " + savedInstanceState.getInt("Selected_match"));
        currentFragment = savedInstanceState.getInt("Pager_Current");
        selectedMatchId = savedInstanceState.getInt("Selected_match");
        mMyMain = (PagerFragment) getSupportFragmentManager().getFragment(savedInstanceState, "mMyMain");
        super.onRestoreInstanceState(savedInstanceState);
    }

    public static int getNumPages() {
        return NUM_PAGES;
    }

    public int  getWidgetSelectedMatchId() {
        return widgetSelectedMatchId;
    }

    public int getWidgetSelectedRowIdx() {
        return widgetSelectedRowIdx;
    }

    public void clearWidgetSelectedRowIdx() {
        widgetSelectedRowIdx = -1;
    }

    public static int getTodaysPage() {
        return TODAYS_PAGE;
    }

    public static int getDefaultDayAdjustment() {
        return DEFAULT_DAY_ADJUSTMENT;
    }

    /*
        Return default page's date in milliseconds.

        DEFAULT_DAY_ADJUSTMENT = 0  today's date
        DEFAULT_DAY_ADJUSTMENT = -1  yesterday's date
        DEFAULT_DAY_ADJUSTMENT = 0  tomorrow's date
     */
    public static String getDefaultPageDayInMillis() {
        return dayFormat.format(System.currentTimeMillis() + DEFAULT_DAY_ADJUSTMENT * TWENTY_FOUR_HOURS_IN_MILLIS);
    }
}
