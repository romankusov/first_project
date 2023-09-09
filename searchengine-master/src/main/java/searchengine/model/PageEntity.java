package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "pages")
public class PageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private SiteEntity siteEntity;

    @Column(columnDefinition = "TEXT NOT NULL, Index (path(512))")
    private String path;

    private int code;

    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;

    public PageEntity(SiteEntity siteEntity, String path, int code, String content) {
        this.siteEntity = siteEntity;
        this.path = path;
        this.code = code;
        this.content = content;
    }

    public PageEntity(String path, int code, String content) {
        this.path = path;
        this.code = code;
        this.content = content;
    }

    public PageEntity(SiteEntity siteEntity, String path) {
        this.siteEntity = siteEntity;
        this.path = path;
    }
}
