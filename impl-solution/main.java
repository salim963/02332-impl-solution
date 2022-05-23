import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.CharStreams;
import java.io.IOException;
import org.stringtemplate.v4.Interpreter;

public class main {
    public static void main(String[] args) throws IOException{

	// we expect exactly one argument: the name of the input file
	if (args.length !=0) {
	    System.err.println("\n");
	    System.err.println("Simple interpreter\n");
	    System.err.println("==================\n\n");
	    System.err.println("Please give as input argument a filenames\n");
	    System.exit(-1);
	}
	String filename="impl_input.txt";

	// open the input file
	CharStream input = CharStreams.fromFileName(filename);
	    //new ANTLRFileStream (filename); // depricated
	
	// create a lexer/scanner
	implLexer lex = new implLexer(input);
	
	// get the stream of tokens from the scanner
	CommonTokenStream tokens = new CommonTokenStream(lex);
	
	// create a parser
	implParser parser = new implParser(tokens);
	
	// and parse anything from the grammar for "start"
	ParseTree parseTree = parser.start();

	// Construct an interpreter and run it on the parse tree
	//Interpreter interpreter = new Interpreter();
	Command p = (Command) new AstMaker().visit(parseTree);
	System.out.println("Type checking ... ");
	p.typecheck(new Environment());
	System.out.println("Type checking successful.");
	System.out.println("Starting execution ... ");
	p.eval(new Environment());
    }
}

// We write an interpreter that implements interface
// "implVisitor<T>" that is automatically generated by ANTLR
// This is parameterized over a return type "<T>" which is in our case
// simply a Double.


class AstMaker extends AbstractParseTreeVisitor<AST> implements implVisitor<AST> {

    public AST visitStart(implParser.StartContext ctx){
	Command program=new NOP();
	for(implParser.CommandContext c:ctx.cs)
	    program=new Sequence(program,(Command)visit(c));
	return program;
    };

    public AST visitSingleCommand(implParser.SingleCommandContext ctx){
	return visit(ctx.c);
    }

    public AST visitMultipleCommands(implParser.MultipleCommandsContext ctx){
	Command program=new NOP();
	for(implParser.CommandContext c:ctx.cs)
	    program=new Sequence(program,(Command)visit(c));
	return program;
    }
    
    public AST visitAssignment(implParser.AssignmentContext ctx){
	String v=ctx.x.getText();
 	Expr e=(Expr)visit(ctx.e);
	return new Assignment(v,e);
    }

    // ### new ###
    public AST visitArrayAssignment(implParser.ArrayAssignmentContext ctx){
	String v=ctx.a.getText();
 	Expr i=(Expr)visit(ctx.i);
 	Expr e=(Expr)visit(ctx.e);
	return new ArrayAssignment(v,i,e);
    }

    public AST visitOutput(implParser.OutputContext ctx){
	Expr e=(Expr)visit(ctx.e);
	return new Output(e);
    }

    // ### new ###
    public AST visitForLoop(implParser.ForLoopContext ctx){
	String v=ctx.x.getText();
	Expr e1=(Expr)visit(ctx.e1);
	Expr e2=(Expr)visit(ctx.e2);
	Command body=(Command)visit(ctx.p);
	return new For(v,e1,e2,body);
	}
	/* Alternative implementation as syntactic sugar:
    public AST visitForLoop(implParser.ForLoopContext ctx){
	String v=ctx.x.getText();
	Expr e1=(Expr)visit(ctx.e1);
	Expr e2=(Expr)visit(ctx.e2);
	Command body=(Command)visit(ctx.p);
	return
        new Sequence(
          new Assignment(v,e1),
	  new While(
	    new Unequal(new Variable(v),e2),
            new Sequence(body,
	      new Assignment(v,
		new Addition(
		  new Variable(v),
		  new Constant(new Double(1)))))));
    }
    */
    // ### new ###
    public AST visitIf(implParser.IfContext ctx){
	Condition c=(Condition)visit(ctx.c);
	Command body=(Command)visit(ctx.p);
	return new If(c,body);
    }

////////////////////////////////////////////////////////////
	@Override
	public AST visitBreakpoint(implParser.BreakpointContext ctx) {
		return null;
	}
///////////////////////////////////////////////////////////////////

	public AST visitWhileLoop(implParser.WhileLoopContext ctx){
	Condition c=(Condition)visit(ctx.c);
	Command body=(Command)visit(ctx.p);
	return new While(c,body);
    }
    
    public AST visitParenthesis(implParser.ParenthesisContext ctx){
	return visit(ctx.e);
    };
    
    public AST visitVariable(implParser.VariableContext ctx){
	return new Variable(ctx.x.getText());
    };

    // ### new ###
    public AST visitArray(implParser.ArrayContext ctx){
	Expr e=(Expr)visit(ctx.e);
	return new Array(ctx.a.getText(),e);
    };
    

    // ### mod ###
    public AST visitAddition(implParser.AdditionContext ctx){
	if (ctx.o.getText().equals("+"))
	    return new Addition((Expr) visit(ctx.e1), (Expr)visit(ctx.e2));
	else return new Subtraction((Expr)visit(ctx.e1),(Expr)visit(ctx.e2));
    };

    // ### mod ###
    public AST visitMultiplication(implParser.MultiplicationContext ctx){
	if (ctx.o.getText().equals("*"))
	return new Multiplication((Expr) visit(ctx.e1), (Expr)visit(ctx.e2));
	else
	    	return new Division((Expr) visit(ctx.e1), (Expr)visit(ctx.e2));

    };

    

    public AST visitConstant(implParser.ConstantContext ctx){
	return new Constant(Double.parseDouble(ctx.c.getText())); 
    };

    // ### New ###
    public AST visitNegativeConstant(implParser.NegativeConstantContext ctx){
	return new Constant(0-Double.parseDouble(ctx.c.getText())); 
    };


    public AST visitUnequal(implParser.UnequalContext ctx){
	Expr v1=(Expr)visit(ctx.e1);
	Expr v2=(Expr)visit(ctx.e2);
	return new Unequal(v1,v2);
    }

    // ### New ###
    public AST visitEqual(implParser.EqualContext ctx){
	Expr v1=(Expr)visit(ctx.e1);
	Expr v2=(Expr)visit(ctx.e2);
	return new Equal(v1,v2);
    }

    // ### New ###
    public AST visitSmaller(implParser.SmallerContext ctx){
	Expr v1=(Expr)visit(ctx.e1);
	Expr v2=(Expr)visit(ctx.e2);
	return new Smaller(v1,v2);
    }

        // ### New ###
    public AST visitDisjunction(implParser.DisjunctionContext ctx){
	Condition v1=(Condition)visit(ctx.c1);
	Condition v2=(Condition)visit(ctx.c2);
	return new Disjunction(v1,v2);
    }
        // ### New ###
    public AST visitConjunction(implParser.ConjunctionContext ctx){
	Condition v1=(Condition)visit(ctx.c1);
	Condition v2=(Condition)visit(ctx.c2);
	return new Conjunction(v1,v2);
    }
        // ### New ###
    public AST visitNegation(implParser.NegationContext ctx){
	Condition c=(Condition)visit(ctx.c);
	return new Negation(c);
    }
    // ### New ###
    public AST visitParenthesisCondition(implParser.ParenthesisConditionContext ctx){
	Condition c=(Condition)visit(ctx.c);
	return c;
    }

}
