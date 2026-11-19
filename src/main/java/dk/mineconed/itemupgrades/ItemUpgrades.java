package dk.mineconed.itemupgrades;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Made by Mineconed
 *
 * ItemUpgrades: upgrade swords, tools and armor in a menu with XP or money.
 */
public final class ItemUpgrades extends JavaPlugin implements Listener {

    /** Hidden color codes that mark our lore line. */
    static final String MAERKE = "§1§2§3§r";

    private static final Map<String, String> GAMMEL = new HashMap<String, String>();

    static {
        String[][] par = {
            {"SHARPNESS", "DAMAGE_ALL"}, {"SMITE", "DAMAGE_UNDEAD"}, {"BANE_OF_ARTHROPODS", "DAMAGE_ARTHROPODS"},
            {"EFFICIENCY", "DIG_SPEED"}, {"UNBREAKING", "DURABILITY"}, {"FORTUNE", "LOOT_BONUS_BLOCKS"},
            {"LOOTING", "LOOT_BONUS_MOBS"}, {"PROTECTION", "PROTECTION_ENVIRONMENTAL"}, {"FIRE_PROTECTION", "PROTECTION_FIRE"},
            {"FEATHER_FALLING", "PROTECTION_FALL"}, {"BLAST_PROTECTION", "PROTECTION_EXPLOSIONS"},
            {"PROJECTILE_PROTECTION", "PROTECTION_PROJECTILE"}, {"RESPIRATION", "OXYGEN"}, {"AQUA_AFFINITY", "WATER_WORKER"},
            {"POWER", "ARROW_DAMAGE"}, {"PUNCH", "ARROW_KNOCKBACK"}, {"FLAME", "ARROW_FIRE"}, {"INFINITY", "ARROW_INFINITE"},
            {"LUCK_OF_THE_SEA", "LUCK"}, {"SWEEPING_EDGE", "SWEEPING"}
        };
        for (String[] p : par) {
            GAMMEL.put(p[0], p[1]);
        }
    }

    private Beskeder msg;
    private final Random rnd = new Random();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        msg = new Beskeder(this);
        Bukkit.getPluginManager().registerEvents(this, this);
        if (getConfig().getString("cost_type", "xp").equalsIgnoreCase("money") && !Penge.findes()) {
            getLogger().warning("cost_type is money but Vault (with an economy plugin) is not found. Using XP.");
        }
        getLogger().info("ItemUpgrades is enabled. Made by Mineconed.");
    }

    // ═══════════════ ENCHANTS ═══════════════

    @SuppressWarnings("deprecation")
    static Enchantment enchant(String navn) {
        String n = navn.toUpperCase();
        String g = GAMMEL.get(n);
        for (String forsoeg : g == null ? new String[]{n} : new String[]{n, g}) {
            try {
                Object o = Enchantment.class.getField(forsoeg).get(null);
                if (o instanceof Enchantment) {
                    return (Enchantment) o;
                }
            } catch (Throwable ignore) {
                // try next
            }
            try {
                Enchantment e = Enchantment.getByName(forsoeg);
                if (e != null) {
                    return e;
                }
            } catch (Throwable ignore) {
                // try next
            }
        }
        return null;
    }

    private static String paent(String s) {
        StringBuilder sb = new StringBuilder();
        for (String d : s.toLowerCase().split("_")) {
            if (!d.isEmpty()) {
                sb.append(sb.length() > 0 ? " " : "").append(Character.toUpperCase(d.charAt(0))).append(d.substring(1));
            }
        }
        return sb.toString();
    }

    /** The category in the config for this item, or null. */
    private String kategori(ItemStack i) {
        if (i == null || i.getType() == Material.AIR) {
            return null;
        }
        String n = i.getType().name();
        ConfigurationSection cs = getConfig().getConfigurationSection("categories");
        if (cs == null) {
            return null;
        }
        for (String k : cs.getKeys(false)) {
            for (String slut : cs.getStringList(k + ".items")) {
                String s = slut.toUpperCase();
                if (n.equals(s) || n.endsWith("_" + s)) {
                    return k;
                }
            }
        }
        return null;
    }

    private List<String> enchantsFor(String kat) {
        return getConfig().getStringList("categories." + kat + ".enchants");
    }

    // ═══════════════ NIVEAU ═══════════════

    static int niveau(ItemStack i) {
        if (i == null || !i.hasItemMeta() || !i.getItemMeta().hasLore()) {
            return 0;
        }
        for (String l : i.getItemMeta().getLore()) {
            if (l.startsWith(MAERKE)) {
                String ren = ChatColor.stripColor(l.substring(MAERKE.length()));
                StringBuilder tal = new StringBuilder();
                for (char c : ren.toCharArray()) {
                    if (Character.isDigit(c)) {
                        tal.append(c);
                    } else if (tal.length() > 0) {
                        break;
                    }
                }
                try {
                    return tal.length() == 0 ? 0 : Integer.parseInt(tal.toString());
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    private int maks() {
        return Math.max(1, getConfig().getInt("max_level", 10));
    }

    private void saetNiveau(ItemStack i, int n) {
        ItemMeta m = i.getItemMeta();
        List<String> lore = m.hasLore() ? new ArrayList<String>(m.getLore()) : new ArrayList<String>();
        for (int x = Math.subtractExact(lore.size(), 1); x >= 0; x = Math.subtractExact(x, 1)) {
            if (lore.get(x).startsWith(MAERKE)) {
                lore.remove(x);
            }
        }
        if (n > 0) {
            lore.add(MAERKE + msg.c("lore_level", "{level}", String.valueOf(n), "{max}", String.valueOf(maks()), "{stars}", stjerner(n)));
        }
        m.setLore(lore);
        i.setItemMeta(m);
    }

    private String stjerner(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maks(); i++) {
            sb.append(i < n ? msg.c("star_full") : msg.c("star_empty"));
        }
        return sb.toString();
    }

    private void anvend(ItemStack i, String kat, int til) {
        int fra = niveau(i);
        int plus = Math.subtractExact(til, fra);
        for (String en : enchantsFor(kat)) {
            Enchantment e = enchant(en);
            if (e == null) {
                continue;
            }
            int nu = i.getEnchantmentLevel(e);
            int ny = Math.max(0, nu + plus);
            if (ny <= 0) {
                i.removeEnchantment(e);
            } else {
                i.addUnsafeEnchantment(e, ny);
            }
        }
        saetNiveau(i, til);
    }

    private int pris(int naesteNiveau) {
        return getConfig().getInt("cost.base", 5) + getConfig().getInt("cost.per_level", 5) * Math.subtractExact(naesteNiveau, 1);
    }

    private int chance(int naesteNiveau) {
        List<Integer> l = getConfig().getIntegerList("success_chance");
        if (l == null || l.isEmpty()) {
            return 100;
        }
        int idx = Math.min(Math.subtractExact(naesteNiveau, 1), Math.subtractExact(l.size(), 1));
        return Math.max(0, Math.min(100, l.get(idx)));
    }

    private boolean penge() {
        return getConfig().getString("cost_type", "xp").equalsIgnoreCase("money") && Penge.findes();
    }

    private String prisTekst(int p) {
        return msg.c(penge() ? "cost_money" : "cost_xp", "{amount}", String.valueOf(p));
    }

    private boolean harRaad(Player p, int pris) {
        if (p.hasPermission("itemupgrades.free")) {
            return true;
        }
        return penge() ? Penge.har(p, pris) : p.getLevel() >= pris;
    }

    private boolean betal(Player p, int pris) {
        if (p.hasPermission("itemupgrades.free")) {
            return true;
        }
        if (penge()) {
            return Penge.traek(p, pris);
        }
        if (p.getLevel() < pris) {
            return false;
        }
        p.setLevel(Math.subtractExact(p.getLevel(), pris));
        return true;
    }

    // ═══════════════ MENU ═══════════════

    @SuppressWarnings("deprecation")
    private static ItemStack mat(String ny, String gammel, int d) {
        Material m = Material.matchMaterial(ny);
        if (m != null) {
            return new ItemStack(m, 1);
        }
        m = Material.matchMaterial(gammel);
        return new ItemStack(m == null ? Material.STONE : m, 1, (short) d);
    }

    private static ItemStack ikon(ItemStack i, String navn, List<String> lore) {
        ItemMeta m = i.getItemMeta();
        if (m != null) {
            m.setDisplayName(navn);
            m.setLore(lore);
            try {
                m.addItemFlags(ItemFlag.values());
            } catch (Throwable ignore) {
                // ok
            }
            i.setItemMeta(m);
        }
        return i;
    }

    @SuppressWarnings("deprecation")
    void aabn(Player p) {
        ItemStack h = p.getItemInHand();
        String kat = kategori(h);
        if (kat == null) {
            msg.send(p, "cannot_upgrade");
            return;
        }
        int nu = niveau(h);
        Menu m = new Menu(h.clone());
        String t = msg.c("title");
        Inventory inv = Bukkit.createInventory(m, 27, t.length() > 32 ? t.substring(0, 32) : t);
        m.inv = inv;
        ItemStack glas = ikon(mat("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", 7), " ", null);
        for (int s = 0; s < 27; s++) {
            inv.setItem(s, glas);
        }
        inv.setItem(13, h.clone());
        if (nu >= maks()) {
            inv.setItem(15, ikon(mat("RED_WOOL", "WOOL", 14), msg.c("button_max"), msg.linjer("lore_max")));
            p.openInventory(inv);
            return;
        }
        int naeste = nu + 1;
        int pris = pris(naeste);
        List<String> bonus = new ArrayList<String>();
        for (String en : enchantsFor(kat)) {
            Enchantment e = enchant(en);
            if (e != null) {
                bonus.add(msg.c("bonus_line", "{enchant}", paent(en), "{level}", String.valueOf(h.getEnchantmentLevel(e) + 1)));
            }
        }
        List<String> info = msg.linjer("lore_info", "{level}", String.valueOf(nu), "{next}", String.valueOf(naeste),
                "{max}", String.valueOf(maks()), "{cost}", prisTekst(pris), "{chance}", String.valueOf(chance(naeste)));
        info.add(" ");
        info.add(msg.c("bonus_header"));
        info.addAll(bonus);
        inv.setItem(11, ikon(new ItemStack(Material.BOOK), msg.c("button_info"), info));
        boolean raad = harRaad(p, pris);
        inv.setItem(15, ikon(raad ? mat("LIME_WOOL", "WOOL", 5) : mat("RED_WOOL", "WOOL", 14),
                msg.c(raad ? "button_upgrade" : "button_no_money"), msg.linjer("lore_upgrade", "{cost}", prisTekst(pris),
                        "{chance}", String.valueOf(chance(naeste)))));
        p.openInventory(inv);
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onClick(InventoryClickEvent e) {
        if (!(e.getView().getTopInventory().getHolder() instanceof Menu) || !(e.getWhoClicked() instanceof Player)) {
            return;
        }
        e.setCancelled(true);
        if (e.getRawSlot() != 15) {
            return;
        }
        Menu m = (Menu) e.getView().getTopInventory().getHolder();
        Player p = (Player) e.getWhoClicked();
        ItemStack h = p.getItemInHand();
        if (h == null || !h.isSimilar(m.foer) || h.getAmount() != m.foer.getAmount()) {
            p.closeInventory();
            msg.send(p, "item_changed");
            return;
        }
        String kat = kategori(h);
        int nu = niveau(h);
        if (kat == null || nu >= maks()) {
            return;
        }
        int naeste = nu + 1;
        int pris = pris(naeste);
        if (!harRaad(p, pris) || !betal(p, pris)) {
            msg.send(p, "no_money", "{cost}", prisTekst(pris));
            Visning.lyd(p, "ENTITY_VILLAGER_NO", "VILLAGER_NO");
            return;
        }
        if (rnd.nextInt(100) < chance(naeste)) {
            anvend(h, kat, naeste);
            p.setItemInHand(h);
            msg.send(p, "success", "{level}", String.valueOf(naeste));
            Visning.lyd(p, "BLOCK_ANVIL_USE", "ANVIL_USE");
            if (naeste >= maks() && getConfig().getBoolean("broadcast_max", true)) {
                Bukkit.broadcastMessage(msg.c("prefix") + msg.c("max_broadcast", "{player}", p.getName(),
                        "{item}", paent(h.getType().name())));
            }
        } else {
            if (getConfig().getBoolean("downgrade_on_fail", false) && nu > 0) {
                anvend(h, kat, Math.subtractExact(nu, 1));
                p.setItemInHand(h);
                msg.send(p, "fail_downgrade", "{level}", String.valueOf(Math.subtractExact(nu, 1)));
            } else {
                msg.send(p, "fail");
            }
            Visning.lyd(p, "BLOCK_ANVIL_DESTROY", "ANVIL_BREAK");
        }
        aabn(p);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getView().getTopInventory().getHolder() instanceof Menu) {
            e.setCancelled(true);
        }
    }

    // ═══════════════ KOMMANDO ═══════════════

    @Override
    @SuppressWarnings("deprecation")
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] a) {
        String a1 = a.length > 0 ? a[0].toLowerCase() : "";
        if (a1.equals("reload")) {
            if (!s.hasPermission("itemupgrades.admin")) {
                msg.send(s, "no_permission");
                return true;
            }
            reloadConfig();
            msg.laes();
            msg.send(s, "reloaded");
            return true;
        }
        if (!(s instanceof Player)) {
            s.sendMessage("Only players can use this.");
            return true;
        }
        Player p = (Player) s;
        if (a1.equals("setlevel")) {
            if (!p.hasPermission("itemupgrades.admin")) {
                msg.send(p, "no_permission");
                return true;
            }
            ItemStack h = p.getItemInHand();
            String kat = kategori(h);
            if (kat == null || a.length < 2) {
                msg.send(p, "cannot_upgrade");
                return true;
            }
            try {
                int n = Math.max(0, Math.min(maks(), Integer.parseInt(a[1])));
                anvend(h, kat, n);
                p.setItemInHand(h);
                msg.send(p, "level_set", "{level}", String.valueOf(n));
            } catch (NumberFormatException e) {
                msg.send(p, "not_number");
            }
            return true;
        }
        if (!p.hasPermission("itemupgrades.use")) {
            msg.send(p, "no_permission");
            return true;
        }
        aabn(p);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String label, String[] a) {
        List<String> ud = new ArrayList<String>();
        if (a.length == 1 && s.hasPermission("itemupgrades.admin")) {
            for (String v : new String[]{"setlevel", "reload"}) {
                if (v.startsWith(a[0].toLowerCase())) {
                    ud.add(v);
                }
            }
        }
        return ud;
    }
}
