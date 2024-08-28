package com.example.quranapp10;

import android.content.Context;
import org.jqurantree.orthography.Chapter;
import org.jqurantree.orthography.Token;
import org.jqurantree.orthography.Verse;
import org.jqurantree.tanzil.TanzilReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TextManager {
    public static TextManager instance;
    private Chapter[] quranDocument;
    private HashMap<String, String> translations;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public TextManager(Context context, Runnable onLoaded) {
        quranDocument = new TanzilReader().readXml(TanzilReader.TANZIL_RESOURCE_PATH);
        executor.submit(() -> {
            loadTranslations(context);
            onLoaded.run();
        });
    }
    public String getTranslationKey(int surah, int ayah, int word) {
        // Adjusting surah and ayah to start from 1
        return (surah + 1) + ":" + (ayah) + ":" + word + ":1";
    }
    public void loadTranslations(Context context) {
        HashMap<String, String> loadedTranslations = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open("wordtranslation.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().replaceAll("^\\['|'\\]$", "").split("',\\s*'");
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim().replaceAll("^\"|\"$", "").replaceAll("[\\[\\]'\"\\]]", "");
                    loadedTranslations.put(key, value);
                }
            }
            translations = loadedTranslations; // Assign the loaded map to the class field
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String getTranslation(String key) {
        // Ensure that translations is not null and contains the key
        return translations != null ? translations.getOrDefault(key, "Translation not found") : "Translation not found";
    }

    public String getTranslation(int surah, int ayah, int word) {
        // Construct the key as Surah:Ayah:Word:1 (considering all start from 1 in this representation)
        String key = (surah + 1) + ":" + (ayah) + ":" + word + ":1";
        return getTranslation(key); // Use the existing method to fetch the translation
    }
    public String getVerseTranslation(int surah, int ayah) {
        String translation = "";
        for (int word = 1; word <= getTokenCount(surah, ayah); word++) {
            translation += getTranslation(surah, ayah, word) + " ";
        }
        return translation;
    }


    public String getVerse(int surah, int ayah) {
        Chapter chapter = quranDocument[surah];
        Verse verse = chapter.getVerse(ayah);
        return verse.toString();  // Ensure Verse class has a toString() that returns the verse text
    }
    public String getVerseArabic(int surah, int ayah) {
        Chapter chapter = quranDocument[surah];
        Verse verse = chapter.getVerse(ayah);
        return verse.toUnicode();
    }
    public String getWord(int surah, int ayah, int word) {
        Chapter chapter = quranDocument[surah];
        Verse verse = chapter.getVerse(ayah);
        Token token = verse.getToken(word);
        return token.toUnicode();
    }
    public int getTokenCount(int surah, int ayah) {
        Chapter chapter = quranDocument[surah];
        Verse verse = chapter.getVerse(ayah);
        return verse.getTokenCount();
    }

    public int getVerseCount(int surah) {
        return quranDocument[surah].getVerseCount();
    }
    public int getChapterCount() {
        return quranDocument.length;
    }
}



