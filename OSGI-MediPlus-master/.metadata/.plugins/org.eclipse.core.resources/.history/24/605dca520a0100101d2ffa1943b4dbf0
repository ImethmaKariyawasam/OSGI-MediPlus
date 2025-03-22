package stockservicepublisher;

import java.util.List;

public interface StockService {

	boolean addStocks(Stock stock);
	boolean updateStock(Stock stock);
	boolean deleteStock(int id);
	boolean getStock(int id);
	String getStockDetails(int id);
	List<Stock> searchStock(String value);
    List<Stock> getAllStocks(); // Added method to fetch all stocks
    List<Stock> getStockWithShare(String itemName, String category); // ✅ Make sure this exists
	boolean updatstockwithshare(Stock stock);

}
