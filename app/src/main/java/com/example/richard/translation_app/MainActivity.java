package com.example.richard.translation_app;


       import android.app.Activity;
       import android.content.Context;
       import android.os.AsyncTask;
        import android.os.Bundle;
       import android.view.KeyEvent;
       import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
       import android.view.inputmethod.InputMethodManager;
       import android.widget.EditText;
        import android.widget.TextView;
        import android.widget.Toast;

        import org.apache.http.HttpEntity;
        import org.apache.http.HttpResponse;
        import org.apache.http.client.ClientProtocolException;
        import org.apache.http.client.methods.HttpPost;
        import org.apache.http.impl.client.DefaultHttpClient;
        import org.apache.http.params.BasicHttpParams;
        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;

        import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.io.UnsupportedEncodingException;
        import java.net.MalformedURLException;

public class MainActivity extends Activity implements View.OnKeyListener {

    EditText translateEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        translateEditText = (EditText)findViewById(R.id.editText);
        translateEditText.setOnKeyListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Calls for the AsyncTask to execute when the translate button is clicked
    public void onTranslateClick(View view) {

        EditText translateEditText = (EditText) findViewById(R.id.editText);

        // If the user entered words to translate then get the JSON data
        if(!isEmpty(translateEditText)){

           // message("In mainactivity Getting Translations, calling SavetheFeed method");


            // Calls for the method doInBackground to execute
            new SaveTheFeed().execute();

        } else {

            // Post an error message if they didn't enter words
            message("Enter an English Word to Translate");

        }

        // This block of code hides the Input method(android keypad) when the user
        // clicks on translate NNNNNNNNNNNNNNNNNBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB
        InputMethodManager inputManager =
                (InputMethodManager)this.
                        getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(
                this.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);

    }// End onTranslateClick

    public void message(String s) {
        Toast.makeText(this, s,
                Toast.LENGTH_SHORT).show();
    }

    // Check if the user entered words to translate
    // Returns false if not empty
    protected boolean isEmpty(EditText editText){

        // Get the text in the EditText convert it into a string, delete whitespace
        // and check length
        return editText.getText().toString().trim().length() == 0;

    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {

        if (keyCode==KeyEvent.KEYCODE_ENTER && event.getAction()==KeyEvent.ACTION_DOWN) {

           onTranslateClick(v);

            //}}}
        }
        return false;
    }


    // Allows you to perform background operations without locking up the user interface
    // until they are finished
    // The void part is stating that it doesn't receive parameters, it doesn't monitor progress
    // and it won't pass a result to onPostExecute
    class SaveTheFeed extends AsyncTask<Void, Void, Void>{

        // Holds JSON data in String format
        String jsonString = "";

        // Will hold the translations that will be displayed on the screen
        String result = "";
        String wordsToTranslate;// This needs to be defined here since it's accessed
        //from multiple asynch methods

        @Override
        protected void onPreExecute(){
            // Get access to the EditText so we can get the text in it
            message("Starting search");
            EditText translateEditText = (EditText) findViewById(R.id.editText);

            // Get the text from EditText
            wordsToTranslate =translateEditText.getText().toString();

        }

        // Everything that should execute in the background goes here
        // You cannot edit the user interface from this method
        @Override
        protected Void doInBackground(Void... voids) {

            // Replace spaces in the String that was entered with + so they can be passed
            // in a URL
             wordsToTranslate = wordsToTranslate.replace(" ", "+");

            // Client used to grab data from a provided URL
            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());

            // Provide the URL for the post request
            HttpPost httpPost = new HttpPost("http://217.199.187.193/risteardobdomain.om/" +
                    "translateme.php?action=translate&english_words="+wordsToTranslate+"&language=german");

           // "http://newjustin.com/translateit.php?action=translations&english_words=" + wordsToTranslate
           // "http://217.199.187.193/risteardobdomain.om/translateme.php?action=translate&english_words="+wordsToTranslate+"&language=german"

            // Define that the data expected is in JSON format
            httpPost.setHeader("Content-type", "application/json");

            // Allows you to input a stream of bytes from the URL
            InputStream inputStream = null;

            try{

                // The client calls for the post request to execute and sends the results back
                HttpResponse response = httpClient.execute(httpPost);

                // Holds the message sent by the response
                HttpEntity entity = response.getEntity();

                // Get the content sent
                inputStream = entity.getContent();

                // A BufferedReader is used because it is efficient
                // The InputStreamReader converts the bytes into characters
                // My JSON data is UTF-8 so I read that encoding
                // 8 defines the input buffer size
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);

                // Storing each line of data in a StringBuilder
                StringBuilder sb = new StringBuilder();

                String line = null;

                // readLine reads all characters up to a \n and then stores them
                while((line = reader.readLine()) != null){

                    sb.append(line + "\n");

                }

                // Save the results in a String
                jsonString = sb.toString();

                // Create a JSONObject by passing the JSON data
                JSONObject jObject = new JSONObject(jsonString);

                // Get the Array named translations that contains all the translations
                JSONArray jArray = jObject.getJSONArray("translations");

                // Cycles through every translation in the array
                outputTranslations(jArray);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        // Called after doInBackground finishes executing
        @Override
        protected void onPostExecute(Void aVoid) {

            // Put the translations in the TextView
            TextView translationTextView = (TextView) findViewById(R.id.translationTextView);
            if(result.length()!=9) {
                translationTextView.setText(result);
            }
            //message("i'm in post-execute");
            else{
                translationTextView.setText("Sorry but we couldn't get that word. Please check your spelling and try again.");
            }

        }

        protected void outputTranslations(JSONArray jsonArray){


            // Used to get the translation using a key
           /* String[] languages = {"arabic", "chinese", "danish", "dutch",
                    "french", "german", "italian", "portuguese", "russian",
                    "spanish"};  */
            String[] languages = {"german"};

            // Save all the translations by getting them with the key
            try{

                for(int i = 0; i < jsonArray.length(); i++){

                    JSONObject translationObject =
                            jsonArray.getJSONObject(i);

                    result = result + languages[i] + " : " +
                            translationObject.getString(languages[i]);
                    // Removed '+"\n"' above. Don't need new line

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }// End inner SaveTheFeed Class

}