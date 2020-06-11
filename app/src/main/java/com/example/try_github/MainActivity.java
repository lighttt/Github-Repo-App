package com.example.try_github;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.try_github.utilities.NetworkUtils;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {

    private EditText mSearchBoxEditText;

    private TextView mUrlDisplayTextView;

    private TextView mSearchResultsTextView;
    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;

    private static final String SEARCH_QUERY_URL = "searchUrl";
    private static final String SEARCH_JSON_DATA = "searchJsonData";

    private static final int GITHUB_SEARCH_LOADER = 22;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSearchBoxEditText =  findViewById(R.id.et_search_box);

        mUrlDisplayTextView = findViewById(R.id.tv_url_display);
        mSearchResultsTextView = findViewById(R.id.tv_github_search_results_json);

        mErrorMessageDisplay = findViewById(R.id.tv_error_message_display);
        mLoadingIndicator =  findViewById(R.id.pb_loading_indicator);

        // Getting data from bundle
        if(savedInstanceState!=null)
        {
            String queryUrl = savedInstanceState.getString(SEARCH_QUERY_URL);
            String jsonData = savedInstanceState.getString(SEARCH_JSON_DATA);

            mUrlDisplayTextView.setText(queryUrl);
            mSearchResultsTextView.setText(jsonData);
        }
        //Initialize the loader
        getSupportLoaderManager().initLoader(GITHUB_SEARCH_LOADER,null,this);

    }

    // ------------- ASYNC FUNCTIONS ----------------------------

    private void makeGithubSearchQuery() {
        mSearchResultsTextView.setText("");
        String githubQuery = mSearchBoxEditText.getText().toString();
        URL githubSearchUrl = NetworkUtils.buildUrl(githubQuery);
        mUrlDisplayTextView.setText(githubSearchUrl.toString());

        Bundle queryBundle = new Bundle();
        queryBundle.putString(SEARCH_QUERY_URL,githubSearchUrl.toString());

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> githubSearchLoader = loaderManager.getLoader(GITHUB_SEARCH_LOADER);
        if(githubSearchLoader == null)
        {
            loaderManager.initLoader(GITHUB_SEARCH_LOADER,queryBundle,this);
        }
        else{
            loaderManager.restartLoader(GITHUB_SEARCH_LOADER,queryBundle,this);
        }
    }

    private void showJsonDataView() {
        // First, make sure the error is invisible
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        // Then, make sure the JSON data is visible
        mSearchResultsTextView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        // First, hide the currently visible data
        mSearchResultsTextView.setVisibility(View.INVISIBLE);
        // Then, show the error
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    // ------------- ASYNC TASK LOADER ----------------------------

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String>(this) {

            String mGithubJson;

            @Override
            protected void onStartLoading() {
               if(args==null)
               {
                   return;
               }
                if (mGithubJson != null) {
                    deliverResult(mGithubJson);
                }else{
                    mLoadingIndicator.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            @Nullable
            @Override
            public String loadInBackground() {
                String searchQueryUrl = args.getString(SEARCH_QUERY_URL);
                if(searchQueryUrl==null || TextUtils.isEmpty(searchQueryUrl))
                {
                    return null;
                }
                try {
                    URL searchUrl = new URL(searchQueryUrl);
                    String githubSearchResults = NetworkUtils.getResponseFromHttpUrl(searchUrl);
                    return  githubSearchResults;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(@Nullable String githubJSon) {
                mGithubJson = githubJSon;
                super.deliverResult(githubJSon);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if (data != null && !data.equals("")) {
            showJsonDataView();
            mSearchResultsTextView.setText(data);
        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }

    // ------------- MENUS ----------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();
        if (itemThatWasClickedId == R.id.action_search) {
            makeGithubSearchQuery();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // ------------- SAVING INSTANCE ----------------------------

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        String queryUrl = mUrlDisplayTextView.getText().toString();
        String jsonData = mSearchResultsTextView.getText().toString();

        outState.putString(SEARCH_QUERY_URL,queryUrl);
        outState.putString(SEARCH_JSON_DATA,jsonData);
    }
}

