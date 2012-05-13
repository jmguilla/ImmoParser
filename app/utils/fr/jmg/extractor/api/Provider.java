package utils.fr.jmg.extractor.api;

public enum Provider {
	LEBONCOIN("leboncoin"), ABRITEL("abritel"), UNKNOWN("unknown");
	public final String name;
	Provider(String name){
		this.name = name;
	}
}
