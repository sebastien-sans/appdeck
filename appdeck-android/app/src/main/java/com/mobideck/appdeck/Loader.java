package com.mobideck.appdeck;

import com.crashlytics.android.Crashlytics;

import hotchemi.android.rate.AppRate;
import hotchemi.android.rate.OnClickButtonListener;
import io.fabric.sdk.android.Fabric;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.littleshoot.proxy.ChainedProxy;
import org.littleshoot.proxy.ChainedProxyAdapter;
import org.littleshoot.proxy.ChainedProxyManager;
import org.littleshoot.proxy.HttpProxyServerBootstrap;
import org.littleshoot.proxy.TransportProtocol;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.gc.materialdesign.views.ProgressBarDeterminate;
import com.gc.materialdesign.views.ProgressBarIndeterminate;
import com.gc.materialdesign.views.ProgressBarIndeterminateDeterminate;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.facebook.FacebookSdk;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import io.netty.handler.codec.http.HttpRequest;

public class Loader extends AppCompatActivity {

	public final static String TAG = "LOADER";
	public final static String JSON_URL = "com.mobideck.appdeck.JSON_URL";
	
	public final static String POP_UP_URL = "com.mobideck.appdeck.POP_UP_URL";
	public final static String PAGE_URL = "com.mobideck.appdeck.URL";
	public final static String ROOT_PAGE_URL = "com.mobideck.appdeck.ROOT_URL";
	
	/* push */
	public final static String PUSH_URL = "com.mobideck.appdeck.PUSH_URL";
	public final static String PUSH_TITLE = "com.mobideck.appdeck.PUSH_TITLE";
    public final static String PUSH_IMAGE_URL = "com.mobideck.appdeck.PUSH_IMAGE_URL";
	
	public String proxyHost;
	public int proxyPort;
    String originalProxyHost = null;
    int originalProxyPort = -1;

    private String alternativeBootstrapURL = null;

    public AppDeckAdManager adManager;

	protected AppDeck appDeck;

	private SmartWebView leftMenuWebView;
	private SmartWebView rightMenuWebView;
	
    private DrawerLayout mDrawerLayout;
    private FrameLayout mDrawerLeftMenu;
    private FrameLayout mDrawerRightMenu;
    private ActionBarDrawerToggle mDrawerToggle;
    
	private PageMenuItem[] menuItems;

    public View nonVideoLayout;
    public ViewGroup videoLayout;

	private HttpProxyServerBootstrap proxyServerBootstrap;

    ProgressBarDeterminate mProgressBarDeterminate;
    ProgressBarIndeterminate mProgressBarIndeterminate;

    Toolbar mToolbar;

    private boolean historyInjected = false;
    public List<String> historyUrls = new ArrayList<String>();

    public boolean willShowActivity = false;

    CallbackManager callbackManager;
    TwitterAuthClient mTwitterAuthClient;

    public SmartWebViewInterface smartWebViewRegiteredForActivityResult = null;

	@SuppressWarnings("unused")
	private GoogleCloudMessagingHelper gcmHelper;
    private AppDeckBroadcastReceiver appDeckBroadcastReceiver;
    
    protected void onCreatePass(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    }
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {

        //Debug.startMethodTracing("calc");
		AppDeckApplication app = (AppDeckApplication) getApplication();


		Intent intent = getIntent();
        String app_json_url = intent.getStringExtra(JSON_URL);
        appDeck = new AppDeck(getBaseContext(), app_json_url);



        Crashlytics crashlytics = new Crashlytics.Builder().disabled(BuildConfig.DEBUG).build();
        if (appDeck.config.twitter_consumer_key != null && appDeck.config.twitter_consumer_secret != null &&
                appDeck.config.twitter_consumer_key.length() > 0 && appDeck.config.twitter_consumer_secret.length() > 0
                ) {
            TwitterAuthConfig authConfig = new TwitterAuthConfig(appDeck.config.twitter_consumer_key, appDeck.config.twitter_consumer_secret);
            Fabric.with(app, crashlytics, new Twitter(authConfig));
            mTwitterAuthClient = new TwitterAuthClient();
        } else {
            Fabric.with(app, crashlytics);
        }

        Log.d(TAG, "Use AppDeck version "+AppDeck.version);

        String action = intent.getAction();
        Uri data = intent.getData();

        if (app.isInitialLoading == false)
        {
            SmartWebViewFactory.setPreferences(this);
            app.isInitialLoading = true;
        }

    	super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        // original proxy host/port
        Proxy proxyConf = null;
        try {
            URI uri = URI.create("http://www.appdeck.mobi");
            Proxy currentProxy = Utils.getProxySelectorConfiguration(uri);
            originalProxyHost = Utils.getProxyHost(currentProxy);
            originalProxyPort = Utils.getProxyPort(currentProxy);
        } catch (Exception e) {
            e.printStackTrace();
        }

    	this.proxyHost = "127.0.0.1";
    	
    	boolean isAvailable = false;
    	this.proxyPort = 8081; // default port
    	do
    	{
    		isAvailable = Utils.isPortAvailable(this.proxyPort);
    		if (isAvailable == false)
    			this.proxyPort = Utils.randInt(10000, 60000);	
    	}
    	while (isAvailable == false);

        Log.i(TAG, "filter registered at @" + this.proxyPort);

        //enableProxy();

    	CacheFiltersSource filtersSource = new CacheFiltersSource();

        appDeck.cache.checkBeacon(this);

        proxyServerBootstrap = DefaultHttpProxyServer
                .bootstrap()
                .withPort(this.proxyPort)
                .withAllowLocalOnly(true)
                .withTransportProtocol(TransportProtocol.TCP)
                .withFiltersSource(filtersSource);

        if (originalProxyHost != null && originalProxyPort != -1)
        {
            proxyServerBootstrap.withChainProxyManager(new ChainedProxyManager() {
                @Override
                public void lookupChainedProxies(HttpRequest httpRequest, Queue<ChainedProxy> chainedProxies) {

                    chainedProxies.add(new ChainedProxyAdapter() {
                        @Override
                        public InetSocketAddress getChainedProxyAddress() {
                            try {
                                return new InetSocketAddress(InetAddress.getByName(Loader.this.originalProxyHost), Loader.this.originalProxyPort);
                            } catch (UnknownHostException uhe) {
                                throw new RuntimeException(
                                        "Unable to resolve "+Loader.this.originalProxyHost+"?!");
                            }
                        }

                    });

                };
            });
        }

        proxyServerBootstrap.start();

        setContentView(R.layout.loader);

        // for video support
        nonVideoLayout = (View)findViewById(R.id.loader_content); // Your own view, read class comments
        videoLayout = (ViewGroup)findViewById(R.id.videoLayout); // Your own view, read class comments

        mToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(mToolbar);

        mProgressBarDeterminate = (ProgressBarDeterminate)findViewById(R.id.progressBarDeterminate);
        mProgressBarDeterminate.setMin(0);
        mProgressBarDeterminate.setMax(100);
        mProgressBarIndeterminate = (ProgressBarIndeterminate)findViewById(R.id.progressBarIndeterminate);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.app_name, R.string.app_name) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (leftMenuWebView != null && drawerView == mDrawerLeftMenu)
                    leftMenuWebView.ctl.sendJsEvent("disappear", "null");
                if (rightMenuWebView != null && drawerView == mDrawerRightMenu)
                    rightMenuWebView.ctl.sendJsEvent("disappear", "null");
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (leftMenuWebView != null && drawerView == mDrawerLeftMenu)
                    leftMenuWebView.ctl.sendJsEvent("appear", "null");
                if (rightMenuWebView != null && drawerView == mDrawerRightMenu)
                    rightMenuWebView.ctl.sendJsEvent("appear", "null");
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);


        if (appDeck.config.leftMenuUrl != null) {
            leftMenuWebView = SmartWebViewFactory.createMenuSmartWebView(this, appDeck.config.leftMenuUrl.toString(), SmartWebViewFactory.POSITION_LEFT);

        	if (appDeck.config.leftmenu_background_color != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        		leftMenuWebView.view.setBackground(appDeck.config.leftmenu_background_color.getDrawable());
        	mDrawerLeftMenu = (FrameLayout) findViewById(R.id.left_drawer);
            mDrawerLeftMenu.post(new Runnable() {
                @Override
                public void run() {
                    Resources resources = getResources();
                    float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, appDeck.config.leftMenuWidth, resources.getDisplayMetrics());
                    DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) mDrawerLeftMenu.getLayoutParams();
                    params.width = (int) (width);
                    mDrawerLeftMenu.setLayoutParams(params);
                    mDrawerLeftMenu.addView(leftMenuWebView.view);
                }
            });

        } else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, findViewById(R.id.left_drawer));
        }
        
        if (appDeck.config.rightMenuUrl != null) {
            rightMenuWebView = SmartWebViewFactory.createMenuSmartWebView(this, appDeck.config.rightMenuUrl.toString(), SmartWebViewFactory.POSITION_RIGHT);
        	if (appDeck.config.rightmenu_background_color != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        		rightMenuWebView.view.setBackground(appDeck.config.rightmenu_background_color.getDrawable());
        	mDrawerRightMenu = (FrameLayout) findViewById(R.id.right_drawer);
            mDrawerRightMenu.post(new Runnable() {
                @Override
                public void run() {
                    Resources resources = getResources();
                    float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, appDeck.config.rightMenuWidth, resources.getDisplayMetrics());
                    DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) mDrawerRightMenu.getLayoutParams();
                    params.width = (int) (width);
                    mDrawerRightMenu.setLayoutParams(params);
                    mDrawerRightMenu.addView(rightMenuWebView.view);
                }
            });
        } else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, findViewById(R.id.right_drawer));
        }

        // configure action bar
        appDeck.actionBarHeight = getActionBarHeight();

        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(getResources().getColor(R.color.AppDeckColorTopBarText), PorterDuff.Mode.SRC_ATOP);
        mDrawerToggle.setHomeAsUpIndicator(upArrow);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // show icon on the left of logo
        getSupportActionBar().setDisplayShowHomeEnabled(true); // show logo
        getSupportActionBar().setHomeButtonEnabled(true); // ???

        // status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //getWindow().setStatusBarColor(getResources().getColor(R.color.AppDeckColorPrimary));
            //getWindow().setTitleColor(getResources().getColor(R.color.AppDeckColorPrimary));
        /*if (appDeck.config.icon_theme.equalsIgnoreCase("light"))
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_navigation_drawer_light);
        else
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_navigation_drawer);*/
        }
		if (appDeck.config.topbar_color != null)
			getSupportActionBar().setBackgroundDrawable(appDeck.config.topbar_color.getDrawable());

		if (appDeck.config.title != null)
			getSupportActionBar().setTitle(appDeck.config.title);
		
		setSupportProgressBarVisibility(false);
		setSupportProgressBarIndeterminate(false);


        /*if (appDeck.config.topbar_color != null)
            mProgressBar.setSmoothProgressDrawableBackgroundDrawable(appDeck.config.topbar_color.getDrawable());*/

        /*mProgressBar.setIndeterminateDrawable(new SmoothProgressDrawable.Builder(this)
                .color(0xff0000)
                .interpolator(new DecelerateInterpolator())
                .sectionsCount(4)
                .separatorLength(8)         //You should use Resources#getDimensionPixelSize
                .strokeWidth(8f)            //You should use Resources#getDimension
                .speed(2f)                 //2 times faster
                .progressiveStartSpeed(2)
                .progressiveStopSpeed(3.4f)
                .reversed(false)
                .mirrorMode(false)
                .progressiveStart(true)
                //.progressiveStopEndedListener(mListener) //called when the stop animation is over
                .build());*/


		getSupportFragmentManager().addOnBackStackChangedListener(new OnBackStackChangedListener() {
            public void onBackStackChanged() {
                AppDeckFragment fragment = getCurrentAppDeckFragment();

                if (fragment != null) {
                    fragment.setIsMain(true);
                }
            }
        });

        adManager = new AppDeckAdManager(this);
        adManager.showAds(AppDeckAdManager.EVENT_START);

		gcmHelper = new GoogleCloudMessagingHelper(getBaseContext());
        appDeckBroadcastReceiver = new AppDeckBroadcastReceiver(this);

        if (data != null)
        {
            alternativeBootstrapURL = data.toString();
            loadRootPage(alternativeBootstrapURL);
        }
		else if (savedInstanceState == null)
		{
			loadRootPage(appDeck.config.bootstrapUrl.toString());
		}

        AppRate.with(this)
                .setInstallDays(10) // default 10, 0 means install day.
                .setLaunchTimes(10) // default 10
                .setRemindInterval(1) // default 1
                .setShowLaterButton(true) // default true
                //.setDebug(true) // default false
                .setOnClickButtonListener(new OnClickButtonListener() { // callback listener.
                    @Override
                    public void onClickButton(int which) {
                        Log.d(Loader.class.getName()+" AppRater Click", Integer.toString(which));
                    }
                })
                .monitor();

        // Show a dialog if meets conditions
        AppRate.showRateDialogIfMeetsConditions(this);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerToggle != null)
            mDrawerToggle.syncState();
    }

    public FrameLayout getBannerAdViewContainer()
    {
        return (FrameLayout)findViewById(R.id.bannerContainer);
    }
    public FrameLayout getInterstitialAdViewContainer()
    {
        return (FrameLayout)findViewById(R.id.app_container);
    }

	boolean isForeground = true;
    @Override
    protected void onResume()
    {
    	super.onResume();
    	isForeground = true;
        if (willShowActivity == false)
            SmartWebViewFactory.onActivityResume(this);
        willShowActivity = false; // always set it to false
        IntentFilter filter = new IntentFilter("com.google.android.c2dm.intent.RECEIVE");
        filter.setPriority(1);
        appDeckBroadcastReceiver.loaderActivity = this;
        registerReceiver(appDeckBroadcastReceiver, filter);

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
        //enableProxy();
        if (adManager != null)
            adManager.onActivityResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    	isForeground = false;
        if (willShowActivity == false)
            SmartWebViewFactory.onActivityPause(this);
        try {
            appDeckBroadcastReceiver.clean();
            unregisterReceiver(appDeckBroadcastReceiver);
        } catch (Exception e) {

        }
    	if (appDeck.noCache)
    		Utils.killApp(true);
        //disableProxy();
        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
        if (adManager != null)
            adManager.onActivityPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
    	outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");    	
    	super.onSaveInstanceState(outState);
        SharedPreferences prefs = getSharedPreferences(AppDeckApplication.class.getSimpleName(), Context.MODE_PRIVATE);

        // only keep maxHistoryUrlsSize URLS
        int maxHistoryUrlsSize = 5;
        if (historyUrls.size() > maxHistoryUrlsSize)
            historyUrls = historyUrls.subList(historyUrls.size() - maxHistoryUrlsSize - 1, historyUrls.size() - 1);

        //Set<String> hs = prefs.getStringSet("set", new HashSet<String>());
        Set<String> in = new HashSet<String>(historyUrls);
        //in.add(String.valueOf(hs.size() + 1));
        prefs.edit().putStringSet("historyUrls", in).commit(); // brevity

        if (adManager != null)
            adManager.onActivitySaveInstanceState(outState);

        Log.i(TAG, "onSaveInstanceState");
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
      super.onRestoreInstanceState(savedInstanceState);
      if (adManager != null)
         adManager.onActivityRestoreInstanceState(savedInstanceState);
      Log.i(TAG, "onRestoreInstanceState");
    }
    
    @Override
    protected void onDestroy()
    {
    	super.onDestroy();
        isForeground = false;
        SmartWebViewFactory.onActivityDestroy(this);
    }    

    // Sliding Menu API
    
    public void toggleMenu()
    {
    	if (isMenuOpen())
    		closeMenu();
    	else
    		openMenu();
    }

    public void toggleLeftMenu()
    {
    	if (isMenuOpen())
    		closeMenu();
    	else
    		openLeftMenu();
    }

    public void toggleRightMenu()
    {
    	if (isMenuOpen())
    		closeMenu();
    	else
    		openRightMenu();
    }    
    
    public boolean isMenuOpen()
    {
    	if (mDrawerLayout == null)
    		return false;
    	if (mDrawerLeftMenu != null && mDrawerLayout.isDrawerOpen(mDrawerLeftMenu))
    		return true;
    	if (mDrawerRightMenu != null && mDrawerLayout.isDrawerOpen(mDrawerRightMenu))
    		return true;
    	return false;
    }
    
    public boolean isLeftMenuOpen()
    {
    	if (mDrawerLayout == null)
    		return false;
    	if (mDrawerLeftMenu != null && mDrawerLayout.isDrawerOpen(mDrawerLeftMenu))
    		return true;
    	return false;    	
    }

    public boolean isRightMenuOpen()
    {
    	if (mDrawerLayout == null)
    		return false;
    	if (mDrawerRightMenu != null && mDrawerLayout.isDrawerOpen(mDrawerRightMenu))
    		return true;
    	return false;
    }    
    
    public void openLeftMenu()
    {
    	closeMenu();
        if (menuEnabled == false)
            return;
    	if (mDrawerLayout == null)
    		return;
    	if (mDrawerLeftMenu != null)
			mDrawerLayout.openDrawer(mDrawerLeftMenu);
    }
    
    public void openRightMenu()
    {
    	closeMenu();
        if (menuEnabled == false)
            return;
    	if (mDrawerLayout == null)
    		return;
		if (mDrawerRightMenu != null)
			mDrawerLayout.openDrawer(mDrawerRightMenu);
    }

    public void openMenu()
    {
        closeMenu();
        if (menuEnabled == false)
            return;
    	if (mDrawerLayout == null)
    		return;
    	if (mDrawerLeftMenu != null)
    	{
			mDrawerLayout.openDrawer(mDrawerLeftMenu);
			return;
    	}
    	if (mDrawerRightMenu != null)
    	{
			mDrawerLayout.openDrawer(mDrawerRightMenu);
			return;
    	}
    }    
    
    public void closeMenu()
    {
       	if (mDrawerLayout == null)
       		return;
       	mDrawerLayout.closeDrawers();   	
    }

    private boolean menuEnabled = true;

    public void disableMenu()
    {
        menuEnabled = false;
    	closeMenu();
       	if (mDrawerLayout == null)
       		return;

        if (appDeck.config.leftMenuUrl != null)
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, findViewById(R.id.left_drawer));
        if (appDeck.config.rightMenuUrl != null)
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, findViewById(R.id.right_drawer));

       	//mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(false); // icon on the left of logo
        //getSupportActionBar().setDisplayShowHomeEnabled(false); // make icon + logo + title clickable

        getSupportActionBar().setDisplayHomeAsUpEnabled(false); // show icon on the left of logo
        getSupportActionBar().setDisplayShowHomeEnabled(true); // show logo
        getSupportActionBar().setHomeButtonEnabled(true); // ???



    }

    public void enableMenu()
    {
        menuEnabled = true;
       	if (mDrawerLayout == null)
       		return;
        if (appDeck.config.leftMenuUrl != null)
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, findViewById(R.id.left_drawer));
        if (appDeck.config.rightMenuUrl != null)
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, findViewById(R.id.right_drawer));

       	//mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // show icon on the left of logo
        getSupportActionBar().setDisplayShowHomeEnabled(true); // make icon + logo + title clickable
        /*if (appDeck.config.icon_theme.equalsIgnoreCase("light"))
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_navigation_drawer_light);
        else
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_navigation_drawer);*/
    }

    boolean menuArrowIsShown = false;
    public void setMenuArrow(boolean show)
    {
        if (menuArrowIsShown == show)
            return;
        menuArrowIsShown = show;
        float start = (show ? 0 : 1);
        float end = (show ? 1 : 0);
        ValueAnimator anim = ValueAnimator.ofFloat(start, end);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float slideOffset = (Float) valueAnimator.getAnimatedValue();
                mDrawerToggle.onDrawerSlide(mDrawerLayout, slideOffset);
            }
        });
        anim.setInterpolator(new DecelerateInterpolator());
        // You can change this duration to more closely match that of the default animation.
        anim.setDuration(500);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (menuArrowIsShown == false)
                    mDrawerToggle.setDrawerIndicatorEnabled(true);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (menuArrowIsShown)
                    mDrawerToggle.setDrawerIndicatorEnabled(false);
                else
                    mDrawerToggle.setDrawerIndicatorEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        anim.start();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    ArrayList<WeakReference<AppDeckFragment>> fragList = new ArrayList<WeakReference<AppDeckFragment>>();
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
    public void onAttachFragment (Fragment fragment) {
    	
    	if (fragment != null)
    	{
    		String tag = fragment.getTag();
    		if (tag != null && tag.equalsIgnoreCase("AppDeckFragment"))
    		{
    			fragList.add(new WeakReference(fragment));
    		}
    	}
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void onDettachFragment (Fragment fragment) {
    	ArrayList<WeakReference<AppDeckFragment>> newlist = new ArrayList<WeakReference<AppDeckFragment>>();    	
        for(WeakReference<AppDeckFragment> ref : fragList) {
        	AppDeckFragment f = ref.get();
            if (f != fragment) {
            	newlist.add(new WeakReference(f));
            }
        }    	
        fragList = newlist;
    }
    
    public ArrayList<AppDeckFragment> getActiveFragments() {
        ArrayList<AppDeckFragment> ret = new ArrayList<AppDeckFragment>();
        for(WeakReference<AppDeckFragment> ref : fragList) {
        	AppDeckFragment f = ref.get();
            if(f != null) {
                //if(f.isActive()) {
                    ret.add(f);
                //}
            }
        }
        return ret;
    }    

    public AppDeckFragment getPreviousAppDeckFragment(AppDeckFragment current)
    {
    	AppDeckFragment previous = null;

        for(WeakReference<AppDeckFragment> ref : fragList) {
        	AppDeckFragment f = ref.get();
        	if (f == current)
        		return previous;
        	previous = f;
        }

        return null;
    }
    
    
    public AppDeckFragment getCurrentAppDeckFragment()
    {
    	FragmentManager fragmentManager = getSupportFragmentManager();
    	return (AppDeckFragment)fragmentManager.findFragmentByTag("AppDeckFragment");
    }
    
    public AppDeckFragment getRootAppDeckFragment()
    {
    	WeakReference<AppDeckFragment> ref = fragList.get(0);
        return ref.get();
    }
    
    public void progressStart()
    {
        mProgressBarDeterminate.setVisibility(View.GONE);
        mProgressBarIndeterminate.setVisibility(View.VISIBLE);
        /*setSupportProgress(0);
        setSupportProgressBarVisibility(true);
        setSupportProgressBarIndeterminateVisibility(true);
    	setSupportProgressBarIndeterminate(true);*/
    }
    
    public void progressSet(int percent)
    {
        mProgressBarDeterminate.setVisibility(View.VISIBLE);
        mProgressBarIndeterminate.setVisibility(View.GONE);
        mProgressBarDeterminate.setProgress(percent);
/*

        if (percent < 25)
            return;
    	setSupportProgressBarIndeterminate(false);
        //int progress = (Window.PROGRESS_END - Window.PROGRESS_START) / 100 * percent;
        setSupportProgress(percent);*/
    }
    
    public void progressStop()
    {
        mProgressBarDeterminate.setVisibility(View.GONE);
        mProgressBarIndeterminate.setVisibility(View.GONE);
        mProgressBarDeterminate.setProgress(0);
        /*
        setSupportProgressBarVisibility(false);
        setSupportProgressBarIndeterminateVisibility(false);
    	setSupportProgressBarIndeterminate(false);
    	
        int progress = (Window.PROGRESS_END - Window.PROGRESS_START);
        progress = 100;
        setSupportProgress(progress);*/
        
    }
    
    protected void prepareRootPage()
    {
    	FragmentManager fragmentManager = getSupportFragmentManager();
    	fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE); 
    	
    	// remove all current menu items
    	setMenuItems(new PageMenuItem[0]);
    	
    	// make sure user see content
    	closeMenu();
    }
    
    public boolean loadSpecialURL(String absoluteURL)
    {
		if (absoluteURL.startsWith("tel:"))
		{
			try{
				Intent intent = new Intent(Intent.ACTION_DIAL);
				intent.setData(Uri.parse(absoluteURL));
				startActivity(intent);
			}catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
		
		if (absoluteURL.startsWith("mailto:")){
			Intent i = new Intent(Intent.ACTION_SEND);  
			i.setType("message/rfc822") ;
			i.putExtra(Intent.EXTRA_EMAIL, new String[]{absoluteURL.substring("mailto:".length())});  
			startActivity(Intent.createChooser(i, ""));
			return true;
		}      	
    	return false;
    }
    
	public int findUnusedId(int fID) {
	    while( this.findViewById(android.R.id.content).findViewById(++fID) != null );
	    return fID;
	}    

    public void loadRootPage(String absoluteURL)
    {
    	fragList = new ArrayList<WeakReference<AppDeckFragment>>();
    	// if we don't have focus get it before load page
    	if (isForeground == false)
    	{
    		createIntent(ROOT_PAGE_URL, absoluteURL);
    		return;
    	}
    	prepareRootPage();
    	if (loadSpecialURL(absoluteURL))
    		return;
		AppDeckFragment fragment = initPageFragment(absoluteURL);
    	pushFragment(fragment);
        setMenuArrow(false);
        adManager.showAds(AppDeckAdManager.EVENT_ROOT);
    }

    public boolean isSameDomain(String domain)
    {
        if (domain.equalsIgnoreCase(this.appDeck.config.bootstrapUrl.getHost()))
            return true;
        Pattern otherDomainRegexp[] = AppDeck.getInstance().config.other_domain;

        if (otherDomainRegexp == null)
            return false;

        for (int i = 0; i < otherDomainRegexp.length; i++) {
            Pattern p = otherDomainRegexp[i];

            if (p.matcher(domain).find())
                return true;

        }

        return false;
    }

    public int loadPage(String absoluteURL)
    {
    	if (loadSpecialURL(absoluteURL))
    		return -1;

        Uri uri = Uri.parse(absoluteURL);
        if (uri != null)
        {
            String host = uri.getHost();
            if (host != null && isSameDomain(host) == false)
            {
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);

                // enable custom tab for chrome
                String EXTRA_CUSTOM_TABS_SESSION = "android.support.customtabs.extra.SESSION";
                Bundle extras = new Bundle();
                extras.putBinder(EXTRA_CUSTOM_TABS_SESSION, null/*sessionICustomTabsCallback.asBinder() Set to null for no session */);

                String EXTRA_CUSTOM_TABS_TOOLBAR_COLOR = "android.support.customtabs.extra.TOOLBAR_COLOR";
                //intent.putExtra(EXTRA_CUSTOM_TABS_TOOLBAR_COLOR, R.color.AppDeckColorAccent);
                //intent.putExtra(EXTRA_CUSTOM_TABS_TOOLBAR_COLOR, R.color.AppDeckColorPrimary);
                extras.putInt(EXTRA_CUSTOM_TABS_TOOLBAR_COLOR, R.color.AppDeckColorApp);

                intent.putExtras(extras);

                startActivity(intent);
                return -1;
            }
        }

    	if (isForeground == false)
    	{
    		createIntent(PAGE_URL, absoluteURL);
    		return -1;
    	}		
		AppDeckFragment fragment = initPageFragment(absoluteURL);
    	
    	/*if (fragment.screenConfiguration != null && fragment.screenConfiguration.isPopUp)
    	{
       		//createIntent(POP_UP_URL, fragment.currentPageUrl);
    		showPopUp(null, absoluteURL);
       		return -1;
    	}*/

        fragment.event = AppDeckAdManager.EVENT_PUSH;
        adManager.showAds(AppDeckAdManager.EVENT_PUSH);
        setMenuArrow(true);
    	return pushFragment(fragment);

    }
    
    public int replacePage(String absoluteURL)
    {
		AppDeckFragment fragment = initPageFragment(absoluteURL);
    	
		fragment.enablePushAnimation = false;
		
    	FragmentManager fragmentManager = getSupportFragmentManager();
    	FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    	//fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    	    	
    	AppDeckFragment oldFragment = (AppDeckFragment)fragmentManager.findFragmentByTag("AppDeckFragment");
    	if (oldFragment != null)
    	{
    		oldFragment.setIsMain(false);
    		fragmentTransaction.remove(oldFragment);
    		onDettachFragment(oldFragment);
    	}
    	
    	fragmentTransaction.add(R.id.loader_container, fragment, "AppDeckFragment");
    	
    	//fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    	return fragmentTransaction.commitAllowingStateLoss();
    }

    public AppDeckFragment initPageFragment(String absoluteURL)
    {
        return initPageFragment(absoluteURL, false, false);
    }

    public AppDeckFragment initPageFragment(String absoluteURL, boolean forcePopUp, boolean forceBrowser)
    {
    	ScreenConfiguration config = appDeck.config.getConfiguration(absoluteURL);

        AppDeckFragment fragment;

        // popup to external URL MUST be browser
        Uri uri = Uri.parse(absoluteURL);
        if (uri != null)
        {
            String host = uri.getHost();
            if (host != null && !host.equalsIgnoreCase(this.appDeck.config.bootstrapUrl.getHost()))
            {
                if (forcePopUp)
                    forceBrowser = true;
            }
        }

    	if (forceBrowser || (config != null && config.type != null && config.type.equalsIgnoreCase("browser")))
    	{
    		fragment = WebBrowser.newInstance(absoluteURL);
    	} else {
            fragment = PageSwipe.newInstance(absoluteURL);
            fragment.setRetainInstance(true);
        }
    	
    	fragment.loader = this;
        fragment.screenConfiguration = config;//appDeck.config.getConfiguration(absoluteURL);
        if (fragment.screenConfiguration != null && fragment.screenConfiguration.isPopUp != null && fragment.screenConfiguration.isPopUp)
            fragment.isPopUp = true;
        if (forcePopUp)
            fragment.isPopUp = true;
        return fragment;
    }
    
    public int pushFragment(AppDeckFragment fragment)
    {
        disableMenuItem();
        setSupportProgressBarVisibility(false);

    	FragmentManager fragmentManager = getSupportFragmentManager();
    	FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    	//fragmentTransaction.setTransitionStyle(1);
    	//fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
    	//fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out,android.R.anim.fade_in,android.R.anim.fade_out);
    	
    	//fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
    	
    	//fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
    	//fragmentTransaction.setCustomAnimations(R.anim.exit, R.anim.enter);
    	
    	AppDeckFragment oldFragment = getCurrentAppDeckFragment();
    	if (oldFragment != null)
    	{
    		oldFragment.setIsMain(false);

    		//fragmentTransaction.hide(oldFragment);
    		//fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
    		//fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
    	}

        fragmentTransaction.add(R.id.loader_container, fragment, "AppDeckFragment");
    	//fragmentTransaction.replace(R.id.loader_container, fragment, "AppDeckFragment");
    	//fragmentTransaction.addToBackStack("AppDeckFragment");

    	fragmentTransaction.addToBackStack("AppDeckFragment");
    	
    	//fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
    	//fragmentTransaction.setTransitionStyle()
    	
    	//fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out);
    	//Animations(android.R.anim.fade_in,android.R.anim.fade_out,android.R.anim.fade_in,android.R.anim.fade_out);
    	    	
    	fragmentTransaction.commitAllowingStateLoss();
    	
        layoutSubViews();

    	return 0;
    }

    public boolean pushFragmentAnimation(AppDeckFragment fragment)
    {
    	AppDeckFragment current = getCurrentAppDeckFragment();
    	AppDeckFragment previous = getPreviousAppDeckFragment(current);

    	if (current == null || previous == null)
    		return false;
    	
    	if (fragment != current)
    		return false;

        if (fragment.isPopUp)
        {
            AppDeckFragmentUpAnimation anim = new AppDeckFragmentUpAnimation(previous, current);
            anim.start();
        } else {
            AppDeckFragmentPushAnimation anim = new AppDeckFragmentPushAnimation(previous, current);
            anim.start();
        }
    	
    	
    	return true;
    }
    
    public boolean popFragment()
    {
    	AppDeckFragment current = getCurrentAppDeckFragment();
    	AppDeckFragment previous = getPreviousAppDeckFragment(current);
    	
    	if (current == null || previous == null)
    		return false;

        setSupportProgressBarVisibility(false);

    	onDettachFragment(current);

        if (current.isPopUp)
        {
            AppDeckFragmentDownAnimation anim = new AppDeckFragmentDownAnimation(current, previous);
            anim.start();
        } else if (current.enablePopAnimation)
    	{
    		AppDeckFragmentPopAnimation anim = new AppDeckFragmentPopAnimation(current, previous);
    		anim.start();
    	}
        //previous.event = AppDeckAdManager.EVENT_POP;
        adManager.showAds(AppDeckAdManager.EVENT_POP);

        // check if we pop to root
        previous = getPreviousAppDeckFragment(previous);
        if (previous == null) {
            setMenuArrow(false);
        }

    	return true;
    }

    public boolean popRootFragment()
    {
    	//AppDeckFragment root = getRootAppDeckFragment();

    	FragmentManager fragmentManager = getSupportFragmentManager();
    	
    	fragmentManager.popBackStack();
    	
    	//todo: a faire
    	//fragmentManager.popBackStack(root, FragmentManager.POP_BACK_STACK_INCLUSIVE); 
    	
    	/*prepareRootPage();
    	pushFragment(root);*/
        adManager.showAds(AppDeckAdManager.EVENT_ROOT);
    	return true;
    }

    public void layoutSubViews()
    {
        if (mProgressBarDeterminate != null)
            mProgressBarDeterminate.bringToFront();
        if (mProgressBarIndeterminate != null)
            mProgressBarIndeterminate.bringToFront();
    }
    
    public void reload()
    {
        for(WeakReference<AppDeckFragment> ref : fragList) {
        	AppDeckFragment f = ref.get();
        	f.reload();
        }
        if (leftMenuWebView != null)
        	leftMenuWebView.ctl.reload();
        if (rightMenuWebView != null)
        	rightMenuWebView.ctl.reload();
    }

    public void evaluateJavascript(String js)
    {
        if (leftMenuWebView != null)
            leftMenuWebView.ctl.evaluateJavascript(js, null);
        if (rightMenuWebView != null)
            rightMenuWebView.ctl.evaluateJavascript(js, null);
        for(WeakReference<AppDeckFragment> ref : fragList) {
            AppDeckFragment f = ref.get();
            f.evaluateJavascript(js);
        }
    }

    public Boolean apiCall(AppDeckApiCall call)
	{
        if (call.command.equalsIgnoreCase("ready")) {
            Log.i("API", "**READY**");

            if (historyInjected == false) {
                historyInjected = true;

                SharedPreferences prefs = getSharedPreferences(AppDeckApplication.class.getSimpleName(), Context.MODE_PRIVATE);
                Set<String> hs = prefs.getStringSet("historyUrls", new HashSet<String>());

                String js = "var appdeckCurrentHistoryURL = Location.href;\r\n";
                for (String historyUrl : hs) {
                    js += "history.pushState(null, null, '"+historyUrl+"');\r\n";
                }
                js += "history.pushState(null, null, appdeckCurrentHistoryURL);\r\n";

                call.smartWebView.evaluateJavascript(js, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        Log.i(TAG, "onReadyFinishedJSResult: " + value);
                    }
                });
            }

            //historyUrls.add(call.smartWebView.getUrl());
        }

        if (call.command.equalsIgnoreCase("share"))
		{
			Log.i("API", "**SHARE**");
					
			String shareTitle = call.param.getString("title");
			String shareUrl = call.param.getString("url");
			String shareImageUrl = call.param.getString("imageurl");

			share(shareTitle, shareUrl, shareImageUrl);
						
			return true;
		}		

		if (call.command.equalsIgnoreCase("preferencesget"))
		{
			Log.i("API", "**PREFERENCES GET**");
					
			String name = call.param.getString("name");
			String defaultValue = call.param.optString("value", "");

		    SharedPreferences prefs = getSharedPreferences(AppDeckApplication.class.getSimpleName(), Context.MODE_PRIVATE);
		    
		    String key = "appdeck_preferences_json1_" + name;
		    String finalValueJson = prefs.getString(key, null);
		    
		    if (finalValueJson == null)
		    	call.setResult(defaultValue);
		    else
		    	call.setResult(finalValueJson);
		    /*{
				try {
					ObjectMapper mapper = new ObjectMapper();
					JsonNode json = mapper.readValue(finalValueJson, JsonNode.class);
					call.setResult(json);
				} catch (JsonParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JsonMappingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }*/			
			return true;
		}		
		
		if (call.command.equalsIgnoreCase("preferencesset"))
		{
			Log.i("API", "**PREFERENCES SET**");
					
			String name = call.param.getString("name");
			String finalValue = call.param.optString("value", "");
			
		    SharedPreferences prefs = getSharedPreferences(AppDeckApplication.class.getSimpleName(), Context.MODE_PRIVATE);
		    SharedPreferences.Editor editor = prefs.edit();
		    String key = "appdeck_preferences_json1_" + name;
		    editor.putString(key, finalValue);
	        editor.apply();

		    call.setResult(finalValue);
		   
			return true;
		}		
		
		if (call.command.equalsIgnoreCase("photobrowser"))
		{
			Log.i("API", "**PHOTO BROWSER**");
			// only show image browser if there are images
			AppDeckJsonArray images = call.param.getArray("images");
			if (images.length() > 0)
			{
				PhotoBrowser photoBrowser = PhotoBrowser.newInstance(call.param, call.appDeckFragment);
				photoBrowser.loader = this;
				photoBrowser.appDeck = appDeck;
				photoBrowser.currentPageUrl = "photo://browser";
				photoBrowser.screenConfiguration = ScreenConfiguration.defaultConfiguration();				
				pushFragment(photoBrowser);
			}
			
			return true;
		}		
		
		if (call.command.equalsIgnoreCase("loadapp"))
		{
			Log.i("API", "**LOAD APP**");
			
			String jsonUrl = call.param.getString("url");
			boolean clearCache = call.param.getBoolean("cache");
			
			// clear cache if asked
			if (clearCache)
				this.appDeck.cache.clear();
			
			// dowload json data, put it in cache, lauch app
			AsyncHttpClient client = new AsyncHttpClient();
			client.get(jsonUrl, new AsyncHttpResponseHandler() {
				
				@Override
				public void onSuccess(int statusCode, Header[] headers, byte[] content) {
			    	if (statusCode == 200)
			    	{
						appDeck.cache.storeInCache(this.getRequestURI().toString(), headers, content);
				    	Intent i = new Intent(Loader.this, Loader.class);
				    	i.putExtra(JSON_URL, this.getRequestURI().toString());
				    	startActivity(i);
			    	}
			    	else
			    		Log.e(TAG, "failed to fetch config: "+this.getRequestURI().toString());
				}

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                    // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                    Log.e(TAG, "Error: "+statusCode);
                }
			});
			
			return true;
		}	

		if (call.command.equalsIgnoreCase("reload"))
		{
			reload();
			return true;
		}
		
		if (call.command.equalsIgnoreCase("pageroot"))
		{
			Log.i("API", "**PAGE ROOT**");
			String absoluteURL = call.smartWebView.resolve(call.input.getString("param"));
			this.loadRootPage(absoluteURL);			
			return true;
		}

		if (call.command.equalsIgnoreCase("pagerootreload"))
		{
			Log.i("API", "**PAGE ROOT RELOAD**");
			String absoluteURL = call.smartWebView.resolve(call.input.getString("param"));
			this.loadRootPage(absoluteURL);
	        if (leftMenuWebView != null)
	        	leftMenuWebView.ctl.reload();
	        if (rightMenuWebView != null)
	        	rightMenuWebView.ctl.reload();
			return true;
		}		
		
		if (call.command.equalsIgnoreCase("pagepush"))
		{
			Log.i("API", "**PAGE PUSH**");
			String absoluteURL = call.smartWebView.resolve(call.input.getString("param"));
			this.loadPage(absoluteURL);			
			return true;
		}

        if (call.command.equalsIgnoreCase("popup"))
        {
            Log.i("API", "**PAGE POPUP**");
            String absoluteURL = call.smartWebView.resolve(call.input.getString("param"));
            this.showPopUp(call.appDeckFragment, absoluteURL);
            return true;
        }

		if (call.command.equalsIgnoreCase("pagepop"))
		{
			Log.i("API", "**PAGE POP**");
			this.popFragment();			
			return true;
		}

		if (call.command.equalsIgnoreCase("pagepoproot"))
		{
			Log.i("API", "**PAGE POP ROOT**");
			popRootFragment();
			return true;
		}		

		if (call.command.equalsIgnoreCase("slidemenu"))
		{
			String command = call.param.getString("command");
			String position = call.param.getString("position");

			if (command.equalsIgnoreCase("toggle"))
			{
				if (position.equalsIgnoreCase("left"))
					openLeftMenu();
				if (position.equalsIgnoreCase("right"))
					openRightMenu();
				if (position.equalsIgnoreCase("main"))
					toggleMenu();
			} else if (command.equalsIgnoreCase("open"))
			{
				if (position.equalsIgnoreCase("left"))
					openLeftMenu();
				if (position.equalsIgnoreCase("right"))
					openRightMenu();
				if (position.equalsIgnoreCase("main"))
					closeMenu();
			} else {
				closeMenu();
			}
			return true;
		}	
		
		if (call.command.startsWith("is"))
		{
			Log.i("API", "** IS ["+call.command+"] **");
			
			boolean result = false;
			
			if (call.command.equalsIgnoreCase("istablet"))
				result = this.appDeck.isTablet;
			else if (call.command.equalsIgnoreCase("isphone"))
				result = !this.appDeck.isTablet;
			else if (call.command.equalsIgnoreCase("isios"))
				result = false;
			else if (call.command.equalsIgnoreCase("isandroid"))
				result = true;
			else if (call.command.equalsIgnoreCase("islandscape"))
				result = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
			else if (call.command.equalsIgnoreCase("isportrait"))
				result = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

			call.setResult(Boolean.valueOf(result));
			
			return true;
		}


        if (call.command.equalsIgnoreCase("facebooklogin")) {
            Log.i("API", "** FACEBOOK LOGIN **");

            Collection permissions = Arrays.asList("publish_actions");

            AppDeckJsonArray values = call.param.getArray("permissions");
            if (values != null && values.length() > 0) {
                List<String> var = new ArrayList<>();
                for (int i = 0; i < values.length(); i++) {
                    var.add(values.getString(i));
                }
                permissions = var;
            }
            final AppDeckApiCall mycall = call;

            LoginManager.getInstance().registerCallback(callbackManager,
                    new FacebookCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult loginResult) {
                            // App code
                            Log.d(TAG, "facebook login ok");

                            JSONObject result = new JSONObject();
                            try {
                                result.put("appID", loginResult.getAccessToken().getApplicationId());
                                result.put("token", loginResult.getAccessToken().getToken());
                                result.put("userID", loginResult.getAccessToken().getUserId());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            mycall.sendCallbackWithResult("success", result);
                        }

                        @Override
                        public void onCancel() {
                            // App code
                            Log.d(TAG, "facebook login cancel");
                            mycall.sendCallBackWithError("cancel");
                        }

                        @Override
                        public void onError(FacebookException exception) {
                            // App code
                            Log.d(TAG, "facebook login error");
                            mycall.sendCallBackWithError(exception.getMessage());
                        }
                    });
            call.postponeResult();
            willShowActivity = true;
            LoginManager.getInstance().logInWithReadPermissions(this, permissions);

            return true;

        }

        if (call.command.equalsIgnoreCase("twitterlogin")) {
            Log.i("API", "** TWITTER LOGIN **");

            if (mTwitterAuthClient == null)
            {
                Toast.makeText(getApplicationContext(), "Twitter is not configured for this app", Toast.LENGTH_LONG).show();
                return true;
            }

            //call.postponeResult();
            call.setResultJSON("true");

            willShowActivity = true;

            final AppDeckApiCall mycall = call;
            mTwitterAuthClient.authorize(this, new com.twitter.sdk.android.core.Callback<TwitterSession>() {

                @Override
                public void success(final Result<TwitterSession> twitterSessionResult) {
                    // Success
                    Log.d(TAG, "Twitter login ok");

                    JSONObject result = new JSONObject();
                    try {
                        result.put("userName", twitterSessionResult.data.getUserName());
                        result.put("authToken", twitterSessionResult.data.getAuthToken().token);
                        result.put("authTokenSecret", twitterSessionResult.data.getAuthToken().secret);
                        result.put("userID", twitterSessionResult.data.getUserId() + "");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mycall.sendCallbackWithResult("success", result);
                }

                @Override
                public void failure(TwitterException e) {
                    Log.d(TAG, "twitter login failed");
                    mycall.sendCallBackWithError(e.getMessage());
                    e.printStackTrace();
                }
            });
            return true;

        }

		Log.i("API ERROR", call.command);
		return false;
	}
	
    @Override
    public void onBackPressed() {
    	
    	// close menu ?
       	if (isLeftMenuOpen())
       	{
       		closeMenu();
       		return;
       	}
       	if (isRightMenuOpen())
       	{
       		closeMenu();
       		return;
       	}

        // current fragment can go back ?
        AppDeckFragment currentFragment = getCurrentAppDeckFragment();
        if (currentFragment != null && currentFragment.canGoBack())
        {
        	currentFragment.goBack();
        	return;
        }
        
        // try to pop a fragment if possible
        if (popFragment()) {
            return;
        }

        // current fragment is home ?
        if (alternativeBootstrapURL == null)
            if (currentFragment != null && currentFragment.currentPageUrl != null)
                if (/*currentFragment == null || currentFragment.currentPageUrl == null ||*/ currentFragment.currentPageUrl.compareToIgnoreCase(appDeck.config.bootstrapUrl.toString()) != 0)
                {
        //        	Debug.stopMethodTracing();
                    loadRootPage(appDeck.config.bootstrapUrl.toString());
                    return;
                }
        
        finish();      

    }

    // Full Screen API
    public void enableFullScreen()
    {
        //getActivity().getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN, android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActionBar actionBar = getSupportActionBar();
        //actionBar.setShowHideAnimationEnabled(false);
        actionBar.hide();
    }

    public void disableFullScreen()
    {
        ActionBar actionBar = getSupportActionBar();
        //actionBar.setShowHideAnimationEnabled(false);
        actionBar.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

    	if (keyCode == KeyEvent.KEYCODE_MENU)
    	{
    		toggleMenu();
    		return true;
    	}

        return super.onKeyDown(keyCode, event);
    }
/*
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_MENU)
        {
            toggleMenu();
            return true;

        }
        return super.dispatchKeyEvent(event);
    }
*/

    public int getActionBarHeight()
    {
    	int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        
        if (actionBarHeight != 0)
        	return actionBarHeight;

       //OR as stated by @Marina.Eariel
       //TypedValue tv = new TypedValue();
       if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
          if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
       }
       
       return actionBarHeight;
    }

    public void disableMenuItem()
    {
        if (menuItems != null)
            for (int i = 0; i < menuItems.length; i++) {
                PageMenuItem item = menuItems[i];
                item.cancel();
            }
    }
    
    public void setMenuItems(PageMenuItem[] newMenuItems)
    {/*
        // does new Menu is compatible with old menu ? (meaning we only remove or add things)
        if (newMenuItems != null && menuItems != null && menu != null && newMenuItems.length > 0 && menuItems.length > 0) {
            int newIdx = newMenuItems.length - 1;
            int oldIdx = menuItems.length - 1;
            PageMenuItem newItem = newMenuItems[newIdx];
            PageMenuItem oldItem = menuItems[oldIdx];
            if (newItem.icon.equalsIgnoreCase(oldItem.icon)) {
                while (newIdx >= 0 && oldIdx >= 0) {
                    newItem = newMenuItems[newIdx];
                    oldItem = menuItems[oldIdx];

                    if (!newItem.icon.equalsIgnoreCase(oldItem.icon))
                        return;

                    oldItem.title = newItem.title;
                    oldItem.content = newItem.content;
                    oldItem.badge = newItem.badge;
                    oldItem.type = newItem.type;
                    oldItem.badgeDrawable.setCount(oldItem.badge);
                    Log.d("setMenuItems", newItem.icon);

                    newIdx--;
                    oldIdx--;
                }
                return;
            }
        }*/

        // hide previous menu
    	if (this.menuItems != null)
    		for (int i = 0; i < this.menuItems.length; i++) {
    			PageMenuItem item = this.menuItems[i];
    			item.cancel();
    		}
    	this.menuItems = newMenuItems;
    	supportInvalidateOptionsMenu();
    }

    //ShareActionProvider mShareActionProvider;

    private Menu menu = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        this.menu = menu;

    	if (menuItems == null)
    		return true;

        if (menuItems.length == 0)
            return true;

        for (int i = 0; i < menuItems.length; i++) {
			PageMenuItem item = menuItems[i];

            MenuItem menuItem = menu.add("button");

			item.setMenuItem(menuItem, this, menu);

		}


/*
        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) menuItem.getActionProvider();
        // Set history different from the default before getting the action
        // view since a call to MenuItem.getActionView() calls
        // onCreateActionView() which uses the backing file name. Omit this
        // line if using the default share history file is desired.
        mShareActionProvider.setShareHistoryFileName("custom_share_history.xml");*/
        return true;
    }


    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // topbar button
        if (menuItems != null) {
            for (int i = 0; i < menuItems.length; i++) {
                PageMenuItem pageMenuItem = menuItems[i];
                if (pageMenuItem.menuItem == item) {
                    pageMenuItem.fire();
                    return true;
                }
            }
        }

        int idx = item.getItemId();

        if (idx == android.R.id.home) {
            if (isMenuOpen() == false) {
                if (menuArrowIsShown) {
                    // try to pop a fragment if possible
                    if (popFragment()) {
                        return true;
                    }
                }
            }
        }

        if (mDrawerToggle.onOptionsItemSelected(item))
            return true;

    	if (idx == android.R.id.home)
    	{
   			toggleMenu();
   			return true;	
    	}

		return super.onOptionsItemSelected(item);
    }    
	
	public void share(String title, String url, String imageURL)
	{

        android.support.v7.widget.ShareActionProvider shareProvider = null;

//		ShareActionProvider shareAction = null;	
//		shareAction = new ShareActionProvider(this);
		
		// add stats
		appDeck.ga.event("action", "share", (url != null && !url.isEmpty() ? url : title), 1);
		
		// create share intent
		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);

        // trim title if needed
        if (title != null)
            title = title.trim();

		sharingIntent.setType("text/plain");
		if (title != null && !title.isEmpty())
			sharingIntent.putExtra(Intent.EXTRA_SUBJECT, title);
		if (url != null && !url.isEmpty())
			sharingIntent.putExtra(Intent.EXTRA_TEXT, url);
		
		// not an image ?
		if (imageURL == null || imageURL.isEmpty())
		{
			startActivity(Intent.createChooser(sharingIntent, "Share via"));
			return;
		}

		// image ?
        DisplayImageOptions options = new DisplayImageOptions.Builder()
        .cacheInMemory(true)
        .cacheOnDisc(true)
        .build();
        
        // patch image URL
        if (imageURL.startsWith("//"))
        	imageURL = "http:"+imageURL;
        
        // Load image, decode it to Bitmap and return Bitmap to callback
        appDeck.imageLoader.loadImage(imageURL, options, new sharingImageLoadingListener(imageURL, sharingIntent));
	}
	
	private class sharingImageLoadingListener extends SimpleImageLoadingListener
	{
    	@SuppressWarnings("unused")
		String imageURL;
    	Intent sharingIntent;
    	
    	sharingImageLoadingListener(String imageURL, Intent sharingIntent)
    	{
    		this.imageURL = imageURL;
    		this.sharingIntent = sharingIntent;
    	}
    	
    	@Override
    	public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
    		Log.e("image Sharing", failReason.toString());
    	};
    	
    	@Override
    	public void onLoadingComplete(String imageUri, View view,
    			Bitmap loadedImage) {
    		super.onLoadingComplete(imageUri, view, loadedImage);

    		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    		loadedImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
    		File f = new File(Environment.getExternalStorageDirectory() + File.separator + "image.jpg");
    		try {
    		    f.createNewFile();
    		    FileOutputStream fo = new FileOutputStream(f);
    		    fo.write(bytes.toByteArray());
    		    fo.close();
    		} catch (IOException e) {                       
    		        e.printStackTrace();
    		}
    		sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + Environment.getExternalStorageDirectory().getPath()  + "image.jpg"));
    		startActivity(Intent.createChooser(sharingIntent, "Share via"));    		
    	}
	}
	
	// cancel popup, popover, dialog from current page
	public void cancelSubViews()
	{
		/*if (appDeck.glPopup != null)
		{
			appDeck.glPopup.finish();
			appDeck.glPopup = null;
		}*/
		if (!isForeground)
			return;
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager == null)
        	return;
		Fragment popover = fragmentManager.findFragmentByTag("fragmentPopOver");
		if (popover != null)
		{
	    	FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
	    	//fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
	    	fragmentTransaction.remove(popover);
	    	fragmentTransaction.commitAllowingStateLoss();			
		}
		Fragment popup = fragmentManager.findFragmentByTag("fragmentPopUp");
		if (popup != null)
		{
			getSupportActionBar().show();
	    	FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
	    	fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);		    	
	    	fragmentTransaction.remove(popup);
	    	fragmentTransaction.commitAllowingStateLoss();			
		}
	}
	
	public void showPopOver(AppDeckFragment origin, AppDeckApiCall call)
	{
        /*
		if (origin != null)
			origin.loader.cancelSubViews();
		
	    // rather create this as a instance variable of your class		
		PopOverFragment popover = new PopOverFragment(origin, call);
		popover.loader = this;
		//popover.setRetainInstance(true);
		//popover.screenConfiguration = appDeck.config.getConfiguration(popover.currentPageUrl);		
		
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		//ft.add(popover, "fragmentPopOver");
		ft.add(R.id.loader_container, popover, "fragmentPopOver");
		ft.commitAllowingStateLoss();
			*/
		/*
		Dialog popUpDialog = new Dialog(getBaseContext(),
                android.R.style.Theme_Translucent_NoTitleBar);
		popUpDialog.setCanceledOnTouchOutside(true);
		popUpDialog.setContentView(popover.getView());*/
	}
	
	public void showPopUp(AppDeckFragment origin, String absoluteURL)
	{
		//Log.w(TAG, "PopUp not suported on Android Platform, use push instead");
		//loadPage(url);

        AppDeckFragment fragment = initPageFragment(absoluteURL, true, false);
        pushFragment(fragment);

/*        PopUpFragment popupfragment = PopUpFragment.newInstance(absoluteURL);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //fragmentTransaction.setTransitionStyle(1);
        //fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        //fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out,android.R.anim.fade_in,android.R.anim.fade_out);

        //fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);

        //fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
        //fragmentTransaction.setCustomAnimations(R.anim.exit, R.anim.enter);

        AppDeckFragment oldFragment = getCurrentAppDeckFragment();
        if (oldFragment != null)
        {
            oldFragment.setIsMain(false);

            //fragmentTransaction.hide(oldFragment);
            //fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
            //fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        }


        fragmentTransaction.add(R.id.loader_container, popupfragment, "fragmentPopUp");
        //fragmentTransaction.replace(R.id.loader_container, fragment, "AppDeckFragment");
        //fragmentTransaction.addToBackStack("AppDeckFragment");

        fragmentTransaction.addToBackStack("fragmentPopUp");

        //fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        //fragmentTransaction.setTransitionStyle()

        //fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out);
        //Animations(android.R.anim.fade_in,android.R.anim.fade_out,android.R.anim.fade_in,android.R.anim.fade_out);

        fragmentTransaction.commitAllowingStateLoss();*/


	}
	
	/*
	public void showPopUp(AppDeckFragment origin, String url)
	{
		if (origin != null)
			origin.loader.cancelSubViews();
		Intent intent = new Intent(this, PopUp.class);
    	//intent.setFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
//    			|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	intent.putExtra(PopUp.POP_UP_URL, (origin != null ? origin.resolveURL(url) : url));
    	startActivity(intent);
    	//overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        Display display = ((android.view.WindowManager) 
                getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        if ((display.getRotation() == Surface.ROTATION_0) || 
            (display.getRotation() == Surface.ROTATION_180)) {
        	//overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
        	overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
        } else if ((display.getRotation() == Surface.ROTATION_90) ||
                   (display.getRotation() == Surface.ROTATION_270)) {
        	//overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        	overridePendingTransition(R.anim.slide_in_left, android.R.anim.fade_out);
        	
        }
		//getActivity().getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN, android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
   		
	}
	*/
	protected void createIntent(String type, String absoluteURL)
	{
		cancelSubViews();
		Intent i = new Intent(this, Loader.class);
    	i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    	//i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	i.putExtra(type, absoluteURL);
    	startActivity(i);
	}
	
    @Override
    protected void onNewIntent(Intent intent)
    {
    	super.onNewIntent(intent);

        SmartWebViewFactory.onActivityNewIntent(this, intent);

    	isForeground = true;
    	Bundle extras = intent.getExtras();
    	if (extras == null)
    		return;

    	// loadUrl intent
    	String url = extras.getString(PAGE_URL);
    	if (url != null && !url.isEmpty())
    	{
    		loadPage(url);
    		return;
    	}

    	// root url
    	url = extras.getString(ROOT_PAGE_URL);
    	if (url != null && !url.isEmpty())
    	{
    		loadRootPage(url);
    		return;
    	}    	  

    	// Push Notification
    	url = extras.getString(PUSH_URL);
    	if (url != null && !url.isEmpty())
    	{
    		String title = extras.getString(PUSH_TITLE);
            String imageUrl = extras.getString(PUSH_IMAGE_URL);
            Log.i(TAG, "Auto Open Push: "+title+" url: "+url);
            //handlePushNotification(title, url, imageUrl);
            try {
                url = appDeck.config.app_base_url.resolve(url).toString();
                loadPage(url);
            } catch (Exception e) {

            }
            return;
    	}
    	
/*
    	// popup url
    	url = extras.getString(POP_UP_URL);
    	if (url != null && !url.isEmpty())
    	{
    		showPopUp(null, url);
    		return;
    	}*/
    	
    }

    @Override
    public  void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (smartWebViewRegiteredForActivityResult != null) {
            smartWebViewRegiteredForActivityResult.onActivityResult(this, requestCode, resultCode, data);
            smartWebViewRegiteredForActivityResult = null;
        } else {
            SmartWebViewFactory.onActivityResult(this, requestCode, resultCode, data);
        }
        if (callbackManager != null)
            callbackManager.onActivityResult(requestCode, resultCode, data);
        if (mTwitterAuthClient != null)
            mTwitterAuthClient.onActivityResult(requestCode, resultCode, data);
    }



    boolean pushDialogInProgress = false;

    public void handlePushNotification(String title, String url, String imageUrl)
    {
        if (url != null)
            url = appDeck.config.app_base_url.resolve(url).toString();
        if (imageUrl != null)
            imageUrl = appDeck.config.app_base_url.resolve(imageUrl).toString();
        if (title == null || title.equalsIgnoreCase(""))
            return;
        new PushDialog(url, title, imageUrl).show();
    }
	
    public class PushDialog
    {
    	String url;
    	String title;
        String imageUrl;
    	
    	public PushDialog(String url, String title, String imageUrl)
    	{
			this.url = url;
			this.title = title;
            this.imageUrl = imageUrl;
		}
    	
    	public void show()
    	{
            if (pushDialogInProgress)
                return;
            pushDialogInProgress = true;
            new AlertDialog.Builder(Loader.this)
            //.setTitle("javaScript dialog")
            .setMessage(title)
            .setPositiveButton(android.R.string.ok, 
                    new DialogInterface.OnClickListener() 
                    {
                        public void onClick(DialogInterface dialog, int which) 
                        {
                        	loadPage(url);
                        }
                    })
            .setNegativeButton(android.R.string.cancel, 
                    new DialogInterface.OnClickListener() 
                    {
                        public void onClick(DialogInterface dialog, int which) 
                        {
                            pushDialogInProgress = false;
                        }
                    })
            .create()
            .show();      		
    	}
    }
    
    boolean shouldRenderActionBar = true;
    public void toggleActionBar()
    {
 	   shouldRenderActionBar = !shouldRenderActionBar;
 	   
 	   if (shouldRenderActionBar)
 		   getSupportActionBar().show();
 	   else
 		   getSupportActionBar().hide();
    }

    // Progress Bar
    @Override
    public void setSupportProgressBarVisibility(boolean visibility)
    {
        if (mProgressBarIndeterminate == null || mProgressBarDeterminate == null)
            return;
        if (visibility) {
            mProgressBarIndeterminate.setVisibility(View.GONE);
            mProgressBarDeterminate.setVisibility(View.VISIBLE);
        } else {
            mProgressBarIndeterminate.setVisibility(View.GONE);
            mProgressBarDeterminate.setVisibility(View.GONE);
        }
    }

    @Override
    public void setSupportProgressBarIndeterminateVisibility(boolean visibility)
    {
        if (mProgressBarIndeterminate == null || mProgressBarDeterminate == null)
            return;
        if (visibility) {
            mProgressBarIndeterminate.setVisibility(View.VISIBLE);
            mProgressBarDeterminate.setVisibility(View.GONE);
        } else {
            mProgressBarIndeterminate.setVisibility(View.GONE);
            mProgressBarDeterminate.setVisibility(View.GONE);
        }
    }

    @Override
    public void setSupportProgressBarIndeterminate(boolean indeterminate)
    {
        if (mProgressBarIndeterminate == null || mProgressBarDeterminate == null)
            return;
        mProgressBarDeterminate.setProgress(0);
    }

    @Override
    public void setSupportProgress(int progress)
    {
        if (mProgressBarIndeterminate == null || mProgressBarDeterminate == null)
            return;
        mProgressBarDeterminate.setProgress(progress);
    }
/*
    void enableProxy()
    {
        System.setProperty("http.proxyHost", this.proxyHost);
        System.setProperty("http.proxyPort", this.proxyPort + "");
        //System.setProperty("https.proxyHost", this.proxyHost);
        //System.setProperty("https.proxyPort", this.proxyPort + "");
        try {
            //WebkitProxy3.setProxy(this, new WebView(), proxyHost, proxyPort, Application.class.getCanonicalName());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    void disableProxy()
    {
        if (this.originalProxyHost != null) {
            System.setProperty("http.proxyHost", this.proxyHost);
            System.setProperty("http.proxyPort", this.proxyPort + "");
            //System.setProperty("https.proxyHost", this.proxyHost);
            //System.setProperty("https.proxyPort", this.proxyPort + "");
        } else {
            System.setProperty("http.proxyHost", "");
            System.setProperty("http.proxyPort", "");
            //System.setProperty("https.proxyHost", "");
            //System.setProperty("https.proxyPort", "");
        }
    }*/
}
