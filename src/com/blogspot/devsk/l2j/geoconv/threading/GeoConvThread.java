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

package com.blogspot.devsk.l2j.geoconv.threading;

import java.util.concurrent.Semaphore;

public class GeoConvThread extends Thread {

	private final Runnable task;
	private final Semaphore semaphore;

	public GeoConvThread(Runnable task, Semaphore semaphore) {
		this.task = task;
		this.semaphore = semaphore;
	}

	public void run(){

		if(task != null){
			try{
				task.run();
			} catch(Throwable t){
				t.printStackTrace();
			}
		}

		semaphore.release();
	}
}
