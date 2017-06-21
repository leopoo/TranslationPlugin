package cn.yiiguxing.plugin.translate;

import java.awt.*;
import java.awt.event.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.text.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.JBColor;
import com.intellij.util.Consumer;
import com.leopoo.translate.util.TranslationResult;

import cn.yiiguxing.plugin.translate.model.BasicExplain;
import cn.yiiguxing.plugin.translate.model.WebExplain;
import cn.yiiguxing.plugin.translate.ui.PhoneticButton;

/**
 * 文本样式
 */
@SuppressWarnings("SpellCheckingInspection")
public final class Styles {

    private static final Logger LOGGER = Logger.getInstance("#" + Styles.class.getCanonicalName());

    private static final SimpleAttributeSet ATTR_QUERY = new SimpleAttributeSet();

    private static final SimpleAttributeSet ATTR_EXPLAIN_BASE = new SimpleAttributeSet();

    private static final SimpleAttributeSet ATTR_EXPLAIN = new SimpleAttributeSet();

    private static final SimpleAttributeSet ATTR_PRE_EXPLAINS = new SimpleAttributeSet();

    private static final SimpleAttributeSet ATTR_EXPLAINS = new SimpleAttributeSet();

    private static final SimpleAttributeSet ATTR_EXPLAINS_HOVER = new SimpleAttributeSet();

    private static final SimpleAttributeSet ATTR_WEB_EXPLAIN_TITLE = new SimpleAttributeSet();

    private static final SimpleAttributeSet ATTR_WEB_EXPLAIN_KEY = new SimpleAttributeSet();

    private static final SimpleAttributeSet ATTR_WEB_EXPLAIN_VALUES = new SimpleAttributeSet();

    private static final float QUERY_FONT_SCALE = 1.35f;

    private static final float PRE_EXPLAINS_FONT_SCALE = 1.15f;

    private static final float EXPLAINS_FONT_SCALE = 1.15f;

    private static final Pattern PATTERN_WORD = Pattern.compile("[a-zA-Z]+");

    static {
        StyleConstants.setItalic(ATTR_QUERY, true);
        StyleConstants.setBold(ATTR_QUERY, true);
        StyleConstants.setForeground(ATTR_QUERY, new JBColor(0xFFEE6000, 0xFFCC7832));

        JBColor fg = new JBColor(0xFF3E7EFF, 0xFF8CBCE1);
        StyleConstants.setForeground(ATTR_EXPLAIN_BASE, fg);
        StyleConstants.setForeground(ATTR_EXPLAIN, fg);

        StyleConstants.setItalic(ATTR_PRE_EXPLAINS, true);
        StyleConstants.setForeground(ATTR_PRE_EXPLAINS, new JBColor(0xFF7F0055, 0xFFEAB1FF));

        StyleConstants.setForeground(ATTR_EXPLAINS, new JBColor(0xFF170591, 0xFFFFC66D));

        StyleConstants.setForeground(ATTR_EXPLAINS_HOVER, new JBColor(0xA60EFF, 0xDF531F));

        StyleConstants.setForeground(ATTR_WEB_EXPLAIN_TITLE, new JBColor(0xFF707070, 0xFF808080));
        StyleConstants.setForeground(ATTR_WEB_EXPLAIN_KEY, new JBColor(0xFF4C4C4C, 0xFF77B767));
        StyleConstants.setForeground(ATTR_WEB_EXPLAIN_VALUES, new JBColor(0xFF707070, 0xFF6A8759));
    }

    private Styles() {
    }

    private static void setMouseListeners(@NotNull JTextPane textPane) {
        for (MouseListener listener : textPane.getMouseListeners()) {
            if (listener instanceof ClickableStyleListener) {
                textPane.removeMouseListener(listener);
            }
        }
        for (MouseMotionListener listener : textPane.getMouseMotionListeners()) {
            if (listener instanceof ClickableStyleListener) {
                textPane.removeMouseMotionListener(listener);
            }
        }

        MouseAdapter listener = new ClickableStyleListener();
        textPane.addMouseListener(listener);
        textPane.addMouseMotionListener(listener);
    }

    public static void insertStylishResultText(@NotNull
    final JTextPane textPane, @NotNull TranslationResult result, @Nullable OnTextClickListener explainsClickListener) {
        setMouseListeners(textPane);

        final StyledDocument document = textPane.getStyledDocument();

        try {
            document.remove(0, document.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
            return;
        }

        insertHeader(textPane, result);

        // TODO 有道讲解
        // BasicExplain basicExplain = result.getBasicExplain();
        // if (basicExplain != null) {
        // insertExplain(textPane, document, basicExplain.getExplains(), true,
        // explainsClickListener);
        // } else {
        // insertExplain(textPane, document, result.getTranslation(), false,
        // explainsClickListener);
        // }
        //
        // WebExplain[] webExplains = result.getWebExplains();
        // insertWebExplain(document, webExplains);

        // TODO zbb
        insertExplain(textPane, document, result.getTranslation(), false, explainsClickListener);

        if (document.getLength() < 1)
            return;

        try {
            int offset = document.getLength() - 1;
            String text = document.getText(offset, 1);
            if (text.charAt(0) == '\n') {
                document.remove(offset, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 不能静态设置，否则scale改变时不能即时更新
    @NotNull
    private static MutableAttributeSet updateFontSize(@NotNull MutableAttributeSet attr, Font font, float scale) {
        StyleConstants.setFontSize(attr, Math.round(font.getSize() * scale));
        return attr;
    }

    private static void insertHeader(@NotNull JTextPane textPane, TranslationResult result) {
        Document document = textPane.getDocument();
        String query = result.getSrc();

        try {
            if (!Utils.isEmptyOrBlankString(query)) {
                query = query.trim();
                document.insertString(document.getLength(),
                        Character.toUpperCase(query.charAt(0)) + query.substring(1) + "\n",
                        updateFontSize(ATTR_QUERY, textPane.getFont(), QUERY_FONT_SCALE));
            }

            // TODO 语音处理
            // BasicExplain be = result.getBasicExplain();
            BasicExplain be = null;
            if (be != null) {
                boolean hasPhonetic = false;

                String phoUK = be.getPhoneticUK();
                if (!Utils.isEmptyOrBlankString(phoUK)) {
                    insertPhonetic(document, result.getSrc(), phoUK, Speech.Phonetic.UK);
                    hasPhonetic = true;
                }

                String phoUS = be.getPhoneticUS();
                if (!Utils.isEmptyOrBlankString(phoUS)) {
                    insertPhonetic(document, result.getSrc(), phoUS, Speech.Phonetic.US);
                    hasPhonetic = true;
                }

                String pho = be.getPhonetic();
                if (!Utils.isEmptyOrBlankString(pho) && !hasPhonetic) {
                    document.insertString(document.getLength(), "[" + pho + "]", ATTR_EXPLAIN);
                    hasPhonetic = true;
                }

                if (hasPhonetic) {
                    document.insertString(document.getLength(), "\n", null);
                }
            }

            document.insertString(document.getLength(), "\n", null);
        } catch (BadLocationException e) {
            LOGGER.error("insertHeader ", e);
        }
    }

    private static void insertPhonetic(@NotNull Document document, @NotNull
    final String query, @NotNull String phoneticText, @NotNull
    final Speech.Phonetic phonetic) throws BadLocationException {
        document.insertString(document.getLength(), phonetic == Speech.Phonetic.UK ? "英[" : "美[", ATTR_EXPLAIN_BASE);

        final Settings settings = Settings.getInstance();
        final String fontFamily = settings.getPhoneticFontFamily();
        if (!settings.isOverrideFont() || Utils.isEmptyOrBlankString(fontFamily)) {
            ATTR_EXPLAIN.removeAttribute(StyleConstants.FontFamily);
        } else {
            StyleConstants.setFontFamily(ATTR_EXPLAIN, fontFamily);
        }
        document.insertString(document.getLength(), phoneticText, ATTR_EXPLAIN);

        document.insertString(document.getLength(), "]", ATTR_EXPLAIN_BASE);

        SimpleAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setComponent(attr, new PhoneticButton(new Consumer<MouseEvent>() {
            @Override
            public void consume(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 1) {
                    Speech.toSpeech(query, phonetic);
                }
            }
        }));
        document.insertString(document.getLength(), " ", attr);
    }

    private static void insertExplain(@NotNull
    final JTextPane textPane, @NotNull StyledDocument doc, @Nullable String[] explains, boolean splitLabel,
            @Nullable OnTextClickListener explainsClickListener) {
        if (explains == null || explains.length == 0)
            return;

        MutableAttributeSet attrPre = updateFontSize(ATTR_PRE_EXPLAINS, textPane.getFont(), PRE_EXPLAINS_FONT_SCALE);
        MutableAttributeSet attr = updateFontSize(ATTR_EXPLAINS, textPane.getFont(), EXPLAINS_FONT_SCALE);
        try {
            for (String exp : explains) {
                if (Utils.isEmptyOrBlankString(exp))
                    continue;

                if (splitLabel) {
                    String[] splits = Utils.splitExplain(exp);
                    if (splits[0] != null) {
                        doc.insertString(doc.getLength(), splits[0] + " ", attrPre);
                        exp = splits[1];
                    }

                    final int offset = doc.getLength();
                    doc.insertString(offset, exp + "\n", attr);

                    Matcher wordMatcher = PATTERN_WORD.matcher(exp);
                    String text;
                    int start;
                    ClickableStyle style;
                    while (wordMatcher.find()) {
                        text = wordMatcher.group();
                        start = wordMatcher.start() + offset;
                        style = new ClickableStyle(textPane, text, start, explainsClickListener);
                        doc.setCharacterAttributes(start, text.length(), setClickableStyle(attr, style), true);
                    }
                } else {
                    doc.insertString(doc.getLength(), exp + "\n", attr);
                }
            }

            doc.insertString(doc.getLength(), "\n", null);
        } catch (BadLocationException e) {
            LOGGER.error("insertExplain ", e);
        }
    }

    private static void insertWebExplain(Document doc, WebExplain[] webExplains) {
        if (webExplains == null || webExplains.length == 0)
            return;

        try {
            doc.insertString(doc.getLength(), "网络释义:\n", ATTR_WEB_EXPLAIN_TITLE);

            for (WebExplain webExplain : webExplains) {
                doc.insertString(doc.getLength(), webExplain.getKey(), ATTR_WEB_EXPLAIN_KEY);
                doc.insertString(doc.getLength(), " -", null);

                String[] values = webExplain.getValues();
                for (int i = 0; i < values.length; i++) {
                    doc.insertString(doc.getLength(), " " + values[i] + (i < values.length - 1 ? ";" : ""),
                            ATTR_WEB_EXPLAIN_VALUES);
                }
                doc.insertString(doc.getLength(), "\n", null);
            }

        } catch (BadLocationException e) {
            LOGGER.error("insertWebExplain ", e);
        }
    }

    private static MutableAttributeSet setClickableStyle(MutableAttributeSet attrSet, ClickableStyle style) {
        attrSet = (MutableAttributeSet) attrSet.copyAttributes();
        attrSet.addAttribute(ClickableStyle.class, style);
        return attrSet;
    }

    public interface OnTextClickListener {
        void onTextClick(@NotNull JTextPane textPane, @NotNull String text);
    }

    @SuppressWarnings("WeakerAccess")
    private static final class ClickableStyle {
        private final JTextPane mTextPane;

        private final String mText;

        private final int mStartOffset;

        private final OnTextClickListener mListener;

        private boolean mHover;

        public ClickableStyle(@NotNull JTextPane textPane, @NotNull String text, int startOffset,
                @Nullable OnTextClickListener listener) {
            this.mTextPane = textPane;
            this.mText = text;
            this.mStartOffset = startOffset;
            this.mListener = listener;
        }

        void performClick() {
            if (mListener != null) {
                mListener.onTextClick(mTextPane, mText);
            }
        }

        void onHover() {
            if (!mHover) {
                StyledDocument document = mTextPane.getStyledDocument();
                MutableAttributeSet attr = updateFontSize(ATTR_EXPLAINS_HOVER, mTextPane.getFont(),
                        EXPLAINS_FONT_SCALE);
                attr = setClickableStyle(attr, this);
                document.setCharacterAttributes(mStartOffset, mText.length(), attr, true);

                mHover = true;
            }
        }

        void clearHover() {
            if (mHover) {
                StyledDocument document = mTextPane.getStyledDocument();
                MutableAttributeSet attr = updateFontSize(ATTR_EXPLAINS, mTextPane.getFont(), EXPLAINS_FONT_SCALE);
                attr = setClickableStyle(attr, this);
                document.setCharacterAttributes(mStartOffset, mText.length(), attr, true);

                mHover = false;
            }
        }

    }

    private static final class ClickableStyleListener extends MouseAdapter {
        private ClickableStyle mLastHover;

        @Override
        public void mouseClicked(MouseEvent e) {
            if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == 0 || e.getClickCount() > 1)
                return;

            ClickableStyle clickableStyle = getClickableStyle(e);
            if (clickableStyle != null) {
                clickableStyle.performClick();
            }
        }

        private ClickableStyle getClickableStyle(MouseEvent e) {
            JTextPane textPane = (JTextPane) e.getComponent();
            StyledDocument document = textPane.getStyledDocument();

            Element ele = document.getCharacterElement(textPane.viewToModel(e.getPoint()));
            MutableAttributeSet as = (MutableAttributeSet) ele.getAttributes();
            return (ClickableStyle) as.getAttribute(ClickableStyle.class);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            final ClickableStyle lastHover = mLastHover;
            final ClickableStyle hover = getClickableStyle(e);

            if (lastHover != hover) {
                mLastHover = hover;

                if (lastHover != null) {
                    lastHover.clearHover();
                }
                if (hover != null) {
                    hover.onHover();
                }
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (mLastHover != null) {
                mLastHover.clearHover();
                mLastHover = null;
            }
        }
    }

}
