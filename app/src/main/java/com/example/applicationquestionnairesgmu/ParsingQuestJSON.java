package com.example.applicationquestionnairesgmu;

import android.content.Context;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;
import java.io.*;

class Question{
    public Integer number_quest;
    public String quest_text;
    public List<Answer> answers;
}

class Answer{
    public int number_answer;
    public String text;
}

public class ParsingQuestJSON {
    private String pathJSON;
    public Question[] questions;

    public ParsingQuestJSON(String path, Context context) throws IOException {
        pathJSON = path;
        Gson g = new Gson();
        questions = g.fromJson(readFromPathJSON(context), Question[].class);
    }

    public void show(){
        List<Question> qList = Arrays.asList(questions);
        for (Question q : qList){
            System.out.println(q.number_quest);
            System.out.println(q.quest_text);
            for (Answer a : q.answers){
                System.out.println(a.number_answer);
                System.out.println(a.text);
            }
            System.out.println("\n");
        }
    }

    private String readFromPathJSON(Context context) throws IOException {
        String json = null;
        try {
            InputStream is = context.getAssets().open(pathJSON);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
