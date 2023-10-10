package com.example.ex42;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.ex42.Fragment.MainActivityFragment;
import com.example.ex42.OverWrite.ListViewActivity;
import com.example.ex42.database.ShoppingDBHelper;
import com.example.ex42.database.enity.User;
import com.example.ex42.util.HideStateBar;
import com.example.ex42.util.ToastUtil;

public class MainActivity extends AppCompatActivity  {
    ToggleButton showPasswordToggleButton ;
    private ImageView captchaImageView;
    private TextView et_Main_account;
    private EditText et_password;
    private Button btn_login;
    private TextView tv_forgetPassword;
    private TextView tv_NewUserRegister;
    private ShoppingDBHelper mDBHelper;
    private ActivityResultLauncher launcher;
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HideStateBar h1 = new HideStateBar();
        h1.hideStatusBar(this);
        setContentView(R.layout.activity_main);

        initView();
        initLisnter();

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            //监听其他Activity结束，如果结束信息为RESULT_OK,则执行如下函数
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == Activity.RESULT_OK){
                    String userName = result.getData().getStringExtra("userName");
                    String userPassword = result.getData().getStringExtra("userPassword");

                    //判断是否已经成功读取数据
                    if(!userName.equals("") && !userPassword.equals("")){
                        et_Main_account.setText(userName);
                        et_password.setText(userPassword);
                        Log.d("MainActivity", "修改参数成功！");
                    }
                    else{
                        Log.d("MainActivity", "修改参数失败！");
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    protected void onStart() {
        super.onStart();
        initDBHelper();
    }

    private void initDBHelper() {
        mDBHelper = ShoppingDBHelper.getInstance(getApplicationContext());
        mDBHelper.openWriteLink();
        mDBHelper.openReadLink();
    }

    private void initLisnter() {
        tv_forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ForgotPasswordActivity.class);
                startActivity(intent);
//                finish();
            }
        });
        tv_NewUserRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,RegisterActivity.class);
                launcher.launch(intent);
            }
        });
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_Main_account.getText().toString().equals("") || et_password.getText().toString().equals("")){
                    ToastUtil.show(MainActivity.this,"请输入正确信息");
                }else {
                    Login();
                }

            }
        });
        showPasswordToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // 显示密码
                    et_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    // 隐藏密码
                    et_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
    }

    private void Login() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                User user = mDBHelper.queryByAccount(et_Main_account.getText().toString());//查出的用户
                if (user == null){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.show(MainActivity.this,"不存在该用户");
                        }
                    });
                }else {
                    String InputPassword = et_password.getText().toString();
                    if (InputPassword.equals(user.password)){
                        Intent intent = new Intent(MainActivity.this, MainActivityFragment.class);
                        intent.putExtra("user", user);
                        startActivity(intent);
                    }else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.show(MainActivity.this,"密码错误");
                            }
                        });
                    }
                }
            }
        }).start();
    }

    private void initView() {
        tv_forgetPassword = findViewById(R.id.tv_forgetPassword);
        tv_NewUserRegister = findViewById(R.id.tv_NewUserRegister);
        btn_login = findViewById(R.id.btn_login);
        et_Main_account = findViewById(R.id.et_Main_account);
        et_password = findViewById(R.id.et_password);
        showPasswordToggleButton = findViewById(R.id.showPasswordToggleButton);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDBHelper.closeLink();
    }
}