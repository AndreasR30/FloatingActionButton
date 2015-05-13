package com.andreas.floatingactionbutton;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.andreas.library.FloatingActionButton;
import com.andreas.library.MorphLayout;


public class MainActivity extends AppCompatActivity implements MorphLayout.OnMorphListener {

    private FloatingActionButton mFab;
    private MorphLayout mFabLayer;
    private SlideDrawable mSlideBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.bar_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        mFab = (FloatingActionButton) findViewById(R.id.fab_floatingActionButton);
        mFab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                mFab.toggle();
                mFabLayer.morph(true, true);
            }
        });

        mFabLayer = (MorphLayout) findViewById(R.id.layer_morphableLayout);
        mFabLayer.setMorphListener(this);
        mFabLayer.setFab(mFab);

        View slideView = findViewById(R.id.slide_view);
        Drawable layer1 = new ColorDrawable(getResources().getColor(android.R.color.white));
        Drawable layer2 = new ColorDrawable(getResources().getColor(R.color.theme_secondary));
        mSlideBackground = new SlideDrawable(new Drawable[] {layer1, layer2}, SlideDrawable.MODE_VERTICAL, false);
        slideView.setBackgroundDrawable(mSlideBackground);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if(mFabLayer.getState() == MorphLayout.State.MORPHED) {
            mFab.toggle();
            mFabLayer.revert(true, true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onMorphStart(int duration) {

        mSlideBackground.startTransition(duration);
    }

    @Override
    public void onMorphEnd() {

    }

    @Override
    public void onRevertStart(int duration) {

        mSlideBackground.reverseTransition(duration);
    }

    @Override
    public void onRevertEnd() {

    }
}
