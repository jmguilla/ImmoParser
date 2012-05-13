package models;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;
import utils.fr.jmg.extractor.Main;
import utils.fr.jmg.extractor.api.Persistable;
import utils.fr.jmg.extractor.api.Type;

/*
 * A bean to describe an appartment
 */
@Entity
public class Description extends Model implements Persistable, Serializable {

	public static Finder<Long,Description> find = new Finder(Long.class, Description.class);

	public BigDecimal price, area;
	public Type type;
	public String zipCode, address, city, lattitude, longitude, url, author;
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
//
//	public boolean isWeekly() {
//		return this.weekly;
//	}
//
//	public String getAuthor() {
//		return this.author;
//	}
//	
//	public BigDecimal getArea() {
//		return area;
//	}
//
//	public Date getCreation() {
//		return creation;
//	}
//
//	public final boolean isValid() {
//		return this.valid;
//	}
//	public final String getAddress() {
//		return address;
//	}
//
//	public final String getCity() {
//		return city;
//	}
//
//	public final String getLattitude() {
//		return lattitude;
//	}
//
//	public final String getLongitude() {
//		return longitude;
//	}
//
//	public final String getUrl() {
//		return url;
//	}
//
//	public final String getZipCode() {
//		return zipCode;
//	}
//
//	public final BigDecimal getPrice() {
//		return price;
//	}
//
//	public final Type getType() {
//		return type;
//	}
//
//	public final Provider getProvider() {
//		String url = getUrl();
//		if(url != null){
//			for(Provider provider : Provider.values()){
//				if(url.contains(provider.name)){
//					return provider;
//				}
//			}
//		}
//		return Provider.UNKNOWN;
//	}

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
