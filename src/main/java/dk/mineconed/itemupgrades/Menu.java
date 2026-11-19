package dk.mineconed.itemupgrades;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 * Made by Mineconed
 *
 * The upgrade menu.
 */
public final class Menu implements InventoryHolder {

    final ItemStack foer;
    Inventory inv;

    Menu(ItemStack foer) {
        this.foer = foer;
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }
}
