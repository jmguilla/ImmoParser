package models;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;
import utils.fr.jmg.extractor.Main;
import utils.fr.jmg.extractor.api.Persistable;
import utils.fr.jmg.extractor.api.Type;

/*
 * A bean to describe an appartment
 */
@Entity
public class Description extends Model implements Persistable, Serializable {

	public static Finder<String,Description> find = new Finder(String.class, Description.class);

	@Id
	public String url;
	public BigDecimal price, area;
	public Type type;
	public String zipCode, address, city, lattitude, longitude, author;
	public boolean valid, weekly;
	public Date creation;

	public Description(Type type, BigDecimal price, String cp, String address,
			String city, String lat, String lon, String url, boolean valid, BigDecimal area, Date creation, String author, boolean weekly) {
		this.weekly = weekly;
		this.author = author;
		this.price = price;
		this.type = type;
		this.zipCode = cp;
		this.url = url;
		this.address = address;
		this.city = city;
		this.lattitude = lat;
		this.longitude = lon;
		this.valid = valid;
		this.area = area;
		this.creation = creation;
	}

	public String toString() {
		return url + " " + type + " " + zipCode + " " + price + " " + area;
	}

	public static List<Description> all() {
		return find.all();
	}

	public static void _refresh() throws IOException{
		Main.main(null);
	}
}