import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        String url = "https://www.lab21.gr/";
        String child = "https://www.lab21.gr/portfolio-work/";
        String path = child.substring(url.length() -1);
        System.out.println(path);

        Connection.Response jsoupResponse = Jsoup.connect(child).userAgent("Chrome/81.0.4044.138").
                timeout(5 * 1000).
                ignoreHttpErrors(true).
                ignoreContentType(true).
                execute();

        String type = jsoupResponse.contentType();
        System.out.println(type);
        Set<String> setOfLinks = new HashSet<>();
        System.out.println(setOfLinks.size());

    }
}
