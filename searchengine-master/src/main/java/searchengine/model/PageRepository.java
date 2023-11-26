package searchengine.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Integer> {

    @Transactional
    Optional<PageEntity> findByPathAndSiteEntityUrl(String path, String url);

    int countBySiteEntityName(String siteEntityName);
}
