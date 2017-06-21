package cn.yiiguxing.plugin.translate;

import cn.yiiguxing.plugin.translate.model.QueryResult;
import com.leopoo.translate.util.TranslationResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface TranslationContract {

    interface Presenter {
        /**
         * @return 历史记录列表
         */
        @NotNull
        List<String> getHistory();

        /**
         * @param query 查询
         * @return 翻译缓存
         */
        @Nullable
        TranslationResult getCache(String query);

        /**
         * 查询翻译
         *
         * @param query 查询字符串
         */
        void query(@Nullable String query);
    }

    interface View {
        /**
         * 显示翻译结果
         *
         * @param query 查询字符串
         * @param result 翻译结果
         */
        void showResult(@NotNull String query, @NotNull TranslationResult result);

        /**
         * 显示错误信息
         *
         * @param query 查询字符串
         * @param error 错误信息
         */
        void showError(@NotNull String query, @NotNull String error);
    }

}
