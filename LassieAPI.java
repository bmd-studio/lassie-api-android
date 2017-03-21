package nl.moeilijkedingen.lassie;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

class LassieAPI {

    private static final String TAG = "LassieAPI";

    private RequestQueue queue;
    Context context;
    private SharedPreferences apiStorage;

    // Initialize API-data holders
    private String lassie_api_url;
    private String lassie_model_key;
    private String lassie_model_secret;
    private String lassie_person_key;
    private String lassie_person_secret;
    String[] accountName = new String[2];

    /**
     * Initiate a new LassieAPI.
     * @param current The context of this class, almost the current Activity
     * @param requestQueue A RequestQueue to which the requests are added
     */
    LassieAPI(Context current, RequestQueue requestQueue) {
        this.queue = requestQueue;
        this.context = current;

        // Get strings form XMLs
        this.lassie_api_url = context.getResources().getString(R.string.api_address);
        this.lassie_person_key = context.getResources().getString(R.string.person_api_key);
        this.lassie_person_secret = context.getResources().getString(R.string.person_api_secret);
        this.lassie_model_key = context.getResources().getString(R.string.api_model_key);
        this.lassie_model_secret = context.getResources().getString(R.string.api_model_secret);
        this.accountName[0] = context.getResources().getString(R.string.first_account_identifier);
        this.accountName[1] = context.getResources().getString(R.string.second_account_identifier);

        // Open SharedPreferences that stores the API-keys
        apiStorage = context.getSharedPreferences("API", 0);
    }

    /**
     * Gets personal API keys after a succesful login.
     * @param username String containing the login username
     * @param password String containing the login password
     * @param callback LassieObjectCallback interface
     */
    void getPersonKeys(String username, String password, LassieObjectCallback callback) {
        String url = apiUrlBuilder("person_create_api");
        String[] hash = postHashContent(this.lassie_person_key, this.lassie_person_secret);
        Map<String, String>  params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        params.put("api_key", hash[0]);
        params.put("api_hash_content", hash[1]);
        params.put("api_hash", hash[2]);
        StringRequest request = JSONObjectRequest(url, callback, Request.Method.POST, params);
        queue.add(request);
    }

    /**
     * Gets personal info of the user based on the API keys in the API-SharedPreferences.
     * @param callback LassieObjectCallback interface
     */
    void getPersonalInfo(LassieObjectCallback callback) {
        String person_api_key = apiStorage.getString("api_key", null);
        String person_api_secret = apiStorage.getString("api_secret", null);
        String url = apiUrlBuilder(person_api_key, person_api_secret, "person_information");
        StringRequest request = JSONObjectRequest(url, callback, Request.Method.GET, null);
        queue.add(request);
    }

    /**
     * Gets payments of a specific account of the user based on the API keys in the API-SharedPreferences.
     * @param account_id Index for the accountName array that holds the account-identifiers
     * @param callback LassieArrayCallback interface
     */
    void getTransactions(int account_id, LassieArrayCallback callback) {
        String person_api_key = apiStorage.getString("api_key", null);
        String person_api_secret = apiStorage.getString("api_secret", null);
        String url = apiUrlBuilder(person_api_key, person_api_secret, "person_payments", "&selection=" + accountName[account_id]);
        StringRequest request = JSONArrayRequest(url, callback, Request.Method.GET);
        queue.add(request);
    }

    /**
     * Updates the person record of the logged in user
     * @param params Hashmap <String, String> containing the keys and values of the updated fields
     * @param callback LassieobjectCallback interface
     */
    void personUpdate(Map<String, String> params, LassieObjectCallback callback) {
        String person_api_key = apiStorage.getString("api_key", null);
        String person_api_secret = apiStorage.getString("api_secret", null);
        String url = apiUrlBuilder("person_update");
        String[] hash = postHashContent(person_api_key, person_api_secret);
        params.put("api_key", hash[0]);
        params.put("api_hash_content", hash[1]);
        params.put("api_hash", hash[2]);
        StringRequest request = JSONObjectRequest(url, callback, Request.Method.POST, params);
        queue.add(request);
    }

    /**
     * Gets the specified method from the model.
     * @param group_model The model-name as specified in the API-docs
     * @param method The method-name as specified in the API-docs
     * @param callback LassieObjectCallback interface
     */
    void getModel(String group_model, String method, LassieObjectCallback callback) {
        String url = apiUrlBuilder(lassie_model_key, lassie_model_secret, "model", group_model, method);
        StringRequest request = JSONObjectRequest(url, callback, Request.Method.GET, null);
        queue.add(request);
    }

    /**
     * Gets the specified method from the model.
     * @param group_model The model-name as specified in the API-docs
     * @param arguments Hashmap of keys and values of the arguments, as specified in the API-docs
     * @param method The method-name as specified in the API-docs
     * @param callback LassieObjectCallback interface
     */
    void getModel(String group_model, String method, HashMap<String, String> arguments, LassieObjectCallback callback) {
        String url = apiUrlBuilder(lassie_model_key, lassie_model_secret, "model", group_model, method, arguments);
        StringRequest request = JSONObjectRequest(url, callback, Request.Method.GET, null);
        queue.add(request);
    }

    /**
     * Gets the specified method from the model.
     * @param group_model The model-name as specified in the API-docs
     * @param method The method-name as specified in the API-docs
     * @param callback LassieArrayCallback interface
     */
    void getModel(String group_model, String method, LassieArrayCallback callback) {
        String url = apiUrlBuilder(lassie_model_key, lassie_model_secret, "model", group_model, method);
        StringRequest request = JSONArrayRequest(url, callback, Request.Method.GET);
        queue.add(request);
    }

    /**
     * Gets the specified method from the model.
     * @param group_model The model-name as specified in the API-docs
     * @param arguments Hashmap of keys and values of the arguments, as specified in the API-docs
     * @param method The method-name as specified in the API-docs
     * @param callback LassieArrayCallback interface
     */
    void getModel(String group_model, String method, HashMap<String, String> arguments, LassieArrayCallback callback) {
        String url = apiUrlBuilder(lassie_model_key, lassie_model_secret, "model", group_model, method, arguments);
        StringRequest request = JSONArrayRequest(url, callback, Request.Method.GET);
        queue.add(request);
    }

    // Volley Request function builders
    private StringRequest buildStringRequest(String url, final LassieStringCallback callback, int method) {
        return new StringRequest(method, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callback.result(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "String Request", error);
                    }
                }
        );
    }

    private StringRequest JSONObjectRequest(final String url, final LassieObjectCallback callback, int method, final Map<String, String> postParams) {
        return new StringRequest(method, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Object json = new JSONTokener(response).nextValue();
                            if (json instanceof JSONObject) {
                                callback.result((JSONObject) json);
                            } else {
                                JSONObject resultObject = new JSONObject();
                                resultObject.put("array", json);
                                callback.result(resultObject);
                            }
                        } catch(JSONException e) {
                            Log.e(TAG, "JSONObjectRequest JSONTokener", e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.w(TAG, "JSONObjectRequest", error);
                        if(error.networkResponse.data!=null) {
                            try {
                                Log.w(TAG, new String(error.networkResponse.data,"UTF-8"));
                                JSONObject resultObject = new JSONObject();
                                resultObject.put("error", new String(error.networkResponse.data,"UTF-8"));
                                callback.result(resultObject);
                            } catch (UnsupportedEncodingException | JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams()
            {
                return postParams;
            }
        };
    }

    private StringRequest JSONArrayRequest(final String url, final LassieArrayCallback callback, int method) {
        return new StringRequest(method, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Object json = new JSONTokener(response).nextValue();
                            if (json instanceof JSONObject) {
                                JSONArray resultArray = new JSONArray();
                                resultArray.put( json);
                                callback.result(resultArray);
                            } else {
                                callback.result((JSONArray) json);
                            }
                        } catch(JSONException e) {
                            Log.w(TAG, "JSONArrayRequest JSONTokener", e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.w(TAG, "JSONArrayRequest", error);
                        if(error.networkResponse.data!=null) {
                            try {
                                Log.w(TAG, new String(error.networkResponse.data,"UTF-8"));
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );
    }


    // API URI Builders
    private String apiUrlBuilder(String endpoint) {
        return lassie_api_url + endpoint;
    }

    private String apiUrlBuilder(String api_key, String api_secret, String endpoint) {
        return lassie_api_url + endpoint + "?api_key=" + api_key + hashContent(api_key, api_secret);
    }

    private String apiUrlBuilder(String api_key, String api_secret, String endpoint, String parameters) {
        return lassie_api_url + endpoint + "?api_key=" + api_key + parameters + hashContent(api_key, api_secret);
    }

    private String apiUrlBuilder(String api_key, String api_secret, String endpoint, String group_model, String method) {
        return lassie_api_url + endpoint + "?api_key=" + api_key + "&name=" + group_model + "&method=" + method + hashContent(api_key, api_secret);
    }

    private String apiUrlBuilder(String api_key, String api_secret, String endpoint, String group_model, String method, HashMap<String, String> arguments) {
        String argumentList = "";
        for (Map.Entry<String, String> entry : arguments.entrySet()) {
            argumentList = argumentList + "&" + entry.getKey() + "=" + entry.getValue();
        }
        return lassie_api_url + endpoint + "?api_key=" + api_key + "&name=" + group_model + "&method=" + method + argumentList + hashContent(api_key, api_secret);
    }

    /**
     * Gets the API-key and API-secret and creates an encrypted hash-URI that must be appended to Lassie API requests.
     * @param api_key The API-key
     * @param api_secret The API-secret
     * @return Hashed LassieAPI-valid String
     */
    private String hashContent(String api_key, String api_secret) {
        int n = new Random().nextInt(10000);
        String hashContent = String.valueOf(System.currentTimeMillis() + n);
        String apiHash = base64sha256(api_key + ":" + hashContent, api_secret);
        return "&api_hash_content=" + hashContent + "&api_hash=" + apiHash;
    }

    /**
     * Gets the API-key and API-secret and creates an encrypted hash-URI that must be appended to Lassie API posts.
     * @param api_key The API-key
     * @param api_secret The API-secret
     * @return String-array containing the API-key, hash-content and hash
     */
    private String[] postHashContent(String api_key, String api_secret) {
        int n = new Random().nextInt(10000);
        String hashContent = String.valueOf(System.currentTimeMillis() + n);
        String apiHash = base64sha256(api_key + ":" + hashContent, api_secret);
        String[] hash = { api_key, hashContent, apiHash };
        return hash;
    }

    /**
     * Gets the API-key, Hash-content and API-secret and generates a hashed String.
     * @param data In the form of api_key + ":" + hashContent
     * @param secret Api-secret
     * @return Hashed string after Base64SHA256 encryption
     */
    private static String base64sha256(String data, String secret) {
        String hash = null;
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] res = sha256_HMAC.doFinal(data.getBytes("UTF-8"));
            hash = getHex(res);
            hash = Base64.encodeToString(hash.getBytes("UTF-8"), Base64.NO_WRAP);
        } catch (Exception e) {
            Log.wtf(TAG, "BASE64SHA256", e);
        }
        return hash;
    }

    private static String getHex(byte[] raw) {
        final String HEXES = "0123456789abcdef";
        if (raw == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
                    .append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    // Callback interfaces

    /**
     * Interface for receiving JSONObjects.
     */
    interface LassieObjectCallback {
        void result(JSONObject result);
    }

    /**
     * Interface for receiving JSONArrays.
     */
    interface LassieArrayCallback {
        void result(JSONArray result);
    }

    /**
     * Interface for receiving Strings.
     */
    interface LassieStringCallback {
        void result(String result);
    }

    /**
     * Iterates through a JSONObject, extracts the actual array, and creates a valid JSONArray.
     * @param jsonObject The JSONObject in the form of {1: {"key": "value", "key": "value"}, 2: {"key": "value", "key": "value"}, etc.}
     * @return Valid JSONArray
     */
    JSONArray JSONObjectToArray(JSONObject jsonObject) {
        JSONArray resultArray = new JSONArray();
        Iterator<String> keys = jsonObject.keys();
        for(int i = 0; i < jsonObject.length(); i++) {
            try {
                String iteratorKey = keys.next();
                JSONObject result = jsonObject.getJSONObject(iteratorKey);
                resultArray.put(result);
            } catch(JSONException e) {
                Log.e(TAG, "JSONObjectToArray", e);
            }
        }
        return resultArray;
    }

}
