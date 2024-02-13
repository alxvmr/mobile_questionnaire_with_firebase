package com.example.applicationquestionnairesgmu;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class QuestionnaireActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private List<String> surveyAnsw; // список ответов анкеты
    private String pathQuest = "quest.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        // считывание вопросов анкеты из json
        try {
            ParsingQuestJSON parsingQuest = new ParsingQuestJSON(pathQuest, getApplicationContext());

            // генерация Activity
            List<Question> qList = Arrays.asList(parsingQuest.questions);
            for (Question q : qList){
                Integer number_question = q.number_quest;
                String question_text = q.quest_text;

                String tvID = "card_" + number_question.toString() + "_question";
                int resID = getResources().getIdentifier(tvID, "id", getPackageName());
                TextView tv = (TextView) findViewById(resID);
                tv.setText(question_text);

                String llID = "ll_" + number_question.toString();
                int resLLID = getResources().getIdentifier(llID, "id", getPackageName());
                LinearLayout layout = (LinearLayout) findViewById(resLLID);
                if (q.answers.size() > 1){ // radio-button
                    createRadioButton(q.answers, layout, number_question);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createRadioButton(List<Answer> answersList, LinearLayout  layout, Integer card_number) {
        int count = answersList.size();
        final RadioButton[] rb = new RadioButton[count];

        RadioGroup rg = new RadioGroup(this);
        rg.setOrientation(RadioGroup.VERTICAL);
        rg.setLayoutParams(new RadioGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        for(Integer i=0; i<count; i++){
            rb[i]  = new RadioButton(this);
            rb[i].setLayoutParams(new RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.WRAP_CONTENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT,
                    1f));
            rb[i].setPadding(10, 0, 0, 0);
            rb[i].setButtonTintList(ColorStateList.valueOf(getColor(R.color.purple)));
            rb[i].setText(answersList.get(i).text);

            String id = "card_" + card_number.toString() + "_rb_" + i.toString();
            int resID = getResources().getIdentifier(id, "id", getPackageName());
            rb[i].setId(resID);
            //Log.i("RadioButton", rb[i].toString());

            rg.addView(rb[i]);
        }

        layout.addView(rg);
    }
}