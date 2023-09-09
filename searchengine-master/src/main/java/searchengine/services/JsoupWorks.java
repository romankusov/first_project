package searchengine.services;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import java.io.IOException;

public class JsoupWorks {

    public static Connection.Response getJsoupResponse(String url) throws IOException
    {
        return Jsoup.connect(url).userAgent("Chrome/81.0.4044.138").
                timeout(5 * 1000).
                ignoreHttpErrors(true).
                ignoreContentType(true).
                execute();
    }

}
