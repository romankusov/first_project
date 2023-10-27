package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class IndexingServiceImpl implements IndexingService {

    @Autowired
    private volatile PageRepository pageRepository;

    @Autowired
    private volatile SiteRepository siteRepository;

    @Autowired
    private volatile LemmaRepository lemmaRepository;

    @Autowired
    private volatile IndexRepository indexRepository;


    private final SitesList sitesList;
    private final List <Thread> tasks = new ArrayList<>();

    @Override
    public IndexingResponse startIndexing() throws InterruptedException
    {
        IndexingResponse response = new IndexingResponse();
        if (isIndexing())
        {
            response.setResult(false);
            response.setError("Индексация уже запущена");
        } else
        {
            tasks.clear();
            deleteAllEntities();
            SiteParser.setStop(false);
            for (Site site : sitesList.getSites())
            {
                Thread thread = new Thread(new SiteParser(siteRepository, pageRepository,
                        lemmaRepository, indexRepository, site));
                tasks.add(thread);
            }
            tasks.forEach(Thread::start);
            response.setResult(true);
        }
        return response;
    }

    @Override
    public IndexingResponse stopIndexing()
    {
        IndexingResponse response = new IndexingResponse();
        if (!isIndexing())
        {
            response.setResult(false);
            response.setError("Индексация не запущена");
        } else
        {
            SiteParser.setStop(true);
            tasks.forEach(Thread::interrupt);
//            String errorMsg = "Индексация остановлена пользователем";
//            siteRepository.findAll().stream().filter(s -> !(s.getStatus() == IndexStatus.INDEXED))
//                    .forEach(s -> siteRepository.update(s, IndexStatus.FAILED, errorMsg));

            tasks.clear();
            response.setResult(true);
        }
        return response;
    }

    @Override
    @Transactional
    public IndexingResponse indexPage(String url) throws Exception
    {
        IndexingResponse response = new IndexingResponse();

        List<Site> siteUrlList = sitesList.getSites().stream()
                .filter(s -> url.contains(s.getUrl())).collect(Collectors.toList());

        if (siteUrlList.size() == 0)
        {
            response.setResult(false);
            response.setError("Данная страница находится за пределами сайтов, " +
                    "указанных в конфигурационном файле");
        } else
        {
            Site site = siteUrlList.get(0);
            if (siteRepository.findByName(site.getName()).isEmpty())
            {
                SiteEntity siteEntity = new SiteEntity();
                siteEntity.setName(site.getName());
                siteEntity.setUrl(site.getUrl());
                siteEntity.setStatus(IndexStatus.FAILED);
                siteEntity.setStatusTime(LocalDateTime.now());
                siteRepository.save(siteEntity);
            }
            SiteEntity siteEntity = siteRepository.findByName(site.getName()).get();
            PageEntity page = JsoupWorks.makeOnePageForDB(siteEntity, url);
            if (pageRepository.findByPathAndSiteEntityName(page.getPath(), siteEntity.getName()).isPresent())
            {
                PageEntity pageForDelete = pageRepository.findByPathAndSiteEntityName(page.getPath(),
                        siteEntity.getName()).get();
                lemmaRepository.decreaseFrequencyLemma(pageForDelete);
                indexRepository.deleteByPageEntity(pageForDelete);
                pageRepository.delete(pageForDelete);
            }
            pageRepository.save(page);
            Map<LemmaEntity, Integer> lemmaEntityAndRankMap = new ConcurrentHashMap<>();
            ConcurrentLinkedQueue<IndexEntity> indexEntities = new ConcurrentLinkedQueue<>();
            LemmasAndIndexesEntMaker lemmasAndIndexesEntMaker =
                    new LemmasAndIndexesEntMaker(lemmaEntityAndRankMap, indexEntities);
            lemmasAndIndexesEntMaker.addLeAndIeToTempCollection(page);
            List<LemmaEntity> lemmaEntityListForSave = lemmasAndIndexesEntMaker.getLemmasListForSave();
            lemmaEntityListForSave = lemmaRepository.saveAllAndFlush(lemmaEntityListForSave);
            indexEntities = lemmasAndIndexesEntMaker.getIndexEntityQueueForSave(lemmaEntityListForSave);
            indexRepository.saveAllAndFlush(indexEntities);
            response.setResult(true);
            response.setError("");
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
        indexRepository.deleteAllInBatch();
        lemmaRepository.deleteAllInBatch();
        pageRepository.deleteAllInBatch();
        siteRepository.deleteAllInBatch();
    }
}
