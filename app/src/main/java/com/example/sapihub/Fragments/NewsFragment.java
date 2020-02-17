package com.example.sapihub.Fragments;


import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.sapihub.Activities.FragmentLoader;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Adapters.NewsListAdapter;
import com.example.sapihub.Model.News;
import com.example.sapihub.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFragment extends Fragment implements View.OnClickListener, NewsListAdapter.ListViewHolder.NewsClickListener {
    private NewsListAdapter adapter;
    private ProgressDialog loadingDialog;
    private Button addNewsButton;
    private ArrayList<News> newsList = new ArrayList<>();

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
        initializeVariables();
        loadingDialog.show();
        getData();
    }

    private void initializeVariables() {
        addNewsButton = getView().findViewById(R.id.addNews);
        addNewsButton.setOnClickListener(this);

        RecyclerView listView = getView().findViewById(R.id.newsList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        listView.setLayoutManager(layoutManager);
        listView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));

        adapter = new NewsListAdapter(newsList,getContext(),this);
        listView.setAdapter(adapter);

        loadingDialog = new ProgressDialog(getContext(), R.style.ProgressDialog);
        loadingDialog.setMessage(getString(R.string.loading));
    }

    private void getData(){
        DatabaseHelper.newsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                newsList.clear();
                for (DataSnapshot newsData : dataSnapshot.getChildren()){
                    News news = newsData.getValue(News.class);
                    newsList.add(news);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        loadingDialog.dismiss();
    }

    @Override
    public void onClick(View v) {
        ((FragmentLoader)getActivity()).replaceFragment(new AddNewsFragment(),null);
    }

    @Override
    public void onNewsClick(int position) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("selectedNews",newsList.get(position));
        ((FragmentLoader)getActivity()).replaceFragment(new NewsDetailFragment(),bundle);
    }
}
