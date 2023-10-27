package searchengine.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface LemmaRepository extends JpaRepository<LemmaEntity, Integer> {

    Iterable<LemmaEntity> findBySiteEntity(SiteEntity siteEntity);

    int countBySiteEntityName(String siteEntityName);

    @Transactional
    default void decreaseFrequencyLemma(PageEntity page)
    {
        SiteEntity siteEntity = page.getSiteEntity();
        Iterable<LemmaEntity> lemmaEntities = findBySiteEntity(siteEntity);
        lemmaEntities.forEach(l -> l.setFrequency(l.getFrequency() - 1));
        saveAll(lemmaEntities);
    }

}
