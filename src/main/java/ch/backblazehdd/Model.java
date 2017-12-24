package ch.backblazehdd;

public class Model {

    String model = "";
    String manufacturer = "";

    public Model(String model) {
        this.model = model;
    }


    public Model(String model, String manufacturer) {
        this.model = model;
        this.manufacturer = manufacturer;
    }

    public Model() {
    }

}
