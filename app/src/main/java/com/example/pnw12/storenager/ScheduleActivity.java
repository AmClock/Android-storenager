package com.example.pnw12.storenager;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.pnw12.storenager.VO.StoreUserVo;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;

public class ScheduleActivity extends AppCompatActivity {

    String time,kcal,menu;
    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();
    Cursor cursor;
    MaterialCalendarView materialCalendarView;

    String shot_Day;

    private ListView listview;
    private ListViewAdapter adapter;
    Button popUpBtn;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.custom_actionbar);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }



        setContentView(R.layout.activity_schedule);


        materialCalendarView = (MaterialCalendarView)findViewById(R.id.calendarView);


        FontAwesome fab = (FontAwesome)findViewById(R.id.btn_next);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScheduleActivity.this , MenuActivity.class);
                startActivity(intent);
                finish();
            }
        });

        adapter = new ListViewAdapter();
        listview = (ListView) findViewById(R.id.List_view);
        //어뎁터 할당
        listview.setAdapter(adapter);

        ConnectServer();

        String[] result = {"2018,11,11"};



        materialCalendarView.state().edit()
                .setFirstDayOfWeek(Calendar.SUNDAY)
                .setMinimumDate(CalendarDay.from(2017, 0, 1)) // 달력의 시작
                .setMaximumDate(CalendarDay.from(2030, 11, 31)) // 달력의 끝
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();

        materialCalendarView.addDecorators(
                new SundayDecorator(),
                new SaturdayDecorator(),
                oneDayDecorator);




        new ApiSimulator(result).executeOnExecutor(Executors.newSingleThreadExecutor());

        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                int Year = date.getYear();
                int Month = date.getMonth() + 1;
                int Day = date.getDay();

                Log.i("Year test", Year + "");
                Log.i("Month test", Month + "");
                Log.i("Day test", Day + "");

                shot_Day = Year + "," + Month + "," + Day;

                Log.i("shot_Day test", shot_Day + "");
                materialCalendarView.clearSelection();


                popUpBtn.setVisibility(View.VISIBLE);

            }
        });


        popUpBtn = (Button)findViewById(R.id.btn_test);
        popUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent( ScheduleActivity.this , PopupActivity.class);
                intent.putExtra("data", shot_Day);
                startActivityForResult(intent, 1);
            }
        });// click event

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ScheduleActivity.this , ScheduleActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }//onCreate end


    private class ApiSimulator extends AsyncTask<Void, Void, List<CalendarDay>> {

        String[] Time_Result;

        ApiSimulator(String[] Time_Result){
            this.Time_Result = Time_Result;
        }

        @Override
        protected List<CalendarDay> doInBackground(@NonNull Void... voids) {
            Log.d("test", "doInBackgroundtestest: "+Time_Result);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Calendar calendar = Calendar.getInstance();
            ArrayList<CalendarDay> dates = new ArrayList<>();

            /*특정날짜 달력에 점표시해주는곳*/
            /*월은 0이 1월 년,일은 그대로*/
            //string 문자열인 Time_Result 을 받아와서 ,를 기준으로짜르고 string을 int 로 변환
            for(int i = 0 ; i < Time_Result.length ; i ++){
                CalendarDay day = CalendarDay.from(calendar);
                String[] time = Time_Result[i].split(",");
                int year = Integer.parseInt(time[0]);
                int month = Integer.parseInt(time[1]);
                int dayy = Integer.parseInt(time[2]);

                dates.add(day);
                calendar.set(year,month-1,dayy);
            }


            return dates;
        }

        @Override
        protected void onPostExecute(@NonNull List<CalendarDay> calendarDays) {
            super.onPostExecute(calendarDays);
            if (isFinishing()) {
                return;
            }

            materialCalendarView.addDecorator(new EventDecorator(Color.parseColor("#D91D81"), calendarDays,ScheduleActivity.this));
        }
    }


    private void ConnectServer() {

        final String SIGNIN_URL = "http://{my ip address }/scheduleSelect.jsp";
        final String urlSuffix = "?userNO=" + StoreUserVo.userNo;

        Log.d("urlSuffix", urlSuffix);
        Log.d("test : ", SIGNIN_URL + urlSuffix);
        class SignupUser extends AsyncTask<String, Void, String> {


            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                /*Log.d(TAG,s);*/
                if (s != null)

                {
                    try {
                        JSONArray jArr = new JSONArray(s);

                        JSONObject json = new JSONObject();

                        for (int i = 0; i < jArr.length(); i++) {
                            json = jArr.getJSONObject(i);

                            String year = json.getString("scheduleYear") + "년 " +
                                    json.getString("scheduleMonth") + "월 " +
                                    json.getString("scheduleDate") + "일 ";
                            String test =  json.getString("scheduleText");
                            String no = json.getString("no");
                            adapter.addVO(year , test , no);
                            adapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else

                {
//                    Toast.makeText(FragmentA.this, "서버와의 통신에 문제가 발생했습니다", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            protected String doInBackground(String... params) {
                BufferedReader bufferedReader = null;

                try {

                    HttpClient client = new DefaultHttpClient();  // 보낼 객체 만들기
                    HttpPost post = new HttpPost(SIGNIN_URL + urlSuffix);  // 주소 뒤에 데이터를 넣기

                    HttpResponse response = client.execute(post); // 데이터 보내기

                    BufferedReader bufreader = new BufferedReader(
                            new InputStreamReader(
                                    response.getEntity().getContent(), "utf-8"));

                    String line = null;
                    String page = "";

                    while ((line = bufreader.readLine()) != null) {
                        page += line;
                    }
                    return page;
                } catch (Exception e) {
                    return null;
                }
            }
        }

        SignupUser su = new SignupUser();
        su.execute(urlSuffix);
    }//ConnectServer end*/


}//FragmentC end