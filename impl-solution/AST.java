import java.util.Scanner;

public abstract class AST {
    public void error(String msg) {
        System.err.println(msg);
        System.exit(-1);
    }
};

abstract class Expr extends AST {
    abstract public Double eval(Environment env);

    abstract public void typecheck(Environment env);
    // simple convention:
    // 0.0 for Double
    // 1.0 for Array
}

class Addition extends Expr {
    Expr e1, e2;

    Addition(Expr e1, Expr e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    public Double eval(Environment env) {
        return e1.eval(env) + e2.eval(env);
    }

    public void typecheck(Environment env) {
        e1.typecheck(env);
        e2.typecheck(env);
    }
}

class Multiplication extends Expr {
    Expr e1, e2;

    Multiplication(Expr e1, Expr e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    public Double eval(Environment env) {
        return e1.eval(env) * e2.eval(env);
    }

    public void typecheck(Environment env) {
        e1.typecheck(env);
        e2.typecheck(env);
    }
}

class Subtraction extends Expr {
    Expr e1, e2;

    Subtraction(Expr e1, Expr e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    public Double eval(Environment env) {
        return e1.eval(env) - e2.eval(env);
    }

    public void typecheck(Environment env) {
        e1.typecheck(env);
        e2.typecheck(env);
    }
}

class Division extends Expr {
    Expr e1, e2;

    Division(Expr e1, Expr e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    public Double eval(Environment env) {
        return e1.eval(env) / e2.eval(env);
    }

    public void typecheck(Environment env) {
        e1.typecheck(env);
        e2.typecheck(env);
    }
}

class Constant extends Expr {
    Double d;

    Constant(Double d) {
        this.d = d;
    }

    public Double eval(Environment env) {
        return d;
    }

    public void typecheck(Environment env) {
    }
}

class Variable extends Expr {
    String varname;

    Variable(String varname) {
        this.varname = varname;
    }

    public Double eval(Environment env) {
        return env.getVariable(varname);
    }

    public void typecheck(Environment env) {
        if (env.getVariable(varname) != 0)
            error("Array used as value.");
	/* The environment of the type checker contains
	for each variable either 
	  0 if it was used in normal assignment or 
	  1 if it was used in an Array assignment
	So if we get 1 here, somebody has used an array
	as an expression (and this is a type error).
	*/
    }
}

// ### New ###
class Array extends Expr {
    String varname;
    Expr e;

    Array(String varname, Expr e) {
        this.varname = varname;
        this.e = e;
    }

    public Double eval(Environment env) {
        Double index = e.eval(env);
        return env.getVariable(varname + "[" + index + "]");
    }

    public void typecheck(Environment env) {
        e.typecheck(env);
        if (env.getVariable(varname) != 1)
            error("Using " + varname + " as an array.");
    }
}

abstract class Command extends AST {
    abstract public void eval(Environment env);

    abstract public void typecheck(Environment env);
}

// Do nothing command 
class NOP extends Command {
    public void eval(Environment env) {
    }

    ;

    public void typecheck(Environment env) {
    }

    ;
}

class Sequence extends Command {
    Command c1, c2;

    Sequence(Command c1, Command c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    public void eval(Environment env) {
        c1.eval(env);
        c2.eval(env);
    }

    public void typecheck(Environment env) {
        c1.typecheck(env);
        c2.typecheck(env);
    }

    ;
}


class Assignment extends Command {
    String v;
    Expr e;

    // x = x+1; 
    Assignment(String v, Expr e) {
        this.v = v;
        this.e = e;
    }

    public void eval(Environment env) {
        Double d = e.eval(env);
        env.setVariable(v, d);
    }

    public void typecheck(Environment env) {
        e.typecheck(env);
        env.checkVariable(v, Double.valueOf(0));
        // variant of setVariable: if already defined, it must be the same now, otherwise it will stop with an error.
    }

    ;
}

// ### New ###
class ArrayAssignment extends Command {
    String v;
    Expr i;
    Expr e;

    // x = x+1; 
    ArrayAssignment(String v, Expr i, Expr e) {
        this.v = v;
        this.i = i;
        this.e = e;
    }

    public void eval(Environment env) {
        Double index = i.eval(env);
        Double value = e.eval(env);
        env.setVariable(v + "[" + index + "]", value);
    }

    public void typecheck(Environment env) {
        i.typecheck(env);
        e.typecheck(env);
        env.checkVariable(v, Double.valueOf(1));
    }

    ;
}


class Output extends Command {
    Expr e;

    Output(Expr e) {
        this.e = e;
    }

    public void eval(Environment env) {
        Double d = e.eval(env);
        System.out.println(d);
    }

    public void typecheck(Environment env) {
        e.typecheck(env);
    }

    ;
}

class While extends Command {
    Condition c;
    Command body;

    While(Condition c, Command body) {
        this.c = c;
        this.body = body;
    }

    public void eval(Environment env) {
        while (c.eval(env))
            body.eval(env);
    }

    public void typecheck(Environment env) {
        c.typecheck(env);
        body.typecheck(env);
    }

    ;
}

// ### New ###
class For extends Command {
    String v;
    Expr e1;
    Expr e2;
    Command body;

    For(String v, Expr e1, Expr e2, Command body) {
        this.v = v;
        this.e1 = e1;
        this.e2 = e2;
        this.body = body;
    }

    public void eval(Environment env) {
        Double d1 = e1.eval(env);
        Double d2 = e2.eval(env);

        for (Double i = d1; i <= d2; i++) {
            env.setVariable(v, i);
            body.eval(env);
        }
    }

    public void typecheck(Environment env) {
        e1.typecheck(env);
        e2.typecheck(env);
        env.checkVariable(v, Double.valueOf(0));
        body.typecheck(env);
    }

    ;
}

// ### New ###
class If extends Command {
    Condition c;
    Command body;

    If(Condition c, Command body) {
        this.c = c;
        this.body = body;
    }

    public void eval(Environment env) {
        if (c.eval(env))
            body.eval(env);
    }

    public void typecheck(Environment env) {
        c.typecheck(env);
        body.typecheck(env);
    }

    ;
}


class Breakpoint extends Command {
    Expr b;

    Breakpoint(Expr b) {
        this.b = b;
    };
    Scanner input = new Scanner(System.in);
    String UserInput = null;

    @Override
    public void eval(Environment env) {
        Double d = b.eval(env);
        System.out.println("Breakpoint b\n");
        UserInput = input.nextLine();


    }
    public void typecheck(Environment env) {
        b.typecheck(env);
    };
}


abstract class Condition extends AST {
    abstract public Boolean eval(Environment env);

    abstract public void typecheck(Environment env);
}

class Unequal extends Condition {
    Expr e1, e2;

    Unequal(Expr e1, Expr e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    public Boolean eval(Environment env) {
        return !e1.eval(env).equals(e2.eval(env));
    }

    public void typecheck(Environment env) {
        e1.typecheck(env);
        e2.typecheck(env);
    }

    ;

}

// ### New ###
class Equal extends Condition {
    Expr e1, e2;

    Equal(Expr e1, Expr e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    public Boolean eval(Environment env) {
        return e1.eval(env).equals(e2.eval(env));
    }

    public void typecheck(Environment env) {
        e1.typecheck(env);
        e2.typecheck(env);
    }

    ;
}

// ### New ###
class Smaller extends Condition {
    Expr e1, e2;

    Smaller(Expr e1, Expr e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    public Boolean eval(Environment env) {
        return e1.eval(env) < (e2.eval(env));
    }

    public void typecheck(Environment env) {
        e1.typecheck(env);
        e2.typecheck(env);
    }

    ;

}

// ### New ###
class Conjunction extends Condition {
    Condition e1, e2;

    Conjunction(Condition e1, Condition e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    public Boolean eval(Environment env) {
        return e1.eval(env) && (e2.eval(env));
    }

    public void typecheck(Environment env) {
        e1.typecheck(env);
        e2.typecheck(env);
    }

    ;

}

// ### New ###
class Disjunction extends Condition {
    Condition e1, e2;

    Disjunction(Condition e1, Condition e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    public Boolean eval(Environment env) {
        return e1.eval(env) || (e2.eval(env));
    }

    public void typecheck(Environment env) {
        e1.typecheck(env);
        e2.typecheck(env);
    }

    ;
}

// ### New ###
class Negation extends Condition {
    Condition e1;

    Negation(Condition e1) {
        this.e1 = e1;
    }

    public Boolean eval(Environment env) {
        return !e1.eval(env);
    }

    public void typecheck(Environment env) {
        e1.typecheck(env);
    }

    ;
}
