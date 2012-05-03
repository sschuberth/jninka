package org.whitesource.jninka.gui.images;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

/**
 * Copyright (C) 2012 White Source (www.whitesourcesoftware.com)
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This patch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this patch.  If not, see <http://www.gnu.org/licenses/>.
 */
public class ImageManager {
	
	private static ImageManager instance;

	private ImageManager() {
	}

	public static ImageManager getInstance() {
		if (instance == null) {
			instance = new ImageManager();
		}
		return instance;
	}

	public Image getLogoIcon() {
		return getImage("wss.png");
	}

	public Image getImage(String imageName) {
		URL url = this.getClass().getResource(imageName);
		Image image = Toolkit.getDefaultToolkit().getImage(url);
		return image;
	}

}
