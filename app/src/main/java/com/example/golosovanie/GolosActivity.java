package com.example.golosovanie;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import android.app.DatePickerDialog;
import android.widget.DatePicker;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import androidx.cardview.widget.CardView;
import android.widget.FrameLayout;
import java.text.SimpleDateFormat;
import android.view.Gravity;

public class GolosActivity extends AppCompatActivity {

    private EditText titleInput, descriptionInput;
    private Button createVoteButton, addAnswerButton, selectDateButton;
    private TextView endDateText;
    private LinearLayout answersContainer, votingListContainer;
    private ArrayList<String> answerOptions = new ArrayList<>();
    private Date endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_golos);

        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        createVoteButton = findViewById(R.id.createVoteButton);
        addAnswerButton = findViewById(R.id.addAnswerButton);
        selectDateButton = findViewById(R.id.selectDateButton);
        endDateText = findViewById(R.id.endDateText);
        answersContainer = findViewById(R.id.answersContainer);
        votingListContainer = findViewById(R.id.votingListContainer);

        addAnswerButton.setOnClickListener(view -> addAnswerField());
        selectDateButton.setOnClickListener(view -> showDatePicker());
        createVoteButton.setOnClickListener(view -> createVoting());

        startVotingUpdateTimer();
    }

    private void startVotingUpdateTimer() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                fetchVotings();
            }
        }, 0, 5000);
    }

    private void fetchVotings() {
        APIClient.get("/getVotings", response -> {
            try {
                votingListContainer.removeAllViews();
                if (response.trim().startsWith("[")) {
                    JSONArray votings = new JSONArray(response);

                    List<JSONObject> activeVotings = new ArrayList<>();
                    List<JSONObject> expiredVotings = new ArrayList<>();

                    long currentTime = System.currentTimeMillis();

                    for (int i = 0; i < votings.length(); i++) {
                        JSONObject voting = votings.getJSONObject(i);
                        long endDateMillis = voting.optLong("end_date");
                        if (endDateMillis > currentTime) {
                            activeVotings.add(voting);
                        } else {
                            expiredVotings.add(voting);
                        }
                    }

                    activeVotings.sort((v1, v2) -> v1.optString("title").compareTo(v2.optString("title")));
                    expiredVotings.sort((v1, v2) -> v1.optString("title").compareTo(v2.optString("title")));

                    if (!activeVotings.isEmpty()) {
                        addSectionHeader("Актуальные голосования");
                        for (JSONObject voting : activeVotings) {
                            displayVoting(voting);
                        }
                    }

                    if (!expiredVotings.isEmpty()) {
                        addSectionHeader("Неактуальные голосования");
                        for (JSONObject voting : expiredVotings) {
                            displayVoting(voting);
                        }
                    }

                } else {
                    // Ответ — объект с сообщением
                    JSONObject responseObject = new JSONObject(response);
                    if (responseObject.has("message")) {
                        String message = responseObject.getString("message");
                        TextView noVotingsMessage = new TextView(this);
                        noVotingsMessage.setText(message);
                        noVotingsMessage.setTextSize(18);
                        noVotingsMessage.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        noVotingsMessage.setPadding(16, 16, 16, 16);
                        votingListContainer.addView(noVotingsMessage);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(GolosActivity.this, "Ошибка при загрузке голосований", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Toast.makeText(GolosActivity.this, "Ошибка при загрузке голосований", Toast.LENGTH_SHORT).show();
        });
    }

    private void addSectionHeader(String headerText) {
        TextView header = new TextView(this);
        header.setText(headerText);
        header.setTextSize(24);
        header.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        header.setPadding(16, 16, 16, 16);
        votingListContainer.addView(header);
    }

    private void displayVoting(JSONObject voting) {
        try {
            String title = voting.optString("title");
            String description = voting.optString("description");
            JSONArray answerOptions = voting.optJSONArray("answer_options");
            JSONObject voteCounts = voting.optJSONObject("vote_counts");

            CardView cardView = new CardView(this);
            CardView.LayoutParams cardParams = new CardView.LayoutParams(
                    CardView.LayoutParams.MATCH_PARENT,
                    CardView.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(16, 16, 16, 16);
            cardView.setLayoutParams(cardParams);
            cardView.setContentPadding(16, 16, 16, 16);
            cardView.setRadius(8);
            cardView.setCardBackgroundColor(getResources().getColor(R.color.cardview_light_background));

            LinearLayout votingLayout = new LinearLayout(this);
            votingLayout.setOrientation(LinearLayout.VERTICAL);
            votingLayout.setPadding(16, 16, 16, 16);
            cardView.addView(votingLayout);

            TextView votingTitle = new TextView(this);
            votingTitle.setText(title);
            votingTitle.setTextSize(20);
            votingTitle.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            votingLayout.addView(votingTitle);

            TextView votingDescription = new TextView(this);
            votingDescription.setText(description);
            votingLayout.addView(votingDescription);

            long endDateMillis = voting.optLong("end_date");
            boolean isExpired = endDateMillis <= System.currentTimeMillis();

            for (int i = 0; i < answerOptions.length(); i++) {
                String option = answerOptions.optString(i);
                int count = voteCounts != null ? voteCounts.optInt(option, 0) : 0;

                LinearLayout optionLayout = new LinearLayout(this);
                optionLayout.setOrientation(LinearLayout.HORIZONTAL);

                Button answerButton = new Button(this);
                answerButton.setText(option);
                answerButton.setOnClickListener(view -> {
                    if (isExpired) {
                        Toast.makeText(this, "Голосование уже неактуально", Toast.LENGTH_SHORT).show();
                    } else {
                        voteForOption(voting.optInt("id"), option);
                    }
                });

                TextView voteCountText = new TextView(this);
                voteCountText.setText(" (" + count + " голосов)");
                voteCountText.setPadding(8, 0, 0, 0);

                optionLayout.addView(answerButton);
                optionLayout.addView(voteCountText);

                votingLayout.addView(optionLayout);
            }

            Date endDate = new Date(endDateMillis);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy, HH:mm");
            String formattedDate = dateFormat.format(endDate);

            TextView endDateText = new TextView(this);
            endDateText.setText("Дата окончания: " + formattedDate);
            votingLayout.addView(endDateText);

            Button deleteButton = new Button(this);
            deleteButton.setText("Удалить голосование");
            deleteButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            deleteButton.setTextColor(getResources().getColor(android.R.color.white));
            deleteButton.setOnClickListener(view -> deleteVoting(voting.optInt("id")));

            LinearLayout.LayoutParams deleteButtonParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            deleteButtonParams.setMargins(0, 16, 0, 0);
            deleteButton.setLayoutParams(deleteButtonParams);
            votingLayout.addView(deleteButton);

            votingListContainer.addView(cardView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void deleteVoting(int votingId) {
        APIClient.delete("/deleteVoting/" + votingId, response -> {
            Toast.makeText(GolosActivity.this, "Голосование удалено", Toast.LENGTH_SHORT).show();
            fetchVotings();
        }, error -> {
            Toast.makeText(GolosActivity.this, "Ошибка при удалении голосования", Toast.LENGTH_SHORT).show();
        });
    }

    private void voteForOption(int votingId, String answer) {
        DatabaseHelper dbHelper = new DatabaseHelper(GolosActivity.this);

        if (dbHelper.hasUserVoted(votingId)) {
            Toast.makeText(GolosActivity.this, "Вы уже проголосовали в этом голосовании", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject voteData = new JSONObject();
        try {
            voteData.put("answer", answer);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(GolosActivity.this, "Ошибка при подготовке данных для голосования", Toast.LENGTH_SHORT).show();
            return;
        }

        APIClient.post("/vote/" + votingId, voteData, response -> {
            Toast.makeText(GolosActivity.this, "Ваш голос учтен", Toast.LENGTH_SHORT).show();
            dbHelper.markVotingAsVoted(votingId);
        }, error -> {
            Toast.makeText(GolosActivity.this, "Ошибка при голосовании", Toast.LENGTH_SHORT).show();
        });
    }

    private void addAnswerField() {
        final EditText answerInput = new EditText(this);
        answerInput.setHint("Вариант ответа");
        answersContainer.addView(answerInput);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    calendar.set(year1, monthOfYear, dayOfMonth, 23, 59);
                    endDate = calendar.getTime();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy, HH:mm");
                    endDateText.setText("Дата окончания: " + dateFormat.format(endDate));
                }, year, month, day);
        datePickerDialog.show();
    }


    private void createVoting() {
        String title = titleInput.getText().toString();
        String description = descriptionInput.getText().toString();

        answerOptions.clear();
        for (int i = 0; i < answersContainer.getChildCount(); i++) {
            EditText answerInput = (EditText) answersContainer.getChildAt(i);
            String answerText = answerInput.getText().toString();
            if (!answerText.isEmpty()) {
                answerOptions.add(answerText);
            }
        }

        if (title.isEmpty() || description.isEmpty() || answerOptions.isEmpty() || endDate == null) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        APIClient.get("/getVotings", response -> {
            try {
                if (response.trim().startsWith("[")) {
                    JSONArray votings = new JSONArray(response);
                    long currentTime = System.currentTimeMillis();
                    for (int i = 0; i < votings.length(); i++) {
                        JSONObject voting = votings.getJSONObject(i);
                        String existingTitle = voting.optString("title");
                        long endDateMillis = voting.optLong("end_date");
                        if (existingTitle.equalsIgnoreCase(title) && endDateMillis > currentTime) {
                            Toast.makeText(GolosActivity.this, "Голосование с таким названием уже существует", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                } else {
                    JSONObject responseObject = new JSONObject(response);
                    if (responseObject.has("message")) {
                        String message = responseObject.getString("message");
                        System.out.println("Сообщение сервера: " + message);
                    }
                }

                JSONObject voteData = new JSONObject();
                voteData.put("title", title);
                voteData.put("description", description);
                voteData.put("answer_option", new JSONArray(answerOptions));
                voteData.put("end_date", endDate.getTime());

                APIClient.post("/createVoting", voteData, postResponse -> {
                    Toast.makeText(this, "Голосование создано", Toast.LENGTH_SHORT).show();
                    clearInputs();
                }, error -> {
                    Toast.makeText(this, "Ошибка при создании голосования", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Ошибка при обработке данных", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Toast.makeText(this, "Ошибка при загрузке голосований", Toast.LENGTH_SHORT).show();
        });
    }


    private void clearInputs() {
        titleInput.setText("");
        descriptionInput.setText("");
        answersContainer.removeAllViews();
        endDateText.setText("Дата окончания: не выбрана");
        answerOptions.clear();
    }
}
