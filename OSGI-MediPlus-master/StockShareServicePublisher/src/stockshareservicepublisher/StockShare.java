package stockshareservicepublisher;

import java.sql.Date;

public class StockShare {

	private int stockshareId;
	private String division;
	private String wardID;
	private String itemName;
	private String category;
	private int quantity;
	private Date provideDate;
	
	
	public StockShare(int stockshareId, String division, String wardID, String itemName, String category, int quantity,
			Date provideDate) {
		super();
		this.stockshareId = stockshareId;
		this.division = division;
		this.wardID = wardID;
		this.itemName = itemName;
		this.category = category;
		this.quantity = quantity;
		this.provideDate = provideDate;
	}
	

	public StockShare( String division, String wardID, String itemName, String category, int quantity,
			Date provideDate) {
		super();
		this.division = division;
		this.wardID = wardID;
		this.itemName = itemName;
		this.category = category;
		this.quantity = quantity;
		this.provideDate = provideDate;
	}
	
	
	public int getStockshareId() {
		return stockshareId;
	}
	public void setStockshareId(int stockshareId) {
		this.stockshareId = stockshareId;
	}
	public String getDivision() {
		return division;
	}
	public void setDivision(String division) {
		this.division = division;
	}
	public String getWardID() {
		return wardID;
	}
	public void setWardID(String wardID) {
		this.wardID = wardID;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public Date getProvideDate() {
		return provideDate;
	}
	public void setProvideDate(Date provideDate) {
		this.provideDate = provideDate;
	}
	
}
