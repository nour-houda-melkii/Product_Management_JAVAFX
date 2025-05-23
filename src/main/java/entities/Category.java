package entities;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.IntegerProperty;

public class Category {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();

    public Category() {}

    public Category(String name) {
        setName(name);
    }

    public Category(int id, String name) {
        setId(id);
        setName(name);
    }

    public Category(String name, String description) {
        setName(name);
        setDescription(description);
    }

    public Category(int id, String name, String description) {
        setId(id);
        setName(name);
        setDescription(description);
    }

    // Getters and Setters
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }
    public StringProperty descriptionProperty() { return description; }

    @Override
    public String toString() {
        return getName();
    }
}