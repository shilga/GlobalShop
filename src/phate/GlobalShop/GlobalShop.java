package phate.GlobalShop;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.avaje.ebeaninternal.server.lib.util.InvalidDataException;
import com.iConomy.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class GlobalShop extends JavaPlugin {
	public static final Logger log = Logger.getLogger("Minecraft");
	private Server server = null;
	private ArrayList<ShopItem> ShopItems = new ArrayList<ShopItem>();
	PluginDescriptionFile pdfFile = null;
	private double buysell=(double) 0.8;
	Configuration config;

	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
		
		log.info("[" + pdfFile.getName() + "] by Phate." + " Plugin Disabled. (version "
				+ pdfFile.getVersion() + ")");
	}

	@Override
	public void onEnable() {
		// TODO Auto-generated method stub
		server = getServer();
		
		config = getConfiguration();
		
		
		pdfFile=this.getDescription();
		log.info("[" + pdfFile.getName() + "] by Phate." + " Plugin Enabled. (version " + pdfFile.getVersion() + ")");
		
		if (this.server.getPluginManager().getPlugin("iConomy") != null) {
			log.info("[" + pdfFile.getName() + "] " + "Good. iConomy found.");
		}
		else {
			log.info("[" + pdfFile.getName() + "] " + "Please install iConomy. Disabling...");
			this.server.getPluginManager().disablePlugin(this);
		}
		
		log.info("[" + pdfFile.getName() + "] " + "Loading Configuration...");
		if(!this.loadConfig()) {
			this.server.getPluginManager().disablePlugin(this);
			log.info("[" + pdfFile.getName() + "] " + "Disabling...");
		}
		else {
			log.info("[" + pdfFile.getName() + "] " + "Configuration loaded.");
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel,
			String[] args) {
		
		String cmd = command.getName();
		Player player = (Player) sender;
		int amount = 1; 					//default amount of 1
		int searchid = -1;
		
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "You have to be a player!");
			return true;
		}
		
		
		// Below this are commands with 0 arguments:
		if (cmd.equalsIgnoreCase("gs_checksell")) {
			double price = this.getSellingPrice(player);
			if(price!= -1.0) {
				player.sendMessage("You will get " + ChatColor.YELLOW + iConomy.format(price) + ChatColor.WHITE +  " for that.");
			}
			return true;
		}
		
		if (cmd.equalsIgnoreCase("gs_sell")) {
			double price = this.getSellingPrice(player);
			ItemStack holdingitem = player.getItemInHand();
			
			if(price!= -1.0) {
				iConomy.getAccount(player.getName()).getHoldings().add(price);
				int itemnum;
				if(Material.getMaterial(holdingitem.getTypeId()).getMaxDurability()==-1) {
					itemnum = this.ShopItems.indexOf(new ShopItem(holdingitem.getTypeId(),"",0,0,holdingitem.getDurability()));
				}
				else {
					itemnum = this.ShopItems.indexOf(new ShopItem(holdingitem.getTypeId(),"",0,0));
				}
				
				String Itemtext=ShopItems.get(itemnum).name;
				player.sendMessage("You got " + ChatColor.YELLOW + iConomy.format(price) + ChatColor.WHITE + " for selling " + ChatColor.YELLOW + holdingitem.getAmount() + " of " + Itemtext.toLowerCase());
				log.info("[" + pdfFile.getName() + "] " + player.getName() + " sold " + holdingitem.getAmount() + " of "+Itemtext.toLowerCase() + " for "+iConomy.format(price) + " Dollars");
				
				player.setItemInHand(null);
			}
			return true;
		}
				
		//Bellow this are commands with min 1 argument:
		if (args.length < 1) {
			return false; //command arguments to less
		}
		
		//Handle, Parse and Check Arguments:
		try {
			searchid = Integer.parseInt(args[0]);
		}
		catch (NumberFormatException e) {
			//do nothing. that's ok
		}
		
		if(args.length >= 2) { //Is where an amount given?
			try {
				amount = Integer.parseInt(args[1]);
				if(amount<1) {
					player.sendMessage(ChatColor.RED + "Amount must be a number greater than 0!");
					return true;
				}
			}
			catch (NumberFormatException e) {
				player.sendMessage(ChatColor.RED + "Amount must be a number!");
				return true;
			}
		}
		
		int itemnum = this.ShopItems.indexOf(new ShopItem(searchid,args[0],0,0,-1));
		if(itemnum == -1) {
			player.sendMessage(ChatColor.RED+"This item is not for sale!");
			return true;
		}
		
		//Get the requested item as object
		ShopItem item = ShopItems.get(itemnum);
		
		if (cmd.equalsIgnoreCase("gs_buy")) { //Player wants to buy something
			ItemStack stack = new ItemStack(item.id, item.amount*amount, (short) (item.damage!=-1 ? item.damage : 0));
			
			if(iConomy.getAccount(player.getName()).getHoldings().hasEnough(item.price*amount)) { //Has the player enough money?
				if(player.getInventory().firstEmpty()!=-1) {
					player.sendMessage("You have bought " + ChatColor.YELLOW + stack.getAmount() + " of "+item.name + ChatColor.WHITE + " for " + ChatColor.YELLOW + iConomy.format(item.price*amount));			
					log.info("[" + pdfFile.getName() + "] " + player.getName() + " bought " + stack.getAmount() + " of "+item.name + " for "+iConomy.format(item.price*amount));
					
					iConomy.getAccount(player.getName()).getHoldings().subtract(item.price*amount); //Substract the money from players account
					player.getInventory().addItem(stack);
				}
				else {
					player.sendMessage(ChatColor.RED + "Your inventory is full!");
				}
			}
			else {
				player.sendMessage(ChatColor.RED + "You do not have enough money for that. You need at least "+iConomy.format(item.price*amount));
			}
			return true;
		}
			
		if (cmd.equalsIgnoreCase("gs_price")) { //Player wants to ask about the price

			player.sendMessage("You can buy " + ChatColor.YELLOW + item.amount + " of " + item.name + ChatColor.WHITE + " for " + ChatColor.YELLOW + iConomy.format(item.price));
			return true;
		}
		
		//Should never be reached
		return false;
	}
	
	public double getSellingPrice(Player player) {
		ItemStack holdingitem = player.getItemInHand();
		double price=-1;
		double condition;
			
		if(holdingitem != null && holdingitem.getTypeId()!=0) {
			
			int itemnum;
			
				if(Material.getMaterial(holdingitem.getTypeId()).getMaxDurability()==-1) {
					player.sendMessage("Durab: "+holdingitem.getDurability());
					itemnum = this.ShopItems.indexOf(new ShopItem(holdingitem.getTypeId(),"",0,0,holdingitem.getDurability()));
					condition = 1;
				}
				else {
					condition = 1 - ((double) holdingitem.getDurability())/((double) Material.getMaterial(holdingitem.getTypeId()).getMaxDurability());
					itemnum = this.ShopItems.indexOf(new ShopItem(holdingitem.getTypeId(),"",0,0));
				}
			if(itemnum!=-1) {
				price = ((double) this.ShopItems.get(itemnum).price) * this.buysell * ((double) holdingitem.getAmount())/ ((double) this.ShopItems.get(itemnum).amount)*condition;
			}
			else {
				player.sendMessage(ChatColor.RED + "This item can not be saled!");
			}
		}
		else {
			player.sendMessage(ChatColor.RED + "You must hold the item you want to sell in your hand.");
		}
		return price;
	}
	
	private Boolean loadConfig() {
		List<String> Stringlist = null;
		if (!(new File(this.getDataFolder(), "config.yml")).exists()){
			log.info("[" + pdfFile.getName() + "] " + "Configfile does not exist. Write out a default");
			this.loadDefaults();
			this.saveConfig();
			return true;
		}
		else {
			config.load();
			
			this.buysell = config.getDouble("buysell",this.buysell);
			Stringlist = config.getStringList("ShopItems", Stringlist);
			
        	try{
        		for (String s : Stringlist){
	        		String[] parts = s.split(":");
	        		if(parts.length == 4){
	        			ShopItems.add(new ShopItem(Integer.parseInt(parts[0]),parts[1],Integer.parseInt(parts[2]),Double.parseDouble(parts[3])));
	        		}
	        		else if(parts.length == 5){
	        			if(Material.getMaterial(Integer.parseInt(parts[0])).getMaxDurability()!=-1) {
	        				throw new InvalidDataException(parts[1]+" get's it's damage by using it.");
	        			}
	        			else {
		        			ShopItems.add(new ShopItem(Integer.parseInt(parts[0]),parts[1],Integer.parseInt(parts[2]),Double.parseDouble(parts[3]),Integer.parseInt(parts[4])));
	        			}
	           		}
	        		else {
	        			throw new InvalidDataException("Wrong size of arguments in ShopItems");
	        		}
        		}
        		return true;
        	}
        	catch(InvalidDataException e) {
        		log.info("[" + pdfFile.getName() + "] " + "Error reading ShopItems."+e.getMessage());
        		return false;
        	}
        	catch (Exception e) {
        		log.info("[" + pdfFile.getName() + "] " + "Error reading ShopItems. Your config.yml is incorrect.");
        		return false;
        	}
		}
	}
	
	private void loadDefaults() {
		this.buysell=(double) 0.8;
		
		this.ShopItems.add(new ShopItem(1,"stone",64,10));
		this.ShopItems.add(new ShopItem(4,"cobblestone",64,7));
		this.ShopItems.add(new ShopItem(278,"diamond_pickaxe",1,160));
		this.ShopItems.add(new ShopItem(35, "white_wool",64,14,0));
		this.ShopItems.add(new ShopItem(35, "black_wool",64,16,15));
	}
	
	private void saveConfig() {
		DecimalFormat df = new DecimalFormat( "0.00" );
		PrintWriter stream = null;
		File folder = this.getDataFolder();
		if (folder != null) {
			folder.mkdirs();
        }
		String folderName = folder.getParent();

		try {
			stream = new PrintWriter(folderName + "/GlobalShop/config.yml");
			//Let's write our goods ;)
				stream.println("#GlobalShop " + pdfFile.getVersion() + " by Phate");
				stream.println("#Configuration File");
				stream.println();
				stream.println("#Here the sale ratio is determined");
				stream.println("buysell: " + this.buysell);
				stream.println();
				stream.println("#Here the shops Item- and Pricelist is determined");
				stream.println("#This is how the items are defined:");
				stream.println("#- ItemID:ItemName:Amount:Price(:Damage/Color)");
				stream.println("ShopItems:");
				for (ShopItem item : ShopItems){
					stream.println("- " + item.id + ":" + item.name + ":" + item.amount + ":" + df.format(item.price) + (item.damage!=-1 ? ":" + item.damage : ""));
				}
				
				stream.close();
		}
		catch (FileNotFoundException e) {
			log.info("[" + pdfFile.getName() + "] " + "Error Saving Config File");
		}
	}
}
