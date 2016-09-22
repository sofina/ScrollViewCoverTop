package com.example.sofina.scrollviewcovertop;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewGroup layout = (ViewGroup) findViewById(R.id.main_layout_root);

        try {
            ActivityInfo[] activityInfos = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES).activities;
            for (ActivityInfo activity : activityInfos) {
                final String className = activity.name;
                final Class<?> clazz = Class.forName(className);
                if(className.equals(getClass().getName())){
                    continue;
                }
                Button b = new Button(this);
                b.setText(activity.labelRes == 0 ? clazz.getSimpleName() : getString(activity.labelRes));
                b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(MainActivity.this, clazz));
                    }
                });
                layout.addView(b);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
