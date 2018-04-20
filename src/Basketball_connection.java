import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;


public class Basketball_connection {

  private Connection connect = null;
  Scanner input;
  String action;
  String table;
  ArrayList<Tuple> filterList;
  ArrayList<Tuple> updateList;
  ArrayList<String> fields_in_new_table;
  ArrayList<String> datatypes;
  String table_name;

  public Basketball_connection() {
    filterList = new ArrayList<>();
    updateList = new ArrayList<>();
    fields_in_new_table = new ArrayList<>();
    datatypes = new ArrayList<>();
    input = new Scanner(System.in).useDelimiter("\\n");
  }

  public void connectDatabase(String username, String password) {
    try {
      Class.forName("com.mysql.jdbc.Driver");
      connect = DriverManager
              .getConnection("jdbc:mysql://localhost/finalProject?"
                      + "user=" + username + "&password=" + password);

    } catch (Exception e) {
      e.printStackTrace();
    }

  }


  // runs the program
  private void run() {
    int leave = 1;
    while (leave == 1) {
      try {
        getAction();
        System.out.println("If you would like to make more queries please type 1 " +
                "otherwise type 0");
        leave = input.nextInt();
      } catch (SQLException e) {
        System.out.println(e.getMessage());
        System.out.println("The database query you requested had an error." +
                "If you would like to try again press 1 otherwise press 0 ");
        leave = input.nextInt();

      } catch (IllegalArgumentException e) {
        System.out.print("You did not choose a valid option, please choose again");
      }

    }
    System.out.println("Thank you for using our program. Have a good day!");


  }

  private void getTable() throws SQLException {
    ArrayList<String> tableList = new ArrayList<>();
    try {
      System.out.println("Here are the available tables");
      PreparedStatement preparedStatement = null;
      preparedStatement = connect.prepareStatement("show tables");
      ResultSet resultSet = preparedStatement.executeQuery();
      //add table to Array List
      while (resultSet.next()) {
        tableList.add(resultSet.getString(1));
      }
    } catch (Exception e) {
      System.out.println("\n******************************************\noops - something broke!!\n******************************************\n");
    }

    //Print out list of tables
    System.out.println(tableList);

    //Get table name from user
    System.out.println("\n Please spell the name of the table you would like to select");
    table = input.next();
    while (!tableList.contains(table)) {
      System.out.println("You spelled a table incorrectly. Please try again");
      table = input.next();
    }


  }

  private void getFilter(ArrayList arrayList, String words) {
    try {
      //reset old fields
      arrayList.clear();

      PreparedStatement preparedStatement = connect.prepareStatement("DESCRIBE " + table + ";");
      ResultSet resultSet = preparedStatement.executeQuery();
      ArrayList<String> tableFields = new ArrayList<>();
      // put fields of a table into an array list
      while (resultSet.next()) {
        tableFields.add(resultSet.getString(1));
      }
      System.out.println("____________________________________________________");
      //print fields for user
      System.out.println("Here are the fields in the table");
      System.out.println(tableFields);
      System.out.println("____________________________________________________");
      //Get use input
      System.out.println("Please print out the field you would like to " + words +
              " and then press enter and enter the value you would like to see. Once you are done" +
              " please press 1 to go the next step");
      System.out.println("____________________________________________________");

      //while loop for user input
      int leave = 0;
      String field, value;
      while (leave == 0) {
        field = input.next();
        while (!tableFields.contains(field)) {
          System.out.println("You spelled a field incorrectly. Please try again");
          field = input.next();
        }
        value = "\"" + input.next() + "\"";
        arrayList.add(new Tuple(field, value));
        System.out.println("value: " + value);
        System.out.println("field: " + field);

        System.out.println("Would you like to add more values? Press 0 to add more fields/values and " +
                "press 1 to go to the next step");
        leave = input.nextInt();
      }
    } catch (Exception e) {
      System.out.println("\n******************************************\noops - something broke!!\n******************************************\n");
    }
  }


  private void getAction() throws SQLException {
    System.out.println("______________________________________________________________________________");
    System.out.println("Please type the number of the action you would like to perform");
    System.out.println("______________________________________________________________________________");
    System.out.println("1. View \n2. Insert\n3. Delete \n4. Create \n5. Update");
    System.out.println("______________________________________________________________________________");
    switch (input.nextInt()) {
      case 1:
        action = "SELECT";
        getTable();
        getFilter(filterList, "filter");
        select();
        break;

      case 2:
        action = "INSERT INTO";
        getTable();
        getFilter(filterList, "add");
        add();
        break;

      case 3:
        action = "DELETE";
        getTable();
        getFilter(filterList, "delete");
        delete();
        break;

      case 4:
        action = "CREATE";
        collectNewTableData();
        createTable();
        break;

      case 5: action = "Update";
        getTable();
        getFilter(filterList,"filter");
        getFilter(updateList, "update");
        update();
        break;
      default:
        throw new IllegalArgumentException("Did not choose a correct number");
    }
  }

  /**
   * Collects table data
   */
  private void collectNewTableData() {
    System.out.println("\nWhat's the name of your new object: ");
    table_name = input.next();
    int i = 0;
    int leave = 1;
    while (leave == 1) {
      System.out.println("\nPlease type the column names of " + table_name + ": ");
      fields_in_new_table.add(input.next());
      System.out.println("\nTo add more columns, type '1', otherwise type '0' to continue: \n");
      leave = input.nextInt();
    }
    System.out.println("\nHere are the columns in your new relations...");
    System.out.println(fields_in_new_table);

    for (String s : fields_in_new_table) {
      System.out.println("\nPlease type the data-type for the field " + s + ": ");
      datatypes.add(input.next());
    }
  }

  /**
   * Adds the final paren and finishes table.
   */
  private void createTable() {
    String sql;
    sql = "CREATE TABLE " + table_name + "(";
    this.finishTable(sql);
  }

  /**
   * Creates tables dynamically. Only adds commas at end of String.
   *
   * @param statement rest of create statement
   */
  private void finishTable(String statement) {
    int i = 0;
    while (i < fields_in_new_table.size()) {
      statement += fields_in_new_table.get(i) + " " + datatypes.get(i) + this.addComma(i);
      i++;
    }
    statement += ");";

    System.out.println(statement);
    try {
      PreparedStatement preparedStatement = connect.prepareStatement(statement);
      preparedStatement.executeUpdate();
      System.out.println("\n____________________________\n");
      System.out.println("\nTable created!\n");
    } catch (SQLException e) {
      System.out.println(e.getMessage());
      System.out.println("\n******************************************\noops - something broke! Check your syntax next time...\n******************************************\n");
    }
  }

  /**
   * @param i end of columns in new table
   */
  private String addComma(int i) {

    if (i < fields_in_new_table.size() - 1) {
      return ", ";
    } else {
      return "";
    }
  }

  private void select() throws SQLException {
    //making the sql statement
    String sql;
    sql = action + " * from " + table;
    int i = filterList.size();
    for (Tuple t : filterList) {
      if (i == filterList.size()) {
        sql += " where ";
      }
      sql += t.getX() + " = " + t.getY();
      if (i > 1) {
        sql += " and ";
      }
      i--;

    }
    sql += ";";
    System.out.println(sql);


    //Print result of sql statment
    try {
      PreparedStatement preparedStatement = connect.prepareStatement(sql);
      ResultSet resultSet = preparedStatement.executeQuery();
      ResultSetMetaData rsmd = resultSet.getMetaData();
      int columnsNumber = rsmd.getColumnCount();
      while (resultSet.next()) {
        System.out.println("\n____________________________________________________\n");
        for (i = 1; i <= columnsNumber; i++) {
          if (i > 1) System.out.print(",  ");
          String columnValue = resultSet.getString(i);
          System.out.print(columnValue + " " + rsmd.getColumnName(i));
          System.out.println("\n____________________________________________________\n");
        }
      }
    } catch (Exception e) {
      System.out.println("\n******************************************\noops - something broke!!\n" +
              "******************************************\n");
    }
    System.out.println("");
  }

  private void update() throws SQLException {
    //making the sql statement
    String sql;
    sql = action + " " + table;

    //set value
    int i = updateList.size();
    for (Tuple t : updateList) {
      if (i == updateList.size()) {
        sql += " set ";
      }
      sql += t.getX() + " = " + t.getY();
      if (i > 1) {
        sql += ", ";
      }
      i--;

    }

    //where value
    for (Tuple t : filterList) {
      sql += " where ";
      sql += t.getX() + " = " + t.getY();
      if (i > 1) {
        sql += "and ";
      }
      i--;

    }
    sql += ";";
    System.out.println(sql);

    try {
      PreparedStatement preparedStatement = connect.prepareStatement(sql);
      preparedStatement.executeUpdate();
    } catch (Exception e) {
      System.out.println("\n******************************************\noops - something broke!!\n******************************************\n");
    }
  }

  private void delete() throws SQLException {
    //making the sql statement
    String sql;
    sql = action + " from " + table;
    int i = filterList.size();
    for (Tuple t : filterList) {
      if (i == filterList.size()) {
        sql += " where ";
      }
      sql += t.getX() + " = " + t.getY();
      if (i > 1) {
        sql += " and ";
      }
      i--;

    }
    sql += ";";
    System.out.println(sql);

    try {
      PreparedStatement preparedStatement = connect.prepareStatement(sql);
      preparedStatement.executeUpdate();
    } catch (Exception e) {
      System.out.println("\n******************************************\noops - something broke!! Check your syntax next time...\n******************************************\n");
    }
  }

  private void add() throws SQLException {
    String sql;
    sql = action + " " + table;
    int i = filterList.size();
    sql += "( ";
    for (Tuple t : filterList) {

      sql += t.getX();
      if (i > 1) {
        sql += " , ";
      }
      i--;
    }
    sql += " )  values ( ";

    i = filterList.size();
    for (Tuple t : filterList) {

      sql += t.getY();
      if (i > 1) {
        sql += " , ";
      }
      i--;
    }
    sql += ");";
    System.out.println(sql);
    try {
      PreparedStatement preparedStatement = connect.prepareStatement(sql);
      preparedStatement.executeUpdate();
    } catch (Exception e) {
      System.out.println("\n******************************************\noops - something broke!! Check your syntax next time...\n******************************************\n");
    }
  }

  // You need to close the resultSet
  private void close() {
    try {
      connect.close();
    } catch (Exception e) {

    }
  }

  /**
   * Prompts user for name, password and connects.
   */
  public void start() throws SQLException {
    //Prompt user for username and password
    System.out.println("Type your username: ");
    String username = input.next();
    System.out.println("Type your password: ");
    String password = input.next();
    this.connectDatabase(username, password);
    System.out.println("Hello there");
  }

  Basketball_connection data;

  public static void main(String[] args) throws Exception {
    Basketball_connection data = new Basketball_connection();
    System.out.println("System starting up... \n\uD83E\uDD16 Boop Beep \uD83E\uDD16\n______________________________________________________________________________");
    data.start();
    data.run();
    //close the database
    System.out.println("\n______________________________________________________________________________\n");
    System.out.println("\nGOODBYE!\n\uD83D\uDE42");
    data.close();
  }
}



