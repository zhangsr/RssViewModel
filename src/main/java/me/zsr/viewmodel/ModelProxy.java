package me.zsr.viewmodel;

import android.text.TextUtils;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.List;

import me.zsr.rssbean.Article;
import me.zsr.rssbean.ArticleDao;
import me.zsr.rssbean.Subscription;
import me.zsr.rssbean.SubscriptionDao;
import me.zsr.rsscommon.LogUtil;
import me.zsr.rsscommon.VolleySingleton;
import me.zsr.rssmodel.ArticleListRequest;
import me.zsr.rssmodel.ArticleModel;
import me.zsr.rssmodel.DBManager;
import me.zsr.rssmodel.SubscriptionModel;
import me.zsr.rssmodel.SubscriptionRequest;

public class ModelProxy {

    public static void markAllRead(boolean read, Article... articles) {
        ArticleModel.getInstance().markRead(read, articles);
    }

    public static void markAllRead(boolean read, List<Article> articles) {
        ArticleModel.getInstance().markRead(read, articles);
    }

    public static void markAllRead(final boolean read, final long subscriptionId) {
        ArticleModel.getInstance().markRead(read, subscriptionId);
    }

    public static void deleteArticle(List<Article> articles) {
        ArticleModel.getInstance().delete(articles);
    }

    public static List<Article> loadAllArticle() {
        return ArticleModel.getInstance().queryAll();
    }

    public static void deleteSubscription(Subscription... subscriptions) {
        SubscriptionModel.getInstance().delete(subscriptions);
    }

    public static void updateSubscription(Subscription... subscriptions) {
        SubscriptionModel.getInstance().update(subscriptions);
    }

    public void saveArticle(final Article article) {
        ArticleModel.getInstance().saveArticle(article);
    }

    public List<Article> queryArticles(long id) {
        return DBManager.getArticleDao().queryBuilder().where(
                ArticleDao.Properties.Id.eq(id)).list();
    }

    // bad smell : subscription in article model
    public List<Subscription> querySubscriptions(long id) {
        return DBManager.getSubscriptionDao().queryBuilder().where(
                SubscriptionDao.Properties.Id.eq(id)).list();
    }

    public static Subscription getSubscriptionById(long id) {
        List<Subscription> subscriptionList = DBManager.getSubscriptionDao().queryBuilder().where(
                SubscriptionDao.Properties.Id.eq(id)).list();
        if (subscriptionList == null || subscriptionList.size() == 0) {
            return null;
        } else {
            return subscriptionList.get(0);
        }
    }

    public static boolean hasSubscribed(String url) {
        return DBManager.getSubscriptionDao().queryBuilder().where(
                SubscriptionDao.Properties.Url.eq(url)).count() > 0;
    }

    public static long getSubscriptionCount() {
        return DBManager.getSubscriptionDao().queryBuilder().count();
    }

    public static void addSubscriptionByUrl(final String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        VolleySingleton.getInstance().getRequestQueue().cancelAll(url);
        SubscriptionRequest request = new SubscriptionRequest(url, new Response.Listener<Subscription>() {
            @Override
            public void onResponse(Subscription response) {
                LogUtil.i("onResponse=" + url);
                if (response == null) {
                    return;
                }
                response.setUrl(url);
                SubscriptionModel.getInstance().insert(response);
                // TODO: 2018/5/13 reload inbox
                // TODO: 2018/5/13 fetch this subscription
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: 2/24/17 optimize retry by adding "feed" suffix
                LogUtil.e("onErrorResponse=" + error.getMessage());
            }
        });
        request.setTag(url);
        LogUtil.i("request=" + url);
        VolleySingleton.getInstance().addToRequestQueue(request);
    }

    public static void addSubscription(List<Subscription> subscriptions) {
        SubscriptionModel.getInstance().insert(subscriptions);
    }

    public static long countArticleSync(long subscriptionId) {
        return ArticleModel.getInstance().countSync(subscriptionId);
    }


    public static void requestUpdateAll() {
        SubscriptionModel.getInstance().fetchAll();
    }

    public static void requestArticleList(String url, Response.Listener<List<Article>> listener) {
        ArticleListRequest request = new ArticleListRequest(url, listener, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        VolleySingleton.getInstance().addToRequestQueue(request);
    }
}
