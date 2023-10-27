package searchengine.services.statistics;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.LemmaRepository;
import searchengine.model.PageRepository;
import searchengine.model.SiteEntity;
import searchengine.model.SiteRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SitesList sites;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private SiteRepository siteRepository;

    @Override
    public StatisticsResponse getStatistics() {

        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setIndexing(true);
        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = sites.getSites();
        try
        {
            for(int i = 0; i < sitesList.size(); i++)
            {
                Site site = sitesList.get(i);
                DetailedStatisticsItem item = new DetailedStatisticsItem();
                String siteName = site.getName();
                SiteEntity siteEntity = siteRepository.findByName(siteName).get();
                item.setName(siteName);
                item.setUrl(site.getUrl());
                int pages = pageRepository.countBySiteEntityName(siteName);
                int lemmas = lemmaRepository.countBySiteEntityName(siteName);
                item.setPages(pages);
                item.setLemmas(lemmas);
                item.setStatus(siteEntity.getStatus().toString());
                item.setError(siteEntity.getLastError());
                item.setStatusTime(getMillisFromLDT(siteEntity.getStatusTime()));
                total.setPages(total.getPages() + pages);
                total.setLemmas(total.getLemmas() + lemmas);
                detailed.add(item);
            }
            data.setTotal(total);
            data.setDetailed(detailed);
            response.setStatistics(data);
            response.setResult(true);
        } catch (Exception ex)
        {
            response.setResult(false);
            response.setError("Указанная страница не найдена");
        }

        return response;
    }

    private long getMillisFromLDT(LocalDateTime localDateTime)
    {
        ZonedDateTime zdt = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
        return zdt.toInstant().toEpochMilli();
    }
}
