package com.example.ex42.Fragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example.ex42.R;
import com.example.ex42.database.enity.User;
import com.example.ex42.util.HideStateBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivityFragment extends AppCompatActivity {

    List<Fragment> mFragmentList = new ArrayList<>();
    private ViewPager mViewPager;
    private BottomNavigationView mBottomNavigationView;
    private User mUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HideStateBar h1 = new HideStateBar();
        h1.hideStatusBar( this);
        setContentView(R.layout.activity_main_fragment);

        mUser = (User) getIntent().getSerializableExtra("user");

        initView();
        initData();
        initAdapter();
        initLinestener();
    }

    private void initLinestener() {
        //页面
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        mBottomNavigationView.setSelectedItemId(R.id.tab_news);
                        break;
                    case 1:
                        mBottomNavigationView.setSelectedItemId(R.id.tab_games);
                        break;
                    case 2:
                        mBottomNavigationView.setSelectedItemId(R.id.tab_myhome);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //图标选择监听
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.tab_news){
                    mViewPager.setCurrentItem(0);
                }else if (item.getItemId() == R.id.tab_games) {
                    mViewPager.setCurrentItem(1);
                }else if (item.getItemId() == R.id.tab_myhome) {
                    mViewPager.setCurrentItem(2);
                }
                return true;
            }
        });

    }

    private void initAdapter() {
        FragmentAdapter pagerAdapter = new FragmentAdapter(getSupportFragmentManager(),mFragmentList);
        mViewPager.setAdapter(pagerAdapter);
    }

    private void initData() {

        News_Fragment news_fragment = News_Fragment.newInstance("首页咨询","");
        mFragmentList.add(news_fragment);
        GameList_Fragment gameList_fragment = GameList_Fragment.newInstance("游戏","");
        Bundle  bundle = new Bundle();
        bundle.putSerializable("mUser",mUser);
        gameList_fragment.setArguments(bundle);
        mFragmentList.add(gameList_fragment);
        MyHome_Fragment myHome_fragment = MyHome_Fragment.newInstance("我的","");
        mFragmentList.add(myHome_fragment);
    }

    private void initView() {
        mViewPager = findViewById(R.id.viewpager);
        mBottomNavigationView = findViewById(R.id.BottomBar);
    }


}