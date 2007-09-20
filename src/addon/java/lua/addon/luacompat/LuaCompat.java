package lua.addon.luacompat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import lua.CallInfo;
import lua.GlobalState;
import lua.Lua;
import lua.StackState;
import lua.VM;
import lua.io.Closure;
import lua.io.LoadState;
import lua.io.Proto;
import lua.value.LBoolean;
import lua.value.LDouble;
import lua.value.LFunction;
import lua.value.LInteger;
import lua.value.LNil;
import lua.value.LNumber;
import lua.value.LString;
import lua.value.LTable;
import lua.value.LValue;

public class LuaCompat extends LFunction {

	public static InputStream STDIN = null;
	public static PrintStream STDOUT = System.out;
	public static LTable      LOADED = new LTable();
	
	public static void install() {
		LTable globals = GlobalState.getGlobalsTable();
		installNames( globals, GLOBAL_NAMES,  GLOBALS_BASE  );

		// math lib
		LTable math = new LTable();
		installNames( math, MATH_NAMES,  MATH_BASE  );
		math.put( "huge", new LDouble( Double.MAX_VALUE ) );
		math.put( "pi", new LDouble( Math.PI ) );		
		globals.put( "math", math );
		
		// string lib
		LTable string = LString.getMetatable();
		installNames( string, STRING_NAMES,  STRING_BASE  );
		globals.put( "string", string );

		// packages lib
		LTable pckg = new LTable();
		installNames( pckg,  PACKAGE_NAMES, PACKAGES_BASE );
		globals.put( "package", pckg );
		pckg.put( "loaded", LOADED );	
	}

	private static void installNames( LTable table, String[] names, int indexBase ) {
		for ( int i=0; i<names.length; i++ )
			table.put( names[i], new LuaCompat(indexBase+i) );
	}

	public static final String[] GLOBAL_NAMES = {
		"assert",
		"loadfile",
		"tonumber",
		"rawget",
		"setfenv",
		"select",
		"collectgarbage",
		"dofile",
		"loadstring",
		"load",
		"tostring",
		"unpack",
		"next",
		"module",
		"require",
	};
	
	public static final String[] MATH_NAMES = {
		"abs",
		"max",
		"min",
		"modf",
		"sin"
	};
	
	public static final String[] STRING_NAMES = {
		"byte",
		"char",
		"dump",
		"find",
		"format",
		"gmatch",
		"gsub",
		"len",
		"lower",
		"match",
		"rep",
		"reverse",
		"sub",
		"upper",
	};

	public static final String[] PACKAGE_NAMES = {
		"loalib",
		"seeall",
	};
	
	private static final int GLOBALS_BASE = 0;
	private static final int ASSERT         = GLOBALS_BASE + 0;
	private static final int LOADFILE       = GLOBALS_BASE + 1;
	private static final int TONUMBER       = GLOBALS_BASE + 2;
	private static final int RAWGET         = GLOBALS_BASE + 3;
	private static final int SETFENV        = GLOBALS_BASE + 4;
	private static final int SELECT         = GLOBALS_BASE + 5;
	private static final int COLLECTGARBAGE = GLOBALS_BASE + 6;
	private static final int DOFILE         = GLOBALS_BASE + 7;
	private static final int LOADSTRING     = GLOBALS_BASE + 8;
	private static final int LOAD           = GLOBALS_BASE + 9;
	private static final int TOSTRING       = GLOBALS_BASE + 10;
	private static final int UNPACK         = GLOBALS_BASE + 11;
	private static final int NEXT           = GLOBALS_BASE + 12;
	private static final int MODULE         = GLOBALS_BASE + 13;
	private static final int REQUIRE        = GLOBALS_BASE + 14;
	
	
	private static final int MATH_BASE = 20;
	private static final int ABS     = MATH_BASE + 0;
	private static final int MAX     = MATH_BASE + 1;
	private static final int MIN     = MATH_BASE + 2;
	private static final int MODF    = MATH_BASE + 3;
	private static final int SIN     = MATH_BASE + 4;
	
	private static final int STRING_BASE = 30;
	private static final int BYTE    = STRING_BASE + 0;
	private static final int CHAR    = STRING_BASE + 1;
	private static final int DUMP    = STRING_BASE + 2;
	private static final int FIND    = STRING_BASE + 3;
	private static final int FORMAT  = STRING_BASE + 4;
	private static final int GMATCH  = STRING_BASE + 5;
	private static final int GSUB    = STRING_BASE + 6;
	private static final int LEN     = STRING_BASE + 7;
	private static final int LOWER   = STRING_BASE + 8;
	private static final int MATCH   = STRING_BASE + 9;
	private static final int REP     = STRING_BASE + 10;
	private static final int REVERSE = STRING_BASE + 11;
	private static final int SUB     = STRING_BASE + 12;
	private static final int UPPER   = STRING_BASE + 13;

	private static final int PACKAGES_BASE = 50;
	private static final int LOADLIB = PACKAGES_BASE + 0;
	private static final int SEEALL  = PACKAGES_BASE + 1;	
	
	private final int id;

	private LuaCompat( int id ) {
		this.id = id;
	}
	
	public boolean luaStackCall( VM vm ) {
		switch ( id ) {
		case ASSERT: {
			if ( !vm.getArgAsBoolean(0) ) {
				String message;
				if ( vm.getArgCount() > 1 ) {
					message = vm.getArgAsString(1);
				} else {
					message = "assertion failed!";
				}
				throw new RuntimeException(message);
			}
			vm.setResult();
		}	break;
		case LOADFILE:
			loadfile(vm, vm.getArgAsString(0));
			break;
		case TONUMBER:
			vm.setResult( toNumber( vm ) );
			break;
		case RAWGET: {
			LValue t = vm.getArg(0);;
			LValue k = vm.getArg(1);
			LValue result = LNil.NIL;
			if ( t instanceof LTable ) {
				result = ( (LTable) t ).get( k );
			}
			vm.setResult( result );
		}	break;
		case SETFENV:
			setfenv( (StackState) vm );
			break;
		case SELECT:
			select( vm );
			break;
		case COLLECTGARBAGE:
			System.gc();
			vm.setResult();
			break;
		case DOFILE:
			return dofile(vm, vm.getArgAsString(0));
		case LOADSTRING:
			loadstring(vm, vm.getArg(0), vm.getArgAsString(1));
			break;
		case LOAD:
			load(vm, vm.getArg(0), vm.getArgAsString(1));
			break;
		case TOSTRING:
			vm.setResult( tostring(vm, vm.getArg(0)) );
			break;
		case UNPACK:
			vm.setResult();
			unpack(vm, vm.getArg(0), vm.getArgAsInt(1), vm.getArgAsInt(2));
			break;
		case NEXT:
			vm.setResult( next(vm, vm.getArg(0), vm.getArgAsInt(1)) );
			break;
		case MODULE: 
			module(vm);
			break;
		case REQUIRE: 
			require(vm);
			break;
		
		// Math functions
		case ABS:
			vm.setResult( abs( vm.getArg( 0 ) ) );
			break;
		case MAX:
			vm.setResult( max( vm.getArg( 0 ), vm.getArg( 1 ) ) );
			break;
		case MIN:
			vm.setResult( min( vm.getArg( 0 ), vm.getArg( 1 ) ) );
			break;
		case MODF:
			modf( vm );
			break;
		case SIN:
			vm.setResult( new LDouble( Math.sin( vm.getArgAsDouble( 0 ) ) ) );
			break;
		
		// String functions
		case BYTE:
			StrLib.byte_( vm );
			break;
		case CHAR:
			StrLib.char_( vm );
			break;
		case DUMP:
			StrLib.dump( vm );
			break;
		case FIND:
			StrLib.find( vm );
			break;
		case FORMAT:
			StrLib.format( vm );
			break;
		case GMATCH:
			StrLib.gmatch( vm );
			break;
		case GSUB:
			StrLib.gsub( vm );
			break;
		case LEN:
			StrLib.len( vm );
			break;
		case LOWER:
			StrLib.lower( vm );
			break;
		case MATCH:
			StrLib.match( vm );
			break;
		case REP:
			StrLib.rep( vm );
			break;
		case REVERSE:
			StrLib.reverse( vm );
			break;
		case SUB:
			StrLib.sub( vm );
			break;
		case UPPER:
			StrLib.upper( vm );
			break;

		// package functions
		case LOADLIB: 
			loadlib(vm);
			break;
		case SEEALL: 
			seeall(vm);
			break;
			
		default:
			luaUnsupportedOperation();
		}
		return false;
	}
	private void select( VM vm ) {
		LValue arg = vm.getArg( 0 );
		if ( arg instanceof LNumber ) {
			final int start;
			final int numResults;
			if ( ( start = arg.luaAsInt() ) > 0 &&
				 ( numResults = Math.max( vm.getArgCount() - start,
						 				  vm.getExpectedResultCount() ) ) > 0 ) {
				// since setResult trashes the arguments, we have to save them somewhere.
				LValue[] results = new LValue[numResults];
				for ( int i = 0; i < numResults; ++i ) {
					results[i] = vm.getArg( i+start );
				}
				vm.setResult();
				for ( int i = 0; i < numResults; ++i ) {
					vm.push( results[i] );
				}
				return;
			}
		} else if ( arg.luaAsString().equals( "#" ) ) {
			vm.setResult( new LInteger( vm.getArgCount() - 1 ) );
		}
		vm.setResult();
	}
	
	private LValue abs( final LValue v ) {
		LValue nv = v.luaUnaryMinus();
		return max( v, nv );
	}
	
	private LValue max( LValue lhs, LValue rhs ) {
		return rhs.luaBinCmpUnknown( Lua.OP_LT, lhs ) ? rhs: lhs;
	}
	
	private LValue min( LValue lhs, LValue rhs ) {
		return rhs.luaBinCmpUnknown( Lua.OP_LT, lhs ) ? lhs: rhs;
	}
	
	private void modf( VM vm ) {
		LValue arg = vm.getArg( 0 );
		double v = arg.luaAsDouble();
		double intPart = ( v > 0 ) ? Math.floor( v ) : Math.ceil( v );
		double fracPart = v - intPart;
		vm.setResult();
		vm.push( intPart );
		vm.push( fracPart );
	}
	
	private LValue toNumber( VM vm ) {
		LValue input = vm.getArg(0);
		if ( input instanceof LNumber ) {
			return input;
		} else if ( input instanceof LString ) {
			int base = 10;
			if ( vm.getArgCount()>1 ) {
				base = vm.getArgAsInt(1);
			}
			return ( (LString) input ).luaToNumber( base );
		}
		return LNil.NIL;
	}
	
	private void setfenv( StackState state ) {
		LValue f = state.getArg(0);
		LValue newenv = state.getArg(1);

		Closure c = null;
		
		// Lua reference manual says that first argument, f, can be a "Lua
		// function" or an integer. Lots of things extend LFunction, but only
		// instances of Closure are "Lua functions".
		if ( f instanceof Closure ) {
			c = (Closure) f;
		} else {
			int callStackDepth = f.luaAsInt();
			if ( callStackDepth > 0 ) {
				CallInfo frame = state.getStackFrame( callStackDepth );
				if ( frame != null ) {
					c = frame.closure;
				}
			} else {
				// This is supposed to set the environment of the current
				// "thread". But, we have not implemented coroutines yet.
				throw new RuntimeException( "not implemented" );
			}
		}
		
		if ( c != null ) {
			if ( newenv instanceof LTable ) {
				c.env = (LTable) newenv;
			}
			state.setResult( c );
			return;
		}
		
		state.setResult();
		return;
	}

	// closes the input stream, provided its not null or System.in
	private static void closeSafely(InputStream is) {
		try {
			if ( is != null && is != STDIN )
				is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// closes the output stream, provided its not null or STDOUT 
	private static void closeSafely(OutputStream os) {
		try {
			if ( os != null && os != STDOUT )
				os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// return true if laoded, false if error put onto the stack
	private static boolean loadis(VM vm, InputStream is, String chunkname ) {
		try {
			if ( 0 != vm.lua_load(is, chunkname) ) {
				vm.setErrorResult( LNil.NIL, "cannot load "+chunkname+": "+vm.getArgAsString(0) );
				return false;
			} else {
				return true;
			}
		} finally {
			closeSafely( is );
		}
	}
	

	// return true if loaded, false if error put onto stack
	public static boolean loadfile( VM vm, String fileName ) {
		InputStream is;
		
		String script;
		if ( ! "".equals(fileName) ) {
			script = fileName;
			is = vm.getClass().getResourceAsStream( "/"+script );
			if ( is == null ) {
				vm.setErrorResult( LNil.NIL, "cannot open "+fileName+": No such file or directory" );
				return false;
			}
		} else {
			is = STDIN;
			script = "-";
		}
		
		// use vm to load the script
		return loadis( vm, is, script );
	}
	
	// return true if call placed on the stack & ready to execute, false otherwise
	private boolean dofile( VM vm, String fileName ) {
		if ( loadfile( vm, fileName ) ) {
			vm.prepStackCall(); // TODO: is this necessary? 
			return true;
		} else {
			return false;
		}
	}

	// return true if loaded, false if error put onto stack
	private boolean loadstring(VM vm,  LValue string, String chunkname) {
		return loadis( vm, 
				string.luaAsString().toInputStream(), 
				("".equals(chunkname)? "(string)": chunkname) );
	}

	// return true if loaded, false if error put onto stack
	private boolean load(VM vm, LValue chunkPartLoader, String chunkname) {
		if ( ! (chunkPartLoader instanceof Closure) ) {
			vm.lua_error("not a closure: "+chunkPartLoader);
		}
		
		// load all the parts
		Closure c = (Closure) chunkPartLoader;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			while ( true ) {
				vm.setResult(c);
				if ( 0 != vm.lua_pcall(0, 1) ) {
					vm.setErrorResult(LNil.NIL, vm.getArgAsString(0));
					return false;
				}
				LValue v = vm.getArg(0);
				if ( v == LNil.NIL )
					break;
				LString s = v.luaAsString();
				s.write(baos, 0, s.length());
			}

			// load the chunk
			return loadis( vm, 
					new ByteArrayInputStream( baos.toByteArray() ), 
					("".equals(chunkname)? "=(load)": chunkname) );
			
		} catch (IOException ioe) {
			vm.setErrorResult(LNil.NIL, ioe.getMessage());
			return false;
		} finally {
			closeSafely( baos );
		}
	}

	private LValue tostring(VM vm, LValue arg) {
		return arg.luaAsString();
	}

	private static LTable toTable(VM vm, LValue list) {
		if ( ! (list instanceof LTable) ) {
			vm.lua_error("not a list: "+list);
			return null;
		}
		return (LTable) list;
	}
	
	private void unpack(VM vm, LValue list, int i, int j) {
		LTable t = toTable(vm, list);
		if ( t == null )
			return; 
		if ( i == 0 )
			i = 1;
		if ( j == 0 )
			j = t.size();
		LValue[] keys = t.getKeys();
		for ( int k=i; k<=j; k++ )
			vm.push( t.get(keys[k-1]) );
	}

	private LValue next(VM vm, LValue table, int index) {
		throw new java.lang.RuntimeException("next() not supported yet");
	}

	
	// ======================== Module, Package loading =============================
	
	public static void module( VM vm ) {		
		vm.lua_error( "module not implemented" );
	}

	/** 
	 * require (modname)
	 * 
	 * Loads the given module. The function starts by looking into the package.loaded table to 
	 * determine whether modname is already loaded. If it is, then require returns the value 
	 * stored at package.loaded[modname]. Otherwise, it tries to find a loader for the module.
	 * 
	 * To find a loader, require is guided by the package.loaders array. By changing this array, 
	 * we can change how require looks for a module. The following explanation is based on the 
	 * default configuration for package.loaders.
	 *  
	 * First require queries package.preload[modname]. If it has a value, this value 
	 * (which should be a function) is the loader. Otherwise require searches for a Lua loader 
	 * using the path stored in package.path. If that also fails, it searches for a C loader 
	 * using the path stored in package.cpath. If that also fails, it tries an all-in-one loader 
	 * (see package.loaders).
	 * 
	 * Once a loader is found, require calls the loader with a single argument, modname. 
	 * If the loader returns any value, require assigns the returned value to package.loaded[modname]. 
	 * If the loader returns no value and has not assigned any value to package.loaded[modname], 
	 * then require assigns true to this entry. In any case, require returns the final value of 
	 * package.loaded[modname]. 
	 * 
	 * If there is any error loading or running the module, or if it cannot find any loader for 
	 * the module, then require signals an error.
	 */	
	public static void require( VM vm ) {
		LString modname = vm.getArgAsLuaString(0);
		if ( LOADED.containsKey(modname) )
			vm.setResult( LOADED.get(modname) );
		else {
			String s = modname.toJavaString();
			if ( ! loadfile(vm, s+".luac") && ! loadfile(vm, s+".lua") )
				vm.lua_error( "not found: "+s );
			else if ( 0 == vm.lua_pcall(0, 1) ) {
				LValue result = vm.getArg(0); 
				if ( result != LNil.NIL )
					LOADED.put(modname, result);
				else if ( ! LOADED.containsKey(modname) ) {
					LOADED.put(modname, LBoolean.TRUE);
					vm.setResult( LBoolean.TRUE );
				}
			}
		}
	}

	public static void loadlib( VM vm ) {
		vm.lua_error( "loadlib not implemented" );
	}
	
	public static void seeall( VM vm ) {
		vm.lua_error( "seeall not implemented" );
	}
	
	
}
