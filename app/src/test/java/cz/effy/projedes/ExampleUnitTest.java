package cz.effy.projedes;

import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void file_parse_test() throws Exception {
        InputStream url = ExampleUnitTest.class.getClassLoader().getResourceAsStream("RoadSegments.ashx.svg");
        assertNotNull(url);
    }
}