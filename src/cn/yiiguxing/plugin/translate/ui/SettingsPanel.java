package cn.yiiguxing.plugin.translate.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import com.intellij.ide.browsers.BrowserLauncher;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.WebBrowserManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.FontComboBox;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ui.JBUI;

import cn.yiiguxing.plugin.translate.AppStorage;
import cn.yiiguxing.plugin.translate.Settings;
import cn.yiiguxing.plugin.translate.Utils;
import cn.yiiguxing.plugin.translate.action.AutoSelectionMode;
import cn.yiiguxing.plugin.translate.compat.IdeaCompat;

/**
 * 设置页
 */
@SuppressWarnings("Since15")
public class SettingsPanel {

    private static final int INDEX_INCLUSIVE = 0;

    private static final int INDEX_EXCLUSIVE = 1;

    private JPanel mWholePanel;

    private JPanel mSelectionSettingsPanel;

    private JComboBox<String> mSelectionMode;

    private JPanel mHistoryPanel;

    private ComboBox mMaxHistoriesSize;

    private JButton mClearHistoriesButton;

    private JPanel mFontPanel;

    private JCheckBox mFontCheckBox;

    private FontComboBox mPrimaryFontComboBox;

    private FontComboBox mPhoneticFontComboBox;

    private JTextPane mFontPreview;

    private JLabel mPrimaryFontLabel;

    private JLabel mPhoneticFontLabel;

    private JPanel mGoogleProxyPanel;

    private JTextField proxyHost;

    private JTextField proxyPort;

    private JTextField proxyUser;

    private JPasswordField proxyPassword;

    private JPanel mTranslatePanel;

    private JRadioButton googleTranslateRadio;

    private JRadioButton baiduTranslateRadio;

    private JRadioButton youdaoTranslateRadio;

    private JFormattedTextField formattedTextField1;

    private Settings mSettings;

    private AppStorage mAppStorage;

    public JComponent createPanel(@NotNull Settings settings, @NotNull AppStorage appStorage) {
        mSettings = settings;
        mAppStorage = appStorage;

        setTitles();
        setRenderer();
        setListeners();

        return mWholePanel;
    }

    private void createUIComponents() {

        mPrimaryFontComboBox = new FontComboBox();
        if (IdeaCompat.BUILD_NUMBER >= IdeaCompat.Version.IDEA2017_1) {
            mPhoneticFontComboBox = new FontComboBox(false, true);
        } else {
            mPhoneticFontComboBox = new FontComboBox();
        }

        fixFontComboBoxSize(mPrimaryFontComboBox);
        fixFontComboBoxSize(mPhoneticFontComboBox);
    }

    private void fixFontComboBoxSize(FontComboBox fontComboBox) {
        Dimension size = fontComboBox.getPreferredSize();
        size.width = size.height * 8;
        fontComboBox.setPreferredSize(size);
    }

    private void setTitles() {
        mSelectionSettingsPanel.setBorder(IdeBorderFactory.createTitledBorder("取词模式"));
        mFontPanel.setBorder(IdeBorderFactory.createTitledBorder("字体"));
        mHistoryPanel.setBorder(IdeBorderFactory.createTitledBorder("历史记录"));
        mGoogleProxyPanel.setBorder(IdeBorderFactory.createTitledBorder("Google Translate Proxy"));
        mTranslatePanel.setBorder(IdeBorderFactory.createTitledBorder("翻译方式"));
    }

    private void setRenderer() {
        mSelectionMode.setRenderer(new ListCellRendererWrapper<String>() {
            @Override
            public void customize(JList list, String value, int index, boolean selected, boolean hasFocus) {
                setText(value);
                if (index == INDEX_INCLUSIVE) {
                    setToolTipText("以最大范围取最近的所有词");
                } else if (index == INDEX_EXCLUSIVE) {
                    setToolTipText("取最近的单个词");
                }
            }
        });
    }

    private void setListeners() {

        googleTranslateRadio.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                final boolean selected = googleTranslateRadio.isSelected();
                proxyHost.setEnabled(selected);
                proxyPort.setEnabled(selected);
                proxyUser.setEnabled(selected);
                proxyPassword.setEnabled(selected);
            }
        });

        mFontCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                final boolean selected = mFontCheckBox.isSelected();
                mPrimaryFontComboBox.setEnabled(selected);
                mPhoneticFontComboBox.setEnabled(selected);
                mFontPreview.setEnabled(selected);
                mPrimaryFontLabel.setEnabled(selected);
                mPhoneticFontLabel.setEnabled(selected);
            }
        });
        mPrimaryFontComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    previewPrimaryFont(mPrimaryFontComboBox.getFontName());
                }
            }
        });
        mPhoneticFontComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    previewPhoneticFont(mPhoneticFontComboBox.getFontName());
                }
            }
        });
        mClearHistoriesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mAppStorage.clearHistories();
            }
        });
    }

    private void previewPrimaryFont(String primary) {
        if (Utils.isEmptyOrBlankString(primary)) {
            mFontPreview.setFont(JBUI.Fonts.label(14));
        } else {
            mFontPreview.setFont(JBUI.Fonts.create(primary, 14));
        }
    }

    private void previewPhoneticFont(String primary) {
        final StyledDocument document = mFontPreview.getStyledDocument();

        Font font;
        if (Utils.isEmptyOrBlankString(primary)) {
            font = JBUI.Fonts.label(14);
        } else {
            font = JBUI.Fonts.create(primary, 14);
        }

        final SimpleAttributeSet attributeSet = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attributeSet, font.getFamily());
        document.setCharacterAttributes(4, 41, attributeSet, true);
    }

    private static void obtainApiKey(String url) {
        WebBrowser browser = WebBrowserManager.getInstance().getFirstActiveBrowser();
        if (browser != null) {
            BrowserLauncher.getInstance().browseUsingPath(url, null, browser, null, ArrayUtil.EMPTY_STRING_ARRAY);
        }
    }

    @NotNull
    private AutoSelectionMode getAutoSelectionMode() {
        if (mSelectionMode.getSelectedIndex() == INDEX_INCLUSIVE) {
            return AutoSelectionMode.INCLUSIVE;
        } else {
            return AutoSelectionMode.EXCLUSIVE;
        }
    }

    private int getMaxHistorySize() {
        final Object size = mMaxHistoriesSize.getEditor().getItem();
        if (size instanceof String) {
            try {
                return Integer.parseInt((String) size);
            } catch (NumberFormatException e) {
                /* no-op */
            }
        }

        return -1;
    }

    public boolean isModified() {
        return (mSettings.getAutoSelectionMode() != getAutoSelectionMode()
                || getMaxHistorySize() != mAppStorage.getMaxHistorySize()
                || mFontCheckBox.isSelected() != mSettings.isOverrideFont()
                || (mSettings.getPrimaryFontFamily() != null
                && mSettings.getPrimaryFontFamily().equals(mPrimaryFontComboBox.getFontName()))
                || (mSettings.getPhoneticFontFamily() != null
                && mSettings.getPhoneticFontFamily().equals(mPhoneticFontComboBox.getFontName()))
                || (mSettings.isSupportBaidu() != baiduTranslateRadio.isSelected())
                || (mSettings.isSupportGoogle() != googleTranslateRadio.isSelected())
                || (mSettings.isSupportYoudao() != youdaoTranslateRadio.isSelected())

                || (mSettings.getProxyHost() != null && mSettings.getProxyHost().equals(proxyHost.getText()))
                || (proxyPort.getText() != null && mSettings.getProxyPort() != Integer.valueOf(proxyPort.getText()))
                || (mSettings.getProxyUser() != null && mSettings.getProxyUser().equals(proxyUser.getText()))
                || (mSettings.getProxyPassword() != null
                && mSettings.getProxyPassword().equals(proxyPassword.getPassword()))

        );
    }

    public void apply() {
        final int maxHistorySize = getMaxHistorySize();
        if (maxHistorySize >= 0) {
            mAppStorage.setMaxHistorySize(maxHistorySize);
        }

        mSettings.setOverrideFont(mFontCheckBox.isSelected());
        mSettings.setPrimaryFontFamily(mPrimaryFontComboBox.getFontName());
        mSettings.setPhoneticFontFamily(mPhoneticFontComboBox.getFontName());

        mSettings.setProxyHost(proxyHost.getText());
        if (proxyPort.getText() != null) {
            mSettings.setProxyPort(Integer.valueOf(proxyPort.getText()));
        }
        mSettings.setProxyUser(proxyUser.getText());
        mSettings.setProxyPassword(new String(proxyPassword.getPassword()));

        mSettings.setSupportBaidu(baiduTranslateRadio.isSelected());
        mSettings.setSupportGoogle(googleTranslateRadio.isSelected());
        mSettings.setSupportYoudao(youdaoTranslateRadio.isSelected());

        mSettings.setAutoSelectionMode(getAutoSelectionMode());
    }

    public void reset() {
        mFontCheckBox.setSelected(mSettings.isOverrideFont());
        mPrimaryFontComboBox.setFontName(mSettings.getPrimaryFontFamily());
        mPhoneticFontComboBox.setFontName(mSettings.getPhoneticFontFamily());
        previewPrimaryFont(mSettings.getPrimaryFontFamily());
        previewPhoneticFont(mSettings.getPhoneticFontFamily());

        baiduTranslateRadio.setSelected(mSettings.isSupportBaidu());
        googleTranslateRadio.setSelected(mSettings.isSupportGoogle());
        youdaoTranslateRadio.setSelected(mSettings.isSupportYoudao());

        proxyHost.setText(mSettings.getProxyHost());
        proxyPort.setText(Integer.toString(mSettings.getProxyPort()));
        proxyUser.setText(mSettings.getProxyUser());
        proxyPassword.setText(mSettings.getProxyPassword());

        mMaxHistoriesSize.getEditor().setItem(Integer.toString(mAppStorage.getMaxHistorySize()));
        // mDefaultApiKey.setSelected(mSettings.isUseDefaultKey());
        mSelectionMode.setSelectedIndex(
                mSettings.getAutoSelectionMode() == AutoSelectionMode.INCLUSIVE ? INDEX_INCLUSIVE : INDEX_EXCLUSIVE);
    }
}
