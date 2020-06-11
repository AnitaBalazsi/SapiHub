package com.example.sapihub.Fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.sapihub.Activities.AddNewsActivity;
import com.example.sapihub.Activities.SplashScreen;
import com.example.sapihub.Helpers.Adapters.NewsListAdapter;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.News;
import com.example.sapihub.Model.User;
import com.example.sapihub.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFragment extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener, AdapterView.OnItemSelectedListener {
    private NewsListAdapter adapter;
    private ImageView addNews;
    private FirebaseRecyclerOptions<News> newsList;
    private SearchView searchView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView listView;
    private Spinner filterNews;
    private User user;
    private LinearLayout captionContainer;
    private static Context context;

    public NewsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getContext();
        initializeVariables();
        loadCurrentUser();

    }

    private void loadCurrentUser() {
        DatabaseHelper.getUserData(Utils.getCurrentUserToken(context), new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                user = (User) object;
                List<String> captions = new ArrayList<>();
                captions.add(user.getDepartment());
                if (user.getDegree() != null){
                    captions.add(user.getDegree());
                }
                loadCaptions(captions);
            }
        });
    }

    private void loadCaptions(List<String> captions){
        captions.add(context.getString(R.string.publicCaption));
        for (final String caption : captions){
            final TextView textView = new TextView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(10,5,10,0);
            textView.setLayoutParams(params);
            textView.setPadding(20,20,20,20);
            textView.setBackground(context.getDrawable(R.drawable.caption_background_white));
            textView.setText(caption);
            captionContainer.addView(textView);

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                     if (textView.getBackground().getConstantState().equals(getResources().getDrawable(R.drawable.caption_background_white).getConstantState())){
                         enableCaption(textView);
                     } else {
                         disableCaption(textView);
                     }
                }
            });
        }
    }

    private void disableCaption(TextView textView) {
        textView.setTextColor(getResources().getColor(android.R.color.darker_gray));
        textView.setBackground(context.getDrawable(R.drawable.caption_background_white));
        adapter.changeCaption("");
        refreshList();
    }

    private void enableCaption(TextView selectedText) {
        for (int i = 0; i < captionContainer.getChildCount(); i++){
            disableCaption((TextView) captionContainer.getChildAt(i));
        }

        selectedText.setTextColor(getResources().getColor(R.color.colorWhite));
        selectedText.setBackground(context.getDrawable(R.drawable.caption_background_green));
        if (selectedText.getText().toString().contains(user.getDegree())){
            adapter.changeCaption(selectedText.getText().toString().concat(user.getStudyYear()));
        } else {
            adapter.changeCaption(selectedText.getText().toString());
        }
        refreshList();
    }

    private void refreshList() {
        RecyclerView.LayoutManager layoutManager = listView.getLayoutManager();
        listView.setAdapter(null);
        listView.setLayoutManager(null);
        listView.setAdapter(adapter);
        listView.setLayoutManager(layoutManager);
        adapter.notifyDataSetChanged();
    }

    private void initializeVariables() {
        captionContainer = getView().findViewById(R.id.captionContainer);
        addNews = getView().findViewById(R.id.addNews);
        addNews.setOnClickListener(this);
        filterNews = getView().findViewById(R.id.filterNews);
        filterNews.setOnItemSelectedListener(this);

        swipeRefreshLayout = getView().findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setOnRefreshListener(this);

        listView = getView().findViewById(R.id.newsList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true); //show newest first
        listView.setLayoutManager(layoutManager);
        loadData(DatabaseHelper.newsReference);

        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState){
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        addNews.setVisibility(View.GONE);
                        break;
                    case RecyclerView.SCROLL_STATE_IDLE:
                        addNews.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        searchView = getView().findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(this);
    }

    private void loadData(Query q){
        newsList = new FirebaseRecyclerOptions.Builder<News>().setQuery(q, News.class).build();
        adapter = new NewsListAdapter(newsList,null,context,Utils.NEWS_FRAGMENT);
        listView.setAdapter(adapter);
        adapter.startListening();
    }


    @Override
    public void onClick(View v) {
        startActivity(new Intent(getActivity(), AddNewsActivity.class));
    }

   @Override
    public boolean onQueryTextSubmit(final String search) {
        if (!search.isEmpty()){
            adapter.changeSearchQuery(search);
            refreshList();
        } else {
            adapter.changeSearchQuery("");
            refreshList();
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(final String newText) {
        if (newText.length() > 0){
            adapter.changeSearchQuery(newText);
            refreshList();
        } else {
            adapter.changeSearchQuery("");
            refreshList();
        }
        return true;
    }


    @Override
    public void onRefresh() {
        super.onResume();
        if (adapter != null){
            adapter.startListening();
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        listView.smoothScrollToPosition(adapter.getItemCount());
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getItemAtPosition(position).toString().equals(getString(R.string.lastPost))){
            loadData(DatabaseHelper.newsReference);
        } else {
            loadData(DatabaseHelper.newsReference.orderByChild("lastComment"));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        loadData(DatabaseHelper.newsReference);
    }
}
