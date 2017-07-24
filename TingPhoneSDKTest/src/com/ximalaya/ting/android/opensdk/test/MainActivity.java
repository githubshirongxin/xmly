package com.ximalaya.ting.android.opensdk.test;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ccbjb.ximalayasis.R;
import com.ximalaya.ting.android.opensdk.auth.call.IXmlyAuthListener;
import com.ximalaya.ting.android.opensdk.player.service.IXmPlayerStatusListener;
import com.ximalaya.ting.android.opensdk.auth.constants.XmlyConstants;
import com.ximalaya.ting.android.opensdk.auth.exception.XmlyException;
import com.ximalaya.ting.android.opensdk.auth.handler.XmlySsoHandler;
import com.ximalaya.ting.android.opensdk.auth.model.XmlyAuth2AccessToken;
import com.ximalaya.ting.android.opensdk.auth.model.XmlyAuthInfo;
import com.ximalaya.ting.android.opensdk.auth.utils.AccessTokenKeeper;
import com.ximalaya.ting.android.opensdk.datatrasfer.AccessTokenManager;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.httputil.XimalayaException;
import com.ximalaya.ting.android.opensdk.model.category.CategoryList;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.ximalaya.ting.android.opensdk.player.appnotification.XmNotificationCreater;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;
import com.ximalaya.ting.android.opensdk.test.fragment.AlbumListFragment;
import com.ximalaya.ting.android.opensdk.test.fragment.PayTrackFragment;
import com.ximalaya.ting.android.opensdk.test.fragment.RadiosFragment;
import com.ximalaya.ting.android.opensdk.test.fragment.ScheduleFragment;
import com.ximalaya.ting.android.opensdk.test.fragment.TracksFragment;
import com.ximalaya.ting.android.opensdk.test.fragment.base.BaseFragment;
import com.ximalaya.ting.android.sdkdownloader.XmDownloadManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/18.
 */

public class MainActivity extends FragmentActivity {

    private static final String[] CONTENT = new String[]{"点播", "直播", "节目表" ,"付费","专辑" };
    private String mAppSecret = "ef86efc71380a07701ecd7db5ae4250a";
    private static final String TAG = "MainFragmentActivity";

    private TextView mTextView;
    private ImageButton mBtnPreSound;
    private ImageButton mBtnPlay;
    private ImageButton mBtnNextSound;
    private SeekBar mSeekBar;
    private ImageView mSoundCover;
    private ProgressBar mProgress;

    private ViewPager mViewPager;
    private PagerTabStrip mIndicator;
    private PagerAdapter mAdapter;

    private Context mContext;

    private XmPlayerManager mPlayerManager;
    private CommonRequest mXimalaya;

    private boolean mUpdateProgress = true;

    private RadiosFragment mRadiosFragment;
    private ScheduleFragment mScheduleFragment;
    private AlbumListFragment mAlbumListFragment;
    private PayTrackFragment mPayTrackFragment;
    private BaseFragment mCurrFragment;

    // 当前 DEMO 应用的回调页，第三方应用应该使用自己的回调页。
    public static final String REDIRECT_URL =  "https://api.ximalaya.com/openapi-collector-app/get_access_token";

    //显示认证后的 AccessToken
    private TextView mTvAccessToken;

    // 显示认证后的 AccessToken 过期时间
    private TextView mTvAccessTokenExpireTime;

    // 显示认证后的 AccessToken 有效或无效提示信息
    private TextView mTvAccessTokenMemo;

    //喜马拉雅授权实体类对象
    private XmlyAuthInfo mAuthInfo;

    // 喜马拉雅授权管理类对象
    private XmlySsoHandler mSsoHandler;

    //封装了 "access_token"，"refresh_token"，并提供了他们的管理功能
    private XmlyAuth2AccessToken mAccessToken;

    private TracksFragment mTracksFragment;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.mainlayout);
        //实例化WebView对象
        //webview = new WebView(this);
        //加载需要显示的网页
        //webview.loadUrl("http://www.baidu.com/");
        //设置Web视图
        //setContentView(webview);

            // 是否使用防劫持方案
            //XmPlayerConfig.getInstance(this).usePreventHijack(false);
            mXimalaya = CommonRequest.getInstanse();

            mXimalaya.init(this ,mAppSecret);

            mPlayerManager = XmPlayerManager.getInstance(mContext);
            Notification mNotification = XmNotificationCreater.getInstanse(this).initNotification(this.getApplicationContext(), MainFragmentActivity.class);

            mPlayerManager.init((int) System.currentTimeMillis(), mNotification);
            mPlayerManager.setOnConnectedListerner(new XmPlayerManager.IConnectListener() {
                @Override
                public void onConnected() {
            mXimalaya.setDefaultPagesize(50);
            mPlayerManager.setPlayMode(XmPlayListControl.PlayMode.PLAY_MODEL_LIST_LOOP);
                }
            });

            // 此代码表示播放时会去监测下是否已经下载
            XmPlayerManager.getInstance(this).setCommonBusinessHandle(XmDownloadManager.getInstance());

            Toast.makeText(MainActivity.this, "" + AccessTokenManager.getInstanse().getUid(), Toast.LENGTH_SHORT).show();

            Map<String, String> map = new HashMap<String, String>();
            CommonRequest.getCategories(map, new IDataCallBack<CategoryList>() {
                @Override
                public void onSuccess(CategoryList object) {
                    System.out.println("object = [" + object + "]");
                }

                @Override
                public void onError(int code, String message) {
                    System.out.println("code = [" + code + "], message = [" + message + "]");
                }
            });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setTitle("喜马拉雅ASIS");
        String state = "";

        try {
            mAuthInfo = new XmlyAuthInfo(this, CommonRequest.getInstanse().getAppKey(), REDIRECT_URL, state);
        } catch (XimalayaException e) {
            e.printStackTrace();
        }
        mSsoHandler = new XmlySsoHandler(this, mAuthInfo);

        initView();
    }

    private void initView() {
        mTvAccessToken = (TextView) findViewById(R.id.tv_access_token);
        mTvAccessTokenExpireTime = (TextView) findViewById(R.id.tv_access_token_expire_time);
        mTvAccessTokenMemo = (TextView) findViewById(R.id.tv_access_token_memo);

        // 演示客户端 + Web 授权，如果手机安装了微博客户端则使用客户端授权，没有则进行网页授权，第三方应用集成时应采用该方式
        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSsoHandler.authorize(new CustomAuthListener());
            }
        });


        // 用户退出登录，清空喜马拉雅授权 SDK 中保存的 access token 信息
        findViewById(R.id.btn_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (mAccessToken != null && mAccessToken.isSessionValid()) {
//                    AccessTokenKeeper.clear(MainActivity.this);
//                    mAccessToken = new XmlyAuth2AccessToken();
//                } else {
//                    Toast.makeText(MainActivity.this, getResources().getString(R.string.tip_has_not_login), Toast.LENGTH_SHORT).show();
//                }
//                mTvAccessTokenMemo.setText(getResources().getString(R.string.tip_access_token_is_invalid));
//                mTvAccessToken.setText("");
//                mTvAccessTokenExpireTime.setText("");
                mViewPager = (ViewPager) findViewById(R.id.pager);
                mViewPager.setOffscreenPageLimit(2);
                mAdapter = new MainActivity.SlidingPagerAdapter(getSupportFragmentManager());
                mViewPager.setAdapter(mAdapter);


                mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageSelected(int arg0) {
                        mCurrFragment = mTracksFragment;
                    }

                    @Override
                    public void onPageScrolled(int arg0, float arg1, int arg2) {
                        System.out.println("淡定  ===  ");
                    }

                    @Override
                    public void onPageScrollStateChanged(int arg0) {

                    }
                });
            }
        });

        final XmlyAuth2AccessToken accessToken = AccessTokenKeeper.readAccessToken (this);
        if (accessToken.isSessionValid()) {
            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            final String date = sdf.format(new Date(accessToken.getExpiresAt()));
            // 显示 access token 信息
            mTvAccessTokenMemo.setText(getResources().getString(R.string.tip_access_token_is_valid));
            mTvAccessToken.setText(getResources().getString(R.string.tip_access_token) + accessToken.toString());
            mTvAccessTokenExpireTime.setText(getResources().getString(R.string.tip_access_token_expires_at) + (!TextUtils.isEmpty(date) ? date : ""));
        }
    }

    // 客户端认证授权回调
    // 非常重要：发起客户端认证授权的 Activity 必须重写 onActivityResults
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    /**
     * 喜马拉雅认证授权回调类。
     * 1. 客户端授权时，需要在 {@link #onActivityResult} 中调用 {@link XmlySsoHandler#authorizeCallBack} 后，
     * 该回调才会被执行。
     * 2. 非 SSO 授权时，当授权结束后，该回调就会被执行。
     * <p>
     * 注意：当认证授权成功时，喜马拉雅 OAuthSDK 中已经保存了 access_token uid 等信息，
     * 需要使用时调用 {@link AccessTokenKeeper#readAccessToken(Context)}。
     */
    class CustomAuthListener implements IXmlyAuthListener {

        // 当认证授权成功时，回调该方法
        @Override
        public void onComplete(Bundle bundle) {
            // 从 Bundle 中解析 access token
            mAccessToken = XmlyAuth2AccessToken.parseAccessToken(bundle);
            if (mAccessToken.isSessionValid()) {
                /**
                 * 关键!!!!!!!!!!
                 * 结果返回之后将取回的结果设置到token管理器中
                 */
                AccessTokenManager.getInstanse().setAccessTokenAndUid(mAccessToken.getToken() , mAccessToken.getExpiresAt() , mAccessToken.getUid());

                DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                final String date = sdf.format(new Date(mAccessToken.getExpiresAt()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 显示 access token 信息
                        mTvAccessTokenMemo.setText(getResources().getString(R.string.tip_access_token_is_valid));
                        mTvAccessToken.setText(getResources().getString(R.string.tip_access_token) + mAccessToken.toString());
                        mTvAccessTokenExpireTime.setText(getResources().getString(R.string.tip_access_token_expires_at) + (!TextUtils.isEmpty(date) ? date : ""));
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 显示 access token 信息
                        mTvAccessTokenMemo.setText(getResources().getString(R.string.tip_access_token_is_invalid));
                        mTvAccessToken.setText("");
                        mTvAccessTokenExpireTime.setText("");
                    }
                });
            }
        }

        // 当授权过程中发生异常（如回调地址无效等信息等）时，回调该方法
        @Override
        public void onXmlyException(final XmlyException e) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 请查看“喜马拉雅Android平台OAuth2SDK文档”，查看错误编号对应的详细错误信息
                    mTvAccessTokenMemo.setText(getResources().getString(R.string.tip_auth_failure) + e.getMessage());
                    mTvAccessToken.setText("");
                    mTvAccessTokenExpireTime.setText("");
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // 当用户主动取消授权时，回调该方法
        @Override
        public void onCancel() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.tip_auth_cancel), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    class SlidingPagerAdapter extends FragmentPagerAdapter {
        public SlidingPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment f = null;
                if (mTracksFragment == null) {
                    mTracksFragment = new TracksFragment();
                }
                f = mTracksFragment;
            return f;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return CONTENT[position % CONTENT.length];
        }

        @Override
        public int getCount() {
            return CONTENT.length;
        }
    }



}
