package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.*;
import java.util.*;
import java.util.concurrent.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class IndexingServiceImpl implements IndexingService {

    @Autowired
    private volatile PageRepository pageRepository;

    @Autowired
    private volatile SiteRepository siteRepository;

    private final SitesList sitesList;
    private final List <SiteParser> tasks = new ArrayList<>();
    private final IndexingResponse response = new IndexingResponse();
    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);

    @Override
    public IndexingResponse startIndexing() throws InterruptedException
    {
        if (isIndexing())
        {
            response.setResult(false);
            response.setErrorMessage("Индексация уже запущена");
        } else
        {
            deleteAllEntities();

            for (Site site : sitesList.getSites())
            {
                SiteParser siteParser = new SiteParser(siteRepository, pageRepository, site);
                tasks.add(siteParser);
            }
            tasks.forEach(executor::submit);
            response.setResult(true);
            response.setErrorMessage("");
        }
        return response;
    }

    @Override
    public IndexingResponse stopIndexing()
    {
        if (executor.getActiveCount() == 0)
        {
            response.setResult(false);
            response.setErrorMessage("Индексация не запущена");
        } else
        {
            SiteParser.setStop(true);
            tasks.forEach(SiteParser::stopPageParsing);
            tasks.clear();
            executor.shutdown();
            try {
                if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS))
                {
                    executor.shutdownNow();
                }
            } catch (InterruptedException ex)
            {
                executor.shutdownNow();
            }
            response.setResult(true);
        }
        return response;
    }

    private boolean isIndexing()
    {
        Iterable<SiteEntity> siteEntities = siteRepository.findAll();
        ArrayList<SiteEntity> siteEntityArrayList = new ArrayList<>();
        siteEntities.forEach(siteEntityArrayList::add);
        return siteEntityArrayList.stream()
                .anyMatch(e -> e.getStatus() == IndexStatus.INDEXING);
    }

    private void deleteAllEntities()
    {
        pageRepository.deleteAll();
        siteRepository.deleteAll();
    }

}
