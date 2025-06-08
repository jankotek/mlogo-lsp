package com.bme.mlogo;

import com.bme.logo.*;
import java.io.*;
import java.util.*;
import static com.bme.logo.Primitives.*;

public class MLogo {
	static final String version = "MLogo 0.1";

	PrintStream out = System.out;

	private boolean callSystemExit = false;

	public static void main(String[] a) {
		MLogo mlogo = new MLogo();
		mlogo.callSystemExit=true;
		mlogo.main2(a);
	}

	public void main2(String[] a) {
		List<String> args = new ArrayList<String>(Arrays.asList(a));
		callSystemExit = true;

		boolean printHelp   = args.size() == 0;
		boolean interactive = false;
		boolean turtles     = false;
		boolean trace       = false;

		for(int z = args.size() - 1; z >= 0; z--) {
			if ("-h".equals(args.get(z))) { printHelp   = true; args.remove(z--); continue; }
			if ("-i".equals(args.get(z))) { interactive = true; args.remove(z--); continue; }
			if ("-t".equals(args.get(z))) { turtles     = true; args.remove(z--); continue; }
			if ("-T".equals(args.get(z))) { trace       = true; args.remove(z--); continue; }
		}

		if (printHelp) {
			out.println(version);
			out.println("usage: MLogo [-hit] file ...");
			out.println();
			out.println(" h : print this help message");
			out.println(" i : provide an interactive REPL session");
			out.println(" t : enable turtle graphics during batch mode");
			out.println(" T : enable execution trace");
			out.println();
		}

		Environment e = kernel();
		primitiveIO(e, trace);

		// the repl always loads turtle graphics primitives,
		// but they're strictly opt-in for batch mode.
		if (turtles) {
			TurtleGraphics t = new TurtleGraphics(e);
			for(String fileName : args) { runFile(e, fileName, t); }
			if (interactive) { repl(e, t); }
			else {
				if(callSystemExit)
					System.exit(0);
				return;
			}
		}
		else {
			for(String fileName : args) { runFile(e, fileName, null); }
			if (interactive) {
				TurtleGraphics t = new TurtleGraphics(e);
				repl(e, t);
			}
		}
	}

	private void repl(Environment env, TurtleGraphics t) {
		out.println(version);
		out.println("type 'exit' to quit.");
		out.println();
		Scanner in = new Scanner(System.in);

		while(true) {
			out.print(">");
			try {
				String line = in.nextLine();
				if ("exit".equals(line)) { break; }
				while(Parser.complete(line).size() > 0) {
					out.print(">>");
					line += "\n" + in.nextLine();
				}
				runString(env, line, t);
			}
			catch(SyntaxError e) {
				out.format("syntax error: %s%n", e.getMessage());
				out.format("\t%s%n\t", e.line);
				for(int z = 0; z < e.lineIndex; z++) {
					out.print(e.line.charAt(z) == '\t' ? '\t' : ' ');
				}
				out.println("^");
				env.reset();
			}
		}
		if(callSystemExit)
			System.exit(0);
	}

	private void runString(Environment env, String sourceText, TurtleGraphics t) {
		try {
			LList code = Parser.parse(sourceText);
			Interpreter.init(code, env);
			while(true) {
				// execute until the interpreter is paused
				if (!Interpreter.runUntil(env)) { return; }
				
				// update the display until animation is complete
				while(!t.update()) {
					try { Thread.sleep(1000 / 30); }
					catch(InterruptedException e) {}
				}
			}
		}
		catch(RuntimeError e) {
			out.format("runtime error: %s%n", e.getMessage());
			//e.printStackTrace();
			for(LAtom atom : e.trace) {
				out.format("\tin %s%n", atom);
			}
			env.reset();
		}
	}

	void runFile(Environment env, String filename, TurtleGraphics t) {
		try {
			LList code = Parser.parse(loadFile(filename));
			if (t == null) {
				Interpreter.run(code, env);
				return;
			}
			Interpreter.init(code, env);
			while(true) {
				// execute until the interpreter is paused
				if (!Interpreter.runUntil(env)) { return; }
				
				// update the display until animation is complete
				while(!t.update()) {
					try { Thread.sleep(1000 / 30); }
					catch(InterruptedException e) {}
				}
			}
		}
		catch(SyntaxError e) {
			out.format("%d: syntax error: %s%n", e.lineNumber, e.getMessage());
			out.format("\t%s%n\t", e.line);
			for(int z = 0; z < e.lineIndex; z++) {
				out.print(e.line.charAt(z) == '\t' ? '\t' : ' ');
			}
			out.println("^");
			if(callSystemExit)
				System.exit(1);
		}
		catch(RuntimeError e) {
			out.format("runtime error: %s%n", e.getMessage());
			for(LAtom atom : e.trace) {
				out.format("\tin %s%n", atom);
			}
			if(callSystemExit)
				System.exit(1);
		}
	}

	public String loadFile(String filename) {
		try {
			Scanner in = new Scanner(new File(filename));
			StringBuilder ret = new StringBuilder();
			while(in.hasNextLine()) {
				// this will conveniently convert platform-specific
				// newlines into an internal unix-style convention:
				ret.append(in.nextLine()+"\n");
			}
			// shave off the trailing newline we just inserted:
			ret.deleteCharAt(ret.length()-1);
			return ret.toString();
		}
		catch(IOException e) {
			out.format("Unable to load file '%s'.%n", filename);
			if(callSystemExit)
				System.exit(1);
			throw new IOError(e);
		}
	}

	void primitiveIO(Environment e, boolean trace) {
		final LWord a = new LWord(LWord.Type.Name, "argument1");
		final Scanner in = new Scanner(System.in);

		if (trace) {
			e.addTracer(new Tracer() {
				public void begin()  { out.println("tracer: begin."); }
				public void end()    { out.println("tracer: end.");   }
				//public void tick() { out.println("tracer: tick.");  }

				public void callPrimitive(String name, Map<LAtom, LAtom> args) {
					out.format("trace: PRIM %s%s%n",
						name,
						args.size() > 0 ? " " + args : ""
					);
				}
				public void call(String name, Map<LAtom, LAtom> args, boolean tail) {
					out.format("trace: CALL %s%s%s%n",
						name,
						args.size() > 0 ? " " + args : "",
						tail ? " (tail)" : ""
					);
				}
				public void output(String name, LAtom val, boolean implicit) {
					out.format("trace: RETURN %s- %s%s%n", name, val, implicit ? " (implicit)" : "");
				}
				public void stop(String name, boolean implicit) {
					out.format("trace: STOP %s%s%n", name, implicit ? " (implicit)" : "");
				}
				public void define(String name) {
					out.format("trace: DEFINE %s%n", name);
				}
			});
		}

		e.bind(new LWord(LWord.Type.Prim, "version") {
			public void eval(Environment e) {
				out.println(MLogo.version);
			}
		});

		e.bind(new LWord(LWord.Type.Prim, "words") {
			public void eval(Environment e) {
				List<LWord> words = new ArrayList<LWord>(e.words());
				Collections.sort(words);
				for(LWord word : words) { out.print(word + " "); }
				out.println();
				out.println();
			}
		});

		e.bind(new LWord(LWord.Type.Prim, "erase") {
			public void eval(Environment e) {
				LWord key = word(e, a);
				// dereference the name to ensure that
				// it originally had a binding.
				// we don't care what it was.
				e.thing(key);
				e.erase(key);
			}
		}, a);

		e.bind(new LWord(LWord.Type.Prim, "trace") {
			public void eval(Environment e) {
				out.println("trace: ");
				for(LAtom s : e.trace()) {
					out.println("\t" + s);
				}
				out.println();
			}
		});

		e.bind(new LWord(LWord.Type.Prim, "print") {
			public void eval(Environment e) {
				out.println(e.thing(a));
			}
		}, a);

		e.bind(new LWord(LWord.Type.Prim, "println") {
			public void eval(Environment e) {
				out.println();
			}
		});

		e.bind(new LWord(LWord.Type.Prim, "readlist") {
			public void eval(Environment e) {
				e.output(Parser.parse(in.nextLine()));
			}
		});
	}
}