package be.intecbrussel.shoppinglist.configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Scanner;

public class MakeDatabase {
    public static void main(String[] args) {
        String dbName = "shoppinglist";
        System.out.print("Deze functie overschrijft of maakt database " + dbName + " aan." +
                "\n(En linkt die aan bestaande user intec.)" +
                "\nBEN JE ZEKER?");
                System.out.print("\nWachtwoord voor root user: ");
        Scanner sc=new Scanner(System.in);
        String password = sc.nextLine();
        try(Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/?user=root&password=" +  password);
            Statement statement=connection.createStatement();  ){

            System.out.println(statement.executeUpdate("DROP DATABASE IF EXISTS " + dbName));
            System.out.println (statement.executeUpdate("CREATE DATABASE " + dbName) );
            System.out.println( statement.executeUpdate("grant all on " + dbName + ".* to 'intec'@'localhost'") );
            System.out.println( statement.executeUpdate("flush privileges") );
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
