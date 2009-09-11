/*
 * The GeoConv project.
 * Copyright (C) 2009 Oleh Faizulin <http://devsk.blogspot.com>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.blogspot.devsk.l2j.geoconv;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import com.blogspot.devsk.l2j.geoconv.threading.GeoConvThreadFactory;
import com.blogspot.devsk.l2j.geoconv.threading.ParseTask;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;

public class GeoGonv {

	public static final File OUT_DIR = new File("generated");

	public static void main(String[] args) {

		if (args == null || args.length == 0) {
			System.out.println("File name was not specified, [\\d]{1,2}_[\\d]{1,2}.txt will be used");
			args = new String[]{"[\\d]{1,2}_[\\d]{1,2}.txt"};
		}

		File dir = new File(".");
		File[] files = dir.listFiles((FileFilter) new RegexFileFilter(args[0]));

		ArrayList<File> checked = new ArrayList<File>();
		for (File file : files) {
			if (file.isDirectory() || file.isHidden() || !file.exists()) {
				System.out.println(file.getAbsoluteFile() + " was ignored.");
			} else {
				checked.add(file);
			}
		}

		if (OUT_DIR.exists() && OUT_DIR.isDirectory() && OUT_DIR.listFiles().length > 0) {
			try {
				System.out.println("Directory with generated files allready exists, making backup...");
				FileUtils.moveDirectory(OUT_DIR, new File("generated-backup-" + System.currentTimeMillis()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (!OUT_DIR.exists()) {
			OUT_DIR.mkdir();
		}

		for (File file : checked) {
			GeoConvThreadFactory.startThread(new ParseTask(file));
		}
	}
}
