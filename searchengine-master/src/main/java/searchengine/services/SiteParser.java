package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import searchengine.config.Site;
import searchengine.model.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SiteParser implements Runnable {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final Site site;
    private ForkJoinPool fjp = new ForkJoinPool();
    private static volatile boolean stop = false;

    public SiteParser(SiteRepository siteRepository, PageRepository pageRepository, Site site) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.site = site;
    }

    public static void setStop(boolean stop) {
        SiteParser.stop = stop;
    }

    public static boolean isStop() {
        return stop;
    }

    @Override
    public void run()
    {
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setName(site.getName());
        siteEntity.setUrl(site.getUrl());
        siteEntity.setStatus(IndexStatus.INDEXING);
        siteEntity.setStatusTime(LocalDateTime.now());
        siteRepository.save(siteEntity);
        SiteEntity insertedSE = siteRepository.findByName(siteEntity.getName()).get();
        try
        {
            String parsedLink = insertedSE.getUrl();
            Set<String> uniqueLinksSet = ConcurrentHashMap.newKeySet();
            uniqueLinksSet = fjp.invoke(new PageParser(parsedLink, insertedSE, pageRepository,
                    uniqueLinksSet, siteRepository));
            if (stop){
                String errorMsg = "Индексация остановлена пользователем";
                siteRepository.update(insertedSE, IndexStatus.FAILED, errorMsg);
                return;
            }
            uniqueLinksSet.clear();
            siteRepository.update(insertedSE, IndexStatus.INDEXED);
        } catch (Exception ex)
        {
            siteRepository.update(insertedSE, IndexStatus.FAILED, ex.getLocalizedMessage());
        }
    }

    public void stopPageParsing()
    {
       fjp.shutdown();
       try
       {
           if (fjp.awaitTermination(800, TimeUnit.MILLISECONDS))
           {
               fjp.shutdownNow();
           }
       } catch (InterruptedException e)
       {
           fjp.shutdownNow();
       }
    }
}
