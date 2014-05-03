/*
 * Copyright 2014 Martin Steiger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.myirc;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class MyMain {
    public static void main(String[] args) {
        if (!SystemTray.isSupported()) 
        {
            throw new UnsupportedOperationException("No system tray!");
        }
        
        final TrayIcon trayIcon = new TrayIcon(loadImage("icons/irc.png"), "MyIRC Bot");
        trayIcon.setImageAutoSize(true);

        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException e) {
            showError("Could not add tray icon", e);
        }

        final MyBot bot = new MyBot(trayIcon, "msteiger_bot");

        PopupMenu menu = new PopupMenu();
        
        final CheckboxMenuItem showJoinParts = new CheckboxMenuItem("Show Joins/Parts", true);
        showJoinParts.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                bot.showJoinsParts(showJoinParts.getState());
            }
        });
        bot.showJoinsParts(showJoinParts.getState());
        
        final CheckboxMenuItem showAllMessages = new CheckboxMenuItem("Show all messages", true);
        showAllMessages.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                bot.showAllMessages(showAllMessages.getState());
            }
        });
        bot.showAllMessages(showAllMessages.getState());
        
        MenuItem quitItem = new MenuItem("Quit");
        quitItem.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                bot.disconnect();
                SystemTray.getSystemTray().remove(trayIcon);
            }
        });
        menu.add(showAllMessages);
        menu.add(showJoinParts);
        menu.add(new MenuItem("-"));
        menu.add(quitItem);
        trayIcon.setPopupMenu(menu);
        
        bot.setVerbose(true);
        try {
            bot.connect("irc.freenode.net");
            bot.joinChannel("#terasology");
        } catch (Exception e) {
            showError("Could not connect to server", e);
            SystemTray.getSystemTray().remove(trayIcon);
        }
    }
    

    private static void showError(String text, Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String msg = text + System.lineSeparator() + sw.toString(); 
                
        JOptionPane.showMessageDialog(null, msg, "An error occurred", JOptionPane.ERROR_MESSAGE);
    }


    private static BufferedImage loadImage(String fname) {
        try {
            String fullPath = "/" + fname;
            URL rsc = MyMain.class.getResource(fullPath);
            if (rsc == null) {
                throw new FileNotFoundException(fullPath);
            }
            return ImageIO.read(rsc);
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }
    
}
