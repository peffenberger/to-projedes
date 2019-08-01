package cz.effy.projedes;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

public class ProjedesWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("test", "updating...");
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        boolean connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();

        for (int widgetId : appWidgetIds) {
            Log.d("test", "updating... remoteId " + widgetId);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.simple_widget);

            Intent intent = new Intent(context, ProjedesWidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.actionButton, pendingIntent);

            String titlePrefix = ExampleAppWidgetConfigure.loadTitlePref(context, widgetId);
            Log.d("test", "titlePrefix... " + titlePrefix);
            if (titlePrefix == null) {
                remoteViews.setTextViewText(R.id.roadView, "n/a");
                remoteViews.setTextViewText(R.id.textView, "No road");
                break;
            }

            remoteViews.setTextViewText(R.id.roadView, titlePrefix);

            if (connected) {
                new RetrieveFeedTask(remoteViews, widgetId, appWidgetManager).execute(titlePrefix);
            } else {
                remoteViews.setTextViewText(R.id.textView, "No network");
            }
        }
    }

    private static class RetrieveFeedTask extends AsyncTask<String, Void, String> {

        private RemoteViews remoteViews;

        private int widgetId;

        private AppWidgetManager appWidgetManager;

        RetrieveFeedTask(RemoteViews remoteViews, int widgetId, AppWidgetManager appWidgetManager) {
            this.remoteViews = remoteViews;
            this.widgetId = widgetId;
            this.appWidgetManager = appWidgetManager;
        }

        @Override
        protected String doInBackground(String... urls) {
            remoteViews.setTextViewText(R.id.textView, "downloading...");
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
            return RodosService.downloadState(urls[0]);
        }

        @Override
        protected void onPostExecute(String delays) {
            Log.d("test", "onPostExecute " + delays);
            remoteViews.setTextViewText(R.id.textView, delays);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

}