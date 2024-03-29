package com.tregz.miksing.home;

import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.tregz.miksing.R;
import com.tregz.miksing.base.BaseActivity;
import com.tregz.miksing.base.play.PlayVideo;
import com.tregz.miksing.data.work.Work;
import com.tregz.miksing.home.item.ItemFragment;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.ui.NavigationUI;

public class HomeActivity extends BaseActivity implements HomeView,
        AppBarLayout.BaseOnOffsetChangedListener<AppBarLayout> {

    private boolean collapsing = false;
    private FloatingActionButton[] buttons = new FloatingActionButton[4];
    private ImageView imageView;
    private HomeNavigation navigation;
    private PlayVideo webView;
    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (portrait()) {
            DrawerLayout drawer = findViewById(R.id.drawer);
            ((AppBarLayout)findViewById(R.id.app_bar)).addOnOffsetChangedListener(this);
            setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
            NavController controller = HomeScenario.getInstance().controller(this);
            NavigationUI.setupActionBarWithNavController(this, controller, drawer);
            NavigationView navigationView = findViewById(R.id.nav_start);
            if (drawer != null) navigation = new HomeNavigation(drawer, navigationView);
            logo();

            // Container for media players
            FrameLayout frame = findViewById(R.id.players);
            panoramic(frame, 0);

            // Listeners
            buttons[0] = findViewById(R.id.fab);
            buttons[0].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!back()) {
                        navigate(R.id.action_homeFragment_to_workFragment);
                        expand((FloatingActionButton)v);
                    }
                }
            });
            buttons[1] = findViewById(R.id.clear_all);
            buttons[1].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((HomeDialog)add(new HomeDialog(HomeActivity.this))).clear();
                }
            });
            buttons[2] = findViewById(R.id.save);
            buttons[2].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((HomeDialog)add(new HomeDialog(HomeActivity.this))).save();
                }
            });
            buttons[3] = findViewById(R.id.web_search);
            buttons[3].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    add(new HomeDialog(HomeActivity.this, webView));
                }
            });
        }

        /* Video player */
        videoView = findViewById(R.id.video_1);
        videoView.setMediaController(new MediaController(this));
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start(); // auto play
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.start(); // loop
            }
        });
        String path1 = "anim/Miksing_Logo-Animated.mp4";
        Task<Uri> t1 = FirebaseStorage.getInstance().getReference().child(path1).getDownloadUrl();
        t1.addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                videoView.setVideoURI(uri);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e.getMessage() != null) toast(e.getMessage());
            }
        });
        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.setVisibility(View.GONE);
                webView.load("5-q3meXJ6W4"); // testing
            }
        });

        // Web video player
        webView = findViewById(R.id.webview);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (portrait()) getMenuInflater().inflate(R.menu.toolbar_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: if (!back()) navigation.toggle(GravityCompat.START);
                return true;
            case R.id.login:
                if (navigation != null) navigation.toggle(GravityCompat.END);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        Log.d(TAG, "OffsetChanged " + verticalOffset);
        if (portrait()) {
            if (verticalOffset < -300) { if (!collapsing) collapsing = true; }
            else if (collapsing) collapsing = false;
            if (collapsing) {
                if (buttons[0].getVisibility() == View.VISIBLE) {
                    imageView.setVisibility(View.INVISIBLE);
                    if (buttons[0].isExpanded()) for (FloatingActionButton fab : buttons) fab.hide();
                    else buttons[0].hide();
                }
            } else if (buttons[0].getVisibility() == View.GONE) {
                imageView.setVisibility(View.VISIBLE);
                if (buttons[0].isExpanded()) for (FloatingActionButton fab : buttons) fab.show();
                else buttons[0].show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        back();
    }

    @Override
    public void onClearItemDetails() {
        Fragment primary = primary();
        if (primary instanceof ItemFragment) ((ItemFragment) primary).clear();
    }

    @Override
    public void onFillItemDetails(Work work) {
        Fragment primary = primary();
        if (primary instanceof ItemFragment) ((ItemFragment) primary).fill(work);
    }

    @Override
    public void onSaveItem() {
        Fragment primary = primary();
        Log.d(TAG, "onSaveItem " + (primary.getClass().getSimpleName()));
        if (primary instanceof ItemFragment) ((ItemFragment) primary).save();
    }

    @Override
    public void onSaved() {
        back();
    }

    private void navigate(int action) {
        HomeScenario.getInstance().navigate(this, action);
    }

    private void expand(FloatingActionButton fab) {
        fab.setExpanded(!fab.isExpanded());
        fab.setImageResource(fab.isExpanded() ? R.drawable.ic_close : R.drawable.ic_add);
    }

    private void panoramic(View view, int top) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = ((int)(getResources().getDisplayMetrics().widthPixels * 0.5625)) + top;
        view.setLayoutParams(params);
    }

    private boolean back() {
        if (portrait() && HomeScenario.getInstance().fragmentId(this) != R.id.homeFragment) {
            expand((FloatingActionButton)findViewById(R.id.fab));
            HomeScenario.getInstance().pop(this);
            // TODO if (icPerson != null) icPerson.setIcon(R.drawable.ic_person);
            return true;
        } else return false;
    }

    private void logo() {
        /* Image viewer */
        imageView = findViewById(R.id.image_1);
        String path2 = "draw/Cshawi-logo-mini.png";
        Task<Uri> t2 = FirebaseStorage.getInstance().getReference().child(path2).getDownloadUrl();
        t2.addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                image(uri, R.id.image_1);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e.getMessage() != null) toast(e.getMessage());
            }
        });
    }

    private boolean portrait() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    private Fragment primary() {
        return HomeScenario.getInstance().primary(this);
    }

    /* testing
    private void testing() {
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            DisplayMetrics dm = getResources().getDisplayMetrics();
            int gap = TypedValue.complexToDimensionPixelSize(tv.data, dm);
            int gap = (int) getResources().getDimension(R.dimen.small_gap);
            int top = (int) getResources().getDimension(R.dimen.app_bar_layout_height) + gap;
            panoramic(appBar, top);
        } */

        //BottomNavigationView bottom = findViewById(R.id.bottom);
        /* Set navigation on bottom view
        NavigationUI.setupWithNavController(bottom, HomeScenario.getInstance().controller(this));
    } */

    static {
        TAG = HomeActivity.class.getSimpleName();
    }
}
