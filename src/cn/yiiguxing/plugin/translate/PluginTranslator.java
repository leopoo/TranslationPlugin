package cn.yiiguxing.plugin.translate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import com.leopoo.language.DetectorFactory;
import com.leopoo.language.util.LangDetectException;
import com.leopoo.translate.enums.LANG;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.leopoo.translate.Translator;
import com.leopoo.translate.enums.ResultState;
import com.leopoo.translate.impl.BaiduTranslator;
import com.leopoo.translate.impl.GoogleTranslator;
import com.leopoo.translate.impl.YoudaoTranslator;
import com.leopoo.translate.util.TranslationResult;

/**
 * 翻译器
 */
@SuppressWarnings("WeakerAccess")
public final class PluginTranslator {

    private static final Logger logger = Logger.getInstance("#" + PluginTranslator.class.getCanonicalName());

    private final LruCache<String, TranslationResult> mCache = new LruCache<String, TranslationResult>(200);

    private final String profilePath = "lang/data";

    private Future<?> mCurrentTask;

    private Translator translator;

    private PluginTranslator() throws IOException, LangDetectException {
        Settings settings = Settings.getInstance();

        DetectorFactory.init(profilePath);

        if (settings.isSupportGoogle()) {
            translator = new GoogleTranslator(settings.getProxyHost(), settings.getProxyPort(), settings.getProxyUser(),
                    settings.getProxyPassword());
        } else if (settings.isSupportBaidu()) {
            translator = new BaiduTranslator();
        } else {
            translator = new YoudaoTranslator();
        }
    }

    /**
     * @return {@link PluginTranslator} 的实例
     */
    public static PluginTranslator getInstance() {
        return ServiceManager.getService(PluginTranslator.class);
    }

    /**
     * 获取缓存
     */
    @Nullable
    public TranslationResult getCache(@NotNull String query) {
        synchronized (mCache) {
            return mCache.get(query);
        }
    }

    /**
     * 查询翻译
     *
     * @param query 目标字符串
     * @param callback 回调
     */
    public void query(String query, Callback callback) {
        if (Utils.isEmptyOrBlankString(query)) {
            if (callback != null) {
                callback.onQuery(query, null);
            }

            return;
        }

        if (mCurrentTask != null) {
            mCurrentTask.cancel(true);
            mCurrentTask = null;
        }

        TranslationResult cache;
        synchronized (mCache) {
            cache = mCache.get(query);
        }
        if (cache != null) {
            if (callback != null) {
                callback.onQuery(query, cache);
            }
        } else {
            mCurrentTask = ApplicationManager.getApplication()
                    .executeOnPooledThread(new QueryRequest(query, callback, translator));
        }
    }

    private final class QueryRequest implements Runnable {

        private final String mQuery;

        private final Callback mCallback;

        private final Translator mTranslator;

        QueryRequest(String query, Callback callback, Translator translator) {
            mQuery = query;
            mCallback = callback;
            mTranslator = translator;
        }

        @Override
        public void run() {
            final String query = mQuery;
            TranslationResult result = null;
            try {
                result = mTranslator.translate(mQuery);
                if (result.getStatus() == ResultState.SUCCESS.getStatus()) {
                    synchronized (mCache) {
                        mCache.put(query, result);
                    }
                }
            } catch (Exception e) {
                result = new TranslationResult();
                result.setStatus(ResultState.FAILD.getStatus());
                logger.warn(e);
            }

            if (mCallback != null) {
                mCallback.onQuery(query, result);
            }
        }
    }

    /**
     * 翻译回调接口
     */
    public interface Callback {
        /**
         * 翻译结束后的回调方法
         *
         * @param query 查询字符串
         * @param result 翻译结果
         */
        void onQuery(@Nullable String query, @Nullable TranslationResult result);
    }

}
