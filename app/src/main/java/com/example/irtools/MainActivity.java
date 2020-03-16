package com.example.irtools;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import edu.stanford.nlp.simple.Sentence;

public class MainActivity extends AppCompatActivity implements ResultAdapter.OnItemClicked {
    //front-end
    private RecyclerView resultRecyclerView;
    private ResultAdapter resultAdapter;
    private LinearLayoutManager llm;
    private GridLayoutManager glm;
    private SearchView searchView;
    private ProgressBar progressBar;
    private AppBarLayout appBarLayout;
    private FloatingActionButton fab;
    private FloatingActionButton fab_change;

    //back-end
    private StitchAppClient stitchClient;
    private RemoteMongoClient mongoClient;
    private RemoteMongoCollection tokenListCollection;
    private RemoteMongoCollection docMetadataCollection;
    private JSONObject bookkeeppingJson;

    private ArrayList<Result> results = new ArrayList<Result>();
    private boolean linearNow = true;
    private ArrayList<String> specialTags = new ArrayList<>(Arrays.asList("title", "h1", "h2",
            "h3", "h4", "h5", "h6", "strong", "bold", "em"));
    private ArrayList<String> headingTags = new ArrayList<>(Arrays.asList("h2",
            "h3"));
    private ArrayList<String> styleTags = new ArrayList<>(Arrays.asList("h4", "h5", "h6",
            "strong", "bold", "em"));

    private final double TITLE_WEIGHT = 1;
    private final double H1_WEIGHT = 0.8;
    private final double HEADING_WEIGHT = 0.5;
    private final double STYLE_WEIGHT = 0.3;

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
        fab_change = findViewById(R.id.fab_change);

        resultRecyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(this, "Greatttt! You found me!!!!!", Toast.LENGTH_LONG).show();
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

        fab_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swapRecyclerLayout();
            }
        });
        establishConnectiontoDatabase();

        //load bookkeeppingJson
        try {
            bookkeeppingJson = new JSONObject(loadJSONFromAsset(this));
        } catch (Exception e) {
            Log.e("IRtool", e.toString());
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

                for (int i = 0; i < words.size(); i++) {
                    String word = words.get(i);
                    final int temp = i;
                    Document query = new Document().append("Token",
                            word);
                    final Task<List<Document>> findOneAndUpdateTask = tokenListCollection.find(query).limit(1)
                            .into(new ArrayList<Document>());
                    findOneAndUpdateTask.addOnCompleteListener(new OnCompleteListener<List<Document>>() {
                        @Override
                        public void onComplete(@androidx.annotation.NonNull Task<List<Document>> task) {
                            Log.d("IRtool", String.format("Successfully found document: %s",
                                    task.getResult()));
                            try {
                                JSONObject reader = new JSONObject(task.getResult().get(0).toJson());
                                JSONArray docIdWtJsonArray = reader.getJSONArray("DocId_wt");

                                for (int i = 0; i < docIdWtJsonArray.length(); i++) {
                                    JSONObject object = docIdWtJsonArray.getJSONObject(i);
                                    Iterator<String> keysIter = object.keys();
                                    String key = keysIter.next();

                                    int index = checkDocIdForResult(key, results);
                                    //no results found
                                    if (index == -1)
                                        results.add(new Result(bookkeeppingJson.getString(key), key
                                                , object.getDouble(key), object.getDouble(key)));
                                    else
                                        results.get(index).setTf_idf(results.get(index).getTf_idf() + object.getDouble(key));
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d("IRtool", "Failed to caculate doc_length, query_length, cosine ( " + e.toString() + ")");
                            }
                            if (temp == words.size() - 1) {
                                Collections.sort(results, new Comparator<Result>() {
                                    @Override
                                    public int compare(Result res1, Result res2) {
                                        //ascending order
                                        //return Double.valueOf(res1.getTf_idf()).compareTo(res2.getTf_idf());

                                        //descending order
                                        return Double.valueOf(res2.getTf_idf()).compareTo(res1.getTf_idf());
                                    }
                                });
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

        resultAdapter = new ResultAdapter(this, results, linearNow);
        resultAdapter.setOnClick(this);
        resultRecyclerView.setAdapter(resultAdapter);
        llm = new LinearLayoutManager(this);
        glm = new GridLayoutManager(this, 2);
        resultRecyclerView.setLayoutManager(llm);
    }

    //    Calculate the tf-idf of each term in the query
//    params: list of strings each of which is a term in the user query
//    return: a hasmap with the term (string) being the key and its tf-idf being the value
    private Map<String, Double> getWtq(List<String> words) {
        Iterator<String> iter = words.iterator();
        Map<String, Double> result = new HashMap<>();
        while (iter.hasNext()) {
            String token = iter.next();
            if (!result.containsKey(token))
                result.put(token, 1.0);
            else
                result.put(token, result.get(token) + 1);
        }
        for (Map.Entry<String, Double> entry : result.entrySet())
            entry.setValue(1 + Math.log10(entry.getValue()));
        return result;
    }

    // change the layout from list to grid and vice versa
    public void swapRecyclerLayout() {
        appBarLayout.setExpanded(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (linearNow) {
                    resultRecyclerView.setLayoutManager(glm);
                    fab_change.setImageResource(R.drawable.ic_action_format_list_numbered);
                } else {
                    resultRecyclerView.setLayoutManager(llm);
                    fab_change.setImageResource(R.drawable.ic_action_grid_on);
                }

                linearNow = !linearNow;

                resultAdapter = new ResultAdapter(getApplicationContext(), results, linearNow);
                resultAdapter.setOnClick(MainActivity.this::onItemClick);
                resultRecyclerView.setAdapter(resultAdapter);
                appBarLayout.setExpanded(false);
                resultAdapter.notifyDataSetChanged();
                resultRecyclerView.scheduleLayoutAnimation();
            }
        }, 500);

    }

    //    load bookkeepping json file from asset
//    params: context to load from asset
//    return: json in string format
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


    public int checkDocIdForResult(String doc_id, ArrayList<Result> results) {
        for (int i = 0; i < results.size(); i++)
            if (results.get(i).getDocId().equals(doc_id))
                return i;
        return -1;
    }

    // connect to the database
    public void establishConnectiontoDatabase() {
        stitchClient = Stitch.getDefaultAppClient();
        Stitch.getDefaultAppClient().getAuth().loginWithCredential(new AnonymousCredential())
                .addOnCompleteListener(new OnCompleteListener<StitchUser>() {
                    @Override
                    public void onComplete(@NonNull final Task<StitchUser> task) {
                        if (task.isSuccessful()) {
                            Log.d("IRtool", "logged in anonymously");
                        } else {
                            Log.e("IRtool", "failed to log in anonymously", task.getException());
                        }
                    }
                });

        mongoClient = stitchClient.getServiceClient(RemoteMongoClient.factory, "mongodb-atlas");
        tokenListCollection = mongoClient.getDatabase(getString(R.string.database)).getCollection(getString(R.string.tokenListCollection));
        docMetadataCollection = mongoClient.getDatabase(getString(R.string.database)).getCollection(getString(R.string.docMetadataCollection));

    }


    @Override
    public void onItemClick(View v, int position) {
        String[] tags = ((String) v.getTag()).split("\\|");
        String url = tags[0];

        Intent intent = new Intent(MainActivity.this, WebDisplayer.class);
        intent.putExtra("url", url);
        intent.putExtra("position", position);
        intent.putExtra("value", Double.parseDouble(tags[1]));
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this,
                Pair.create(v.findViewById(Integer.parseInt(tags[2])), getString(R.string.transition_to_web_position)),
                Pair.create(v.findViewById(Integer.parseInt(tags[3])), getString(R.string.transition_to_web_tfidf)));
        startActivity(intent, options.toBundle());
    }
}
