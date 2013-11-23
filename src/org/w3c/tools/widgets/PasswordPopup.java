package org.w3c.tools.widgets;

import java.awt.Button;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextComponent;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @version $Revision: 1.1 $
 * @author  Beno�t Mah� (bmahe@w3.org)
 */
public class PasswordPopup extends Panel implements ActionListener {

    protected TextField user;

    protected TextField passwd;

    protected String orig;

    protected Image img;

    protected boolean ok;

    protected boolean cancel;

    public boolean canceled() {
        return cancel;
    }

    protected synchronized void done(boolean cancel) {
        this.cancel = cancel;
        this.ok = true;
        notifyAll();
    }

    public synchronized boolean waitForCompletion() {
        try {
            wait();
        } catch (InterruptedException ex) {
        }
        return ok;
    }

    public String getUserName() {
        return user.getText();
    }

    public String getPassword() {
        return passwd.getText();
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("Ok") || ae.getSource().equals(passwd)) {
            if (!user.getText().equals("")) {
                done(false);
            } else {
                user.requestFocus();
            }
        } else if (ae.getActionCommand().equals("Cancel")) {
            done(true);
        } else if (ae.getSource().equals(user)) {
            passwd.requestFocus();
        }
    }

    public void init() {
        ok = false;
        user.requestFocus();
    }

    public PasswordPopup() {
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        GridBagLayout mgbl = new GridBagLayout();
        GridBagConstraints mgbc = new GridBagConstraints();
        Label l;
        Button b;
        Panel p = new Panel(gbl);
        ok = false;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.weighty = 0;
        mgbc.fill = GridBagConstraints.NONE;
        mgbc.weightx = 0;
        mgbc.weighty = 0;
        mgbc.insets = new Insets(16, 10, 16, 5);
        setLayout(mgbl);
        user = new TextField(10);
        user.addActionListener(this);
        passwd = new TextField(10);
        passwd.setEchoChar('*');
        passwd.addActionListener(this);
        l = new Label("User: ", Label.RIGHT);
        gbc.gridwidth = 1;
        gbl.setConstraints(l, gbc);
        p.add(l);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(user, gbc);
        p.add(user);
        l = new Label("Password: ", Label.RIGHT);
        gbc.gridwidth = 1;
        gbl.setConstraints(l, gbc);
        p.add(l);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(passwd, gbc);
        p.add(passwd);
        mgbc.gridwidth = GridBagConstraints.REMAINDER;
        mgbl.setConstraints(p, mgbc);
        add(p);
        p = new Panel(new GridLayout(1, 2, 20, 20));
        b = new Button("Ok");
        b.addActionListener(this);
        p.add(b);
        b = new Button("Cancel");
        b.addActionListener(this);
        p.add(b);
        mgbl.setConstraints(p, mgbc);
        add(p);
    }
}
