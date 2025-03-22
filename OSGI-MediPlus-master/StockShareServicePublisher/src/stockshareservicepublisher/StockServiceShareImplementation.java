package stockshareservicepublisher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.hospital.core.database.IDatabaseService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class StockServiceShareImplementation implements StockShareService {

    private IDatabaseService dbService;
    private BundleContext context;
    private Connection connection;

    public StockServiceShareImplementation(BundleContext context) {
        this.context = context;
        initializeConnection();
    }

    private void initializeConnection() {
        try {
            ServiceReference<IDatabaseService> serviceReference = context.getServiceReference(IDatabaseService.class);
            if (serviceReference != null) {
                dbService = context.getService(serviceReference);
                if (dbService != null) {
                    connection = dbService.getConnection();
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to initialize database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean ensureConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                initializeConnection();
            }
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection issue: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean addStockShare(StockShare stockShare) {
        if (!ensureConnection()) return false;

        String sql = "INSERT INTO stock_shares (division, ward_id, item_name, category, quantity, provide_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, stockShare.getDivision());
            pstmt.setString(2, stockShare.getWardID());
            pstmt.setString(3, stockShare.getItemName());
            pstmt.setString(4, stockShare.getCategory());
            pstmt.setInt(5, stockShare.getQuantity());
            pstmt.setDate(6, stockShare.getProvideDate());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding stock share: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateStockShare(StockShare stockShare) {
        if (!ensureConnection()) return false;

        String sql = "UPDATE stock_shares SET division=?, ward_id=?, item_name=?, category=?, quantity=?, provide_date=? WHERE stockshare_id=?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, stockShare.getDivision());
            pstmt.setString(2, stockShare.getWardID());
            pstmt.setString(3, stockShare.getItemName());
            pstmt.setString(4, stockShare.getCategory());
            pstmt.setInt(5, stockShare.getQuantity());
            pstmt.setDate(6, stockShare.getProvideDate());
            pstmt.setInt(7, stockShare.getStockshareId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating stock share: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteStockShare(int stockShareId) {
        if (!ensureConnection()) return false;

        String sql = "DELETE FROM stock_shares WHERE stockshare_id=?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, stockShareId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting stock share: " + e.getMessage());
            return false;
        }
    }

    @Override
    public  StockShare getStockShareDetails(int stockShareId) {
        if (!ensureConnection()) return null;

        String sql = "SELECT * FROM stock_shares WHERE stockshare_id=?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, stockShareId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new StockShare(
                    rs.getInt("stockshareId"),
                    rs.getString("division"),
                    rs.getString("wardID"),
                    rs.getString("itemName"),
                    rs.getString("category"),
                    rs.getInt("quantity"),
                    rs.getDate("provideDate")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching stock share: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<StockShare> getAllStocksShare() {
        if (!ensureConnection()) return null;

        List<StockShare> stockShares = new ArrayList<>();
        String sql = "SELECT * FROM stock_shares";
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                stockShares.add(new StockShare(
                    rs.getInt("stockshare_id"),
                    rs.getString("division"),
                    rs.getString("ward_id"),
                    rs.getString("item_name"),
                    rs.getString("category"),
                    rs.getInt("quantity"),
                    rs.getDate("provide_date")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all stock shares: " + e.getMessage());
        }
        return stockShares;
    }

	

	@Override
	public List<StockShare> search(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StockShare getStockShare(int stockShareId) {
		// TODO Auto-generated method stub
		return null;
	}
}