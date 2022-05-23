import java.util.HashMap;
import java.util.Map.Entry;

class Environment {
    private HashMap<String,Double> variableValues = new HashMap<String,Double>();
    public Environment() { }
    public void setVariable(String name, Double value) {
	variableValues.put(name, value);
    }
    
    public Double getVariable(String name){
	Double value = variableValues.get(name); 
	if (value == null) { System.err.println("Variable not defined: "+name); System.exit(-1); }
	return value;
    }
    public void checkVariable(String name, Double value){
	Double v = variableValues.get(name); 
	if (v == null)
	    variableValues.put(name, value);
	else if (!v.equals(value)){
	    System.err.println("Variable already defined with different type: "+name);
	    System.exit(-1);
	}
    }
    
    public String toString() {
	String table = "";
	for (Entry<String,Double> entry : variableValues.entrySet()) {
	    table += entry.getKey() + "\t-> " + entry.getValue() + "\n";
	}
	return table;
    }   
}

