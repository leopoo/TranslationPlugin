package cn.yiiguxing.plugin.translate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.messages.Topic;
import com.intellij.util.xmlb.XmlSerializerUtil;

import cn.yiiguxing.plugin.translate.action.AutoSelectionMode;

/**
 * Settings
 */
@State(name = "TranslationSettings", storages = @Storage(id = "other", file = "$APP_CONFIG$/translation.xml"))
public class Settings implements PersistentStateComponent<Settings> {



    private String proxyHost;
    private int proxyPort;
    private String proxyUser;
    private String proxyPassword;

    private boolean supportGoogle;
    private boolean supportBaidu;
    private boolean supportYoudao;

    private boolean overrideFont;
    private String primaryFontFamily;
    private String phoneticFontFamily;

    @NotNull
    private AutoSelectionMode autoSelectionMode = AutoSelectionMode.INCLUSIVE;

    /**
     * Get the instance of this service.
     *
     * @return the unique {@link Settings} instance.
     */
    public static Settings getInstance() {
        return ServiceManager.getService(Settings.class);
    }

    @Nullable
    @Override
    public Settings getState() {
        return this;
    }

    @Override
    public void loadState(Settings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @Override
    public String toString() {
        return "Settings{" +
                "supportGoogle=" + supportGoogle +
                ", supportBaidu='" + supportBaidu + '\'' +
                ", supportYoudao='" + supportYoudao + '\'' +
                ", overrideFont=" + overrideFont +
                ", primaryFontFamily='" + primaryFontFamily + '\'' +
                ", phoneticFontFamily='" + phoneticFontFamily + '\'' +
                ", autoSelectionMode=" + autoSelectionMode +
                '}';
    }

    /**
     * 返回自动取词模式
     */
    @NotNull
    public AutoSelectionMode getAutoSelectionMode() {
        return autoSelectionMode;
    }

    /**
     * 设置自动取词模式
     */
    public void setAutoSelectionMode(@NotNull AutoSelectionMode autoSelectionMode) {
        this.autoSelectionMode = autoSelectionMode;
    }

    /**
     * 返回是否覆盖默认字体
     */
    public boolean isOverrideFont() {
        return overrideFont;
    }

    /**
     * 设置是否覆盖默认字体
     */
    public void setOverrideFont(boolean overrideFont) {
        if (this.overrideFont != overrideFont) {
            this.overrideFont = overrideFont;

            getSettingsChangePublisher().onOverrideFontChanged(this);
        }
    }

    public String getPrimaryFontFamily() {
        return primaryFontFamily;
    }

    public void setPrimaryFontFamily(String primaryFontFamily) {
        if (this.primaryFontFamily != null
                ? !this.primaryFontFamily.equals(primaryFontFamily) : primaryFontFamily != null) {
            this.primaryFontFamily = primaryFontFamily;

            getSettingsChangePublisher().onOverrideFontChanged(this);
        }
    }

    public String getPhoneticFontFamily() {
        return phoneticFontFamily;
    }

    public void setPhoneticFontFamily(String phoneticFontFamily) {
        if (this.phoneticFontFamily != null
                ? !this.phoneticFontFamily.equals(phoneticFontFamily) : phoneticFontFamily != null) {
            this.phoneticFontFamily = phoneticFontFamily;

            getSettingsChangePublisher().onOverrideFontChanged(this);
        }
    }

    @NotNull
    private static SettingsChangeListener getSettingsChangePublisher() {
        return ApplicationManager
                .getApplication()
                .getMessageBus()
                .syncPublisher(SettingsChangeListener.TOPIC);
    }

    public interface SettingsChangeListener {
        Topic<SettingsChangeListener> TOPIC =
                Topic.create("TranslationSettingsChanged", SettingsChangeListener.class);

        void onOverrideFontChanged(@NotNull Settings settings);
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public boolean isSupportGoogle() {
        return supportGoogle;
    }

    public void setSupportGoogle(boolean supportGoogle) {
        this.supportGoogle = supportGoogle;
    }

    public boolean isSupportBaidu() {
        return supportBaidu;
    }

    public void setSupportBaidu(boolean supportBaidu) {
        this.supportBaidu = supportBaidu;
    }

    public boolean isSupportYoudao() {
        return supportYoudao;
    }

    public void setSupportYoudao(boolean supportYoudao) {
        this.supportYoudao = supportYoudao;
    }
}
