package cz.effy.projedes;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;
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

import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        Context appContext2 = InstrumentationRegistry.getContext();

        assertEquals("cz.effy.rodos", appContext.getPackageName());

        Log.d("test", "jedeme");

//        InputStream stream = appContext.getResources().openRawResource(cz.effy.rodos.test.R.raw.road_segments);

//        InputStream stream = appContext2.getAssets().open("RoadSegments.ashx.svg");

        String url = "https://rodos.vsb.cz/Handlers/RoadSegments.ashx?lang=cs&road=D11";

        List<String> delays = downloadUrl(new URL(url));
        Log.d("test", "Zpozdeni " + delays.toString());
    }

    private List<String> parseData(InputStream stream) throws XmlPullParserException, IOException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();

        xpp.setInput(new InputStreamReader(stream));
        int eventType = xpp.getEventType();
        List<String> delays = new ArrayList<>();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            String name = xpp.getName();
            if (eventType == XmlPullParser.START_DOCUMENT) {
//                Log.d("test", "Start document");
            } else if (eventType == XmlPullParser.START_TAG) {
//                Log.d("test", "Start tag " + xpp.getName());
                if ("defs".equals(name)) {
                    skip(xpp);
                }
//            } else if (eventType == XmlPullParser.END_TAG) {
//                Log.d("test", "End tag " + xpp.getName());
            } else if (eventType == XmlPullParser.TEXT) {
                String text = xpp.getText();
                if (!text.trim().isEmpty())
//                    Log.d("test", "Text " + text);
                    if (text.startsWith("zpo")) {
                        delays.add(text);
                    }
            }
            eventType = xpp.next();
        }
//        Log.d("test", "End document");
//        Log.d("test", "Zpozdeni " + delays.toString());
        return delays;
    }


    private List<String> downloadUrl(URL url) throws IOException {
        InputStream stream = null;
        HttpsURLConnection connection = null;
        List<String> delays = null;
        try {
            connection = (HttpsURLConnection) url.openConnection();
            // Timeout for reading InputStream arbitrarily set to 3000ms.
            connection.setReadTimeout(10000);
            // Timeout for connection.connect() arbitrarily set to 3000ms.
            connection.setConnectTimeout(10000);
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
            if (stream != null) {
                // Converts Stream to String with max length of 500.
                delays = parseData(stream);
            }
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
