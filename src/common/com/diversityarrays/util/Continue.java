/*
 * dalserver-interop library - implementation of DAL server for interoperability
 * Copyright (C) 2015  Diversity Arrays Technology
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.diversityarrays.util;


public class Continue {
	
	static public Continue CONTINUE = new Continue(null, true);

	static public Continue STOP = new Continue(null, false);

	static public Continue error(Throwable t) {
		return new Continue(t, false);
	}

	public final Throwable throwable;
	public final boolean shouldContinue;

	private Continue(Throwable t, boolean b) {
		this.throwable = t;
		this.shouldContinue = b;
	}
	
	@Override
	public String toString() {
		if (isError()) {
			return "Error: " + throwable.getMessage(); 
		}
		return shouldContinue ? "CONTINUE" : "STOP";
	}
	
	public boolean isError() {
		return throwable != null;
	}
}