package com.example.ead.be;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
public class ServiceController {

    @Autowired
    private Persistence persistence;

    @GetMapping("/")
    public String index() {
        return "Greetings from EAD CA2 Template project 2024-25!";
    }

    @GetMapping("/recipes")
    public List<Recipe> getAllRecipes() {
        System.out.println("About to get all the recipes in MongoDB!");
        return persistence.getAllRecipes();
    }

    @DeleteMapping("/recipe/{name}")
    public int deleteRecipe(@PathVariable("name") String name) {
        System.out.println("About to delete all the recipes named " + name);
        return persistence.deleteRecipesByName(Arrays.asList(name));
    }

    @PostMapping("/recipe")
    @ResponseStatus(HttpStatus.CREATED)
    public int saveRecipe(@RequestBody Recipe rec) {
        System.out.println("=== Recipe Submission Debug ===");
        System.out.println("Received recipe data: " + rec);
        System.out.println("Recipe name: " + rec.getName());
        System.out.println("Ingredients: " + rec.getIngredients());
        System.out.println("Prep time: " + rec.getPrepTimeInMinutes());

        try {
            int result = persistence.addRecipes(Arrays.asList(rec));
            System.out.println("MongoDB operation result: " + result);
            System.out.println("=== End Recipe Submission Debug ===");
            return result;
        } catch (Exception e) {
            System.err.println("Error saving recipe: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

}
