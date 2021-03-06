package phate.GlobalShop;

public class ShopItem {
	public int id;
	public double price;
	public String name;
	public int amount;
	public int damage = -1;
	
	public ShopItem(int id, String name, int amount, double price) {
		this.id=id;
		this.price=price;
		this.name=name;
		this.amount=amount;
		this.damage=-1;
	}
	
	public ShopItem(int id, String name, int amount, double price, int damage) {
		this.id=id;
		this.price=price;
		this.name=name;
		this.amount=amount;
		this.damage=damage;
	}
	
	
	public boolean equals(Object o) {
		ShopItem obj = (ShopItem) o;
		if(obj.id==this.id) {
			if(this.damage!=-1) {
				if(this.damage==obj.damage) {
					return true;
				}
			}
			else {
				return true;
			}
		}
		
		if(this.name.equalsIgnoreCase(obj.name)) {
			if(this.damage==-1) {
				return true;
			}
			else {
				if(this.damage==obj.damage) {
					return true;
				}
			}
		}
		
		
		//items dont match
		return false;
	}
}
