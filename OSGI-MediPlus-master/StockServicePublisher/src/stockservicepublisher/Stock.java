package stockservicepublisher;

import java.sql.Date;

public class Stock {
	private int itemId;
	private String itemName;
	private String category;
	private int quantity;
	private double unitPrice;
	private String supplier;
	private Date expiryDate;
	private Date lastUpdated;
	
	public Stock() {}

	public Stock(int itemId, String itemName, String category, int quantity, double unitPrice, String supplier,
			Date expiryDate, Date lastUpdated) {
		this.itemId = itemId;
		this.itemName = itemName;
		this.category = category;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.supplier = supplier;
		this.expiryDate = expiryDate;
		this.lastUpdated = lastUpdated;
	}
	
	public Stock( String itemName, String category, int quantity, double unitPrice, String supplier,
			Date expiryDate, Date lastUpdated) {
		this.itemName = itemName;
		this.category = category;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.supplier = supplier;
		this.expiryDate = expiryDate;
		this.lastUpdated = lastUpdated;
	}
	
	public Stock( String itemName, String category, int quantity) {
		this.itemName = itemName;
		this.category = category;
	    this.quantity = quantity;
	}
	
	


	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
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

	public double getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(double unitPrice) {
		this.unitPrice = unitPrice;
	}

	public String getSupplier() {
		return supplier;
	}

	public void setSupplier(String supplier) {
		this.supplier = supplier;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	
	
}
