package gov.frb.ma.msu.toR;
import java.io.*;



public class Model
{
  String Name;  // name of model
  int NEq;         // number of equations
  int NLag;        // maximum lag in model
  int NLead;       // maximum lead in model

  Equation[] Equations = new Equation[Aim.Max_Array_Size];   // array of Equations
  String[] Coefficients = new String[Aim.Max_Array_Size];    // coefficient names
  Variable[] Variables = new Variable[Aim.Max_Array_Size];  // Variable objects

  int NVars;       // number of variables
  int NCoeffs;     // number of coefficients

  public Model(String s) {
      Name = s;
      NEq = 0;
      NLag = 0;
      NLead = 0;
      NVars = 0;
      NCoeffs = 0;
  }

    public int getNEq() { return NEq; }

    public int ErrorCheck() { 
    int i;
    int errorFound = 0;

    System.err.println("Checking for errors......");

    if (NEq != NVars) {
	System.err.println("Error: Number of variables not equal to number " +
			   "of equations.");
	errorFound++;
    }

    for (i = 0; i < NEq; i++)
    {
	if ((Equations[i].LHS.PowerErrorCheck() > 0) ||
	    (Equations[i].RHS.PowerErrorCheck() > 0)) {
	    System.err.print("Error in equation #" + (i+1) + ": ");
	    System.err.print("Variables cannot be raised to a power nor ");
	    System.err.print("appear\n                    in an exponent");
	    System.err.println(" or denominator.");
	    errorFound++;
	}
	
	if ((Equations[i].LHS.ProductErrorCheck() > 1) ||
	    (Equations[i].RHS.ProductErrorCheck() > 1)) {
	    System.err.print("Error in equation #" + (i+1) + ": ");
	    System.err.println("Equation has additive constant or is nonlinear");
	    System.err.println("                    in its variables.");
	    errorFound++;
	}
    }
    return errorFound;
  }
    
    public void AddEquation(Equation e) {
	Equations[NEq] = e;
	NEq++;
    }
    
    public void AddCoefficient(String s) {
	Coefficients[NCoeffs] = s;
	NCoeffs++;
    }

  public void AddVariable(Variable v) {
      Variables[NVars] = v;
      NVars++;
  }

  public int FindCoefficientIndex(String s) {
    // returns the index of the String in Coefficients
    // that matches the String s, or -1 if there is no match.
    int i = 0;
    while ((i < NCoeffs) && !(Coefficients[i].equals(s)))
      i++;
    if (i < NCoeffs)
      return i;
    else
      return -1;
  }

  public int FindEquationIndex(String s) {
    // returns the index of the equation in Equations
    // that matches the String s, or -1 if there is no match.
    int i = 0;
    while ((i < NEq) && !(Equations[i].Name.equals(s)))
      i++;
    if (i < NEq)
      return i;
    else
      return -1;
  }

  public int FindVariableIndex(String s) {
    // returns the index of the variable in Variables whose
    // name matches the String s, or -1 if there is no match.
    int i = 0;
    while ((i < NVars) && !((Variables[i].Name).equals(s)))
      i++;
    if (i < NVars)
      return i;
    else
      return -1;
  }

  public void Print() { // for debugging
    int i;
    for (i = 0; i < NEq; i++) {
      System.out.println("Equation #" + (i+1) + ":");
      Equations[i].Print();
    }
  }

  public void ExpandSubtrees() {
    int i;
    for (i = 0; i < NEq; i++)
      Equations[i].ExpandSubtrees();
  }

  public void setName(String s) { Name = s; }
  public void setNEq(int n) { NEq = n; }
  public void setNLag(int n) { NLag = n; }
  public void setNLead(int n) { NLead = n; }
  public void setNCoeffs(int n) { NCoeffs = n; }

  public void PrintFunctions() {
    int i;
    PrintStream dataPS;
    PrintStream matrixPS;
    PrintStream paramPS;
    String lcName = Name;
    lcName.toLowerCase();
    String dataFileName = lcName + "_SparseAimDataR.r";
    String matrixFileName = lcName + "_SparseAimMatricesR.r";
    String paramFileName = lcName + "_SparseAimParamsR.r";
    
    try {
	dataPS = new PrintStream(new FileOutputStream(dataFileName));

	dataPS.println(lcName + "_SparseAimDataR <- function(){");
	dataPS.println();
	dataPS.println("# " + lcName + "_aim_data()");
	dataPS.println("#     This function will return various information about the AIM model,");
	dataPS.println("#     but will not compute the G and H matrices.");
	dataPS.println();
	dataPS.println("  eqname = mat.or.vec(" + NEq + ", 1);");
	dataPS.println("  param = mat.or.vec(" + NCoeffs + ", 1);");
	dataPS.println("  endog = mat.or.vec(" + NEq + ", 1);");
	dataPS.println("  delay = mat.or.vec(" + NEq + ", 1);");
	dataPS.println("  vtype = mat.or.vec(" + NEq + ", 1);");
	dataPS.println("  eqtype = mat.or.vec(" + NEq + ", 1);");
	dataPS.println();
	dataPS.println("  modname = '" + Name + "';");
	dataPS.println("  neq = " + NEq + ";");
	dataPS.println("  np = " + NCoeffs + ";");
	dataPS.println("  nlag = " + NLag + ";");
	dataPS.println("  nlead = " + NLead + ";");
	dataPS.println();

	for (i = 0; i < NEq; i++)
	    dataPS.println("  eqname[" + (i+1) + "] = '" +
			   Equations[i].Name + "';");
//	dataPS.println("  eqname_ = char(eqname);");
	dataPS.println();

	for (i = 0; i < NEq; i++) {
	    dataPS.print("  eqtype[" + (i+1) + "] = " + Equations[i].EqType +
			   ";   ");
	    if (i % 3 == 2)
	      dataPS.println();
	}
	if (i % 3 != 1)
	  dataPS.println();
	dataPS.println("  eqtype_ = eqtype;");
	dataPS.println();

	for (i = 0; i < NCoeffs; i++)
	dataPS.println("  param[" + (i+1) + "] = '" + Coefficients[i]
		       + "';");
//	dataPS.println("  param_ = char(param);");
	dataPS.println();

	for (i = 0; i < NVars; i++)
	  dataPS.println("  endog[" + (i+1) + "] = '" +
			 Variables[i].Name + "';");
//	dataPS.println("  endog_ = char(endog);");
	dataPS.println();

	for (i = 0; i < NVars; i++) {
	  dataPS.print("  delay[" + (i+1) + "] = " + Variables[i].returnDelay() +
		       ";   ");
	  if (i % 3 == 2)
	    dataPS.println();
	}
	if (i % 3 != 1)
	  dataPS.println();
	dataPS.println("  delay_ = delay;");
	dataPS.print("\n");
	
	for (i = 0; i < NEq; i++) {
	  dataPS.print("  vtype[" + (i+1) + "] = " + Variables[i].DataType +
		       ";   ");
	  if (i % 3 == 2)
	    dataPS.println();
	}
	if (i % 3 != 1)
	  dataPS.println();
	dataPS.println("  vtype_ = vtype;");
	dataPS.print("\n");
	dataPS.println("output <- list(eqname, param, endog, delay, vtype, eqtype)");
	dataPS.println("output");
	dataPS.print("}");
	dataPS.print("\n\n\n");

	dataPS.close();
    } catch (Exception e) {
	System.err.println("ERROR: " + e.getMessage());
    }

/*****************************************************************
  Now print out the function compute_aim_matrices(). This function
  will compute the G and H matrices.  Actually this is a script not
  a function.  It is easier to deal with the parameters in R if
  this part is a script since you don't have to declare them globals
  or reassign them inside the function.
******************************************************************/

    try {
      matrixPS = new PrintStream(new FileOutputStream(matrixFileName));
            
      matrixPS.println("# " + lcName + "AimMatricesR()");
      matrixPS.println("#     This script will compute the G and H matrices.");
      matrixPS.println();

  /*    matrixPS.println("  g = mat.or.vec(" + NEq + ", " + ((NLag+1)*NEq)
		       + ");"); */
      matrixPS.println("  h = mat.or.vec(" + NEq + ", " + ((NLag+1+NLead)*NEq)
		       + ");");
      matrixPS.println();

      for (i = 0; i < NEq; i++) {
	Equations[i].LHS.PrintGMatrixEntries(this, i, Aim.Left_Side,
					     matrixPS);
	Equations[i].RHS.PrintGMatrixEntries(this, i, Aim.Right_Side,
					     matrixPS);
	Equations[i].LHS.PrintHMatrixEntries(this, i, Aim.Left_Side,
					     matrixPS);
	Equations[i].RHS.PrintHMatrixEntries(this, i, Aim.Right_Side,
					     matrixPS);
      }

      matrixPS.println();
//      matrixPS.println("  cofg = g;");
      matrixPS.println("  cofh = h;");

      matrixPS.close();
    } catch (Exception e) {
      System.err.println("ERROR: " + e.getMessage());
    }
    
 /*****************************************************************
    Now create a script with methods to initialize parameters.  There 
    will be a setParameters(double[]params) function to generate all 
    the parameters, a set<paramName>(<new value>) method for each function, 
    as well as an R function. 
  ******************************************************************/    
    try{
    	paramPS = new PrintStream(new FileOutputStream(paramFileName));
    
    	//functions to set each individual parameter
    	
    	for (i = 0; i < NCoeffs; i++){
    		paramPS.println("set" + Coefficients[i]
    		               + "Globally <- function(newValue){");
    		paramPS.println(Coefficients[i] + "<<- newValue;");
    		paramPS.println("} \n");
			paramPS.println("set" + Coefficients[i] 
			           + "Locally <- function(paramVector, newValue){");
			paramPS.println("#Requires paramVector to have names");
			paramPS.println("paramVector['" + Coefficients[i] 
			           + "'] <<- newValue \n }");
			paramPS.println();
    	}
    	
    	    	   	
    	//function to set all parameters
    	paramPS.println("setAllParamsGlobally <- function(newParamVector){");
    	for(i = 0; i < NCoeffs; i++){
    		paramPS.println("\t " + Coefficients[i] + "<<- paramVector" + (i+1));
    	}
    	paramPS.println("} \n");
    	
    	paramPS.println("updateParamVector <- function(oldParamVector, newParamVector){");
    	paramPS.println("\t oldParamVector <<- newParamVector \n }");
    	paramPS.close();
    } catch(Exception e){
    	System.err.println("ERROR: " + e.getMessage());
    }
    
  }
  
}

