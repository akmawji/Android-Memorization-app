package com.example.quranapp10;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class VerseQuizActivity extends AppCompatActivity {
    private TextManager textManager;
    private TextView arabicVerseTextView, feedbackTextView;
    private RadioGroup answersGroup;
    private Button backButton, nextVerseButton;
    private int currentSurah;
    private int currentAyah;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verse_quiz);  // Reusing the same layout with modifications for verses

        initUI();
        textManager = new TextManager(this, this::onDataLoaded);
    }

    private void initUI() {
        arabicVerseTextView = findViewById(R.id.arabicVerseTextView);
        feedbackTextView = findViewById(R.id.feedbackTextView);
        answersGroup = findViewById(R.id.answersGroup);
        backButton = findViewById(R.id.backButton);
        nextVerseButton = findViewById(R.id.nextButton);

        backButton.setOnClickListener(v -> finish());
        nextVerseButton.setOnClickListener(v -> displayNextVerseQuiz());

        currentSurah = getIntent().getIntExtra("currentSurah", 0); // Default to 0 if not found
        currentAyah = getIntent().getIntExtra("currentAyah", 1); // Default to 1 if not found

        enableUI(false);  // Disable UI until data is loaded
    }

    private void onDataLoaded() {
        runOnUiThread(() -> {
            enableUI(true);
            displayVerseQuiz();
        });
    }

    private void enableUI(boolean enable) {
        backButton.setEnabled(enable);
        nextVerseButton.setEnabled(enable);
        answersGroup.setEnabled(enable);
    }

    private void displayVerseQuiz() {
        String arabicVerse = textManager.getVerseArabic(currentSurah, currentAyah);
        arabicVerseTextView.setText(arabicVerse);

        String correctAnswer = textManager.getVerseTranslation(currentSurah, currentAyah); // Assume Verse has a toString() method returning the translation
        List<String> answers = generateRandomAnswers(correctAnswer);
        displayAnswers(answers, correctAnswer);
    }

    private List<String> generateRandomAnswers(String correctAnswer) {
        ArrayList<String> answers = new ArrayList<>();
        answers.add(correctAnswer);
        while (answers.size() < 4) {
            int randomAyah = new Random().nextInt(textManager.getVerseCount(currentSurah)) + 1;
            String randomAnswer = textManager.getVerseTranslation(currentSurah, randomAyah);
            if (!answers.contains(randomAnswer) && !randomAnswer.equals(correctAnswer)) {
                answers.add(randomAnswer);
            }
        }
        Collections.shuffle(answers);
        return answers;
    }

    private void displayAnswers(List<String> answers, String correctAnswer) {
        answersGroup.removeAllViews();
        for (String answer : answers) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(answer);
            radioButton.setId(View.generateViewId());
            radioButton.setOnClickListener(v -> {
                if (radioButton.getText().equals(correctAnswer)) {
                    feedbackTextView.setText("Correct!");
                } else {
                    feedbackTextView.setText("Incorrect, try again!");
                }
            });
            answersGroup.addView(radioButton);
        }
    }

    private void displayNextVerseQuiz() {
        currentAyah++;
        if (currentAyah > textManager.getVerseCount(currentSurah)) {
            currentAyah = 1; // Wrap around to the first verse if it exceeds the number of verses
            currentSurah = (currentSurah + 1) % textManager.getChapterCount(); // Cycle through surahs
        }
        feedbackTextView.setText("");
        displayVerseQuiz();
    }
}
