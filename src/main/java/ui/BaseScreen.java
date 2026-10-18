package ui;

import util.Theme;

import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * Abstract base class for every screen (login, dashboard, quiz, result).
 * Syllabus: abstract class + inheritance + interface (ThemeListener).
 */
public abstract class BaseScreen extends JPanel implements Theme.ThemeListener {

    protected BaseScreen() {
        super(new BorderLayout());
    }

    /** Every screen must build its own components (abstract method). */
    protected abstract void buildUI();

    /** Called when light/dark mode changes. Screens may override it. */
    @Override
    public void themeChanged() {
        repaint();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        Theme.addListener(this);
    }

    @Override
    public void removeNotify() {
        Theme.removeListener(this);
        super.removeNotify();
    }
}