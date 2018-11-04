package me.zsr.viewmodel;

import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import me.zsr.rssbean.Discover;
import me.zsr.rsscommon.LogUtil;
import me.zsr.rsscommon.StringUtil;
import me.zsr.rsscommon.VolleySingleton;

public class SearchDiscoverViewModel extends DiscoverViewModel {
    private static final String[] BLACK_LIST = new String[] {
            "http://feeds.feedburner.com/zhihu-daily"
    };

    public SearchDiscoverViewModel(ViewModelObserver<Discover> observer, Context context) {
        super(observer, context);
    }

    public void search(String keyword) {
        if (StringUtil.isNullOrEmpty(keyword)) {
            return;
        }

        try {
            searchByFeedly(keyword);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void searchByFeedly(final String keyword) throws URISyntaxException {
        List<BasicNameValuePair> params = new LinkedList<>();
        params.add(new BasicNameValuePair("query", keyword));
        final String requestUrl = URIUtils.createURI("http", "feedly.com", -1,
                "/v3/search/feeds", URLEncodedUtils.format(params, "utf-8"), null).toString();
        VolleySingleton.getInstance().getRequestQueue().cancelAll(this);
        StringRequest request = new StringRequest(requestUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        LogUtil.d("onResponse=" + requestUrl);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String json = jsonObject.getString("results");
                            Type listType = new TypeToken<List<FeedlyResult>>() {}.getType();
                            List<FeedlyResult> resultList = new Gson().fromJson(json, listType);
                            filterResult(resultList);

                            if (mObserver != null) {
                                mObserver.onDataChanged(feedlyResult2Discover(resultList));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        LogUtil.e("onErrorResponse=" + error);
                    }
                }
        );
        request.setTag(this);
        LogUtil.d("request=" + requestUrl);
        VolleySingleton.getInstance().addToRequestQueue(request);
    }

    private void filterResult(List<FeedlyResult> resultList) {
        List<FeedlyResult> removeList = new ArrayList<>();
        for (FeedlyResult result : resultList) {
            for (String s : BLACK_LIST) {
                if (s.equals(result.feedId.substring(5))) {
                    removeList.add(result);
                }
            }
        }
    }

    private List<Discover> feedlyResult2Discover(List<FeedlyResult> feedlyResultList) {
        List<Discover> discoverList = new ArrayList<>();
        for (FeedlyResult feedlyResult : feedlyResultList) {
            Discover discover = new Discover();
            discover.setTitle(feedlyResult.title);
            discover.setUrl(feedlyResult.feedId.substring(5));
            discoverList.add(discover);
        }
        return discoverList;
    }
}
