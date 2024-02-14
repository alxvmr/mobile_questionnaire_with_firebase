package com.example.applicationquestionnairesgmu;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
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
                else if (q.answers.size() == 1){ // edit text
                    createEditText(layout, number_question);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Button button_back = (Button)findViewById(R.id.btn_back);
        button_back.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(QuestionnaireActivity.this, MenuActivity.class);
                startActivity(intent);
            }
        });
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

            int id = View.generateViewId();
            rb[i].setId(id);

            rg.addView(rb[i]);
        }

        layout.addView(rg);
    }

    @SuppressLint("ResourceAsColor")
    private void createEditText(LinearLayout layout, Integer card_number){
        EditText editText = new EditText(this);
        int height = (int) getResources().getDimension(R.dimen.edit_text_height);

        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                height);
        llp.setMargins(dpToPx(10), dpToPx(10), 0, 0);
        editText.setLayoutParams(llp);
        editText.setBackgroundResource(R.drawable.custom_edittext);
        editText.setCompoundDrawablePadding(dpToPx(8));
        editText.setHint("Введите значение");
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        editText.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        editText.setTextColor(R.color.black);
        //editText.getBackground().mutate().setColorFilter(R.color.cardview_dark_background), PorterDuff.Mode.SRC_ATOP);
        int id = View.generateViewId();
        editText.setId(id);

        layout.addView(editText);
    }

    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px)
    {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }
}