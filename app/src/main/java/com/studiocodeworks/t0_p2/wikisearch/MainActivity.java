package com.studiocodeworks.t0_p2.wikisearch;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONObject;

import com.studiocodeworks.products.mobilemerchant.*;

import java.net.URLEncoder;


public class MainActivity extends ActionBarActivity implements IDataReceivedHandler, View.OnClickListener
{
    private static final String GOOGLE_SEARCH_ENDPOINT = "https://www.googleapis.com/customsearch/v1";
    private static final String GOOGLE_SEARCH_API_KEY  = "AIzaSyC6m-rxkjHxrBObzGmx2CEb64c-12VWW6I"; // @"AIzaSyBs7unIlIWBJqljYbfzss9kNYSZ5jKpSO0"
    private static final String GOOGLE_SEARCH_ENGINE_ID= "001296046484476294385:zq7iayl0vl4";

    // we may be using a service that tracks sessions, create a store for session cookies
    private CookieStore     m_cookieStore               = null;
    // we'll need an http context to do anything really
    private HttpContext     m_httpContext               = null;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // initialize our cookier store and http context
        m_cookieStore   = new BasicCookieStore();
        m_httpContext   = new BasicHttpContext();
        m_httpContext.setAttribute(ClientContext.COOKIE_STORE, m_cookieStore);
        Button searchButton = (Button)findViewById(R.id.button_search);
        searchButton.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void search(String stuffToSearchFor)
    {
        // before we launch a long web search, show a spinner
        ProgressBar progressBar = (ProgressBar)findViewById(R.id.progress_loading);
        progressBar.setVisibility(View.VISIBLE);

        SCAsyncGET asyncGet = new SCAsyncGET();
        asyncGet.setDataHandler(this);
        asyncGet.setExpectingSingleResult(true);
        asyncGet.setHttpContext(m_httpContext);
        // we need to set up the target url and parameters as an array of parameters
        // key/value after target url
        // we set this up when we call execute
        asyncGet.execute(GOOGLE_SEARCH_ENDPOINT,    // this is the URL we will hit
                "key",                              // this is the 'key' parameter marker in the request
                GOOGLE_SEARCH_API_KEY,              // this is the API key that allows us to use this service
                "cx",                               // this is the application ID for search
                GOOGLE_SEARCH_ENGINE_ID,            // this is the actual ID
                "q",                                // the 'query' parameter in the url
                stuffToSearchFor);                  // this is the actual text we want to search for
        // execute will return immediately
        // the results will be delivered back to us
        // the url will look like this
        // https://www.googleapis.com/customsearch/v1?key=<somekeyvalues>&cx=<someidvalues>&query=Bill%20Gates
    }

    @Override
    public void dataReceived(SCAsyncGET request)
    {
        // we got some stuff
        // take the top most one
        try
        {
            JSONObject jobj = request.getResultObject();
            // pull out the items array
            // get the first one
            JSONArray items = jobj.getJSONArray("items");
            JSONObject item = items.getJSONObject(0);
            // pull out the link object
            String link = item.getString("link");
            // we want to load a new activity with window here
            // create an intent to display our web activity
            Intent intent = new Intent(this, WebActivity.class);
            // put the url in the parameter
            intent.putExtra("web-url", link);
            // display the window by starting the activity
            startActivity(intent);
            // turn off the progress bar
            ProgressBar progressBar = (ProgressBar)findViewById(R.id.progress_loading);
            progressBar.setVisibility(View.GONE);
        }
        catch(Exception ex)
        {
            Log.e("MainActivity", "An error occurred while processing JSON: " + ex);
        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v)
    {
        if(v.getId() == R.id.button_search)
        {
            // get the string from the search box
            EditText searchEditText = (EditText)findViewById(R.id.edittext_search_query);
            String   text = searchEditText.getText().toString();

            try
            {
                // url encode the text before sending
                text = URLEncoder.encode(text, "UTF-8");
                search(text);
            }
            catch (Exception ex)
            {
                Log.e("MainActivity", "An error occurred while encoding search text: " + text + ", Error: " + ex);
            }
        }
    }
}
