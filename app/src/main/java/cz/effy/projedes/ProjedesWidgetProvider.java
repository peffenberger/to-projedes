package cz.effy.projedes;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class ProjedesWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int count = appWidgetIds.length;

        for (int i = 0; i < count; i++) {
            int widgetId = appWidgetIds[i];

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.simple_widget);
            Intent intent = new Intent(context, ProjedesWidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.actionButton, pendingIntent);

            String url = "https://rodos.vsb.cz/Handlers/RoadSegments.ashx?lang=cs&road=D11";

            new RetrieveFeedTask(remoteViews, widgetId, appWidgetManager).execute(url);
        }
    }

    private class RetrieveFeedTask extends AsyncTask<String, Void, String> {

        private Exception exception;

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
            try {
                URL url = new URL(urls[0]);
                List<String> delays = null;
                Log.d("test", "Preparing download");
                remoteViews.setTextViewText(R.id.textView, "downloading...");
                appWidgetManager.updateAppWidget(widgetId, remoteViews);
                delays = downloadUrl(url);
                Log.d("test", "Download completed " + (delays != null ? delays.toString() : "null"));
                StringBuilder sb = new StringBuilder();
                if (delays != null && delays.size() == 2) {
                    sb.append(delays.get(0));
                    sb.append("\n");
                    sb.append(delays.get(1));
                }
                return sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
                this.exception = e;
                return "Error " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String delays) {
            Log.d("test", "onPostExecute " + delays);
            remoteViews.setTextViewText(R.id.textView, delays);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    private List<String> parseData(InputStream stream) throws XmlPullParserException, IOException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();

        xpp.setInput(new InputStreamReader(stream));
        int eventType = xpp.getEventType();
        List<String> delays = new ArrayList<>();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String name = xpp.getName();
                if ("defs".equals(name)) {
                    skip(xpp);
                }
            } else if (eventType == XmlPullParser.TEXT) {
                String text = xpp.getText();
                if (!text.trim().isEmpty())
                    if (text.startsWith("zpo")) {
                        delays.add(text);
                    }
            }
            eventType = xpp.next();
        }
        return delays;
    }


    private List<String> downloadUrl(URL url) throws IOException {
        InputStream stream = null;
        HttpsURLConnection connection = null;
        List<String> delays = null;
        try {
            connection = (HttpsURLConnection) url.openConnection();
            connection.setReadTimeout(15000);
            connection.setConnectTimeout(15000);
            // For this use case, set HTTP method to GET.
            connection.setRequestMethod("GET");
            // Already true by default but setting just in case; needs to be true since this request
            // is carrying an input (response) body.
            connection.setDoInput(true);
            // Open communications link (network traffic occurs here).
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpsURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }
            // Retrieve the response body as an InputStream.
            stream = connection.getInputStream();
            Log.d("test", "Preparing to parse");
            if (stream != null) {
                // Converts Stream to String with max length of 500.
                delays = parseData(stream);
            }
            Log.d("test", "Parsing completed");
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } finally {
            // Close Stream and disconnect HTTPS connection.
            if (stream != null) {
                stream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return delays;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}