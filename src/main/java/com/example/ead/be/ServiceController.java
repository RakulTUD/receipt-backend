package com.example.ead.be;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Integer> deleteRecipe(@PathVariable("name") String name) {
        System.out.println("About to delete all the recipes named " + name);
        int result = persistence.deleteRecipesByName(Arrays.asList(name));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/recipe")
    public ResponseEntity<Void> saveRecipe(@RequestBody Recipe rec) {
        System.out.println("=== Recipe Submission Debug ===");
        System.out.println("Received recipe data: " + rec);
        System.out.println("Recipe name: " + rec.getName());
        System.out.println("Ingredients: " + rec.getIngredients());
        System.out.println("Prep time: " + rec.getPrepTimeInMinutes());

        try {
            int result = persistence.addRecipes(Arrays.asList(rec));
            System.out.println("MongoDB operation result: " + result);
            System.out.println("=== End Recipe Submission Debug ===");
            if (result > 0) {
                return new ResponseEntity<>(HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            System.err.println("Error saving recipe: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}