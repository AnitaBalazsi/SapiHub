package com.example.sapihub.Fragments;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.sapihub.Activities.AddNewsActivity;
import com.example.sapihub.Activities.NewsDetailsActivity;
import com.example.sapihub.Activities.UserProfileActivity;
import com.example.sapihub.Helpers.Database.DatabaseHelper;
import com.example.sapihub.Helpers.Adapters.NewsListAdapter;
import com.example.sapihub.Helpers.Database.FirebaseCallback;
import com.example.sapihub.Helpers.Utils;
import com.example.sapihub.Model.Comment;
import com.example.sapihub.Model.News;
import com.example.sapihub.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFragment extends Fragment implements View.OnClickListener, NewsListAdapter.ListViewHolder.NewsClickListener, SearchView.OnQueryTextListener{
    private NewsListAdapter adapter;
    private ProgressDialog loadingDialog;
    private ImageView addNews;
    private ArrayList<News> newsList = new ArrayList<>();
    private SearchView searchView;

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

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    private void initializeVariables() {
        addNews = getView().findViewById(R.id.addNews);
        addNews.setOnClickListener(this);

        RecyclerView listView = getView().findViewById(R.id.newsList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true); //show newest first

        listView.setLayoutManager(layoutManager);

        adapter = new NewsListAdapter(null,newsList,getContext(),this);
        listView.setAdapter(adapter);
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

        loadingDialog = new ProgressDialog(getContext(), R.style.ProgressDialog);
        loadingDialog.setMessage(getString(R.string.loading));

        searchView = getView().findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(this);
    }

    private void getData(){
        DatabaseHelper.newsReference.orderByChild("date").addValueEventListener(new ValueEventListener() {
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
        startActivity(new Intent(getActivity(), AddNewsActivity.class));
    }


    @Override
    public void onNewsClick(int position, String tag) {
        Intent openDetails = new Intent(getActivity(), NewsDetailsActivity.class);
        openDetails.putExtra("selectedNews", newsList.get(position));
        startActivity(openDetails);
    }

    @Override
    public void onProfileClick(int position) {
        String author = newsList.get(position).getAuthor();
        if (author.equals(Utils.getCurrentUserToken(getContext()))){
            ViewPager viewPager = getActivity().findViewById(R.id.viewpager);
            viewPager.setCurrentItem(3);
        } else {
            Intent profileIntent = new Intent(getActivity(),UserProfileActivity.class);
            profileIntent.putExtra("userId",newsList.get(position).getAuthor());
            startActivity(profileIntent);
        }
    }

    @Override
    public void onMoreOptionsClick(View itemView, final int position) {
        PopupMenu popupMenu = new PopupMenu(getContext(), itemView.findViewById(R.id.moreImage));
        getActivity().getMenuInflater().inflate(R.menu.post_options_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (String.valueOf(item.getTitle()).equals(getActivity().getString(R.string.deletePost))){
                    showConfirmDeleteDialog(position);
                } else {
                    //todo
                }
                return true;
            }
        });
        popupMenu.show();
    }

    private void deletePost(News news) {
        DatabaseHelper.deleteNews(news, new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
              if ((Boolean) object){
                  newsList.remove(object);
                  adapter.notifyDataSetChanged();
              }
            }
        });
    }

    private void showConfirmDeleteDialog(final int position) {
        new AlertDialog.Builder(getContext(),R.style.AlertDialogTheme)
                .setTitle(getString(R.string.deletePost))
                .setMessage(getString(R.string.confirmDeleteNews))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        deletePost(newsList.get(position));
                    }})
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    @Override
    public void onSavePost(final View itemView, final int position) {
        final ImageView savePostImage = itemView.findViewById(R.id.savePost);
        DatabaseHelper.getNewsKey(newsList.get(position), new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                final String newsKey = (String) object;
                DatabaseHelper.isSaved(Utils.getCurrentUserToken(getContext()), newsKey, new FirebaseCallback() {
                    @Override
                    public void onCallback(Object object) {
                        if ((Boolean) object){
                            //if saved
                            deletePostFromSaved(savePostImage,position,newsKey);
                        } else {
                            addPostToSaved(savePostImage,position,newsKey);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onWriteComment(View itemView, int position) {
        LinearLayout commentLayout = itemView.findViewById(R.id.commentLayout);
        if (commentLayout.getVisibility() == View.VISIBLE){
            commentLayout.setVisibility(View.GONE);
        } else {
            commentLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSendComment(View itemView, int position, String tag) {
        final EditText commentInput = itemView.findViewById(R.id.commentInput);
        if (commentInput.getText().toString().isEmpty()){
            commentInput.setError(getString(R.string.emptyField));
            commentInput.requestFocus();
        } else {
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            final Comment comment = new Comment(Utils.getCurrentUserToken(getContext()),date,commentInput.getText().toString().trim());
            DatabaseHelper.getNewsKey(newsList.get(position), new FirebaseCallback() {
                @Override
                public void onCallback(Object object) {
                    DatabaseHelper.addComment((String) object,comment);
                    commentInput.setText(null);
                }
            });
        }
    }

    private void deletePostFromSaved(final ImageView savePostImage, int position, String newsKey) {
        DatabaseHelper.deleteSavedPost(Utils.getCurrentUserToken(getContext()), newsKey, new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                if ((Boolean) object){
                    Toast.makeText(getContext(),getString(R.string.postRemovedFromSaved),Toast.LENGTH_SHORT).show();
                    savePostImage.setImageDrawable(getContext().getDrawable(R.drawable.ic_star_border));
                }
            }
        });
    }

    private void addPostToSaved(final ImageView savePostImage, int position, String newsKey) {
        DatabaseHelper.savePost(Utils.getCurrentUserToken(getContext()), newsKey, newsList.get(position).getTitle(), new FirebaseCallback() {
            @Override
            public void onCallback(Object object) {
                if ((Boolean) object){
                    Toast.makeText(getContext(),getString(R.string.postSaved),Toast.LENGTH_SHORT).show();
                    savePostImage.setImageDrawable(getContext().getDrawable(R.drawable.ic_star));
                }
            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(final String query) {
        if (!query.isEmpty()){
            newsList.clear();
            DatabaseHelper.newsReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot newsData : dataSnapshot.getChildren()){
                        News news = newsData.getValue(News.class);
                        if (news.getTitle().contains(query) || news.getContent().contains(query)){
                            newsList.add(news);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            getData();
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(final String newText) {
        if (newText.length() > 0){
            newsList.clear();
            DatabaseHelper.newsReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot newsData : dataSnapshot.getChildren()){
                        News news = newsData.getValue(News.class);
                        if (news.getTitle().contains(newText) || news.getContent().contains(newText)){
                            newsList.add(news);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            getData();
        }
        return true;
    }


}
