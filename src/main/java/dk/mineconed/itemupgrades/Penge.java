package dk.mineconed.itemupgrades;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Made by Mineconed
 *
 * Vault money without needing Vault to build the plugin.
 */
public final class Penge {

    private Penge() {
    }

    private static Object oekonomi() {
        try {
            Class<?> ec = Class.forName("net.milkbowl.vault.economy.Economy");
            Object reg = Bukkit.getServicesManager().getClass().getMethod("getRegistration", Class.class)
                    .invoke(Bukkit.getServicesManager(), ec);
            return reg == null ? null : reg.getClass().getMethod("getProvider").invoke(reg);
        } catch (Throwable e) {
            return null;
        }
    }

    public static boolean findes() {
        return oekonomi() != null;
    }

    public static boolean har(Player p, double beloeb) {
        Object e = oekonomi();
        if (e == null) {
            return false;
        }
        try {
            return (Boolean) e.getClass().getMethod("has", OfflinePlayer.class, double.class).invoke(e, p, beloeb);
        } catch (Throwable ex) {
            return false;
        }
    }

    public static boolean traek(Player p, double beloeb) {
        Object e = oekonomi();
        if (e == null) {
            return false;
        }
        try {
            Object svar = e.getClass().getMethod("withdrawPlayer", OfflinePlayer.class, double.class).invoke(e, p, beloeb);
            return (Boolean) svar.getClass().getMethod("transactionSuccess").invoke(svar);
        } catch (Throwable ex) {
            return false;
        }
    }
}
