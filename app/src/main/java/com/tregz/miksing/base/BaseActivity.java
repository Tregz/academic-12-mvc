package com.tregz.miksing.base;

import android.net.Uri;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.tregz.miksing.R;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseActivity extends AppCompatActivity {
    protected static String TAG = BaseActivity.class.getSimpleName();

    public List<BaseDialog> dialogs = new ArrayList<>();

    public ViewGroup getViewGroup() {
        return getWindow().getDecorView().findViewById(android.R.id.content);
    }

    public BaseDialog add(BaseDialog dialog) {
        dialogs.add(dialog);
        return dialog;
    }

    @Override
    protected void onDestroy() {
        for (BaseDialog dialog : dialogs) if (dialog.alert != null) dialog.alert.dismiss();
        super.onDestroy();
    }

    protected void image(Uri uri, int resource) {
        Glide.with(this).load(uri).into((ImageView)findViewById(resource));
    }

    protected void toast(@NonNull String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
