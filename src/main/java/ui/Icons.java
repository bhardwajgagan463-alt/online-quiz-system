package ui;

import util.Theme;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Supplier;

/** Vector icons drawn with Java2D, so no image files are needed. */
public final class Icons {

    public enum Type {
        USER, LOCK, MAIL, EYE, EYE_OFF, PLUS, EDIT, TRASH, CHECK, CROSS, LOGOUT,
        SUN, MOON, DASHBOARD, DOCUMENT, CHART, CLOCK, SEARCH, SAVE,
        ARROW_LEFT, ARROW_RIGHT, LIST, HOME
    }

    private Icons() { }

    // ---- Method overloading: three ways to create the same icon ----

    /** Icon takes the color of the component it is painted on (button text color). */
    public static Icon of(Type type, int size) {
        return new VectorIcon(type, size, null, null);
    }

    /** Icon with a fixed color. */
    public static Icon of(Type type, int size, Color color) {
        return new VectorIcon(type, size, color, null);
    }

    /** Icon whose color is looked up every time it paints (survives light/dark toggle). */
    public static Icon of(Type type, int size, Supplier<Color> color) {
        return new VectorIcon(type, size, null, color);
    }

    public static Icon muted(Type type, int size) {
        return of(type, size, () -> Theme.TEXT_MUTED);
    }

    // ---- The icon class (implements the Icon interface) ----
    private static class VectorIcon implements Icon {
        private final Type type;
        private final int size;
        private final Color fixed;
        private final Supplier<Color> dynamic;

        VectorIcon(Type type, int size, Color fixed, Supplier<Color> dynamic) {
            this.type = type;
            this.size = size;
            this.fixed = fixed;
            this.dynamic = dynamic;
        }

        @Override public int getIconWidth()  { return size; }
        @Override public int getIconHeight() { return size; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            g2.scale(size / 24.0, size / 24.0);          // all drawing uses a 24x24 grid
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(colorFor(c));
            draw(g2);
            g2.dispose();
        }

        private Color colorFor(Component c) {
            if (dynamic != null) return dynamic.get();
            if (fixed != null) return fixed;
            return c != null ? c.getForeground() : Theme.TEXT;
        }

        private void draw(Graphics2D g) {
            switch (type) {
                case USER -> {
                    g.draw(new Ellipse2D.Double(8, 3, 8, 8));
                    g.draw(new Arc2D.Double(5, 14, 14, 14, 0, 180, Arc2D.OPEN));
                }
                case LOCK -> {
                    g.draw(new RoundRectangle2D.Double(5, 11, 14, 10, 3, 3));
                    g.draw(new Arc2D.Double(8, 3, 8, 10, 0, 180, Arc2D.OPEN));
                    line(g, 8, 8, 8, 11);
                    line(g, 16, 8, 16, 11);
                    line(g, 12, 15, 12, 17);
                }
                case MAIL -> {
                    g.draw(new RoundRectangle2D.Double(3, 5, 18, 14, 3, 3));
                    poly(g, false, 3.5, 7.5, 12, 13.5, 20.5, 7.5);
                }
                case EYE -> drawEye(g);
                case EYE_OFF -> {
                    drawEye(g);
                    line(g, 4, 4, 20, 20);
                }
                case PLUS -> {
                    line(g, 12, 5, 12, 19);
                    line(g, 5, 12, 19, 12);
                }
                case EDIT -> {
                    poly(g, true, 4, 20, 5, 15, 16, 4, 20, 8, 9, 19);
                    line(g, 13, 7, 17, 11);
                }
                case TRASH -> {
                    line(g, 4, 7, 20, 7);
                    poly(g, false, 9, 7, 9, 4, 15, 4, 15, 7);
                    poly(g, false, 6, 7, 7, 20, 17, 20, 18, 7);
                    line(g, 10, 11, 10, 16);
                    line(g, 14, 11, 14, 16);
                }
                case CHECK -> poly(g, false, 5, 12.5, 10, 17.5, 19, 7);
                case CROSS -> {
                    line(g, 6, 6, 18, 18);
                    line(g, 18, 6, 6, 18);
                }
                case LOGOUT -> {
                    poly(g, false, 10, 4, 5, 4, 5, 20, 10, 20);
                    line(g, 9, 12, 20, 12);
                    poly(g, false, 16, 8, 20, 12, 16, 16);
                }
                case SUN -> {
                    g.draw(new Ellipse2D.Double(8, 8, 8, 8));
                    for (int i = 0; i < 8; i++) {
                        double a = Math.PI / 4 * i;
                        line(g, 12 + 7 * Math.cos(a), 12 + 7 * Math.sin(a),
                                12 + 10 * Math.cos(a), 12 + 10 * Math.sin(a));
                    }
                }
                case MOON -> {
                    Area moon = new Area(new Ellipse2D.Double(4, 4, 16, 16));
                    moon.subtract(new Area(new Ellipse2D.Double(10, 1, 14, 14)));
                    g.draw(moon);
                }
                case DASHBOARD -> {
                    g.draw(new RoundRectangle2D.Double(4, 4, 7, 7, 2, 2));
                    g.draw(new RoundRectangle2D.Double(13, 4, 7, 7, 2, 2));
                    g.draw(new RoundRectangle2D.Double(4, 13, 7, 7, 2, 2));
                    g.draw(new RoundRectangle2D.Double(13, 13, 7, 7, 2, 2));
                }
                case DOCUMENT -> {
                    g.draw(new RoundRectangle2D.Double(5, 3, 14, 18, 3, 3));
                    line(g, 8, 9, 16, 9);
                    line(g, 8, 13, 16, 13);
                    line(g, 8, 17, 13, 17);
                }
                case CHART -> {
                    line(g, 5, 20, 5, 13);
                    line(g, 12, 20, 12, 5);
                    line(g, 19, 20, 19, 10);
                }
                case CLOCK -> {
                    g.draw(new Ellipse2D.Double(3, 3, 18, 18));
                    poly(g, false, 12, 7, 12, 12, 16, 14);
                }
                case SEARCH -> {
                    g.draw(new Ellipse2D.Double(4, 4, 12, 12));
                    line(g, 14.5, 14.5, 20, 20);
                }
                case SAVE -> {
                    g.draw(new RoundRectangle2D.Double(4, 4, 16, 16, 3, 3));
                    g.draw(new Rectangle2D.Double(8, 4, 8, 5));
                    g.draw(new Rectangle2D.Double(7, 13, 10, 7));
                }
                case ARROW_LEFT -> {
                    line(g, 19, 12, 5, 12);
                    poly(g, false, 11, 6, 5, 12, 11, 18);
                }
                case ARROW_RIGHT -> {
                    line(g, 5, 12, 19, 12);
                    poly(g, false, 13, 6, 19, 12, 13, 18);
                }
                case LIST -> {
                    line(g, 5, 7, 19, 7);
                    line(g, 5, 12, 19, 12);
                    line(g, 5, 17, 19, 17);
                }
                case HOME -> {
                    poly(g, false, 3, 11, 12, 3, 21, 11);
                    poly(g, false, 5, 10, 5, 21, 19, 21, 19, 10);
                    poly(g, false, 10, 21, 10, 15, 14, 15, 14, 21);
                }
            }
        }

        private static void drawEye(Graphics2D g) {
            Path2D eye = new Path2D.Double();
            eye.moveTo(2, 12);
            eye.curveTo(6, 5, 18, 5, 22, 12);
            eye.curveTo(18, 19, 6, 19, 2, 12);
            g.draw(eye);
            g.draw(new Ellipse2D.Double(9, 9, 6, 6));
        }

        private static void line(Graphics2D g, double x1, double y1, double x2, double y2) {
            g.draw(new Line2D.Double(x1, y1, x2, y2));
        }

        private static void poly(Graphics2D g, boolean close, double... p) {
            Path2D path = new Path2D.Double();
            path.moveTo(p[0], p[1]);
            for (int i = 2; i < p.length; i += 2) {
                path.lineTo(p[i], p[i + 1]);
            }
            if (close) path.closePath();
            g.draw(path);
        }
    }
}