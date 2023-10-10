package com.example.ex42.Fragment;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.NetworkOnMainThreadException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.ex42.InternetGameNews.BaseResponse;
import com.example.ex42.InternetGameNews.Constants;
import com.example.ex42.InternetGameNews.DetailActivity;
import com.example.ex42.InternetGameNews.Maintivitytest;
import com.example.ex42.InternetGameNews.News;
import com.example.ex42.InternetGameNews.NewsAdapter;
import com.example.ex42.InternetGameNews.NewsRequest;
import com.example.ex42.R;
import com.example.ex42.util.HideStateBar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link News_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class News_Fragment extends Fragment {

    View rootView;
    private ListView lvNewsList;
    private List<News> newsData;
    private NewsAdapter adapter;
    private int page = 1;
    private int mCurrentColIndex = 0;
    private int[] mCols = new int[]{Constants.NEWS_COL5,
            Constants.NEWS_COL7,
            Constants.NEWS_COL8,
            Constants.NEWS_COL10,
            Constants.NEWS_COL11};

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public News_Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment News_Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static News_Fragment newInstance(String param1, String param2) {
        News_Fragment fragment = new News_Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private void initData() {
        newsData = new ArrayList<>();
        adapter = new NewsAdapter(this.getContext() ,
                R.layout.list_item , newsData);
        lvNewsList.setAdapter(adapter);
        refreshData(1);
    }

    private void refreshData(final int page) {
        new Thread(new Runnable(){

            @Override
            public void run() {
                NewsRequest requestObj = new NewsRequest();
                requestObj.setCol(mCols[mCurrentColIndex]);
                requestObj.setNum(Constants.NEWS_NUM);
                requestObj.setPage(page);
                String urlParams = requestObj.toString();
                Request request = new Request.Builder()
                        .url(Constants.GENERAL_NEWS_URL + urlParams)
                        .get().build();
                try {
                    OkHttpClient client = new OkHttpClient();
                    client.newCall(request).enqueue(callback);
                }catch (NetworkOnMainThreadException ex){
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    private void initView() {
        lvNewsList = rootView.findViewById(R.id.lv_news_list);
        lvNewsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getContext(), DetailActivity.class);
                News news = adapter.getItem(i);
                intent.putExtra(Constants.NEWS_DETAIL_URL_KEY, news.getContentUrl());
                intent.putExtra("news",news);
                startActivity(intent);
            }
        });

    }


    private okhttp3.Callback callback = new okhttp3.Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.e(TAG, "Failed to connect server!");
            Log.e("999999999",Log.getStackTraceString(e));
            e.printStackTrace();
        }

        @Override
        public void onResponse(Call call, Response response)
                throws IOException {
            if (response.isSuccessful()) {
                final String body = response.body().string();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Gson gson = new Gson();
                        Type jsonType = new TypeToken<BaseResponse<List<News>>>() {
                        }.getType();
                        BaseResponse<List<News>> newsListResponse = gson.fromJson(body, jsonType);
                        for (News news : newsListResponse.getData()) {
                            adapter.add(news);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
            } else {
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null){
            rootView = inflater.inflate(R.layout.fragment_news_, container, false);
        }
//        initView();
//        initData();
        return rootView;
    }

}