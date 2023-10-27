package searchengine.services.indexing;

import lombok.NoArgsConstructor;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@NoArgsConstructor
public class LemmasAndIndexesEntMaker {

    private static LemmasFinder lemmasFinder;

    static {
        try {
            lemmasFinder = LemmasFinder.getInstance();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static AtomicInteger lemmasCount = new AtomicInteger(0);

    private static AtomicInteger indexCount = new AtomicInteger(0);

    private Map<LemmaEntity, Integer> lemmaMapForDB;
    private ConcurrentLinkedQueue<IndexEntity> indexEntityQueue;

    public LemmasAndIndexesEntMaker(Map<LemmaEntity, Integer> lemmaMapForDB,
                                    ConcurrentLinkedQueue<IndexEntity> indexEntityQueue) throws Exception {
        this.lemmaMapForDB = lemmaMapForDB;
        this.indexEntityQueue = indexEntityQueue;
    }
    public static AtomicInteger getLemmasCount() {
        return lemmasCount;
    }

    public static AtomicInteger getIndexCount() {
        return indexCount;
    }

    public void addLeAndIeToTempCollection(PageEntity page) throws Exception
    {
        Map<String, Integer> lemmasAndRankFromPage = getLemmasAndRankFromPage(page);
        SiteEntity siteEntity = page.getSiteEntity();

        for (String strLemma : lemmasAndRankFromPage.keySet())
        {
            LemmaEntity lemmaEntity = new LemmaEntity(siteEntity, strLemma);
            putNewLemmas(lemmaEntity);
            lemmasCount.incrementAndGet();//
            Integer rank = lemmasAndRankFromPage.get(strLemma);
            indexEntityQueue.add(new IndexEntity(page, lemmaEntity, rank));
            indexCount.incrementAndGet();
        }
    }

    public List<LemmaEntity> getLemmasListForSave()
    {
        List<LemmaEntity> lemmaEntityList = new ArrayList<>();
        for (LemmaEntity lemmaEntity : lemmaMapForDB.keySet())
        {
            int frequency = lemmaMapForDB.get(lemmaEntity);
            lemmaEntity.setFrequency(frequency);
            lemmaEntityList.add(lemmaEntity);
        }
        return lemmaEntityList;
    }

    public ConcurrentLinkedQueue<IndexEntity> getIndexEntityQueueForSave(List<LemmaEntity> savedLemmaEntityList)
    {
        for (LemmaEntity lemmaEntity : savedLemmaEntityList)
        {
            for (IndexEntity indexEntity : indexEntityQueue)
            {
                boolean sameLemma = lemmaEntity.getLemma().equals(indexEntity.getLemmaEntity().getLemma())
                        && lemmaEntity.getSiteEntity().equals(indexEntity.getLemmaEntity().getSiteEntity());
                if (sameLemma)
                {
                    indexEntity.setLemmaEntity(lemmaEntity);
                }
            }
        }
        return indexEntityQueue;
    }


    private Map<String, Integer> getLemmasAndRankFromPage(PageEntity page) throws Exception
    {
        String text = page.getContent();
        return lemmasFinder.getLemmaMap(text);
    }

    private void putNewLemmas(LemmaEntity lemmaEntity)
    {
        int frequency = lemmaMapForDB.getOrDefault(lemmaEntity, 0) + 1;
        if (frequency > 1)
        {
            lemmasCount.decrementAndGet();
        }
        lemmaMapForDB.put(lemmaEntity, frequency);
    }

}
