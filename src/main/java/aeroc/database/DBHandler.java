package database;


import java.sql.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import datatypes.Aircraft;

import datatypes.ICAOCodeBlock;

public class DBHandler {

	private static HashMap<String, Aircraft> hmapAircraft = new HashMap<String, Aircraft>();

	private List<ICAOCodeBlock> vBlocks = new Vector<ICAOCodeBlock>();

	Connection logCon = null;

	public static HashMap<String, Aircraft> getHmapAircraft() {
		return hmapAircraft;
	}

	public static void setHmapAircraft(HashMap<String, Aircraft> hmapAircraft) {
		DBHandler.hmapAircraft = hmapAircraft;
	}

	public Aircraft getAircraft(String hex) {

		Aircraft craft = null;

		try {

			craft = hmapAircraft.get(hex);

		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

		}

		return craft;
	}

	@SuppressWarnings("null")
	public ICAOCodeBlock getBlock(String hex) {

		ICAOCodeBlock result = null;

		int code = Integer.parseInt(hex, 16);

		Iterator<ICAOCodeBlock> iter = vBlocks.iterator();

		while (iter.hasNext()) {

			ICAOCodeBlock block = iter.next();

			if ((code & block.getSignificantBitMask()) == block.getBitMask()) {

				result = block;
				return result;

			}

		}

		return result;

	}

	public int loadDB(String path) {

		int ret = 0;
		Connection c = null;

		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection(path);

			ResultSet resultSet = null;
			Statement statement = null;

			statement = c.createStatement();
			resultSet = statement

					.executeQuery("SELECT * FROM AIRCRAFT");

			while (resultSet.next()) {

				Aircraft craft = new Aircraft();

				craft.setHex(resultSet.getString("ModeS"));
				craft.setType(resultSet.getString("ICAOTypeCode"));
				craft.setReg(resultSet.getString("Registration"));
				craft.setInterested(resultSet.getBoolean("Interested"));
				craft.setOperator(resultSet.getString("OperatorFlagCode"));

				hmapAircraft.put(craft.getHex(), craft);
			}
			
			ret = 1;

			

		} catch (Exception e) {
			
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

		} finally {

			try {
				c.close();
			} catch (SQLException e) {
	
				e.printStackTrace();
			}
		}
		
		
		
		return ret;

	}

	public int loadBlocks(String path) {

		int ret = 0;
		Connection c = null;

		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection(path);

			ResultSet resultSet = null;
			Statement statement = null;

			statement = c.createStatement();
			resultSet = statement

					.executeQuery("SELECT * FROM CodeBlockView order by SignificantBitMask DESC");

			while (resultSet.next()) {

				ICAOCodeBlock block = new ICAOCodeBlock();

				block.setBitMask(resultSet.getInt("BitMask"));
				block.setCodeBlockId(resultSet.getInt("CodeBlockId"));
				block.setCountryId(resultSet.getInt("CountryId"));
				block.setSignificantBitMask(resultSet.getInt("SignificantBitMask"));
				int mil = resultSet.getInt("IsMilitary");

				block.setCountry(resultSet.getString("Country"));

				if (mil == 1) {
					block.setIsMilitary(true);
				} else {
					block.setIsMilitary(false);
				}

				vBlocks.add(block);
			}

			ret = 1;

		} catch (Exception e) {
			
			System.err.println(e.getClass().getName() + ": " + e.getMessage());

		} finally {

			try {
				c.close();
			} catch (SQLException e) {
				
				e.printStackTrace();
			}
		}
		
		
		return ret;

	}

}
