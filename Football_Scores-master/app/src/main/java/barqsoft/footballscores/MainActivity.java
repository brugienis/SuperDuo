package barqsoft.footballscores;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity {

    public static int selectedMatchId;
//    public static int currentFragment = 2;
    private final String saveTag = "Save Test";
    private PagerFragment mMyMain;
    public static final String WIDGET_SELECTED_ROW_ID = "widget_selected_row_id";
    public static final String WIDGET_SELECTED_ROW_IDX = "widget_selected_row_idx";
    private int widgetSelectedRowId = -1;
    private int widgetSelectedRowIdx = -1;
    // FIXME: 9/11/2015 unclutter below fields. Makr fields private and create getters
    private static final int NUM_PAGES = 7;

    private static final int TODAYS_PAGE = NUM_PAGES / 2;    /* number of pages or tabs */
    public static int currentFragment = TODAYS_PAGE;

    private final static String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        if (intent.hasExtra(WIDGET_SELECTED_ROW_ID)) {
            widgetSelectedRowId = intent.getIntExtra(WIDGET_SELECTED_ROW_ID, -1);
            widgetSelectedRowIdx = intent.getIntExtra(WIDGET_SELECTED_ROW_IDX, -1);
        }
        Log.d(LOG_TAG, "onCreate - widgetSelectedRowIdx/widgetSelectedRowId: " + widgetSelectedRowIdx + "/" + widgetSelectedRowId);
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

    public int getWidgetSelectedRowId() {
        return widgetSelectedRowId;
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
}
