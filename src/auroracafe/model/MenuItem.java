package auroracafe.model;

import java.io.Serializable;

public class MenuItem implements Serializable {
    private final int id;
    private String name;
    private String category;
    private double price;
    private boolean available;
    private String description;
    private String imageUrl;

    public MenuItem(int id, String name, String category, double price, boolean available, String description) {
        this(id, name, category, price, available, description, "");
    }

    public MenuItem(int id, String name, String category, double price, boolean available, String description, String imageUrl) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.available = available;
        this.description = description;
        this.imageUrl = imageUrl == null ? "" : imageUrl;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl == null ? "" : imageUrl; }

    @Override
    public String toString() { return name; }
}
