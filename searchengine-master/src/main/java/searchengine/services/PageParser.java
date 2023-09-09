package searchengine.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.model.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class PageParser extends RecursiveTask<Set<String>> {
    private final String url;
    private final SiteEntity site;
    private final PageRepository pageRepository;
    private final Set<String> uniqueLinksSet;
    private SiteRepository siteRepository;

    @Override
    protected Set<String> compute()
    {
        if (SiteParser.isStop())
        {
            return new HashSet<>();
        }
        uniqueLinksSet.add(url);
        try {
            List<PageParser> tasklist = new ArrayList<>();
            Connection.Response jsoupResponse = JsoupWorks.getJsoupResponse(url);
            Document document = jsoupResponse.parse();
            Set <String> setOfLinks = new HashSet<>();
            if (Objects.requireNonNull(jsoupResponse.contentType()).startsWith("text"))
            {
                PageEntity page = makePageForDB(jsoupResponse, document);
                pageRepository.save(page);
                setOfLinks = getLinksFromPage(document);
            }
            if (setOfLinks.size() > 0)
            {
                uniqueLinksSet.addAll(setOfLinks);
                for (String link: setOfLinks)
                {
                    Thread.sleep(200);
                    PageParser task = new PageParser(link, site, pageRepository, uniqueLinksSet, siteRepository);
                    task.fork();
                    tasklist.add(task);
                }
            }
            tasklist.forEach(ForkJoinTask::join);
        } catch (InterruptedException | IOException e)
        {
            e.printStackTrace();
            String errorMsg = e.getLocalizedMessage() + " on page: " + url;
            siteRepository.update(site, errorMsg);
        }
        return uniqueLinksSet;
    }

    public boolean isPropperLink(String url)
    {
        return  url.startsWith(site.getUrl()) &&
                !url.contains("?") &&
                !url.contains("#") &&
                !url.endsWith("jpeg") &&
                !url.endsWith("jpg") &&
                !url.endsWith("pdf") &&
                !url.endsWith("mp3");
    }
    public String linkFormat(String link)
    {
        String formatedLink = link;
        if (!link.endsWith("/"))
        {
            formatedLink = link + "/";
        }
        return formatedLink;
    }

    private PageEntity makePageForDB(Connection.Response jsoupResponse, Document document )
    {
        String path = url.substring(site.getUrl().length() - 1);
        try
        {
            int code = jsoupResponse.statusCode();
            String content = document.html();
            return new PageEntity(site, path, code, content);
        } catch (Exception ex) {
            log.error(ex.getLocalizedMessage());
            return new PageEntity(site, path);
        }
    }

    private Set<String> getLinksFromPage(Document document) throws IOException
    {
        Elements elements = document.select("a");
        return elements.stream().map(e -> e.absUrl("href"))
                .map(String::toLowerCase)
                .map(this::linkFormat)
                .filter(this::isPropperLink)
                .filter(e -> !uniqueLinksSet.contains(e))
                .collect(Collectors.toSet());
    }
}
