package searchengine.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public interface LemmaRepository extends JpaRepository<LemmaEntity, Integer> {

    List<LemmaEntity> findBySiteEntity(SiteEntity siteEntity);

    int countBySiteEntityName(String siteEntityName);

    @Transactional
    default void decreaseFrequencyLemmaAndGetZeroFr(List<LemmaEntity> lemmasForDecrease)
    {
        lemmasForDecrease.forEach(l -> l.setFrequency(l.getFrequency() - 1));
        saveAll(lemmasForDecrease);
        deleteZeroFreqLemmas();
    }

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM lemmas l WHERE l.frequency = 0", nativeQuery = true)
    void deleteZeroFreqLemmas();

    @Transactional
    default List<LemmaEntity> upsertLemmas(SiteEntity siteEntity, List<LemmaEntity> newLemmas)
    {
        List<LemmaEntity> lemmaEntityListForIndex = new ArrayList<>();
        List<LemmaEntity> lemmasBySiteList = findBySiteEntity(siteEntity);
        Map<Boolean, List<LemmaEntity>> lemmaMapForUpsert = newLemmas.stream()
                .collect(Collectors.partitioningBy(lemmasBySiteList::contains));
        for (Map.Entry <Boolean, List<LemmaEntity>> entry : lemmaMapForUpsert.entrySet())
        {
            if (!entry.getKey())
            {
                lemmaEntityListForIndex.addAll(saveAll(entry.getValue()));
                continue;
            }
            lemmasBySiteList.stream().filter(l -> entry.getValue().contains(l))
                    .forEach(l -> l.setFrequency(l.getFrequency() + 1));
            lemmaEntityListForIndex.addAll(saveAll(lemmasBySiteList));
        }
        return lemmaEntityListForIndex;
    }

}
