package util;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * One place for every color, font and spacing value in the app.
 * 'final' class (syllabus: final) -> cannot be extended, and it is never instantiated.
 */
public final class Theme {

    // ---------- Interface (syllabus: interfaces) ----------
    /** Screens implement this to be told when light/dark mode changes. */
    public interface ThemeListener {
        void themeChanged();
    }

    // ---------- static members (syllabus: static) ----------
    private static final List<ThemeListener> listeners = new ArrayList<>();
    private static boolean dark = false;

    private Theme() { }   // private constructor: utility class

    // ---------- Colors that never change (final constants) ----------
    public static final Color PRIMARY         = Color.decode("#4F46E5");  // indigo
    public static final Color PRIMARY_HOVER   = Color.decode("#4338CA");
    public static final Color PRIMARY_PRESSED = Color.decode("#3730A3");
    public static final Color SUCCESS         = Color.decode("#16A34A");  // correct / pass
    public static final Color DANGER          = Color.decode("#DC2626");  // wrong / fail
    public static final Color WARNING         = Color.decode("#F59E0B");
    public static final Color WHITE           = Color.WHITE;

    // ---------- Colors that change with light/dark (set in applyPalette) ----------
    public static Color BACKGROUND;      // window background
    public static Color SURFACE;         // cards, sidebar, tables
    public static Color BORDER;
    public static Color TEXT;
    public static Color TEXT_MUTED;
    public static Color PRIMARY_SOFT;    // light indigo tint (hover, selected, chips)
    public static Color SUCCESS_SOFT;
    public static Color DANGER_SOFT;
    public static Color INPUT_BG;
    public static Color TABLE_STRIPE;    // alternate row color
    public static Color TABLE_HEADER_BG;
    public static Color SHADOW;          // card shadow (semi-transparent)

    // ---------- Fonts ----------
    public static final String FONT_FAMILY = pickFontFamily();

    public static final int SIZE_TITLE   = 28;
    public static final int SIZE_HEADING = 18;
    public static final int SIZE_BODY    = 14;
    public static final int SIZE_SMALL   = 12;
    public static final int SIZE_TIMER   = 22;
    public static final int SIZE_SCORE   = 64;

    public static final Font TITLE     = new Font(FONT_FAMILY, Font.BOLD,  SIZE_TITLE);
    public static final Font HEADING   = new Font(FONT_FAMILY, Font.BOLD,  SIZE_HEADING);
    public static final Font BODY      = new Font(FONT_FAMILY, Font.PLAIN, SIZE_BODY);
    public static final Font BODY_BOLD = new Font(FONT_FAMILY, Font.BOLD,  SIZE_BODY);
    public static final Font SMALL     = new Font(FONT_FAMILY, Font.PLAIN, SIZE_SMALL);
    public static final Font TIMER     = new Font(FONT_FAMILY, Font.BOLD,  SIZE_TIMER);
    public static final Font SCORE     = new Font(FONT_FAMILY, Font.BOLD,  SIZE_SCORE);

    // ---------- Spacing and shape ----------
    public static final int PAD_SM     = 8;
    public static final int PAD        = 16;
    public static final int PAD_LG     = 24;
    public static final int RADIUS     = 16;   // cards
    public static final int RADIUS_BTN = 12;   // buttons and inputs
    public static final int SHADOW_SIZE = 8;   // soft shadow thickness

    // ---------- Public API ----------

    /** Call once at startup, before creating any window. */
    public static void install() {
        // Make FlatLaf's accent (focus rings, selections) match our indigo
        FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", "#4F46E5"));
        applyPalette();
        applyLookAndFeel();
    }

    /** Switch between light and dark mode. */
    public static void toggle() {
        setDark(!dark);
    }

    public static void setDark(boolean value) {
        dark = value;
        applyPalette();
        applyLookAndFeel();
        FlatLaf.updateUI();                       // refresh every open window
        for (ThemeListener l : new ArrayList<>(listeners)) {
            l.themeChanged();                     // let custom-painted components repaint
        }
    }

    public static boolean isDark() {
        return dark;
    }

    public static void addListener(ThemeListener l) {
        listeners.add(l);
    }

    public static void removeListener(ThemeListener l) {
        listeners.remove(l);
    }

    // ---------- Method overloading (syllabus) ----------
    public static Font font(int size) {
        return new Font(FONT_FAMILY, Font.PLAIN, size);
    }

    public static Font font(int style, int size) {
        return new Font(FONT_FAMILY, style, size);
    }

    public static Color withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    /** Mix two colors: ratio 0 = a, ratio 1 = b. Handy for hover effects. */
    public static Color blend(Color a, Color b, float ratio) {
        float r = 1f - ratio;
        return new Color(
                Math.round(a.getRed()   * r + b.getRed()   * ratio),
                Math.round(a.getGreen() * r + b.getGreen() * ratio),
                Math.round(a.getBlue()  * r + b.getBlue()  * ratio));
    }

    // ---------- Private helpers ----------

    private static String pickFontFamily() {
        List<String> installed = Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        for (String name : new String[] {"Inter", "Segoe UI"}) {
            if (installed.contains(name)) {
                return name;
            }
        }
        return Font.SANS_SERIF;   // fallback on Linux/macOS
    }

    private static void applyPalette() {
        if (dark) {
            BACKGROUND      = Color.decode("#0F172A");
            SURFACE         = Color.decode("#1E293B");
            BORDER          = Color.decode("#334155");
            TEXT            = Color.decode("#F1F5F9");
            TEXT_MUTED      = Color.decode("#94A3B8");
            PRIMARY_SOFT    = Color.decode("#312E81");
            SUCCESS_SOFT    = Color.decode("#14532D");
            DANGER_SOFT     = Color.decode("#7F1D1D");
            INPUT_BG        = Color.decode("#0F172A");
            TABLE_STRIPE    = Color.decode("#243247");
            TABLE_HEADER_BG = Color.decode("#334155");
            SHADOW          = new Color(0, 0, 0, 90);
        } else {
            BACKGROUND      = Color.decode("#F3F4F6");
            SURFACE         = Color.decode("#FFFFFF");
            BORDER          = Color.decode("#E5E7EB");
            TEXT            = Color.decode("#111827");
            TEXT_MUTED      = Color.decode("#6B7280");
            PRIMARY_SOFT    = Color.decode("#EEF2FF");
            SUCCESS_SOFT    = Color.decode("#DCFCE7");
            DANGER_SOFT     = Color.decode("#FEE2E2");
            INPUT_BG        = Color.decode("#FFFFFF");
            TABLE_STRIPE    = Color.decode("#F9FAFB");
            TABLE_HEADER_BG = Color.decode("#F3F4F6");
            SHADOW          = new Color(0, 0, 0, 18);
        }
    }

    private static void applyLookAndFeel() {
        // Global font and shapes (must be set before the L&F is installed)
        UIManager.put("defaultFont", new FontUIResource(BODY));
        UIManager.put("Component.arc", RADIUS_BTN);
        UIManager.put("Button.arc", RADIUS_BTN);
        UIManager.put("TextComponent.arc", RADIUS_BTN);
        UIManager.put("ProgressBar.arc", 999);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("TabbedPane.showTabSeparators", true);

        // Palette -> Swing defaults (ColorUIResource lets them change on theme switch)
        UIManager.put("Panel.background", res(BACKGROUND));
        UIManager.put("Component.borderColor", res(BORDER));
        UIManager.put("Separator.foreground", res(BORDER));
        for (String key : new String[] {
                "TextField.background", "PasswordField.background", "TextArea.background",
                "ComboBox.background", "FormattedTextField.background", "Spinner.background"}) {
            UIManager.put(key, res(INPUT_BG));
        }

        // JTable: alternating rows, styled header, padding
        UIManager.put("Table.background", res(SURFACE));
        UIManager.put("Table.alternateRowColor", res(TABLE_STRIPE));
        UIManager.put("Table.selectionBackground", res(PRIMARY_SOFT));
        UIManager.put("Table.selectionForeground", res(TEXT));
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
        UIManager.put("Table.gridColor", res(BORDER));
        UIManager.put("TableHeader.background", res(TABLE_HEADER_BG));
        UIManager.put("TableHeader.foreground", res(TEXT));
        UIManager.put("TableHeader.font", new FontUIResource(BODY_BOLD));
        UIManager.put("TableHeader.height", 40);
                // Tabs (JTabbedPane) and lists
        UIManager.put("TabbedPane.tabType", "underlined");
        UIManager.put("TabbedPane.tabHeight", 42);
        UIManager.put("TabbedPane.tabInsets", new Insets(4, 16, 4, 16));
        UIManager.put("TabbedPane.background", res(BACKGROUND));
        UIManager.put("TabbedPane.contentAreaColor", res(BORDER));
        UIManager.put("TabbedPane.underlineColor", res(PRIMARY));
        UIManager.put("TabbedPane.selectedForeground",
                res(dark ? blend(PRIMARY, Color.WHITE, 0.5f) : PRIMARY));
        UIManager.put("TabbedPane.hoverColor", res(PRIMARY_SOFT));
        UIManager.put("TabbedPane.focusColor", res(PRIMARY_SOFT));
        UIManager.put("List.background", res(SURFACE));

        if (dark) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
    }

    private static ColorUIResource res(Color c) {
        return new ColorUIResource(c);
    }

    // Initialize the palette once when the class loads
    static {
        applyPalette();
    }
}