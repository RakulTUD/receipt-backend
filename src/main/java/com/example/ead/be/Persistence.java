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

    @Value("${databaseCollection}")
    private String mongoCollection;

    private MongoClient mongoClient;
    private MongoCollection<Recipe> collection;

    public static List<Recipe> recipes = Arrays.asList(
        new Recipe("elotes", Arrays.asList("corn", "mayonnaise", "cotija cheese", "sour cream", "lime"), 35),
        new Recipe("loco moco", Arrays.asList("ground beef", "butter", "onion", "egg", "bread bun", "mushrooms"), 54),
        new Recipe("patatas bravas", Arrays.asList("potato", "tomato", "olive oil", "onion", "garlic", "paprika"), 80),
        new Recipe("fried rice", Arrays.asList("rice", "soy sauce", "egg", "onion", "pea", "carrot", "sesame oil"), 40)
    );

    public Persistence() {}

    @PostConstruct
    public void initMongoDBClient() {
        Logger.getLogger("org.mongodb.driver").setLevel(Level.WARNING);

        // Debug prints for MongoDB connection values
        System.out.println("=== MongoDB Connection Debug ===");
        System.out.println("mongoUri: " + mongoUri);
        System.out.println("mongoDatabase: " + mongoDatabase);
        System.out.println("mongoCollection: " + mongoCollection);
        System.out.println("===============================");

        // Check for null or empty mongoUri
        if (mongoUri == null || mongoUri.isEmpty()) {
            System.err.println("MongoDB URI is not configured. Please set 'spring.data.mongodb.uri' in application.properties.");
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
        } catch (MongoException me) {
            System.err.println("Unable to connect to MongoDB: " + me);
            System.exit(1);
        }

        MongoDatabase database = mongoClient.getDatabase(mongoDatabase);
        collection = database.getCollection(mongoCollection, Recipe.class);
    }

    public List<Recipe> getAllRecipes() {
        MongoCursor<Recipe> cur = collection.find().iterator();
        List<Recipe> myRecipes = new ArrayList<>();
        while (cur.hasNext()) {
            myRecipes.add(cur.next());
        }
        return myRecipes;
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
            System.err.println("Unable to delete recipes: " + me);
            return -1;
        }
    }

    public int deleteRecipesByName(List<String> recipeNames) {
        Bson deleteFilter = Filters.in("name", recipeNames);
        return this.deleteRecipes(deleteFilter);
    }
}