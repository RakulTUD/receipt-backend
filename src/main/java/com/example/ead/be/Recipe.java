package com.example.ead.be;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class Recipe {
    @JsonProperty("name")
    private String name;

    @JsonProperty("ingredients")
    private List<String> ingredients;

    @JsonProperty("prepTimeInMinutes")
    private int prepTimeInMinutes;

    public Recipe(@JsonProperty("name") String name,
                  @JsonProperty("ingredients") List<String> ingredients,
                  @JsonProperty("prepTimeInMinutes") int prepTimeInMinutes) {
        this.name = name;
        this.ingredients = ingredients;
        this.prepTimeInMinutes = prepTimeInMinutes;
    }

    public Recipe() {
        ingredients = new ArrayList<>();
        name = "";
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Recipe{");
        sb.append("name=").append(name);
        sb.append(", ingredients=").append(ingredients);
        sb.append(", prepTimeInMinutes=").append(prepTimeInMinutes);
        sb.append('}');
        return sb.toString();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<String> getIngredients() { return ingredients; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }
    public int getPrepTimeInMinutes() { return prepTimeInMinutes; }
    public void setPrepTimeInMinutes(int prepTimeInMinutes) { this.prepTimeInMinutes = prepTimeInMinutes; }
}