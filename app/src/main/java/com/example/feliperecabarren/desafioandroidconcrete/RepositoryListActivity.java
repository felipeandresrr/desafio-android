package com.example.feliperecabarren.desafioandroidconcrete;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import adapter.RepositoryListAdapter;
import asynctasks.ProgressBarAsyncTask;
import asynctasks.RepositoryAsyncTask;
import constants.UrlConstans;
import dto.RepositoryList;
import utils.UtilsRepository;


public class RepositoryListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RepositoryListAdapter repositoryListAdapter;
    ArrayList<RepositoryList> repositoryLists;
    UrlConstans urlConstans;
    Boolean isScrolling = false;
    int currentItems, totalItems, scrollOutItems;
    LinearLayoutManager manager;
    int countPage = 1;
    int totalRepository = 0;
    UtilsRepository utilsRepository;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_repositorio);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        repositoryLists = new ArrayList<>();
        urlConstans = new UrlConstans();
        manager = new LinearLayoutManager(this);
        utilsRepository = new UtilsRepository();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewRepositorio);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                    isScrolling = true;
                }
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                currentItems = manager.getChildCount();
                totalItems = manager.getItemCount();
                scrollOutItems = manager.findFirstVisibleItemPosition();

                if(isScrolling && (currentItems + scrollOutItems == totalItems))
                {
                    recyclerView.setAlpha(0.1f);
                    progressBar.setVisibility(View.VISIBLE);
                    isScrolling = false;
                    countPage++;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            callRepositoryUrl();
                        }
                    }, 1000);
                }
            }
        });
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setAlpha(0.1f);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                callRepositoryUrl();
            }
        }, 1000);

    }

    public void callRepositoryUrl(){
        try{
            String jsonResult = new RepositoryAsyncTask().execute(urlConstans.getReposityListUrl(countPage)).get();
            JSONObject jsonObject = utilsRepository.stringToJsonObject(jsonResult);
            loadListRepository(jsonObject);
        }
        catch (Exception e){
            String error = e.toString();
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setAlpha(0.1f);
        }

    }

    public void loadListRepository(JSONObject jsonObject){
        try{
            JSONArray jsonArray = jsonObject.getJSONArray("items");
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObjectItem = jsonArray.getJSONObject(i);
                JSONObject ownerObject = jsonObjectItem.getJSONObject("owner");
                String avatar = ownerObject.getString("avatar_url");
                repositoryLists.add(new RepositoryList(
                        jsonObjectItem.getString("name"),
                        jsonObjectItem.getString("description"),
                        jsonObjectItem.getString("forks_count"),
                        jsonObjectItem.getString("stargazers_count"),
                        ownerObject.getString("avatar_url"),
                        ownerObject.getString("login"),
                        ownerObject.getString("login"),
                        jsonObjectItem.getString("open_issues"),
                        jsonObjectItem.getString("forks_count")
                ));
            }
            totalRepository = repositoryLists.size() / countPage;

            repositoryListAdapter =  new RepositoryListAdapter(repositoryLists,this,this);
            repositoryListAdapter.notifyDataSetChanged();
            recyclerView.setAdapter(repositoryListAdapter);
            recyclerView.scrollToPosition(totalRepository + 1);
            progressBar.setVisibility(View.INVISIBLE);
            recyclerView.setAlpha(1f);
        }catch (JSONException e){

        }

    }



}
