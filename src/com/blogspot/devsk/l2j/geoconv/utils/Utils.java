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

import com.blogspot.devsk.l2j.geoconv.model.Block;
import com.blogspot.devsk.l2j.geoconv.model.Region;

public class Utils {

	public static int getBlockIndex(int x, int y) {
		int bx = x / 8;
		int by = y / 8;

		return (bx << 8) + by;
	}

	public static int getCellIndex(int x, int y) {
		int bx = x % 8;
		int by = y % 8;

		return (bx << 3) + by;
	}

	public static short validateCellData(short height, byte NSWE, BlockType blockType){
		if(BlockType.FLAT.equals(blockType)){
			int h = (height & 0xfff0);
			int nswe = NSWE & 0x0f;
			return (short) (h | nswe);
		} else {
			int h = height & 0xfff0;

			// Why we are dividing height in L2J code by 2? We can make info correct at the time of generation
			// Seems that it is some crap that must be cleared
			h = h << 1;

			int nswe = NSWE & 0x0f;
			return (short) (h | nswe);
		}
	}

	public static Region createRegion(String fileName){
		String region = fileName.substring(0, fileName.indexOf('.'));
		String[] xy = region.split("_");
		return new Region(Integer.parseInt(xy[0]), Integer.parseInt(xy[1]));
	}

	public static BlockType getBlockType(Block block){

		short[][] cellsWithLayers = block.getCells();

		int layerCount = 0;
		for(short[] layeredCell : cellsWithLayers){
			layerCount = Math.max(layerCount, layeredCell.length);
		}

		if(layerCount > 1){
			return BlockType.MULTILEVEL;
		} else {

			for(short[] layeredCell : cellsWithLayers){
				if(layeredCell.length == 0){
					return BlockType.COMPLEX;
				}
			}

			short first = cellsWithLayers[0][0];

			for(short[] layeredCell : cellsWithLayers){
				if(layeredCell[0] != first){
					return BlockType.COMPLEX;
				}
			}

			if((first & 0x0f) != (NSWEClient.NSWE.getValue())){
				System.out.println("Flat block represented as complex: " + Integer.toBinaryString(first & 0x0f));
				return BlockType.COMPLEX;
			}

			return BlockType.FLAT;
		}
	}

	public static short makeShort(byte first, byte second) {
		return (short) (second << 8 | first & 0xff);
	}

	public static byte[] makeBytes(short value){
		byte[] b = new byte[2];
		b[0] = (byte) (value & 0xff);
		b[1] = (byte) (value >> 8);
		return b;
	}

	public static byte toServerNSWE(int clientNSWE){

		clientNSWE = clientNSWE & 0x0f;
		int result = 0;

		if((clientNSWE & NSWEClient.N.getValue()) != 0){
			result |= NSWEServer.N.getValue();
		}

		if((clientNSWE & NSWEClient.S.getValue()) != 0){
			result |= NSWEServer.S.getValue();
		}

		if((clientNSWE & NSWEClient.W.getValue()) != 0){
			result |= NSWEServer.W.getValue();
		}

		if((clientNSWE & NSWEClient.E.getValue()) != 0){
			result |= NSWEServer.E.getValue();
		}

		return (byte) result;
	}
}
