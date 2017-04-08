package wit.ie.invasivespeciestracker.model;



/**
 * Created by brendan on 06/11/2016.
 */

public class Species {

    private String species, habitat, location, date, additionalInfo;


    public Species() {
    }

    public Species(String species, String habitat, String location, String date, String additionalInfo) {


        this.species = species;
        this.habitat = habitat;
        this.location = location;
        this.date = date;
        this.additionalInfo = additionalInfo;
    }


    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getHabitat() {
        return habitat;
    }

    public void setHabitat(String habitat) {
        this.habitat = habitat;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }


}
