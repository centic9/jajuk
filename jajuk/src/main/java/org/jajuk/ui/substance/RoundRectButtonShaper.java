/*
 * Adapted by Jajuk team
 * Copyright (C) 2003-2009 the Jajuk Team
 * http://jajuk.info
 * 
 * aTunes 1.14.0
 * Copyright (C) 2006-2009 Alex Aranda, Sylvain Gaudard, Thomas Beckers and contributors
 *
 * See http://www.atunes.org/wiki/index.php?title=Contributing for information about contributors
 *
 * http://www.atunes.org
 * http://sourceforge.net/projects/atunes
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package org.jajuk.ui.substance;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;

import javax.swing.AbstractButton;

/*
 * based on code from Xtreme Media Player
 */
/**
 * The Class RoundRectButtonShaper.
 */
public class RoundRectButtonShaper extends ButtonShaper {

  public String getDisplayName() {
    return "RoundRect";
  }

  public GeneralPath getButtonOutline(AbstractButton button) {
    int width = button.getWidth() - 1;
    int height = button.getHeight() - 1;

    Shape shape = new RoundRectangle2D.Double(0, 0, width, height, width / 3d, height / 3d);
    GeneralPath generalPath = new GeneralPath(shape);

    return generalPath;
  }

}