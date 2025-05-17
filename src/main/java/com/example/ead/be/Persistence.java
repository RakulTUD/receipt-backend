package com.example.ead.be;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Component
public class Persistence {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String mongoDatabase;

    @Value("${spring.data.mongodb.collection}")
    private String mongoCollection;

    private final Environment environment; // To access active profiles

    private MongoClient mongoClient;
    private MongoCollection<Recipe> collection;

    public static List<Recipe> recipes = Arrays.asList(
        new Recipe("elotes", Arrays.asList("corn", "mayonnaise", "cotija cheese", "sour cream", "lime"), 35),
        new Recipe("loco moco", Arrays.asList("ground beef", "butter", "onion", "egg", "bread bun", "mushrooms"), 54),
        new Recipe("patatas bravas", Arrays.asList("potato", "tomato", "olive oil", "onion", "garlic", "paprika"), 80),
        new Recipe("fried rice", Arrays.asList("rice", "soy sauce", "egg", "onion", "pea", "carrot", "sesame oil"), 40)
    );

    // Inject Environment to access active profiles
    public Persistence(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void initMongoDBClient() {
        Logger.getLogger("org.mongodb.driver").setLevel(Level.WARNING);

        // Log active profiles and configuration
        System.out.println("=== MongoDB Connection Debug ===");
        System.out.println("Active Profiles: " + Arrays.toString(environment.getActiveProfiles()));
        System.out.println("mongoUri: " + mongoUri);
        System.out.println("mongoDatabase: " + mongoDatabase);
        System.out.println("mongoCollection: " + mongoCollection);
        System.out.println("===============================");

        // Validate MongoDB URI
        if (mongoUri == null || mongoUri.isEmpty()) {
            System.err.println("MongoDB URI is not configured for profile: " + Arrays.toString(environment.getActiveProfiles()));
            throw new IllegalStateException("MongoDB URI is not configured.");
        }

        ConnectionString connectionString = new ConnectionString(mongoUri);

        CodecRegistry pojoCodecRegistry = fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            fromProviders(PojoCodecProvider.builder().automatic(true).build())
        );

        MongoClientSettings settings = MongoClientSettings.builder()
            .codecRegistry(pojoCodecRegistry)
            .applyConnectionString(connectionString)
            .build();

        try {
            mongoClient = MongoClients.create(settings);
            MongoDatabase database = mongoClient.getDatabase(mongoDatabase);
            collection = database.getCollection(mongoCollection, Recipe.class);
            System.out.println("Successfully connected to MongoDB database: " + mongoDatabase);

            // Seed default recipes if collection is empty
            seedInitialRecipes();
        } catch (MongoException me) {
            System.err.println("Unable to connect to MongoDB: " + me.getMessage());
            throw new RuntimeException("MongoDB connection failed", me);
        }
    }

    private void seedInitialRecipes() {
        try {
            // Check if the collection is empty
            long count = collection.countDocuments();
            if (count == 0) {
                System.out.println("Collection '" + mongoCollection + "' is empty. Seeding default recipes...");
                int insertedCount = addRecipes(recipes);
                if (insertedCount > 0) {
                    System.out.println("Successfully seeded " + insertedCount + " recipes.");
                } else {
                    System.err.println("Failed to seed default recipes.");
                }
            } else {
                System.out.println("Collection '" + mongoCollection + "' already contains " + count + " documents. Skipping seeding.");
            }
        } catch (MongoException me) {
            System.err.println("Error seeding default recipes: " + me.getMessage());
        }
    }

    public List<Recipe> getAllRecipes() {
        try (MongoCursor<Recipe> cur = collection.find().iterator()) {
            List<Recipe> myRecipes = new ArrayList<>();
            while (cur.hasNext()) {
                myRecipes.add(cur.next());
            }
            return myRecipes;
        } catch (MongoException me) {
            System.err.println("Error retrieving recipes: " + me.getMessage());
            return new ArrayList<>();
        }
    }

    public int addRecipes(List<Recipe> recipes) {
        try {
            InsertManyResult result = collection.insertMany(recipes);
            return result.getInsertedIds().size();
        } catch (MongoException me) {
            System.err.println("MongoDB Error: " + me.getMessage());
            return -1;
        }
    }

    public int deleteRecipes(Bson deleteFilter) {
        try {
            DeleteResult deleteResult = collection.deleteMany(deleteFilter);
            return (int) deleteResult.getDeletedCount();
        } catch (MongoException me) {
            System.err.println("Unable to delete recipes: " + me.getMessage());
            return -1;
        }
    }

    public int deleteRecipesByName(List<String> recipeNames) {
        Bson deleteFilter = Filters.in("name", recipeNames);
        return this.deleteRecipes(deleteFilter);
    }
}