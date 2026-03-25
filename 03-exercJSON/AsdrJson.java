import java.io.*;

public class AsdrJson {

  private static final int BASE_TOKEN_NUM = 301;
  
  public static final int STRING  = 301;
  public static final int NUM 	 = 302;
  public static final int ARRAY   = 303;
  public static final int OBJECT  = 304;
  public static final int MEMBERS = 305;
  public static final int ELEMENTS = 306;
  public static final int VALUE = 307;
  public static final int MEMBERS_RESTO = 308;
  public static final int ELEMENTS_RESTO = 309;
  public static final int JSON = 310;

    public static final String tokenList[] = 
      {"STRING",
		 "NUM",
       "ARRAY",
       "OBJECT"
      };
                                      
  /* referencia ao objeto Scanner gerado pelo JFLEX */
  private Yylex lexer;

  public ParserVal yylval;

  private static int laToken;
  private boolean debug;

  
  /* construtor da classe */
  public AsdrJson (Reader r) {
      lexer = new Yylex(r, this);
  }

  /***** Gramática original 
 
   JSON --> ARRAY
         | OBJECT

   OBJECT: "{" MEMBERS "}"
   
   MEMBERS: STRING ":" VALUE
      |   STRING ":" VALUE "," MEMBERS

   **** Fatorando:
   MEMBERS: STRING ":" VALUE MEMBERS_RESTO
   MEMBERS_RESTO: "," MEMBERS
      | vazio
   ****

   ARRAY: "[" ELEMENTS "]"
   
   ELEMENTS: ELEMENTS "," VALUE
      | VALUE

   **** Removendo a recursão à esquerda:
   ELEMENTS -> VALUE ELEMENTS_RESTO
   ELEMENTS_RESTO -> , VALUE ELEMENTS_RESTO
      | vazio
   ****
   
   VALUE: STRING
      | NUMBER
      | OBJECT
      | ARRAY
***/  

   private void Json() {
      if (laToken == ARRAY){
         ARRAY();
      }
      else if (laToken == OBJECT){
         OBJECT();
      }
      else {
         yyerror("esperado token: OBJECT ou ARRAY");
      }
   }

   private void OBJECT() {
      if (laToken == '{') {
         verifica('{');
         MEMBERS();
         verifica('}');
      }
      else {
         yyerror("esperado token: {");
      }
   }

   private void ARRAY() {
      if (laToken == '[') {
         verifica('[');
         ELEMENTS();
         verifica(']');
      }
      else {
         yyerror("esperado token: [");
      }
   }

   private void MEMBERS() {
      if (laToken == STRING) {
         verifica(STRING);
         verifica(':');
         VALUE();
         MEMBERS_RESTO();
      }
      else {
         yyerror("esperado token: STRING");
      }
   }

   private void MEMBERS_RESTO() {
      if (laToken == ',') {
         verifica(',');
         MEMBERS();
      }
      else {
         yyerror("esperado token: ,");
      }
   }

   private void ELEMENTS() {
      if (laToken == VALUE){
         VALUE();
         ELEMENTS_RESTO();
      }
      else {
         yyerror("esperado token: STRING, NUM, { ou [");
      }
   }

   private void ELEMENTS_RESTO() {
      if (laToken == ',') {
         verifica(',');
         VALUE();
         ELEMENTS_RESTO();
      }
      else {
         yyerror("esperado token: ,");
      }
   }

   private void VALUE() {
      if (laToken == STRING) {
         verifica(STRING);
      }
      else if (laToken == NUM) {
         verifica(NUM);
      }
      else if (laToken == "{") {
         OBJECT();
      }
      else if (laToken == "[") {
         ARRAY();
      }
      else {
         yyerror("esperado token: STRING, NUM, OBJECT ou ARRAY");
      }
   }

  private void verifica(int expected) {
      if (laToken == expected)
         laToken = this.yylex();
      else {
         String expStr, laStr;       

		expStr = ((expected < BASE_TOKEN_NUM )
                ? ""+(char)expected
			     : tokenList[expected-BASE_TOKEN_NUM]);
         
		laStr = ((laToken < BASE_TOKEN_NUM )
                ? (char)laToken+""
                : tokenList[laToken-BASE_TOKEN_NUM]);

          yyerror( "esperado token : " + expStr +
                   " na entrada: " + laStr);
     }
   }

   /* metodo de acesso ao Scanner gerado pelo JFLEX */
   private int yylex() {
       int retVal = -1;
       try {
           yylval = new ParserVal(0); //zera o valor do token
           retVal = lexer.yylex(); //le a entrada do arquivo e retorna um token
       } catch (IOException e) {
           System.err.println("IO Error:" + e);
          }
       return retVal; //retorna o token para o Parser 
   }

  /* metodo de manipulacao de erros de sintaxe */
  public void yyerror (String error) {
     System.err.println("Erro: " + error);
     System.err.println("Entrada rejeitada");
     System.out.println("\n\nFalhou!!!");
     System.exit(1);
     
  }

  public void setDebug(boolean trace) {
      debug = true;
  }


  /**
   * Runs the scanner on input files.
   *
   * This main method is the debugging routine for the scanner.
   * It prints debugging information about each returned token to
   * System.out until the end of file is reached, or an error occured.
   *
   * @param args   the command line, contains the filenames to run
   *               the scanner on.
   */
  public static void main(String[] args) {
     AsdrJson parser = null;
     try {
         if (args.length == 0)
            parser = new AsdrJson(new InputStreamReader(System.in));
         else 
            parser = new  AsdrJson( new java.io.FileReader(args[0]));

          parser.setDebug(false);


          laToken = parser.yylex();          

          parser.Json();
     
          if (laToken== Yylex.YYEOF)
             System.out.println("\n\nSucesso!");
          else     
             System.out.println("\n\nFalhou - esperado EOF.");               

        }
        catch (java.io.FileNotFoundException e) {
          System.out.println("File not found : \""+args[0]+"\"");
        }
//        catch (java.io.IOException e) {
//          System.out.println("IO error scanning file \""+args[0]+"\"");
//          System.out.println(e);
//        }
//        catch (Exception e) {
//          System.out.println("Unexpected exception:");
//          e.printStackTrace();
//      }
    
  }
  
}

