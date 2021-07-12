package com.atlasmc.desertdweller.cooking.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.atlasmc.desertdweller.cooking.Cooking;
import com.atlasmc.desertdweller.cooking.customfood.CustomFoodItem;
import com.atlasmc.desertdweller.cooking.events.CustomFoodEatenEvent;

import de.tr7zw.nbtapi.NBTItem;

public class FoodEating implements Listener{
	
	@EventHandler
	private void onEat(PlayerItemConsumeEvent e) {
		ItemStack eatenItem = e.getItem();
		NBTItem nbti = new NBTItem(eatenItem);
		if(nbti.hasKey("Plugin") && nbti.getString("Plugin").equals("DesertsCooking") && e.getPlayer().hasPermission("cooking.eat")) {
			CustomFoodItem item = new CustomFoodItem(eatenItem);
			if(item.completed) {
				e.setCancelled(true);
				CustomFoodEatenEvent event = new CustomFoodEatenEvent(e.getItem(), e.getPlayer());
				Bukkit.getServer().getPluginManager().callEvent(event);
				if(event.isCancelled())
					return;
				float modifier = item.flavor.efficiency(Cooking.preferences.get(e.getPlayer().getUniqueId()));
				e.getPlayer().setFoodLevel(e.getPlayer().getFoodLevel() + (int) (item.food * modifier));
				e.getPlayer().setSaturation(e.getPlayer().getSaturation() + item.saturation * modifier);
				if(item.poisoned) {
					e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.POISON,1200,4));
				}
				item.flavor.eatenPlayerMessage(e.getPlayer());
				//Remove 1 from amount.
				if(item.item.getAmount() <= 1) {
					ItemStack newItem = new ItemStack(Material.AIR);
					if(e.getPlayer().getInventory().getItemInMainHand().equals(eatenItem)) { 
						e.getPlayer().getInventory().setItemInMainHand(newItem);
					}else if(e.getPlayer().getInventory().getItemInOffHand().equals(eatenItem)) {
						e.getPlayer().getInventory().setItemInOffHand(newItem);
					}
				}else {
					if(e.getPlayer().getInventory().getItemInMainHand().equals(eatenItem)) { 
						eatenItem.setAmount(eatenItem.getAmount() - 1);
						e.getPlayer().getInventory().setItemInMainHand(eatenItem);
					}else if(e.getPlayer().getInventory().getItemInOffHand().equals(eatenItem)) {
						eatenItem.setAmount(eatenItem.getAmount() - 1);
						e.getPlayer().getInventory().setItemInOffHand(eatenItem);
					}
				}
			}else if(!item.invalidItem) {
				e.setCancelled(true);
				e.getPlayer().sendMessage(ChatColor.RED + "That item is not completed! Put it into an oven, stove or cooking pot to finish it.");
			}else if(item.invalidItem) {
				e.setCancelled(true);
				e.getPlayer().sendMessage(ChatColor.GOLD + "This item is invalid, please report this to an Admin.");
			}
		}
	}
}
