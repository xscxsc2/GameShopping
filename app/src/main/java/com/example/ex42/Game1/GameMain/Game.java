package com.example.ex42.Game1.GameMain;

import static com.example.ex42.util.TimeUtil.parseSimpleToCharge;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.TypedArray;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.ex42.Battery.BaseHandlerCallBack;
import com.example.ex42.Battery.PowerConsumptionRankingsBatteryView;
import com.example.ex42.R;
import com.example.ex42.Service.MusicService;
import com.example.ex42.database.ShoppingDBHelper;
import com.example.ex42.database.enity.RankInfo;
import com.example.ex42.database.enity.User;
import com.example.ex42.util.HideStateBar;
import com.example.ex42.util.TimeUtil;
import com.example.ex42.util.ToastUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class NumItem{
    int xNum = 0;
    int oNum = 0;

    public int GetXNum(){
        return this.xNum;
    }

    public int GetONum(){
        return this.oNum;
    }

    public void SetXNum(int num){
        this.xNum += num;
    }

    public void SetONum(int num){
        this.oNum += num;
    }
}

public class Game extends AppCompatActivity implements BaseHandlerCallBack{
    private DrawerLayout dl_layout; // 声明一个抽屉布局对象
    private ListView lv_drawer_right; // 声明右侧菜单的列表视图对象
    private List<RankInfo> mRankInfos;
    private ShoppingDBHelper mDBHelper;
    private User mUser;
    private boolean start = false;
    private static String TAG = "Game";
    private String timeStr;
    private Chronometer chronometer;
    public static Game instance;
    private GridView game_GridButton;
    private TextView tv_pause ;
    private Button btn_pause;
    private List<NumItem> rowNum = new ArrayList<>();
    private List<NumItem> columnNum = new ArrayList<>();
    private List<Icon> iconList = new ArrayList<>();
    private OriginState originState = new OriginState();
    private boolean isGameWon = false;
    private long pauseOffset = 0;
    private boolean running = false;
    private long baseTime = 0;

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private PowerConsumptionRankingsBatteryView mPowerConsumptionRankingsBatteryView;
    private int power;

    private NoLeakHandler mHandler;
    private BroadcastReceiver mBatteryLevelReceiver;
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private MusicService musicService;
    private boolean isBound = false;
    private void initDBHelper() {
        mDBHelper = ShoppingDBHelper.getInstance(getApplicationContext());
        mDBHelper.openWriteLink();
        mDBHelper.openReadLink();
    }
    private ServiceConnection MusicServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };
    private Button mStartButton;
    private Button mPauseButton;

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(MusicServiceConnection);
            isBound = false;
        }
    }

    private Button mBegin_game;
    private TextView mGame_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HideStateBar h1 = new HideStateBar();
        h1.hideStatusBar(this);
        setContentView(R.layout.activity_game);

/////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////初始化排行榜/////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////

        initDBHelper();
        mUser = (User) getIntent().getSerializableExtra("user");
        mRankInfos = mDBHelper.queryAllRankInfo();
        for(RankInfo rankInfo:mRankInfos){
            Log.d("RankAllInfo", rankInfo.toString());
        }

        // 定义一个 Comparator 对象，用于按照 OverTime 属性从小到大排序
        Comparator<RankInfo> comparator = new Comparator<RankInfo>() {
            @Override
            public int compare(RankInfo o1, RankInfo o2) {
                long charge1 = parseSimpleToCharge(o1.getOverTime());
                long charge2 = parseSimpleToCharge(o2.getOverTime());
                return Long.compare(charge1, charge2);
            }
        };

        // 对 mRankInfos 列表进行排序
        Collections.sort(mRankInfos, comparator);
        for(RankInfo rankInfo:mRankInfos){
            Log.d("RankAllInfo", rankInfo.toString());
        }

        // 从布局文件中获取名叫dl_layout的抽屉布局
        dl_layout = findViewById(R.id.dl_layout);
        // 给抽屉布局设置侧滑监听器
        dl_layout.addDrawerListener(new SlidingListener());
        initListDrawer(); // 初始化侧滑的菜单列表


        mHandler = new NoLeakHandler(this);
        mPowerConsumptionRankingsBatteryView = (PowerConsumptionRankingsBatteryView) findViewById(R.id.mPowerConsumptionRankingsBatteryView);

        mBatteryLevelReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                mPowerConsumptionRankingsBatteryView.setLevelHeight(batteryLevel);
                if (batteryLevel < 30) {
                    mPowerConsumptionRankingsBatteryView.setLowerPower();
                } else if (batteryLevel < 60) {
                    mPowerConsumptionRankingsBatteryView.setOffline();
                } else {
                    mPowerConsumptionRankingsBatteryView.setOnline();
                }
            }
        };

        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBatteryLevelReceiver, batteryLevelFilter);

        mHandler.sendEmptyMessage(0);

/////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////初始化排行榜/////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////

        Button quitButton = findViewById(R.id.Game_Quit_Button);
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isBound) {
                    musicService.stop();
                }
                Game.this.finish();
            }
        });

        tv_pause = findViewById(R.id.tv_pause);

        //初始化GridView数据
        InitData();
        IconAdapter iconAdapter = new IconAdapter(this, R.layout.item_grid_icon, iconList);
        game_GridButton = findViewById(R.id.Game_GridButton);
        game_GridButton.setAdapter(iconAdapter);

        game_GridButton.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Icon icon = iconList.get(i);
                if(icon.GetCanClick()){
                    ImageView imageView = view.findViewById(R.id.item_img);

                    switch ((String)imageView.getTag()){
                        case "itemImage_null":
                            imageView.setImageResource(R.drawable.itemimage_x);
                            icon.SetIconId(1);
                            imageView.setTag("itemImage_x");
                            UpdateUI(i, 0);
                            break;
                        case "itemImage_x":
                            imageView.setImageResource(R.drawable.itemimage_o);
                            imageView.setTag("itemImage_o");
                            icon.SetIconId(2);
                            UpdateUI(i, 1);
                            break;
                        case "itemImage_o":
                            imageView.setImageResource(R.drawable.itemimage_null);
                            imageView.setTag("itemImage_null");
                            icon.SetIconId(0);
                            UpdateUI(i, 2);
                            break;
                    }

                    if (start == false){
                        start = true;
                        mStartButton.performClick();
                    }
                }

                boolean isWin = IsWin(i);
//                boolean isWin = true;
                if(isWin){
                    //执行游戏结束函数
                    FinshGame();
                }
            }
        });

        initChoronometer();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, MusicServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销广播接收器
//        unregisterReceiver(mTimerReceiver);
        mHandler.removeCallbacksAndMessages(null);
        unregisterReceiver(mBatteryLevelReceiver);
    }

    public boolean IsWin(int position){
        int point = 0;
        int[][] answer = originState.GetAnswer();

        for(int i = 0; i < 8; i ++){
            for(int j = 0; j < 8; j ++){
                if(iconList.get(point).GetIconId() != answer[i][j])
                    return false;
                point += 1;
            }
        }

        return true;
    }

    //游戏结束时，需要执行的函数
    void FinshGame(){
        mPauseButton.performClick();

        Chronometer chronometer = findViewById(R.id.chronometer);
        long elapsedMillis = SystemClock.elapsedRealtime() - chronometer.getBase();
        String time = TimeUtil.chargeToString(elapsedMillis);
        Log.d(TAG, time);
        RankInfo rankInfo = new RankInfo();
        rankInfo.account = mUser.account;
        rankInfo.OverTime = time;

        new Thread(new Runnable() {
            @Override
            public void run() {
                mDBHelper.insertRankInfo(rankInfo);
            }
        }).start();

        AlertDialog.Builder builder = new AlertDialog.Builder(Game.this);
        builder.setMessage("恭喜，你通关了，成绩已记录排行榜，即将关闭此页面")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Game.this.finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

        Log.d("Rank_InFo", rankInfo.toString());
    }

    private void initListDrawer() {
        // 下面初始化右侧菜单的列表视图
        lv_drawer_right = findViewById(R.id.lv_drawer_right);
        RankAdapter adapter = new RankAdapter(this, mRankInfos);


        lv_drawer_right.setAdapter(adapter);
        lv_drawer_right.setOnItemClickListener(new RightListListener());

    }

    // 定义一个右侧菜单列表的点击监听器
    private class RightListListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            dl_layout.closeDrawers(); // 关闭所有抽屉
        }
    }

    // 定义一个抽屉布局的侧滑监听器
    private class SlidingListener implements DrawerLayout.DrawerListener {
        // 在拉出抽屉的过程中触发
        public void onDrawerSlide(View drawerView, float slideOffset) {}

        // 在侧滑抽屉打开后触发
        public void onDrawerOpened(View drawerView) { }

        // 在侧滑抽屉关闭后触发
        public void onDrawerClosed(View drawerView) {

        }

        // 在侧滑状态变更时触发
        public void onDrawerStateChanged(int paramInt) {}
    }

    private void initChoronometer() {
        chronometer = findViewById(R.id.chronometer);
        chronometer.setFormat("%s");

        mStartButton = findViewById(R.id.start_button);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    musicService.play();
                }
                if (!running) {
                    chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset + baseTime);
                    chronometer.start();
                    running = true;
                    JgRunning();
                }
            }
        });

        mPauseButton = findViewById(R.id.pause_button);
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    musicService.pause();
                }
                if (running) {
                    chronometer.stop();
                    pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
                    running = false;
                    JgRunning();
                }
            }
        });
    }


    public void InitData(){
        TypedArray images = getResources().obtainTypedArray(R.array.images);
        int[][] intArray = originState.GetState();

        for(int i = 0; i < 8; i ++){
            NumItem rowItem = new NumItem();
            NumItem columnItem = new NumItem();

            rowNum.add(rowItem);
            columnNum.add(columnItem);
        }

        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++)
            {
                Icon icon = new Icon();

                //TODO:Set ImageId
                icon.SetIconId(intArray[i][j]);
                icon.SetImageId(images.getResourceId(intArray[i][j], 0));

                if(intArray[i][j] != 0){
                    icon.SetIsRight(true);
                    icon.SetCanClick(false);

                    switch (intArray[i][j]){
                        case 1:
                            rowNum.get(i).SetXNum(1);
                            columnNum.get(j).SetXNum(1);
                            break;
                        case 2:
                            rowNum.get(i).SetONum(1);
                            columnNum.get(j).SetONum(1);
                            break;
                    }
                }
                else{
                    icon.SetCanClick(true);
                }
                iconList.add(icon);
            }
        }

        UpdateAllItem();
//        Log.d("GridView", "数组长度为" + String.valueOf(iconList.size()));
    }

    //更新各个组件的显示
    private void UpdateUI(int position, int system){

        int row = position / 8;
        int column = position % 8;

        //更新行列数据
        switch (system){
            case 0:
                //从Null变成X，需要将X数量+1
                rowNum.get(row).SetXNum(1);
                columnNum.get(column).SetXNum(1);
                break;
            case 1:
                //从X变成O，需要将X数量-1，O数量+1
                rowNum.get(row).SetXNum(-1);
                columnNum.get(column).SetXNum(-1);
                rowNum.get(row).SetONum(1);
                columnNum.get(column).SetONum(1);
                break;
            case 2:
                //从O变成Null,需要将O数量-1
                rowNum.get(row).SetONum(-1);
                columnNum.get(column).SetONum(-1);
                break;
        }

        UpdateOneItem(row);
        UpdateOneItem(column);
    }

    private void UpdateAllItem(){
        for(int i = 0; i < 8; i ++){
            UpdateOneItem(i);
        }
    }
    private void UpdateOneItem(int i){
        NumItem rowItem = rowNum.get(i);
        NumItem columnItem = columnNum.get(i);
        switch (i){
            case 0:
                SetText(findViewById(R.id.Left_TextView1), String.valueOf(rowItem.xNum) + "/" + String.valueOf(rowItem.oNum));
                SetText(findViewById(R.id.Up_TextView1), String.valueOf(columnItem.xNum) + "/" + String.valueOf(columnItem.oNum));
                break;
            case 1:
                SetText(findViewById(R.id.Left_TextView2), String.valueOf(rowItem.xNum) + "/" + String.valueOf(rowItem.oNum));
                SetText(findViewById(R.id.Up_TextView2), String.valueOf(columnItem.xNum) + "/" + String.valueOf(columnItem.oNum));
                break;
            case 2:
                SetText(findViewById(R.id.Left_TextView3), String.valueOf(rowItem.xNum) + "/" + String.valueOf(rowItem.oNum));
                SetText(findViewById(R.id.Up_TextView3), String.valueOf(columnItem.xNum) + "/" + String.valueOf(columnItem.oNum));
                break;
            case 3:
                SetText(findViewById(R.id.Left_TextView4), String.valueOf(rowItem.xNum) + "/" + String.valueOf(rowItem.oNum));
                SetText(findViewById(R.id.Up_TextView4), String.valueOf(columnItem.xNum) + "/" + String.valueOf(columnItem.oNum));
                break;
            case 4:
                SetText(findViewById(R.id.Left_TextView5), String.valueOf(rowItem.xNum) + "/" + String.valueOf(rowItem.oNum));
                SetText(findViewById(R.id.Up_TextView5), String.valueOf(columnItem.xNum) + "/" + String.valueOf(columnItem.oNum));
                break;
            case 5:
                SetText(findViewById(R.id.Left_TextView6), String.valueOf(rowItem.xNum) + "/" + String.valueOf(rowItem.oNum));
                SetText(findViewById(R.id.Up_TextView6), String.valueOf(columnItem.xNum) + "/" + String.valueOf(columnItem.oNum));
                break;
            case 6:
                SetText(findViewById(R.id.Left_TextView7), String.valueOf(rowItem.xNum) + "/" + String.valueOf(rowItem.oNum));
                SetText(findViewById(R.id.Up_TextView7), String.valueOf(columnItem.xNum) + "/" + String.valueOf(columnItem.oNum));
                break;
            case 7:
                SetText(findViewById(R.id.Left_TextView8), String.valueOf(rowItem.xNum) + "/" + String.valueOf(rowItem.oNum));
                SetText(findViewById(R.id.Up_TextView8), String.valueOf(columnItem.xNum) + "/" + String.valueOf(columnItem.oNum));
                break;
        }
    }

    private void SetText(TextView textView, String text){
        textView.setText(text);
    }

    public void JgRunning(){
        if (running == true){
            tv_pause.setText("游戏中");
        } else if (running == false) {
            tv_pause.setText("暂停中");
        }
    }

    @Override
    public void callBack(Message msg) {
        switch (msg.what) {
            case 0:
                mHandler.sendEmptyMessageDelayed(0, 1000); // update every second
                break;
            default:
                break;
        }
    }

    private static class NoLeakHandler<T extends BaseHandlerCallBack> extends Handler {
        private WeakReference<T> wr;

        public NoLeakHandler(T t) {
            wr = new WeakReference<>(t);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            T t = wr.get();
            if (t != null) {
                t.callBack(msg);
            }
        }
    }
}