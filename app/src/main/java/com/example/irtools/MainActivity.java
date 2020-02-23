package com.example.irtools;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mongodb.lang.NonNull;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.auth.providers.anonymous.AnonymousCredential;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import edu.stanford.nlp.simple.Sentence;

public class MainActivity extends AppCompatActivity {
    //front-end
    private RecyclerView resultRecyclerView;
    private ResultAdapter resultAdapter;
    private LinearLayoutManager llm;
    private SearchView searchView;
    private ProgressBar progressBar;
    private AppBarLayout appBarLayout;
    private FloatingActionButton fab;

    //back-end
    private StitchAppClient stitchClient;
    private RemoteMongoClient mongoClient;
    private RemoteMongoCollection mCollection;
    private JSONObject bookkeeppingJson;

    private ArrayList<Result> results = new ArrayList<Result>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //transparent status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        resultRecyclerView = findViewById(R.id.resultRecyclerView);
        searchView = findViewById(R.id.searchView);
        progressBar = findViewById(R.id.progressbar);
        appBarLayout = findViewById(R.id.appBarLayout);
        fab = findViewById(R.id.fab);

        progressBar.setVisibility(View.INVISIBLE);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
                if (offset <= -200)
                    fab.show();
                else
                    fab.hide();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appBarLayout.setExpanded(true);
                searchView.onActionViewExpanded();
                searchView.setIconified(false);

            }
        });

        establishConnectiontoDatabase();

        //load bookkeeppingJson
        try {
            bookkeeppingJson = new JSONObject(loadJSONFromAsset(this));
        } catch (Exception e) {
            Log.e("IRtools", e.toString());
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String userQuery) {
                progressBar.setVisibility(View.VISIBLE);
                searchView.clearFocus();
                results.clear();
                resultAdapter.notifyDataSetChanged();

                Sentence sen = new Sentence(userQuery.toLowerCase());
                List<String> words = sen.lemmas();
                for (String word : words) {
                    Document query = new Document().append("Token",
                            word);
                    final Task<List<Document>> findOneAndUpdateTask = mCollection.find(query).limit(1)
                            .into(new ArrayList<Document>());
                    findOneAndUpdateTask.addOnCompleteListener(new OnCompleteListener<List<Document>>() {
                        @Override
                        public void onComplete(@androidx.annotation.NonNull Task<List<Document>> task) {
                            Log.d("IRtools", String.format("Successfully found document: %s",
                                    task.getResult()));
                            try {
                                JSONObject reader = new JSONObject(task.getResult().get(0).toJson());
                                JSONArray metadata = reader.getJSONArray("Metadata");

                                for (int i = 0; i < metadata.length(); i++) {
                                    JSONObject object = metadata.getJSONObject(i);
                                    Iterator<String> iter = object.keys();
                                    String key = iter.next();

                                    int index = checkDocIdForResult(key, results);
                                    //no results found
                                    if (index == -1)
                                        results.add(new Result(bookkeeppingJson.getString(key), key
                                                , object.getDouble(key), object.getDouble(key)));
                                    else
                                        results.get(index).setTf_idf(results.get(index).getTf_idf() + object.getDouble(key));
                                }
                                Collections.sort(results, new Comparator<Result>() {
                                    @Override
                                    public int compare(Result res1, Result res2) {
                                        //ascending order
                                        //return Double.valueOf(res1.getTf_idf()).compareTo(res2.getTf_idf());

                                        //descending order
                                        return Double.valueOf(res2.getTf_idf()).compareTo(res1.getTf_idf());
                                    }
                                });

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            //shrink the list
                            if (results.size() > 20)
                                results.subList(20, results.size()).clear();
                            //update the view
                            if (results.size() > 6)
                                appBarLayout.setExpanded(false);
                            progressBar.setVisibility(View.INVISIBLE);
                            resultAdapter.notifyDataSetChanged();
                            resultRecyclerView.scheduleLayoutAnimation();
                        }
                    });
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        resultAdapter = new ResultAdapter(this, R.layout.one_result_item, results);
        resultRecyclerView.setAdapter(resultAdapter);
        llm = new LinearLayoutManager(this);
        resultRecyclerView.setLayoutManager(llm);
    }

    public String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("bookkeeping.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    //find which result already contains doc_id. If found return index; otherwise, return -1
    public int checkDocIdForResult(String doc_id, ArrayList<Result> results) {
        for (int i = 0; i < results.size(); i++)
            if (results.get(i).getDocId().equals(doc_id))
                return i;
        return -1;
    }

    public void establishConnectiontoDatabase() {
        stitchClient = Stitch.initializeDefaultAppClient(getString(R.string.app_id));
        Stitch.getDefaultAppClient().getAuth().loginWithCredential(new AnonymousCredential())
                .addOnCompleteListener(new OnCompleteListener<StitchUser>() {
                    @Override
                    public void onComplete(@NonNull final Task<StitchUser> task) {
                        if (task.isSuccessful()) {
                            Log.d("IRtools", "logged in anonymously");
                        } else {
                            Log.e("IRtools", "failed to log in anonymously", task.getException());
                        }
                    }
                });

        mongoClient = stitchClient.getServiceClient(RemoteMongoClient.factory, "mongodb-atlas");
        mCollection = mongoClient.getDatabase(getString(R.string.database)).getCollection(getString(R.string.collection));
    }
}
