/*
 * This file has been adapted to Jajuk by the Jajuk Team.
 * Jajuk Copyright (C) 2007 The Jajuk Team
 * 
 * Found at http://www.jroller.com/santhosh/date/20050528
 * Original copyright information follows:
 *
 * Copyright santhosh kumar
 * 
 * @author santhosh kumar - santhosh@in.fiorano.com Drop down button
 */

package ext;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.jajuk.ui.widgets.JajukButton;
import org.jajuk.ui.widgets.JajukJToolbar;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;

public abstract class DropDownButton extends JajukButton implements ChangeListener,
    PopupMenuListener, ActionListener, PropertyChangeListener, Const {

  private final JButton arrowButton;

  private boolean popupVisible = false;

  public DropDownButton(ImageIcon icon) {
    super(icon);
    if (icon.getIconWidth() < 20) {
      arrowButton = new JajukButton(IconLoader.getIcon(JajukIcons.DROP_DOWN_16X16));
    } else {
      arrowButton = new JajukButton(IconLoader.getIcon(JajukIcons.DROP_DOWN_32X32));
    }
    getModel().addChangeListener(this);
    arrowButton.getModel().addChangeListener(this);
    arrowButton.addActionListener(this);
    arrowButton.setBorder(null);
    arrowButton.setMargin(new Insets(1, 0, 1, 0));
    addPropertyChangeListener("enabled", this); // NOI18N
  }

  /*------------------------------[ PropertyChangeListener ]---------------------------------------------------*/

  public void propertyChange(PropertyChangeEvent evt) {
    arrowButton.setEnabled(isEnabled());
  }

  /*------------------------------[ ChangeListener ]---------------------------------------------------*/

  public void stateChanged(ChangeEvent e) {
    if (e.getSource() == getModel()) {
      if (popupVisible && !getModel().isRollover()) {
        getModel().setRollover(true);
        return;
      }
      arrowButton.getModel().setRollover(getModel().isRollover());
      arrowButton.setSelected(getModel().isArmed() && getModel().isPressed());
    } else {
      if (popupVisible && !arrowButton.getModel().isSelected()) {
        arrowButton.getModel().setSelected(true);
        return;
      }
      getModel().setRollover(arrowButton.getModel().isRollover());
    }
  }

  /*------------------------------[ ActionListener ]---------------------------------------------------*/

  public void actionPerformed(ActionEvent ae) {
    JPopupMenu popup = getPopupMenu();
    popup.addPopupMenuListener(this);
    popup.show(this, 0, getHeight());
  }

  /*------------------------------[ PopupMenuListener ]---------------------------------------------------*/

  public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    popupVisible = true;
    getModel().setRollover(true);
    arrowButton.getModel().setSelected(true);
  }

  public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    popupVisible = false;

    getModel().setRollover(false);
    arrowButton.getModel().setSelected(false);
    ((JPopupMenu) e.getSource()).removePopupMenuListener(this);
    // act as good programmer :)
  }

  public void popupMenuCanceled(PopupMenuEvent e) {
    popupVisible = false;
  }

  /*------------------------------[ Other Methods ]---------------------------------------------------*/

  protected abstract JPopupMenu getPopupMenu();

  public JButton addToToolBar(JToolBar toolbar) {
    JToolBar tempBar = new JajukJToolbar();
    tempBar.setAlignmentX(0.5f);
    tempBar.add(this);
    tempBar.add(arrowButton);
    toolbar.add(tempBar);
    return this;
  }
}
