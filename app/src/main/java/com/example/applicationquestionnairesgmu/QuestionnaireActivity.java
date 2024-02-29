package com.example.applicationquestionnairesgmu;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.loader.content.AsyncTaskLoader;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Layout;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class QuestionnaireActivity extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    private DatabaseReference myRef;
    FirebaseUser user = mAuth.getCurrentUser();

    private String pathQuest = "quest.json"; // данные анкеты - id_вопроса, номер_вопроса, начало нумерации(0, 1, -1 (если поле ввода)), ответ
    private ArrayList<HashMap<String,Integer>> anketa_data = new ArrayList<HashMap<String,Integer>>();

    LinearLayout LLoader;
    LinearLayout LQuest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        LLoader = (LinearLayout)findViewById(R.id.layout_loader);
        LQuest = (LinearLayout)findViewById(R.id.layout_quest);

        LQuest.setVisibility(View.GONE);
        LLoader.setVisibility(View.VISIBLE);

        new Thread(new Runnable() {
            public void run(){
                // считывание вопросов анкеты из json
                try {
                    ParsingQuestJSON parsingQuest = new ParsingQuestJSON(pathQuest, getApplicationContext());

                    // генерация Activity
                    List<Question> qList = Arrays.asList(parsingQuest.questions);
                    for (Question q : qList){
                        HashMap<String, Integer> question_data = new HashMap<>();

                        Integer number_question = q.number_quest;
                        String question_text = q.quest_text;

                        question_data.put("number_question", number_question);

                        String tvID = "card_" + number_question.toString() + "_question";
                        int resID = getResources().getIdentifier(tvID, "id", getPackageName());
                        TextView tv = (TextView) findViewById(resID);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv.setText(question_text);
                            }
                        });

                        String llID = "ll_" + number_question.toString();
                        int resLLID = getResources().getIdentifier(llID, "id", getPackageName());
                        LinearLayout layout = (LinearLayout) findViewById(resLLID);
                        if (q.answers.size() > 1){ // radio-button
                            // определение начала нумерации
                            question_data.put("start_num", q.answers.get(0).number_answer);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    createRadioButton(q.answers, layout, question_data);
                                }
                            });
                        }
                        else if (q.answers.size() == 1){ // edit text
                            question_data.put("start_num", -1); // поле ввода без выбора
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    createEditText(layout, question_data);
                                }
                            });
                        }
                        anketa_data.add(question_data);
                    }
                    Thread.sleep(3000);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LQuest.setVisibility(View.VISIBLE);
                            LLoader.setVisibility(View.GONE);
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        Button button_back = (Button)findViewById(R.id.btn_back);
        button_back.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(QuestionnaireActivity.this, MenuActivity.class);
                startActivity(intent);
            }
        });

        //RadioGroup rg_card_1 = findViewById(R.id.radios);

        Button button_send_answers = (Button) findViewById(R.id.button_send);
        button_send_answers.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                HashMap<String, String> answers = get_answers();
                HashMap<String, List<Integer>> ef_question = get_empty_filled_questions(answers);
                List<Integer> empty_num_q = ef_question.get("empty");
                List<Integer> filled_num_q = ef_question.get("filled");
                if (empty_num_q.size() != 0){
                    // меняем цвет незаполненных вопросов
                    for (Integer num : empty_num_q){
                        change_color_question_empty(num);
                    }
                    // меняем цвет заполненных ответов
                    for (Integer num : filled_num_q){
                        change_color_question_filled(num);
                    }
                    Toast.makeText(QuestionnaireActivity.this, "Заполните все вопросы", Toast.LENGTH_SHORT).show();
                }
                else{
                    // отправляем данные в БД
                    myRef = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference usersRef = myRef.child(user.getUid());
                    usersRef.push().setValue(answers);

                    Intent intent = new Intent(QuestionnaireActivity.this, MenuActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    private void createRadioButton(List<Answer> answersList, LinearLayout  layout, HashMap<String, Integer> q_data) {
        int count = answersList.size();
        final RadioButton[] rb = new RadioButton[count];

        RadioGroup rg = new RadioGroup(this);
        rg.setOrientation(RadioGroup.VERTICAL);
        rg.setLayoutParams(new RadioGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        int rg_id = View.generateViewId();
        rg.setId(rg_id);
        q_data.put("id", rg_id);

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
    private void createEditText(LinearLayout layout, HashMap<String, Integer> q_data){
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
        int et_id = View.generateViewId();
        editText.setId(et_id);
        q_data.put("id", et_id);

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

    private HashMap<String, String> get_answers(){
        HashMap<String, String> answers = new HashMap<String, String>();
        for (HashMap<String, Integer> question : anketa_data){
            int start_num = question.get("start_num");
            if (start_num != -1) { // если это radio group
                int rg_id = question.get("id");
                RadioGroup rg = (RadioGroup) findViewById(rg_id);
                int rb_checked_id = rg.getCheckedRadioButtonId();
                View rb_checked = rg.findViewById(rb_checked_id);
                Integer index = rg.indexOfChild(rb_checked);
                if (index != -1) {
                    if (start_num == 0) {
                        answers.put(question.get("number_question").toString(), index.toString());
                    } else {
                        index += 1;
                        answers.put(question.get("number_question").toString(), index.toString());
                    }
                }
                else{
                    answers.put(question.get("number_question").toString(), "-1");
                }
            }
            else { // это editText
                int et_id = question.get("id");
                EditText et = (EditText) findViewById(et_id);
                answers.put(question.get("number_question").toString(), et.getText().toString());
            }
        }
        return answers;
    }
    private HashMap<String, List<Integer>> get_empty_filled_questions(HashMap<String, String> answers){
        HashMap<String, List<Integer>> res = new HashMap<String, List<Integer>>();
        List<Integer> res_empty = new ArrayList<Integer>();
        List<Integer> res_filled = new ArrayList<Integer>();
        for (Map.Entry<String, String> a : answers.entrySet()){
            if (Objects.equals(a.getValue(), "-1") || Objects.equals(a.getValue(), "")){
                res_empty.add(Integer.parseInt(a.getKey()));
            }
            else{
                res_filled.add(Integer.parseInt(a.getKey()));
            }
        }
        res.put("empty", res_empty);
        res.put("filled", res_filled);
        return res;
    }

    private void change_color_question_empty(Integer num_q){
        String id_card = "ll_"+num_q.toString();
        int id_card_int = getResources().getIdentifier(id_card, "id", getPackageName());
        LinearLayout ll = (LinearLayout) findViewById(id_card_int);
        ll.setBackgroundColor(ContextCompat.getColor(this, R.color.empty_answer));
    }

    private void change_color_question_filled(Integer num_q){
        String id_card = "ll_"+num_q.toString();
        int id_card_int = getResources().getIdentifier(id_card, "id", getPackageName());
        LinearLayout ll = (LinearLayout) findViewById(id_card_int);
        ll.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
    }
}