package org.figuramc.luaj.vm2.lib;

import org.figuramc.luaj.vm2.LuaValue;

class TableLibFunction extends LibFunction {
	@Override
	public LuaValue call() {
		return argerror(1, "table expected, got no value");
	}
}
