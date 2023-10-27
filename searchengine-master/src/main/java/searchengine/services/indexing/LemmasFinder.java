package searchengine.services.indexing;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class LemmasFinder {

    private final String REG_EXP_RUSSIAN_WORD = "([а-яА-Я]+(-[а-яА-Я]+)*)";
    @Autowired
    private final LuceneMorphology luceneMorph;

    public static LemmasFinder getInstance() throws IOException {
        LuceneMorphology morphology= new RussianLuceneMorphology();
        return new LemmasFinder(morphology);
    }
    public LemmasFinder(LuceneMorphology luceneMorph) {
        this.luceneMorph = luceneMorph;
    }

    public Map<String, Integer> getLemmaMap(String text) throws Exception {
        List<String> russianPropperWordsList = russianPropperWordsList(text);
        if(russianPropperWordsList.isEmpty())
        {
            throw new Exception("На странице отсутствуют русские слова");
        }
        Map<String, Integer> lemmaMap = new HashMap<>();
        for (String word : russianPropperWordsList)
        {
            if(!luceneMorph.checkString(word))
            {
                continue;
            }
            String lemma = luceneMorph.getNormalForms(word).get(0);
            lemmaMap.put(lemma, lemmaMap.getOrDefault(lemma, 0) + 1);
        }
        return lemmaMap;
    }

    private List<String> russianPropperWordsList(String text)
    {
        String[] textArray = text.toLowerCase(Locale.ROOT)
                .trim()
                .split("\\b");

        return Arrays.stream(textArray)
                .filter(s -> s.matches(REG_EXP_RUSSIAN_WORD))
                .filter(this::isPropperWord).collect(Collectors.toList());
    }

    private boolean isPropperWord(String word)
    {
        String[] morphElementsInfo = luceneMorph.getMorphInfo(word).get(0).split("\\s+");
        return morphElementsInfo.length > 2;
    }
}
