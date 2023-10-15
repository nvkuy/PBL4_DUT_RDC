

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Scanner;


public class GenKeyAndSave {
	
	// TODO: Add to be a feature of admin later..
	
	private static final String DB_URL = "jdbc:mysql://127.0.0.2:3306/rdc";
	private static final String USER_NAME = "root";
	private static final String PASSWORD = "";

	public static void main(String[] args) {

		try {
			
			Connection conn = DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);
			String insertSQL = "INSERT INTO _COMP VALUES (?, ?)";
			PreparedStatement preparedStmt = conn.prepareStatement(insertSQL);
			
			Scanner scanner = new Scanner(System.in);
			
			while (true) {
				
				System.out.println("Continue? (Y/N)");
				String choice;
				choice = scanner.nextLine();
				if (!choice.toUpperCase().equals("Y")) break;
				
				RSA rsa = new RSA();
				System.out.println("Computer ID: ");
				String compID = scanner.nextLine();
				
				System.out.println("Sure? (Y/N)");
				choice = scanner.nextLine();
				if (!choice.toUpperCase().equals("Y")) continue;
				
				String pubKey = rsa.getPublicKeyStr();
				String priKey = rsa.getPrivateKeyStr();
				
				System.out.println(compID);
				System.out.println(pubKey);
				System.out.println(priKey);
				
				preparedStmt.setString(1, compID);
				preparedStmt.setString(2, pubKey);
				
				if (preparedStmt.executeUpdate() > 0) 
					System.out.println("Success!");
				else
					System.out.println("Error!");
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		

	}

}
