package com.andreas.floatingactionbutton;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.andreas.library.FloatingActionButton;
import com.andreas.library.MorphLayout;


public class MainActivity extends AppCompatActivity {

    private FloatingActionButton mFab;
    private MorphLayout mFabLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFabLayer = (MorphLayout) findViewById(R.id.layer_morphableLayout);
        mFab = (FloatingActionButton) findViewById(R.id.fab_floatingActionButton);

        mFabLayer.setFab(mFab);
        mFab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                mFabLayer.morph(true);
            }
        });
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
            mFabLayer.revert(true);
        } else {
            super.onBackPressed();
        }
    }
}
