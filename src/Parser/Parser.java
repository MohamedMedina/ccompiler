package parser;

import lexer.*;
import compiler.*;
import java.util.Vector;
import javax.swing.tree.DefaultMutableTreeNode;
import SemanticAnalyzer.SemanticAnalyzer;
import CodeGenerator.CodeGenerator;

/**
 *
 * @author javiergs
 */
public class Parser {

  private static DefaultMutableTreeNode root;
  private static Vector<Token> tokens;
  private static int currentToken;
  private static Gui gui;
  private static boolean newlineErr;
  private static int labelCount;

  private static int progEnd = 0;

  public static DefaultMutableTreeNode run(Vector<Token> t, Gui g) {
    Parser.gui = g;
    SemanticAnalyzer.initializeGUI(gui);
    tokens = t;
    currentToken = 0;
    labelCount = 0;
    SemanticAnalyzer.clearAll();
    CodeGenerator.clear(gui);
    root = new DefaultMutableTreeNode("PROGRAM");
    rule_program(root);
    gui.writeSymbolTable(SemanticAnalyzer.getSymbolTable());
    CodeGenerator.writeCode(gui);
    return root;
  }

  private static void rule_program(DefaultMutableTreeNode parent) {
    DefaultMutableTreeNode node;
    progEnd++;
    if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("{")) {
      node = new DefaultMutableTreeNode("{");
      parent.add(node);
      currentToken++;
    }
    else{ 
        error(1);
        while((currentToken < tokens.size()) && !(isFirst("body") || (tokens.get(currentToken).getWord().equals("}"))))
        {
            if (tokens.get(currentToken).getToken().equals("ERROR"))
            {
                node = new DefaultMutableTreeNode("error" + "(" +tokens.get(currentToken).getWord()+ ")");
                parent.add(node);
            }
            currentToken++;
        }
    }
    
    node = new DefaultMutableTreeNode("BODY");
    parent.add(node);
    rule_body(node);
    
    if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("}")) {
      progEnd--;  
      if(progEnd == 0)
      {
        //CodeGeneration
        CodeGenerator.addInstruction("OPR", "0", "0");
        //CodeGeneration  
      }
      node = new DefaultMutableTreeNode("}");
      parent.add(node);
      currentToken++;
    }
    else{
        error(2);
    }
    return;
  }
  
  private static void rule_body(DefaultMutableTreeNode parent) {
    DefaultMutableTreeNode node;
    newlineErr = false;
    int currentLine = -1; 
    while(currentToken < tokens.size() && !(tokens.get(currentToken).getWord().equals("}"))){
        currentLine = tokens.get(currentToken).getLine();
      
      if (currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("IDENTIFIER")){
        
        node = new DefaultMutableTreeNode("ASSIGNMENT");
        parent.add(node);
        rule_assignment(node);      
      
        if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(";") && isSameLine()) {
        node = new DefaultMutableTreeNode(";");
        parent.add(node);
        currentToken++;
       }
        else{
            error(3);
        }
      }else if((currentToken < tokens.size())&&
             ((tokens.get(currentToken).getWord().equals("int"))||(tokens.get(currentToken).getWord().equals("float"))||
             (tokens.get(currentToken).getWord().equals("boolean"))||(tokens.get(currentToken).getWord().equals("char"))||
             (tokens.get(currentToken).getWord().equals("string"))||(tokens.get(currentToken).getWord().equals("void")))){
              
            node = new DefaultMutableTreeNode("VARIABLE");
            parent.add(node);
            rule_variable(node);
          
            if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(";") && isSameLine()) {
                node = new DefaultMutableTreeNode(";");
                parent.add(node);
                currentToken++;
            }
            else{ 
                error(3);
            }
      }else if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("while")){
            
            node = new DefaultMutableTreeNode("WHILE");
            parent.add(node);
            rule_while(node);  
      }else if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("if")){
            
            node = new DefaultMutableTreeNode("IF");
            parent.add(node);
            rule_if(node);  
      }else if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("switch")){
            
            node = new DefaultMutableTreeNode("SWITCH");
            parent.add(node);
            rule_switch(node);  
      }else if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("return")){
            
            node = new DefaultMutableTreeNode("RETURN");
            parent.add(node);
            rule_return(node);  
            
            if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(";") && isSameLine()) {
                node = new DefaultMutableTreeNode(";");
                parent.add(node);
                currentToken++;
            }
            else{ 
                error(3);
            }
      }else if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("print")){
            
            node = new DefaultMutableTreeNode("PRINT");
            parent.add(node);
            rule_print(node);  
            
            if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(";") && isSameLine()) {
                node = new DefaultMutableTreeNode(";");
                parent.add(node);
                currentToken++;
            }
            else{ 
                error(3);
            }
      }
      else{ 
          newlineErr = true;
          error(4);
          while((currentToken < tokens.size()) && !(isFirst("body") || isFollow("body")) && currentLine == tokens.get(currentToken).getLine())
          {
              if (tokens.get(currentToken).getToken().equals("ERROR"))
              {
                node = new DefaultMutableTreeNode("error" + "(" +tokens.get(currentToken).getWord()+ ")");
                parent.add(node);
              }
                currentToken++;
          }
      }
    }
  }
 
   private static void rule_assignment(DefaultMutableTreeNode parent){
    DefaultMutableTreeNode node;
    String id = "";
    if (currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("IDENTIFIER")) {
        //semantic
        SemanticAnalyzer.pushStack(SemanticAnalyzer.getIdType(tokens.get(currentToken).getWord(), tokens.get(currentToken).getLine()));
        //semantic
        node = new DefaultMutableTreeNode(tokens.get(currentToken).getWord());
        //CodeGeneration
        id = tokens.get(currentToken).getWord();
        //CodeGeneration
        parent.add(node);
        currentToken++;
        
        if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("=") && isSameLine()) {
          node = new DefaultMutableTreeNode(tokens.get(currentToken).getWord());
          parent.add(node);
          currentToken++;
        }else{
            error(5);
            while((currentToken < tokens.size()) && isSameLine() && !(isFirst("expression") || isFollow("expression")))
            {
                if (tokens.get(currentToken).getToken().equals("ERROR"))
                {
                    node = new DefaultMutableTreeNode("error" + "(" +tokens.get(currentToken).getWord()+ ")");
                    parent.add(node);
                }
                currentToken++;
            }
        }
        node = new DefaultMutableTreeNode("EXPRESSION");
        parent.add(node);
        rule_expression(node);  
        
        //semantec
        String x = SemanticAnalyzer.popStack();
        String y = SemanticAnalyzer.popStack();
        String result = SemanticAnalyzer.calculateCube (x, y, "=" );           
        if(!result.equals("OK") && !y.equals("")) {
            SemanticAnalyzer.error(gui, 2, tokens.get(currentToken-1).getLine(), "");
        //semantec
        }
        //CodeGeneration
        CodeGenerator.addInstruction("STO", id, "0");
        //CodeGeneration
    }
  }  
  
  private static void rule_variable(DefaultMutableTreeNode parent){
    DefaultMutableTreeNode node;
    String type = "",id = "";
    if (currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("KEYWORD")) {
        if((currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("int"))|| (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("float"))||
                (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("boolean"))||(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("char"))||
                (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("string"))||(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("void"))){
              node = new DefaultMutableTreeNode("keyword" + "(" + tokens.get(currentToken).getWord() + ")");
              //CodeGeneration
              type = tokens.get(currentToken).getWord();
              //CodeGeneration
              parent.add(node);
              currentToken++;
        }
        if (currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("IDENTIFIER") && isSameLine()) {
          //semantic
          SemanticAnalyzer.checkVariable(tokens.get(currentToken-1).getWord(),tokens.get(currentToken).getWord(), tokens.get(currentToken).getLine());
          //semantic
          node = new DefaultMutableTreeNode("identifier" + "(" + tokens.get(currentToken).getWord() + ")");
          //CodeGeneration
          id = tokens.get(currentToken).getWord();
          //CodeGeneration
          parent.add(node);
          currentToken++;
        }else{
            error(6);
        }
        //CodeGeneration
        CodeGenerator.addVariable(type, id);
        //CodeGeneration
    }
  }
  
  private static void rule_while(DefaultMutableTreeNode parent){
    DefaultMutableTreeNode node;
    String e1 = ""+labelCount;
    labelCount++;
    String e2 = ""+labelCount;
    labelCount++;
    if (currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("KEYWORD")) {
        if((currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("while"))){
              node = new DefaultMutableTreeNode(tokens.get(currentToken).getWord());
              parent.add(node);
              currentToken++;
        }
        if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("(") && isSameLine()) {
          //CodeGeneration
          CodeGenerator.addLabel(e1, CodeGenerator.getInstructionCount()+1);
          //CodeGeneration
          node = new DefaultMutableTreeNode(tokens.get(currentToken).getWord());
          parent.add(node);
          currentToken++;
        }else{
            error(8);
            while((currentToken < tokens.size()) && isSameLine() && !(isFirst("expression") || isFirst("program") || (tokens.get(currentToken).getWord().equals(")"))))
            {
                if (tokens.get(currentToken).getToken().equals("ERROR"))
                {
                    node = new DefaultMutableTreeNode("error" + "(" +tokens.get(currentToken).getWord()+ ")");
                    parent.add(node);
                }
                currentToken++;
            }
        }
        
        node = new DefaultMutableTreeNode("EXPRESSION");
        parent.add(node);
        rule_expression(node);
        
        //semantic
        String x = SemanticAnalyzer.popStack(); 
        if (!x.equals("boolean")) {
            SemanticAnalyzer.error(gui, 3, tokens.get(currentToken-1).getLine(),"");          
        } 
        //semantic 
        //CodeGeneration
        CodeGenerator.addInstruction("JMC", "#"+e2, "false");
        //CodeGeneration
        if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(")") && isSameLine()) {
          node = new DefaultMutableTreeNode(tokens.get(currentToken).getWord());
          parent.add(node);
          currentToken++;
        }else{
            error(7);
            while((currentToken < tokens.size()) && isSameLine() && !(isFirst("program")))
            {
                if (tokens.get(currentToken).getToken().equals("ERROR"))
                {
                    node = new DefaultMutableTreeNode("error" + "(" +tokens.get(currentToken).getWord()+ ")");
                    parent.add(node);
                }
                currentToken++;
            }
        }
        
        node = new DefaultMutableTreeNode("PROGRAM");
        parent.add(node);
        rule_program(node);  
   
        //CodeGeneration
        CodeGenerator.addInstruction("JMP", "#"+e1, "0");
        //CodeGeneration
        //CodeGeneration
        CodeGenerator.addLabel(e2, CodeGenerator.getInstructionCount()+1);
        //CodeGeneration
    }
  }
     
  private static void rule_if(DefaultMutableTreeNode parent){
    DefaultMutableTreeNode node;
    String e1 = ""+labelCount;
    labelCount++;
    String e2 = ""+labelCount;
    labelCount++;
    if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("if")) {
        node = new DefaultMutableTreeNode(tokens.get(currentToken).getWord());
        parent.add(node);
        currentToken++;
        
        if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("(") && isSameLine()) {
          node = new DefaultMutableTreeNode(tokens.get(currentToken).getWord());
          parent.add(node);
          currentToken++;
        }else{
            error(8);
            while((currentToken < tokens.size()) && isSameLine() && !(isFirst("expression") || isFirst("program") || (tokens.get(currentToken).getWord().equals("else")) || (tokens.get(currentToken).getWord().equals(")"))))
            {
                if (tokens.get(currentToken).getToken().equals("ERROR"))
                {
                    node = new DefaultMutableTreeNode("error" + "(" +tokens.get(currentToken).getWord()+ ")");
                    parent.add(node);
                }
                currentToken++;
            }
        }
        node = new DefaultMutableTreeNode("EXPRESSION");
        parent.add(node);
        rule_expression(node);
        
        //CodeGeneration
        CodeGenerator.addInstruction("JMC", "#"+e1, "false");
        //CodeGeneration
        
        //semantic
        String x = SemanticAnalyzer.popStack(); 
        if (!x.equals("boolean")) {
            SemanticAnalyzer.error(gui, 3, tokens.get(currentToken-1).getLine(),""); 
        }
        //semantic
        
        if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(")") && isSameLine()) {
          node = new DefaultMutableTreeNode(tokens.get(currentToken).getWord());
          parent.add(node);
          currentToken++;
        }else{
            error(7);
            while((currentToken < tokens.size()) && isSameLine() && !((isFirst("program")) || (tokens.get(currentToken).getWord().equals("else"))))
            {
                if (tokens.get(currentToken).getToken().equals("ERROR"))
                {
                    node = new DefaultMutableTreeNode("error" + "(" +tokens.get(currentToken).getWord()+ ")");
                    parent.add(node);
                }
                currentToken++;
            }
        }
        
        node = new DefaultMutableTreeNode("PROGRAM");
        parent.add(node);
        rule_program(node);


        if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("else")) {
          //CodeGeneration
          CodeGenerator.addLabel(e1, CodeGenerator.getInstructionCount()+2);
          //CodeGeneration
          //CodeGeneration
          CodeGenerator.addInstruction("JMP", "#"+e2, "0");
          //CodeGeneration
          node = new DefaultMutableTreeNode(tokens.get(currentToken).getWord());
          parent.add(node);
          currentToken++;
          
          node = new DefaultMutableTreeNode("PROGRAM");
          parent.add(node);
          rule_program(node); 
          //CodeGeneration
          CodeGenerator.addLabel(e2, CodeGenerator.getInstructionCount()+1);
          //CodeGeneration
        }else{
          //CodeGeneration
          CodeGenerator.addLabel(e1, CodeGenerator.getInstructionCount()+1);
          //CodeGeneration
        }

    }
  }  
  
  private static void rule_switch(DefaultMutableTreeNode parent){
    DefaultMutableTreeNode node;
    String end = "";
    end = ""+labelCount;
    labelCount++;
    String id = "";
    if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("switch")) {
        node = new DefaultMutableTreeNode(tokens.get(currentToken).getWord());
        parent.add(node);
        currentToken++;
        
        if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("(") && isSameLine()) {
          node = new DefaultMutableTreeNode(tokens.get(currentToken).getWord());
          parent.add(node);
          currentToken++;
        }else{
            error(8);
            while((currentToken < tokens.size()) && !( isFirst("cases") || ((tokens.get(currentToken).getToken().equals("IDENTIFIER")) || (tokens.get(currentToken).getWord().equals(")")) && isSameLine()) || (tokens.get(currentToken).getWord().equals("{"))))
            {
                if (tokens.get(currentToken).getToken().equals("ERROR"))
                {
                    node = new DefaultMutableTreeNode("error" + "(" +tokens.get(currentToken).getWord()+ ")");
                    parent.add(node);
                }
                currentToken++;
            }
        }
    if (currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("IDENTIFIER")) {
        if(SemanticAnalyzer.getIdType(tokens.get(currentToken).getWord(), tokens.get(currentToken).getLine()).equals("int")){
          //CodeGeneration
           id = tokens.get(currentToken).getWord();
          //CodeGeneration          
          node = new DefaultMutableTreeNode("identifier" + "(" + tokens.get(currentToken).getWord() + ")");
          parent.add(node);
          currentToken++;
        }
        else{
            SemanticAnalyzer.error(gui, 4, tokens.get(currentToken-1).getLine(), "");
            currentToken++;
        }
            
    }else{
            error(10);
            while((currentToken < tokens.size()) && !( isFirst("cases") || (tokens.get(currentToken).getWord().equals("{")) || ((tokens.get(currentToken).getWord().equals(")")) && isSameLine())))
            {
                if (tokens.get(currentToken).getToken().equals("ERROR"))
                {
                    node = new DefaultMutableTreeNode("error" + "(" +tokens.get(currentToken).getWord()+ ")");
                    parent.add(node);
                }
                currentToken++;
            }
        }
        
        if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(")") && isSameLine()) {
          node = new DefaultMutableTreeNode(tokens.get(currentToken).getWord());
          parent.add(node);
          currentToken++;
        }else{
            error(7);
            while((currentToken < tokens.size()) && isSameLine() && !((isFirst("cases")) || (tokens.get(currentToken).getWord().equals("{"))))
            {
                if (tokens.get(currentToken).getToken().equals("ERROR"))
                {
                    node = new DefaultMutableTreeNode("error" + "(" +tokens.get(currentToken).getWord()+ ")");
                    parent.add(node);
                }
                currentToken++;
            }
        }
        
        if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("{")) {
          node = new DefaultMutableTreeNode(tokens.get(currentToken).getWord());
          parent.add(node);
          currentToken++;
        }else{
            error(11);
            while((currentToken < tokens.size()) && isSameLine() && !((isFirst("cases")) || (isFirst("default")) || (tokens.get(currentToken).getWord().equals("}")) ))
            {
                if (tokens.get(currentToken).getToken().equals("ERROR"))
                {
                    node = new DefaultMutableTreeNode("error" + "(" +tokens.get(currentToken).getWord()+ ")");
                    parent.add(node);
                }
                currentToken++;
            }
        }
        
        node = new DefaultMutableTreeNode("CASES");
        parent.add(node);
        rule_cases(node, id, end);
        
        if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("default")) {
          node = new DefaultMutableTreeNode("DEFAULT");
          parent.add(node);
          rule_default(node);
          //currentToken++;
        }
        if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("}")) {
          node = new DefaultMutableTreeNode(tokens.get(currentToken).getWord());
          parent.add(node);
          currentToken++;
        }else{
            error(2);
        }
        //CodeGeneration
        CodeGenerator.addLabel(end, CodeGenerator.getInstructionCount()+1);
        //CodeGeneration 
    }
  } 
  
   private static void rule_cases(DefaultMutableTreeNode parent, String id, String end){
    DefaultMutableTreeNode node;

    String val = "";
    do{
    String e1 = ""+labelCount;
    labelCount++;
    if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("case")) {
        //CodeGeneration
        val = tokens.get(currentToken).getWord();
        CodeGenerator.addInstruction("LOD", id, "0");
        //CodeGeneration
        node = new DefaultMutableTreeNode(tokens.get(currentToken).getWord());
        parent.add(node);
        currentToken++;
    }else
    {
        newlineErr = true;
        error(13);
    }
    
    if (currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("INTEGER")) {
      //CodeGeneration
      val = tokens.get(currentToken).getWord();
      CodeGenerator.addInstruction("LIT", val, "0");
      //CodeGeneration
      node = new DefaultMutableTreeNode("integer" + "(" + tokens.get(currentToken).getWord() + ")");
      parent.add(node);
      currentToken++;
    }else if (currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("OCTAL")) {
      //CodeGeneration
      val = tokens.get(currentToken).getWord();
      CodeGenerator.addInstruction("LIT", val, "0");
      //CodeGeneration
      node = new DefaultMutableTreeNode("octal" + "(" + tokens.get(currentToken).getWord() + ")");
      parent.add(node);
      currentToken++;
    }else if (currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("BINARY")) {
      //CodeGeneration
      val = tokens.get(currentToken).getWord();
      CodeGenerator.addInstruction("LIT", val, "0");
      //CodeGeneration
      node = new DefaultMutableTreeNode("binary" + "(" + tokens.get(currentToken).getWord() + ")");
      parent.add(node);
      currentToken++;
    }else if (currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("HEXADECIMAL")) {
      //CodeGeneration
      val = tokens.get(currentToken).getWord();
      CodeGenerator.addInstruction("LIT", val, "0");
      //CodeGeneration
      node = new DefaultMutableTreeNode("hexadecimal" + "(" + tokens.get(currentToken).getWord() + ")");
      parent.add(node);
      currentToken++;
    }else{
        SemanticAnalyzer.error(gui, 4, tokens.get(currentToken-1).getLine(), "");
        while((currentToken < tokens.size()) && !( isFirst("program") || (tokens.get(currentToken).getWord().equals(":") && isSameLine()) || tokens.get(currentToken).getWord().equals("}")))
            {
                if (tokens.get(currentToken).getToken().equals("ERROR"))
                {
                    node = new DefaultMutableTreeNode("error" + "(" +tokens.get(currentToken).getWord()+ ")");
                    parent.add(node);
                }
                currentToken++;
            }   
    }
    
    if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(":") && isSameLine()) {
          node = new DefaultMutableTreeNode(tokens.get(currentToken).getWord());
          parent.add(node);
          currentToken++;
        }else{
            error(12);
            while((currentToken < tokens.size()) && isSameLine() && !( isFirst("program") || tokens.get(currentToken).getWord().equals("}") || tokens.get(currentToken).getWord().equals(":")))
            {
                if (tokens.get(currentToken).getToken().equals("ERROR"))
                {
                    node = new DefaultMutableTreeNode("error" + "(" +tokens.get(currentToken).getWord()+ ")");
                    parent.add(node);
                }
                currentToken++;
            }
        }
        //CodeGeneration
        CodeGenerator.addInstruction("OPR", "15", "0");
        //CodeGeneration
        //CodeGeneration
        CodeGenerator.addInstruction("JMC", "#"+e1, "false");
        //CodeGeneration
        node = new DefaultMutableTreeNode("PROGRAM");
        parent.add(node);
        rule_program(node);
        //CodeGeneration
        CodeGenerator.addInstruction("JMP", "#"+end, "0");
        //CodeGeneration
        //CodeGeneration
        CodeGenerator.addLabel(e1, CodeGenerator.getInstructionCount()+1);
        //CodeGeneration       
    }while(tokens.get(currentToken).getWord().equals("case"));
  } 
   
  private static void rule_default(DefaultMutableTreeNode parent){
    DefaultMutableTreeNode node;
    if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("default")) {
        node = new DefaultMutableTreeNode(tokens.get(currentToken).getWord());
        parent.add(node);
        currentToken++;
    }
    
    if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(":") && isSameLine()) {
          node = new DefaultMutableTreeNode(tokens.get(currentToken).getWord());
          parent.add(node);
          currentToken++;
        }else{
            error(12);
            while((currentToken < tokens.size()) && isSameLine() && !( isFirst("program") || tokens.get(currentToken).getWord().equals("}") ))
            {
                if (tokens.get(currentToken).getToken().equals("ERROR"))
                {
                    node = new DefaultMutableTreeNode("error" + "(" +tokens.get(currentToken).getWord()+ ")");
                    parent.add(node);
                }
                currentToken++;
            }
        }
    
        node = new DefaultMutableTreeNode("PROGRAM");
        parent.add(node);
        rule_program(node);
  }
  
  
  
  private static void rule_return(DefaultMutableTreeNode parent){
    DefaultMutableTreeNode node;
    if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("return")) {
        node = new DefaultMutableTreeNode(tokens.get(currentToken).getWord());
        //CodeGeneration
        CodeGenerator.addInstruction("OPR", "1", "0");
        //CodeGeneration
        parent.add(node);
        currentToken++;
    }
  }  
  
  private static void rule_print(DefaultMutableTreeNode parent){
    DefaultMutableTreeNode node;
    if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("print")) {
        node = new DefaultMutableTreeNode(tokens.get(currentToken).getWord());
        parent.add(node);
        currentToken++;
        
        if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("(") && isSameLine()) {
          node = new DefaultMutableTreeNode(tokens.get(currentToken).getWord());
          parent.add(node);
          currentToken++;
        }else{
            error(8);
            while((currentToken < tokens.size()) && isSameLine() && !(isFirst("expression") || (tokens.get(currentToken).getWord().equals(")"))))
            {
                if (tokens.get(currentToken).getToken().equals("ERROR"))
                {
                    node = new DefaultMutableTreeNode("error" + "(" +tokens.get(currentToken).getWord()+ ")");
                    parent.add(node);
                }
                currentToken++;
            }
        }
        
        node = new DefaultMutableTreeNode("EXPRESSION");
        parent.add(node);
        rule_expression(node);   
        
        if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(")") && isSameLine()) {
          node = new DefaultMutableTreeNode(tokens.get(currentToken).getWord());
          parent.add(node);
          currentToken++;
          //CodeGeneration
          CodeGenerator.addInstruction("OPR", "21", "0");
          //CodeGeneration
        }else{
            error(7);
        }
    }

  }
  
  private static void rule_expression(DefaultMutableTreeNode parent) {
      
    DefaultMutableTreeNode node = new DefaultMutableTreeNode("X");
    parent.add(node);
    rule_X(node);
    
    while (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("|") && isSameLine()) {
        node = new DefaultMutableTreeNode("|");
        parent.add(node);
        currentToken++;
        node = new DefaultMutableTreeNode("X");
        parent.add(node);

        rule_X(node);

        //semantic
        String x = SemanticAnalyzer.popStack();
        String y = SemanticAnalyzer.popStack();
        String result = SemanticAnalyzer.calculateCube(x, y, "|" );
        SemanticAnalyzer.pushStack(result);
        //semantic
        //CodeGeneration
        CodeGenerator.addInstruction("OPR", "8", "0");
        //CodeGeneration
        
    }
  }

  private static void rule_X(DefaultMutableTreeNode parent) {
    DefaultMutableTreeNode node = new DefaultMutableTreeNode("Y");
    parent.add(node);
    rule_Y(node);
    while (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("&") && isSameLine()) {
      if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("&")) {
        node = new DefaultMutableTreeNode("&");
        parent.add(node);
        currentToken++;
        node = new DefaultMutableTreeNode("Y");
        parent.add(node);

        rule_Y(node);
        //semantic
        String x = SemanticAnalyzer.popStack();
        String y = SemanticAnalyzer.popStack();
        String result = SemanticAnalyzer.calculateCube(x, y, "&");
        SemanticAnalyzer.pushStack(result);
        //semantic
        //CodeGeneration
        CodeGenerator.addInstruction("OPR", "9", "0");
        //CodeGeneration
      } 
    }
  }

  private static void rule_Y(DefaultMutableTreeNode parent) {
    //semantic
    boolean operatorWasUsed = false;
    //semantic
    DefaultMutableTreeNode node;
    if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("!") && isSameLine()) {
      //semantic
      operatorWasUsed = true;
      //semantic
      node = new DefaultMutableTreeNode("!");
      parent.add(node);
      currentToken++;
    }
    node = new DefaultMutableTreeNode("R");
    parent.add(node);
    rule_R(node);
    
    
    if (operatorWasUsed){
        //semantic
        String x = SemanticAnalyzer.popStack();
        String result = SemanticAnalyzer.calculateCube(x, "!");
        SemanticAnalyzer.pushStack(result);
        //semantic
        //CodeGeneration
        CodeGenerator.addInstruction("OPR", "10", "0");
        //CodeGeneration
    }
    //semantic
  }
  
  private static void rule_R(DefaultMutableTreeNode parent) {
    //semantic
    String operator = "";
    //semantic
    DefaultMutableTreeNode node = new DefaultMutableTreeNode("E");
    parent.add(node);
    rule_E(node);
    while ((currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(">") || currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("<")||
            currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("==") || currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("!=")) && isSameLine()) {
      if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(">")) {
        //semantic
        operator = ">";
        //semantic
        node = new DefaultMutableTreeNode(">");
        parent.add(node);
        currentToken++;
        node = new DefaultMutableTreeNode("E");
        parent.add(node);
        rule_E(node);
      } else if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("<")) {
        //semantic
        operator = "<";
        //semantic
        node = new DefaultMutableTreeNode("<");
        parent.add(node);
        node = new DefaultMutableTreeNode("E");
        parent.add(node);
        currentToken++;
        rule_E(node);
      }else if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("==")) {
        //semantic
        operator = "==";
        //semantic
        node = new DefaultMutableTreeNode("==");
        parent.add(node);
        node = new DefaultMutableTreeNode("E");
        parent.add(node);
        currentToken++;
        rule_E(node);
      }else if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("!=")) {
        //semantic
        operator = "!=";
        //semantic
        node = new DefaultMutableTreeNode("!=");
        parent.add(node);
        node = new DefaultMutableTreeNode("E");
        parent.add(node);
        currentToken++;
        rule_E(node);
      }
      //semantic
      String x = SemanticAnalyzer.popStack();
      String y = SemanticAnalyzer.popStack();
      String result = SemanticAnalyzer.calculateCube(x, y, operator );
      SemanticAnalyzer.pushStack(result);
      //semantic
      //CodeGeneration
      if(operator.equals(">"))
          CodeGenerator.addInstruction("OPR", "11", "0");
      else if(operator.equals("<"))
          CodeGenerator.addInstruction("OPR", "12", "0");
      else if(operator.equals("=="))
          CodeGenerator.addInstruction("OPR", "15", "0");
      else
          CodeGenerator.addInstruction("OPR", "16", "0");
      //CodeGeneration
    }
  }
  
  private static void rule_E(DefaultMutableTreeNode parent) {
    //semantic
    String operator = "";
    //semantic
    DefaultMutableTreeNode node;
    node = new DefaultMutableTreeNode("A");
    parent.add(node);
    rule_A(node);
    while ((currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("+") || currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("-"))&& isSameLine()) {
      if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("+")) {
        //semantic
        operator = "+";
        //semantic
        node = new DefaultMutableTreeNode("+");
        parent.add(node);
        currentToken++;
        node = new DefaultMutableTreeNode("A");
        parent.add(node);
        rule_A(node);
      } else if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("-")) {
        //semantic
        operator = "-";
        //semantic
        node = new DefaultMutableTreeNode("-");
        parent.add(node);
        currentToken++;
        node = new DefaultMutableTreeNode("A");
        parent.add(node);
        rule_A(node);
      }
      //semantic
      String x = SemanticAnalyzer.popStack();
      String y = SemanticAnalyzer.popStack();
      String result = SemanticAnalyzer.calculateCube(x, y, operator );
      SemanticAnalyzer.pushStack(result);
      //semantic
      //CodeGeneration
      if(operator.equals("+"))
          CodeGenerator.addInstruction("OPR", "2", "0");
      else
          CodeGenerator.addInstruction("OPR", "3", "0");
      //CodeGeneration
    }
  }
      
  private static void rule_A(DefaultMutableTreeNode parent) {
    //semantic
    String operator = "";
    //semantic
    DefaultMutableTreeNode node = new DefaultMutableTreeNode("B");
    parent.add(node);
    rule_B(node);
    while ((currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("*") || currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("/")) && isSameLine()) {
      if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("*")) {
        //semantic
        operator = "*";
        //semantic
        node = new DefaultMutableTreeNode("*");
        parent.add(node);
        currentToken++;
        node = new DefaultMutableTreeNode("B");
        parent.add(node);
        rule_B(node);
            
        
      } else if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("/")) {
        //semantic
        operator = "/";
        //semantic
        node = new DefaultMutableTreeNode("/");
        parent.add(node);
        currentToken++;
        node = new DefaultMutableTreeNode("B");
        parent.add(node);
        rule_B(node);
      }
      //semantic
      String x = SemanticAnalyzer.popStack();
      String y = SemanticAnalyzer.popStack();
      String result = SemanticAnalyzer.calculateCube(x, y, operator );
      SemanticAnalyzer.pushStack(result);
      //semantic
      //CodeGeneration
      if(operator.equals("*"))
          CodeGenerator.addInstruction("OPR", "4", "0");
      else
          CodeGenerator.addInstruction("OPR", "5", "0");
      //CodeGeneration
    }
  }

  private static void rule_B(DefaultMutableTreeNode parent) {
    DefaultMutableTreeNode node;
    //semantic
    boolean operatorWasUsed = false;
    //semantic
    if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("-") && isSameLine()) {
      //semantic
      operatorWasUsed = true;
      //semantic
      //CodeGeneration
      CodeGenerator.addInstruction("LIT", "0", "0");
      //CodeGeneration
      node = new DefaultMutableTreeNode("-");
      parent.add(node);
      currentToken++;
    }
    node = new DefaultMutableTreeNode("C");
    parent.add(node);
    rule_C(node);
    
    if (operatorWasUsed){
        //semantic
        String x = SemanticAnalyzer.popStack();
        String result = SemanticAnalyzer.calculateCube(x, "-");
        SemanticAnalyzer.pushStack(result);
        //semantic
        //CodeGeneration
        CodeGenerator.addInstruction("OPR", "3", "0");
        //CodeGeneration
    }
    
  }
   
  private static void rule_C(DefaultMutableTreeNode parent) {
    DefaultMutableTreeNode node;
    String val = "", id = "";
    if(currentToken < tokens.size() && isSameLine())
    {
    if (currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("INTEGER")) {
      //semantic
      SemanticAnalyzer.pushStack("int"); 
      //semantic
      //CodeGeneration
      val = tokens.get(currentToken).getWord();
      CodeGenerator.addInstruction("LIT", val, "0");
      //CodeGeneration
      node = new DefaultMutableTreeNode("integer" + "(" + tokens.get(currentToken).getWord() + ")");
      parent.add(node);
      currentToken++;
    }else if (currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("OCTAL")) {
      //semantic
      SemanticAnalyzer.pushStack("int"); 
      //semantic
      //CodeGeneration
      val = tokens.get(currentToken).getWord();
      CodeGenerator.addInstruction("LIT", val, "0");
      //CodeGeneration
      node = new DefaultMutableTreeNode("octal" + "(" + tokens.get(currentToken).getWord() + ")");
      parent.add(node);
      currentToken++;
    }else if (currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("BINARY")) {
      //semantic
      SemanticAnalyzer.pushStack("int"); 
      //semantic
      //CodeGeneration
      val = tokens.get(currentToken).getWord();
      CodeGenerator.addInstruction("LIT", val, "0");
      //CodeGeneration
      node = new DefaultMutableTreeNode("binary" + "(" + tokens.get(currentToken).getWord() + ")");
      parent.add(node);
      currentToken++;
    }else if (currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("HEXADECIMAL")) {
      //semantic
      SemanticAnalyzer.pushStack("int"); 
      //semantic
      //CodeGeneration
      val = tokens.get(currentToken).getWord();
      CodeGenerator.addInstruction("LIT", val, "0");
      //CodeGeneration
      node = new DefaultMutableTreeNode("hexadecimal" + "(" + tokens.get(currentToken).getWord() + ")");
      parent.add(node);
      currentToken++;
    }else if (currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("CHARACTER")) {
      //semantic
      SemanticAnalyzer.pushStack("char"); 
      //semantic
      //CodeGeneration
      val = tokens.get(currentToken).getWord();
      CodeGenerator.addInstruction("LIT", val, "0");
      //CodeGeneration
      node = new DefaultMutableTreeNode("character" + "(" + tokens.get(currentToken).getWord() + ")");
      parent.add(node);
      currentToken++;
    }else if (currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("STRING")) {
      //semantic
      SemanticAnalyzer.pushStack("string"); 
      //semantic
      //CodeGeneration
      val = tokens.get(currentToken).getWord();
      CodeGenerator.addInstruction("LIT", val, "0");
      //CodeGeneration
      node = new DefaultMutableTreeNode("string" + "(" + tokens.get(currentToken).getWord() + ")");
      parent.add(node);
      currentToken++;
    }else if (currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("FLOAT")) {
      //semantic
      SemanticAnalyzer.pushStack("float"); 
      //semantic
      //CodeGeneration
      val = tokens.get(currentToken).getWord();
      CodeGenerator.addInstruction("LIT", val, "0");
      //CodeGeneration
      node = new DefaultMutableTreeNode("float" + "(" + tokens.get(currentToken).getWord() + ")");
      parent.add(node);
      currentToken++;
    }else if (currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("IDENTIFIER")) {
        //semantic
        SemanticAnalyzer.pushStack(SemanticAnalyzer.getIdType(tokens.get(currentToken).getWord(), tokens.get(currentToken).getLine()));
        //semantic
        //CodeGeneration
        id = tokens.get(currentToken).getWord();
        CodeGenerator.addInstruction("LOD", id, "0");
        //CodeGeneration
        node = new DefaultMutableTreeNode("identifier" + "(" + tokens.get(currentToken).getWord() + ")");
        parent.add(node);
        currentToken++;
    }else if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("true")) {
        //semantic
        SemanticAnalyzer.pushStack("boolean"); 
        //semantic
        //CodeGeneration
        val = tokens.get(currentToken).getWord();
        CodeGenerator.addInstruction("LIT", val, "0");
        //CodeGeneration
        node = new DefaultMutableTreeNode("boolean" + "(" + tokens.get(currentToken).getWord() + ")");
        parent.add(node);
        currentToken++;
    }else if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("false")) {
        //semantic
        SemanticAnalyzer.pushStack("boolean"); 
        //semantic
        //CodeGeneration
        val = tokens.get(currentToken).getWord();
        CodeGenerator.addInstruction("LIT", val, "0");
        //CodeGeneration
        node = new DefaultMutableTreeNode("boolean" + "(" + tokens.get(currentToken).getWord() + ")");
        parent.add(node);
        currentToken++;
    }else if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("(")) {
        node = new DefaultMutableTreeNode("(");
        parent.add(node);
        currentToken++;
        
        node = new DefaultMutableTreeNode("EXPRESSION");
        parent.add(node);
        rule_expression(node);
        
        if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(")")){
          node = new DefaultMutableTreeNode(")");
          parent.add(node);
          currentToken++;
       }else{ 
          error(7);
        }
    }
    else{
        error(9);
    }
  }
  else
        error(9);
  }
  
    public static void error(int err) {
        int n;
        
        if (currentToken < tokens.size()){
            if (currentToken != 0 && !newlineErr && (tokens.get(currentToken).getLine() != tokens.get(currentToken - 1).getLine()))
                n = tokens.get(currentToken-1).getLine();
            else
                n = tokens.get(currentToken).getLine();
        }
        else 
            n = tokens.get(currentToken-1).getLine();
        
        if(newlineErr)
            newlineErr = false;
        
        switch (err) {
            case 1:
                gui.writeConsole("Line " + n + ":[Syntactic] expected  {");
                break;
            case 2: 
                gui.writeConsole("Line " + n + ":[Syntactic] expected }");
                break;
            case 3:
                gui.writeConsole("Line " + n + ":[Syntactic] expected;");
                break;
            case 4:
                gui.writeConsole("Line " +n+":[Syntactic] expected identifier or keyword");
                break;
            case 5:
                gui.writeConsole("Line " +n+":[Syntactic] expected =");
                break;
            case 6:
                gui.writeConsole("Line " +n+":[Syntactic] expected identifier");
                break;
            case 7:
                gui.writeConsole("Line " +n+":[Syntactic] expected )");
                break;
            case 8:
                gui.writeConsole("Line " +n+":[Syntactic] expected(");
                break;
            case 9:
                gui.writeConsole("Line " +n+":[Syntactic] expected value, identifier,(");
                break;
            case 10:
                gui.writeConsole("Line " +n+":[Syntactic] expected identifier");
                break;            
            case 11:
                gui.writeConsole("Line " +n+":[Syntactic] expected {");
                break;
            case 12:
                gui.writeConsole("Line " +n+":[Syntactic] expected :");
                break;
            case 13:
                gui.writeConsole("Line " +n+":[Syntactic] expected case");
                break;
          }
    }

    public static boolean isSameLine() {
        return (tokens.get(currentToken).getLine() == tokens.get(currentToken - 1).getLine());
    }
    
    public static boolean isFirst(String s ){        
        boolean first = false; 
        
        switch (s)
        {
            case "body":
                if ((tokens.get(currentToken).getToken().equals("IDENTIFIER")) || (tokens.get(currentToken).getWord().equals("int"))||
                   (tokens.get(currentToken).getWord().equals("boolean"))||(tokens.get(currentToken).getWord().equals("char"))||
                   (tokens.get(currentToken).getWord().equals("string"))||(tokens.get(currentToken).getWord().equals("void"))||
                   (tokens.get(currentToken).getWord().equals("float"))|| (tokens.get(currentToken).getWord().equals("while"))||
                   (tokens.get(currentToken).getWord().equals("if")) || (tokens.get(currentToken).getWord().equals("return"))||
                   (tokens.get(currentToken).getWord().equals("print")))
                    first = true;
                break;
            case "expression":
                if ((tokens.get(currentToken).getWord().equals("!")) || (tokens.get(currentToken).getWord().equals("-"))||
                   (tokens.get(currentToken).getToken().equals("INTEGER"))||(tokens.get(currentToken).getToken().equals("OCTAL"))||
                   (tokens.get(currentToken).getToken().equals("BINARY"))||(tokens.get(currentToken).getToken().equals("HEXADECIMAL"))||
                   (tokens.get(currentToken).getToken().equals("CHARACTER"))|| (tokens.get(currentToken).getToken().equals("STRING"))||
                   (tokens.get(currentToken).getToken().equals("FLOAT")) || (tokens.get(currentToken).getToken().equals("IDENTIFIER"))||
                   (tokens.get(currentToken).getWord().equals("(")) || (tokens.get(currentToken).getWord().equals("true")) ||
                   (tokens.get(currentToken).getWord().equals("false")))
                    first = true;
                break;
            case "program":
                if(tokens.get(currentToken).getWord().equals("{"))
                    first = true;
                break;
            case "cases":
                if(tokens.get(currentToken).getWord().equals("case"))
                    first = true;
                break;
            case "default":
                if(tokens.get(currentToken).getWord().equals("default"))
                    first = true;
                break;
        }
        return first;
    }
    
    public static boolean isFollow(String s ){        
        boolean follow = false; 
        
        switch (s)
        {
            case "body":
                if (tokens.get(currentToken).getWord().equals("}"))
                    follow = true;
                break;
            case "expression":
                if ((tokens.get(currentToken).getWord().equals(";")) || (tokens.get(currentToken).getWord().equals(")")))
                    follow = true;
                break;
            case "switch":
                if (isFollow("body"))
                    follow = true;
                break;
        }
        return follow;
    }

}
