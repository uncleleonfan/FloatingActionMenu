package com.leon.floatingactionmenu;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new ListAdapter(this));
    }

    public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {

        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }


        @Override
        public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.d(TAG, "onCreateViewHolder: onCreateViewHolder");
            return new ListViewHolder(LayoutInflater.from(mContext).inflate(R.layout.view_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ListViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 30;
        }


        class ListViewHolder extends RecyclerView.ViewHolder {

            public ListViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
