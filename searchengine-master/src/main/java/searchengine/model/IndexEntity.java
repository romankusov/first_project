package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Objects;

import static jakarta.persistence.ConstraintMode.CONSTRAINT;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "index_s")
public class IndexEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", foreignKey = @ForeignKey(value = CONSTRAINT,
            foreignKeyDefinition = "FOREIGN KEY (page_id) REFERENCES pages (id) ON DELETE CASCADE ON UPDATE CASCADE"), nullable = false)
    private PageEntity pageEntity;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "lemma_id", foreignKey = @ForeignKey(value = CONSTRAINT,
            foreignKeyDefinition = "FOREIGN KEY (lemma_id) REFERENCES lemmas (id) ON DELETE CASCADE ON UPDATE CASCADE"), nullable = false)
    private LemmaEntity lemmaEntity;

    @Column(name = "ranking", nullable = false)
    private int rank;

    public IndexEntity(PageEntity pageEntity, LemmaEntity lemmaEntity, int rank) {
        this.pageEntity = pageEntity;
        this.lemmaEntity = lemmaEntity;
        this.rank = rank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndexEntity)) return false;
        IndexEntity that = (IndexEntity) o;
        return pageEntity.equals(that.pageEntity) && lemmaEntity.equals(that.lemmaEntity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageEntity, lemmaEntity);
    }
}
