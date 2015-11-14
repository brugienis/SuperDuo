package barqsoft.footballscores.widget;


import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * RemoteViewsService controlling the data being shown in the scrollable weather detail widget
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ScoresCollectionWidgetRemoteViewsService extends RemoteViewsService {

    private static final String[] SCORE_COLUMNS = {
            DatabaseContract.scores_table._ID,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.MATCH_ID
    };
    private static final int ID_IDX = 0;
    private static final int HOME_IDX = 1;
    private static final int HOME_GOALS_IDX = 2;
    private static final int AWAY_IDX = 3;
    private static final int AWAY_GOALS_IDX = 4;
    private static final int TIME_IDX = 5;
    private static final int MATCH_ID_IDX = 6;
    private static final String ASC = " ASC";
    // FIXME: 12/11/2015 remove after tests
    private static int cnt;

    private final static String LOG_TAG = ScoresCollectionWidgetRemoteViewsService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor mCursor = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (mCursor != null) {
                    mCursor.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                String currDate = MainActivity.getDefaultPageDayInMillis();
                mCursor = getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(),
                        SCORE_COLUMNS, null, new String[]
                                {currDate}, DatabaseContract.scores_table.TIME_COL + ASC);
                Log.v(LOG_TAG, "onDataSetChanged - count: " + mCursor.getCount());
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (mCursor != null) {
                    mCursor.close();
                    mCursor = null;
                }
            }

            @Override
            public int getCount() {
                return mCursor == null ? 0 : mCursor.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        mCursor == null || !mCursor.moveToPosition(position)) {
                    return null;
                }

                String homeName = mCursor.getString(HOME_IDX);
                int homeScore = mCursor.getInt(HOME_GOALS_IDX);
                String awayName = mCursor.getString(AWAY_IDX);
                int awayScore = mCursor.getInt(AWAY_GOALS_IDX);
                String timeStr = mCursor.getString(TIME_IDX);

                String scores = Utilies.getScores(homeScore, awayScore);

                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_scores_collection_list_item);

                if (position == 0) {
                    views.setTextViewText(R.id.home_name, homeName + cnt++);
                } else {
                    views.setTextViewText(R.id.home_name, homeName);
                }
                views.setTextViewText(R.id.time, timeStr);
                views.setTextViewText(R.id.scores, scores);
                views.setTextViewText(R.id.away_name, awayName);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    views.setContentDescription(R.id.home_name, getString(R.string.home_team_cont_desc, homeName));
                    views.setContentDescription(R.id.time, getString(R.string.time_cont_desc, timeStr));
                    views.setContentDescription(R.id.scores, getString(R.string.scores_cont_desc, scores));
                    views.setContentDescription(R.id.away_name, getString(R.string.away_team_cont_desc, awayName));
                }

                final Intent fillInIntent = new Intent();

                fillInIntent.putExtra(MainActivity.WIDGET_SELECTED_MATCH_ID, mCursor.getDouble(MATCH_ID_IDX));
                fillInIntent.putExtra(MainActivity.WIDGET_SELECTED_ROW_IDX, position);
//                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                views.setOnClickFillInIntent(R.id.widget_scores_collection_list_item, fillInIntent);
                return views;
            }

//            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
//            private void setRemoteContentDescription(RemoteViews views, String description) {
//                views.setContentDescription(R.id.widget_icon, description);
//            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_scores_collection_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (mCursor.moveToPosition(position))
                    return mCursor.getLong(ID_IDX);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}

