package client.utils;

import javax.swing.*;

public class Utils {
    public static JMenu addAll(JMenu menu, JMenuItem... menuItems) {
        for (JMenuItem menuItem : menuItems) {
            menu.add(menuItem);
        }
        return menu;
    }

    public static JMenuBar addAll(JMenuBar menuBar, JMenu... menus) {
        for (JMenu menu : menus) {
            menuBar.add(menu);
        }
        return menuBar;
    }
}
