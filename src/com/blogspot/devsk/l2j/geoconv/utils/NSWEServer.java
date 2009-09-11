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

package com.blogspot.devsk.l2j.geoconv.utils;

public enum NSWEServer {

	N(8),
	S(4),
	W(2),
	E(1),
	NSWE(8 | 4 | 2 | 1);

	private byte value;

	NSWEServer(int i) {
		this.value = (byte) i;
	}

	public byte getValue() {
		return value;
	}
}
