package cz.effy.projedes;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RodosService {

    final static String BASE_URL = "https://rodos.vsb.cz/Handlers/RoadSegments.ashx?aggregate=true&lang=cs&road=";

    public static String downloadState(String road) {
        try {
            URL url = new URL(BASE_URL + road);
            List<String> delays = null;
            Log.d("test", "Preparing download " + url.toString());
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
            return "Error " + e.getMessage();
        }
    }

    static List<String> parseData(InputStream stream) throws XmlPullParserException, IOException {
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

    private static List<String> downloadUrl(URL url) throws IOException {
        InputStream stream = null;
        HttpURLConnection connection = null;
        List<String> delays = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(45000);
            connection.setConnectTimeout(45000);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:68.0) Gecko/20100101 Firefox/68.0");
            connection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
            connection.setUseCaches(false);
            connection.connect();
            int responseCode = connection.getResponseCode();
            Log.d("test", "resp code " + responseCode);
            if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_MOVED_TEMP) {
                throw new IOException("HTTP error code: " + responseCode);
            }
            // Retrieve the response body as an InputStream.
            stream = connection.getInputStream();
            Log.d("test", "getResponseMessage " + connection.getResponseMessage());
            Log.d("test", "Preparing to parse");
            // Converts Stream to String with max length of 500.
            if (stream != null) delays = parseData(stream);
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

    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
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
