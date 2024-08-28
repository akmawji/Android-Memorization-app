package com.example.quranapp10;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextManager textManager;
    private TextView arabicTextView;
    private TextView translationTextView;
    private TextView wordTranslationTextView;
    private TextView wordTextView;
    private Spinner chapterSpinner;
    private Spinner verseSpinner;
    private ArrayAdapter<String> chapterAdapter;
    private ArrayAdapter<String> verseAdapter;
    private Button prevVerseButton, nextVerseButton, prevWordButton, nextWordButton, startQuizButton, startVerseQuizButton;
    private int currentSurah = 0;
    private int currentAyah = 1;
    private int currentWordIndex = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        textManager = new TextManager(this, this::onTranslationsLoaded);  // Passing callback to handle post-load operations
    }

    private void initUI() {
        arabicTextView = findViewById(R.id.arabicTextView);
        translationTextView = findViewById(R.id.translationTextView);
        wordTextView = findViewById(R.id.wordTextView);
        wordTranslationTextView = findViewById(R.id.wordTranslationTextView);

        chapterSpinner = findViewById(R.id.chapterSpinner);
        verseSpinner = findViewById(R.id.verseSpinner);

        prevVerseButton = findViewById(R.id.prevButton);
        nextVerseButton = findViewById(R.id.nextButton);
        prevWordButton = findViewById(R.id.prevWordButton);
        nextWordButton = findViewById(R.id.nextWordButton);
        startQuizButton = findViewById(R.id.startQuizButton);
        startQuizButton.setOnClickListener(v -> {
            Intent testIntent = new Intent(MainActivity.this, QuizActivity.class);
            testIntent.putExtra("currentSurah", currentSurah);
            testIntent.putExtra("currentAyah", currentAyah);
            startActivity(testIntent);
        });
        startVerseQuizButton = findViewById(R.id.startVerseQuizButton);
        startVerseQuizButton.setOnClickListener(v -> {
            Intent testIntent = new Intent(MainActivity.this, VerseQuizActivity.class);
            testIntent.putExtra("currentSurah", currentSurah);
            testIntent.putExtra("currentAyah", currentAyah);
            startActivity(testIntent);
        });
        prevVerseButton.setOnClickListener(v -> navigateVerse(false));
        nextVerseButton.setOnClickListener(v -> navigateVerse(true));

        // Set button listeners for word navigation
        prevWordButton.setOnClickListener(v -> navigateWord(false));
        nextWordButton.setOnClickListener(v -> navigateWord(true));

        enableUI(false);  // Disable UI until translations are loaded
    }

    private void onTranslationsLoaded() {
        // This method will be called once translations are loaded
        runOnUiThread(() -> {
            enableUI(true);
            setupSpinners();
            displayVerse(currentSurah, currentAyah);
        });
    }

    private void enableUI(boolean enable) {
        chapterSpinner.setEnabled(enable);
        verseSpinner.setEnabled(enable);
        prevVerseButton.setEnabled(enable);
        nextVerseButton.setEnabled(enable);
        prevWordButton.setEnabled(enable);
        nextWordButton.setEnabled(enable);
        startQuizButton.setEnabled(enable);
        startVerseQuizButton.setEnabled(enable);
    }
        private void setupSpinners() {
            chapterSpinner = findViewById(R.id.chapterSpinner);
            verseSpinner = findViewById(R.id.verseSpinner);

            // Populate chapter spinner
            List<String> chapters = new ArrayList<>();
            for (int i = 1; i <= textManager.getChapterCount(); i++) {
                chapters.add("Chapter " + i);
            }
            chapterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, chapters);
            chapterSpinner.setAdapter(chapterAdapter);

            // Set chapter spinner listener
            chapterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    currentSurah = position;
                    currentAyah = 1; // Reset to the first verse of the selected chapter
                    populateVerseSpinner();
                    displayVerse(currentSurah, currentAyah);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            populateVerseSpinner();
        }

        private void populateVerseSpinner() {
            List<String> verses = new ArrayList<>();
            for (int i = 1; i <= textManager.getVerseCount(currentSurah); i++) {
                verses.add("Verse " + i);
            }
            verseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, verses);
            verseSpinner.setAdapter(verseAdapter);

            // Set verse spinner listener
            verseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    currentAyah = position + 1;
                    displayVerse(currentSurah, currentAyah);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        private void displayVerse(int surah, int ayah) {
            String verseArabic = textManager.getVerseArabic(surah, ayah);
            StringBuilder verseTranslation = new StringBuilder();
            for (int i = 0; i < textManager.getTokenCount(surah,ayah); i++) {
                String key = textManager.getTranslationKey(currentSurah, currentAyah, currentWordIndex + i);
                String translation = textManager.getTranslation(key);
                verseTranslation.append(translation).append(" ");
            }
            arabicTextView.setText(verseArabic);
            translationTextView.setText(verseTranslation);
            currentWordIndex = 1;  // Reset the word index
            displayWord();
        }

        private void displayWord() {
            String word = textManager.getWord(currentSurah, currentAyah, currentWordIndex);
            wordTextView.setText(word);
            String key = textManager.getTranslationKey(currentSurah, currentAyah, currentWordIndex);
            String wordTranslation = textManager.getTranslation(key);
            wordTranslationTextView.setText(wordTranslation);
        }

        private void navigateVerse(boolean next) {
            if (next) {
                currentAyah++;
                if (currentAyah > textManager.getVerseCount(currentSurah)) {
                    currentSurah++;
                    if (currentSurah > textManager.getChapterCount() - 1) {
                        currentSurah = 0; // Wrap to the first surah
                    }
                    currentAyah = 1;
                }
            } else {
                currentAyah--;
                if (currentAyah < 1) {
                    currentSurah--;
                    if (currentSurah < 0) {
                        currentSurah = textManager.getChapterCount() - 1; // Wrap to the last surah
                    }
                    currentAyah = textManager.getVerseCount(currentSurah);
                }
            }
            displayVerse(currentSurah, currentAyah);
        }

        private void navigateWord(boolean next) {
            int tokenCount = textManager.getTokenCount(currentSurah, currentAyah);
            if (next) {
                currentWordIndex++;
                if (currentWordIndex > tokenCount) {
                    currentWordIndex = 1; // Wrap around to the first word
                }
            } else {
                currentWordIndex--;
                if (currentWordIndex < 1) {
                    currentWordIndex = tokenCount; // Wrap around to the last word
                }
            }
            displayWord();
        }
    }



