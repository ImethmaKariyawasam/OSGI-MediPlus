package stockshareservicepublisher;

import java.util.List;


public interface StockShareService {

    boolean addStockShare(StockShare stockShare);
    boolean updateStockShare(StockShare stockShare);
    boolean deleteStockShare(int stockShareId);
    StockShare getStockShare(int stockShareId); // Changed return type to StockShare
    StockShare getStockShareDetails(int stockShareId); // Fixed method name typo
    List<StockShare> search(String value);
    List<StockShare> getAllStocksShare(); // Added method to fetch all stocks

}
