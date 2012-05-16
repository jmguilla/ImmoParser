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

    public static Finder<String, Description> find = new Finder(String.class,
            Description.class);

    @Id
    public String url;
    public BigDecimal price, area, latitude, longitude;
    public Type type;
    public String zipCode, address, city, author, formattedAddress;
    public boolean valid, weekly;
    public Date creation;

    public Description(Type type, BigDecimal price, String cp, String address,
            String city, String formattedAddress, BigDecimal latitude,
            BigDecimal longitude, String url, boolean valid, BigDecimal area,
            Date creation, String author, boolean weekly) {
        this.weekly = weekly;
        this.author = author;
        this.price = price;
        this.type = type;
        this.zipCode = cp;
        this.url = url;
        this.address = address;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.valid = valid;
        this.area = area;
        this.creation = creation;
        this.formattedAddress = formattedAddress;
    }

    public String toString() {
        return url + " " + type + " " + zipCode + " " + price + " " + area;
    }

    public static List<Description> all() {
        return find.all();
    }

    public static void _refresh() throws IOException {
        Main.main(null);
    }
}
