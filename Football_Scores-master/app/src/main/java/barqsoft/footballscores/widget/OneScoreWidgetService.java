package barqsoft.footballscores.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.widget.RemoteViews;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * Created by business on 28/10/2015.
 */
public class OneScoreWidgetService  extends IntentService {

//    private static int testCnt;

    private static final String[] SCORE_COLUMNS = {
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.TIME_COL
    };
    private static final int HOME_IDX = 0;
    private static final int HOME_GOALS_IDX = 1;
    private static final int AWAY_IDX = 2;
    private static final int AWAY_GOALS_IDX = 3;
    private static final int TIME_IDX = 4;
    private static final String DESC = " DESC";
    // FIXME: 12/11/2015 remove after tests
    private static int cnt;

    private final static String LOG_TAG = OneScoreWidgetService.class.getSimpleName();

    public OneScoreWidgetService() {
        super(LOG_TAG);
    }

    /**
     *
     * Retrieves the most recent results for default day from DB. Data is sorted in
     * descending order of 'time' and 'home' columns. Process sorted data until score is found
     * with value > -1 and show the data in a Score One widget.
     * If all rows have score value = -1, show the last row data in a Score One widget.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        String currDate = MainActivity.getDefaultPageDayInMillis();

        // get the most recent results for default day with scores
        Cursor cursor = getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(),
                SCORE_COLUMNS, null, new String[]{currDate},
                DatabaseContract.scores_table.TIME_COL + DESC +
                " ," + DatabaseContract.scores_table.HOME_COL + DESC);
//        Log.v(LOG_TAG, "onHandleIntent - count: " + cursor.getCount());

        String homeName = getResources().getString(R.string.retrieving_scores);
        String awayName  = getResources().getString(R.string.retrieving_scores);
        int homeScore = -1;
        int awayScore = -1;
        String timeStr = "";

        String scores;
        if (cursor == null || cursor.getCount() == 0) {
            homeName = awayName = getResources().getString(R.string.empty_scores_list);
        } else {
//            cursor.moveToFirst();
            while (true) {
                cursor.moveToNext();
                if (cursor.isAfterLast()) {
                    break;
                }
                homeName = cursor.getString(HOME_IDX);
                homeScore = cursor.getInt(HOME_GOALS_IDX);
                awayName = cursor.getString(AWAY_IDX);
                awayScore = cursor.getInt(AWAY_GOALS_IDX);
                timeStr = cursor.getString(TIME_IDX);
                if (homeScore > -1) {
                    break;
                }
            };
        }

        if (cursor != null) {
            cursor.close();
        }

        scores = Utilies.getScores(homeScore, awayScore);

        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                OneScoreWidgetProvider.class));

        for (int appWidgetId : appWidgetIds) {

            RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_one_score);

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
//            views.setOnClickPendingIntent(R.id.oneScoreWidget, pendingIntent);
            views.setOnClickPendingIntent(R.id.widget_one_score_list_item, pendingIntent);

            views.setTextViewText(R.id.home_name, homeName + cnt++);
            views.setTextViewText(R.id.time, timeStr);
            views.setTextViewText(R.id.scores, scores);
            views.setTextViewText(R.id.away_name, awayName);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                views.setContentDescription(R.id.home_name, getString(R.string.home_team_cont_desc, homeName));
                views.setContentDescription(R.id.time, getString(R.string.time_cont_desc, timeStr));
                views.setContentDescription(R.id.scores, getString(R.string.scores_cont_desc, scores));
                views.setContentDescription(R.id.away_name, getString(R.string.away_team_cont_desc, awayName));
            }

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
