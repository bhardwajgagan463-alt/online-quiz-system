package ui;

import util.Theme;

import javax.swing.BorderFactory;
import javax.swing.Icon;

/** Sun/moon button that switches light and dark mode. Inheritance + interface. */
public class ThemeToggleButton extends RoundedButton implements Theme.ThemeListener {

    public ThemeToggleButton() {
        super("", currentIcon(), Style.GHOST);
        setToolTipText("Switch light / dark mode");
        setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        addActionListener(e -> Theme.toggle());
    }

    private static Icon currentIcon() {
        return Icons.of(Theme.isDark() ? Icons.Type.SUN : Icons.Type.MOON, 20);
    }

    @Override
    public void themeChanged() {
        setIcon(currentIcon());
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