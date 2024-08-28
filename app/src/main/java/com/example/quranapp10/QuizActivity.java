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

public class QuizActivity extends AppCompatActivity {
    private TextManager textManager;
    private TextView arabicVerseTextView, wordTextView, feedbackTextView;
    private RadioGroup answersGroup;
    private Button backButton, nextWordButton;
    private int currentSurah;
    private int currentAyah;
    private int currentWordIndex = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        initUI();
        textManager = new TextManager(this, this::onDataLoaded);
    }

    private void initUI() {
        arabicVerseTextView = findViewById(R.id.arabicVerseTextView);
        wordTextView = findViewById(R.id.wordTextView);
        feedbackTextView = findViewById(R.id.feedbackTextView);
        answersGroup = findViewById(R.id.answersGroup);
        backButton = findViewById(R.id.backButton);
        nextWordButton = findViewById(R.id.nextButton);

        backButton.setOnClickListener(v -> finish());
        nextWordButton.setOnClickListener(v -> displayNextWordQuiz());

        currentSurah = getIntent().getIntExtra("currentSurah", 0); // Default to 0 if not found
        currentAyah = getIntent().getIntExtra("currentAyah", 1); // Default to 1 if not found

        enableUI(false);  // Disable UI until data is loaded
    }

    private void onDataLoaded() {
        runOnUiThread(() -> {
            enableUI(true);
            displayWordQuiz();
        });
    }

    private void enableUI(boolean enable) {
        backButton.setEnabled(enable);
        nextWordButton.setEnabled(enable);
        answersGroup.setEnabled(enable);
    }

    private void displayWordQuiz() {
        String arabicVerse = textManager.getVerseArabic(currentSurah, currentAyah);
        arabicVerseTextView.setText(arabicVerse);

        String currentWord = textManager.getWord(currentSurah, currentAyah, currentWordIndex);
        wordTextView.setText(currentWord);

        String correctAnswer = textManager.getTranslation(currentSurah, currentAyah, currentWordIndex);
        List<String> answers = generateRandomAnswers(correctAnswer);
        displayAnswers(answers, correctAnswer);
    }

    private List<String> generateRandomAnswers(String correctAnswer) {
        ArrayList<String> answers = new ArrayList<>();
        answers.add(correctAnswer);
        while (answers.size() < 4) {
            String randomAnswer = textManager.getTranslation(
                    new Random().nextInt(textManager.getChapterCount()),
                    new Random().nextInt(textManager.getVerseCount(currentSurah)),
                    new Random().nextInt(textManager.getTokenCount(currentSurah, currentAyah)));
            if (!answers.contains(randomAnswer) && !randomAnswer.equals(correctAnswer)) {
                answers.add(randomAnswer);
            }
        }
        Collections.shuffle(answers);
        return answers;
    }

    private void displayAnswers(List<String> answers, String correctAnswer) {
        answersGroup.removeAllViews();
        for (int i = 0; i < answers.size(); i++) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(answers.get(i));
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

    private void displayNextWordQuiz() {
        int tokenCount = textManager.getTokenCount(currentSurah, currentAyah);
        currentWordIndex++;
        if (currentWordIndex > tokenCount) {
            currentWordIndex = 1; // Wrap around to the first word
        }
        feedbackTextView.setText("");
        displayWordQuiz();
    }
}


