package hu.bme.mit.textmine.mongo.dictionary.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.Getter;

@Getter
public enum PartOfSpeech {
    PRONOMINAL_ADVERB("nm-hsz"),
    PRONOUN("nm"),
    NOUN("fn"),
    INFINITIVE("fn-ign"),
    ADVERBIAL_PARTICIPLE("hat-ign"),
    ARTICLE("ne"),
    ADVERB("hsz"),
    NUMERAL("szn"),
    VERB("ige"),
    VERB_PARTICIPLE("ige-ign"),
    PREVERB("ik"),
    INTERJECTION("isz"),
    CONJUNCTIVE("ksz"),
    ADJECTIVE("mn"),
    PARTICIPLE("mn-ign"),
    MODIFIER("módsz"),
    SENTENTIAL("msz"),
    POSTPOSITION("nu"),
    POSTPOSITIONAL_ADJECTIVE("nu-mn"),
    PARTICLE("part"),
    AUXILIARY("ssz");

    private String hungarianAbbreviation;

    private static final Map<String, PartOfSpeech> mapping = Maps.newHashMap();

    static {
        for (PartOfSpeech pos : PartOfSpeech.values()) {
            PartOfSpeech.mapping.put(pos.hungarianAbbreviation, pos);
            PartOfSpeech.mapping.put(pos.toString(), pos);
            PartOfSpeech.mapping.put(pos.toString().toLowerCase(), pos);
        }
    }

    PartOfSpeech(String hungarianAbbreviation) {
        this.hungarianAbbreviation = hungarianAbbreviation;
    }

    public static PartOfSpeech of(String abbrev) {
        if (null == abbrev || abbrev.equals("")) {
            return null;
        }
        PartOfSpeech pos = PartOfSpeech.mapping.get(abbrev.toLowerCase());
        if (pos == null) {
            throw new IllegalArgumentException("Invalid POS abbreviation: " + abbrev);
        }
        return pos;
    }

    public static List<PartOfSpeech> of(List<String> abbrevs) {
        if (null == abbrevs) {
            return Lists.newArrayList();
        }
        return abbrevs.stream().map(PartOfSpeech::of).collect(Collectors.toList());
    }
}
