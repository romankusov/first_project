package searchengine.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Integer> {
    Optional<PageEntity> findByPathAndSiteEntityName(String path, String siteEntityName);

    int countBySiteEntityName(String siteEntityName);
}
