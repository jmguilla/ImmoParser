package utils.fr.jmg.extractor.api;

/*
 * Where all HTML tag go
 */
public enum HTMLTags {
    HREF("href"), SPAN("span"), A("a"), UL("ul");
    public final String name;

    HTMLTags(String name) {
        this.name = name;
    }
}
