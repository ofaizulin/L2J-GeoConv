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

import com.blogspot.devsk.l2j.geoconv.GeoGonv;
import com.blogspot.devsk.l2j.geoconv.model.Block;
import com.blogspot.devsk.l2j.geoconv.model.Region;
import com.blogspot.devsk.l2j.geoconv.utils.BlockType;
import com.blogspot.devsk.l2j.geoconv.utils.Utils;

import java.io.*;

public class ParseTask implements Runnable {

	private final File file;
	private final Region region;

	public ParseTask(File file) {
		this.file = file;
		region = Utils.createRegion(file.getName());
	}

	public void run() {
		System.out.println("Parsing region " + region);
		parseFile();
		System.out.println("Region " + region + " parsed, dumping file");
		dumpRegion();
		System.out.println("Region " + region + " generated");
	}

	public void parseFile() {

		LineNumberReader lnr = null;
		try {
			lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(file)));

			String s;
			while ((s = lnr.readLine()) != null) {
				if (!s.startsWith("[")) {
					continue;
				}

				parseCell(s);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (lnr != null) {
					lnr.close();
				}
			} catch (IOException e) {
				// ignore
			}
		}
	}

	public void parseCell(String cellString) {

		String cellCoords = cellString.substring(cellString.indexOf('['), cellString.indexOf(']') + 1);
		cellString = cellString.replace(cellCoords, "");
		cellCoords = cellCoords.substring(1, cellCoords.length() - 1);

		String[] cellXY = cellCoords.split(",");

		int x = Integer.parseInt(cellXY[0]);
		int y = Integer.parseInt(cellXY[1]);

		int blockIndex = Utils.getBlockIndex(x, y);
		int cellIndex = Utils.getCellIndex(x, y);

		char[] cellStringChars = cellString.toCharArray();
		int size = 0;
		for (char cellStringChar : cellStringChars) {
			if (Character.isDigit(cellStringChar)) {
				size++;
			} else {
				break;
			}
		}

		String layersStr = cellString.substring(0, size);
		int layers = Integer.parseInt(layersStr);
		cellString = cellString.replaceFirst(Integer.toString(layers), "");

		short[] layerArr = new short[layers];
		for (int i = 0; i < layers; i++) {
			int index = cellString.indexOf(')') + 1;
			String layerString = cellString.substring(0, index);
			cellString = cellString.substring(index);
			layerString = layerString.substring(1, layerString.length() - 1);
			String[] hNSWE = layerString.split(":");

			int h = Integer.parseInt(hNSWE[0]) & 0xfff0;
			int NSWE = Integer.parseInt(hNSWE[1], 2) & 0x0f;
			NSWE = Utils.toServerNSWE(NSWE);

			layerArr[i] = (short) (h | NSWE);
		}

		Block block = region.getBlocks()[blockIndex];

		if (block == null) {
			block = new Block();
			region.getBlocks()[blockIndex] = block;
		}

		block.getCells()[cellIndex] = layerArr;
	}

	public void dumpRegion() {

		File result = new File(GeoGonv.OUT_DIR, region.toString() + ".l2j");

		try {
			result.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Can't create file: " + result.getAbsoluteFile());
			return;
		}

		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(result);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		for (Block block : region.getBlocks()) {
			BlockType blockType = Utils.getBlockType(block);
			switch(blockType){
				case FLAT:
					dumpFlat(block, outputStream);
					break;
				case COMPLEX:
					dumpComplex(block, outputStream);
					break;
				case MULTILEVEL:
					dumpMultilevel(block, outputStream);
					break;
			}
		}

		try{
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void dumpFlat(Block block, OutputStream out){

		try {
			out.write(BlockType.FLAT.getType());
			out.write(Utils.makeBytes(block.getCells()[0][0]));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void dumpComplex(Block block, OutputStream out){
		try{
			out.write(BlockType.COMPLEX.getType());
			for(short[] cell : block.getCells()){
				if(cell.length == 0){
					int height = (16000 & 0xfff0) << 1;
					height = height & 0xfff0;
					out.write(Utils.makeBytes((short) height));
				} else {
					short value = cell[0];
					int h = (value & 0xfff0) << 1;
					int nswe = value & 0x0f;
					out.write(Utils.makeBytes((short) (h | nswe)));
				}
			}
		}catch (IOException e){
			e.printStackTrace();
		}
	}

	public void dumpMultilevel(Block block, OutputStream out){
		try {
			out.write(BlockType.MULTILEVEL.getType());

			for(short[] cells : block.getCells()){
				out.write((byte)cells.length);
				for(short value : cells){

					int h = (value & 0xfff0) << 1;
					int nswe = value & 0x0f;
					out.write(Utils.makeBytes((short) (h | nswe)));
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
