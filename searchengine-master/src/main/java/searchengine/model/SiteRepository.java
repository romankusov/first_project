package searchengine.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SiteRepository extends CrudRepository <SiteEntity, Integer> {

    Optional<SiteEntity> findByName(String name);

    @Transactional
    default void update(SiteEntity siteEntity, IndexStatus status, String error)
    {
        String name = siteEntity.getName();
        SiteEntity sEforUpdate = findByName(name).get();
        sEforUpdate.setStatus(status);
        sEforUpdate.setLastError(error);
        sEforUpdate.setStatusTime(LocalDateTime.now());
        save(sEforUpdate);
    }

    @Transactional
    default void update(SiteEntity siteEntity, String error)
    {
        String name = siteEntity.getName();
        SiteEntity sEforUpdate = findByName(name).get();
        sEforUpdate.setLastError(error);
        sEforUpdate.setStatusTime(LocalDateTime.now());
        save(sEforUpdate);
    }

    @Transactional
    default void update(SiteEntity siteEntity, IndexStatus status)
    {
        String name = siteEntity.getName();
        SiteEntity sEforUpdate = findByName(name).get();
        sEforUpdate.setStatus(status);
        sEforUpdate.setStatusTime(LocalDateTime.now());
        save(sEforUpdate);
    }




}
