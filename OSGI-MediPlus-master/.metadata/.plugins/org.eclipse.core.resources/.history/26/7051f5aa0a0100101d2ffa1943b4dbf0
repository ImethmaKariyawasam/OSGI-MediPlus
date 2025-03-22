package stockservicepublisher;

import java.util.List;
import com.hospital.core.database.IDatabaseService;
import java.lang.reflect.ParameterizedType;
import java.sql.*;
import java.util.ArrayList;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class StockServiceImplementation implements StockService {
	
	private IDatabaseService dbService;
    private BundleContext context;
    private Connection connection;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;
    private static boolean EXISTS=false;

    public StockServiceImplementation(BundleContext context) {
        this.context = context;
        initializeConnection();
    }

    private void initializeConnection() {
        ServiceReference serviceReference = context.getServiceReference(IDatabaseService.class.getName());
        if (serviceReference != null) {
            dbService = (IDatabaseService) context.getService(serviceReference);
            try {
                connection = dbService.getConnection();
            } catch (SQLException e) {
                System.err.println("Failed to initialize database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private boolean ensureConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                initializeConnection();  // Reinitialize the connection if it's closed or null
            }
            // Check if the connection is open and valid
            if (connection != null && !connection.isClosed()) {
                try (Statement stmt = connection.createStatement()) {
                    // Simple query to test the connection
                    stmt.executeQuery("SELECT 1");
                    return true;  // Connection is valid
                } catch (SQLException e) {
                    System.err.println("Error executing test query: " + e.getMessage());
                    return false;  // Connection is not valid
                }
            } else {
                System.err.println("Connection is null or closed.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error checking connection status: " + e.getMessage());
            return false;  // Connection is not valid or couldn't be established
        }
    }




    private <T> T executeWithRetry(DatabaseOperation<T> operation) {
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                if (!ensureConnection()) {
                    throw new SQLException("Failed to establish database connection");
                }
                return operation.execute(connection);
            } catch (SQLNonTransientConnectionException | SQLRecoverableException e) {
                attempts++;
                System.err.println("Connection error on attempt " + attempts + ": " + e.getMessage());
                if (attempts < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                        initializeConnection();
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (SQLException e) {
                System.err.println("SQL error: " + e.getMessage());
                return operation.handleError(e);
            }
        }
        return operation.handleError(new SQLException("Max retry attempts reached"));
    }
    
    @FunctionalInterface
    private interface DatabaseOperation<T> {
        T execute(Connection conn) throws SQLException;
        @SuppressWarnings("unchecked")
		default T handleError(SQLException e) {
            e.printStackTrace();
            if (Boolean.class.equals(((Class<?>) ((ParameterizedType) getClass()
                .getGenericInterfaces()[0]).getActualTypeArguments()[0]))) {
                return (T) Boolean.FALSE;
            }
            return null;
        }
    }

    
    @Override
    public boolean addStocks(Stock stock) {
        return executeWithRetry(conn -> {
        	String sql = "INSERT INTO stocks(item_name, category, quantity, unit_price, supplier, expiry_date, last_updated) VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, stock.getItemName());
                pstmt.setString(2, stock.getCategory());
                pstmt.setInt(3, stock.getQuantity());
                pstmt.setDouble(4, stock.getUnitPrice());
                pstmt.setString(5, stock.getSupplier());
                pstmt.setDate(6, stock.getExpiryDate());
                pstmt.setDate(7, stock.getLastUpdated());

                int affectedRows = pstmt.executeUpdate();
                return affectedRows > 0; // Returns true if the insert was successful
            }
        });
    }


    @Override
    public boolean updateStock(Stock stock) {
        return executeWithRetry(conn -> {
            String sql = "UPDATE stocks SET item_name=?, category=?, quantity=?, unit_price=?, supplier=?, expiry_date=?, last_updated=? WHERE item_id=?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, stock.getItemName());
                pstmt.setString(2, stock.getCategory());
                pstmt.setInt(3, stock.getQuantity());
                pstmt.setDouble(4, stock.getUnitPrice());
                pstmt.setString(5, stock.getSupplier());
                pstmt.setDate(6, stock.getExpiryDate());
                pstmt.setDate(7, stock.getLastUpdated());
                pstmt.setInt(8, stock.getItemId());
                return pstmt.executeUpdate() > 0;
            }
        });
    }
    
    
    public boolean updatstockwithshare(Stock stock) {
        return executeWithRetry(conn -> {
            String sql = "UPDATE stocks SET quantity=? WHERE item_name=? AND category=?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, stock.getQuantity());   // Corrected index (1-based)
                pstmt.setString(2, stock.getItemName()); // Corrected index (1-based)
                pstmt.setString(3, stock.getCategory()); // Corrected index (1-based)
                return pstmt.executeUpdate() > 0;
            }
        });
    }


    @Override
    public boolean deleteStock(int id) {
        return executeWithRetry(conn -> {
            String sql = "DELETE FROM stocks WHERE item_id=?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                return pstmt.executeUpdate() > 0;
            }
        });
    }

    @Override
    public List<Stock> getAllStocks() {
        return executeWithRetry(conn -> {
            String sql = "SELECT * FROM stocks ";
            List<Stock> stockList = new ArrayList<>();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    stockList.add(new Stock(
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getString("category"),
                        rs.getInt("quantity"),
                        rs.getDouble("unit_price"),
                        rs.getString("supplier"),
                        rs.getDate("expiry_date"),
                        rs.getDate("last_updated")
                    ));
                }
            }
            return stockList;
        });
    }
    
    @Override
    public List<Stock> getStockWithShare(String itemsname, String category) {
        return executeWithRetry(conn -> {
            String sql = "SELECT  item_name,category,quantity FROM stocks WHERE item_name = ? AND category = ?";
            List<Stock> stockList = new ArrayList<>();
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, itemsname);
                pstmt.setString(2, category);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        stockList.add(new Stock(
                            rs.getString("item_name"),
                            rs.getString("category"),
                            rs.getInt("quantity")
                        ));
                    }
                }
            }
            return stockList;
        });
    }




	@Override
	public String getStockDetails(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Stock> searchStock(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getStock(int id) {
		// TODO Auto-generated method stub
		return false;
	}

}
