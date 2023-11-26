package searchengine.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface IndexRepository extends JpaRepository<IndexEntity, Integer> {

    @Transactional
    void deleteByPageEntity(PageEntity page);

    @Transactional
    @Query("SELECT i.lemmaEntity FROM IndexEntity i WHERE i.pageEntity = :page")
    List<LemmaEntity> getLemmasByPage(@Param("page") PageEntity pageEntity);
}
