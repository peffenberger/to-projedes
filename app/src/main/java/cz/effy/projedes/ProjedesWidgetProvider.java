package cz.effy.projedes;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.List;

import cz.effy.projedes.dto.RoadState;

public class ProjedesWidgetProvider extends AppWidgetProvider {

    private static int BLUE = Color.rgb(0, 14, 225);
    private static int YELLOW = Color.rgb(255, 200, 19);
    private static int LIGHT_RED = Color.rgb(200, 150, 150);
    private static int DARK_RED = Color.rgb(239, 0, 0);
    private static int BLACK = Color.rgb(0, 0, 0);

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

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    private static Bitmap drawStatus(List<String> colors) {
        Bitmap bmp = Bitmap.createBitmap(colors.size() * 20, 10, Bitmap.Config.ARGB_4444);
        Canvas c = new Canvas(bmp);

        Paint myPaint = new Paint();
        for (int i = 0; i < colors.size(); i++) {
            String cc = colors.get(i);
            switch (cc) {
                case "00c10e":
                    myPaint.setColor(BLUE);
                    break;
                case "ffc813":
                    myPaint.setColor(YELLOW);
                    break;
                case "ff6613":
                    myPaint.setColor(LIGHT_RED);
                    break;
                case "ef0000":
                    myPaint.setColor(DARK_RED);
                    break;
                default:
                    myPaint.setColor(BLACK);
            }
            c.drawRect(20 * i, 1, 18 + (20 * i), 10, myPaint);
        }
        return bmp;
    }

    private static class RetrieveFeedTask extends AsyncTask<String, Void, RoadState> {

        private RemoteViews remoteViews;

        private int widgetId;

        private AppWidgetManager appWidgetManager;

        RetrieveFeedTask(RemoteViews remoteViews, int widgetId, AppWidgetManager appWidgetManager) {
            this.remoteViews = remoteViews;
            this.widgetId = widgetId;
            this.appWidgetManager = appWidgetManager;
        }

        @Override
        protected RoadState doInBackground(String... urls) {
            remoteViews.setTextViewText(R.id.textView, "downloading...");
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
            return RodosService.downloadState(urls[0]);
        }

        @Override
        protected void onPostExecute(RoadState delays) {
//            Log.d("test", "onPostExecute " + delays);
            remoteViews.setTextViewText(R.id.textView, delays.getTextDelays());
            List<String> colors = delays.getColors();
            if (colors != null && !colors.isEmpty()) {
                remoteViews.setImageViewBitmap(R.id.imageViewTop, drawStatus(colors.subList(0, colors.size() / 2)));
                remoteViews.setImageViewBitmap(R.id.imageViewBottom, drawStatus(colors.subList(colors.size() / 2, colors.size())));
            }
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

}